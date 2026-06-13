package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeFeatureGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleSandboxPolicy;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;
import dev.echo.standalone.runtime.modules.EchoRuntimeSystemModuleStatusReport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeModuleSmokeHarness {
    private EchoRuntimeModuleSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-modules-smoke");
        writeDescriptor(fixtureRoot.resolve("echo-core/META-INF/echo.runtime.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echo-core",
                  "name": "ECHO Core Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "runtime_module",
                  "side": "both",
                  "trust": "trusted",
                  "official": true,
                  "standalone": true,
                  "requires": [],
                  "optional": [],
                  "provides": ["echo:services", "echo:diagnostics"],
                  "consumes": [],
                  "access": {"services": true}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("ashfall/META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoashfallprotocol",
                  "name": "ECHO Ashfall Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "content_pack",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": true,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "optional": ["echoworldcore"],
                  "provides": ["ashfall:chapter"],
                  "consumes": ["echo:services"],
                  "access": {"services": true}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("broken/META-INF/echo.native.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "broken-addon",
                  "name": "Broken Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "test_fixture",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["missing-required-module"],
                  "optional": [],
                  "provides": ["broken:feature"],
                  "consumes": [],
                  "access": {}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("tooling/META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "echoreportcore",
                  "name": "ECHO ReportCore Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "library",
                  "role": "report_core",
                  "side": "both",
                  "trust": "trusted",
                  "official": true,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "optional": [],
                  "provides": ["reports.write"],
                  "consumes": [],
                  "access": {"services": true}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("dev/META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "signalosexample",
                  "name": "SignalOS Example Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "role": "developer_tool",
                  "side": "common",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": true,
                  "requires": ["echo-core"],
                  "optional": [],
                  "provides": ["signalos.example_content"],
                  "consumes": [],
                  "gameModes": ["creator_tools"],
                  "access": {"services": true}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("disabled/META-INF/echo.mod.json"), """
                {
                  "schema": "echo.runtime.module.v1",
                  "id": "minecraft-only",
                  "name": "Minecraft Only Runtime Fixture",
                  "version": "1.0.0",
                  "kind": "addon",
                  "side": "both",
                  "trust": "sandboxed",
                  "official": false,
                  "standalone": false,
                  "requires": [],
                  "optional": [],
                  "provides": ["minecraft.only"],
                  "consumes": [],
                  "access": {}
                }
                """);
        writeDescriptor(fixtureRoot.resolve("neoforge-only/META-INF/neoforge.mods.toml"), """
                license="All Rights Reserved"

                [[mods]]
                modId="neoforge_only_fixture"
                version="1.0.0"
                displayName="NeoForge Only Fixture"

                [[dependencies.neoforge_only_fixture]]
                modId="neoforge"
                type="required"
                versionRange="[26.1.2.29-beta,)"
                ordering="NONE"
                side="BOTH"
                """);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeModuleRuntimeResult result = EchoRuntimeModuleManager.descriptorOnly()
                .run(List.of(fixtureRoot), services);

        EchoRuntimeModuleRegistry registry = result.registry();
        require(registry.descriptors().size() == 6, "six descriptors should be discovered");
        require(registry.find("neoforge_only_fixture").isEmpty(),
                "neoforge.mods.toml should remain outside standalone module activation");
        require(registry.lifecycle("echo-core") == EchoRuntimeModuleLifecycle.READY, "echo-core should be READY");
        require(registry.lifecycle("echoashfallprotocol") == EchoRuntimeModuleLifecycle.READY, "ashfall fixture should be READY");
        require(registry.lifecycle("broken-addon") == EchoRuntimeModuleLifecycle.FAILED, "broken fixture should fail safely");
        require(registry.lifecycle("minecraft-only") == EchoRuntimeModuleLifecycle.DISABLED,
                "standalone=false fixture should be DISABLED");
        require(registry.runtimeStatus("echo-core") == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "echo-core should be runtime-active");
        require(registry.runtimeStatus("echoashfallprotocol") == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "ashfall fixture should be runtime-active");
        require(registry.runtimeStatus("echoreportcore") == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "report fixture should be runtime-tooling-only");
        require(registry.runtimeStatus("signalosexample") == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "SignalOS example fixture should be runtime-dev-only");
        require(registry.runtimeStatus("minecraft-only") == EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON,
                "standalone=false fixture should be runtime-disabled-with-reason");
        require(registry.runtimeStatus("broken-addon") == EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON,
                "broken fixture should be runtime-disabled-with-reason");
        require(registry.trace("echoashfallprotocol").contains(EchoRuntimeModuleLifecycle.SERVICES_BOUND),
                "ashfall fixture should bind services");

        EchoRuntimeModuleGraph moduleGraph = services.require(EchoRuntimeModuleGraph.class);
        require(moduleGraph.dependencyEdges().stream()
                        .anyMatch(edge -> edge.fromModuleId().equals("echoashfallprotocol")
                                && edge.toModuleId().equals("echo-core")
                                && edge.kind().equals("requires")),
                "module graph should include ashfall -> echo-core required edge");
        require(moduleGraph.failedModuleIds().contains("broken-addon"),
                "module graph should record broken module failure");

        EchoRuntimeFeatureGraph featureGraph = services.require(EchoRuntimeFeatureGraph.class);
        require(featureGraph.providersByFeature().containsKey("echo:services"),
                "feature graph should include echo:services provider");
        require(featureGraph.consumersByFeature().get("echo:services").contains("echoashfallprotocol"),
                "feature graph should include ashfall as echo:services consumer");

        EchoRuntimeModuleSandboxPolicy sandboxPolicy = services.require(EchoRuntimeModuleSandboxPolicy.class);
        require(sandboxPolicy.descriptorOnly(), "module runtime should stay descriptor-only");
        require(!sandboxPolicy.classloaderCreationAllowed(), "module runtime should not create classloaders");
        require(!sandboxPolicy.moduleCodeExecutionAllowed(), "module runtime should not execute module code");
        require(!diagnostics.diagnostics().isEmpty(), "bad module should emit diagnostics without crashing");

        EchoRuntimeSystemModuleStatusReport systemStatus = EchoRuntimeSystemModuleStatusReport.forRequiredModules(
                registry,
                List.of("echo-core", "echoreportcore", "signalosexample")
        );
        require(systemStatus.require("echo-core").status() == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "required loaded system module should report runtime-active");
        require(systemStatus.require("echoreportcore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "required tooling system module should report runtime-tooling-only");
        require(systemStatus.require("signalosexample").status() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "required example system module should report runtime-dev-only");
        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                result,
                services,
                diagnostics,
                sandboxPolicy,
                systemStatus
        );
        System.out.println("phase14.3 module runtime smoke PASS descriptors=6 failed=1 statuses=3");
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoRuntimeModuleRuntimeResult result,
            EchoDefaultRuntimeServiceRegistry services,
            EchoRuntimeLogBridge diagnostics,
            EchoRuntimeModuleSandboxPolicy sandboxPolicy,
            EchoRuntimeSystemModuleStatusReport systemStatus
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoRuntimeModuleRegistry registry = result.registry();
        EchoRuntimeModuleGraph moduleGraph = result.moduleGraph();
        EchoRuntimeFeatureGraph featureGraph = result.featureGraph();
        List<String> moduleIds = registry.descriptors().stream()
                .map(descriptor -> descriptor.id())
                .sorted()
                .toList();
        Map<String, EchoRuntimeModuleLifecycle> lifecycles = registry.lifecycleSnapshot();
        Map<String, EchoRuntimeModuleStatus> statuses = registry.runtimeStatusSnapshot();

        write(root.resolve("runtime-modules.json"), """
                {
                  "schema": "echo.standalone.runtime_modules.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "mode": "descriptor-only",
                  "descriptorCount": %d,
                  "readyCount": %d,
                  "failedCount": %d,
                  "disabledCount": %d,
                  "ignoredNeoForgeOnlyDescriptor": true,
                  "descriptorOnly": %s,
                  "classloaderCreationAllowed": %s,
                  "moduleCodeExecutionAllowed": %s,
                  "moduleIds": %s,
                  "diagnosticCount": %d,
                  "diagnosticCodes": %s
                }
                """.formatted(
                moduleIds.size(),
                countLifecycle(lifecycles, EchoRuntimeModuleLifecycle.READY),
                countLifecycle(lifecycles, EchoRuntimeModuleLifecycle.FAILED),
                countLifecycle(lifecycles, EchoRuntimeModuleLifecycle.DISABLED),
                sandboxPolicy.descriptorOnly(),
                sandboxPolicy.classloaderCreationAllowed(),
                sandboxPolicy.moduleCodeExecutionAllowed(),
                jsonArray(moduleIds),
                diagnostics.diagnostics().size(),
                jsonArray(diagnostics.countsByCode().keySet().stream().sorted().toList())
        ));

        write(root.resolve("runtime-module-graph.json"), """
                {
                  "schema": "echo.standalone.runtime_module_graph.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "moduleIds": %s,
                  "dependencyOrderedModuleIds": %s,
                  "dependencyEdges": %s,
                  "failedModuleIds": %s,
                  "issueCount": %d,
                  "hasBlockingIssues": %s,
                  "issues": %s
                }
                """.formatted(
                jsonArray(moduleGraph.moduleIds()),
                jsonArray(moduleGraph.dependencyOrderedModuleIds()),
                jsonEdges(moduleGraph.dependencyEdges()),
                jsonArray(moduleGraph.failedModuleIds()),
                moduleGraph.issues().size(),
                moduleGraph.hasBlockingIssues(),
                jsonIssues(moduleGraph.issues())
        ));

        write(root.resolve("runtime-feature-graph.json"), """
                {
                  "schema": "echo.standalone.runtime_feature_graph.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "providersByFeature": %s,
                  "consumersByFeature": %s,
                  "missingRequiredFeatures": %s
                }
                """.formatted(
                jsonStringListMap(featureGraph.providersByFeature()),
                jsonStringListMap(featureGraph.consumersByFeature()),
                jsonArray(featureGraph.missingRequiredFeatures())
        ));

        write(root.resolve("runtime-services.json"), """
                {
                  "schema": "echo.standalone.runtime_services.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "serviceTypes": %s,
                  "moduleRegistryBound": %s,
                  "moduleGraphBound": %s,
                  "featureGraphBound": %s,
                  "sandboxPolicyBound": %s,
                  "descriptorOnly": %s,
                  "moduleCodeExecutionAllowed": %s,
                  "systemStatus": %s
                }
                """.formatted(
                jsonArray(services.snapshot().keySet().stream()
                        .map(Class::getName)
                        .sorted()
                        .toList()),
                services.find(EchoRuntimeModuleRegistry.class).isPresent(),
                services.find(EchoRuntimeModuleGraph.class).isPresent(),
                services.find(EchoRuntimeFeatureGraph.class).isPresent(),
                services.find(EchoRuntimeModuleSandboxPolicy.class).isPresent(),
                sandboxPolicy.descriptorOnly(),
                sandboxPolicy.moduleCodeExecutionAllowed(),
                jsonSystemStatus(systemStatus)
        ));

        write(root.resolve("runtime-module-status.json"), """
                {
                  "schema": "echo.standalone.runtime_module_status.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "lifecycles": %s,
                  "runtimeStatuses": %s,
                  "notes": %s,
                  "activeCount": %d,
                  "toolingOnlyCount": %d,
                  "devOnlyCount": %d,
                  "disabledWithReasonCount": %d
                }
                """.formatted(
                jsonLifecycleMap(lifecycles),
                jsonStatusMap(statuses),
                jsonNotes(moduleIds, registry),
                countStatus(statuses, EchoRuntimeModuleStatus.RUNTIME_ACTIVE),
                countStatus(statuses, EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY),
                countStatus(statuses, EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY),
                countStatus(statuses, EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON)
        ));
    }

    private static int countLifecycle(Map<String, EchoRuntimeModuleLifecycle> lifecycles, EchoRuntimeModuleLifecycle expected) {
        return (int) lifecycles.values().stream().filter(expected::equals).count();
    }

    private static int countStatus(Map<String, EchoRuntimeModuleStatus> statuses, EchoRuntimeModuleStatus expected) {
        return (int) statuses.values().stream().filter(expected::equals).count();
    }

    private static String jsonEdges(List<EchoRuntimeModuleGraph.Edge> edges) {
        return edges.stream()
                .map(edge -> "{\"fromModuleId\": \"" + escape(edge.fromModuleId())
                        + "\", \"toModuleId\": \"" + escape(edge.toModuleId())
                        + "\", \"kind\": \"" + escape(edge.kind()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonIssues(List<dev.echo.standalone.runtime.modules.EchoRuntimeModuleIssue> issues) {
        return issues.stream()
                .map(issue -> "{\"code\": \"" + escape(issue.code())
                        + "\", \"severity\": \"" + issue.severity().name()
                        + "\", \"moduleId\": \"" + escape(issue.moduleId())
                        + "\", \"summary\": \"" + escape(issue.summary()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonSystemStatus(EchoRuntimeSystemModuleStatusReport report) {
        return report.entries().stream()
                .map(entry -> "{\"moduleId\": \"" + escape(entry.moduleId())
                        + "\", \"status\": \"" + entry.status().id()
                        + "\", \"reason\": \"" + escape(entry.reason()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonNotes(List<String> moduleIds, EchoRuntimeModuleRegistry registry) {
        return moduleIds.stream()
                .map(moduleId -> "\"" + escape(moduleId) + "\": "
                        + jsonArray(registry.notes(moduleId).stream().sorted().toList()))
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonStringListMap(Map<String, List<String>> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": " + jsonArray(entry.getValue()))
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonLifecycleMap(Map<String, EchoRuntimeModuleLifecycle> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + entry.getValue().name() + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonStatusMap(Map<String, EchoRuntimeModuleStatus> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + entry.getValue().id() + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void writeDescriptor(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
