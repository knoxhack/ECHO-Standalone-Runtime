package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoDataJson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Machine-readable Ashfall standalone parity contract.
 *
 * <p>Loads {@code ashfall-standalone-parity-checklist.json} from the classpath
 * and exposes the required content IDs and domains. The runtime uses this to
 * report exactly what is missing by ID/domain instead of vague "content missing"
 * errors.
 */
public final class EchoAshfallParityContract {

    public static final String CHECKLIST_RESOURCE = "ashfall-standalone-parity-checklist.json";
    public static final String SCHEMA = "echo.standalone.parity_checklist.v1";

    private final Map<String, Object> raw;
    private final List<String> requiredNamespaces;
    private final List<Domain> domains;
    private final Map<String, Domain> domainById;
    private final Map<String, MetadataRule> metadataRules;

    public EchoAshfallParityContract() {
        this(loadResource(CHECKLIST_RESOURCE));
    }

    @SuppressWarnings("unchecked")
    public EchoAshfallParityContract(String jsonText) {
        Objects.requireNonNull(jsonText, "jsonText");
        Object parsed = EchoDataJson.parse(jsonText);
        if (!(parsed instanceof Map)) {
            throw new IllegalArgumentException("Parity checklist must be a JSON object");
        }
        this.raw = Collections.unmodifiableMap((Map<String, Object>) parsed);
        String schema = string(raw.get("schema"));
        if (!SCHEMA.equals(schema)) {
            throw new IllegalArgumentException("Unsupported parity checklist schema: " + schema);
        }
        this.requiredNamespaces = Collections.unmodifiableList(stringList(raw.get("requiredNamespaces")));
        this.domains = Collections.unmodifiableList(parseDomains(raw.get("domains")));
        LinkedHashMap<String, Domain> byId = new LinkedHashMap<>();
        for (Domain domain : domains) {
            byId.put(domain.id(), domain);
        }
        this.domainById = Collections.unmodifiableMap(byId);
        this.metadataRules = Collections.unmodifiableMap(parseMetadataRules(raw.get("metadataRules")));
    }

    public List<String> requiredNamespaces() {
        return requiredNamespaces;
    }

    public List<Domain> domains() {
        return domains;
    }

    public List<String> domainIds() {
        return domains.stream().map(Domain::id).toList();
    }

    public List<String> requiredIdsForDomain(String domainId) {
        Domain domain = domainById.get(domainId);
        return domain == null ? List.of() : domain.requiredIds();
    }

    public boolean hasDomain(String domainId) {
        return domainById.containsKey(domainId);
    }

    public MetadataRule metadataRule(String ruleId) {
        return metadataRules.get(ruleId);
    }

    /**
     * Validates an audit report and returns a structured result.
     *
     * <p>The report is expected to contain a "domains" map where each domain has
     * "present", "partial", "missing", and "unsupported" id lists.
     */
    public ValidationResult validateReport(Map<String, Object> report) {
        Objects.requireNonNull(report, "report");
        Map<String, Object> reportDomains = map(report.get("domains"));
        List<DomainResult> results = new ArrayList<>();
        List<String> globalMissing = new ArrayList<>();
        List<String> globalPartial = new ArrayList<>();
        List<String> globalUnsupported = new ArrayList<>();
        boolean pass = true;

        for (Domain domain : domains) {
            Map<String, Object> domainReport = map(reportDomains.get(domain.id()));
            Set<String> present = stringSet(domainReport.get("present"));
            Set<String> partial = stringSet(domainReport.get("partial"));
            Set<String> missing = stringSet(domainReport.get("missing"));
            Set<String> unsupported = stringSet(domainReport.get("unsupported"));

            List<String> domainMissing = new ArrayList<>();
            List<String> domainPartial = new ArrayList<>();
            List<String> domainUnsupported = new ArrayList<>();

            for (String requiredId : domain.requiredIds()) {
                if (unsupported.contains(requiredId)) {
                    domainUnsupported.add(requiredId);
                } else if (missing.contains(requiredId) || !present.contains(requiredId)) {
                    domainMissing.add(requiredId);
                } else if (partial.contains(requiredId)) {
                    domainPartial.add(requiredId);
                }
            }

            boolean domainPass = domainMissing.isEmpty()
                    && domainUnsupported.isEmpty()
                    && domainPartial.isEmpty();
            pass &= domainPass;

            globalMissing.addAll(domainMissing);
            globalPartial.addAll(domainPartial);
            globalUnsupported.addAll(domainUnsupported);

            results.add(new DomainResult(
                    domain.id(),
                    domain.label(),
                    domainPass,
                    domainMissing,
                    domainPartial,
                    domainUnsupported
            ));
        }

        return new ValidationResult(
                pass,
                Collections.unmodifiableList(results),
                Collections.unmodifiableList(globalMissing),
                Collections.unmodifiableList(globalPartial),
                Collections.unmodifiableList(globalUnsupported)
        );
    }

