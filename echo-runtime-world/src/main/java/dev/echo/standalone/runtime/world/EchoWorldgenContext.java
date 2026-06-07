package dev.echo.standalone.runtime.world;

import java.util.Objects;

/**
 * Immutable context passed to world generation stages.
 * Holds seed, chunk position, biome source, noise sampler, and surface rules.
 */
public final class EchoWorldgenContext {
    private final long seed;
    private final int chunkX;
    private final int chunkY;
    private final int chunkZ;
    private final int chunkSize;
    private final EchoVoxelBiomeSource biomeSource;
    private final EchoNoiseSampler noiseSampler;
    private final EchoSurfaceRules surfaceRules;

    public EchoWorldgenContext(
            long seed,
            int chunkX,
            int chunkY,
            int chunkZ,
            int chunkSize,
            EchoVoxelBiomeSource biomeSource,
            EchoNoiseSampler noiseSampler,
            EchoSurfaceRules surfaceRules
    ) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        this.seed = seed;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.chunkSize = chunkSize;
        this.biomeSource = biomeSource == null ? EchoVoxelBiomeSources.generic() : biomeSource;
        this.noiseSampler = noiseSampler == null ? new EchoNoiseSampler(seed) : noiseSampler;
        this.surfaceRules = surfaceRules == null ? EchoSurfaceRules.empty() : surfaceRules;
    }

    public long seed() {
        return seed;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkY() {
        return chunkY;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public int chunkSize() {
        return chunkSize;
    }

    public EchoVoxelBiomeSource biomeSource() {
        return biomeSource;
    }

    public EchoNoiseSampler noiseSampler() {
        return noiseSampler;
    }

    public EchoSurfaceRules surfaceRules() {
        return surfaceRules;
    }

    public int worldX(int localX) {
        return chunkX * chunkSize + localX;
    }

    public int worldY(int localY) {
        return chunkY * chunkSize + localY;
    }

    public int worldZ(int localZ) {
        return chunkZ * chunkSize + localZ;
    }

    public EchoVoxelBiome biomeAt(int worldX, int worldZ) {
        return biomeSource.biomeAt(seed, worldX, worldZ);
    }

    public double noiseAt(int worldX, int worldY, int worldZ) {
        return noiseSampler.sample(worldX * 0.05D, worldY * 0.05D, worldZ * 0.05D);
    }

    public double noiseOctaveAt(int worldX, int worldY, int worldZ, int octaves, double persistence, double lacunarity) {
        return noiseSampler.sampleOctave(worldX * 0.05D, worldY * 0.05D, worldZ * 0.05D, octaves, persistence, lacunarity);
    }

    public EchoWorldgenContext withChunk(int chunkX, int chunkY, int chunkZ) {
        return new EchoWorldgenContext(seed, chunkX, chunkY, chunkZ, chunkSize, biomeSource, noiseSampler, surfaceRules);
    }
}
