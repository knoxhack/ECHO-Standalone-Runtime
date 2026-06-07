package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSource;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkSource;
import dev.echo.standalone.runtime.world.EchoVoxelWorldGenerationProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientRuntimeWorldgenCatalog {
    private static final EchoClientRuntimeWorldgenCatalog EMPTY =
            new EchoClientRuntimeWorldgenCatalog(List.of(), List.of(), List.of(), List.of());

    private final List<StructurePlacement> placements;
    private final List<FeaturePlacement> features;
    private final List<RegionSurfaceRule> regionRules;
    private final List<BiomeOverlayRule> biomeRules;

    private EchoClientRuntimeWorldgenCatalog(
            List<StructurePlacement> placements,
            List<FeaturePlacement> features,
            List<RegionSurfaceRule> regionRules,
            List<BiomeOverlayRule> biomeRules
    ) {
        this.placements = placements == null ? List.of() : List.copyOf(placements);
        this.features = features == null ? List.of() : List.copyOf(features);
        this.regionRules = regionRules == null ? List.of() : List.copyOf(regionRules);
        this.biomeRules = biomeRules == null ? List.of() : List.copyOf(biomeRules);
    }

    static EchoClientRuntimeWorldgenCatalog empty() {
        return EMPTY;
    }

    static EchoClientRuntimeWorldgenCatalog fromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return EMPTY;
        }
        ArrayList<StructurePlacement> placements = new ArrayList<>();
        ArrayList<FeaturePlacement> features = new ArrayList<>();
        ArrayList<RegionSurfaceRule> regionRules = new ArrayList<>();
        ArrayList<BiomeOverlayRule> biomeRules = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> metadata = map(row == null ? null : row.get("metadata"));
            if (isBiomeRow(row, metadata)) {
                BiomeOverlayRule rule = biomeOverlayRule(row, metadata);
                if (rule != null) {
                    biomeRules.add(rule);
                }
            }
            if (isFeatureRow(row, metadata)) {
                FeaturePlacement feature = featurePlacement(row, metadata);
                if (feature != null) {
                    features.add(feature);
                }
            }
            if (isStructureRow(row, metadata)) {
                StructurePlacement placement = structurePlacement(row, metadata);
                if (placement != null) {
                    placements.add(placement);
                }
            }
            if (isRegionRow(row)) {
                RegionSurfaceRule rule = regionSurfaceRule(row, metadata);
                if (rule != null) {
                    regionRules.add(rule);
                }
            }
        }
        return placements.isEmpty() && features.isEmpty() && regionRules.isEmpty() && biomeRules.isEmpty()
                ? EMPTY
                : new EchoClientRuntimeWorldgenCatalog(placements, features, regionRules, biomeRules);
    }

    boolean emptyCatalog() {
        return placements.isEmpty() && features.isEmpty() && regionRules.isEmpty() && biomeRules.isEmpty();
    }

    String detailSummaryForSmoke() {
        String placement = placements.isEmpty()
                ? "none"
                : placements.get(0).blockId()
                        + "@"
                        + placements.get(0).x()
                        + ","
                        + placements.get(0).y()
                        + ","
                        + placements.get(0).z()
                        + " "
                        + placements.get(0).width()
                        + "x"
                        + placements.get(0).height()
                        + "x"
                        + placements.get(0).depth()
                        + " "
                        + placements.get(0).shape();
        String region = regionRules.isEmpty()
                ? "none"
                : regionRules.get(0).blockId()
                        + "@"
                        + regionRules.get(0).centerX()
                        + ","
                        + regionRules.get(0).centerZ()
                        + " r"
                        + regionRules.get(0).radius()
                        + " y"
                        + regionRules.get(0).fixedY();
        String feature = features.isEmpty()
                ? "none"
                : features.get(0).blockId()
                        + "@"
                        + features.get(0).x()
                        + ","
                        + features.get(0).y()
                        + ","
                        + features.get(0).z()
                        + " "
                        + features.get(0).width()
                        + "x"
                        + features.get(0).height()
                        + "x"
                        + features.get(0).depth()
                        + " "
                        + features.get(0).shape();
        String biome = biomeRules.isEmpty()
                ? "none"
                : biomeRules.get(0).biome().id()
                        + "@"
                        + biomeRules.get(0).centerX()
                        + ","
                        + biomeRules.get(0).centerZ()
                        + " r"
                        + biomeRules.get(0).radius();
        return "placement=" + placement + " feature=" + feature + " region=" + region + " biome=" + biome;
    }

    EchoVoxelSessionRuntimeProfile decorate(
            EchoVoxelSessionRuntimeProfile profile,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (profile == null || emptyCatalog()) {
            return profile;
        }
        return new EchoVoxelSessionRuntimeProfile(
                decorate(profile.worldProfile(), bridge),
                profile.starterHotbar()
        );
    }

    private EchoVoxelWorldRuntimeProfile decorate(
            EchoVoxelWorldRuntimeProfile profile,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (profile == null || emptyCatalog()) {
            return profile;
        }
        EchoVoxelWorldGenerationProfile generation = profile.generationProfile();
        EchoVoxelChunkSource baseSource = generation.chunkSource();
        EchoVoxelBiomeSource biomeSource = biomeRules.isEmpty()
                ? generation.biomeSource()
                : new RuntimeWorldgenBiomeSource(generation.biomeSource(), biomeRules);
        EchoVoxelChunkSource decoratedSource = (seed, chunkX, chunkY, chunkZ) -> {
            EchoVoxelChunk chunk = baseSource.generateChunk(seed, chunkX, chunkY, chunkZ);
            if (chunkY == 0) {
                applyRegionRules(chunk, seed, bridge);
                applyFeaturePlacements(chunk, seed, bridge);
                applyStructurePlacements(chunk, bridge);
                applyBiomeRules(chunk, seed, biomeSource);
            }
            return chunk;
        };
        return new EchoVoxelWorldRuntimeProfile(
                new EchoVoxelWorldGenerationProfile(
                        generation.worldId(),
                        generation.chunkSize(),
                        generation.spawnX(),
                        generation.spawnY(),
                        generation.spawnZ(),
                        generation.spawnYawDegrees(),
                        biomeSource,
                        decoratedSource
                ),
                profile.streamRadius()
        );
    }

    private void applyBiomeRules(
            EchoVoxelChunk chunk,
            long seed,
            EchoVoxelBiomeSource biomeSource
    ) {
        if (biomeRules.isEmpty() || biomeSource == null) {
            return;
        }
        int size = chunk.size();
        int baseX = chunk.id().x() * size;
        int baseZ = chunk.id().z() * size;
        for (int localZ = 0; localZ < size; localZ++) {
            for (int localX = 0; localX < size; localX++) {
                int worldX = baseX + localX;
                int worldZ = baseZ + localZ;
                EchoVoxelBiome biome = biomeSource.biomeAt(seed, worldX, worldZ);
                if (!isRuntimeBiome(biome)) {
                    continue;
                }
                for (int localY = 0; localY < size; localY++) {
                    EchoVoxelBlockState state = chunk.stateAtLocal(localX, localY, localZ);
                    if (!state.air()) {
                        chunk.setStateLocal(localX, localY, localZ, state.withProperty("biome", biome.id()));
                    }
                }
            }
        }
    }

    private void applyRegionRules(
            EchoVoxelChunk chunk,
            long seed,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (regionRules.isEmpty() || bridge == null) {
            return;
        }
        int size = chunk.size();
        int baseX = chunk.id().x() * size;
        int baseZ = chunk.id().z() * size;
        for (RegionSurfaceRule rule : regionRules) {
            EchoVoxelBlock block = block(bridge, rule.blockId());
            if (block == null) {
                continue;
            }
            for (int localZ = 0; localZ < size; localZ++) {
                for (int localX = 0; localX < size; localX++) {
                    int worldX = baseX + localX;
                    int worldZ = baseZ + localZ;
                    if (!rule.applies(seed, worldX, worldZ)) {
                        continue;
                    }
                    int localY = rule.fixedY() >= 0
                            ? rule.fixedY()
                            : highestSurfaceY(chunk, localX, localZ) + rule.surfaceYOffset();
                    if (localY < 0 || localY >= size) {
                        continue;
                    }
                    chunk.setStateLocal(
                            localX,
                            localY,
                            localZ,
                            state(block, "runtime_region", rule.contentId())
                                    .withProperty("region", rule.regionId())
                    );
                }
            }
        }
    }

    private void applyFeaturePlacements(
            EchoVoxelChunk chunk,
            long seed,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (features.isEmpty() || bridge == null) {
            return;
        }
        int size = chunk.size();
        int baseX = chunk.id().x() * size;
        int baseY = chunk.id().y() * size;
        int baseZ = chunk.id().z() * size;
        for (FeaturePlacement feature : features) {
            EchoVoxelBlock block = block(bridge, feature.blockId());
            if (block == null) {
                continue;
            }
            for (int localZ = 0; localZ < size; localZ++) {
                for (int localX = 0; localX < size; localX++) {
                    int worldX = baseX + localX;
                    int worldZ = baseZ + localZ;
                    if (!feature.applies(seed, worldX, worldZ)) {
                        continue;
                    }
                    int anchorY = feature.fixedY() >= 0
                            ? feature.fixedY()
                            : highestSurfaceY(chunk, localX, localZ) + feature.surfaceYOffset();
                    for (int dz = 0; dz < feature.depth(); dz++) {
                        for (int dy = 0; dy < feature.height(); dy++) {
                            for (int dx = 0; dx < feature.width(); dx++) {
                                if (!feature.shape().includes(dx, dy, dz, feature.asStructureShape())) {
                                    continue;
                                }
                                int localTargetX = worldX + dx - baseX;
                                int localTargetY = anchorY + dy - baseY;
                                int localTargetZ = worldZ + dz - baseZ;
                                if (localTargetX < 0 || localTargetY < 0 || localTargetZ < 0
                                        || localTargetX >= size || localTargetY >= size || localTargetZ >= size) {
                                    continue;
                                }
                                chunk.setStateLocal(
                                        localTargetX,
                                        localTargetY,
                                        localTargetZ,
                                        state(block, "runtime_feature", feature.contentId())
                                                .withProperty("feature", feature.featureId())
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyStructurePlacements(
            EchoVoxelChunk chunk,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        if (placements.isEmpty() || bridge == null) {
            return;
        }
        int size = chunk.size();
        int baseX = chunk.id().x() * size;
        int baseY = chunk.id().y() * size;
        int baseZ = chunk.id().z() * size;
        for (StructurePlacement placement : placements) {
            EchoVoxelBlock block = block(bridge, placement.blockId());
            if (block == null) {
                continue;
            }
            for (int dz = 0; dz < placement.depth(); dz++) {
                for (int dy = 0; dy < placement.height(); dy++) {
                    for (int dx = 0; dx < placement.width(); dx++) {
                        if (!placement.shape().includes(dx, dy, dz, placement)) {
                            continue;
                        }
                        int worldX = placement.x() + dx;
                        int worldY = placement.y() + dy;
                        int worldZ = placement.z() + dz;
                        int localX = worldX - baseX;
                        int localY = worldY - baseY;
                        int localZ = worldZ - baseZ;
                        if (localX < 0 || localY < 0 || localZ < 0
                                || localX >= size || localY >= size || localZ >= size) {
                            continue;
                        }
                        chunk.setStateLocal(
                                localX,
                                localY,
                                localZ,
                                state(block, "runtime_structure", placement.contentId())
                                        .withProperty("structure", placement.structureId())
                        );
                    }
                }
            }
        }
    }

    private static EchoVoxelBlockState state(EchoVoxelBlock block, String source, String contentId) {
        return EchoVoxelBlockState.of(block)
                .withProperty("source", source)
                .withProperty("runtimeContentId", contentId);
    }

    private static int highestSurfaceY(EchoVoxelChunk chunk, int localX, int localZ) {
        for (int y = chunk.size() - 1; y >= 0; y--) {
            if (!chunk.stateAtLocal(localX, y, localZ).air()) {
                return y;
            }
        }
        return 0;
    }

    private static EchoVoxelBlock block(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String blockId
    ) {
        if (blockId == null || blockId.isBlank()) {
            return null;
        }
        try {
            return bridge.registry().requireLiveVoxelBlock(blockId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static StructurePlacement structurePlacement(
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        String blockId = firstText(
                row.get("placementBlockId"),
                metadata.get("placementBlockId"),
                row.get("structureBlockId"),
                metadata.get("structureBlockId"),
                row.get("blockId"),
                metadata.get("blockId"),
                row.get("liveVoxelId"),
                metadata.get("liveVoxelId")
        );
        if (blockId.isBlank()) {
            return null;
        }
        String contentId = firstText(row.get("contentId"), metadata.get("contentId"), blockId);
        int x = intValue(firstText(row.get("x"), metadata.get("x"), row.get("originX"), metadata.get("originX")), 0);
        int y = intValue(firstText(
                row.get("y"),
                metadata.get("y"),
                metadata.get("startY"),
                metadata.get("startHeight"),
                valueAt(metadata, "start_height", "absolute")
        ), 5);
        int z = intValue(firstText(row.get("z"), metadata.get("z"), row.get("originZ"), metadata.get("originZ")), 0);
        int width = Math.max(1, intValue(firstText(row.get("width"), metadata.get("width")), 1));
        int height = Math.max(1, intValue(firstText(row.get("height"), metadata.get("height")), 1));
        int depth = Math.max(1, intValue(firstText(row.get("depth"), metadata.get("depth")), 1));
        PlacementShape shape = PlacementShape.parse(firstText(row.get("shape"), metadata.get("shape")));
        String structureId = firstText(
                row.get("structureId"),
                metadata.get("structureId"),
                row.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        return new StructurePlacement(contentId, structureId, blockId, x, y, z, width, height, depth, shape);
    }

    private static FeaturePlacement featurePlacement(
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        String blockId = firstText(
                row.get("featureBlockId"),
                metadata.get("featureBlockId"),
                row.get("generationBlockId"),
                metadata.get("generationBlockId"),
                row.get("placementBlockId"),
                metadata.get("placementBlockId"),
                row.get("blockId"),
                metadata.get("blockId"),
                row.get("liveVoxelId"),
                metadata.get("liveVoxelId")
        );
        if (blockId.isBlank()) {
            return null;
        }
        String contentId = firstText(row.get("contentId"), metadata.get("contentId"), blockId);
        String featureId = firstText(
                row.get("featureId"),
                metadata.get("featureId"),
                row.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        int x = intValue(firstText(row.get("x"), metadata.get("x"), row.get("centerX"), metadata.get("centerX")), 0);
        int y = intValue(firstText(
                row.get("y"),
                metadata.get("y"),
                row.get("fixedY"),
                metadata.get("fixedY")
        ), -1);
        int z = intValue(firstText(row.get("z"), metadata.get("z"), row.get("centerZ"), metadata.get("centerZ")), 0);
        int width = Math.max(1, intValue(firstText(row.get("width"), metadata.get("width")), 1));
        int height = Math.max(1, intValue(firstText(row.get("height"), metadata.get("height")), 1));
        int depth = Math.max(1, intValue(firstText(row.get("depth"), metadata.get("depth")), 1));
        int radius = Math.max(0, intValue(firstText(row.get("radius"), metadata.get("radius")), 0));
        int surfaceYOffset = intValue(firstText(row.get("surfaceYOffset"), metadata.get("surfaceYOffset")), 1);
        int count = Math.max(1, intValue(firstText(row.get("count"), metadata.get("count")), 1));
        double chance = Math.max(0.0D, Math.min(1.0D, doubleValue(firstText(
                row.get("chance"),
                metadata.get("chance"),
                row.get("density"),
                metadata.get("density")
        ), 1.0D)));
        PlacementShape shape = PlacementShape.parse(firstText(row.get("shape"), metadata.get("shape")));
        long seedSalt = text(contentId).hashCode();
        return new FeaturePlacement(
                contentId,
                featureId,
                blockId,
                x,
                y,
                z,
                width,
                height,
                depth,
                radius,
                surfaceYOffset,
                count,
                chance,
                shape,
                seedSalt
        );
    }

    private static RegionSurfaceRule regionSurfaceRule(
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        String blockId = firstText(
                row.get("surfaceBlockId"),
                metadata.get("surfaceBlockId"),
                row.get("regionBlockId"),
                metadata.get("regionBlockId"),
                row.get("generationBlockId"),
                metadata.get("generationBlockId"),
                row.get("blockId"),
                metadata.get("blockId")
        );
        if (blockId.isBlank()) {
            return null;
        }
        String contentId = firstText(row.get("contentId"), metadata.get("contentId"), blockId);
        String regionId = firstText(
                row.get("regionId"),
                metadata.get("regionId"),
                row.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        int centerX = intValue(firstText(row.get("centerX"), metadata.get("centerX"), row.get("x"), metadata.get("x")), 0);
        int centerZ = intValue(firstText(row.get("centerZ"), metadata.get("centerZ"), row.get("z"), metadata.get("z")), 0);
        int radius = Math.max(0, intValue(firstText(row.get("radius"), metadata.get("radius")), 0));
        int fixedY = intValue(firstText(row.get("fixedY"), metadata.get("fixedY"), row.get("y"), metadata.get("y")), -1);
        int surfaceYOffset = intValue(firstText(row.get("surfaceYOffset"), metadata.get("surfaceYOffset")), 0);
        long seedSalt = text(contentId).hashCode();
        return new RegionSurfaceRule(contentId, regionId, blockId, centerX, centerZ, radius, fixedY, surfaceYOffset, seedSalt);
    }

    private static BiomeOverlayRule biomeOverlayRule(
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        String biomeId = firstText(
                row.get("biomeId"),
                metadata.get("biomeId"),
                row.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                row.get("contentId"),
                metadata.get("contentId")
        );
        if (biomeId.isBlank()) {
            return null;
        }
        String displayName = firstText(row.get("displayName"), metadata.get("displayName"), idPath(biomeId));
        double temperature = doubleValue(firstText(row.get("temperature"), metadata.get("temperature")), 0.8D);
        double downfall = Math.max(0.0D, doubleValue(firstText(
                row.get("downfall"),
                metadata.get("downfall"),
                metadata.get("humidity")
        ), 0.1D));
        int fogColor = colorValue(firstText(
                row.get("fogColor"),
                metadata.get("fogColor"),
                metadata.get("fog_color"),
                valueAt(metadata, "effects", "fog_color")
        ), 0x5F7741);
        int grassColor = colorValue(firstText(
                row.get("grassColor"),
                metadata.get("grassColor"),
                metadata.get("grass_color"),
                valueAt(metadata, "effects", "grass_color")
        ), 0x5F7741);
        String ambientParticle = firstText(
                row.get("ambientParticle"),
                metadata.get("ambientParticle"),
                valueAt(metadata, "effects", "ambient_particle", "options", "type"),
                "minecraft:ash"
        );
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add("runtime_worldgen");
        tags.addAll(stringList(row.get("tags")));
        tags.addAll(stringList(row.get("biomeTags")));
        tags.addAll(stringList(metadata.get("tags")));
        tags.addAll(stringList(metadata.get("biomeTags")));
        EchoVoxelBiome biome = new EchoVoxelBiome(
                biomeId,
                displayName,
                temperature,
                downfall,
                fogColor,
                grassColor,
                ambientParticle,
                List.copyOf(tags)
        );
        int centerX = intValue(firstText(row.get("centerX"), metadata.get("centerX"), row.get("x"), metadata.get("x")), 0);
        int centerZ = intValue(firstText(row.get("centerZ"), metadata.get("centerZ"), row.get("z"), metadata.get("z")), 0);
        int radius = Math.max(0, intValue(firstText(row.get("radius"), metadata.get("radius")), 0));
        long seedSalt = text(firstText(row.get("contentId"), metadata.get("contentId"), biomeId)).hashCode();
        return new BiomeOverlayRule(biome, centerX, centerZ, radius, seedSalt);
    }

    private static boolean isStructureRow(Map<String, Object> row, Map<String, Object> metadata) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(firstText(row.get("contentKind"), metadata.get("contentKind")));
        String domain = firstText(row.get("domain"), metadata.get("domain")).toLowerCase(Locale.ROOT);
        String worldgenType = normalizedEnumToken(firstText(
                row.get("worldgenType"),
                metadata.get("worldgenType"),
                row.get("type"),
                metadata.get("type")
        ));
        if (worldgenType.equals("FEATURE") || kind.equals("FEATURE") || domain.equals("features")) {
            return false;
        }
        return kind.equals("STRUCTURE")
                || domain.equals("structures")
                || ((kind.equals("WORLDGEN_DEFINITION") || domain.equals("worldgen"))
                        && (worldgenType.equals("STRUCTURE")
                        || !firstText(row.get("structureId"), metadata.get("structureId")).isBlank()))
                || domain.equals("maps");
    }

    private static boolean isFeatureRow(Map<String, Object> row, Map<String, Object> metadata) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(firstText(row.get("contentKind"), metadata.get("contentKind")));
        String domain = firstText(row.get("domain"), metadata.get("domain")).toLowerCase(Locale.ROOT);
        String worldgenType = normalizedEnumToken(firstText(
                row.get("worldgenType"),
                metadata.get("worldgenType"),
                row.get("type"),
                metadata.get("type")
        ));
        return kind.equals("FEATURE")
                || domain.equals("features")
                || worldgenType.equals("FEATURE")
                || ((kind.equals("WORLDGEN_DEFINITION") || domain.equals("worldgen"))
                && !firstText(
                        row.get("featureId"),
                        metadata.get("featureId"),
                        row.get("featureBlockId"),
                        metadata.get("featureBlockId"),
                        row.get("generationBlockId"),
                        metadata.get("generationBlockId")
                ).isBlank());
    }

    private static boolean isRegionRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(text(row.get("contentKind")));
        String domain = text(row.get("domain")).toLowerCase(Locale.ROOT);
        return kind.equals("WORLD_REGION")
                || domain.equals("world_regions")
                || domain.equals("regions");
    }

    private static boolean isBiomeRow(Map<String, Object> row, Map<String, Object> metadata) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(firstText(row.get("contentKind"), metadata.get("contentKind")));
        String domain = firstText(row.get("domain"), metadata.get("domain")).toLowerCase(Locale.ROOT);
        String worldgenType = normalizedEnumToken(firstText(
                row.get("worldgenType"),
                metadata.get("worldgenType"),
                row.get("type"),
                metadata.get("type")
        ));
        return kind.equals("BIOME")
                || domain.equals("biomes")
                || worldgenType.equals("BIOME")
                || ((kind.equals("WORLDGEN_DEFINITION") || domain.equals("worldgen"))
                        && !firstText(row.get("biomeId"), metadata.get("biomeId")).isBlank());
    }

    private static Object valueAt(Map<String, Object> root, String... keys) {
        if (root == null || keys == null || keys.length == 0) {
            return null;
        }
        Object value = root;
        for (String key : keys) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(key);
        }
        return value;
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String normalizedEnumToken(String value) {
        return text(value)
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_')
                .replace(' ', '_');
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static int intValue(String value, int fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            try {
                double parsed = Double.parseDouble(text);
                if (!Double.isFinite(parsed)) {
                    return fallback;
                }
                return (int) Math.round(parsed);
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }

    private static double doubleValue(String value, double fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            double parsed = Double.parseDouble(text);
            return Double.isFinite(parsed) ? parsed : fallback;
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int colorValue(String value, int fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            if (text.startsWith("#")) {
                return Integer.parseUnsignedInt(text.substring(1), 16);
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return (int) Long.parseUnsignedLong(text.substring(2), 16);
            }
            return (int) Long.parseLong(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static List<String> stringList(Object value) {
        if (value instanceof Iterable<?> iterable) {
            ArrayList<String> result = new ArrayList<>();
            for (Object item : iterable) {
                String text = text(item);
                if (!text.isBlank()) {
                    result.add(text);
                }
            }
            return List.copyOf(result);
        }
        String text = text(value);
        if (text.isBlank()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (String token : text.split("[,|]")) {
            String trimmed = token.trim();
            if (!trimmed.isBlank()) {
                result.add(trimmed);
            }
        }
        return List.copyOf(result);
    }

    private static String idPath(String id) {
        String text = text(id);
        int separator = text.indexOf(':');
        return separator >= 0 ? text.substring(separator + 1) : text;
    }

    private static boolean isRuntimeBiome(EchoVoxelBiome biome) {
        return biome != null && biome.hasTag("runtime_worldgen");
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, Object> mapped = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                mapped.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(mapped);
    }

    private record StructurePlacement(
            String contentId,
            String structureId,
            String blockId,
            int x,
            int y,
            int z,
            int width,
            int height,
            int depth,
            PlacementShape shape
    ) {
    }

    private record FeaturePlacement(
            String contentId,
            String featureId,
            String blockId,
            int x,
            int y,
            int z,
            int width,
            int height,
            int depth,
            int radius,
            int surfaceYOffset,
            int count,
            double chance,
            PlacementShape shape,
            long seedSalt
    ) {
        int fixedY() {
            return y;
        }

        boolean applies(long seed, int worldX, int worldZ) {
            if (radius <= 0) {
                return worldX == x && worldZ == z && chance > 0.0D;
            }
            long dx = worldX - x;
            long dz = worldZ - z;
            if (dx * dx + dz * dz > (long) radius * radius || chance <= 0.0D) {
                return false;
            }
            if (worldX == x && worldZ == z) {
                return true;
            }
            long mixed = seed ^ seedSalt;
            mixed ^= (long) worldX * 0x9E3779B97F4A7C15L;
            mixed ^= (long) worldZ * 0xC2B2AE3D27D4EB4FL;
            mixed ^= mixed >>> 33;
            int area = Math.max(1, (radius * 2 + 1) * (radius * 2 + 1));
            int densitySlots = Math.max(count, (int) Math.round(area * chance));
            return Math.floorMod(mixed, area) < Math.min(area, densitySlots);
        }

        StructurePlacement asStructureShape() {
            return new StructurePlacement(
                    contentId,
                    featureId,
                    blockId,
                    x,
                    y,
                    z,
                    width,
                    height,
                    depth,
                    shape
            );
        }
    }

    private record RegionSurfaceRule(
            String contentId,
            String regionId,
            String blockId,
            int centerX,
            int centerZ,
            int radius,
            int fixedY,
            int surfaceYOffset,
            long seedSalt
    ) {
        boolean applies(long seed, int x, int z) {
            if (radius > 0) {
                long dx = x - centerX;
                long dz = z - centerZ;
                return dx * dx + dz * dz <= (long) radius * radius;
            }
            long mixed = seed ^ seedSalt;
            mixed ^= (long) x * 0x9E3779B97F4A7C15L;
            mixed ^= (long) z * 0xC2B2AE3D27D4EB4FL;
            mixed ^= mixed >>> 33;
            return Math.floorMod(mixed, 19L) == 0L;
        }
    }

    private record BiomeOverlayRule(
            EchoVoxelBiome biome,
            int centerX,
            int centerZ,
            int radius,
            long seedSalt
    ) {
        private BiomeOverlayRule {
            if (biome == null) {
                throw new IllegalArgumentException("biome must not be null");
            }
        }

        boolean applies(long seed, int x, int z) {
            if (radius > 0) {
                long dx = x - centerX;
                long dz = z - centerZ;
                return dx * dx + dz * dz <= (long) radius * radius;
            }
            long mixed = seed ^ seedSalt;
            mixed ^= (long) x * 0x9E3779B97F4A7C15L;
            mixed ^= (long) z * 0xC2B2AE3D27D4EB4FL;
            mixed ^= mixed >>> 33;
            return Math.floorMod(mixed, 23L) == 0L;
        }
    }

    private static final class RuntimeWorldgenBiomeSource implements EchoVoxelBiomeSource {
        private final EchoVoxelBiomeSource base;
        private final List<BiomeOverlayRule> biomeRules;

        private RuntimeWorldgenBiomeSource(
                EchoVoxelBiomeSource base,
                List<BiomeOverlayRule> biomeRules
        ) {
            this.base = base;
            this.biomeRules = biomeRules == null ? List.of() : List.copyOf(biomeRules);
        }

        @Override
        public String id() {
            String baseId = base == null ? "echo:unknown" : base.id();
            return baseId + "+runtime_worldgen";
        }

        @Override
        public EchoVoxelBiome biomeAt(long seed, int x, int z) {
            for (int i = biomeRules.size() - 1; i >= 0; i--) {
                BiomeOverlayRule rule = biomeRules.get(i);
                if (rule.applies(seed, x, z)) {
                    return rule.biome();
                }
            }
            return base == null ? null : base.biomeAt(seed, x, z);
        }
    }

    private enum PlacementShape {
        MARKER,
        PLATFORM,
        PILLAR,
        WALL,
        CUBE;

        static PlacementShape parse(String value) {
            String normalized = normalizedEnumToken(value);
            if (normalized.isBlank()) {
                return MARKER;
            }
            try {
                return PlacementShape.valueOf(normalized);
            } catch (IllegalArgumentException exception) {
                return MARKER;
            }
        }

        boolean includes(int dx, int dy, int dz, StructurePlacement placement) {
            return switch (this) {
                case MARKER -> dx == 0 && dy == 0 && dz == 0;
                case PLATFORM -> dy == 0;
                case PILLAR -> dx == 0 && dz == 0;
                case WALL -> dz == 0;
                case CUBE -> true;
            };
        }
    }
}
