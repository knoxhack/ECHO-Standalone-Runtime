package dev.echo.standalone.runtime.world.chunk;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoChunkSectionContract;

import java.util.Arrays;
import java.util.Objects;

/**
 * A 16×16×16 chunk section storing blockstates and biomes using palette compression.
 *
 * <p>Blockstates are stored with a per-section palette and a bit-packed index array. Biomes are
 * stored at 4×4×4 resolution with a separate palette. The section tracks a version counter for
 * incremental meshing and save dirty tracking.
 */
public final class EchoChunkSection implements EchoChunkSectionContract {

    public static final int SECTION_SIZE = EchoChunkSectionContract.SECTION_SIZE;
    public static final int VOLUME = EchoChunkSectionContract.SECTION_VOLUME;
    public static final int BIOME_CELL_SIZE = 4;
    public static final int BIOME_CELLS_PER_AXIS = SECTION_SIZE / BIOME_CELL_SIZE;
    public static final int BIOME_VOLUME = BIOME_CELLS_PER_AXIS * BIOME_CELLS_PER_AXIS * BIOME_CELLS_PER_AXIS;

    private final int sectionY;
    private final EchoBlockStateContract defaultState;
    private final String defaultBiome;

    private EchoPalette<EchoBlockStateContract> blockPalette;
    private EchoBitStorage blockStorage;
    private EchoPalette<String> biomePalette;
    private EchoBitStorage biomeStorage;
    private long version;

    public EchoChunkSection(int sectionY, EchoBlockStateContract defaultState, String defaultBiome) {
        this.sectionY = sectionY;
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState");
        this.defaultBiome = defaultBiome == null ? "" : defaultBiome;
        this.blockPalette = EchoPalette.of(defaultState);
        this.blockStorage = new EchoBitStorage(1, VOLUME);
        this.biomePalette = EchoPalette.of(this.defaultBiome);
        this.biomeStorage = new EchoBitStorage(1, BIOME_VOLUME);
    }

    public int sectionY() {
        return sectionY;
    }

    public int minBlockY() {
        return sectionY * SECTION_SIZE;
    }

    public long version() {
        return version;
    }

    public EchoBlockStateContract stateAt(int localX, int localY, int localZ) {
        validateLocal(localX, localY, localZ);
        int index = blockIndex(localX, localY, localZ);
        return blockPalette.valueFor((int) blockStorage.get(index));
    }

    public void setState(int localX, int localY, int localZ, EchoBlockStateContract state) {
        validateLocal(localX, localY, localZ);
        Objects.requireNonNull(state, "state");
        int index = blockIndex(localX, localY, localZ);
        if (!blockPalette.contains(state)) {
            blockPalette = blockPalette.with(state);
            blockStorage = resizeStorage(blockStorage, blockPalette.minimumBits(), VOLUME);
        }
        int paletteId = blockPalette.idFor(state);
        if (paletteId != blockStorage.get(index)) {
            blockStorage.set(index, paletteId);
            version++;
        }
    }

    public String biomeAt(int localX, int localY, int localZ) {
        validateLocal(localX, localY, localZ);
        int index = biomeIndex(localX, localY, localZ);
        return biomePalette.valueFor((int) biomeStorage.get(index));
    }

    public void setBiome(int localX, int localY, int localZ, String biomeId) {
        validateLocal(localX, localY, localZ);
        String safeBiome = biomeId == null ? "" : biomeId;
        int index = biomeIndex(localX, localY, localZ);
        if (!biomePalette.contains(safeBiome)) {
            biomePalette = biomePalette.with(safeBiome);
            biomeStorage = resizeStorage(biomeStorage, biomePalette.minimumBits(), BIOME_VOLUME);
        }
        int paletteId = biomePalette.idFor(safeBiome);
        if (paletteId != biomeStorage.get(index)) {
            biomeStorage.set(index, paletteId);
            version++;
        }
    }

    public boolean empty() {
        if (blockPalette.size() != 1) {
            return false;
        }
        return blockPalette.valueFor(0).air();
    }

    public EchoPalette<EchoBlockStateContract> blockPalette() {
        return blockPalette;
    }

    public EchoBitStorage blockStorage() {
        return blockStorage;
    }

    public EchoPalette<String> biomePalette() {
        return biomePalette;
    }

    public EchoBitStorage biomeStorage() {
        return biomeStorage;
    }

    private static EchoBitStorage resizeStorage(EchoBitStorage oldStorage, int newBits, int size) {
        if (oldStorage.bitsPerEntry() == newBits) {
            return oldStorage;
        }
        EchoBitStorage newStorage = new EchoBitStorage(newBits, size);
        for (int i = 0; i < size; i++) {
            newStorage.set(i, oldStorage.get(i));
        }
        return newStorage;
    }

    private static int blockIndex(int localX, int localY, int localZ) {
        return (localY * SECTION_SIZE + localZ) * SECTION_SIZE + localX;
    }

    private static int biomeIndex(int localX, int localY, int localZ) {
        int bx = localX / BIOME_CELL_SIZE;
        int by = localY / BIOME_CELL_SIZE;
        int bz = localZ / BIOME_CELL_SIZE;
        return (by * BIOME_CELLS_PER_AXIS + bz) * BIOME_CELLS_PER_AXIS + bx;
    }

    private static void validateLocal(int localX, int localY, int localZ) {
        if (localX < 0 || localX >= SECTION_SIZE || localY < 0 || localY >= SECTION_SIZE || localZ < 0 || localZ >= SECTION_SIZE) {
            throw new IndexOutOfBoundsException(
                    "Local coordinates [" + localX + ", " + localY + ", " + localZ + "] outside section [0, 15]"
            );
        }
    }
}
