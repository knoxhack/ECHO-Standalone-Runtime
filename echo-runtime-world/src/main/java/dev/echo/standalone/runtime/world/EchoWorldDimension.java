package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;

public record EchoWorldDimension(
        String id,
        String displayName,
        String environment,
        double gravity,
        List<String> regionIds
) {
    public EchoWorldDimension {
        id = EchoWorldText.requireText(id, "id");
        displayName = EchoWorldText.requireText(displayName, "displayName");
        environment = EchoWorldText.requireText(environment, "environment");
        if (gravity <= 0.0D) {
            throw new IllegalArgumentException("gravity must be positive");
        }
        Objects.requireNonNull(regionIds, "regionIds");
        regionIds = regionIds.stream()
                .map(regionId -> EchoWorldText.requireText(regionId, "regionId"))
                .sorted()
                .toList();
    }
}
