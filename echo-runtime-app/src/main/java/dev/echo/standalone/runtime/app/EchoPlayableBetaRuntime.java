package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class EchoPlayableBetaRuntime implements EchoRuntime {
    private final EchoRuntimeContext context;
    private final EchoRuntimeLifecycleManager lifecycleManager;
    private final EchoRuntimeShutdownController shutdownController;

    public EchoPlayableBetaRuntime(
            EchoRuntimeContext context,
            EchoRuntimeLifecycleManager lifecycleManager,
            EchoRuntimeShutdownController shutdownController
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.lifecycleManager = Objects.requireNonNull(lifecycleManager, "lifecycleManager");
        this.shutdownController = Objects.requireNonNull(shutdownController, "shutdownController");
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
        lifecycleManager.transition(EchoRuntimeLifecycle.STARTING_GAME_LOOP);
        lifecycleManager.transition(EchoRuntimeLifecycle.RUNNING);
        try {
            Path saveRoot = context.environment().workspaceRoot()
                    .resolve("saves/ashfall-playable-beta-app");
            EchoStandalonePlayableLoopResult result = new EchoStandalonePlayableLoopRuntime().run(
                    context.services(),
                    context.environment().workspaceRoot(),
                    saveRoot
            );
            if (!result.ready()) {
                throw new IllegalStateException("Playable beta loop failed: " + result.summary());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to run playable beta loop", exception);
        }
        requestStop(EchoRuntimeShutdownHook.noop("playable_beta_loop_complete"));
    }

    @Override
    public void requestStop(EchoRuntimeShutdownHook shutdownHook) {
        shutdownController.requestStop(shutdownHook);
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPING);
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPED);
    }
}
