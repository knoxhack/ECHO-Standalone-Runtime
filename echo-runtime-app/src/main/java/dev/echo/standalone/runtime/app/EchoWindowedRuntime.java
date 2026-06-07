package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;
import dev.echo.standalone.runtime.render.EchoRenderBackend;
import dev.echo.standalone.runtime.render.EchoRenderViewport;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleController;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleResult;
import dev.echo.standalone.runtime.render.EchoRenderWindowSettings;
import dev.echo.standalone.runtime.render.EchoRenderWindowState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public final class EchoWindowedRuntime implements EchoRuntime {
    private final EchoRuntimeContext context;
    private final EchoRuntimeLifecycleManager lifecycleManager;
    private final EchoRuntimeShutdownController shutdownController;
    private final EchoRenderBackend backend;
    private final EchoRenderWindowLifecycleController windowController;

    public EchoWindowedRuntime(
            EchoRuntimeContext context,
            EchoRuntimeLifecycleManager lifecycleManager,
            EchoRuntimeShutdownController shutdownController,
            EchoRenderBackend backend,
            EchoRenderWindowLifecycleController windowController
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.lifecycleManager = Objects.requireNonNull(lifecycleManager, "lifecycleManager");
        this.shutdownController = Objects.requireNonNull(shutdownController, "shutdownController");
        this.backend = Objects.requireNonNull(backend, "backend");
        this.windowController = Objects.requireNonNull(windowController, "windowController");
    }

    @Override
    public EchoRuntimeContext context() {
        return context;
    }

    @Override
    public EchoRuntimeLifecycle lifecycle() {
        return lifecycleManager.current();
    }

    @Override
    public void start() {
        lifecycleManager.transition(EchoRuntimeLifecycle.STARTING_RENDERER);
        boolean injectCrash = Boolean.parseBoolean(context.configuration().properties()
                .getOrDefault("echo.window.injectCrash", "false"));
        EchoRenderWindowLifecycleResult lifecycle = windowController.runCrashSafe(
                "windowed-runtime-start",
                () -> {
                    EchoRenderWindowState created = windowController.create(EchoRenderWindowSettings.windowedGame());
                    context.services().register(EchoRenderWindowState.class, created);
                    context.services().register(EchoRenderWindowLifecycleResult.class, new EchoRenderWindowLifecycleResult(
                            created,
                            backend.windowEvents(),
                            false,
                            false,
                            "created"
                    ));
                    EchoRenderWindowState resized = windowController.resize(new EchoRenderViewport(1600, 900));
                    context.services().register(EchoRenderWindowState.class, resized);
                    EchoRenderWindowState fullscreen = windowController.fullscreen();
                    context.services().register(EchoRenderWindowState.class, fullscreen);
                    EchoRenderWindowState windowed = windowController.windowed(new EchoRenderViewport(1280, 720));
                    context.services().register(EchoRenderWindowState.class, windowed);
                    lifecycleManager.transition(EchoRuntimeLifecycle.RUNNING);
                    runWindowedPlayableLoopIfEnabled();
                    if (injectCrash) {
                        throw new IllegalStateException("simulated window lifecycle crash");
                    }
                    windowController.close("window_close_requested");
                }
        );
        context.services().register(EchoRenderWindowLifecycleResult.class, lifecycle);
        context.services().register(EchoRenderWindowState.class, lifecycle.finalState());
        EchoWindowedRuntimeResult result = new EchoWindowedRuntimeResult(context.environment().runtimeId(), lifecycle);
        context.services().register(EchoWindowedRuntimeResult.class, result);

        if (lifecycle.crashHandled()) {
            context.diagnostics().emit(new EchoRuntimeDiagnostic(
                    "ECHO-STANDALONE-WINDOW-CRASH-SAFE-SHUTDOWN",
                    EchoRuntimeDiagnosticSeverity.FATAL,
                    "window_runtime",
                    "Window lifecycle closed safely after a runtime crash.",
                    lifecycle.shutdownReason(),
                    Map.of("runtimeId", context.environment().runtimeId())
            ));
            throw new IllegalStateException("Window lifecycle crash handled safely: " + lifecycle.shutdownReason());
        }

        requestStop(EchoRuntimeShutdownHook.noop("window_close_requested"));
    }

    private void runWindowedPlayableLoopIfEnabled() {
        boolean playableLoop = Boolean.parseBoolean(context.configuration().properties()
                .getOrDefault("echo.window.playableLoop", "false"));
        if (!playableLoop) {
            return;
        }
        try {
            Path baseSaveRoot = context.environment().workspaceRoot()
                    .resolve("saves/ashfall-windowed-playable-loop")
                    .resolve(context.environment().runtimeId());
            Files.createDirectories(baseSaveRoot);
            Path saveRoot = Files.createTempDirectory(baseSaveRoot, "run-");
            EchoStandalonePlayableLoopResult result = new EchoStandalonePlayableLoopRuntime().run(
                    context.services(),
                    context.environment().workspaceRoot(),
                    saveRoot
            );
            if (!result.ready()) {
                throw new IllegalStateException("Windowed playable loop failed: " + result.summary());
            }
            EchoStandaloneSystemModuleBootResult systemModuleBoot = context.services()
                    .find(EchoStandaloneSystemModuleBootResult.class)
                    .orElse(EchoStandaloneSystemModuleBootResult.inactive());
            context.services().register(EchoStandaloneLiveWindowWalkthroughResult.class,
                    EchoStandaloneLiveWindowWalkthroughResult.from(
                            "windowed-runtime",
                            false,
                            false,
                            false,
                            systemModuleBoot.adapterCoreRuntimeBridgeActive(),
                            result
                    ));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to run windowed playable loop", exception);
        }
    }

    @Override
    public void requestStop(EchoRuntimeShutdownHook shutdownHook) {
        shutdownController.requestStop(shutdownHook);
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPING);
        backend.close();
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPED);
    }
}
