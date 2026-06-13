package dev.echo.standalone.runtime.world;

import java.util.List;

public final class EchoVoxelOpenlandsBiomes {
    public static final String SOURCE_ID = "echoopenlandsprotocol:openlands_biomes";
    public static final EchoVoxelBiome MEADOWS = new EchoVoxelBiome(
            "echoopenlandsprotocol:meadows",
            "Meadows",
            0.72D,
            0.48D,
            0x9FCBDA,
            0x6EA84F,
            "echoopenlandsprotocol:pollen_mote",
            List.of("openlands", "starter", "gentle", "grassland", "berries")
    );
    public static final EchoVoxelBiome WOODLANDS = new EchoVoxelBiome(
            "echoopenlandsprotocol:woodlands",
            "Woodlands",
            0.62D,
            0.72D,
            0x7898A6,
            0x4F7A3F,
            "echoopenlandsprotocol:leaf_mote",
            List.of("openlands", "wood", "resin", "mushrooms")
    );
    public static final EchoVoxelBiome STONEHILLS = new EchoVoxelBiome(
            "echoopenlandsprotocol:stonehills",
            "Stonehills",
            0.55D,
            0.32D,
            0xA8B4B6,
            0x6F8F50,
            "echoopenlandsprotocol:dust_mote",
            List.of("openlands", "stone", "ore", "caves")
    );
    public static final EchoVoxelBiome MARSHLANDS = new EchoVoxelBiome(
            "echoopenlandsprotocol:marshlands",
            "Marshlands",
            0.66D,
            0.9D,
            0x7FA7A0,
            0x5F8B54,
            "echoopenlandsprotocol:marsh_mist",
            List.of("openlands", "wetland", "reeds", "clay")
    );

    private static final int REGION_SIZE_BLOCKS = 48;
    private static final double STARTER_MEADOW_RADIUS_BLOCKS = 28.0D;
    private static final List<EchoVoxelBiome> BIOMES = List.of(
            MEADOWS,
            WOODLANDS,
            STONEHILLS,
            MARSHLANDS
    );
    private static final EchoVoxelBiomeSource SOURCE = new OpenlandsBiomeSource();

    private EchoVoxelOpenlandsBiomes() {
    }

    public static EchoVoxelBiomeSource source() {
        return SOURCE;
    }

    public static List<EchoVoxelBiome> all() {
        return BIOMES;
    }

    public static EchoVoxelBiome biomeAt(long seed, double x, double z) {
        return biomeAt(seed, (int) Math.floor(x), (int) Math.floor(z));
    }

    public static EchoVoxelBiome biomeAt(long seed, int x, int z) {
        double spawnDistance = Math.sqrt((double) x * x + (double) z * z);
        if (spawnDistance <= STARTER_MEADOW_RADIUS_BLOCKS) {
            return MEADOWS;
        }
        int regionX = Math.floorDiv(x, REGION_SIZE_BLOCKS);
        int regionZ = Math.floorDiv(z, REGION_SIZE_BLOCKS);
        double moisture = noise(seed ^ 0x4D415253484C414EL, regionX, regionZ);
        if (moisture >= 0.78D) {
            return MARSHLANDS;
        }
        double ridge = noise(seed ^ 0x53544F4E4548494CL, regionX, regionZ);
        if (ridge >= 0.72D) {
            return STONEHILLS;
        }
        double canopy = noise(seed ^ 0x574F4F444C414E44L, regionX, regionZ);
        return canopy >= 0.46D ? WOODLANDS : MEADOWS;
    }

    private static double noise(long seed, int regionX, int regionZ) {
        long mixed = seed;
        mixed ^= (long) regionX * 0x9E3779B97F4A7C15L;
        mixed ^= (long) regionZ * 0xC2B2AE3D27D4EB4FL;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return ((mixed >>> 11) & ((1L << 53) - 1)) * 0x1.0p-53;
    }

    private static final class OpenlandsBiomeSource implements EchoVoxelBiomeSource {
        @Override
        public String id() {
            return SOURCE_ID;
        }

        @Override
        public EchoVoxelBiome biomeAt(long seed, int x, int z) {
            return EchoVoxelOpenlandsBiomes.biomeAt(seed, x, z);
        }
    }
}
