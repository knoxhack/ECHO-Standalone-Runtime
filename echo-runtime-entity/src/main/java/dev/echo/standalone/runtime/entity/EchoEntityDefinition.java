package dev.echo.standalone.runtime.entity;

import java.util.Objects;

public record EchoEntityDefinition(
        String definitionId,
        String displayName,
        EchoEntityKind kind,
        int maxHealth,
        int movementSpeed,
        String aiProfile
) {
    public EchoEntityDefinition {
        definitionId = EchoEntityText.requireText(definitionId, "definitionId");
        displayName = EchoEntityText.requireText(displayName, "displayName");
        Objects.requireNonNull(kind, "kind");
        if (maxHealth <= 0) {
            throw new IllegalArgumentException("maxHealth must be positive");
        }
        if (movementSpeed < 0) {
            throw new IllegalArgumentException("movementSpeed must not be negative");
        }
        aiProfile = EchoEntityText.requireText(aiProfile, "aiProfile");
    }
}