    /**
     * Convenience view returning only the missing required IDs across all domains.
     */
    public List<String> missingIds(Map<String, Object> report) {
        return validateReport(report).missingIds();
    }

    private static String loadResource(String name) {
        ClassLoader classLoader = EchoAshfallParityContract.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalStateException("Parity checklist not found on classpath: " + name);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read parity checklist: " + name, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Domain> parseDomains(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Domain> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            Map<String, Object> domainMap = (Map<String, Object>) map;
            String id = string(domainMap.get("id"));
            String label = string(domainMap.get("label"));
            List<String> nodeKinds = stringList(domainMap.get("nodeKinds"));
            int minCount = number(domainMap.get("minCount")).intValue();
            List<String> requiredIds = new ArrayList<>();
            Object entries = domainMap.get("entries");
            if (entries instanceof List<?> entryList) {
                for (Object entryItem : entryList) {
                    if (!(entryItem instanceof Map<?, ?> entryMap)) {
                        continue;
                    }
                    String entryId = string(entryMap.get("id"));
                    Boolean required = bool(entryMap.get("required"));
                    if (!entryId.isBlank() && (required == null || required)) {
                        requiredIds.add(entryId);
                    }
                }
            }
            result.add(new Domain(id, label, nodeKinds, minCount, Collections.unmodifiableList(requiredIds)));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, MetadataRule> parseMetadataRules(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        LinkedHashMap<String, MetadataRule> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = string(entry.getKey());
            if (!(entry.getValue() instanceof Map<?, ?> ruleMap)) {
                continue;
            }
            Map<String, Object> rule = (Map<String, Object>) ruleMap;
            result.put(key, new MetadataRule(
                    stringList(rule.get("requiredFields")),
                    stringList(rule.get("atLeastOneOf"))
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = string(item);
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return result;
    }

    private static Set<String> stringSet(Object value) {
        return new LinkedHashSet<>(stringList(value));
    }

    private static String string(Object value) {
        return value instanceof String s ? s : "";
    }

    private static Boolean bool(Object value) {
        return value instanceof Boolean b ? b : null;
    }

    private static Number number(Object value) {
        return value instanceof Number n ? n : 0;
    }

    public record Domain(
            String id,
            String label,
            List<String> nodeKinds,
            int minCount,
            List<String> requiredIds
    ) {
    }

    public record MetadataRule(
            List<String> requiredFields,
            List<String> atLeastOneOf
    ) {
    }

    public record DomainResult(
            String domainId,
            String domainLabel,
            boolean passed,
            List<String> missingIds,
            List<String> partialIds,
            List<String> unsupportedIds
    ) {
    }

    public record ValidationResult(
            boolean passed,
            List<DomainResult> domainResults,
            List<String> missingIds,
            List<String> partialIds,
            List<String> unsupportedIds
    ) {
    }
}
