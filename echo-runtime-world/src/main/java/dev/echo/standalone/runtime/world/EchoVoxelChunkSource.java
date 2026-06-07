package dev.echo.standalone.runtime.world;

@FunctionalInterface
public interface EchoVoxelChunkSource {
    EchoVoxelChunk generateChunk(long seed, int chunkX, int chunkY, int chunkZ);
}
