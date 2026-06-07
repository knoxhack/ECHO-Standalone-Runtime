package dev.echo.standalone.runtime.entity;

import java.util.Objects;

public record EchoEntityAiComponent(String profile, EchoEntityAiState state) {
    public EchoEntityAiComponent {
        profile = EchoEntityText.requireText(profile, "profile");
        Objects.requireNonNull(state, "state");
    }

    public EchoEntityAiComponent withState(EchoEntityAiState nextState) {
        return new EchoEntityAiComponent(profile, nextState);
    }
}
