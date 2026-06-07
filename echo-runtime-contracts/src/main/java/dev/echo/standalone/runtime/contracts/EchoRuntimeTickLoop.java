package dev.echo.standalone.runtime.contracts;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public interface EchoRuntimeTickLoop {
    EchoRuntimeLifecycle lifecycle();

    boolean running();

    void start(EchoRuntimeTickHandler handler);

    void pause();

    void resume();

    void stop();

    void tickOnce(EchoRuntimeTickHandler handler);

    @FunctionalInterface
    interface EchoRuntimeTickHandler {
        void tick(EchoRuntimeTickContext context);
    }

    record EchoRuntimeTickContext(long tickIndex, Instant startedAt, Duration budget, List<EchoRuntimeTickLayer> layers) {
        public EchoRuntimeTickContext {
            Objects.requireNonNull(startedAt, "startedAt");
            Objects.requireNonNull(budget, "budget");
            Objects.requireNonNull(layers, "layers");
            layers = List.copyOf(layers);
            if (tickIndex < 0) {
                throw new IllegalArgumentException("tickIndex must not be negative");
            }
        }

        public EchoRuntimeTickContext(long tickIndex, Instant startedAt, Duration budget) {
            this(tickIndex, startedAt, budget, List.of());
        }
    }
}
