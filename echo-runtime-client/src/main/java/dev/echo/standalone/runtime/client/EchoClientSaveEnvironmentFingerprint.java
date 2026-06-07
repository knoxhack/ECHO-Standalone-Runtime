package dev.echo.standalone.runtime.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EchoClientSaveEnvironmentFingerprint {
    static final String ALGORITHM = "sha256:echo.client.save_environment.v1";
    static final String ALGORITHM_METADATA_KEY = "saveEnvironmentFingerprintAlgorithm";
    static final String FINGERPRINT_METADATA_KEY = "saveEnvironmentFingerprint";
    static final String MODULE_IDS_METADATA_KEY = "saveEnvironmentModuleIds";
    static final String RESOURCE_PACK_IDS_METADATA_KEY = "saveEnvironmentResourcePackIds";
    static final String MODULE_COUNT_METADATA_KEY = "saveEnvironmentModuleCount";
    static final String ACTIVE_MODULE_COUNT_METADATA_KEY = "saveEnvironmentActiveModuleCount";
    static final String RESOURCE_PACK_COUNT_METADATA_KEY = "saveEnvironmentResourcePackCount";

    private EchoClientSaveEnvironmentFingerprint() {
    }

    static Map<String, String> metadata(
            EchoClientModScanSummary modScan,
            List<EchoClientResourcePackSummary> resourcePacks
    ) {
        EchoClientModScanSummary safeModScan = modScan == null ? EchoClientModScanSummary.empty() : modScan;
        List<EchoClientResourcePackSummary> safeResourcePacks =
                resourcePacks == null ? List.of() : List.copyOf(resourcePacks);
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put(ALGORITHM_METADATA_KEY, ALGORITHM);
        metadata.put(FINGERPRINT_METADATA_KEY, fingerprint(safeModScan, safeResourcePacks));
        metadata.put(MODULE_IDS_METADATA_KEY, joinedModuleIds(safeModScan.modules()));
        metadata.put(RESOURCE_PACK_IDS_METADATA_KEY, joinedResourcePackIds(safeResourcePacks));
        metadata.put(MODULE_COUNT_METADATA_KEY, Integer.toString(safeModScan.descriptorCount()));
        metadata.put(ACTIVE_MODULE_COUNT_METADATA_KEY, Integer.toString(safeModScan.activeCount()));
        metadata.put(RESOURCE_PACK_COUNT_METADATA_KEY, Integer.toString(safeResourcePacks.size()));
        return Map.copyOf(metadata);
    }

    static String shortFingerprint(String fingerprint) {
        if (fingerprint == null || fingerprint.isBlank()) {
            return "";
        }
        return fingerprint.length() <= 12 ? fingerprint : fingerprint.substring(0, 12);
    }

    private static String fingerprint(
            EchoClientModScanSummary modScan,
            List<EchoClientResourcePackSummary> resourcePacks
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(ALGORITHM).append('\n');
        appendModules(builder, modScan.modules());
        appendResourcePacks(builder, resourcePacks);
        builder.append("graphIssues=").append(modScan.graphIssueCount()).append('\n');
        builder.append("errors=").append(modScan.errorCount()).append('\n');
        builder.append("warnings=").append(modScan.warningCount()).append('\n');
        return sha256(builder.toString());
    }

    private static void appendModules(StringBuilder builder, List<EchoClientModSummary> modules) {
        ArrayList<EchoClientModSummary> sorted = new ArrayList<>(modules == null ? List.of() : modules);
        sorted.sort(Comparator.comparing(EchoClientModSummary::id));
        builder.append("modules=").append(sorted.size()).append('\n');
        for (EchoClientModSummary module : sorted) {
            builder.append("module|")
                    .append(escape(module.id())).append('|')
                    .append(escape(module.version())).append('|')
                    .append(escape(module.kind())).append('|')
                    .append(escape(module.side())).append('|')
                    .append(module.standalone()).append('|')
                    .append(module.official()).append('|')
                    .append(module.nativeEntrypointDeclared()).append('|')
                    .append(module.adapterCoreDeclared()).append('|')
                    .append(escape(module.runtimeStatus())).append('|')
                    .append(module.requiredCount()).append('|')
                    .append(module.optionalCount()).append('|')
                    .append(joinedStrings(module.adapterCoreDomains())).append('|')
                    .append(joinedStrings(module.adapterCoreRuntimes()))
                    .append('\n');
        }
    }

    private static void appendResourcePacks(
            StringBuilder builder,
            List<EchoClientResourcePackSummary> resourcePacks
    ) {
        ArrayList<EchoClientResourcePackSummary> sorted =
                new ArrayList<>(resourcePacks == null ? List.of() : resourcePacks);
        sorted.sort(Comparator.comparing(EchoClientResourcePackSummary::id));
        builder.append("resourcePacks=").append(sorted.size()).append('\n');
        for (EchoClientResourcePackSummary pack : sorted) {
            builder.append("resourcePack|")
                    .append(escape(pack.id())).append('|')
                    .append(joinedSet(pack.namespaces())).append('|')
                    .append(pack.textureCount()).append('|')
                    .append(pack.animatedTextureMetadataCount()).append('|')
                    .append(pack.modelCount()).append('|')
                    .append(pack.blockstateCount()).append('|')
                    .append(pack.langCount()).append('|')
                    .append(pack.soundsJsonCount()).append('|')
                    .append(pack.soundEventCount())
                    .append('\n');
        }
    }

    private static String joinedModuleIds(List<EchoClientModSummary> modules) {
        return (modules == null ? List.<EchoClientModSummary>of() : modules).stream()
                .map(EchoClientModSummary::id)
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static String joinedResourcePackIds(List<EchoClientResourcePackSummary> resourcePacks) {
        return (resourcePacks == null ? List.<EchoClientResourcePackSummary>of() : resourcePacks).stream()
                .map(EchoClientResourcePackSummary::id)
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static String joinedSet(Set<String> values) {
        return joinedStrings(values == null ? List.of() : values.stream().sorted().toList());
    }

    private static String joinedStrings(List<String> values) {
        return (values == null ? List.<String>of() : values).stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isBlank())
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    private static String escape(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(Character.forDigit((b >>> 4) & 0x0F, 16));
                builder.append(Character.forDigit(b & 0x0F, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }
}
