package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoWorldDebugGenerator {
    public EchoWorldState generate(EchoWorldGenerationSettings settings) {
        Objects.requireNonNull(settings, "settings");
        EchoWorldDebugProfile profile = settings.profile();
        EchoWorldRegion region = new EchoWorldRegion(
                settings.regionId(),
                profile.regionDisplayName(),
                profile.dangerLevel(),
                profile.regionHazardIds(),
                profile.weather().profileId()
        );
        EchoWorldDimension dimension = new EchoWorldDimension(
                settings.dimensionId(),
                profile.dimensionDisplayName(),
                profile.environment(),
                profile.gravity(),
                List.of(region.id())
        );
        EchoWorldHazard hazard = profile.hazard();
        EchoWorldWeatherField weather = profile.weather();
        ArrayList<EchoWorldCell> cells = new ArrayList<>();
        for (int z = 0; z < settings.chunkSize(); z++) {
            for (int x = 0; x < settings.chunkSize(); x++) {
                boolean insideHazard = distanceSquared(x, z, hazard.origin().x(), hazard.origin().z())
                        <= hazard.radiusCells() * hazard.radiusCells();
                cells.add(new EchoWorldCell(
                        new EchoWorldPosition(x, 0, z),
                        terrainFor(profile, x, z),
                        region.id(),
                        insideHazard ? List.of(hazard.id()) : List.of(),
                        x == 3 && z == 3
                ));
            }
        }
        EchoWorldChunk chunk = new EchoWorldChunk(
                new EchoWorldChunkId(0, 0),
                region.id(),
                cells,
                List.of(hazard),
                weather,
                profile.pois()
        );
        return new EchoWorldState(
                settings.worldId(),
                settings.seed(),
                0L,
                List.of(dimension),
                List.of(region),
                List.of(chunk)
        );
    }

    private static int distanceSquared(int ax, int az, int bx, int bz) {
        int dx = ax - bx;
        int dz = az - bz;
        return dx * dx + dz * dz;
    }

    private static String terrainFor(EchoWorldDebugProfile profile, int x, int z) {
        if (x == 0 && z == 0) {
            return profile.originTerrain();
        }
        if ((x + z) % 3 == 0) {
            return profile.primaryTerrain();
        }
        return profile.secondaryTerrain();
    }
}
