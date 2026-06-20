package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoStandaloneAlphaReadinessGate {
    private static final List<String> REQUIRED_DOCS = List.of(
            "docs/echo/standalone/ECHO_STANDALONE_RUNTIME_ARCHITECTURE.md",
            "docs/echo/standalone/ECHO_STANDALONE_APP_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_MODULE_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_PACKOS_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_ASSET_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_UI_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_SAVE_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_DATA_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_WORLD_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_ENTITY_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_ITEM_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_GAMEPLAY_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_RENDER_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_AUDIO_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_NETWORK_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_SCRIPTING_RUNTIME.md",
            "docs/echo/standalone/ECHO_STANDALONE_COMPATIBILITY_MIGRATION.md",
            "docs/echo/standalone/ECHO_STANDALONE_VERTICAL_SLICE.md",
            "docs/echo/standalone/ECHO_STANDALONE_LAUNCHER_MVP.md",
            "docs/echo/standalone/ECHO_STANDALONE_ALPHA_READINESS.md",
            "docs/ashfall-standalone-parity-contract.md",
            "docs/ashfall-standalone-parity-checklist.json"
    );
    private static final List<String> REQUIRED_REPORTS = List.of(
            "reports/echo/standalone/runtime-architecture.json",
            "reports/echo/standalone/runtime-boot.json",
            "reports/echo/standalone/runtime-modules.json",
            "reports/echo/standalone/runtime-packos.json",
            "reports/echo/standalone/asset-index.json",
            "reports/echo/standalone/runtime-ui.json",
            "reports/echo/standalone/runtime-save.json",
            "reports/echo/standalone/runtime-data.json",
            "reports/echo/standalone/runtime-world.json",
            "reports/echo/standalone/runtime-entity.json",
            "reports/echo/standalone/runtime-item.json",
            "reports/echo/standalone/runtime-gameplay.json",
            "reports/echo/standalone/runtime-render.json",
            "reports/echo/standalone/runtime-audio.json",
            "reports/echo/standalone/runtime-network.json",
            "reports/echo/standalone/runtime-scripting.json",
            "reports/echo/standalone/runtime-compatibility.json",
            "reports/echo/standalone/runtime-vertical-slice.json",
            "reports/echo/standalone/runtime-launcher.json",
            "reports/echo/standalone/runtime-alpha-readiness.json",
            "reports/echo/standalone/alpha-readiness-gate.json",
            "reports/echo/standalone/alpha-readiness-checks.json",
            "reports/echo/standalone/alpha-readiness-blockers.json",
            "reports/echo/standalone/alpha-readiness-support-bundle.json",
            "reports/echo/standalone/alpha-readiness-release-policy.json"
    );

    public EchoStandaloneAlphaReadinessResult evaluate(
            EchoRuntimeServiceRegistry services,
            Path workspaceRoot
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        Path root = workspaceRoot.toAbsolutePath().normalize();
        ArrayList<EchoStandaloneAlphaReadinessCheck> checks = new ArrayList<>();
        addArtifactChecks(checks, root, "docs", REQUIRED_DOCS);
        addArtifactChecks(checks, root, "reports", REQUIRED_REPORTS);

        EchoStandaloneLauncherResult launcher = new EchoStandaloneLauncherRuntime().run(
                services,
                EchoStandaloneLauncherRequest.verifyOnly(root)
        );
        checks.add(new EchoStandaloneAlphaReadinessCheck(
                "launcher.verification",
                "launcher",
                launcher.verification().ready(),
                true,
                "standalone launcher verification is ready"
        ));
        checks.add(new EchoStandaloneAlphaReadinessCheck(
                "launcher.repair-plan",
                "launcher",
                launcher.repairPlan().planningOnly() && launcher.repairPlan().actionCount() == 0,
                true,
                "launcher repair plan is planning-only and empty"
        ));
        checks.add(new EchoStandaloneAlphaReadinessCheck(
                "launcher.support-bundle",
                "launcher",
                launcher.supportBundle()
                        .map(EchoStandaloneSupportBundle::complete)
                        .orElse(false),
                true,
                "launcher support bundle manifest has all required entries"
        ));

        checks.addAll(contentGraphParityChecks(root));

        EchoStandaloneAlphaReadinessStatus status = checks.stream()
                .anyMatch(EchoStandaloneAlphaReadinessCheck::blocked)
                        ? EchoStandaloneAlphaReadinessStatus.BLOCKED
                        : EchoStandaloneAlphaReadinessStatus.READY;
        EchoStandaloneAlphaReadinessResult result = new EchoStandaloneAlphaReadinessResult(
                "echo:standalone-alpha-readiness",
                status,
                checks,
                launcher
        );
        services.register(EchoStandaloneAlphaReadinessResult.class, result);
        return result;
    }

    private static List<EchoStandaloneAlphaReadinessCheck> contentGraphParityChecks(Path root) {
        ArrayList<EchoStandaloneAlphaReadinessCheck> checks = new ArrayList<>();
        Path contractPath = root.resolve("docs/ashfall-standalone-parity-checklist.json");
        boolean contractPresent = Files.isRegularFile(contractPath);
        checks.add(new EchoStandaloneAlphaReadinessCheck(
                "content_graph.parity_contract",
                "content_graph",
                contractPresent,
                true,
                contractPresent
                        ? "Ashfall parity checklist is present: " + contractPath
                        : "Missing Ashfall parity checklist: " + contractPath
        ));

        Path auditPath = root.resolve("build/reports/ashfall-content-graph-audit.json");
        if (Files.isRegularFile(auditPath)) {
            try {
                String text = Files.readString(auditPath);
                Object parsed = dev.echo.standalone.runtime.data.EchoDataJson.parse(text);
                if (parsed instanceof Map<?, ?> map) {
                    Object statusValue = map.get("status");
                    String auditStatus = statusValue == null ? "" : String.valueOf(statusValue);
                    boolean pass = "PASS".equals(auditStatus);
                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> domains = (Map<String, Map<String, Object>>) map.get("domains");
                    List<String> failingDomains = new ArrayList<>();
                    if (domains != null) {
                        for (Map.Entry<String, Map<String, Object>> entry : domains.entrySet()) {
                            Object passed = entry.getValue().get("passed");
                            if (passed instanceof Boolean b && !b) {
                                failingDomains.add(entry.getKey());
                            }
                        }
                    }
                    checks.add(new EchoStandaloneAlphaReadinessCheck(
                            "content_graph.audit_report",
                            "content_graph",
                            pass,
                            true,
                            pass
                                    ? "Content graph audit report is PASS: " + auditPath
                                    : "Content graph audit report is BLOCKED for domains: " + failingDomains
                    ));
                }
            } catch (Exception e) {
                checks.add(new EchoStandaloneAlphaReadinessCheck(
                        "content_graph.audit_report",
                        "content_graph",
                        false,
                        true,
                        "Failed to read content graph audit report: " + e.getMessage()
                ));
            }
        }
        return checks;
    }

    private static void addArtifactChecks(
            ArrayList<EchoStandaloneAlphaReadinessCheck> checks,
            Path root,
            String category,
            List<String> paths
    ) {
        for (String relativePath : paths) {
            checks.add(new EchoStandaloneAlphaReadinessCheck(
                    category + "." + relativePath,
                    category,
                    Files.isRegularFile(root.resolve(relativePath)),
                    true,
                    relativePath
            ));
        }
    }
}
