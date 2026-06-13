package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeBootContext;
import dev.echo.standalone.runtime.app.EchoRuntimeBootResult;
import dev.echo.standalone.runtime.app.EchoRuntimeExitCode;
import dev.echo.standalone.runtime.app.EchoRuntimeLauncher;
import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilities;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;
import dev.echo.standalone.runtime.contracts.EchoRuntimeTickLayer;

import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeAppSmokeHarness {
    private EchoRuntimeAppSmokeHarness() {
    }

    public static void main(String[] args) {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoRuntimeBootResult headless = new EchoRuntimeLauncher().launch(EchoRuntimeBootContext.headless(workspaceRoot));
        require(headless.success(), "headless boot should succeed");
        require(headless.finalLifecycle() == EchoRuntimeLifecycle.STOPPED, "headless boot should stop cleanly");
        require(headless.ticksRun() == 3, "headless boot should run three deterministic ticks");
        require(headless.lifecycleTrace().contains(EchoRuntimeLifecycle.RUNNING), "headless boot should reach RUNNING");
        require(headless.diagnostics().stream().anyMatch(diagnostic ->
                        diagnostic.code().equals("ECHO-STANDALONE-LIFECYCLE-TRANSITION")
                                && diagnostic.runtimeLayer().equals("app_runtime")),
                "headless boot should emit lifecycle diagnostics through the diagnostic writer");
        require(headless.shutdownHook().reason().equals("headless_tick_loop_complete")
                        && !headless.shutdownHook().userRequested()
                        && !headless.shutdownHook().saveBeforeExit(),
                "headless boot should expose the deterministic shutdown hook");
        require(headless.systemModuleBoot().adapterCoreRuntimeBridgeActive(),
                "headless boot should attach the AdapterCore system-module runtime bridge");
        require(headless.systemModuleBoot().moduleDescriptors() >= 90,
                "headless boot should discover the full Echo module catalog");
        require(headless.systemModuleBoot().requiredSystemModules() == 8,
                "headless boot should activate all Agent 3 required system modules");
        require(headless.systemModuleBoot().executableSystemModules() == 8,
                "headless boot should execute all Agent 3 required system modules");
        requireRuntimeModes();
        requireTickLayers();

        EchoRuntimeBootResult playableBeta = new EchoRuntimeLauncher()
                .launch(EchoRuntimeBootContext.playableBeta(workspaceRoot));
        require(playableBeta.success(), "playable-beta boot should succeed");
        require(playableBeta.finalLifecycle() == EchoRuntimeLifecycle.STOPPED,
                "playable-beta boot should stop cleanly");
        require(playableBeta.systemModuleBoot().adapterCoreRuntimeBridgeActive(),
                "playable-beta boot should attach the AdapterCore system-module runtime bridge");
        require(playableBeta.systemModuleBoot().executableSystemModules() == 8,
                "playable-beta boot should execute all Agent 3 required system modules");
        require(playableBeta.ashfallFirstPlayableLoopReady(),
                "playable-beta boot should run the Ashfall first playable loop checklist");
        EchoRuntimeBootResult packagedTester = new EchoRuntimeLauncher()
                .launch(EchoRuntimeBootContext.packagedTester(workspaceRoot));
        require(packagedTester.success(), "packaged-tester boot should succeed");
        require(packagedTester.systemModuleBoot().adapterCoreRuntimeBridgeActive(),
                "packaged-tester boot should attach the AdapterCore runtime bridge");
        require(packagedTester.ashfallFirstPlayableLoopReady() && packagedTester.liveWindowWalkthroughReady(),
                "packaged-tester boot should run the live packaged walkthrough");

        EchoRuntimeBootResult crashed = new EchoRuntimeLauncher().launch(new EchoRuntimeBootContext(
                "echo-standalone-crash-smoke",
                EchoRuntimeMode.HEADLESS_TEST,
                workspaceRoot,
                workspaceRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(50),
                0,
                new CrashingApplication(),
                EchoRuntimeCapabilities.empty(),
                Map.of()
        ));
        require(crashed.exitCode() == EchoRuntimeExitCode.CRASHED, "crash smoke should return CRASHED");
        require(crashed.crashHandled(), "crash smoke should be caught by crash boundary");
        require(crashed.finalLifecycle() == EchoRuntimeLifecycle.CRASHED, "crash smoke should end CRASHED");
        require(crashed.diagnostics().stream().anyMatch(diagnostic -> diagnostic.severity() == EchoRuntimeDiagnosticSeverity.FATAL),
                "crash smoke should emit a fatal diagnostic");
        require(hasLifecycleDiagnostics(crashed),
                "crash smoke should preserve lifecycle diagnostics before the fatal diagnostic");

        try {
            writeBootReport(workspaceRoot, headless, playableBeta, packagedTester, crashed);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write app boot system-module report", exception);
        }
        System.out.println("phase14.2 app runtime smoke PASS ticksRun=" + headless.ticksRun()
                + " systemModules=" + headless.systemModuleBoot().executableSystemModules()
                + "/" + headless.systemModuleBoot().requiredSystemModules()
                + " adapterCoreBridge=" + headless.systemModuleBoot().adapterCoreRuntimeBridgeActive()
                + " playableBeta=" + playableBeta.ashfallFirstPlayableLoopReady()
                + " packagedTester=" + packagedTester.liveWindowWalkthroughReady());
    }

    private static void writeBootReport(
            Path workspaceRoot,
            EchoRuntimeBootResult headless,
            EchoRuntimeBootResult playableBeta,
            EchoRuntimeBootResult packagedTester,
            EchoRuntimeBootResult crashed
    ) throws IOException {
        Path reportDir = standaloneRoot(workspaceRoot).resolve("reports/echo/standalone");
        Files.createDirectories(reportDir);
        Files.writeString(reportDir.resolve("runtime-boot.json"), runtimeBootReport(headless, playableBeta, packagedTester));
        Files.writeString(reportDir.resolve("runtime-lifecycle.json"), runtimeLifecycleReport(headless));
        Files.writeString(reportDir.resolve("runtime-tick-loop.json"), runtimeTickLoopReport(headless));
        Files.writeString(reportDir.resolve("runtime-crash-boundary.json"), runtimeCrashBoundaryReport(crashed));
        Path report = reportDir.resolve("runtime-app-boot-system-modules.json");
        String json = "{\n"
                + "  \"schema\": \"echo.standalone.app_boot_system_modules.v1\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"runtimeModes\": " + stringArray(Arrays.stream(EchoRuntimeMode.values())
                .map(EchoRuntimeMode::id).toList()) + ",\n"
                + "  \"tickLayers\": " + stringArray(Arrays.stream(EchoRuntimeTickLayer.values())
                .map(EchoRuntimeTickLayer::id).toList()) + ",\n"
                + "  \"exitCode\": " + headless.exitCode().code() + ",\n"
                + "  \"finalLifecycle\": \"" + headless.finalLifecycle().id() + "\",\n"
                + "  \"ticksRun\": " + headless.ticksRun() + ",\n"
                + "  \"diagnosticWriterActive\": " + hasLifecycleDiagnostics(headless) + ",\n"
                + "  \"diagnosticCount\": " + headless.diagnostics().size() + ",\n"
                + "  \"shutdownHookReason\": \"" + headless.shutdownHook().reason() + "\",\n"
                + "  \"shutdownHookUserRequested\": " + headless.shutdownHook().userRequested() + ",\n"
                + "  \"shutdownHookSaveBeforeExit\": " + headless.shutdownHook().saveBeforeExit() + ",\n"
                + "  \"moduleDescriptors\": " + headless.systemModuleBoot().moduleDescriptors() + ",\n"
                + "  \"adapterCoreCoverageTotal\": " + headless.systemModuleBoot().adapterCoreCoverageTotal() + ",\n"
                + "  \"requiredSystemModules\": " + headless.systemModuleBoot().requiredSystemModules() + ",\n"
                + "  \"executableSystemModules\": " + headless.systemModuleBoot().executableSystemModules() + ",\n"
                + "  \"adapterCoreRuntimeBridgeActive\": "
                + headless.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "  \"playableBetaExitCode\": " + playableBeta.exitCode().code() + ",\n"
                + "  \"playableBetaLifecycle\": \"" + playableBeta.finalLifecycle().id() + "\",\n"
                + "  \"playableBetaAdapterCoreRuntimeBridgeActive\": "
                + playableBeta.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "  \"ashfallFirstPlayableLoopReady\": "
                + playableBeta.ashfallFirstPlayableLoopReady() + ",\n"
                + "  \"packagedTesterExitCode\": " + packagedTester.exitCode().code() + ",\n"
                + "  \"packagedTesterMode\": \"" + EchoRuntimeMode.PACKAGED_TESTER.id() + "\",\n"
                + "  \"packagedTesterAdapterCoreRuntimeBridgeActive\": "
                + packagedTester.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "  \"packagedTesterLiveWindowWalkthroughReady\": "
                + packagedTester.liveWindowWalkthroughReady() + "\n"
                + "}\n";
        Files.writeString(report, json);
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
        return workspaceRoot.resolve("echo-standalone-runtime");
    }

    private static String runtimeBootReport(
            EchoRuntimeBootResult headless,
            EchoRuntimeBootResult playableBeta,
            EchoRuntimeBootResult packagedTester
    ) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_boot.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeAppSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"summary\": \"Headless, playable-beta, and packaged-tester app boot paths reached stopped success with AdapterCore system-module bridge activity.\",\n"
                + "  \"headless\": {\n"
                + "    \"exitCode\": " + headless.exitCode().code() + ",\n"
                + "    \"finalLifecycle\": \"" + headless.finalLifecycle().id() + "\",\n"
                + "    \"ticksRun\": " + headless.ticksRun() + ",\n"
                + "    \"lifecycleReachedRunning\": " + headless.lifecycleTrace().contains(EchoRuntimeLifecycle.RUNNING) + ",\n"
                + "    \"diagnosticWriterActive\": " + hasLifecycleDiagnostics(headless) + ",\n"
                + "    \"adapterCoreRuntimeBridgeActive\": " + headless.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "    \"moduleDescriptors\": " + headless.systemModuleBoot().moduleDescriptors() + ",\n"
                + "    \"adapterCoreCoverageTotal\": " + headless.systemModuleBoot().adapterCoreCoverageTotal() + ",\n"
                + "    \"requiredSystemModules\": " + headless.systemModuleBoot().requiredSystemModules() + ",\n"
                + "    \"executableSystemModules\": " + headless.systemModuleBoot().executableSystemModules() + "\n"
                + "  },\n"
                + "  \"playableBeta\": {\n"
                + "    \"exitCode\": " + playableBeta.exitCode().code() + ",\n"
                + "    \"finalLifecycle\": \"" + playableBeta.finalLifecycle().id() + "\",\n"
                + "    \"adapterCoreRuntimeBridgeActive\": " + playableBeta.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "    \"ashfallFirstPlayableLoopReady\": " + playableBeta.ashfallFirstPlayableLoopReady() + ",\n"
                + "    \"executableSystemModules\": " + playableBeta.systemModuleBoot().executableSystemModules() + "\n"
                + "  },\n"
                + "  \"packagedTester\": {\n"
                + "    \"exitCode\": " + packagedTester.exitCode().code() + ",\n"
                + "    \"finalLifecycle\": \"" + packagedTester.finalLifecycle().id() + "\",\n"
                + "    \"adapterCoreRuntimeBridgeActive\": " + packagedTester.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "    \"liveWindowWalkthroughReady\": " + packagedTester.liveWindowWalkthroughReady() + "\n"
                + "  }\n"
                + "}\n";
    }

    private static String runtimeLifecycleReport(EchoRuntimeBootResult headless) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_lifecycle.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeAppSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"summary\": \"Headless app runtime completed the deterministic lifecycle trace.\",\n"
                + "  \"finalLifecycle\": \"" + headless.finalLifecycle().id() + "\",\n"
                + "  \"lifecycleTrace\": " + stringArray(headless.lifecycleTrace().stream()
                .map(EchoRuntimeLifecycle::id).toList()) + ",\n"
                + "  \"lifecycleDiagnosticsPreserved\": " + hasLifecycleDiagnostics(headless) + "\n"
                + "}\n";
    }

    private static String runtimeTickLoopReport(EchoRuntimeBootResult headless) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_tick_loop.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeAppSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"summary\": \"Headless runtime executed the deterministic three-tick loop and clean shutdown hook.\",\n"
                + "  \"ticksRun\": " + headless.ticksRun() + ",\n"
                + "  \"tickLayers\": " + stringArray(Arrays.stream(EchoRuntimeTickLayer.values())
                .map(EchoRuntimeTickLayer::id).toList()) + ",\n"
                + "  \"shutdownHookReason\": \"" + headless.shutdownHook().reason() + "\",\n"
                + "  \"shutdownHookUserRequested\": " + headless.shutdownHook().userRequested() + ",\n"
                + "  \"shutdownHookSaveBeforeExit\": " + headless.shutdownHook().saveBeforeExit() + "\n"
                + "}\n";
    }

    private static String runtimeCrashBoundaryReport(EchoRuntimeBootResult crashed) {
        return "{\n"
                + "  \"schema\": \"echo.standalone.runtime_crash_boundary.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoRuntimeAppSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"summary\": \"App runtime crash boundary caught a simulated fatal app failure and preserved diagnostics.\",\n"
                + "  \"exitCode\": " + crashed.exitCode().code() + ",\n"
                + "  \"finalLifecycle\": \"" + crashed.finalLifecycle().id() + "\",\n"
                + "  \"crashHandled\": " + crashed.crashHandled() + ",\n"
                + "  \"fatalDiagnosticEmitted\": " + crashed.diagnostics().stream()
                .anyMatch(diagnostic -> diagnostic.severity() == EchoRuntimeDiagnosticSeverity.FATAL) + ",\n"
                + "  \"lifecycleDiagnosticsPreserved\": " + hasLifecycleDiagnostics(crashed) + "\n"
                + "}\n";
    }

    private static boolean hasLifecycleDiagnostics(EchoRuntimeBootResult result) {
        return result.diagnostics().stream().anyMatch(diagnostic ->
                diagnostic.code().equals("ECHO-STANDALONE-LIFECYCLE-TRANSITION")
                        && diagnostic.runtimeLayer().equals("app_runtime"));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireRuntimeModes() {
        List<String> ids = Arrays.stream(EchoRuntimeMode.values())
                .map(EchoRuntimeMode::id)
                .toList();
        require(ids.equals(List.of("headless-test", "windowed-dev", "playable-beta", "packaged-tester")),
                "runtime boot modes must match the Agent 3 boundary lock");
    }

    private static void requireTickLayers() {
        List<String> ids = Arrays.stream(EchoRuntimeTickLayer.values())
                .map(EchoRuntimeTickLayer::id)
                .toList();
        require(ids.equals(List.of(
                        "pre_tick",
                        "input",
                        "network",
                        "world",
                        "entity",
                        "player",
                        "gameplay",
                        "ui",
                        "audio",
                        "render",
                        "save",
                        "post_tick"
                )),
                "tick layers must match the Agent 3 app-loop contract");
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append("\"").append(values.get(i)).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    private static final class CrashingApplication implements EchoRuntimeApplication {
        @Override
        public String applicationId() {
            return "echo-crash-smoke";
        }

        @Override
        public EchoRuntime createRuntime(EchoRuntimeContext context) {
            return new EchoRuntime() {
                @Override
                public EchoRuntimeContext context() {
                    return context;
                }

                @Override
                public EchoRuntimeLifecycle lifecycle() {
                    return EchoRuntimeLifecycle.RUNNING;
                }

                @Override
                public void start() {
                    throw new IllegalStateException("simulated fatal app runtime failure");
                }

                @Override
                public void requestStop(EchoRuntimeShutdownHook shutdownHook) {
                }
            };
        }
    }
}
