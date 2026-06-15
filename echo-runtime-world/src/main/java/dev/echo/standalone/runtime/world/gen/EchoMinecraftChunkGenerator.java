package dev.echo.standalone.runtime.world.gen;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.world.EchoNoiseSampler;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockStateParser;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;

import java.util.Objects;

/**
 * Generates full-height Minecraft-style terrain into {@link EchoChunkColumn} instances.
 *
 * <p>This generator uses 2D heightmap noise plus simple surface rules. It fills columns from
 * the bottom of the world up to the computed surface height with stone, dirt, and grass.
 */
public final class EchoMinecraftChunkGenerator {

    public static final int SEA_LEVEL = 63;
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    private final long seed;
    private final EchoBlockRegistry blockRegistry;
    private final EchoBlockStateParser parser;
    private final EchoNoiseSampler noise;

    public EchoMinecraftChunkGenerator(long seed, EchoBlockRegistry blockRegistry) {
        this.seed = seed;
        this.blockRegistry = Objects.requireNonNull(blockRegistry, "blockRegistry");
        this.parser = new EchoBlockStateParser(blockRegistry);
        this.noise = new EchoNoiseSampler(seed);
    }

    public EchoChunkColumn generateColumn(int chunkX, int chunkZ) {
        EchoBlockStateContract air = blockRegistry.air();
        EchoChunkColumn column = new EchoChunkColumn(chunkX, chunkZ, air, "");

        for (int z = 0; z < EchoChunkColumn.SECTION_SIZE; z++) {
            for (int x = 0; x < EchoChunkColumn.SECTION_SIZE; x++) {
                int worldX = chunkX * EchoChunkColumn.SECTION_SIZE + x;
                int worldZ = chunkZ * EchoChunkColumn.SECTION_SIZE + z;
                int surfaceHeight = surfaceHeight(worldX, worldZ);

                for (int y = MIN_Y; y <= Math.min(surfaceHeight, MAX_Y); y++) {
                    EchoBlockStateContract state = blockAtDepth(surfaceHeight - y, worldX, y, worldZ);
                    column.setState(worldX, y, worldZ, state);
                }
                // Water below sea level where there is air.
                for (int y = surfaceHeight + 1; y <= SEA_LEVEL && y <= MAX_Y; y++) {
                    column.setState(worldX, y, worldZ, parser.parse("minecraft:water[level=0]"));
                }
            }
        }
        return column;
    }

    private int surfaceHeight(int worldX, int worldZ) {
        double continentNoise = noise.sampleOctave(worldX * 0.0015D, 0.0D, worldZ * 0.0015D, 3, 0.5D, 2.0D);
        double detailNoise = noise.sampleOctave(worldX * 0.01D, 0.0D, worldZ * 0.01D, 2, 0.5D, 2.0D);
        double height = SEA_LEVEL + continentNoise * 32.0D + detailNoise * 4.0D;
        return (int) Math.round(height);
    }

    private EchoBlockStateContract blockAtDepth(int depthBelowSurface, int worldX, int worldY, int worldZ) {
        if (depthBelowSurface == 0) {
            if (worldY >= SEA_LEVEL - 2) {
                return parser.parse("minecraft:grass_block[snowy=false]");
            }
            return parser.parse("minecraft:sand");
        }
        if (depthBelowSurface <= 3) {
            return parser.parse("minecraft:dirt");
        }
        if (depthBelowSurface <= 5 && noise.sample(worldX * 0.1D, worldY * 0.1D, worldZ * 0.1D) > 0.6D) {
            return parser.parse("minecraft:gravel");
        }
        return parser.parse("minecraft:stone");
    }

    public long seed() {
        return seed;
    }
}
