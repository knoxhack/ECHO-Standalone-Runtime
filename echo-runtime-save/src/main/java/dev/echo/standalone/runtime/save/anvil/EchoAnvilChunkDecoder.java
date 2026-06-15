package dev.echo.standalone.runtime.save.anvil;

import dev.echo.standalone.runtime.nbt.EchoNbtCompound;
import dev.echo.standalone.runtime.nbt.EchoNbtList;
import dev.echo.standalone.runtime.nbt.EchoNbtTag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decodes an Anvil chunk NBT compound into raw section data.
 *
 * <p>Supports both the pre-1.18 format ({@code Palette}/{@code BlockStates}) and the 1.18+
 * format ({@code block_states.palette}/{@code block_states.data}, {@code biomes.palette}/
 * {@code biomes.data}).
 */
public final class EchoAnvilChunkDecoder {

    public static final int SECTION_SIZE = 16;
    public static final int SECTION_VOLUME = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
    public static final int BIOME_VOLUME = 4 * 4 * 4;

    public EchoAnvilChunkData decode(EchoNbtCompound root) {
        EchoNbtCompound chunk = root.contains("Level") ? root.getCompound("Level") : root;

        int chunkX = chunk.getInt("xPos");
        int chunkZ = chunk.getInt("zPos");

        Map<Integer, EchoAnvilSectionData> sections = new LinkedHashMap<>();
        EchoNbtList sectionList = chunk.getList("sections");
        for (EchoNbtTag tag : sectionList.elements()) {
            if (tag instanceof EchoNbtCompound sectionCompound) {
                EchoAnvilSectionData section = decodeSection(sectionCompound);
                sections.put(section.sectionY(), section);
            }
        }

        return new EchoAnvilChunkData(chunkX, chunkZ, sections, List.of(), List.of());
    }

    private EchoAnvilSectionData decodeSection(EchoNbtCompound section) {
        int sectionY = section.getByte("Y");

        List<String> blockPalette;
        long[] blockData;
        int bitsPerBlock;
        if (section.contains("block_states")) {
            EchoNbtCompound blockStates = section.getCompound("block_states");
            blockPalette = decodePalette(blockStates.getList("palette"));
            blockData = decodeLongArray(blockStates, "data");
        } else {
            blockPalette = decodePalette(section.getList("Palette"));
            blockData = decodeLongArray(section, "BlockStates");
        }
        bitsPerBlock = computeBits(blockPalette.size());

        List<String> biomePalette;
        long[] biomeData;
        int bitsPerBiome;
        if (section.contains("biomes")) {
            EchoNbtCompound biomes = section.getCompound("biomes");
            biomePalette = decodePalette(biomes.getList("palette"));
            biomeData = decodeLongArray(biomes, "data");
        } else {
            biomePalette = List.of();
            biomeData = new long[0];
        }
        bitsPerBiome = computeBits(biomePalette.size());

        return new EchoAnvilSectionData(
                sectionY,
                blockPalette,
                blockData,
                bitsPerBlock,
                biomePalette,
                biomeData,
                bitsPerBiome
        );
    }

    private List<String> decodePalette(EchoNbtList list) {
        List<String> palette = new ArrayList<>(list.size());
        for (EchoNbtTag tag : list.elements()) {
            if (tag instanceof dev.echo.standalone.runtime.nbt.EchoNbtString s) {
                palette.add(s.value());
            } else if (tag instanceof EchoNbtCompound compound) {
                // Some biome palettes may be compounds in newer formats.
                palette.add(compound.toString());
            } else {
                palette.add(tag.toString());
            }
        }
        return List.copyOf(palette);
    }

    private long[] decodeLongArray(EchoNbtCompound compound, String key) {
        dev.echo.standalone.runtime.nbt.EchoNbtLongArray array =
                compound.get(key, dev.echo.standalone.runtime.nbt.EchoNbtLongArray.class).orElse(null);
        if (array == null) {
            return new long[0];
        }
        return array.value();
    }

    private static int computeBits(int paletteSize) {
        if (paletteSize <= 1) {
            return 1;
        }
        return Math.max(4, 32 - Integer.numberOfLeadingZeros(paletteSize - 1));
    }
}
