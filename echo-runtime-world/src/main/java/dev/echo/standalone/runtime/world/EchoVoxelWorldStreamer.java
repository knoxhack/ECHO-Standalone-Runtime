package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class EchoVoxelWorldStreamer {
    private final EchoVoxelChunkSource chunkSource;
    private final EchoVoxelBiomeSource biomeSource;
    private final int horizontalRadius;

    public EchoVoxelWorldStreamer(
            EchoVoxelChunkSource chunkSource,
            int horizontalRadius
    ) {
        this(chunkSource, null, horizontalRadius);
    }

    public EchoVoxelWorldStreamer(
            EchoVoxelChunkSource chunkSource,
            EchoVoxelBiomeSource biomeSource,
            int horizontalRadius
    ) {
        this.chunkSource = Objects.requireNonNull(chunkSource, "chunkSource");
        this.biomeSource = biomeSource;
        if (horizontalRadius < 0) {
            throw new IllegalArgumentException("horizontalRadius must not be negative");
        }
        this.horizontalRadius = horizontalRadius;
    }

    public EchoVoxelWorld applyBiomeSource(EchoVoxelWorld world) {
        Objects.requireNonNull(world, "world");
        return biomeSource == null ? world : world.withBiomeSource(biomeSource);
    }

    public EchoVoxelWorld streamAround(EchoVoxelWorld world, double x, double z) {
        return streamAround(world, x, z, horizontalRadius);
    }

    public EchoVoxelWorld streamAround(EchoVoxelWorld world, double x, double z, int requestedHorizontalRadius) {
        world = applyBiomeSource(world);
        int radius = Math.max(0, requestedHorizontalRadius);
        int centerChunkX = Math.floorDiv((int) Math.floor(x), world.chunkSize());
        int centerChunkZ = Math.floorDiv((int) Math.floor(z), world.chunkSize());
        Set<EchoVoxelChunkId> loadedChunkIds = world.loadedChunkIds();
        ArrayList<EchoVoxelChunk> missing = new ArrayList<>();
        for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
            for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
                EchoVoxelChunkId chunkId = new EchoVoxelChunkId(chunkX, 0, chunkZ);
                if (!loadedChunkIds.contains(chunkId)) {
                    missing.add(chunkSource.generateChunk(world.seed(), chunkX, 0, chunkZ));
                }
            }
        }
        return world.withAdditionalChunks(missing);
    }

    public EchoVoxelChunk generateChunk(EchoVoxelWorld world, EchoVoxelChunkId chunkId) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(chunkId, "chunkId");
        return chunkSource.generateChunk(world.seed(), chunkId.x(), chunkId.y(), chunkId.z());
    }

    public List<EchoVoxelChunkId> requiredChunkIds(EchoVoxelWorld world, double x, double z) {
        return requiredChunkIds(world, x, z, horizontalRadius);
    }

    public List<EchoVoxelChunkId> requiredChunkIds(
            EchoVoxelWorld world,
            double x,
            double z,
            int requestedHorizontalRadius
    ) {
        Objects.requireNonNull(world, "world");
        int radius = Math.max(0, requestedHorizontalRadius);
        int centerChunkX = Math.floorDiv((int) Math.floor(x), world.chunkSize());
        int centerChunkZ = Math.floorDiv((int) Math.floor(z), world.chunkSize());
        ArrayList<EchoVoxelChunkId> result = new ArrayList<>();
        for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++) {
            for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++) {
                result.add(new EchoVoxelChunkId(chunkX, 0, chunkZ));
            }
        }
        return List.copyOf(result);
    }
}
