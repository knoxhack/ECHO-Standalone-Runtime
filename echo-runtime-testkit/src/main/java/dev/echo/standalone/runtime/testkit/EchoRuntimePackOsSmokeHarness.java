package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeFeatureGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.packos.EchoRuntimePackChannel;
import dev.echo.standalone.runtime.packos.EchoRuntimePackCompatibilityReport;
import dev.echo.standalone.runtime.packos.EchoRuntimePackIntegrityReport;
import dev.echo.standalone.runtime.packos.EchoRuntimePackMountPlan;
import dev.echo.standalone.runtime.packos.EchoRuntimePackOs;
import dev.echo.standalone.runtime.packos.EchoRuntimePackRepairPlan;
import dev.echo.standalone.runtime.packos.EchoRuntimePackSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimePackOsSmokeHarness {
    private EchoRuntimePackOsSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-packos-smoke");
        Path modulesRoot = fixtureRoot.resolve("modules");
        writeDescriptor(modulesRoot.resolve("echo-core/META-INF/echo.runtime.json"), """
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
        writeDescriptor(modulesRoot.resolve("ashfall/META-INF/echo.mod.json"), """
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
                  "optional": [],
                  "provides": ["ashfall:chapter"],
                  "consumes": ["echo:services"],
                  "access": {"services": true}
                }
                """);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(List.of(modulesRoot), services);
        EchoRuntimeModuleGraph moduleGraph = modules.moduleGraph();
        EchoRuntimeFeatureGraph featureGraph = modules.featureGraph();

        Path packRoot = fixtureRoot.resolve("packs/ashfall");
        Path lockfile = packRoot.resolve("ashfall.lock.json");
        writeFile(lockfile, """
                {
                  "schema": "echo.runtime.pack_lock.v1",
                  "packId": "ashfall",
                  "runtimeVersion": "0.1.0-phase14.4-packos-runtime",
                  "lockedModules": {
                    "echo-core": "1.0.0",
                    "echoashfallprotocol": "1.0.0"
                  },
                  "lockedFeatures": ["ashfall:chapter", "echo:services"]
                }
                """);
        Path profile = packRoot.resolve("ashfall.pack.json");
        writeFile(profile, """
                {
                  "schema": "echo.runtime.pack.v1",
                  "packId": "ashfall",
                  "packName": "ECHO: Ashfall Protocol",
                  "variant": "dev_sandbox",
                  "channel": "alpha",
                  "runtimeVersion": "0.1.0-phase14.4-packos-runtime",
                  "enabledModules": ["echo-core", "echoashfallprotocol"],
                  "enabledFeatures": ["ashfall:chapter", "echo:services"],
                  "lockfile": "ashfall.lock.json",
                  "saveCompatibility": {
                    "minimumRuntimeVersion": "0.1.0-phase14.4-packos-runtime",
                    "migrationPolicy": "plan_only"
                  },
                  "assetPacks": ["assets/ashfall-base", "assets/ashfall-dev"],
                  "dataPacks": ["data/ashfall-base"],
                  "theme": "ashfall_cyberglass",
                  "launchMode": "headless-test"
                }
                """);

        EchoRuntimePackOs packOs = EchoRuntimePackOs.createDefault();
        EchoRuntimePackSession session = packOs.loadSession(profile, moduleGraph, featureGraph, services);
        require(session.launchAllowed(), "valid Ashfall pack session should be launch-allowed");
        require(session.profile().channel() == EchoRuntimePackChannel.ALPHA, "channel should parse as alpha");
        require(session.profile().variant().equals("dev_sandbox"), "variant should be exposed");
        require(session.mountPlan().mounts().size() == 5, "mount plan should include runtime defaults, assets, data, and theme");
        require(!session.repairPlan().executionAllowed(), "repair plan must never execute in Phase 14.4");
        require(services.require(EchoRuntimePackSession.class).packId().equals("ashfall"), "pack session service should bind");
        require(services.require(EchoRuntimePackMountPlan.class).theme().equals("ashfall_cyberglass"), "mount plan service should bind");
        require(services.require(EchoRuntimePackIntegrityReport.class).integrityReady(), "integrity report should bind");
        require(services.require(EchoRuntimePackCompatibilityReport.class).compatible(), "compatibility report should bind");

        Path badProfile = packRoot.resolve("ashfall-bad.pack.json");
        writeFile(badProfile, """
                {
                  "schema": "echo.runtime.pack.v1",
                  "packId": "ashfall",
                  "packName": "ECHO: Ashfall Protocol Broken Fixture",
                  "variant": "dev_sandbox",
                  "channel": "alpha",
                  "runtimeVersion": "0.1.0-phase14.4-packos-runtime",
                  "enabledModules": ["echo-core", "missing-addon"],
                  "enabledFeatures": ["missing:feature"],
                  "lockfile": "ashfall.lock.json",
                  "saveCompatibility": {
                    "minimumRuntimeVersion": "0.1.0-phase14.4-packos-runtime",
                    "migrationPolicy": "execute"
                  },
                  "assetPacks": ["assets/ashfall-base"],
                  "dataPacks": ["data/ashfall-base"],
                  "theme": "ashfall_cyberglass",
                  "launchMode": "headless-test"
                }
                """);
        EchoRuntimePackSession badSession = packOs.loadSession(
                badProfile,
                moduleGraph,
                featureGraph,
                new EchoDefaultRuntimeServiceRegistry()
        );
        require(!badSession.launchAllowed(), "incompatible pack state should be refused");
        require(!badSession.compatibilityReport().blockers().isEmpty(), "bad session should explain blockers");
        require(!badSession.repairPlan().executionAllowed(), "bad session repair plan must still be planning-only");

        EchoRuntimePackRepairPlan repairPlan = badSession.repairPlan();
        require(!repairPlan.plannedActions().isEmpty(), "bad session should produce repair advice");

        System.out.println("phase14.4 packos runtime smoke PASS pack=ashfall mounts="
                + session.mountPlan().mounts().size()
                + " blockers="
                + badSession.compatibilityReport().blockers().size());
    }

    private static void writeDescriptor(Path path, String content) throws IOException {
        writeFile(path, content);
    }

    private static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
