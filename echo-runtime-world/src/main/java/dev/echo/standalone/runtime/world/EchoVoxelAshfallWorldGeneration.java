package dev.echo.standalone.runtime.world;

import java.util.Objects;
import java.util.function.Function;

public final class EchoVoxelAshfallWorldGeneration {
    public static final String WORLD_ID = "echoashfallprotocol:standalone_crash_site";
    private static final int CHUNK_SIZE = 16;
    private static final double SPAWN_X = 7.5D;
    private static final double SPAWN_Y = 5.8D;
    private static final double SPAWN_Z = -4.5D;
    private static final double SPAWN_YAW_DEGREES = 35.0D;

    private EchoVoxelAshfallWorldGeneration() {
    }

    public static EchoVoxelWorldGenerationProfile crashSiteProfile(Blocks blocks) {
        Blocks safeBlocks = Objects.requireNonNull(blocks, "blocks");
        return new EchoVoxelWorldGenerationProfile(
                WORLD_ID,
                CHUNK_SIZE,
                SPAWN_X,
                SPAWN_Y,
                SPAWN_Z,
                SPAWN_YAW_DEGREES,
                EchoVoxelAshfallBiomes.source(),
                (seed, chunkX, chunkY, chunkZ) -> generateChunk(seed, safeBlocks, chunkX, chunkY, chunkZ)
        );
    }

    public static EchoVoxelWorld generateCrashSiteRegion(long seed, Blocks blocks, int horizontalRadius) {
        return new EchoVoxelWorldGenerator().generateRegion(crashSiteProfile(blocks), seed, horizontalRadius);
    }

    public static EchoVoxelWorld generateCrashSite(long seed, Blocks blocks) {
        return generateCrashSiteRegion(seed, blocks, 0);
    }

