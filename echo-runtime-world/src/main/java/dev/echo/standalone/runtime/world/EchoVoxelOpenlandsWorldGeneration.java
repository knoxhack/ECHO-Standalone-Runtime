package dev.echo.standalone.runtime.world;

import java.util.Objects;
import java.util.function.Function;

public final class EchoVoxelOpenlandsWorldGeneration {
    public static final String WORLD_ID = "echoopenlandsprotocol:standalone_first_hour";
    private static final int CHUNK_SIZE = 16;
    private static final double SPAWN_X = 7.5D;
    private static final double SPAWN_Y = 5.8D;
    private static final double SPAWN_Z = 7.5D;
    private static final double SPAWN_YAW_DEGREES = 22.0D;

    private EchoVoxelOpenlandsWorldGeneration() {
    }

    public static EchoVoxelWorldGenerationProfile firstHourProfile(Blocks blocks) {
        Blocks safeBlocks = Objects.requireNonNull(blocks, "blocks");
        return new EchoVoxelWorldGenerationProfile(
                WORLD_ID,
                CHUNK_SIZE,
                SPAWN_X,
                SPAWN_Y,
                SPAWN_Z,
                SPAWN_YAW_DEGREES,
                EchoVoxelOpenlandsBiomes.source(),
                (seed, chunkX, chunkY, chunkZ) -> generateChunk(seed, safeBlocks, chunkX, chunkY, chunkZ)
        );
    }

    public static EchoVoxelWorld generateFirstHourRegion(long seed, Blocks blocks, int horizontalRadius) {
        return new EchoVoxelWorldGenerator().generateRegion(firstHourProfile(blocks), seed, horizontalRadius);
    }

    public static EchoVoxelWorld generateFirstHour(long seed, Blocks blocks) {
        return generateFirstHourRegion(seed, blocks, 0);
    }

    public static EchoVoxelWorldStreamer streamer(Blocks blocks, int horizontalRadius) {
        EchoVoxelWorldGenerationProfile profile = firstHourProfile(blocks);
        return new EchoVoxelWorldStreamer(profile.chunkSource(), profile.biomeSource(), horizontalRadius);
    }

