package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;

public record EchoWorldRegion(
        String id,
        String displayName,
        int dangerLevel,
        List<String> hazardIds,
        String weatherProfile
) {
    public EchoWorldRegion {
        id = EchoWorldText.requireText(id, "id");
        displayName = EchoWorldText.requireText(displayName, "displayName");
        if (dangerLevel < 0) {
            throw new IllegalArgumentException("dangerLevel must not be negative");
        }
        Objects.requireNonNull(hazardIds, "hazardIds");
        weatherProfile = EchoWorldText.requireText(weatherProfile, "weatherProfile");
        hazardIds = hazardIds.stream().sorted().toList();
    }
}
