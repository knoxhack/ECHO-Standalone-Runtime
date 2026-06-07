package dev.echo.standalone.runtime.world;

/**
 * Generates chunks for a world. Implementations may be data-driven or procedural.
 */
public interface EchoChunkGenerator {

    /**
     * Generate a single chunk.
     *
     * @param seed    world seed
     * @param chunkX  chunk X coordinate
     * @param chunkY  chunk Y coordinate
     * @param chunkZ  chunk Z coordinate
     * @return generated chunk (must not be null)
     */
    EchoVoxelChunk generateChunk(long seed, int chunkX, int chunkY, int chunkZ);

    /**
     * The biome source used by this generator for biome resolution.
     */
    EchoVoxelBiomeSource biomeSource();

    /**
     * The spawn X coordinate for new players.
     */
    double spawnX();

    /**
     * The spawn Y coordinate for new players.
     */
    double spawnY();

    /**
     * The spawn Z coordinate for new players.
     */
    double spawnZ();

    /**
     * The spawn yaw (in degrees) for new players.
     */
    double spawnYawDegrees();
}
