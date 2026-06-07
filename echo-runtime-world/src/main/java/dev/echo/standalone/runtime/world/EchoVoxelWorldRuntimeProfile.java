package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoVoxelWorldRuntimeProfile(
        EchoVoxelWorldGenerationProfile generationProfile,
        int streamRadius
) {
    public EchoVoxelWorldRuntimeProfile {
        generationProfile = Objects.requireNonNull(generationProfile, "generationProfile");
        if (streamRadius < 0) {
            throw new IllegalArgumentException("streamRadius must not be negative");
        }
    }

    public EchoVoxelWorld generate(long seed) {
        return generate(seed, streamRadius);
    }

    public EchoVoxelWorld generate(long seed, int horizontalRadius) {
        if (horizontalRadius < 0) {
            throw new IllegalArgumentException("horizontalRadius must not be negative");
        }
        return new EchoVoxelWorldGenerator().generateRegion(generationProfile, seed, horizontalRadius);
    }

    public EchoVoxelWorld generateAndStream(long seed) {
        EchoVoxelWorld world = generate(seed);
        return streamer().streamAround(world, world.spawnX(), world.spawnZ());
    }

    public EchoVoxelWorldStreamer streamer() {
        return new EchoVoxelWorldStreamer(
                generationProfile.chunkSource(),
                generationProfile.biomeSource(),
                streamRadius
        );
    }
}
