package dev.echo.standalone.runtime.world.chunk;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;
import dev.echo.standalone.runtime.save.anvil.EchoAnvilSectionData;
import dev.echo.standalone.runtime.world.block.state.EchoBlock;
import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;
import dev.echo.standalone.runtime.world.block.state.EchoBlockStateParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts between {@link EchoChunkSection} and raw Anvil section data.
 */
public final class EchoAnvilSectionAdapter {

    private final EchoBlockRegistry registry;
    private final EchoBlockStateParser parser;

    public EchoAnvilSectionAdapter(EchoBlockRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.parser = new EchoBlockStateParser(registry);
    }

    public EchoChunkSection adapt(EchoAnvilSectionData data) {
        EchoBlockStateContract defaultState = registry.air();
        String defaultBiome = data.biomePalette().isEmpty() ? "" : data.biomePalette().get(0);
        EchoChunkSection section = new EchoChunkSection(data.sectionY(), defaultState, defaultBiome);

        if (data.hasBlockStates()) {
            EchoBitStorage blockIndices = new EchoBitStorage(data.bitsPerBlockState(), data.volume(), data.blockStateData());
            for (int y = 0; y < EchoChunkSection.SECTION_SIZE; y++) {
                for (int z = 0; z < EchoChunkSection.SECTION_SIZE; z++) {
                    for (int x = 0; x < EchoChunkSection.SECTION_SIZE; x++) {
                        int index = (y * EchoChunkSection.SECTION_SIZE + z) * EchoChunkSection.SECTION_SIZE + x;
                        int paletteIndex = (int) blockIndices.get(index);
                        String blockStateString = data.blockStatePalette().get(paletteIndex);
                        EchoBlockStateContract state = parser.parse(blockStateString);
                        section.setState(x, y, z, state);
                    }
                }
            }
        }

        if (data.hasBiomes()) {
            EchoBitStorage biomeIndices = new EchoBitStorage(data.bitsPerBiome(), data.biomeVolume(), data.biomeData());
            for (int y = 0; y < EchoChunkSection.SECTION_SIZE; y++) {
                for (int z = 0; z < EchoChunkSection.SECTION_SIZE; z++) {
                    for (int x = 0; x < EchoChunkSection.SECTION_SIZE; x++) {
                        int biomeIndex = (y / EchoChunkSection.BIOME_CELL_SIZE * EchoChunkSection.BIOME_CELLS_PER_AXIS
                                + z / EchoChunkSection.BIOME_CELL_SIZE) * EchoChunkSection.BIOME_CELLS_PER_AXIS
                                + x / EchoChunkSection.BIOME_CELL_SIZE;
                        int paletteIndex = (int) biomeIndices.get(biomeIndex);
                        String biomeId = data.biomePalette().get(paletteIndex);
                        section.setBiome(x, y, z, biomeId);
                    }
                }
            }
        }

        return section;
    }

    public EchoAnvilSectionData encode(EchoChunkSection section) {
        // Build blockstate palette by scanning all positions.
        LinkedHashMap<EchoBlockStateContract, Integer> blockStateToIndex = new LinkedHashMap<>();
        List<String> blockPalette = new ArrayList<>();
        int[] blockIndices = new int[EchoChunkSection.VOLUME];

        int index = 0;
        for (int y = 0; y < EchoChunkSection.SECTION_SIZE; y++) {
            for (int z = 0; z < EchoChunkSection.SECTION_SIZE; z++) {
                for (int x = 0; x < EchoChunkSection.SECTION_SIZE; x++) {
                    EchoBlockStateContract state = section.stateAt(x, y, z);
                    Integer paletteIndex = blockStateToIndex.get(state);
                    if (paletteIndex == null) {
                        paletteIndex = blockPalette.size();
                        blockStateToIndex.put(state, paletteIndex);
                        blockPalette.add(stateToString(state));
                    }
                    blockIndices[index++] = paletteIndex;
                }
            }
        }

        int bitsPerBlock = computeBits(blockPalette.size());
        long[] blockData = packIndices(blockIndices, bitsPerBlock);

        // Build biome palette by scanning all biome cells.
        LinkedHashMap<String, Integer> biomeToIndex = new LinkedHashMap<>();
        List<String> biomePalette = new ArrayList<>();
        int[] biomeIndices = new int[EchoChunkSection.BIOME_VOLUME];

        int biomeIndex = 0;
        for (int by = 0; by < EchoChunkSection.BIOME_CELLS_PER_AXIS; by++) {
            for (int bz = 0; bz < EchoChunkSection.BIOME_CELLS_PER_AXIS; bz++) {
                for (int bx = 0; bx < EchoChunkSection.BIOME_CELLS_PER_AXIS; bx++) {
                    int x = bx * EchoChunkSection.BIOME_CELL_SIZE;
                    int y = by * EchoChunkSection.BIOME_CELL_SIZE;
                    int z = bz * EchoChunkSection.BIOME_CELL_SIZE;
                    String biomeId = section.biomeAt(x, y, z);
                    Integer paletteIndex = biomeToIndex.get(biomeId);
                    if (paletteIndex == null) {
                        paletteIndex = biomePalette.size();
                        biomeToIndex.put(biomeId, paletteIndex);
                        biomePalette.add(biomeId);
                    }
                    biomeIndices[biomeIndex++] = paletteIndex;
                }
            }
        }

        int bitsPerBiome = computeBits(biomePalette.size());
        long[] biomeData = packIndices(biomeIndices, bitsPerBiome);

        return new EchoAnvilSectionData(
                section.sectionY(),
                List.copyOf(blockPalette),
                blockData,
                bitsPerBlock,
                List.copyOf(biomePalette),
                biomeData,
                bitsPerBiome
        );
    }

    private String stateToString(EchoBlockStateContract state) {
        EchoBlock block = (EchoBlock) state.block();
        StringBuilder sb = new StringBuilder();
        sb.append(block.id());
        if (!block.properties().isEmpty()) {
            sb.append('[');
            boolean first = true;
            for (var property : block.properties()) {
                if (!first) {
                    sb.append(',');
                }
                sb.append(property.name()).append('=').append(serializedValue(state, property));
                first = false;
            }
            sb.append(']');
        }
        return sb.toString();
    }

    private <T> String serializedValue(EchoBlockStateContract state, dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract<T> property) {
        return property.serialize(state.value(property));
    }

    private static long[] packIndices(int[] indices, int bitsPerEntry) {
        EchoBitStorage storage = new EchoBitStorage(bitsPerEntry, indices.length);
        for (int i = 0; i < indices.length; i++) {
            storage.set(i, indices[i]);
        }
        return storage.rawData();
    }

    private static int computeBits(int paletteSize) {
        if (paletteSize <= 1) {
            return 1;
        }
        return Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
    }

    @SuppressWarnings("unchecked")
    private static <T> T unchecked(Object value) {
        return (T) value;
    }
}