    public static EchoVoxelWorldStreamer streamer(Blocks blocks, int horizontalRadius) {
        return new EchoVoxelWorldStreamer(crashSiteProfile(blocks).chunkSource(), horizontalRadius);
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
                EchoVoxelBiome biome = EchoVoxelAshfallBiomes.biomeAt(seed, worldX, worldZ);
                int height = terrainHeight(worldX, worldZ, seed, biome);
                double dc = debrisChance(seed, worldX, worldZ);
                boolean toxic = biome.hasTag("toxic") || biome.hasTag("radiation");
                boolean industrial = biome.hasTag("industrial") || biome.hasTag("city");
                boolean cold = biome.hasTag("cold");
                boolean oreVein = (dc < 0.03D || biome.hasTag("radiation") && dc < 0.08D) && height >= 2;
                boolean puddle = toxic
                        ? dc >= 0.45D && dc < 0.62D && height <= 2
                        : dc >= 0.55D && dc < 0.62D && height <= 1;
                boolean grass = dc >= 0.35D && dc < (toxic ? 0.46D : 0.40D) && height >= 2;
                boolean rubble = dc >= 0.15D && dc < (industrial ? 0.29D : 0.20D) && height >= 1;
                boolean twisted = (dc >= 0.90D && dc < 0.93D || industrial && dc >= 0.72D && dc < 0.78D)
                        && height >= 1;
                boolean berry = !cold && dc >= 0.80D && dc < 0.82D && height >= 2;

                for (int y = 0; y <= height; y++) {
                    EchoVoxelBlock fillBlock;
                    if (y == height) {
                        if (puddle) {
                            fillBlock = blocks.puddle();
                        } else if (grass) {
                            fillBlock = blocks.grass();
                        } else if (berry) {
                            fillBlock = blocks.berryBush();
                        } else if (rubble) {
                            fillBlock = blocks.rubble();
                        } else {
                            fillBlock = blocks.ash();
                        }
                    } else if (oreVein && y == height - 1) {
                        fillBlock = blocks.ore();
                    } else if (twisted && y == height - 1) {
                        fillBlock = blocks.twistedMetal();
                    } else {
                        fillBlock = blocks.basalt();
                    }
                    setGeneratedBlockLocal(chunk, x, y, z, fillBlock, "terrain", biome);
                }
                if (dc < 0.07D && height + 1 < CHUNK_SIZE && !puddle && !berry) {
                    setGeneratedBlockLocal(chunk, x, height + 1, z, blocks.rust(), "debris", biome);
                }
            }
        }
        if (chunkX == 0 && chunkZ == 0) {
            addCrashSiteStructures(chunk, blocks);
        }
        return chunk;
    }

    private static int terrainHeight(int x, int z, long seed, EchoVoxelBiome biome) {
        int ridge = (int) Math.round(Math.sin((x + seed % 7) * 0.45D) + Math.cos((z - seed % 5) * 0.38D));
        int height = 1 + ridge;
        if (biome.hasTag("toxic")) {
            height--;
        } else if (biome.hasTag("industrial") || biome.hasTag("city")) {
            height++;
        } else if (biome.hasTag("cold")) {
            height = Math.max(height, 2);
        }
        return Math.max(0, Math.min(4, height));
    }

    private static void addCrashSiteStructures(EchoVoxelChunk chunk, Blocks blocks) {
        platform(chunk, 2, 2, 4, 4, blocks.rust());
        platform(chunk, 10, 2, 3, 3, blocks.hazard());
        pillar(chunk, 3, 3, 2, blocks.terminal());
        pillar(chunk, 6, 5, 2, blocks.cache());
        pillar(chunk, 11, 4, 3, blocks.power());
        wall(chunk, 7, 7, 5, blocks.rust());
    }

    private static void platform(EchoVoxelChunk chunk, int startX, int startZ, int width, int depth, EchoVoxelBlock block) {
        EchoVoxelBiome biome = EchoVoxelAshfallBiomes.CRASH_ZONE_WASTELAND;
        for (int z = startZ; z < startZ + depth; z++) {
            for (int x = startX; x < startX + width; x++) {
                setGeneratedBlockLocal(chunk, x, 4, z, block, "structure", biome);
            }
        }
    }

    private static void pillar(EchoVoxelChunk chunk, int x, int z, int height, EchoVoxelBlock block) {
        EchoVoxelBiome biome = EchoVoxelAshfallBiomes.CRASH_ZONE_WASTELAND;
        for (int y = 4; y < 4 + height; y++) {
            setGeneratedBlockLocal(chunk, x, y, z, block, "structure", biome);
        }
    }

    private static void wall(EchoVoxelChunk chunk, int startX, int z, int length, EchoVoxelBlock block) {
        EchoVoxelBiome biome = EchoVoxelAshfallBiomes.CRASH_ZONE_WASTELAND;
        for (int x = startX; x < startX + length; x++) {
            setGeneratedBlockLocal(chunk, x, 4, z, block, "structure", biome);
            setGeneratedBlockLocal(chunk, x, 5, z, block, "structure", biome);
        }
    }

    private static void setGeneratedBlockLocal(
            EchoVoxelChunk chunk,
            int x,
            int y,
            int z,
            EchoVoxelBlock block,
            String source,
            EchoVoxelBiome biome
    ) {
        chunk.setStateLocal(x, y, z, EchoVoxelBlockState.of(block)
                .withProperty("source", source)
                .withProperty("biome", biome.id()));
    }

    private static double debrisChance(long seed, int x, int z) {
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
            EchoVoxelBlock ash,
            EchoVoxelBlock basalt,
            EchoVoxelBlock rust,
            EchoVoxelBlock cache,
            EchoVoxelBlock terminal,
            EchoVoxelBlock hazard,
            EchoVoxelBlock power,
            EchoVoxelBlock ore,
            EchoVoxelBlock puddle,
            EchoVoxelBlock grass,
            EchoVoxelBlock rubble,
            EchoVoxelBlock twistedMetal,
            EchoVoxelBlock berryBush
    ) {
        public Blocks {
            ash = requireBlock(ash, "ash");
            basalt = requireBlock(basalt, "basalt");
            rust = requireBlock(rust, "rust");
            cache = requireBlock(cache, "cache");
            terminal = requireBlock(terminal, "terminal");
            hazard = requireBlock(hazard, "hazard");
            power = requireBlock(power, "power");
            ore = requireBlock(ore, "ore");
            puddle = requireBlock(puddle, "puddle");
            grass = requireBlock(grass, "grass");
            rubble = requireBlock(rubble, "rubble");
            twistedMetal = requireBlock(twistedMetal, "twistedMetal");
            berryBush = requireBlock(berryBush, "berryBush");
        }

        public static Blocks defaults() {
            return new Blocks(
                    block("echoashfallprotocol:fallout_dust", "Toxic Ash", 0xFF786A55, 0.6D),
                    block("echoashfallprotocol:scorched_ash", "Scorched Basalt", 0xFF3A3D37, 1.8D),
                    block("echoashfallprotocol:rusted_metal_debris", "Rusted Debris", 0xFF8E5F2B, 1.2D),
                    block("echoashfallprotocol:echo_cache", "Crash Cache", 0xFF2FB9D9, 1.0D),
                    block("echoterminal:echo_terminal", "Field Terminal", 0xFF45D4B4, 1.0D),
                    block("echoashfallprotocol:toxic_waste_barrel", "Ash Hazard", 0xFFB83E3E, 0.3D),
                    block("echoashfallprotocol:power_node", "Damaged Power Node", 0xFFE8B44D, 1.4D),
                    block("echoashfallprotocol:scrap_ore", "Scrap Ore", 0xFF6B5A3A, 2.5D),
                    block("echoashfallprotocol:toxic_puddle", "Toxic Puddle", 0xFF3D5C3A, 0.1D),
                    block("echoashfallprotocol:wasteland_grass", "Wasteland Grass", 0xFF6B7A3A, 0.4D),
                    block("echoashfallprotocol:rubble", "Rubble", 0xFF5A5A5A, 1.0D),
                    block("echoashfallprotocol:twisted_metal", "Twisted Metal", 0xFF7A6A4A, 1.5D),
                    block("echoashfallprotocol:wild_berry_bush", "Wild Berry Bush", 0xFF4A6A2A, 0.3D)
            );
        }

        public static Blocks fromRegistry(Function<String, EchoVoxelBlock> lookup) {
            return new Blocks(
                    lookup.apply("echoashfallprotocol:fallout_dust"),
                    lookup.apply("echoashfallprotocol:scorched_ash"),
                    lookup.apply("echoashfallprotocol:rusted_metal_debris"),
                    lookup.apply("echoashfallprotocol:echo_cache"),
                    lookup.apply("echoterminal:echo_terminal"),
                    lookup.apply("echoashfallprotocol:toxic_waste_barrel"),
                    lookup.apply("echoashfallprotocol:power_node"),
                    lookup.apply("echoashfallprotocol:scrap_ore"),
                    lookup.apply("echoashfallprotocol:toxic_puddle"),
                    lookup.apply("echoashfallprotocol:wasteland_grass"),
                    lookup.apply("echoashfallprotocol:rubble"),
                    lookup.apply("echoashfallprotocol:twisted_metal"),
                    lookup.apply("echoashfallprotocol:wild_berry_bush")
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
