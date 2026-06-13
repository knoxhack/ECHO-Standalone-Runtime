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
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
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
        writeReports(workspaceRoot, session, badSession);

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

    private static void writeReports(
            Path workspaceRoot,
            EchoRuntimePackSession session,
            EchoRuntimePackSession badSession
    ) throws IOException {
        Path reportDir = standaloneRoot(workspaceRoot).resolve("reports/echo/standalone");
        Files.createDirectories(reportDir);
        Files.writeString(reportDir.resolve("runtime-packos.json"), runtimePackOsReport(session, badSession));
        Files.writeString(reportDir.resolve("runtime-pack-session.json"), runtimePackSessionReport(session));
        Files.writeString(reportDir.resolve("runtime-pack-integrity.json"), runtimePackIntegrityReport(session, badSession));
        Files.writeString(reportDir.resolve("runtime-pack-mount-plan.json"), runtimePackMountPlanReport(session));
    }

    private static String runtimePackOsReport(
            EchoRuntimePackSession session,
            EchoRuntimePackSession badSession
    ) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_packos.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimePackOsSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"packId\": \"" + session.packId() + "\",\n"
                + "  \"channel\": \"" + session.profile().channel().name().toLowerCase() + "\",\n"
                + "  \"variant\": \"" + session.profile().variant() + "\",\n"
                + "  \"runtimeVersion\": \"" + session.profile().runtimeVersion() + "\",\n"
                + "  \"launchMode\": \"" + session.profile().launchMode().id() + "\",\n"
                + "  \"launchAllowed\": " + session.launchAllowed() + ",\n"
                + "  \"badProfileLaunchAllowed\": " + badSession.launchAllowed() + ",\n"
                + "  \"badProfileBlockers\": " + badSession.compatibilityReport().blockers().size() + ",\n"
                + "  \"badProfileRepairActions\": " + badSession.repairPlan().plannedActions().size() + ",\n"
                + "  \"repairExecutionAllowed\": " + session.repairPlan().executionAllowed() + "\n"
                + "}\n";
    }

    private static String runtimePackSessionReport(EchoRuntimePackSession session) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_pack_session.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimePackOsSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"packId\": \"" + session.packId() + "\",\n"
                + "  \"packName\": \"" + escape(session.profile().packName()) + "\",\n"
                + "  \"enabledModuleCount\": " + session.profile().enabledModules().size() + ",\n"
                + "  \"enabledFeatureCount\": " + session.profile().enabledFeatures().size() + ",\n"
                + "  \"lockedModuleCount\": " + session.lockfile().lockedModules().size() + ",\n"
                + "  \"lockedFeatureCount\": " + session.lockfile().lockedFeatures().size() + ",\n"
                + "  \"launchAllowed\": " + session.launchAllowed() + ",\n"
                + "  \"sessionServiceBound\": true,\n"
                + "  \"mountPlanServiceBound\": true,\n"
                + "  \"integrityReportServiceBound\": true,\n"
                + "  \"compatibilityReportServiceBound\": true\n"
                + "}\n";
    }

    private static String runtimePackIntegrityReport(
            EchoRuntimePackSession session,
            EchoRuntimePackSession badSession
    ) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_pack_integrity.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimePackOsSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"integrityReady\": " + session.integrityReport().integrityReady() + ",\n"
                + "  \"integrityBlockers\": " + session.integrityReport().blockers().size() + ",\n"
                + "  \"integrityWarnings\": " + session.integrityReport().warnings().size() + ",\n"
                + "  \"compatible\": " + session.compatibilityReport().compatible() + ",\n"
                + "  \"compatibilityBlockers\": " + session.compatibilityReport().blockers().size() + ",\n"
                + "  \"badProfileCompatible\": " + badSession.compatibilityReport().compatible() + ",\n"
                + "  \"badProfileCompatibilityBlockers\": " + badSession.compatibilityReport().blockers().size() + ",\n"
                + "  \"repairExecutionAllowed\": " + session.repairPlan().executionAllowed() + ",\n"
                + "  \"badProfileRepairExecutionAllowed\": " + badSession.repairPlan().executionAllowed() + "\n"
                + "}\n";
    }

    private static String runtimePackMountPlanReport(EchoRuntimePackSession session) {
        long assetMounts = session.mountPlan().mounts().stream()
                .filter(mount -> mount.kind().equals("asset"))
                .count();
        long dataMounts = session.mountPlan().mounts().stream()
                .filter(mount -> mount.kind().equals("data"))
                .count();
        long themeMounts = session.mountPlan().mounts().stream()
                .filter(mount -> mount.kind().equals("theme"))
                .count();
        boolean runtimeDefaultsMounted = session.mountPlan().mounts().stream()
                .anyMatch(mount -> mount.path().equals("runtime/defaults"));
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_pack_mount_plan.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimePackOsSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"packId\": \"" + session.packId() + "\",\n"
                + "  \"mountCount\": " + session.mountPlan().mounts().size() + ",\n"
                + "  \"assetMounts\": " + assetMounts + ",\n"
                + "  \"dataMounts\": " + dataMounts + ",\n"
                + "  \"themeMounts\": " + themeMounts + ",\n"
                + "  \"runtimeDefaultsMounted\": " + runtimeDefaultsMounted + ",\n"
                + "  \"theme\": \"" + session.mountPlan().theme() + "\"\n"
                + "}\n";
    }

    private static Path standaloneRoot(Path workspaceRoot) {
        if (workspaceRoot.getFileName() != null
                && workspaceRoot.getFileName().toString().equalsIgnoreCase("echo-standalone-runtime")) {
            return workspaceRoot;
        }
        if (Files.isDirectory(workspaceRoot.resolve("echo-runtime-app"))
                && Files.isRegularFile(workspaceRoot.resolve("settings.gradle"))) {
            return workspaceRoot;
        }
        Path nested = workspaceRoot.resolve("echo-standalone-runtime");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        return workspaceRoot;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
