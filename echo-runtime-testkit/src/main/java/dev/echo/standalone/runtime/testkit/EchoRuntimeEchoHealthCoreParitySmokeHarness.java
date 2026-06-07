package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoHealthCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoHealthCoreParitySmokeHarness {
    private EchoRuntimeEchoHealthCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeReport = executeNativeReferenceReport("echo-native-m17", EchoHealthCoreStandaloneAdapter.REFERENCE_RUNTIME_NAME);
        EchoHealthCoreStandaloneAdapter standaloneAdapter = new EchoHealthCoreStandaloneAdapter();
        Map<String, Object> standaloneReport = standaloneAdapter.executeReport("echo-native-m17", EchoHealthCoreStandaloneAdapter.REFERENCE_RUNTIME_NAME);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceReportPassed(nativeReport), "native HealthCore reference report should pass");
        require(standaloneAdapter.referenceReportPassed(standaloneReport), "standalone HealthCore report should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("healthReportExecuted")),
                "standalone activation should execute health report");
        require(nativeReport.get("adapterCoreContract").equals(standaloneReport.get("adapterCoreContract")),
                "native and standalone health contracts should match");
        require(nativeReport.get("reportId").equals(standaloneReport.get("reportId")),
                "native and standalone report ids should match");
        require(nativeReport.get("runtimeName").equals(standaloneReport.get("runtimeName")),
                "native and standalone runtime names should match");
        require(nativeReport.get("status").equals(standaloneReport.get("status")),
                "native and standalone statuses should match");
        require(nativeReport.get("metrics").equals(standaloneReport.get("metrics")),
                "native and standalone metrics should match");
        require(nativeReport.get("modules").equals(standaloneReport.get("modules")),
                "native and standalone module states should match");
        require(nativeReport.get("observations").equals(standaloneReport.get("observations")),
                "native and standalone observations should match");
        require(nativeReport.get("supportBundle").equals(standaloneReport.get("supportBundle")),
                "native and standalone support bundle metadata should match");

        System.out.println("echohealthcore parity smoke PASS contract="
                + nativeReport.get("adapterCoreContract")
                + " report="
                + nativeReport.get("reportId")
                + " modules="
                + ((List<?>) nativeReport.get("modules")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceReport(String packId, String runtimeName) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("adapterCoreContract", EchoHealthCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        report.put("service", "echohealthcore:health_reporter");
        report.put("reportExecuted", true);
        report.put("reportId", EchoHealthCoreStandaloneAdapter.REFERENCE_REPORT_ID);
        report.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        report.put("runtimeName", runtimeName == null || runtimeName.isBlank() ? EchoHealthCoreStandaloneAdapter.REFERENCE_RUNTIME_NAME : runtimeName);
        report.put("runtimeVersion", "m17-contract");
        report.put("status", "OK");
        report.put("metrics", List.of(
                metric("echohealthcore:runtime_status", "status", 1, "OK"),
                metric("echohealthcore:module_health", "modules", 3, "OK"),
                metric("echohealthcore:budget_violation", "violations", 0, "OK")
        ));
        report.put("modules", List.of(
                module("echoadaptercore", "OK", "AdapterCore contract registry reachable"),
                module("echoscreencore", "OK", "ScreenCore composition service executed"),
                module(EchoHealthCoreStandaloneAdapter.MODULE_ID, "OK", "Health reporter service executed")
        ));
        report.put("observations", List.of(
                observation("diagnostics.snapshot", "OK", "Health snapshot captured for native bootstrap"),
                observation("runtime.budget", "OK", "No runtime budget violations observed"),
                observation("crash.boundary", "OK", "No crash context present")
        ));
        report.put("diagnostics", List.of("health.report.ready", "health.modules.ok", "health.local_only"));
        report.put("supportBundle", Map.of(
                "bundleId", "echohealthcore:support/native_bootstrap",
                "localOnly", true,
                "containsCrashContext", false
        ));
        report.put("localOnly", true);
        report.put("referenceBehavior", "healthcore_writes_runtime_health_report");
        return Map.copyOf(report);
    }

    private static boolean nativeReferenceReportPassed(Map<String, Object> report) {
        return Boolean.TRUE.equals(report.get("reportExecuted"))
                && EchoHealthCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(report.get("adapterCoreContract"))
                && EchoHealthCoreStandaloneAdapter.REFERENCE_REPORT_ID.equals(report.get("reportId"))
                && "OK".equals(report.get("status"))
                && Boolean.TRUE.equals(report.get("localOnly"))
                && String.valueOf(report.get("metrics")).contains("echohealthcore:runtime_status")
                && String.valueOf(report.get("modules")).contains(EchoHealthCoreStandaloneAdapter.MODULE_ID)
                && String.valueOf(report.get("observations")).contains("diagnostics.snapshot")
                && String.valueOf(report.get("supportBundle")).contains("containsCrashContext=false");
    }

    private static Map<String, Object> metric(String id, String unit, int value, String status) {
        Map<String, Object> metric = new LinkedHashMap<>();
        metric.put("id", id);
        metric.put("unit", unit);
        metric.put("value", value);
        metric.put("status", status);
        return Map.copyOf(metric);
    }

    private static Map<String, String> module(String id, String status, String summary) {
        Map<String, String> module = new LinkedHashMap<>();
        module.put("id", id);
        module.put("status", status);
        module.put("summary", summary);
        return Map.copyOf(module);
    }

    private static Map<String, String> observation(String id, String status, String summary) {
        Map<String, String> observation = new LinkedHashMap<>();
        observation.put("id", id);
        observation.put("status", status);
        observation.put("summary", summary);
        return Map.copyOf(observation);
    }
}
