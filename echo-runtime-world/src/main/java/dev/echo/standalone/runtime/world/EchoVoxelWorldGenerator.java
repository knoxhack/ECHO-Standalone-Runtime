package dev.echo.standalone.runtime.world;

import java.util.ArrayList;

public final class EchoVoxelWorldGenerator {
    public EchoVoxelWorld generateRegion(EchoVoxelWorldGenerationProfile profile, long seed, int horizontalRadius) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null");
        }
        if (horizontalRadius < 0) {
            throw new IllegalArgumentException("horizontalRadius must not be negative");
        }
        ArrayList<EchoVoxelChunk> chunks = new ArrayList<>();
        for (int chunkZ = -horizontalRadius; chunkZ <= horizontalRadius; chunkZ++) {
            for (int chunkX = -horizontalRadius; chunkX <= horizontalRadius; chunkX++) {
                chunks.add(profile.generateChunk(seed, chunkX, 0, chunkZ));
            }
        }
        return new EchoVoxelWorld(
                profile.worldId(),
                seed,
                profile.chunkSize(),
                chunks,
                profile.spawnX(),
                profile.spawnY(),
                profile.spawnZ(),
                profile.spawnYawDegrees(),
                profile.biomeSource()
        );
    }

    public EchoVoxelWorld generateWorld(EchoVoxelWorldGenerationProfile profile, long seed) {
        return generateRegion(profile, seed, 0);
    }
}
