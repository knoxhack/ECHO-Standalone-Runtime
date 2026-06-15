package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.chunk.EchoChunkColumn;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A Minecraft-style world holding {@link EchoChunkColumn} instances indexed by chunk coordinates.
 */
public final class EchoMinecraftWorld {

    private final String worldId;
    private final long seed;
    private final EchoBlockRegistry blockRegistry;
    private final LinkedHashMap<Long, EchoChunkColumn> columns = new LinkedHashMap<>();
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    public EchoMinecraftWorld(String worldId, long seed, EchoBlockRegistry blockRegistry) {
        this.worldId = Objects.requireNonNull(worldId, "worldId");
        if (worldId.isBlank()) {
            throw new IllegalArgumentException("worldId must not be blank");
        }
        this.seed = seed;
        this.blockRegistry = Objects.requireNonNull(blockRegistry, "blockRegistry");
        this.spawnX = 0;
        this.spawnY = 80;
        this.spawnZ = 0;
    }

    public String worldId() {
        return worldId;
    }

    public long seed() {
        return seed;
    }

    public EchoBlockRegistry blockRegistry() {
        return blockRegistry;
    }

    public EchoBlockStateContract stateAt(int x, int y, int z) {
        EchoChunkColumn column = columnAt(chunkX(x), chunkZ(z));
        return column.stateAt(x, y, z);
    }

    public void setState(int x, int y, int z, EchoBlockStateContract state) {
        EchoChunkColumn column = columnAt(chunkX(x), chunkZ(z));
        column.setState(x, y, z, state);
    }

    public EchoChunkColumn columnAt(int chunkX, int chunkZ) {
        long key = columnKey(chunkX, chunkZ);
        return columns.computeIfAbsent(key, k -> new EchoChunkColumn(chunkX, chunkZ, blockRegistry.air(), ""));
    }

    public Optional<EchoChunkColumn> existingColumn(int chunkX, int chunkZ) {
        return Optional.ofNullable(columns.get(columnKey(chunkX, chunkZ)));
    }

    public Map<Long, EchoChunkColumn> columns() {
        return Map.copyOf(columns);
    }

    public int loadedColumnCount() {
        return columns.size();
    }

    public int spawnX() {
        return spawnX;
    }

    public int spawnY() {
        return spawnY;
    }

    public int spawnZ() {
        return spawnZ;
    }

    public void setSpawn(int x, int y, int z) {
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;
    }

    private static long columnKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    private static int chunkX(int blockX) {
        return Math.floorDiv(blockX, EchoChunkColumn.SECTION_SIZE);
    }

    private static int chunkZ(int blockZ) {
        return Math.floorDiv(blockZ, EchoChunkColumn.SECTION_SIZE);
    }
}
