package dev.echo.standalone.runtime.world.gen;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.world.EchoNoiseSampler;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockStateParser;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;
import dev.echo.standalone.runtime.world.gen.biome.EchoBiomeSource;
import dev.echo.standalone.runtime.world.gen.biome.EchoSurfaceRule;

import java.util.Objects;

/**
 * Generates full-height Openlands terrain into {@link EchoChunkColumn} instances.
 *
 * <p>Uses 2D heightmap noise plus Openlands biome-specific surface rules. The default sea level
 * and vertical range match the Openlands standard world type.
 */
public final class EchoOpenlandsChunkGenerator {

    public static final int SEA_LEVEL = 63;
    public static final int MIN_Y = -64;
    public static final int MAX_Y = 320;

    private final long seed;
    private final EchoBlockRegistry blockRegistry;
    private final EchoBlockStateParser parser;
    private final EchoNoiseSampler noise;
    private final EchoBiomeSource biomeSource;
    private final EchoSurfaceRule surfaceRule;

    public EchoOpenlandsChunkGenerator(long seed, EchoBlockRegistry blockRegistry,
                                       EchoBiomeSource biomeSource, EchoSurfaceRule surfaceRule) {
        this.seed = seed;
        this.blockRegistry = Objects.requireNonNull(blockRegistry, "blockRegistry");
        this.parser = new EchoBlockStateParser(blockRegistry);
        this.noise = new EchoNoiseSampler(seed);
        this.biomeSource = Objects.requireNonNull(biomeSource, "biomeSource");
        this.surfaceRule = Objects.requireNonNull(surfaceRule, "surfaceRule");
    }

    public EchoChunkColumn generateColumn(int chunkX, int chunkZ) {
        EchoBlockStateContract air = blockRegistry.air();
        EchoBlockStateContract bedrock = parser.parse("echoworldstarter:bedrock");
        EchoChunkColumn column = new EchoChunkColumn(chunkX, chunkZ, air, "echoopenlandsprotocol:meadows");

        for (int z = 0; z < EchoChunkColumn.SECTION_SIZE; z++) {
            for (int x = 0; x < EchoChunkColumn.SECTION_SIZE; x++) {
                int worldX = chunkX * EchoChunkColumn.SECTION_SIZE + x;
                int worldZ = chunkZ * EchoChunkColumn.SECTION_SIZE + z;
                String biomeId = biomeSource.biomeAt(worldX, worldZ);
                int surfaceHeight = surfaceHeight(worldX, worldZ);

                for (int y = MIN_Y; y <= Math.min(surfaceHeight, MAX_Y); y++) {
                    EchoBlockStateContract state = surfaceRule.apply(
                            biomeId, surfaceHeight - y, y, surfaceHeight, y <= SEA_LEVEL
                    );
                    column.setState(worldX, y, worldZ, state);
                }
                // Water below sea level where there is air.
                for (int y = surfaceHeight + 1; y <= SEA_LEVEL && y <= MAX_Y; y++) {
                    column.setState(worldX, y, worldZ, water());
                }
                // Bedrock floor.
                column.setState(worldX, MIN_Y, worldZ, bedrock);
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

    private EchoBlockStateContract water() {
        return parser.parse("echoopenlandsprotocol:mud");
    }

    public long seed() {
        return seed;
    }
}
