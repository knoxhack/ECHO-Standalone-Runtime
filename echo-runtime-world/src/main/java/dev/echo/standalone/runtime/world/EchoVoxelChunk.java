package dev.echo.standalone.runtime.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoVoxelChunk {
    private final EchoVoxelChunkId id;
    private final int size;
    private final EchoVoxelBlockState[] states;
    private final String[] biomeIds;
    private long version;

    public EchoVoxelChunk(EchoVoxelChunkId id, int size) {
        this.id = Objects.requireNonNull(id, "id");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        this.size = size;
        this.states = new EchoVoxelBlockState[size * size * size];
        this.biomeIds = new String[states.length];
        for (int index = 0; index < states.length; index++) {
            states[index] = EchoVoxelBlockState.AIR;
            biomeIds[index] = "";
        }
    }

    public EchoVoxelChunkId id() {
        return id;
    }

    public int size() {
        return size;
    }

    public long version() {
        return version;
    }

    public EchoVoxelBlock blockAtLocal(int x, int y, int z) {
        return stateAtLocal(x, y, z).block();
    }

    public EchoVoxelBlockState stateAtLocal(int x, int y, int z) {
        if (!inside(x, y, z)) {
            return EchoVoxelBlockState.AIR;
        }
        return states[index(x, y, z)];
    }

    public Optional<String> biomeIdAtLocal(int x, int y, int z) {
        if (!inside(x, y, z)) {
            return Optional.empty();
        }
        String biomeId = biomeIds[index(x, y, z)];
        return biomeId == null || biomeId.isBlank() ? Optional.empty() : Optional.of(biomeId);
    }

    public void setBlockLocal(int x, int y, int z, EchoVoxelBlock block) {
        setStateLocal(x, y, z, EchoVoxelBlockState.of(block));
    }

    public void setStateLocal(int x, int y, int z, EchoVoxelBlockState state) {
        if (!inside(x, y, z)) {
            throw new IllegalArgumentException("local block coordinate is outside chunk");
        }
        int stateIndex = index(x, y, z);
        EchoVoxelBlockState safeState = Objects.requireNonNull(state, "state");
        if (!safeState.equals(states[stateIndex])) {
            version++;
        }
        states[stateIndex] = safeState;
        biomeIds[stateIndex] = biomeId(safeState);
    }

    public List<EchoVoxelBlockInstance> nonAirBlocks() {
        ArrayList<EchoVoxelBlockInstance> result = new ArrayList<>();
        int baseX = id.x() * size;
        int baseY = id.y() * size;
        int baseZ = id.z() * size;
        for (int y = 0; y < size; y++) {
            for (int z = 0; z < size; z++) {
                for (int x = 0; x < size; x++) {
                    EchoVoxelBlock block = blockAtLocal(x, y, z);
                    if (!block.air()) {
                        result.add(new EchoVoxelBlockInstance(
                                baseX + x,
                                baseY + y,
                                baseZ + z,
                                block,
                                stateAtLocal(x, y, z)
                        ));
                    }
                }
            }
        }
        return result;
    }

    TickSummary tickLoadedBlocks(long gameTick) {
        if (gameTick < 0L) {
            throw new IllegalArgumentException("gameTick must not be negative");
        }
        int tickedBlocks = 0;
        int hazardBlocks = 0;
        int metadataWrites = 0;
        for (int y = 0; y < size; y++) {
            for (int z = 0; z < size; z++) {
                for (int x = 0; x < size; x++) {
                    int stateIndex = index(x, y, z);
                    EchoVoxelBlockState state = states[stateIndex];
                    if (state.air()) {
                        continue;
                    }
                    tickedBlocks++;
                    EchoVoxelBlockState next = state.ticked()
                            .withProperty("lastTick", Long.toString(gameTick));
                    metadataWrites++;
                    if (isHazard(state.block())) {
                        hazardBlocks++;
                        next = next.withProperty("hazardActive", "true");
                        metadataWrites++;
                    }
                    if (!next.equals(states[stateIndex])) {
                        version++;
                    }
                    states[stateIndex] = next;
                    biomeIds[stateIndex] = biomeId(next);
                }
            }
        }
        return new TickSummary(tickedBlocks, hazardBlocks, metadataWrites);
    }

    private static boolean isHazard(EchoVoxelBlock block) {
        String id = block.id();
        return id.contains("toxic_ash")
                || id.contains("fallout_dust")
                || id.contains("ash_hazard")
                || id.contains("toxic_waste_barrel")
                || id.contains("toxic_puddle");
    }

    private static String biomeId(EchoVoxelBlockState state) {
        return state.property("biome").orElse("");
    }

    private boolean inside(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < size && y < size && z < size;
    }

    private int index(int x, int y, int z) {
        return (y * size + z) * size + x;
    }

    record TickSummary(int tickedBlocks, int hazardBlocks, int metadataWrites) {
    }
}
