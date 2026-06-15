package dev.echo.standalone.runtime.save.anvil;

import java.util.List;

/**
 * Raw blockstate and biome data decoded from one Anvil chunk section.
 *
 * <p>This DTO intentionally avoids runtime block types so that {@code echo-runtime-save} stays
 * independent of {@code echo-runtime-world}.
 */
public record EchoAnvilSectionData(
        int sectionY,
        List<String> blockStatePalette,
        long[] blockStateData,
        int bitsPerBlockState,
        List<String> biomePalette,
        long[] biomeData,
        int bitsPerBiome
) {

    public int volume() {
        return 16 * 16 * 16;
    }

    public int biomeVolume() {
        return 4 * 4 * 4;
    }

    public boolean hasBlockStates() {
        return blockStatePalette != null && !blockStatePalette.isEmpty() && blockStateData != null;
    }

    public boolean hasBiomes() {
        return biomePalette != null && !biomePalette.isEmpty() && biomeData != null;
    }
}
