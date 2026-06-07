package dev.echo.standalone.runtime.entity;

import java.util.List;
import java.util.Objects;

public record EchoEntityAiTickResult(List<EchoEntityAiIntent> intents, int movements, int attacks) {
    public EchoEntityAiTickResult {
        Objects.requireNonNull(intents, "intents");
        intents = List.copyOf(intents);
        if (movements < 0) {
            throw new IllegalArgumentException("movements must not be negative");
        }
        if (attacks < 0) {
            throw new IllegalArgumentException("attacks must not be negative");
        }
    }
}
