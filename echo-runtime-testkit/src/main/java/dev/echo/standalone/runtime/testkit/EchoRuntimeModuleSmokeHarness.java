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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        System.out.println("phase14.3 module runtime smoke PASS descriptors=6 failed=1 statuses=3");
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
