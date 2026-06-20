package dev.echo.standalone.runtime.world;

import java.util.Objects;
import java.util.List;
import java.util.Optional;

public final class EchoWorldQuery {
    private final EchoWorldState world;

    public EchoWorldQuery(EchoWorldState world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    public Optional<EchoWorldCell> cellAt(EchoWorldPosition position) {
        Objects.requireNonNull(position, "position");
        return world.chunks().stream()
                .flatMap(chunk -> chunk.cells().stream())
                .filter(cell -> cell.position().equals(position))
                .findFirst();
    }

    public Optional<EchoWorldPoi> poi(String id) {
        String normalized = EchoWorldText.requireText(id, "id");
        List<String> candidateIds = poiQueryIds(normalized);
        return world.chunks().stream()
                .flatMap(chunk -> chunk.pointsOfInterest().stream())
                .filter(poi -> candidateIds.contains(poi.id()))
                .findFirst();
    }

    public Optional<EchoWorldDimension> dimension(String id) {
        return world.dimension(id);
    }

    public double hazardIntensityAt(EchoWorldPosition position) {
        Objects.requireNonNull(position, "position");
        return world.chunks().stream()
                .flatMap(chunk -> chunk.hazards().stream())
                .filter(hazard -> withinRadius(position, hazard))
                .mapToDouble(EchoWorldHazard::intensity)
                .max()
                .orElse(0.0D);
    }

    private static boolean withinRadius(EchoWorldPosition position, EchoWorldHazard hazard) {
        int dx = position.x() - hazard.origin().x();
        int dz = position.z() - hazard.origin().z();
        return dx * dx + dz * dz <= hazard.radiusCells() * hazard.radiusCells();
    }

    private static List<String> poiQueryIds(String id) {
        return switch (id) {
            case "echoashfallprotocol:poi/drop_pod", "ashfall:terminal_pod" ->
                    List.of("echoashfallprotocol:poi/drop_pod", "ashfall:terminal_pod");
            default -> List.of(id);
        };
    }
}
