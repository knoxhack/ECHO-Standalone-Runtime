package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeClock;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record EchoFixedStepRuntimeClock(Instant baseInstant, Duration tickDuration) implements EchoRuntimeClock {
    public EchoFixedStepRuntimeClock {
        Objects.requireNonNull(baseInstant, "baseInstant");
        Objects.requireNonNull(tickDuration, "tickDuration");
        if (tickDuration.isNegative() || tickDuration.isZero()) {
            throw new IllegalArgumentException("tickDuration must be positive");
        }
    }

    @Override
    public Instant now() {
        return baseInstant;
    }

    @Override
    public long tickNanos() {
        return tickDuration.toNanos();
    }

    public Instant instantForTick(long tickIndex) {
        if (tickIndex < 0) {
            throw new IllegalArgumentException("tickIndex must not be negative");
        }
        return baseInstant.plusNanos(Math.multiplyExact(tickDuration.toNanos(), tickIndex));
    }
}
