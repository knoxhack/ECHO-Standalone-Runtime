package dev.echo.standalone.runtime.world.chunk;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A vertical column of 16×16 chunk sections indexed by section Y.
 *
 * <p>The column lazily creates sections on first write and returns air/default biome for
 * coordinates in sections that have not been allocated.
 */
public final class EchoChunkColumn {

    public static final int SECTION_SIZE = EchoChunkSection.SECTION_SIZE;

    private final int chunkX;
    private final int chunkZ;
    private final EchoBlockStateContract defaultState;
    private final String defaultBiome;
    private final LinkedHashMap<Integer, EchoChunkSection> sections = new LinkedHashMap<>();
    private long version;

    public EchoChunkColumn(int chunkX, int chunkZ, EchoBlockStateContract defaultState, String defaultBiome) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.defaultState = Objects.requireNonNull(defaultState, "defaultState");
        this.defaultBiome = defaultBiome == null ? "" : defaultBiome;
    }

    public int chunkX() {
        return chunkX;
    }

    public int chunkZ() {
        return chunkZ;
    }

    public long version() {
        return version;
    }

    public EchoBlockStateContract stateAt(int x, int y, int z) {
        int sectionY = sectionY(y);
        EchoChunkSection section = sections.get(sectionY);
        if (section == null) {
            return defaultState;
        }
        return section.stateAt(localCoord(x), localCoord(y), localCoord(z));
    }

    public void setState(int x, int y, int z, EchoBlockStateContract state) {
        int sectionY = sectionY(y);
        EchoChunkSection section = sections.computeIfAbsent(
                sectionY,
                sy -> new EchoChunkSection(sy, defaultState, defaultBiome)
        );
        section.setState(localCoord(x), localCoord(y), localCoord(z), state);
        if (section.version() > version) {
            version = section.version();
        }
    }

    public String biomeAt(int x, int y, int z) {
        int sectionY = sectionY(y);
        EchoChunkSection section = sections.get(sectionY);
        if (section == null) {
            return defaultBiome;
        }
        return section.biomeAt(localCoord(x), localCoord(y), localCoord(z));
    }

    public void setBiome(int x, int y, int z, String biomeId) {
        int sectionY = sectionY(y);
        EchoChunkSection section = sections.computeIfAbsent(
                sectionY,
                sy -> new EchoChunkSection(sy, defaultState, defaultBiome)
        );
        section.setBiome(localCoord(x), localCoord(y), localCoord(z), biomeId);
        if (section.version() > version) {
            version = section.version();
        }
    }

    public Optional<EchoChunkSection> section(int sectionY) {
        return Optional.ofNullable(sections.get(sectionY));
    }

    public Map<Integer, EchoChunkSection> sections() {
        return Map.copyOf(sections);
    }

    public boolean empty() {
        if (sections.isEmpty()) {
            return defaultState.air();
        }
        return sections.values().stream().allMatch(EchoChunkSection::empty);
    }

    public int sectionCount() {
        return sections.size();
    }

    private static int sectionY(int blockY) {
        return Math.floorDiv(blockY, SECTION_SIZE);
    }

    private static int localCoord(int blockCoord) {
        return Math.floorMod(blockCoord, SECTION_SIZE);
    }
}