    public static EchoVoxelChunk generateChunk(
            long seed,
            Blocks blocks,
            int chunkX,
            int chunkY,
            int chunkZ
    ) {
        Objects.requireNonNull(blocks, "blocks");
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(chunkX, chunkY, chunkZ), CHUNK_SIZE);
        if (chunkY != 0) {
            return chunk;
        }
        for (int z = 0; z < CHUNK_SIZE; z++) {
            for (int x = 0; x < CHUNK_SIZE; x++) {
                int worldX = chunkX * CHUNK_SIZE + x;
                int worldZ = chunkZ * CHUNK_SIZE + z;
                EchoVoxelBiome biome = EchoVoxelOpenlandsBiomes.biomeAt(seed, worldX, worldZ);
                int height = terrainHeight(worldX, worldZ, seed, biome);
                double scatter = featureChance(seed, worldX, worldZ);
                for (int y = 0; y <= height; y++) {
                    EchoVoxelBlock fillBlock = y == height
                            ? surfaceBlock(blocks, biome, scatter, height)
                            : subsurfaceBlock(blocks, biome, y, height, scatter);
                    setGeneratedBlockLocal(chunk, x, y, z, fillBlock, "terrain", biome);
                }
                placeSurfaceResource(chunk, blocks, biome, x, height + 1, z, scatter);
            }
        }
        if (chunkX == 0 && chunkZ == 0) {
            addFirstHourLandmarks(chunk, blocks);
        }
        return chunk;
    }

    private static int terrainHeight(int x, int z, long seed, EchoVoxelBiome biome) {
        int roll = (int) Math.round(Math.sin((x + seed % 13) * 0.28D) + Math.cos((z - seed % 11) * 0.24D));
        int height = 2 + roll;
        if (biome.hasTag("stone")) {
            height += 1;
        } else if (biome.hasTag("wetland")) {
            height -= 1;
        }
        return Math.max(1, Math.min(5, height));
    }

    private static EchoVoxelBlock surfaceBlock(Blocks blocks, EchoVoxelBiome biome, double scatter, int height) {
        if (biome.hasTag("wetland")) {
            if (scatter < 0.48D && height <= 2) {
                return blocks.mud();
            }
            return scatter < 0.68D ? blocks.clay() : blocks.forestSoil();
        }
        if (biome.hasTag("stone")) {
            if (scatter < 0.18D) {
                return blocks.limestone();
            }
            return scatter < 0.64D ? blocks.fieldstone() : blocks.shale();
        }
        if (biome.hasTag("wood")) {
            return scatter < 0.72D ? blocks.forestSoil() : blocks.meadowGrass();
        }
        return scatter < 0.10D ? blocks.forestSoil() : blocks.meadowGrass();
    }

    private static EchoVoxelBlock subsurfaceBlock(
            Blocks blocks,
            EchoVoxelBiome biome,
            int y,
            int height,
            double scatter
    ) {
        if (biome.hasTag("wetland") && y == height - 1 && scatter < 0.62D) {
            return blocks.clay();
        }
        if (biome.hasTag("stone") && y >= height - 2) {
            return scatter < 0.5D ? blocks.fieldstone() : blocks.limestone();
        }
        if (y <= 1 && scatter < 0.14D) {
            return blocks.deepstone();
        }
        return blocks.fieldstone();
    }

    private static void placeSurfaceResource(
            EchoVoxelChunk chunk,
            Blocks blocks,
            EchoVoxelBiome biome,
            int x,
            int y,
            int z,
            double scatter
    ) {
        if (y >= CHUNK_SIZE) {
            return;
        }
        if (biome.hasTag("wetland") && scatter >= 0.74D && scatter < 0.80D) {
            setGeneratedBlockLocal(chunk, x, y, z, blocks.mud(), "resource", biome,
                    "resource", "reed_fiber",
                    "gatherable", "true");
            return;
        }
        if (biome.hasTag("wood") && scatter >= 0.82D && scatter < 0.86D) {
            pillar(chunk, x, z, y, 2, blocks.branchwoodLog(), biome, "resource",
                    "resource", "branchwood_log",
                    "gatherable", "true");
            return;
        }
        if (biome.hasTag("stone") && scatter >= 0.80D && scatter < 0.86D) {
            setGeneratedBlockLocal(chunk, x, y, z, blocks.fieldstone(), "resource", biome,
                    "resource", "loose_fieldstone",
                    "gatherable", "true");
            return;
        }
        if (biome.hasTag("starter") && scatter >= 0.87D && scatter < 0.90D) {
            setGeneratedBlockLocal(chunk, x, y, z, blocks.meadowGrass(), "resource", biome,
                    "resource", "berries",
                    "gatherable", "true");
        }
    }

    private static void addFirstHourLandmarks(EchoVoxelChunk chunk, Blocks blocks) {
        EchoVoxelBiome biome = EchoVoxelOpenlandsBiomes.MEADOWS;
        for (int x = 1; x <= 14; x++) {
            setGeneratedBlockLocal(chunk, x, 4, 8, blocks.oldRoadBlock(), "landmark", biome,
                    "oldRoadSegment", "true",
                    "starterRoute", "spawn_to_waystone");
        }
        setGeneratedBlockLocal(chunk, 2, 5, 8, blocks.oldRoadMarker(), "landmark", biome,
                "landmark", "old_road_marker",
                "hint", "first_waystone_west");
        setGeneratedBlockLocal(chunk, 4, 5, 5, blocks.branchwoodLog(), "resource", biome,
                "resource", "branchwood_log",
                "gatherable", "true",
                "starterGuarantee", "tree");
        setGeneratedBlockLocal(chunk, 5, 5, 5, blocks.branchwoodLog(), "resource", biome,
                "resource", "branchwood_log",
                "gatherable", "true",
                "starterGuarantee", "tree");
        setGeneratedBlockLocal(chunk, 10, 5, 4, blocks.fieldstone(), "resource", biome,
                "resource", "loose_fieldstone",
                "gatherable", "true",
                "starterGuarantee", "stone");
        setGeneratedBlockLocal(chunk, 11, 5, 4, blocks.fieldstone(), "resource", biome,
                "resource", "loose_fieldstone",
                "gatherable", "true",
                "starterGuarantee", "stone");
        setGeneratedBlockLocal(chunk, 6, 5, 11, blocks.meadowGrass(), "resource", biome,
                "resource", "berries",
                "gatherable", "true",
                "starterGuarantee", "food");
        setGeneratedBlockLocal(chunk, 7, 5, 11, blocks.mud(), "resource", biome,
                "resource", "reed_fiber",
                "gatherable", "true",
                "starterGuarantee", "fiber");
        well(chunk, blocks, 12, 11, biome);
        caveMouth(chunk, blocks, 13, 2, biome);
        waystoneSite(chunk, blocks, 1, 11, biome);
    }

    private static void well(EchoVoxelChunk chunk, Blocks blocks, int centerX, int centerZ, EchoVoxelBiome biome) {
        platform(chunk, centerX - 1, centerZ - 1, 3, 3, 5, blocks.fieldstone(), biome, "landmark",
                "landmark", "water_or_well_hint",
                "starterGuarantee", "water");
        setGeneratedBlockLocal(chunk, centerX, 6, centerZ, blocks.oldRoadMarker(), "landmark", biome,
                "landmark", "ruined_well_marker",
                "hint", "safe_water_near_spawn");
    }

    private static void caveMouth(EchoVoxelChunk chunk, Blocks blocks, int startX, int startZ, EchoVoxelBiome biome) {
        for (int z = startZ; z < startZ + 3; z++) {
            setGeneratedBlockLocal(chunk, startX, 4, z, blocks.deepstone(), "landmark", biome,
                    "landmark", "cave_mouth",
                    "starterGuarantee", "cave_or_ruin");
            setGeneratedBlockLocal(chunk, startX, 5, z, blocks.deepstone(), "landmark", biome,
                    "landmark", "cave_mouth",
                    "starterGuarantee", "cave_or_ruin");
        }
    }

    private static void waystoneSite(EchoVoxelChunk chunk, Blocks blocks, int x, int z, EchoVoxelBiome biome) {
        platform(chunk, x, z, 3, 3, 5, blocks.waystonePlinth(), biome, "landmark",
                "landmark", "broken_waystone_site",
                "starterGuarantee", "first_waystone");
        setGeneratedBlockLocal(chunk, x + 1, 6, z + 1, blocks.brokenWaystone(), "landmark", biome,
                "landmark", "broken_waystone",
                "waystoneState", "discovered");
    }

    private static void platform(
            EchoVoxelChunk chunk,
            int startX,
            int startZ,
            int width,
            int depth,
            int y,
            EchoVoxelBlock block,
            EchoVoxelBiome biome,
            String source,
            String... properties
    ) {
        for (int z = startZ; z < startZ + depth; z++) {
            for (int x = startX; x < startX + width; x++) {
                setGeneratedBlockLocal(chunk, x, y, z, block, source, biome, properties);
            }
        }
    }

    private static void pillar(
            EchoVoxelChunk chunk,
            int x,
            int z,
            int startY,
            int height,
            EchoVoxelBlock block,
            EchoVoxelBiome biome,
            String source,
            String... properties
    ) {
        for (int y = startY; y < startY + height && y < CHUNK_SIZE; y++) {
            setGeneratedBlockLocal(chunk, x, y, z, block, source, biome, properties);
        }
    }

    private static void setGeneratedBlockLocal(
            EchoVoxelChunk chunk,
            int x,
            int y,
            int z,
            EchoVoxelBlock block,
            String source,
            EchoVoxelBiome biome,
            String... properties
    ) {
        if (properties.length % 2 != 0) {
            throw new IllegalArgumentException("generated block properties must be key/value pairs");
        }
        EchoVoxelBlockState state = EchoVoxelBlockState.of(block)
                .withProperty("source", source)
                .withProperty("biome", biome.id());
        for (int index = 0; index < properties.length; index += 2) {
            state = state.withProperty(properties[index], properties[index + 1]);
        }
        chunk.setStateLocal(x, y, z, state);
    }

    private static double featureChance(long seed, int x, int z) {
        long mixed = seed;
        mixed ^= (long) x * 0x9E3779B97F4A7C15L;
        mixed ^= (long) z * 0xC2B2AE3D27D4EB4FL;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return ((mixed >>> 11) & ((1L << 53) - 1)) * 0x1.0p-53;
    }

    private static EchoVoxelBlock block(String id, String name, int argb, double hardness) {
        return new EchoVoxelBlock(id, name, argb, true, true, hardness);
    }

    public record Blocks(
            EchoVoxelBlock meadowGrass,
            EchoVoxelBlock forestSoil,
            EchoVoxelBlock mud,
            EchoVoxelBlock clay,
            EchoVoxelBlock fieldstone,
            EchoVoxelBlock limestone,
            EchoVoxelBlock shale,
            EchoVoxelBlock deepstone,
            EchoVoxelBlock branchwoodLog,
            EchoVoxelBlock branchwoodPlanks,
            EchoVoxelBlock oldRoadBlock,
            EchoVoxelBlock oldRoadMarker,
            EchoVoxelBlock brokenWaystone,
            EchoVoxelBlock waystonePlinth
    ) {
        public Blocks {
            meadowGrass = requireBlock(meadowGrass, "meadowGrass");
            forestSoil = requireBlock(forestSoil, "forestSoil");
            mud = requireBlock(mud, "mud");
            clay = requireBlock(clay, "clay");
            fieldstone = requireBlock(fieldstone, "fieldstone");
            limestone = requireBlock(limestone, "limestone");
            shale = requireBlock(shale, "shale");
            deepstone = requireBlock(deepstone, "deepstone");
            branchwoodLog = requireBlock(branchwoodLog, "branchwoodLog");
            branchwoodPlanks = requireBlock(branchwoodPlanks, "branchwoodPlanks");
            oldRoadBlock = requireBlock(oldRoadBlock, "oldRoadBlock");
            oldRoadMarker = requireBlock(oldRoadMarker, "oldRoadMarker");
            brokenWaystone = requireBlock(brokenWaystone, "brokenWaystone");
            waystonePlinth = requireBlock(waystonePlinth, "waystonePlinth");
        }

        public static Blocks defaults() {
            return new Blocks(
                    block("echoopenlandsprotocol:meadow_grass_block", "Meadow Grass Block", 0xFF6EA84F, 0.45D),
                    block("echoopenlandsprotocol:forest_soil", "Forest Soil", 0xFF5E4A2F, 0.55D),
                    block("echoopenlandsprotocol:mud", "Mud", 0xFF5C5840, 0.35D),
                    block("echoopenlandsprotocol:clay", "Clay", 0xFF9B8772, 0.6D),
                    block("echoopenlandsprotocol:fieldstone", "Fieldstone", 0xFF787D78, 1.4D),
                    block("echoopenlandsprotocol:limestone", "Limestone", 0xFFA7A58E, 1.3D),
                    block("echoopenlandsprotocol:shale", "Shale", 0xFF596064, 1.5D),
                    block("echoopenlandsprotocol:deepstone", "Deepstone", 0xFF3E4548, 2.0D),
                    block("echoopenlandsprotocol:branchwood_log", "Branchwood Log", 0xFF7D603B, 1.0D),
                    block("echoopenlandsprotocol:branchwood_planks", "Branchwood Planks", 0xFFA77D4B, 0.8D),
                    block("echoopenlandsprotocol:old_road_block", "Old Road Block", 0xFF8A8171, 1.1D),
                    block("echoopenlandsprotocol:old_road_marker", "Old Road Marker", 0xFF9E967D, 1.2D),
                    block("echoopenlandsprotocol:broken_waystone", "Broken Waystone", 0xFF7C8788, 2.0D),
                    block("echoopenlandsprotocol:waystone_plinth", "Waystone Plinth", 0xFF6F7777, 1.7D)
            );
        }

        public static Blocks fromRegistry(Function<String, EchoVoxelBlock> lookup) {
            return new Blocks(
                    lookup.apply("echoopenlandsprotocol:meadow_grass_block"),
                    lookup.apply("echoopenlandsprotocol:forest_soil"),
                    lookup.apply("echoopenlandsprotocol:mud"),
                    lookup.apply("echoopenlandsprotocol:clay"),
                    lookup.apply("echoopenlandsprotocol:fieldstone"),
                    lookup.apply("echoopenlandsprotocol:limestone"),
                    lookup.apply("echoopenlandsprotocol:shale"),
                    lookup.apply("echoopenlandsprotocol:deepstone"),
                    lookup.apply("echoopenlandsprotocol:branchwood_log"),
                    lookup.apply("echoopenlandsprotocol:branchwood_planks"),
                    lookup.apply("echoopenlandsprotocol:old_road_block"),
                    lookup.apply("echoopenlandsprotocol:old_road_marker"),
                    lookup.apply("echoopenlandsprotocol:broken_waystone"),
                    lookup.apply("echoopenlandsprotocol:waystone_plinth")
            );
        }

        private static EchoVoxelBlock requireBlock(EchoVoxelBlock block, String name) {
            if (block == null || block.air()) {
                throw new IllegalArgumentException(name + " block must be a non-air voxel block");
            }
            return block;
        }
    }
}
