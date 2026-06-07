package dev.echo.standalone.runtime.world;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoWorldChunk(
        EchoWorldChunkId id,
        String regionId,
        List<EchoWorldCell> cells,
        List<EchoWorldHazard> hazards,
        EchoWorldWeatherField weather,
        List<EchoWorldPoi> pointsOfInterest
) {
    public EchoWorldChunk {
        Objects.requireNonNull(id, "id");
        regionId = EchoWorldText.requireText(regionId, "regionId");
        Objects.requireNonNull(cells, "cells");
        Objects.requireNonNull(hazards, "hazards");
        Objects.requireNonNull(weather, "weather");
        Objects.requireNonNull(pointsOfInterest, "pointsOfInterest");
        cells = cells.stream()
                .sorted(Comparator.comparing(cell -> cell.position().key()))
                .toList();
        hazards = hazards.stream()
                .sorted(Comparator.comparing(EchoWorldHazard::id))
                .toList();
        pointsOfInterest = pointsOfInterest.stream()
                .sorted(Comparator.comparing(EchoWorldPoi::id))
                .toList();
    }
}
