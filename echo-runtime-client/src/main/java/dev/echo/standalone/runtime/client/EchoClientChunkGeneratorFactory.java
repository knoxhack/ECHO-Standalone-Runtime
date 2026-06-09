package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.data.EchoWorldgenBiomeRegistry;
import dev.echo.standalone.runtime.world.EchoVoxelAshfallBiomes;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoChunkGenerator;
import dev.echo.standalone.runtime.world.EchoNoiseChunkGenerator;
import dev.echo.standalone.runtime.world.EchoSurfaceRules;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSource;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds chunk generators from runtime data registries or fallback profiles.
 */
final class EchoClientChunkGeneratorFactory {

    private EchoClientChunkGeneratorFactory() {
    }

    /**
     * Creates a noise-driven chunk generator using the given biome registry and block palette.
     * Falls back to an Ashfall-compatible generator if no biomes are registered.
     */
    static EchoChunkGenerator fromDataOrFallback(
            EchoWorldgenBiomeRegistry biomeRegistry,
            Map<String, EchoVoxelBlock> blockPalette,
            int chunkSize,
            double spawnX,
            double spawnY,
            double spawnZ,
            double spawnYawDegrees
    ) {
        Objects.requireNonNull(blockPalette, "blockPalette");
        EchoVoxelBiomeSource biomeSource = biomeRegistry == null || biomeRegistry.biomes().isEmpty()
                ? EchoVoxelAshfallBiomes.source()
                : new EchoDataBiomeSource(biomeRegistry);
        return new EchoNoiseChunkGenerator(
                chunkSize,
                biomeSource,
                EchoSurfaceRules.empty(),
                spawnX,
                spawnY,
                spawnZ,
                spawnYawDegrees,
                blockPalette
        );
    }

    private static final class EchoDataBiomeSource implements EchoVoxelBiomeSource {
        private final List<EchoVoxelBiome> biomes;

        private EchoDataBiomeSource(EchoWorldgenBiomeRegistry registry) {
            biomes = registry.biomes().stream()
                    .map(definition -> new EchoVoxelBiome(
                            definition.id(),
                            definition.displayName(),
                            definition.temperature(),
                            definition.downfall(),
                            definition.fogColor(),
                            definition.grassColor(),
                            definition.ambientParticle(),
                            definition.tags()
                    ))
                    .toList();
        }

        @Override
        public String id() {
            return "echo:data_biomes";
        }

        @Override
        public EchoVoxelBiome biomeAt(long seed, int x, int z) {
            long mixed = seed;
            mixed ^= (long) x * 0x9E3779B97F4A7C15L;
            mixed ^= (long) z * 0xC2B2AE3D27D4EB4FL;
            mixed ^= mixed >>> 33;
            int index = Math.floorMod(Long.hashCode(mixed), biomes.size());
            return biomes.get(index);
        }
    }
}
