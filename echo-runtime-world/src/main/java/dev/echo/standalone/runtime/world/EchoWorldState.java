package dev.echo.standalone.runtime.world;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EchoWorldState(
        String worldId,
        long seed,
        long tick,
        List<EchoWorldDimension> dimensions,
        List<EchoWorldRegion> regions,
        List<EchoWorldChunk> chunks
) {
    public EchoWorldState {
        worldId = EchoWorldText.requireText(worldId, "worldId");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
        Objects.requireNonNull(dimensions, "dimensions");
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(chunks, "chunks");
        dimensions = dimensions.stream()
                .sorted(Comparator.comparing(EchoWorldDimension::id))
                .toList();
        regions = regions.stream()
                .sorted(Comparator.comparing(EchoWorldRegion::id))
                .toList();
        chunks = chunks.stream()
                .sorted(Comparator.comparing(chunk -> chunk.id().key()))
                .toList();
    }

    public Optional<EchoWorldChunk> chunk(EchoWorldChunkId id) {
        Objects.requireNonNull(id, "id");
        return chunks.stream()
                .filter(chunk -> chunk.id().equals(id))
                .findFirst();
    }

    public Optional<EchoWorldDimension> dimension(String id) {
        String normalized = EchoWorldText.requireText(id, "id");
        return dimensions.stream()
                .filter(dimension -> dimension.id().equals(normalized))
                .findFirst();
    }

    public int cellCount() {
        return chunks.stream().mapToInt(chunk -> chunk.cells().size()).sum();
    }

    public int dimensionCount() {
        return dimensions.size();
    }

    public int hazardCount() {
        return chunks.stream().mapToInt(chunk -> chunk.hazards().size()).sum();
    }

    public int poiCount() {
        return chunks.stream().mapToInt(chunk -> chunk.pointsOfInterest().size()).sum();
    }
}
