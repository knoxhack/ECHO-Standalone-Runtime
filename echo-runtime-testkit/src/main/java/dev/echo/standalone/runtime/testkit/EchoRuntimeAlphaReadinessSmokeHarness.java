package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessGate;
import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessResult;
import dev.echo.standalone.runtime.app.EchoStandaloneAlphaReadinessStatus;
import dev.echo.standalone.runtime.app.EchoStandaloneSupportBundle;
import dev.echo.standalone.runtime.app.EchoStandaloneSupportBundleEntry;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoRuntimeAlphaReadinessSmokeHarness {
    private static final String GENERATED_AT = "1970-01-01T00:00:00Z";
    private static final String GENERATED_BY = "EchoRuntimeAlphaReadinessSmokeHarness";

    private EchoRuntimeAlphaReadinessSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoStandaloneAlphaReadinessGate gate = new EchoStandaloneAlphaReadinessGate();
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoStandaloneAlphaReadinessResult ready = gate.evaluate(services, workspaceRoot);

        require(services.require(EchoStandaloneAlphaReadinessResult.class) == ready,
                "alpha readiness result should be service-bound");
        require(ready.status() == EchoStandaloneAlphaReadinessStatus.READY,
                "current workspace should pass alpha readiness");
        require(ready.ready(),
                "ready helper should report true");
        require(ready.blockedCount() == 0,
                "ready workspace should have no blocking failures");
        require(ready.checkCount() == 51,
                "alpha readiness gate should evaluate fifty-one checks");
        require(ready.passedCount() == ready.checkCount(),
                "all readiness checks should pass");
        require(ready.launcherResult().verification().ready(),
                "launcher verification should pass inside readiness gate");
        require(!ready.launcherResult().launched(),
                "readiness gate should use launcher verify-only mode");
        require(ready.supportBundleReady(),
                "launcher support bundle should be ready");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("docs.docs/echo/standalone/ECHO_STANDALONE_ALPHA_READINESS.md")),
                "alpha readiness documentation should be checked");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("reports.reports/echo/standalone/runtime-alpha-readiness.json")),
                "alpha readiness report should be checked");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("docs.docs/ashfall-standalone-parity-contract.md")),
                "ashfall parity contract should be checked");
        require(ready.checks().stream().anyMatch(check -> check.checkId()
                        .equals("docs.docs/ashfall-standalone-parity-checklist.json")),
                "ashfall parity checklist should be checked");


        writeReadinessReports(workspaceRoot, ready);
        require(!Files.readString(
                        workspaceRoot.resolve("reports/echo/standalone/runtime-alpha-readiness.json"),
                        StandardCharsets.UTF_8
                ).contains("bootstrapStandaloneEvidenceReports"),
                "runtime alpha readiness report should be smoke-generated evidence");
        EchoStandaloneAlphaReadinessResult refreshed = gate.evaluate(
                new EchoDefaultRuntimeServiceRegistry(),
                workspaceRoot
        );
        require(refreshed.ready() && refreshed.checkCount() == ready.checkCount(),
                "generated alpha readiness reports should keep the gate ready");

        Path brokenRoot = Files.createTempDirectory("echo-runtime-alpha-readiness-missing");
        EchoStandaloneAlphaReadinessResult blocked = gate.evaluate(
                new EchoDefaultRuntimeServiceRegistry(),
                brokenRoot
        );
        require(blocked.status() == EchoStandaloneAlphaReadinessStatus.BLOCKED,
                "missing workspace should block alpha readiness");
        require(blocked.blockedCount() > 0,
                "missing workspace should report blockers");
        require(!blocked.supportBundleReady(),
                "missing workspace should not have a ready support bundle");

        System.out.println("phase14.20 alpha readiness smoke PASS status="
                + ready.status().name()
                + " checks="
                + ready.checkCount()
                + " blocked="
                + ready.blockedCount()
                + " supportBundle="
                + ready.supportBundleReady()
                + " reports=6");
    }

    private static void writeReadinessReports(
            Path workspaceRoot,
            EchoStandaloneAlphaReadinessResult ready
    ) throws IOException {
        Path reportsRoot = workspaceRoot.resolve("reports/echo/standalone");
        Files.createDirectories(reportsRoot);
        writeReport(reportsRoot.resolve("runtime-alpha-readiness.json"), runtimeReadinessReport(ready));
        writeReport(reportsRoot.resolve("alpha-readiness-gate.json"), gateReport(ready));
        writeReport(reportsRoot.resolve("alpha-readiness-checks.json"), checksReport(ready));
        writeReport(reportsRoot.resolve("alpha-readiness-blockers.json"), blockersReport(ready));
        writeReport(reportsRoot.resolve("alpha-readiness-support-bundle.json"), supportBundleReport(ready));
        writeReport(reportsRoot.resolve("alpha-readiness-release-policy.json"), releasePolicyReport(ready));
    }

    private static Map<String, Object> runtimeReadinessReport(EchoStandaloneAlphaReadinessResult ready) {
        Map<String, Object> report = baseReport("echo.standalone.runtime_alpha_readiness.v1", ready.status().name());
        report.put("gateId", ready.gateId());
        report.put("ready", ready.ready());
        report.put("checkCount", ready.checkCount());
        report.put("passedCount", ready.passedCount());
        report.put("blockedCount", ready.blockedCount());
        report.put("supportBundleReady", ready.supportBundleReady());
        report.put("launcher", launcherSummary(ready));
        report.put("reportsGenerated", 6);
        return report;
    }

    private static Map<String, Object> gateReport(EchoStandaloneAlphaReadinessResult ready) {
        Map<String, Object> report = baseReport("echo.standalone.alpha_readiness_gate.v1", ready.status().name());
        report.put("gateId", ready.gateId());
        report.put("ready", ready.ready());
        report.put("blockingFailures", ready.blockedCount());
        report.put("checksPassed", ready.passedCount());
        report.put("checksTotal", ready.checkCount());
        report.put("launcherVerificationReady", ready.launcherResult().verification().ready());
        report.put("launcherLaunched", ready.launcherResult().launched());
        return report;
    }

    private static Map<String, Object> checksReport(EchoStandaloneAlphaReadinessResult ready) {
        Map<String, Object> report = baseReport("echo.standalone.alpha_readiness_checks.v1", ready.status().name());
        report.put("checkCount", ready.checkCount());
        report.put("passedCount", ready.passedCount());
        report.put("blockedCount", ready.blockedCount());
        report.put("checks", ready.checks().stream()
                .map(check -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("checkId", check.checkId());
                    row.put("category", check.category());
                    row.put("passed", check.passed());
                    row.put("blocking", check.blocking());
                    row.put("blocked", check.blocked());
                    row.put("detail", check.detail());
                    return row;
                })
                .toList());
        return report;
    }

    private static Map<String, Object> blockersReport(EchoStandaloneAlphaReadinessResult ready) {
        String status = ready.blockedCount() == 0 ? "PASS" : "BLOCKED";
        Map<String, Object> report = baseReport("echo.standalone.alpha_readiness_blockers.v1", status);
        report.put("blockedCount", ready.blockedCount());
        report.put("blockers", ready.checks().stream()
                .filter(check -> check.blocked())
                .map(check -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("checkId", check.checkId());
                    row.put("category", check.category());
                    row.put("detail", check.detail());
                    return row;
                })
                .toList());
        return report;
    }

    private static Map<String, Object> supportBundleReport(EchoStandaloneAlphaReadinessResult ready) {
        String status = ready.supportBundleReady() ? "PASS" : "BLOCKED";
        Map<String, Object> report = baseReport("echo.standalone.alpha_readiness_support_bundle.v1", status);
        report.put("ready", ready.supportBundleReady());
        ready.launcherResult().supportBundle().ifPresentOrElse(bundle -> {
            report.put("bundleId", bundle.bundleId());
            report.put("bundleGeneratedAt", bundle.generatedAt());
            report.put("entryCount", bundle.entries().size());
            report.put("presentEntryCount", bundle.presentEntryCount());
            report.put("manifestPath", bundle.manifestPath());
            report.put("manifestPresent", bundle.manifestPresent());
            report.put("manifestEntryCount", bundle.manifestEntryCount());
            report.put("archivePath", bundle.archivePath());
            report.put("archivePresent", bundle.archivePresent());
            report.put("archiveByteSize", bundle.archiveByteSize());
            report.put("diagnostics", bundle.diagnostics());
            report.put("entries", bundle.entries().stream()
                    .map(EchoRuntimeAlphaReadinessSmokeHarness::supportBundleEntry)
                    .toList());
        }, () -> {
            report.put("entryCount", 0);
            report.put("presentEntryCount", 0);
            report.put("diagnostics", java.util.List.of("supportBundle=missing"));
            report.put("entries", java.util.List.of());
        });
        return report;
    }

    private static Map<String, Object> releasePolicyReport(EchoStandaloneAlphaReadinessResult ready) {
        Map<String, Object> report = baseReport("echo.standalone.alpha_readiness_release_policy.v1",
                ready.ready() ? "PASS" : "BLOCKED");
        report.put("releaseLane", "alpha");
        report.put("releaseAllowed", ready.ready());
        report.put("releaseBlocked", !ready.ready());
        report.put("hardGate", true);
        report.put("requiredSupportBundle", true);
        report.put("blockingFailures", ready.blockedCount());
        report.put("launcherMode", "verify-only");
        report.put("standaloneLaunchSkipped", !ready.launcherResult().launched());
        report.put("betaGateCovered", false);
        return report;
    }

    private static Map<String, Object> launcherSummary(EchoStandaloneAlphaReadinessResult ready) {
        Map<String, Object> launcher = new LinkedHashMap<>();
        launcher.put("ready", ready.launcherResult().ready());
        launcher.put("detectedStandaloneWorkspace", ready.launcherResult().detection().standaloneWorkspace());
        launcher.put("verificationReady", ready.launcherResult().verification().ready());
        launcher.put("verificationPassedCount", ready.launcherResult().verification().passedCount());
        launcher.put("verificationFailedCount", ready.launcherResult().verification().failedCount());
        launcher.put("repairActions", ready.launcherResult().repairPlan().actionCount());
        launcher.put("launched", ready.launcherResult().launched());
        launcher.put("supportBundlePresent", ready.launcherResult().supportBundle().isPresent());
        return launcher;
    }

    private static Map<String, Object> supportBundleEntry(EchoStandaloneSupportBundleEntry entry) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("relativePath", entry.relativePath());
        row.put("present", entry.present());
        row.put("byteSize", entry.byteSize());
        return row;
    }

    private static Map<String, Object> baseReport(String schema, String status) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schema", schema);
        report.put("generatedAt", GENERATED_AT);
        report.put("generatedBy", GENERATED_BY);
        report.put("status", status);
        return report;
    }

    private static void writeReport(Path path, Map<String, Object> payload) throws IOException {
        Files.writeString(path, toJson(payload) + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + string
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                out.append('\n')
                        .append("  ")
                        .append(toJson(String.valueOf(entry.getKey())))
                        .append(": ")
                        .append(indent(toJson(entry.getValue())));
                first = false;
            }
            if (!map.isEmpty()) {
                out.append('\n');
            }
            return out.append('}').toString();
        }
        if (value instanceof Collection<?> collection) {
            StringBuilder out = new StringBuilder("[");
            boolean first = true;
            for (Object item : collection) {
                if (!first) {
                    out.append(',');
                }
                out.append('\n').append("  ").append(indent(toJson(item)));
                first = false;
            }
            if (!collection.isEmpty()) {
                out.append('\n');
            }
            return out.append(']').toString();
        }
        if (value instanceof EchoStandaloneSupportBundle bundle) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("bundleId", bundle.bundleId());
            row.put("complete", bundle.complete());
            row.put("entryCount", bundle.entries().size());
            row.put("presentEntryCount", bundle.presentEntryCount());
            return toJson(row);
        }
        return toJson(String.valueOf(value));
    }

    private static String indent(String value) {
        return value.replace("\n", "\n  ");
    }
}
