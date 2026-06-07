package dev.echo.standalone.runtime.world;

import java.util.List;
import java.util.Locale;

public final class EchoVoxelBiomeSources {
    public static final String GENERIC_SOURCE_ID = "echo:generic_biomes";
    private static final EchoVoxelBiome GENERIC_BIOME = new EchoVoxelBiome(
            "echo:generic_surface",
            "Generic Surface",
            0.8D,
            0.1D,
            0x6A6A6A,
            0x5F7741,
            "minecraft:ash",
            List.of("generic", "surface")
    );
    private static final EchoVoxelBiomeSource GENERIC_SOURCE = new FixedBiomeSource(GENERIC_SOURCE_ID, GENERIC_BIOME);

    private EchoVoxelBiomeSources() {
    }

    public static EchoVoxelBiome defaultBiome() {
        return GENERIC_BIOME;
    }

    public static EchoVoxelBiomeSource generic() {
        return GENERIC_SOURCE;
    }

    public static EchoVoxelBiomeSource byId(String sourceId) {
        String normalized = normalize(sourceId);
        if (normalized.equals(EchoVoxelAshfallBiomes.SOURCE_ID)) {
            return EchoVoxelAshfallBiomes.source();
        }
        return GENERIC_SOURCE;
    }

    public static EchoVoxelBiomeSource byWorldId(String worldId) {
        String normalized = normalize(worldId);
        if (normalized.equals(EchoVoxelAshfallWorldGeneration.WORLD_ID)) {
            return EchoVoxelAshfallBiomes.source();
        }
        return GENERIC_SOURCE;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record FixedBiomeSource(String id, EchoVoxelBiome biome) implements EchoVoxelBiomeSource {
        private FixedBiomeSource {
            id = EchoWorldText.requireText(id, "id");
            if (biome == null) {
                throw new IllegalArgumentException("biome must not be null");
            }
        }

        @Override
        public EchoVoxelBiome biomeAt(long seed, int x, int z) {
            return biome;
        }
    }
}
