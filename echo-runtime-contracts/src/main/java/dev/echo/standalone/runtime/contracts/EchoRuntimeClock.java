package dev.echo.standalone.runtime.contracts;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public interface EchoRuntimeClock {
    Instant now();

    long tickNanos();

    default Duration tickDuration() {
        return Duration.ofNanos(tickNanos());
    }

    static EchoRuntimeClock fixed(Instant instant, Duration tickDuration) {
        Objects.requireNonNull(instant, "instant");
        Objects.requireNonNull(tickDuration, "tickDuration");
        return new EchoRuntimeClock() {
            @Override
            public Instant now() {
                return instant;
            }

            @Override
            public long tickNanos() {
                return tickDuration.toNanos();
            }
        };
    }
}
