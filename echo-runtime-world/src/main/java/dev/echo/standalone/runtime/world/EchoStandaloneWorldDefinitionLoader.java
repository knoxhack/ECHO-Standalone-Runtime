package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneAtmosphere;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneBiomeProfile;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneDifficulty;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneHazard;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneHazardTickDamageRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneRegion;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneRegionTransitionRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneSpawnRule;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneSpawnRuleEventRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffect;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectLoadRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStatusEffectSaveRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStructurePoiLookupRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneStructurePlacement;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeather;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleProfile;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherScheduleRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWeatherStateApplyRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldCellSampleRequest;
import dev.echo.standalone.runtime.world.EchoStandaloneWorldEffectsRuntime.EchoStandaloneWorldEffectTick;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoStandaloneWorldDefinitionLoader {
    private static final EchoStandaloneWorldDefinitionProfile ASHFALL_CRASH_ZONE =
            new EchoStandaloneWorldDefinitionProfile(
                    "ashfall-crash-zone",
                    "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions/ashfall_crash_zone_wasteland.json",
                    "",
                    "addons/echoweathercore/src/main/resources/data/echoweathercore/weather_profiles/ash_storm.json",
                    "echoashfallprotocol:crash_zone_wasteland",
                    "",
                    "World region must declare at least one hazard id.",
                    "echoashfallprotocol:crash_zone_wasteland",
                    "echoashfallprotocol:common_wasteland_biomes",
                    "echoashfallprotocol:secure_crash_outpost",
                    "echoashfallprotocol:mission/",
                    "echoweathercore:ash_storm",
                    "Ash Storm",
                    "Visibility loss expected.",
                    "ASH_STORM",
                    "MODERATE",
                    "REGIONAL",
                    "minecraft:ash",
                    0.45D,
                    "echoashfallprotocol:event.",
                    "echorendercore:hazard/",
                    "agent7-player",
                    "agent7-weather",
                    32,
                    68,
                    32,
                    30,
                    30,
                    "crash_zone",
                    "agent7-data-backed-parity",
                    "echoashfallprotocol:ashfall_crash_zone_definition_world",
                    "ashfall:surface",
                    "Ashfall Surface",
                    "toxic_wasteland",
                    "echoashfallprotocol:poi/",
                    "echoashfallprotocol:drop_pod",
                    "echoashfallprotocol:rad_zombie",
                    7L,
                    6001L,
                    6002L,
                    6000L,
                    6007L,
                    8401L,
                    20401L,
                    6006L,
                    6008L,
                    6004L,
                    6005L,
                    6009L,
                    6003L
            );
    private static final EchoStandaloneWorldDefinitionProfile ASHFALL_TOXIC_SWAMP =
            new EchoStandaloneWorldDefinitionProfile(
                    "ashfall-toxic-swamp",
                    "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoworldcore/world_regions/ashfall_toxic_swamp.json",
                    "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/missioncore/missions/first_relay_station_route.json",
                    "addons/echoashfallprotocol/src/main/resources/data/echoashfallprotocol/echoweathercore/weather_profiles/ashfall_toxic_front.json",
                    "echoashfallprotocol:toxic_swamp",
                    "echoashfallprotocol:hazard/toxic_ash",
                    "World region must declare toxic ash hazard.",
                    "echoashfallprotocol:toxic_swamp",
                    "echoashfallprotocol:toxic_air_biomes",
                    "echoashfallprotocol:first_relay_station_route",
                    "echoashfallprotocol:mission/",
                    "echoashfallprotocol:ashfall_toxic_front",
                    "Ashfall Toxic Front",
                    "Toxic front approaching.",
                    "TOXIC_RAIN",
                    "MODERATE",
                    "ROUTE_BASED",
                    "minecraft:spore_blossom_air",
                    0.38D,
                    "echoashfallprotocol:event.",
                    "echorendercore:hazard/",
                    "agent7-toxic-player",
                    "agent7-toxic-weather",
                    48,
                    68,
                    48,
                    46,
                    46,
                    "toxic_swamp",
                    "agent7-second-slice-data-backed-parity",
                    "echoashfallprotocol:ashfall_toxic_swamp_definition_world",
                    "ashfall:surface",
                    "Ashfall Surface",
                    "toxic_wasteland",
                    "echoashfallprotocol:poi/",
                    "echoashfallprotocol:drop_pod",
                    "echoashfallprotocol:rad_zombie",
                    7L,
                    9001L,
                    9002L,
                    9000L,
                    9007L,
                    11401L,
                    23401L,
                    9006L,
                    9008L,
                    9004L,
                    9005L,
                    9009L,
                    9003L
            );

    public static EchoStandaloneWorldDefinitionProfile ashfallCrashZoneProfile() {
        return ASHFALL_CRASH_ZONE;
    }

    public static EchoStandaloneWorldDefinitionProfile ashfallToxicSwampProfile() {
        return ASHFALL_TOXIC_SWAMP;
    }

    public EchoStandaloneWorldDefinitionSnapshot loadDefinition(
            Path repoRoot,
            EchoStandaloneWorldDefinitionProfile profile
    ) throws IOException {
        EchoStandaloneWorldDefinitionProfile safeProfile = Objects.requireNonNull(profile, "profile");
        Path root = Objects.requireNonNull(repoRoot, "repoRoot");
        Path regionPath = root.resolve(safeProfile.regionPath());
        Map<String, Object> region = readObject(regionPath);
        String regionId = string(region, "id", safeProfile.defaultRegionId());
        List<String> hazardIds = stringList(region, "hazardIds");
        String hazardId;
        if (safeProfile.requiresSpecificHazard()) {
            hazardId = hazardIds.stream()
                    .filter(id -> id.equals(safeProfile.requiredHazardId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(safeProfile.missingHazardMessage()));
        } else {
            if (hazardIds.isEmpty()) {
                throw new IllegalArgumentException(safeProfile.missingHazardMessage());
            }
            hazardId = hazardIds.get(0);
        }
        Path hazardPath = hazardPath(root, hazardId);
        Map<String, Object> hazard = readObject(hazardPath);
        Path missionPath = safeProfile.missionPath().isBlank()
                ? missionPath(root, regionId, hazardId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "No missioncore mission binds region " + regionId + " to hazard " + hazardId))
                : root.resolve(safeProfile.missionPath());
        Map<String, Object> mission = readObject(missionPath);
        Path weatherPath = root.resolve(safeProfile.weatherPath());
        Map<String, Object> weather = readObject(weatherPath);
        String biomeId = firstOr(region, "biomeIds", safeProfile.defaultBiomeId());
        Path biomePath = biomePath(root, biomeId);
        Map<String, Object> biome = readObject(biomePath);
        String structureId = preferredStructure(region, safeProfile);
        Path structurePath = structurePath(root, structureId);
        Map<String, Object> structure = readObject(structurePath);

        int regionRadius = integer(region, "radius", 96);
        int hazardSeverity = integer(hazard, "defaultSeverity", integer(hazard, "severity", 0));
        double damagePerTick = Math.max(0.25D, hazardSeverity / 12.5D);
        String weatherId = string(weather, "id", safeProfile.defaultWeatherId());
        String weatherPathName = idPath(weatherId);
        String weatherAudioCue = string(weather, "soundCoreAmbienceId",
                safeProfile.defaultWeatherAudioCuePrefix() + weatherPathName);
        String weatherRenderProfile = string(weather, "particleVisualProfileId",
                safeProfile.defaultWeatherRenderProfilePrefix() + weatherPathName);
        String missionId = safeProfile.missionRuntimePrefix()
                + idPath(string(mission, "id", safeProfile.defaultMissionId()));
        String difficulty = string(mission, "difficulty", "normal").toLowerCase();
        double hazardMultiplier = hazardMultiplier(difficulty);
        double spawnMultiplier = spawnMultiplier(difficulty);
        SpawnChoice spawn = spawnChoice(biome, safeProfile);
        double visibility = doubleAt(weather, List.of("effects", "visibilityMultiplier"), safeProfile.defaultVisibility());
        String particle = stringAt(biome, List.of("effects", "ambient_particle", "options", "type"),
                safeProfile.defaultParticle());
        String biomeTag = "#" + firstOr(region, "biomeTags", safeProfile.defaultBiomeTag());
        float statusSaveDamage = (float) (damagePerTick * hazardMultiplier);

        EchoStandaloneWorldEffectTick tick = new EchoStandaloneWorldEffectTick(
                safeProfile.playerId(),
                safeProfile.playerX(),
                safeProfile.playerY(),
                safeProfile.playerZ(),
                20.0D,
                "",
                new EchoStandaloneRegion(
                        regionId,
                        string(region, "displayName", idPath(regionId)),
                        0,
                        regionRadius,
                        0,
                        regionRadius,
                        missionId
                ),
                new EchoStandaloneHazard(
                        hazardId,
                        idPath(hazardId),
                        safeProfile.playerX(),
                        safeProfile.playerZ(),
                        Math.max(4, regionRadius / 8),
                        damagePerTick,
                        "echostatuscore:status/" + idPath(hazardId)
                ),
                new EchoStandaloneWeather(
                        weatherId,
                        string(weather, "displayName", safeProfile.defaultWeatherDisplayName()).toUpperCase() + ": "
                                + string(weather, "terminalWarning", safeProfile.defaultWeatherTerminalWarning()),
                        weatherAudioCue,
                        weatherRenderProfile
                ),
                new EchoStandaloneAtmosphere(
                        "echoatmospherecore:" + weatherPathName + "_field",
                        visibility,
                        particle,
                        "fog_color:" + integerAt(biome, List.of("effects", "fog_color"), 0)
                ),
                new EchoStandaloneBiomeProfile(
                        biomeId,
                        biomeTag,
                        hazardId
                ),
                new EchoStandaloneStructurePlacement(
                        structureId,
                        safeProfile.poiPrefix() + idPath(structureId),
                        safeProfile.structureX(),
                        integerAt(structure, List.of("start_height", "absolute"), 68),
                        safeProfile.structureZ()
                ),
                new EchoStandaloneSpawnRule(
                        "echospawncore:spawn/" + idPath(spawn.entityId()) + "_" + safeProfile.spawnRuleSuffix(),
                        spawn.entityId(),
                        regionId,
                        spawn.maxCount(),
                        spawn.weight()
                ),
                new EchoStandaloneStatusEffect(
                        "echostatuscore:status/" + idPath(hazardId),
                        200,
                        Math.max(1, hazardSeverity / 25),
                        "echoworldcore.hazard." + idPath(hazardId) + ".status"
                ),
                new EchoStandaloneDifficulty(
                        "echodifficultycore:" + difficulty,
                        hazardMultiplier,
                        spawnMultiplier
                )
        );

        EchoWorldState worldState = worldState(safeProfile, region, hazardId, hazard, weather, biome, structureId, tick);
        return new EchoStandaloneWorldDefinitionSnapshot(
                worldState,
                tick,
                List.of(
                        slash(root.relativize(regionPath)),
                        slash(root.relativize(hazardPath)),
                        slash(root.relativize(missionPath)),
                        slash(root.relativize(weatherPath)),
                        slash(root.relativize(biomePath)),
                        slash(root.relativize(structurePath))
                ),
                regionId,
                hazardId,
                string(mission, "id", safeProfile.defaultMissionId()),
                weatherId,
                biomeId,
                structureId,
                new EchoStandaloneRegionTransitionRequest(
                        safeProfile.playerId(),
                        "",
                        regionId,
                        missionId,
                        safeProfile.regionEnterTick(),
                        safeProfile.sourceReason()
                ),
                new EchoStandaloneRegionTransitionRequest(
                        safeProfile.playerId(),
                        regionId,
                        "",
                        "",
                        safeProfile.regionExitTick(),
                        safeProfile.sourceReason()
                ),
                new EchoStandaloneWeatherScheduleRequest(
                        safeProfile.weatherScheduleTick(),
                        2400,
                        safeProfile.structureX(),
                        safeProfile.playerY(),
                        safeProfile.structureZ(),
                        "LOCAL".equals(string(weather, "scope", "REGIONAL")) ? 800 : 2400,
                        safeProfile.sourceReason(),
                        new EchoStandaloneWeatherScheduleProfile(
                                weatherId,
                                string(weather, "type", safeProfile.defaultWeatherType()),
                                string(weather, "defaultSeverity", safeProfile.defaultWeatherSeverity()),
                                string(weather, "scope", safeProfile.defaultWeatherScope()),
                                integer(weather, "durationTicks", 12000),
                                integer(weather, "warningTicks", 2400),
                                integer(weather, "weight", 40),
                                booleanValue(weather.get("enabled"), true)
                        )
                ),
                new EchoStandaloneWeatherStateApplyRequest(
                        safeProfile.weatherActorId(),
                        regionId,
                        "FORECAST",
                        safeProfile.weatherForecastTick(),
                        safeProfile.sourceReason(),
                        tick.weather(),
                        tick.atmosphere()
                ),
                new EchoStandaloneWeatherStateApplyRequest(
                        safeProfile.weatherActorId(),
                        regionId,
                        "ACTIVE",
                        safeProfile.weatherActiveTick(),
                        safeProfile.sourceReason(),
                        tick.weather(),
                        tick.atmosphere()
                ),
                new EchoStandaloneWeatherStateApplyRequest(
                        safeProfile.weatherActorId(),
                        regionId,
                        "ENDED",
                        safeProfile.weatherEndedTick(),
                        safeProfile.sourceReason(),
                        new EchoStandaloneWeather(
                                weatherId,
                                string(weather, "displayName", safeProfile.defaultWeatherDisplayName()).toUpperCase()
                                        + ": CLEAR",
                                "echoweathercore:event.clear",
                                "echorendercore:weather/clear"
                        ),
                        new EchoStandaloneAtmosphere(
                                "echoatmospherecore:clear_field",
                                1.0D,
                                "minecraft:empty",
                                "weather_phase:ENDED"
                        )
                ),
                new EchoStandaloneHazardTickDamageRequest(
                        safeProfile.playerId(),
                        20.0D,
                        hazardSeverity,
                        safeProfile.hazardDamageTick(),
                        safeProfile.sourceReason(),
                        tick.hazard(),
                        tick.difficulty()
                ),
                new EchoStandaloneWorldCellSampleRequest(
                        safeProfile.playerId(),
                        worldState.worldId(),
                        safeProfile.playerX(),
                        safeProfile.playerY(),
                        safeProfile.playerZ(),
                        safeProfile.worldCellSampleTick(),
                        safeProfile.sourceReason(),
                        tick.region(),
                        tick.hazard(),
                        tick.biome(),
                        tick.structure()
                ),
                new EchoStandaloneStructurePoiLookupRequest(
                        safeProfile.playerId(),
                        regionId,
                        safeProfile.playerX(),
                        safeProfile.playerY(),
                        safeProfile.playerZ(),
                        128,
                        safeProfile.structureLookupTick(),
                        safeProfile.sourceReason(),
                        tick.structure()
                ),
                new EchoStandaloneStatusEffectSaveRequest(
                        safeProfile.playerId(),
                        hazardId,
                        statusSaveDamage,
                        safeProfile.statusSaveTick(),
                        safeProfile.sourceReason(),
                        tick.statusEffect()
                ),
                new EchoStandaloneStatusEffectLoadRequest(
                        safeProfile.playerId(),
                        hazardId,
                        tick.statusEffect().saveKey(),
                        savedStatusState(hazardId, tick.statusEffect(), statusSaveDamage, safeProfile.statusSaveTick()),
                        safeProfile.statusLoadTick(),
                        safeProfile.sourceReason()
                ),
                new EchoStandaloneSpawnRuleEventRequest(
                        safeProfile.playerId(),
                        regionId,
                        safeProfile.playerX(),
                        safeProfile.playerY(),
                        safeProfile.playerZ(),
                        0,
                        safeProfile.spawnEventTick(),
                        safeProfile.sourceReason(),
                        tick.spawnRule(),
                        tick.difficulty()
                )
        );
    }

    public EchoStandaloneAgent7CatalogSnapshot loadAgent7Catalog(Path repoRoot) throws IOException {
        Path root = Objects.requireNonNull(repoRoot, "repoRoot");
        List<Path> regionFiles = matchingJson(root, "echoworldcore/world_regions");
        List<Path> hazardFiles = matchingJson(root, "echoworldcore/world_hazards");
        List<Path> weatherFiles = matchingJson(root, "weather_profiles");
        List<Path> biomeFiles = matchingJson(root, "worldgen/biome");
        List<Path> structureFiles = matchingJson(root, "worldgen/structure");
        List<Path> missionFiles = matchingJson(root, "missioncore/missions");

        List<String> regionIds = idsFromFiles(root, regionFiles, "echoworldcore/world_regions");
        List<String> hazardIds = idsFromFiles(root, hazardFiles, "echoworldcore/world_hazards");
        List<String> weatherIds = idsFromFiles(root, weatherFiles, "weather_profiles");
        List<String> biomeIds = idsFromNamespaceFiles(root, biomeFiles, "worldgen/biome");
        List<String> structureIds = idsFromNamespaceFiles(root, structureFiles, "worldgen/structure");
        List<String> difficultyIds = missionFiles.stream()
                .map(path -> {
                    try {
                        return "echodifficultycore:" + string(readObject(path), "difficulty", "");
                    } catch (IOException exception) {
                        return "";
                    }
                })
                .filter(id -> !id.isBlank() && !id.endsWith(":"))
                .distinct()
                .sorted()
                .toList();
        int spawnRuleCount = biomeFiles.stream().mapToInt(path -> {
            try {
                return spawnRuleCount(readObject(path));
            } catch (IOException exception) {
                return 0;
            }
        }).sum();
        List<String> statusEffectIds = hazardIds.stream()
                .map(id -> "echostatuscore:status/" + idPath(id))
                .distinct()
                .sorted()
                .toList();
        ArrayList<String> sourceFiles = new ArrayList<>();
        for (Path path : allCatalogFiles(regionFiles, hazardFiles, weatherFiles, biomeFiles, structureFiles, missionFiles)) {
            sourceFiles.add(slash(root.relativize(path)));
        }
        return new EchoStandaloneAgent7CatalogSnapshot(
                regionIds,
                hazardIds,
                weatherIds,
                biomeIds,
                structureIds,
                statusEffectIds,
                difficultyIds,
                spawnRuleCount,
                List.copyOf(sourceFiles));
    }

    private static EchoWorldState worldState(
            EchoStandaloneWorldDefinitionProfile profile,
            Map<String, Object> region,
            String hazardId,
            Map<String, Object> hazard,
            Map<String, Object> weather,
            Map<String, Object> biome,
            String structureId,
            EchoStandaloneWorldEffectTick tick
    ) {
        EchoWorldRegion worldRegion = new EchoWorldRegion(
                tick.region().id(),
                tick.region().displayName(),
                Math.max(1, integer(hazard, "defaultSeverity", 0) / 20),
                stringList(region, "hazardIds"),
                tick.weather().id()
        );
        EchoWorldDimension dimension = new EchoWorldDimension(
                profile.dimensionId(),
                profile.dimensionDisplayName(),
                profile.dimensionTerrainProfile(),
                1.0D,
                List.of(worldRegion.id())
        );
        EchoWorldHazard worldHazard = new EchoWorldHazard(
                hazardId,
                tick.hazard().type(),
                Math.min(1.0D, integer(hazard, "defaultSeverity", 0) / 100.0D),
                new EchoWorldPosition(2, 0, 2),
                3
        );
        EchoWorldWeatherField weatherField = new EchoWorldWeatherField(
                tick.weather().id(),
                doubleValue(weather.get("temperatureCelsius"), 41.5D),
                doubleValue(weather.get("windSpeed"), 18.0D),
                1.0D - tick.atmosphere().visibility(),
                tick.atmosphere().visibility() * 100.0D
        );
        ArrayList<EchoWorldCell> cells = new ArrayList<>();
        for (int z = 0; z < 6; z++) {
            for (int x = 0; x < 6; x++) {
                boolean insideHazard = distanceSquared(x, z, worldHazard.origin().x(), worldHazard.origin().z())
                        <= worldHazard.radiusCells() * worldHazard.radiusCells();
                cells.add(new EchoWorldCell(
                        new EchoWorldPosition(x, 0, z),
                        terrainFor(biome, x, z),
                        tick.region().id(),
                        insideHazard ? List.of(hazardId) : List.of(),
                        x == 3 && z == 3
                ));
            }
        }
        EchoWorldChunk chunk = new EchoWorldChunk(
                new EchoWorldChunkId(0, 0),
                tick.region().id(),
                cells,
                List.of(worldHazard),
                weatherField,
                List.of(new EchoWorldPoi(
                        profile.poiPrefix() + idPath(structureId),
                        "structure",
                        idPath(structureId),
                        new EchoWorldPosition(3, 0, 3)
                ))
        );
        return new EchoWorldState(
                profile.worldId(),
                profile.worldSeed(),
                0L,
                List.of(dimension),
                List.of(worldRegion),
                List.of(chunk)
        );
    }

    private static Optional<Path> missionPath(Path root, String regionId, String hazardId) throws IOException {
        List<Path> missionFiles = matchingJson(root, "missioncore/missions");
        return missionFiles.stream()
                .filter(path -> missionMatches(path, regionId, hazardId))
                .filter(path -> path.getFileName().toString().equals("secure_crash_outpost.json"))
                .findFirst()
                .or(() -> missionFiles.stream()
                        .filter(path -> missionMatches(path, regionId, hazardId))
                        .findFirst());
    }

    private static boolean missionMatches(Path path, String regionId, String hazardId) {
        try {
            Map<String, Object> mission = readObject(path);
            return regionId.equals(stringAt(mission, List.of("metadata", "worldRegion"), ""))
                    && hazardId.equals(stringAt(mission, List.of("metadata", "hazardContext"), ""));
        } catch (RuntimeException | IOException ignored) {
            return false;
        }
    }

    private static Path hazardPath(Path root, String hazardId) {
        Id id = Id.parse(hazardId);
        return namespaceDataPath(root, id.namespace(), "echoworldcore/world_hazards/" + id.path() + ".json");
    }

    private static Path biomePath(Path root, String biomeId) {
        Id id = Id.parse(biomeId);
        return namespaceDataPath(root, id.namespace(), "worldgen/biome/" + id.path() + ".json");
    }

    private static Path structurePath(Path root, String structureId) {
        Id id = Id.parse(structureId);
        return namespaceDataPath(root, id.namespace(), "worldgen/structure/" + id.path() + ".json");
    }

    private static String preferredStructure(
            Map<String, Object> region,
            EchoStandaloneWorldDefinitionProfile profile
    ) {
        List<String> structureIds = stringList(region, "structureIds");
        return structureIds.stream()
                .filter(id -> id.equals(profile.fallbackStructureId()))
                .findFirst()
                .orElse(structureIds.isEmpty() ? profile.fallbackStructureId() : structureIds.get(0));
    }

    private static SpawnChoice spawnChoice(
            Map<String, Object> biome,
            EchoStandaloneWorldDefinitionProfile profile
    ) {
        Object monsters = valueAt(biome, List.of("spawners", "monster")).orElse(null);
        if (monsters instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            Object type = map.get("type");
            return new SpawnChoice(
                    String.valueOf(type == null ? profile.fallbackSpawnEntityId() : type),
                    integer(map, "maxCount", 1),
                    integer(map, "weight", 1)
            );
        }
        return new SpawnChoice(profile.fallbackSpawnEntityId(), 1, 1);
    }

    private static Map<String, Object> savedStatusState(
            String hazardId,
            EchoStandaloneStatusEffect statusEffect,
            float damageApplied,
            long gameTick
    ) {
        Map<String, Object> statusPayload = new LinkedHashMap<>();
        statusPayload.put("effectId", statusEffect.id());
        statusPayload.put("durationTicks", statusEffect.durationTicks());
        statusPayload.put("amplifier", statusEffect.amplifier());
        statusPayload.put("hazardId", hazardId);
        statusPayload.put("damageApplied", damageApplied);
        statusPayload.put("gameTick", gameTick);
        Map<String, Object> savedStatus = new LinkedHashMap<>();
        savedStatus.put(statusEffect.saveKey(), Map.copyOf(statusPayload));
        savedStatus.put("adapterCoreModule", "echoworldcore");
        return Map.copyOf(savedStatus);
    }

    private static List<Path> matchingJson(Path root, String dataSuffix) throws IOException {
        ArrayList<Path> matches = new ArrayList<>();
        for (Path base : dataRoots(root)) {
            if (!Files.isDirectory(base)) {
                continue;
            }
            try (var paths = Files.walk(base)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .filter(path -> slash(base.relativize(path)).contains("/" + dataSuffix + "/")
                                || slash(base.relativize(path)).startsWith(dataSuffix + "/"))
                        .forEach(matches::add);
            }
        }
        return matches.stream().sorted(Comparator.comparing(path -> slash(root.relativize(path)))).toList();
    }

    private static Path namespaceDataPath(Path root, String namespace, String relativePath) {
        Path rootData = root.resolve("src/main/resources/data").resolve(namespace).resolve(relativePath);
        Path addonData = root.resolve("addons").resolve(namespace)
                .resolve("src/main/resources/data").resolve(namespace).resolve(relativePath);
        return Files.exists(addonData) ? addonData : rootData;
    }

    private static List<Path> dataRoots(Path root) throws IOException {
        ArrayList<Path> roots = new ArrayList<>();
        Path rootData = root.resolve("src/main/resources/data");
        if (Files.isDirectory(rootData)) {
            roots.add(rootData);
        }
        Path addons = root.resolve("addons");
        if (Files.isDirectory(addons)) {
            try (var addonPaths = Files.list(addons)) {
                addonPaths.map(path -> path.resolve("src/main/resources/data"))
                        .filter(Files::isDirectory)
                        .sorted(Comparator.comparing(Path::toString))
                        .forEach(roots::add);
            }
        }
        return List.copyOf(roots);
    }

    private static List<Path> allCatalogFiles(List<Path> regions,
            List<Path> hazards,
            List<Path> weather,
            List<Path> biomes,
            List<Path> structures,
            List<Path> missions) {
        ArrayList<Path> files = new ArrayList<>();
        files.addAll(regions);
        files.addAll(hazards);
        files.addAll(weather);
        files.addAll(biomes);
        files.addAll(structures);
        files.addAll(missions);
        return files.stream().distinct().sorted(Comparator.comparing(Path::toString)).toList();
    }

    private static List<String> idsFromFiles(Path root, List<Path> files, String contentDirectory) {
        return files.stream()
                .map(path -> {
                    try {
                        Map<String, Object> json = readObject(path);
                        String explicit = string(json, "id", "");
                        return explicit.isBlank() ? contentId(root, path, contentDirectory) : explicit;
                    } catch (IOException exception) {
                        return "";
                    }
                })
                .filter(id -> !id.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    private static List<String> idsFromNamespaceFiles(Path root, List<Path> files, String contentDirectory) {
        return files.stream()
                .map(path -> contentId(root, path, contentDirectory))
                .filter(id -> !id.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    private static String contentId(Path root, Path path, String contentDirectory) {
        String normalized = slash(root.relativize(path));
        String marker = "/data/";
        int dataIndex = normalized.indexOf(marker);
        if (dataIndex < 0) {
            return "";
        }
        String dataPath = normalized.substring(dataIndex + marker.length());
        int slash = dataPath.indexOf('/');
        if (slash < 0) {
            return "";
        }
        String namespace = dataPath.substring(0, slash);
        String pathPart = dataPath.substring(slash + 1);
        String prefix = contentDirectory + "/";
        int prefixIndex = pathPart.indexOf(prefix);
        if (prefixIndex < 0) {
            return "";
        }
        String idPath = pathPart.substring(prefixIndex + prefix.length());
        if (idPath.endsWith(".json")) {
            idPath = idPath.substring(0, idPath.length() - ".json".length());
        }
        return namespace + ":" + idPath;
    }

    private static int spawnRuleCount(Map<String, Object> biome) {
        Object spawners = biome.get("spawners");
        if (!(spawners instanceof Map<?, ?> groups)) {
            return 0;
        }
        int count = 0;
        for (Object value : groups.values()) {
            if (value instanceof List<?> list) {
                count += list.size();
            }
        }
        return count;
    }

    private static int distanceSquared(int ax, int az, int bx, int bz) {
        int dx = ax - bx;
        int dz = az - bz;
        return dx * dx + dz * dz;
    }

    private static String terrainFor(Map<String, Object> biome, int x, int z) {
        List<String> features = flattenedStringList(biome.get("features"));
        if (!features.isEmpty()) {
            return idPath(features.get(Math.floorMod(x + z, features.size())));
        }
        return (x + z) % 2 == 0 ? "ash_dune" : "scorched_flat";
    }

    private static List<String> flattenedStringList(Object value) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        addFlattenedStrings(values, value);
        return List.copyOf(values);
    }

    private static void addFlattenedStrings(LinkedHashSet<String> values, Object value) {
        if (value instanceof String text) {
            values.add(text);
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addFlattenedStrings(values, item);
            }
        }
    }

    private static double hazardMultiplier(String difficulty) {
        return switch (difficulty) {
            case "easy" -> 1.0D;
            case "hard" -> 1.5D;
            case "extreme" -> 2.0D;
            default -> 1.25D;
        };
    }

    private static double spawnMultiplier(String difficulty) {
        return switch (difficulty) {
            case "easy" -> 0.85D;
            case "hard" -> 1.25D;
            case "extreme" -> 1.5D;
            default -> 1.0D;
        };
    }

    private static Map<String, Object> readObject(Path path) throws IOException {
        Object parsed = Json.parse(Files.readString(path, StandardCharsets.UTF_8));
        if (parsed instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> typed = (Map<String, Object>) map;
            return typed;
        }
        throw new IllegalArgumentException(path + " must contain a JSON object.");
    }

    private static String firstOr(Map<String, Object> object, String key, String fallback) {
        List<String> values = stringList(object, key);
        return values.isEmpty() ? fallback : values.get(0);
    }

    private static String string(Map<?, ?> object, String key, String fallback) {
        Object value = object.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    private static String stringAt(Map<String, Object> object, List<String> path, String fallback) {
        return valueAt(object, path).map(String::valueOf).orElse(fallback);
    }

    private static int integer(Map<?, ?> object, String key, int fallback) {
        return integerValue(object.get(key), fallback);
    }

    private static int integerAt(Map<String, Object> object, List<String> path, int fallback) {
        return valueAt(object, path).map(value -> integerValue(value, fallback)).orElse(fallback);
    }

    private static int integerValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            return Integer.parseInt(String.valueOf(value));
        }
        return fallback;
    }

    private static double doubleAt(Map<String, Object> object, List<String> path, double fallback) {
        return valueAt(object, path).map(value -> doubleValue(value, fallback)).orElse(fallback);
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value != null) {
            return Double.parseDouble(String.valueOf(value));
        }
        return fallback;
    }

    private static boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return fallback;
    }

    private static List<String> stringList(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().map(String::valueOf).toList();
    }

    @SuppressWarnings("unchecked")
    private static Optional<Object> valueAt(Map<String, Object> object, List<String> path) {
        Object current = object;
        for (String segment : path) {
            if (!(current instanceof Map<?, ?> map)) {
                return Optional.empty();
            }
            current = ((Map<String, Object>) map).get(segment);
            if (current == null) {
                return Optional.empty();
            }
        }
        return Optional.of(current);
    }

    private static String idPath(String id) {
        int colon = id.indexOf(':');
        String path = colon >= 0 ? id.substring(colon + 1) : id;
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static String slash(Path path) {
        return path.toString().replace('\\', '/');
    }

    public record EchoStandaloneWorldDefinitionProfile(
            String id,
            String regionPath,
            String missionPath,
            String weatherPath,
            String defaultRegionId,
            String requiredHazardId,
            String missingHazardMessage,
            String defaultBiomeId,
            String defaultBiomeTag,
            String defaultMissionId,
            String missionRuntimePrefix,
            String defaultWeatherId,
            String defaultWeatherDisplayName,
            String defaultWeatherTerminalWarning,
            String defaultWeatherType,
            String defaultWeatherSeverity,
            String defaultWeatherScope,
            String defaultParticle,
            double defaultVisibility,
            String defaultWeatherAudioCuePrefix,
            String defaultWeatherRenderProfilePrefix,
            String playerId,
            String weatherActorId,
            int playerX,
            int playerY,
            int playerZ,
            int structureX,
            int structureZ,
            String spawnRuleSuffix,
            String sourceReason,
            String worldId,
            String dimensionId,
            String dimensionDisplayName,
            String dimensionTerrainProfile,
            String poiPrefix,
            String fallbackStructureId,
            String fallbackSpawnEntityId,
            long worldSeed,
            long regionEnterTick,
            long regionExitTick,
            long weatherScheduleTick,
            long weatherForecastTick,
            long weatherActiveTick,
            long weatherEndedTick,
            long hazardDamageTick,
            long worldCellSampleTick,
            long structureLookupTick,
            long statusSaveTick,
            long statusLoadTick,
            long spawnEventTick
    ) {
        public EchoStandaloneWorldDefinitionProfile {
            id = EchoWorldText.requireText(id, "id");
            regionPath = EchoWorldText.requireText(regionPath, "regionPath");
            missionPath = missionPath == null ? "" : missionPath.trim();
            weatherPath = EchoWorldText.requireText(weatherPath, "weatherPath");
            defaultRegionId = EchoWorldText.requireText(defaultRegionId, "defaultRegionId");
            requiredHazardId = requiredHazardId == null ? "" : requiredHazardId.trim();
            missingHazardMessage = EchoWorldText.requireText(missingHazardMessage, "missingHazardMessage");
            defaultBiomeId = EchoWorldText.requireText(defaultBiomeId, "defaultBiomeId");
            defaultBiomeTag = EchoWorldText.requireText(defaultBiomeTag, "defaultBiomeTag");
            defaultMissionId = EchoWorldText.requireText(defaultMissionId, "defaultMissionId");
            missionRuntimePrefix = EchoWorldText.requireText(missionRuntimePrefix, "missionRuntimePrefix");
            defaultWeatherId = EchoWorldText.requireText(defaultWeatherId, "defaultWeatherId");
            defaultWeatherDisplayName = EchoWorldText.requireText(defaultWeatherDisplayName, "defaultWeatherDisplayName");
            defaultWeatherTerminalWarning =
                    EchoWorldText.requireText(defaultWeatherTerminalWarning, "defaultWeatherTerminalWarning");
            defaultWeatherType = EchoWorldText.requireText(defaultWeatherType, "defaultWeatherType");
            defaultWeatherSeverity = EchoWorldText.requireText(defaultWeatherSeverity, "defaultWeatherSeverity");
            defaultWeatherScope = EchoWorldText.requireText(defaultWeatherScope, "defaultWeatherScope");
            defaultParticle = EchoWorldText.requireText(defaultParticle, "defaultParticle");
            if (defaultVisibility < 0.0D) {
                throw new IllegalArgumentException("defaultVisibility must not be negative");
            }
            defaultWeatherAudioCuePrefix =
                    EchoWorldText.requireText(defaultWeatherAudioCuePrefix, "defaultWeatherAudioCuePrefix");
            defaultWeatherRenderProfilePrefix =
                    EchoWorldText.requireText(defaultWeatherRenderProfilePrefix, "defaultWeatherRenderProfilePrefix");
            playerId = EchoWorldText.requireText(playerId, "playerId");
            weatherActorId = EchoWorldText.requireText(weatherActorId, "weatherActorId");
            spawnRuleSuffix = EchoWorldText.requireText(spawnRuleSuffix, "spawnRuleSuffix");
            sourceReason = EchoWorldText.requireText(sourceReason, "sourceReason");
            worldId = EchoWorldText.requireText(worldId, "worldId");
            dimensionId = EchoWorldText.requireText(dimensionId, "dimensionId");
            dimensionDisplayName = EchoWorldText.requireText(dimensionDisplayName, "dimensionDisplayName");
            dimensionTerrainProfile = EchoWorldText.requireText(dimensionTerrainProfile, "dimensionTerrainProfile");
            poiPrefix = EchoWorldText.requireText(poiPrefix, "poiPrefix");
            fallbackStructureId = EchoWorldText.requireText(fallbackStructureId, "fallbackStructureId");
            fallbackSpawnEntityId = EchoWorldText.requireText(fallbackSpawnEntityId, "fallbackSpawnEntityId");
        }

        boolean requiresSpecificHazard() {
            return !requiredHazardId.isBlank();
        }
    }

    public record EchoStandaloneWorldDefinitionSnapshot(
            EchoWorldState worldState,
            EchoStandaloneWorldEffectTick effectTick,
            List<String> sourceFiles,
            String regionId,
            String hazardId,
            String missionId,
            String weatherId,
            String biomeId,
            String structureId,
            EchoStandaloneRegionTransitionRequest regionEnterRequest,
            EchoStandaloneRegionTransitionRequest regionExitRequest,
            EchoStandaloneWeatherScheduleRequest weatherScheduleRequest,
            EchoStandaloneWeatherStateApplyRequest weatherStateApplyRequest,
            EchoStandaloneWeatherStateApplyRequest weatherPhaseStateApplyRequest,
            EchoStandaloneWeatherStateApplyRequest weatherEndedStateApplyRequest,
            EchoStandaloneHazardTickDamageRequest hazardTickDamageRequest,
            EchoStandaloneWorldCellSampleRequest worldCellSampleRequest,
            EchoStandaloneStructurePoiLookupRequest structurePoiLookupRequest,
            EchoStandaloneStatusEffectSaveRequest statusEffectSaveRequest,
            EchoStandaloneStatusEffectLoadRequest statusEffectLoadRequest,
            EchoStandaloneSpawnRuleEventRequest spawnRuleEventRequest) {
        public EchoStandaloneWorldDefinitionSnapshot {
            sourceFiles = List.copyOf(sourceFiles);
        }
    }

    public record EchoStandaloneAgent7CatalogSnapshot(
            List<String> regionIds,
            List<String> hazardIds,
            List<String> weatherProfileIds,
            List<String> biomeIds,
            List<String> structureIds,
            List<String> statusEffectIds,
            List<String> difficultyIds,
            int spawnRuleCount,
            List<String> sourceFiles) {
        public EchoStandaloneAgent7CatalogSnapshot {
            regionIds = List.copyOf(regionIds);
            hazardIds = List.copyOf(hazardIds);
            weatherProfileIds = List.copyOf(weatherProfileIds);
            biomeIds = List.copyOf(biomeIds);
            structureIds = List.copyOf(structureIds);
            statusEffectIds = List.copyOf(statusEffectIds);
            difficultyIds = List.copyOf(difficultyIds);
            if (spawnRuleCount < 0) {
                throw new IllegalArgumentException("spawnRuleCount must not be negative");
            }
            sourceFiles = List.copyOf(sourceFiles);
        }
    }

    private record Id(String namespace, String path) {
        static Id parse(String value) {
            int colon = value.indexOf(':');
            if (colon < 1 || colon == value.length() - 1) {
                throw new IllegalArgumentException("Expected namespaced id: " + value);
            }
            return new Id(value.substring(0, colon), value.substring(colon + 1));
        }
    }

    private record SpawnChoice(String entityId, int maxCount, int weight) {
    }

    private static final class Json {
        private final String text;
        private int index;

        private Json(String text) {
            this.text = text;
        }

        static Object parse(String text) {
            Json parser = new Json(text);
            Object value = parser.readValue();
            parser.skipWhitespace();
            if (!parser.end()) {
                throw parser.error("Unexpected trailing JSON content");
            }
            return value;
        }

        private Object readValue() {
            skipWhitespace();
            if (end()) {
                throw error("Unexpected end of JSON");
            }
            char c = peek();
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield readNumber();
                    }
                    throw error("Unexpected JSON token: " + c);
                }
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            LinkedHashMap<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (tryConsume('}')) {
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                object.put(key, readValue());
                skipWhitespace();
                if (tryConsume('}')) {
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            ArrayList<Object> array = new ArrayList<>();
            skipWhitespace();
            if (tryConsume(']')) {
                return array;
            }
            while (true) {
                array.add(readValue());
                skipWhitespace();
                if (tryConsume(']')) {
                    return array;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = next();
                if (c == '"') {
                    return builder.toString();
                }
                if (c == '\\') {
                    if (end()) {
                        throw error("Unterminated escape sequence");
                    }
                    char escape = next();
                    switch (escape) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append(readUnicodeEscape());
                        default -> throw error("Unsupported escape sequence: \\" + escape);
                    }
                } else {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private char readUnicodeEscape() {
            if (index + 4 > text.length()) {
                throw error("Incomplete unicode escape");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object readNumber() {
            int start = index;
            if (peek() == '-') {
                index++;
            }
            while (!end() && Character.isDigit(peek())) {
                index++;
            }
            boolean decimal = false;
            if (!end() && peek() == '.') {
                decimal = true;
                index++;
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            if (!end() && (peek() == 'e' || peek() == 'E')) {
                decimal = true;
                index++;
                if (!end() && (peek() == '+' || peek() == '-')) {
                    index++;
                }
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            String value = text.substring(start, index);
            return decimal ? Double.parseDouble(value) : Long.parseLong(value);
        }

        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw error("Expected literal " + literal);
            }
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (!end() && Character.isWhitespace(peek())) {
                index++;
            }
        }

        private boolean tryConsume(char expected) {
            if (!end() && peek() == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (end() || next() != expected) {
                throw error("Expected '" + expected + "'");
            }
        }

        private char peek() {
            return text.charAt(index);
        }

        private char next() {
            return text.charAt(index++);
        }

        private boolean end() {
            return index >= text.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at index " + index);
        }
    }
}
