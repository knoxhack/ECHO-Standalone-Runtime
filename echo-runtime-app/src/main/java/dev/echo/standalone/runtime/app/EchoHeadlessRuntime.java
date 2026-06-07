package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;

import java.util.Objects;

public final class EchoHeadlessRuntime implements EchoRuntime {
    private final EchoRuntimeContext context;
    private final EchoRuntimeLifecycleManager lifecycleManager;
    private final EchoHeadlessRuntimeTickLoop tickLoop;
    private final EchoRuntimeShutdownController shutdownController;

    public EchoHeadlessRuntime(
            EchoRuntimeContext context,
            EchoRuntimeLifecycleManager lifecycleManager,
            EchoHeadlessRuntimeTickLoop tickLoop,
            EchoRuntimeShutdownController shutdownController
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.lifecycleManager = Objects.requireNonNull(lifecycleManager, "lifecycleManager");
        this.tickLoop = Objects.requireNonNull(tickLoop, "tickLoop");
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
        tickLoop.start(tick -> {
        });
        if (!shutdownController.stopRequested()) {
            requestStop(EchoRuntimeShutdownHook.noop("headless_tick_loop_complete"));
        }
    }

    @Override
    public void requestStop(EchoRuntimeShutdownHook shutdownHook) {
        shutdownController.requestStop(shutdownHook);
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPING);
        tickLoop.stop();
        lifecycleManager.transition(EchoRuntimeLifecycle.STOPPED);
    }
}
