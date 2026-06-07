package dev.echo.standalone.runtime.gameplay;

import java.util.Objects;

public record EchoGameplayRuntimeResult(
        EchoGameplayMissionState mission,
        EchoSurvivalState survival,
        EchoProgressionState progression,
        EchoFactionRuntime factions,
        EchoNotificationLog notifications,
        EchoHazardGameplaySystem hazardSystem,
        EchoWeatherGameplaySystem weatherSystem,
        EchoInteractionSystem interactionSystem,
        EchoGameplaySaveHook saveHook
) {
    public EchoGameplayRuntimeResult {
        Objects.requireNonNull(mission, "mission");
        Objects.requireNonNull(survival, "survival");
        Objects.requireNonNull(progression, "progression");
        Objects.requireNonNull(factions, "factions");
        Objects.requireNonNull(notifications, "notifications");
        Objects.requireNonNull(hazardSystem, "hazardSystem");
        Objects.requireNonNull(weatherSystem, "weatherSystem");
        Objects.requireNonNull(interactionSystem, "interactionSystem");
        Objects.requireNonNull(saveHook, "saveHook");
    }
}
