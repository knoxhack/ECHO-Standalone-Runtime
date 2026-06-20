package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldPosition;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldSaveResult;
import dev.echo.standalone.runtime.world.EchoWorldState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeWorldSmokeHarness {
    private EchoRuntimeWorldSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoWorldState state = world.world();

        require(services.require(EchoWorldRuntimeResult.class) == world, "world runtime result should be service-bound");
        require(services.require(EchoWorldState.class) == state, "world state should be service-bound");
        require(state.dimensionCount() == 1, "debug world should contain one dimension");
        require(state.dimension("ashfall:surface").orElseThrow().regionIds().contains("ashfall:crash_site"),
                "Ashfall surface dimension should own the crash site region");
        require(state.regions().size() == 1, "debug world should contain one region");
        require(state.chunks().size() == 1, "debug world should contain one chunk");
        require(state.cellCount() == 16, "debug world should contain a 4x4 cell grid");
        require(state.hazardCount() == 1, "debug world should contain one hazard");
        require(state.poiCount() == 2, "debug world should contain two POIs");
        require(world.query().cellAt(new EchoWorldPosition(0, 0, 0)).orElseThrow().terrain().equals("crash_debris"),
                "origin cell should be crash debris");
        require(world.query().hazardIntensityAt(new EchoWorldPosition(1, 0, 1)) == 0.72D,
                "hazard center should report toxic ash intensity");
        require(world.query().poi("echoashfallprotocol:poi/drop_pod").orElseThrow().label().equals("Starting Drop Pod"),
                "drop pod POI should be queryable");
        require(world.query().poi("ashfall:terminal_pod").orElseThrow().id().equals("echoashfallprotocol:poi/drop_pod"),
                "legacy terminal pod alias should resolve to the canonical drop pod POI");
        require(world.query().dimension("ashfall:surface").orElseThrow().environment().equals("toxic_wasteland"),
                "Ashfall surface dimension should be queryable");

        Path fixtureRoot = Files.createTempDirectory("echo-runtime-world-smoke");
        EchoSaveProfile saveProfile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-world",
                "Ashfall World",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-world"),
                Map.of("phase", "14.9")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(services, saveProfile);
        EchoWorldSaveResult saved = world.saveHook().save(saves, "slot-world", "tx-world-001");
        require(saved.commit().filesWritten() == 2, "world save hook should write summary and one chunk");
        require(saved.writtenPaths().contains("world/summary.json"), "world summary should be written");
        require(saved.writtenPaths().contains("world/chunks/0_0.json"), "chunk summary should be written");

        EchoSaveManifest manifest = saves.readManifest("slot-world");
        require(manifest.file("world/summary.json").isPresent(), "manifest should track world summary");
        require(manifest.file("world/chunks/0_0.json").isPresent(), "manifest should track chunk summary");
        EchoSaveCorruptionReport saveCheck = saves.check("slot-world");
        require(saveCheck.healthy(), "world save should pass corruption check");
        writeReports(Path.of(".").toAbsolutePath().normalize(), world, saved, manifest, saveCheck);

        System.out.println("phase14.9 world runtime smoke PASS regions="
                + state.regions().size()
                + " dimensions="
                + state.dimensionCount()
                + " chunks="
                + state.chunks().size()
                + " cells="
                + state.cellCount()
                + " hazards="
                + state.hazardCount()
                + " poi="
                + state.poiCount()
                + " savedFiles="
                + saved.writtenPaths().size());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoWorldRuntimeResult world,
            EchoWorldSaveResult saved,
            EchoSaveManifest manifest,
            EchoSaveCorruptionReport saveCheck
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoWorldState state = world.world();
        var dimension = state.dimension("ashfall:surface").orElseThrow();
        var region = state.regions().stream()
                .filter(value -> value.id().equals("ashfall:crash_site"))
                .findFirst()
                .orElseThrow();
        var chunk = state.chunks().getFirst();
        var hazard = chunk.hazards().getFirst();
        var weather = chunk.weather();

        write(root.resolve("runtime-world.json"), """
                {
                  "schema": "echo.standalone.runtime_world.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "worldId": "%s",
                  "seed": %d,
                  "tick": %d,
                  "dimensionCount": %d,
                  "regionCount": %d,
                  "chunkCount": %d,
                  "cellCount": %d,
                  "hazardCount": %d,
                  "poiCount": %d,
                  "worldRuntimeServiceBound": true,
                  "worldStateServiceBound": true,
                  "queryOriginTerrain": "%s",
                  "queryHazardIntensity": %.2f,
                  "queryTerminalLabel": "%s",
                  "legacyTerminalAliasResolved": true,
                  "queryDimensionEnvironment": "%s",
                  "savedFileCount": %d,
                  "saveHealthy": %s
                }
                """.formatted(
                escape(state.worldId()),
                state.seed(),
                state.tick(),
                state.dimensionCount(),
                state.regions().size(),
                state.chunks().size(),
                state.cellCount(),
                state.hazardCount(),
                state.poiCount(),
                escape(world.query().cellAt(new EchoWorldPosition(0, 0, 0)).orElseThrow().terrain()),
                world.query().hazardIntensityAt(new EchoWorldPosition(1, 0, 1)),
                escape(world.query().poi("echoashfallprotocol:poi/drop_pod").orElseThrow().label()),
                escape(world.query().dimension("ashfall:surface").orElseThrow().environment()),
                saved.writtenPaths().size(),
                saveCheck.healthy()
        ));

        write(root.resolve("world-dimensions.json"), """
                {
                  "schema": "echo.standalone.world_dimensions.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "dimensionCount": %d,
                  "dimensions": %s,
                  "ashfallSurfaceEnvironment": "%s",
                  "ashfallSurfaceGravity": %.2f,
                  "ashfallSurfaceOwnsCrashSite": %s
                }
                """.formatted(
                state.dimensionCount(),
                state.dimensions().stream()
                        .map(value -> "{\"id\": \"" + escape(value.id())
                                + "\", \"displayName\": \"" + escape(value.displayName())
                                + "\", \"environment\": \"" + escape(value.environment())
                                + "\", \"gravity\": " + value.gravity()
                                + ", \"regionIds\": " + jsonArray(value.regionIds()) + "}")
                        .collect(java.util.stream.Collectors.joining(", ", "[", "]")),
                escape(dimension.environment()),
                dimension.gravity(),
                dimension.regionIds().contains("ashfall:crash_site")
        ));

        write(root.resolve("world-regions.json"), """
                {
                  "schema": "echo.standalone.world_regions.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "regionCount": %d,
                  "regions": %s,
                  "crashSiteDisplayName": "%s",
                  "crashSiteDangerLevel": %d,
                  "crashSiteHazards": %s,
                  "crashSiteWeatherProfile": "%s"
                }
                """.formatted(
                state.regions().size(),
                state.regions().stream()
                        .map(value -> "{\"id\": \"" + escape(value.id())
                                + "\", \"displayName\": \"" + escape(value.displayName())
                                + "\", \"dangerLevel\": " + value.dangerLevel()
                                + ", \"hazardIds\": " + jsonArray(value.hazardIds())
                                + ", \"weatherProfile\": \"" + escape(value.weatherProfile()) + "\"}")
                        .collect(java.util.stream.Collectors.joining(", ", "[", "]")),
                escape(region.displayName()),
                region.dangerLevel(),
                jsonArray(region.hazardIds()),
                escape(region.weatherProfile())
        ));

        write(root.resolve("world-chunks.json"), """
                {
                  "schema": "echo.standalone.world_chunks.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "chunkCount": %d,
                  "cellCount": %d,
                  "chunks": %s,
                  "originTerrain": "%s",
                  "originBlocked": %s,
                  "blockedCellCount": %d,
                  "crashDebrisCellCount": %d
                }
                """.formatted(
                state.chunks().size(),
                state.cellCount(),
                state.chunks().stream()
                        .map(value -> "{\"id\": \"" + escape(value.id().key())
                                + "\", \"regionId\": \"" + escape(value.regionId())
                                + "\", \"cellCount\": " + value.cells().size()
                                + ", \"hazardCount\": " + value.hazards().size()
                                + ", \"poiCount\": " + value.pointsOfInterest().size() + "}")
                        .collect(java.util.stream.Collectors.joining(", ", "[", "]")),
                escape(world.query().cellAt(new EchoWorldPosition(0, 0, 0)).orElseThrow().terrain()),
                world.query().cellAt(new EchoWorldPosition(0, 0, 0)).orElseThrow().blocked(),
                chunk.cells().stream().filter(dev.echo.standalone.runtime.world.EchoWorldCell::blocked).count(),
                chunk.cells().stream().filter(cell -> cell.terrain().equals("crash_debris")).count()
        ));

        write(root.resolve("world-hazards.json"), """
                {
                  "schema": "echo.standalone.world_hazards.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "hazardCount": %d,
                  "hazards": %s,
                  "toxicAshId": "%s",
                  "toxicAshType": "%s",
                  "toxicAshIntensity": %.2f,
                  "toxicAshRadiusCells": %d,
                  "queryCenterIntensity": %.2f
                }
                """.formatted(
                state.hazardCount(),
                chunk.hazards().stream()
                        .map(value -> "{\"id\": \"" + escape(value.id())
                                + "\", \"type\": \"" + escape(value.type())
                                + "\", \"intensity\": " + value.intensity()
                                + ", \"origin\": \"" + escape(value.origin().key())
                                + "\", \"radiusCells\": " + value.radiusCells() + "}")
                        .collect(java.util.stream.Collectors.joining(", ", "[", "]")),
                escape(hazard.id()),
                escape(hazard.type()),
                hazard.intensity(),
                hazard.radiusCells(),
                world.query().hazardIntensityAt(new EchoWorldPosition(1, 0, 1))
        ));

        write(root.resolve("world-weather.json"), """
                {
                  "schema": "echo.standalone.world_weather.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "weatherProfile": "%s",
                  "temperatureCelsius": %.2f,
                  "windSpeed": %.2f,
                  "ashDensity": %.2f,
                  "visibility": %.2f,
                  "regionWeatherProfile": "%s"
                }
                """.formatted(
                escape(weather.profileId()),
                weather.temperatureCelsius(),
                weather.windSpeed(),
                weather.ashDensity(),
                weather.visibility(),
                escape(region.weatherProfile())
        ));

        write(root.resolve("world-pois.json"), """
                {
                  "schema": "echo.standalone.world_pois.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "poiCount": %d,
                  "pois": %s,
                  "terminalLabel": "%s",
                  "terminalType": "%s",
                  "legacyTerminalAliasResolved": true,
                  "crashCacheQueryable": %s
                }
                """.formatted(
                state.poiCount(),
                chunk.pointsOfInterest().stream()
                        .map(value -> "{\"id\": \"" + escape(value.id())
                                + "\", \"type\": \"" + escape(value.type())
                                + "\", \"label\": \"" + escape(value.label())
                                + "\", \"position\": \"" + escape(value.position().key()) + "\"}")
                        .collect(java.util.stream.Collectors.joining(", ", "[", "]")),
                escape(world.query().poi("echoashfallprotocol:poi/drop_pod").orElseThrow().label()),
                escape(world.query().poi("echoashfallprotocol:poi/drop_pod").orElseThrow().type()),
                world.query().poi("ashfall:crash_cache").isPresent()
        ));

        write(root.resolve("world-save-hooks.json"), """
                {
                  "schema": "echo.standalone.world_save_hooks.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "filesWritten": %d,
                  "writtenPaths": %s,
                  "manifestTracksSummary": %s,
                  "manifestTracksChunk": %s,
                  "saveHealthy": %s,
                  "commitMetadata": %s
                }
                """.formatted(
                saved.commit().filesWritten(),
                jsonArray(saved.writtenPaths()),
                manifest.file("world/summary.json").isPresent(),
                manifest.file("world/chunks/0_0.json").isPresent(),
                saveCheck.healthy(),
                jsonStringMap(manifest.metadata())
        ));
    }

    private static String jsonStringMap(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + escape(entry.getValue()) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
