package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeTickLayer;
import dev.echo.standalone.runtime.contracts.EchoRuntimeTickLoop;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoHeadlessRuntimeTickLoop implements EchoRuntimeTickLoop {
    private static final List<EchoRuntimeTickLayer> DEFAULT_LAYERS = Arrays.asList(EchoRuntimeTickLayer.values());

    private final EchoFixedStepRuntimeClock clock;
    private final int maxTicks;
    private final Duration budget;
    private int ticksRun;
    private boolean running;
    private boolean paused;
    private boolean stopRequested;

    public EchoHeadlessRuntimeTickLoop(EchoFixedStepRuntimeClock clock, int maxTicks, Map<String, String> properties) {
        this.clock = Objects.requireNonNull(clock, "clock");
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must not be negative");
        }
        this.maxTicks = maxTicks;
        this.budget = Duration.ofNanos(clock.tickNanos());
    }

    @Override
    public EchoRuntimeLifecycle lifecycle() {
        if (running) {
            return paused ? EchoRuntimeLifecycle.PAUSED : EchoRuntimeLifecycle.RUNNING;
        }
        return stopRequested ? EchoRuntimeLifecycle.STOPPED : EchoRuntimeLifecycle.CREATED;
    }

    @Override
    public boolean running() {
        return running;
    }

    @Override
    public void start(EchoRuntimeTickHandler handler) {
        Objects.requireNonNull(handler, "handler");
        running = true;
        stopRequested = false;
        try {
            while (!stopRequested && !paused && ticksRun < maxTicks) {
                tickOnce(handler);
            }
        } finally {
            running = false;
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void stop() {
        stopRequested = true;
        running = false;
    }

    @Override
    public void tickOnce(EchoRuntimeTickHandler handler) {
        Objects.requireNonNull(handler, "handler");
        EchoRuntimeTickContext context = new EchoRuntimeTickContext(
                ticksRun,
                clock.instantForTick(ticksRun),
                budget,
                DEFAULT_LAYERS
        );
        handler.tick(context);
        ticksRun += 1;
    }

    public int ticksRun() {
        return ticksRun;
    }

    public List<EchoRuntimeTickLayer> layers() {
        return DEFAULT_LAYERS;
    }
}
