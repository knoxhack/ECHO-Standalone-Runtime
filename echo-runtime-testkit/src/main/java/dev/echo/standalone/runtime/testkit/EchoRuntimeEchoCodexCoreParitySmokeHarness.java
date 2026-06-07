package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoCodexCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimeEchoCodexCoreParitySmokeHarness {
    private EchoRuntimeEchoCodexCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeLookup = executeNativeReferenceLookup(EchoCodexCoreStandaloneAdapter.REFERENCE_QUERY);
        EchoCodexCoreStandaloneAdapter standaloneAdapter = new EchoCodexCoreStandaloneAdapter();
        Map<String, Object> standaloneLookup = standaloneAdapter.executeLookup(EchoCodexCoreStandaloneAdapter.REFERENCE_QUERY);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceLookupPassed(nativeLookup), "native CodexCore reference lookup should pass");
        require(standaloneAdapter.referenceLookupPassed(standaloneLookup), "standalone CodexCore lookup should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("codexLookupExecuted")),
                "standalone activation should execute codex lookup");
        require(nativeLookup.get("adapterCoreContract").equals(standaloneLookup.get("adapterCoreContract")),
                "native and standalone codex contracts should match");
        require(nativeLookup.get("selectedEntryId").equals(standaloneLookup.get("selectedEntryId")),
                "native and standalone selected entry should match");
        require(nativeLookup.get("resultIds").equals(standaloneLookup.get("resultIds")),
                "native and standalone result ids should match");
        require(nativeLookup.get("terminalArchiveReference").equals(standaloneLookup.get("terminalArchiveReference")),
                "native and standalone terminal archive references should match");
        require(nativeLookup.get("relatedContent").equals(standaloneLookup.get("relatedContent")),
                "native and standalone related content should match");
        require(nativeLookup.get("diagnostics").equals(standaloneLookup.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echocodexcore parity smoke PASS contract="
                + nativeLookup.get("adapterCoreContract")
                + " selected="
                + nativeLookup.get("selectedEntryId")
                + " results="
                + ((List<?>) nativeLookup.get("resultIds")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceLookup(String query) {
        String normalizedQuery = normalize(query);
        List<Map<String, Object>> entries = nativeReferenceEntries();
        List<Map<String, Object>> visibleMatches = entries.stream()
                .filter(entry -> Boolean.TRUE.equals(entry.get("visible")))
                .filter(entry -> searchableText(entry).contains(normalizedQuery))
                .toList();
        Map<String, Object> selected = visibleMatches.isEmpty() ? Map.of() : visibleMatches.get(0);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("adapterCoreContract", EchoCodexCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        report.put("service", "echocodexcore:codex_service");
        report.put("query", normalizedQuery);
        report.put("lookupExecuted", true);
        report.put("visibleEntryCount", entries.stream().filter(entry -> Boolean.TRUE.equals(entry.get("visible"))).count());
        report.put("matchedCount", visibleMatches.size());
        report.put("resultIds", visibleMatches.stream().map(entry -> String.valueOf(entry.get("id"))).toList());
        report.put("selectedEntryId", String.valueOf(selected.getOrDefault("id", "")));
        report.put("selectedTitleKey", String.valueOf(selected.getOrDefault("titleKey", "")));
        report.put("selectedBodyKey", String.valueOf(selected.getOrDefault("bodyKey", "")));
        report.put("selectedKind", String.valueOf(selected.getOrDefault("kind", "")));
        report.put("selectedCategory", String.valueOf(selected.getOrDefault("category", "")));
        report.put("selectedDiscoveryState", String.valueOf(selected.getOrDefault("discoveryState", "")));
        report.put("terminalArchiveReference", String.valueOf(selected.getOrDefault("terminalArchiveReference", "")));
        report.put("relatedContent", list(selected.get("relatedContent")));
        report.put("diagnostics", List.of(
                "codex.lookup.visible_entries_indexed",
                "codex.lookup.reference_entry_resolved",
                "codex.lookup.discovery_state_respected"
        ));
        report.put("referenceBehavior", "codex_lookup_resolves_visible_crash_beacon_entry");
        return Map.copyOf(report);
    }

    private static boolean nativeReferenceLookupPassed(Map<String, Object> report) {
        return Boolean.TRUE.equals(report.get("lookupExecuted"))
                && EchoCodexCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(report.get("adapterCoreContract"))
                && EchoCodexCoreStandaloneAdapter.REFERENCE_ENTRY_ID.equals(report.get("selectedEntryId"))
                && "guide_reference".equals(report.get("selectedKind"))
                && "terminal".equals(report.get("selectedCategory"))
                && "discovered".equals(report.get("selectedDiscoveryState"))
                && "echoterminal:archive/ashfall_crash_beacon".equals(report.get("terminalArchiveReference"))
                && list(report.get("relatedContent")).contains("echolens:scan/crash_beacon")
                && list(report.get("diagnostics")).contains("codex.lookup.reference_entry_resolved");
    }

    private static List<Map<String, Object>> nativeReferenceEntries() {
        return List.of(
                entry(
                        EchoCodexCoreStandaloneAdapter.REFERENCE_ENTRY_ID,
                        "guide_reference",
                        "terminal",
                        "discovered",
                        "codex.echoashfallprotocol.crash_beacon.title",
                        "codex.echoashfallprotocol.crash_beacon.body",
                        "echoterminal:archive/ashfall_crash_beacon",
                        List.of("crash", "beacon", "starter route", "field operations"),
                        List.of("echolens:scan/crash_beacon", "echoholomap:marker/crash_site")
                ),
                entry(
                        "echoashfallprotocol:codex/toxic_rain",
                        "lore_database",
                        "systems",
                        "teased",
                        "codex.echoashfallprotocol.toxic_rain.title",
                        "codex.echoashfallprotocol.toxic_rain.body",
                        "echoterminal:archive/toxic_rain",
                        List.of("weather", "rain", "hazard", "ashfall"),
                        List.of("echoweathercore:forecast/toxic_rain")
                ),
                entry(
                        "echoashfallprotocol:codex/nexus_spoiler",
                        "lore_database",
                        "lore",
                        "hidden",
                        "codex.echoashfallprotocol.nexus_spoiler.title",
                        "codex.echoashfallprotocol.nexus_spoiler.body",
                        "echoterminal:archive/nexus_spoiler",
                        List.of("nexus", "spoiler", "finale"),
                        List.of("echomissioncore:mission/awaken_nexus_core")
                )
        );
    }

    private static Map<String, Object> entry(
            String id,
            String kind,
            String category,
            String discoveryState,
            String titleKey,
            String bodyKey,
            String terminalArchiveReference,
            List<String> searchTerms,
            List<String> relatedContent
    ) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", id);
        entry.put("kind", kind);
        entry.put("category", category);
        entry.put("discoveryState", discoveryState);
        entry.put("visible", List.of("teased", "discovered", "updated", "archived").contains(discoveryState));
        entry.put("titleKey", titleKey);
        entry.put("bodyKey", bodyKey);
        entry.put("terminalArchiveReference", terminalArchiveReference);
        entry.put("searchTerms", List.copyOf(searchTerms));
        entry.put("relatedContent", List.copyOf(relatedContent));
        return Map.copyOf(entry);
    }

    private static String searchableText(Map<String, Object> entry) {
        return normalize(entry.get("id") + " "
                + entry.get("titleKey") + " "
                + entry.get("bodyKey") + " "
                + entry.get("kind") + " "
                + entry.get("category") + " "
                + entry.get("terminalArchiveReference") + " "
                + String.join(" ", list(entry.get("searchTerms"))) + " "
                + String.join(" ", list(entry.get("relatedContent"))));
    }

    private static String normalize(Object value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT).replace('_', ' ').trim();
    }

    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
