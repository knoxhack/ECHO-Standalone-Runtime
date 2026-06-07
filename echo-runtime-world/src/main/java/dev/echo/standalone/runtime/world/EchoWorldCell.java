package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Objects;

public record EchoWorldCell(
        EchoWorldPosition position,
        String terrain,
        String regionId,
        List<String> hazardIds,
        boolean blocked
) {
    public EchoWorldCell {
        Objects.requireNonNull(position, "position");
        terrain = EchoWorldText.requireText(terrain, "terrain");
        regionId = EchoWorldText.requireText(regionId, "regionId");
        Objects.requireNonNull(hazardIds, "hazardIds");
        hazardIds = hazardIds.stream().sorted().toList();
    }
}
