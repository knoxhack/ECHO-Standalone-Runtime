package dev.echo.standalone.runtime.world;

import java.util.Map;
import java.util.Objects;

/**
 * A chunk generator driven by noise, surface rules, and biome tags.
 * Replaces hardcoded procedural terrain with a configurable pipeline.
 */
public final class EchoNoiseChunkGenerator implements EchoChunkGenerator {
    private final int chunkSize;
    private final EchoVoxelBiomeSource biomeSource;
    private final EchoSurfaceRules surfaceRules;
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;
    private final double spawnYawDegrees;
    private final Map<String, EchoVoxelBlock> blockPalette;

    public EchoNoiseChunkGenerator(
            int chunkSize,
            EchoVoxelBiomeSource biomeSource,
            EchoSurfaceRules surfaceRules,
            double spawnX,
            double spawnY,
            double spawnZ,
            double spawnYawDegrees,
            Map<String, EchoVoxelBlock> blockPalette
    ) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        this.chunkSize = chunkSize;
        this.biomeSource = biomeSource == null ? EchoVoxelBiomeSources.generic() : biomeSource;
        this.surfaceRules = surfaceRules == null ? EchoSurfaceRules.empty() : surfaceRules;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnYawDegrees = spawnYawDegrees;
        this.blockPalette = blockPalette == null ? Map.of() : Map.copyOf(blockPalette);
    }

    @Override
    public EchoVoxelChunk generateChunk(long seed, int chunkX, int chunkY, int chunkZ) {
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(chunkX, chunkY, chunkZ), chunkSize);
        if (chunkY != 0) {
            return chunk;
        }
        EchoWorldgenContext context = new EchoWorldgenContext(
                seed, chunkX, chunkY, chunkZ, chunkSize, biomeSource,
                new EchoNoiseSampler(seed), surfaceRules
        );
        for (int z = 0; z < chunkSize; z++) {
            for (int x = 0; x < chunkSize; x++) {
                int worldX = context.worldX(x);
                int worldZ = context.worldZ(z);
                EchoVoxelBiome biome = context.biomeAt(worldX, worldZ);
                int height = terrainHeight(worldX, worldZ, seed, biome, context);
                for (int y = 0; y <= height && y < chunkSize; y++) {
                    int worldY = context.worldY(y);
                    double noise = context.noiseAt(worldX, worldY, worldZ);
                    EchoVoxelBlock fillBlock = resolveBlock(biome, height, y, worldX, worldY, worldZ, noise);
                    chunk.setStateLocal(x, y, z, EchoVoxelBlockState.of(fillBlock));
                }
            }
        }
        return chunk;
    }

    @Override
    public EchoVoxelBiomeSource biomeSource() {
        return biomeSource;
    }

    @Override
    public double spawnX() {
        return spawnX;
    }

    @Override
    public double spawnY() {
        return spawnY;
    }

    @Override
    public double spawnZ() {
        return spawnZ;
    }

    @Override
    public double spawnYawDegrees() {
        return spawnYawDegrees;
    }

    private int terrainHeight(int x, int z, long seed, EchoVoxelBiome biome, EchoWorldgenContext context) {
        double baseNoise = context.noiseOctaveAt(x, 0, z, 3, 0.5D, 2.0D);
        int height = 1 + (int) Math.round(baseNoise * 2.0D);
        if (biome.hasTag("toxic")) {
            height--;
        } else if (biome.hasTag("industrial") || biome.hasTag("city")) {
            height++;
        } else if (biome.hasTag("cold")) {
            height = Math.max(height, 2);
        }
        return Math.max(0, Math.min(4, height));
    }

    private EchoVoxelBlock resolveBlock(EchoVoxelBiome biome, int height, int y, int worldX, int worldY, int worldZ, double noise) {
        EchoVoxelBlock defaultBlock = blockPalette.getOrDefault("default", EchoVoxelBlock.AIR);
        if (y == height) {
            EchoVoxelBlock surface = surfaceRules.apply(worldX, 0, worldZ, noise, biome, defaultBlock);
            if (surface != defaultBlock) {
                return surface;
            }
            return selectSurfaceBlock(biome, worldX, worldZ, noise);
        }
        return selectSubSurfaceBlock(biome, height, y, noise);
    }

    private EchoVoxelBlock selectSurfaceBlock(EchoVoxelBiome biome, int worldX, int worldZ, double noise) {
        if (biome.hasTag("toxic") && noise > 0.3D) {
            return blockPalette.getOrDefault("toxic_puddle", blockPalette.getOrDefault("ash", EchoVoxelBlock.AIR));
        }
        if (noise > 0.35D && noise < 0.55D && !biome.hasTag("cold")) {
            return blockPalette.getOrDefault("grass", blockPalette.getOrDefault("ash", EchoVoxelBlock.AIR));
        }
        if (noise > 0.15D && noise < 0.35D) {
            return blockPalette.getOrDefault("rubble", blockPalette.getOrDefault("ash", EchoVoxelBlock.AIR));
        }
        return blockPalette.getOrDefault("ash", EchoVoxelBlock.AIR);
    }

    private EchoVoxelBlock selectSubSurfaceBlock(EchoVoxelBiome biome, int height, int y, double noise) {
        if (y == height - 1 && noise < 0.03D) {
            return blockPalette.getOrDefault("ore", blockPalette.getOrDefault("basalt", EchoVoxelBlock.AIR));
        }
        return blockPalette.getOrDefault("basalt", EchoVoxelBlock.AIR);
    }
}
