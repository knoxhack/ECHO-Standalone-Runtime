package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoLensStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimeEchoLensParitySmokeHarness {
    private EchoRuntimeEchoLensParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeScan = executeNativeReferenceScan(
                EchoLensStandaloneAdapter.REFERENCE_TARGET_ID,
                EchoLensStandaloneAdapter.REFERENCE_SCAN_MODE);
        EchoLensStandaloneAdapter standaloneAdapter = new EchoLensStandaloneAdapter();
        Map<String, Object> standaloneScan = standaloneAdapter.executeScan(
                EchoLensStandaloneAdapter.REFERENCE_TARGET_ID,
                EchoLensStandaloneAdapter.REFERENCE_SCAN_MODE);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceScanPassed(nativeScan),
                "native Lens reference scan should resolve damaged power node");
        require(standaloneAdapter.referenceScanPassed(standaloneScan),
                "standalone Lens scan should resolve damaged power node");
        require(Boolean.TRUE.equals(standaloneActivation.get("fieldScanExecuted")),
                "standalone activation should execute field scan");
        require(nativeScan.get("adapterCoreContract").equals(standaloneScan.get("adapterCoreContract")),
                "native and standalone scan contracts should match");
        require(nativeScan.get("targetId").equals(standaloneScan.get("targetId")),
                "native and standalone scan target should match");
        require(nativeScan.get("providerIds").equals(standaloneScan.get("providerIds")),
                "native and standalone provider ids should match");
        require(nativeScan.get("sections").equals(standaloneScan.get("sections")),
                "native and standalone sections should match");
        require(nativeScan.get("diagnosticCodes").equals(standaloneScan.get("diagnosticCodes")),
                "native and standalone diagnostic codes should match");

        System.out.println("echolens parity smoke PASS contract="
                + nativeScan.get("adapterCoreContract")
                + " target="
                + nativeScan.get("targetId")
                + " sections="
                + ((List<?>) nativeScan.get("sections")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceScan(String targetId, String scanMode) {
        String normalizedTarget = normalizeId(targetId);
        String normalizedMode = normalize(scanMode);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("adapterCoreContract", EchoLensStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        report.put("service", "echolens:inspection_service");
        report.put("targetId", normalizedTarget);
        report.put("scanMode", normalizedMode);
        report.put("scanExecuted", true);
        report.put("providerIds", List.of(
                "echolens:target_identity",
                "echolens:machine_status",
                "echolens:integration_status"
        ));
        report.put("sections", List.of(
                section("identity", List.of(
                        row("target", normalizedTarget),
                        row("kind", "machine"),
                        row("source", "adaptercore")
                )),
                section("machine", List.of(
                        row("status", "damaged"),
                        row("power", "offline"),
                        row("repair", "requires_power_cell")
                )),
                section("links", List.of(
                        row("index", "echoindex:recipe_search/index_query"),
                        row("terminal", "echoterminal:surface/field_ops")
                ))
        ));
        report.put("diagnosticCodes", List.of("lens.machine.offline", "lens.repair.power_cell_required"));
        report.put("referenceBehavior", "field_scan_resolves_damaged_power_node");
        return Map.copyOf(report);
    }

    private static boolean nativeReferenceScanPassed(Map<String, Object> report) {
        return Boolean.TRUE.equals(report.get("scanExecuted"))
                && EchoLensStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(report.get("adapterCoreContract"))
                && EchoLensStandaloneAdapter.REFERENCE_TARGET_ID.equals(report.get("targetId"))
                && list(report.get("diagnosticCodes")).contains("lens.repair.power_cell_required")
                && sections(report).stream().anyMatch(section ->
                        "machine".equals(section.get("id"))
                                && String.valueOf(section.get("rows")).contains("requires_power_cell"));
    }

    private static Map<String, Object> section(String id, List<Map<String, String>> rows) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("id", id);
        section.put("rows", List.copyOf(rows));
        return Map.copyOf(section);
    }

    private static Map<String, String> row(String key, String value) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("key", key);
        row.put("value", value);
        return Map.copyOf(row);
    }

    private static String normalizeId(Object value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT).trim();
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

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> sections(Map<String, Object> report) {
        Object value = report.get("sections");
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }
}
