package dev.echo.standalone.runtime.world;

import java.util.List;

public final class EchoVoxelAshfallBiomes {
    public static final String SOURCE_ID = "echoashfallprotocol:ashfall_biomes";
    public static final EchoVoxelBiome CRASH_ZONE_WASTELAND = new EchoVoxelBiome(
            "echoashfallprotocol:crash_zone_wasteland",
            "Crash Zone Wasteland",
            0.9D,
            0.1D,
            0x8A5D51,
            0x5A4452,
            "minecraft:ash",
            List.of("ashfall", "wasteland", "crash_zone")
    );
    public static final EchoVoxelBiome THE_WASTELAND = new EchoVoxelBiome(
            "echoashfallprotocol:the_wasteland",
            "The Wasteland",
            1.0D,
            0.0D,
            0x725F4A,
            0x554A33,
            "minecraft:ash",
            List.of("ashfall", "wasteland")
    );
    public static final EchoVoxelBiome RUINED_PLAINS = new EchoVoxelBiome(
            "echoashfallprotocol:ruined_plains",
            "Ruined Plains",
            0.8D,
            0.2D,
            0x6D6B58,
            0x65713A,
            "minecraft:ash",
            List.of("ashfall", "plains")
    );
    public static final EchoVoxelBiome TOXIC_SWAMP = new EchoVoxelBiome(
            "echoashfallprotocol:toxic_swamp",
            "Toxic Swamp",
            0.9D,
            0.9D,
            0x506A6A,
            0x78A333,
            "minecraft:spore_blossom_air",
            List.of("ashfall", "swamp", "toxic")
    );
    public static final EchoVoxelBiome INDUSTRIAL_RUINS = new EchoVoxelBiome(
            "echoashfallprotocol:industrial_ruins",
            "Industrial Ruins",
            0.6D,
            0.4D,
            0x5A5550,
            0x444A37,
            "minecraft:ash",
            List.of("ashfall", "industrial", "ruins")
    );
    public static final EchoVoxelBiome RADIATION_ZONE = new EchoVoxelBiome(
            "echoashfallprotocol:radiation_zone",
            "Radiation Zone",
            1.5D,
            0.1D,
            0x526A66,
            0x9DBA3C,
            "minecraft:warped_spore",
            List.of("ashfall", "radiation", "hazard")
    );
    public static final EchoVoxelBiome RUINED_CITYSCAPE = new EchoVoxelBiome(
            "echoashfallprotocol:ruined_cityscape",
            "Ruined Cityscape",
            0.7D,
            0.2D,
            0x4F5960,
            0x505944,
            "minecraft:ash",
            List.of("ashfall", "city", "ruins")
    );
    public static final EchoVoxelBiome CRYOGENIC_RUINS = new EchoVoxelBiome(
            "echoashfallprotocol:cryogenic_ruins",
            "Cryogenic Ruins",
            -0.5D,
            0.5D,
            0x8AA0A8,
            0x687A78,
            "minecraft:snowflake",
            List.of("ashfall", "cold", "ruins")
    );
    public static final EchoVoxelBiome NEXUS_SCAR = new EchoVoxelBiome(
            "echoashfallprotocol:nexus_scar",
            "Nexus Scar",
            1.2D,
            0.0D,
            0x4B315F,
            0x6A2BA8,
            "minecraft:warped_spore",
            List.of("ashfall", "nexus", "anomaly")
    );

    private static final int REGION_SIZE_BLOCKS = 64;
    private static final double CRASH_ZONE_RADIUS_BLOCKS = 42.0D;
    private static final List<EchoVoxelBiome> BIOMES = List.of(
            CRASH_ZONE_WASTELAND,
            THE_WASTELAND,
            RUINED_PLAINS,
            TOXIC_SWAMP,
            INDUSTRIAL_RUINS,
            RADIATION_ZONE,
            RUINED_CITYSCAPE,
            CRYOGENIC_RUINS,
            NEXUS_SCAR
    );
    private static final EchoVoxelBiomeSource SOURCE = new AshfallBiomeSource();

    private EchoVoxelAshfallBiomes() {
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
        double distanceFromCrash = Math.sqrt((double) x * x + (double) z * z);
        if (distanceFromCrash <= CRASH_ZONE_RADIUS_BLOCKS) {
            return CRASH_ZONE_WASTELAND;
        }
        int regionX = Math.floorDiv(x, REGION_SIZE_BLOCKS);
        int regionZ = Math.floorDiv(z, REGION_SIZE_BLOCKS);
        double anomaly = noise(seed ^ 0x4E4558555353414CL, regionX, regionZ);
        if (anomaly >= 0.94D) {
            return NEXUS_SCAR;
        }
        double moisture = noise(seed ^ 0x544F584943535750L, regionX, regionZ);
        if (moisture >= 0.78D) {
            return TOXIC_SWAMP;
        }
        double ruins = noise(seed ^ 0x494E445553545259L, regionX, regionZ);
        if (ruins >= 0.82D) {
            return INDUSTRIAL_RUINS;
        }
        if (ruins >= 0.68D) {
            return RUINED_CITYSCAPE;
        }
        double radiation = noise(seed ^ 0x5241444941544544L, regionX, regionZ);
        if (radiation >= 0.78D) {
            return RADIATION_ZONE;
        }
        double cold = noise(seed ^ 0x4352594F5255494EL, regionX, regionZ);
        if (cold >= 0.86D) {
            return CRYOGENIC_RUINS;
        }
        double plains = noise(seed ^ 0x504C41494E53L, regionX, regionZ);
        return plains >= 0.48D ? RUINED_PLAINS : THE_WASTELAND;
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

    private static final class AshfallBiomeSource implements EchoVoxelBiomeSource {
        @Override
        public String id() {
            return SOURCE_ID;
        }

        @Override
        public EchoVoxelBiome biomeAt(long seed, int x, int z) {
            return EchoVoxelAshfallBiomes.biomeAt(seed, x, z);
        }
    }
}
