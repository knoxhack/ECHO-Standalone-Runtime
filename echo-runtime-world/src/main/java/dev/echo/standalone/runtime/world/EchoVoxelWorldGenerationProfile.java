package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoVoxelWorldGenerationProfile(
        String worldId,
        int chunkSize,
        double spawnX,
        double spawnY,
        double spawnZ,
        double spawnYawDegrees,
        EchoVoxelBiomeSource biomeSource,
        EchoVoxelChunkSource chunkSource
) {
    public EchoVoxelWorldGenerationProfile {
        worldId = EchoWorldText.requireText(worldId, "worldId");
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        biomeSource = biomeSource == null ? EchoVoxelBiomeSources.generic() : biomeSource;
        chunkSource = Objects.requireNonNull(chunkSource, "chunkSource");
    }

    EchoVoxelChunk generateChunk(long seed, int chunkX, int chunkY, int chunkZ) {
        EchoVoxelChunk chunk = chunkSource.generateChunk(seed, chunkX, chunkY, chunkZ);
        if (chunk.size() != chunkSize) {
            throw new IllegalArgumentException("generated chunk size must match profile chunk size");
        }
        return chunk;
    }

    public static EchoVoxelWorldGenerationProfile fromChunkGenerator(
            String worldId,
            int chunkSize,
            EchoChunkGenerator generator
    ) {
        Objects.requireNonNull(generator, "generator");
        return new EchoVoxelWorldGenerationProfile(
                worldId,
                chunkSize,
                generator.spawnX(),
                generator.spawnY(),
                generator.spawnZ(),
                generator.spawnYawDegrees(),
                generator.biomeSource(),
                generator::generateChunk
        );
    }
}
