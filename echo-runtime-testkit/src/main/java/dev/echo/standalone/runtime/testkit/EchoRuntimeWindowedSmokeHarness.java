package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeBootContext;
import dev.echo.standalone.runtime.app.EchoRuntimeBootResult;
import dev.echo.standalone.runtime.app.EchoRuntimeExitCode;
import dev.echo.standalone.runtime.app.EchoRuntimeLauncher;
import dev.echo.standalone.runtime.app.EchoWindowedRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCapabilities;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;
import dev.echo.standalone.runtime.render.EchoRecordingRenderBackend;
import dev.echo.standalone.runtime.render.EchoRenderViewport;
import dev.echo.standalone.runtime.render.EchoRenderWindowEventType;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleController;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleResult;
import dev.echo.standalone.runtime.render.EchoRenderWindowMode;
import dev.echo.standalone.runtime.render.EchoRenderWindowSettings;
import dev.echo.standalone.runtime.render.EchoRenderWindowState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeWindowedSmokeHarness {
    private EchoRuntimeWindowedSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path workspaceRoot = Path.of(".").toAbsolutePath().normalize();
        EchoRenderWindowLifecycleResult normal = windowLifecycleSmoke();
        EchoRenderWindowLifecycleResult crashedWindow = crashSafeWindowSmoke();
        EchoRuntimeBootResult windowedBoot = new EchoRuntimeLauncher()
                .launch(EchoRuntimeBootContext.windowed(workspaceRoot));
        EchoRuntimeBootResult crashedBoot = new EchoRuntimeLauncher()
                .launch(windowedCrashContext(workspaceRoot));
        EchoRuntimeBootResult deterministicLiveBoot = new EchoRuntimeLauncher()
                .launch(deterministicLiveContext(workspaceRoot));

        require(windowedBoot.success(), "windowed runtime boot should succeed");
        require(windowedBoot.finalLifecycle() == EchoRuntimeLifecycle.STOPPED,
                "windowed runtime should stop after close");
        require(windowedBoot.lifecycleTrace().contains(EchoRuntimeLifecycle.STARTING_RENDERER),
                "windowed runtime should start renderer lifecycle");
        require(windowedBoot.lifecycleTrace().contains(EchoRuntimeLifecycle.RUNNING),
                "windowed runtime should reach running lifecycle");
        require(windowedBoot.lifecycleTrace().contains(EchoRuntimeLifecycle.STOPPING),
                "windowed runtime should stop through stopping lifecycle");
        require(windowedBoot.ashfallFirstPlayableLoopReady(),
                "windowed runtime should run the Ashfall playable loop while renderer lifecycle is active");
        require(windowedBoot.liveWindowWalkthroughReady(),
                "windowed runtime should record an AdapterCore-backed walkthrough over the playable loop");

        require(crashedBoot.exitCode() == EchoRuntimeExitCode.CRASHED,
                "fault-injected windowed runtime should return CRASHED");
        require(crashedBoot.crashHandled(),
                "fault-injected windowed runtime should be caught by crash boundary");
        require(crashedBoot.finalLifecycle() == EchoRuntimeLifecycle.CRASHED,
                "fault-injected windowed runtime should end CRASHED");
        require(crashedBoot.diagnostics().stream().anyMatch(diagnostic ->
                        diagnostic.code().equals("ECHO-STANDALONE-WINDOW-CRASH-SAFE-SHUTDOWN")
                                && diagnostic.severity() == EchoRuntimeDiagnosticSeverity.FATAL),
                "fault-injected windowed runtime should emit crash-safe shutdown diagnostic");
        require(deterministicLiveBoot.success(), "deterministic live runtime boot should succeed");
        require(deterministicLiveBoot.finalLifecycle() == EchoRuntimeLifecycle.STOPPED,
                "deterministic live runtime should stop after deterministic smoke close");
        require(deterministicLiveBoot.ashfallFirstPlayableLoopReady(),
                "deterministic live runtime should run the Ashfall playable loop");
        require(deterministicLiveBoot.liveWindowWalkthroughReady(),
                "deterministic live runtime should record the live-window walkthrough action set");
        require(deterministicLiveBoot.shutdownHook().reason().equals("live_window_deterministic_close")
                        || deterministicLiveBoot.shutdownHook().reason().equals("live_window_headless_fallback"),
                "deterministic live runtime should stop through smoke close or headless fallback");
        writeReport(workspaceRoot, normal, crashedWindow, windowedBoot, crashedBoot, deterministicLiveBoot);

        System.out.println("phase15.2 windowed runtime smoke PASS events="
                + normal.events().size()
                + " crashEvents=" + crashedWindow.events().size()
                + " normalClosed=" + normal.closedSafely()
                + " crashClosed=" + crashedWindow.closedSafely()
                + " playableLoop=" + windowedBoot.ashfallFirstPlayableLoopReady()
                + " livePlayableLoop=" + deterministicLiveBoot.ashfallFirstPlayableLoopReady()
                + " liveWalkthrough=" + deterministicLiveBoot.liveWindowWalkthroughReady());
    }

    private static EchoRenderWindowLifecycleResult windowLifecycleSmoke() {
        EchoRecordingRenderBackend backend = new EchoRecordingRenderBackend();
        EchoRenderWindowLifecycleController controller = new EchoRenderWindowLifecycleController(backend);
        EchoRenderWindowLifecycleResult result = controller.runCrashSafe("window-lifecycle-smoke", () -> {
            EchoRenderWindowState created = controller.create(EchoRenderWindowSettings.windowedGame());
            require(created.open(), "created window should be open");
            require(created.mode() == EchoRenderWindowMode.WINDOWED, "created window should be windowed");
            require(created.viewport().width() == 1280 && created.viewport().height() == 720,
                    "created window should use default 1280x720 viewport");

            EchoRenderWindowState resized = controller.resize(new EchoRenderViewport(1600, 900));
            require(resized.viewport().width() == 1600 && resized.viewport().height() == 900,
                    "resize should update viewport");

            EchoRenderWindowState fullscreen = controller.fullscreen();
            require(fullscreen.mode() == EchoRenderWindowMode.FULLSCREEN,
                    "fullscreen transition should update mode");

            EchoRenderWindowState restored = controller.windowed(new EchoRenderViewport(1280, 720));
            require(restored.mode() == EchoRenderWindowMode.WINDOWED,
                    "windowed transition should restore windowed mode");

            EchoRenderWindowState closeRequested = controller.requestClose("user-close");
            require(closeRequested.closeRequested(), "close request should be recorded before closing");
            backend.close();
        });

        require(!result.crashHandled(), "normal window lifecycle should not report a crash");
        require(result.closedSafely(), "normal window lifecycle should close safely");
        require(!result.finalState().open(), "normal final window state should be closed");
        require(result.finalState().closeRequested(), "normal final window state should retain close request");
        require(hasEvent(result, EchoRenderWindowEventType.CREATED), "created event should be recorded");
        require(hasEvent(result, EchoRenderWindowEventType.RESIZED), "resize event should be recorded");
        require(hasEvent(result, EchoRenderWindowEventType.FULLSCREEN_ENTERED),
                "fullscreen event should be recorded");
        require(hasEvent(result, EchoRenderWindowEventType.WINDOWED_ENTERED),
                "windowed restore event should be recorded");
        require(hasEvent(result, EchoRenderWindowEventType.CLOSE_REQUESTED),
                "close request event should be recorded");
        require(hasEvent(result, EchoRenderWindowEventType.CLOSED), "closed event should be recorded");
        return result;
    }

    private static EchoRenderWindowLifecycleResult crashSafeWindowSmoke() {
        EchoRecordingRenderBackend backend = new EchoRecordingRenderBackend();
        EchoRenderWindowLifecycleController controller = new EchoRenderWindowLifecycleController(backend);
        EchoRenderWindowLifecycleResult result = controller.runCrashSafe("window-crash-smoke", () -> {
            controller.create(EchoRenderWindowSettings.windowedGame());
            controller.resize(new EchoRenderViewport(1440, 810));
            throw new IllegalStateException("simulated window failure");
        });

        require(result.crashHandled(), "crash window lifecycle should report handled crash");
        require(result.closedSafely(), "crash window lifecycle should close safely");
        require(!result.finalState().open(), "crash final window state should be closed");
        require(result.finalState().closeRequested(), "crash final window state should retain close request");
        require(hasEvent(result, EchoRenderWindowEventType.CRASH_SAFE_CLOSED),
                "crash-safe close event should be recorded");
        return result;
    }

    private static EchoRuntimeBootContext deterministicLiveContext(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-live-deterministic",
                EchoRuntimeMode.PLAYABLE_BETA,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "window.visible",
                        "window.lifecycle",
                        "ashfall.playable_loop",
                        "ashfall.playable_mission",
                        "ashfall.vertical_slice",
                        "live.deterministic_close"
                )),
                Map.of(
                        "echo.window.live", "true",
                        "echo.window.deterministicClose", "true",
                        "echo.window.playableLoop", "true"
                )
        );
    }

    private static EchoRuntimeBootContext windowedCrashContext(Path workspaceRoot) {
        Path normalizedRoot = workspaceRoot.toAbsolutePath().normalize();
        return new EchoRuntimeBootContext(
                "echo-standalone-windowed-crash",
                EchoRuntimeMode.PACKAGED_TESTER,
                normalizedRoot,
                normalizedRoot.resolve("reports/echo/standalone"),
                Instant.EPOCH,
                Duration.ofMillis(16),
                0,
                new EchoWindowedRuntimeApplication(),
                EchoRuntimeCapabilities.of(List.of(
                        "window.lifecycle",
                        "window.resize",
                        "window.fullscreen",
                        "window.close",
                        "window.crash_safe_shutdown"
                )),
                Map.of("echo.window.injectCrash", "true")
        );
    }

    private static boolean hasEvent(EchoRenderWindowLifecycleResult result, EchoRenderWindowEventType type) {
        return result.events().stream().anyMatch(event -> event.type() == type);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void writeReport(
            Path workspaceRoot,
            EchoRenderWindowLifecycleResult normal,
            EchoRenderWindowLifecycleResult crashedWindow,
            EchoRuntimeBootResult windowedBoot,
            EchoRuntimeBootResult crashedBoot,
            EchoRuntimeBootResult deterministicLiveBoot
    ) throws IOException {
        Path standaloneRoot = standaloneRoot(workspaceRoot);
        Path report = standaloneRoot.resolve("reports/echo/standalone/runtime-windowed.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, "{\n"
                + "  \"schema\": \"echo.standalone.windowed_runtime.v2\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"phase15.2-windowed-runtime\",\n"
                + "  \"phase\": \"15.2\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"summary\": \"Windowed and live-smoke runtime boot paths create the renderer lifecycle, execute the AdapterCore-backed Ashfall playable loop, close cleanly, and fail closed after a simulated window crash.\",\n"
                + "  \"windowedExitCode\": " + windowedBoot.exitCode().code() + ",\n"
                + "  \"windowedLifecycle\": \"" + windowedBoot.finalLifecycle().id() + "\",\n"
                + "  \"windowedAdapterCoreRuntimeBridgeActive\": "
                + windowedBoot.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "  \"windowedAshfallFirstPlayableLoopReady\": "
                + windowedBoot.ashfallFirstPlayableLoopReady() + ",\n"
                + "  \"deterministicLiveExitCode\": " + deterministicLiveBoot.exitCode().code() + ",\n"
                + "  \"deterministicLiveLifecycle\": \"" + deterministicLiveBoot.finalLifecycle().id() + "\",\n"
                + "  \"deterministicLiveAdapterCoreRuntimeBridgeActive\": "
                + deterministicLiveBoot.systemModuleBoot().adapterCoreRuntimeBridgeActive() + ",\n"
                + "  \"deterministicLiveAshfallFirstPlayableLoopReady\": "
                + deterministicLiveBoot.ashfallFirstPlayableLoopReady() + ",\n"
                + "  \"windowedWalkthroughReady\": " + windowedBoot.liveWindowWalkthroughReady() + ",\n"
                + "  \"windowedWalkthroughSummary\": \"" + escape(windowedBoot.liveWindowWalkthroughSummary()) + "\",\n"
                + "  \"deterministicLiveWalkthroughReady\": " + deterministicLiveBoot.liveWindowWalkthroughReady() + ",\n"
                + "  \"deterministicLiveWalkthroughSummary\": \"" + escape(deterministicLiveBoot.liveWindowWalkthroughSummary()) + "\",\n"
                + "  \"normalWindowEvents\": " + normal.events().size() + ",\n"
                + "  \"normalWindowClosedSafely\": " + normal.closedSafely() + ",\n"
                + "  \"crashWindowEvents\": " + crashedWindow.events().size() + ",\n"
                + "  \"crashWindowClosedSafely\": " + crashedWindow.closedSafely() + ",\n"
                + "  \"crashBootExitCode\": " + crashedBoot.exitCode().code() + ",\n"
                + "  \"crashBootHandled\": " + crashedBoot.crashHandled() + ",\n"
                + "  \"implementation\": {\n"
                + "    \"application\": \"dev.echo.standalone.runtime.app.EchoWindowedRuntimeApplication\",\n"
                + "    \"windowedRuntime\": \"dev.echo.standalone.runtime.app.EchoWindowedRuntime\",\n"
                + "    \"liveRuntime\": \"dev.echo.standalone.runtime.app.EchoLiveWindowRuntime\",\n"
                + "    \"bootContext\": \"dev.echo.standalone.runtime.app.testkit deterministic live context\",\n"
                + "    \"controller\": \"dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleController\"\n"
                + "  }\n"
                + "}\n");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Path standaloneRoot(Path workspaceRoot) {
        if (workspaceRoot.getFileName() != null
                && workspaceRoot.getFileName().toString().equals("echo-standalone-runtime")) {
            return workspaceRoot;
        }
        Path nested = workspaceRoot.resolve("echo-standalone-runtime");
        if (Files.isDirectory(nested)) {
            return nested;
        }
        return workspaceRoot;
    }
}
