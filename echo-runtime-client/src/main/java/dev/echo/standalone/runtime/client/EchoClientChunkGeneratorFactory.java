package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.data.EchoWorldgenBiomeRegistry;
import dev.echo.standalone.runtime.world.EchoChunkGenerator;
import dev.echo.standalone.runtime.world.EchoNoiseChunkGenerator;
import dev.echo.standalone.runtime.world.EchoSurfaceRules;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSource;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

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
                ? EchoClientAshfallBiomeSource.instance()
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
}
