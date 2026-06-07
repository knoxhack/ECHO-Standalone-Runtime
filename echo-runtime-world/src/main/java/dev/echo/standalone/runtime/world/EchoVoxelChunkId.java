package dev.echo.standalone.runtime.world;

public record EchoVoxelChunkId(int x, int y, int z) {
    public static EchoVoxelChunkId fromBlock(int blockX, int blockY, int blockZ, int chunkSize) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be positive");
        }
        return new EchoVoxelChunkId(
                Math.floorDiv(blockX, chunkSize),
                Math.floorDiv(blockY, chunkSize),
                Math.floorDiv(blockZ, chunkSize)
        );
    }
}
