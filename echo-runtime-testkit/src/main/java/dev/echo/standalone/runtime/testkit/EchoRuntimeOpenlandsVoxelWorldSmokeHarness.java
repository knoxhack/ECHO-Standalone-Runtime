package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockInstance;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelOpenlandsBiomes;
import dev.echo.standalone.runtime.world.EchoVoxelOpenlandsWorldGeneration;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldProfiles;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class EchoRuntimeOpenlandsVoxelWorldSmokeHarness {
    private static final long SEED = 42L;

    private EchoRuntimeOpenlandsVoxelWorldSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path repoRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        EchoVoxelOpenlandsWorldGeneration.Blocks blocks = EchoVoxelOpenlandsWorldGeneration.Blocks.defaults();
        EchoVoxelWorld world = EchoVoxelOpenlandsWorldGeneration.generateFirstHourRegion(SEED, blocks, 1);

        require(world.worldId().equals(EchoVoxelOpenlandsWorldGeneration.WORLD_ID),
                "Openlands world id should be standalone_first_hour");
        require(world.loadedChunkCount() == 9, "first-hour region should load a 3x3 chunk region");
        require(world.hasChunk(new EchoVoxelChunkId(0, 0, 0)), "origin chunk should be loaded");
        require(world.hasChunk(new EchoVoxelChunkId(-1, 0, -1)), "negative starter neighbor should be loaded");
        require(world.biomeAt(world.spawnX(), world.spawnZ()).id().equals(EchoVoxelOpenlandsBiomes.MEADOWS.id()),
                "spawn should begin in the relaxed Meadows starter biome");
        require(EchoVoxelOpenlandsBiomes.all().size() == 4, "MVP Openlands biome source should expose 4 biomes");
        require(EchoVoxelOpenlandsBiomes.all().stream()
                        .map(EchoVoxelBiome::ambientParticle)
                        .noneMatch(particle -> particle.startsWith("minecraft:")),
                "Openlands biome ambience must not reference Minecraft-owned particle names");
        require(!world.blockAt(7, 4, 7).air(), "spawn should have safe solid terrain below the player");
        require(!world.blockAt(1, 4, 8).air(), "origin road should contain generated blocks");

        StarterSignals signals = scanStarterSignals(world);
        require(signals.hasBranchwood, "starter region should guarantee branchwood logs");
        require(signals.hasLooseStone, "starter region should guarantee loose fieldstone");
        require(signals.hasFiber, "starter region should guarantee reed fiber");
        require(signals.hasBerries, "starter region should guarantee berries");
        require(signals.hasWaterHint, "starter region should guarantee water or well hint");
        require(signals.hasOldRoad, "starter region should guarantee old road segment");
        require(signals.hasRoadMarker, "starter region should guarantee old road marker");
        require(signals.hasCaveOrRuin, "starter region should guarantee a cave mouth or ruin silhouette");
        require(signals.hasBrokenWaystone, "starter region should guarantee first broken waystone site");
        require(signals.minecraftReferences == 0, "Openlands generated block IDs/properties must avoid Minecraft names");

        EchoVoxelWorldRuntimeProfile profile = EchoVoxelWorldProfiles.openlandsFirstHour(
                id -> Optional.ofNullable(defaultBlockMap(blocks).get(id))
                        .orElseThrow(() -> new IllegalArgumentException("missing Openlands block " + id)),
                1
        );
        EchoVoxelWorld profiled = profile.generate(SEED, 0);
        require(profiled.worldId().equals(EchoVoxelOpenlandsWorldGeneration.WORLD_ID),
                "world profile should generate the Openlands first-hour world");
        require(profiled.loadedChunkCount() == 1, "explicit radius 0 profile generation should load one origin chunk");

        EchoVoxelWorldStreamer streamer = profile.streamer();
        EchoVoxelWorld streamed = streamer.streamAround(profiled, -1.0D, -1.0D);
        require(streamed.loadedChunkCount() == 9, "profile streamer should expand Openlands chunks around spawn");
        require(!streamed.blockAt(-1, 0, -1).air(), "streamed Openlands neighbor chunk should contain terrain");
        require(streamed.biomeIdAtBlock(-1, 0, -1).orElseThrow().startsWith("echoopenlandsprotocol:"),
                "streamed terrain should carry Openlands biome IDs");

        Path report = repoRoot.resolve("reports/echo/standalone/openlands-voxel-world.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, reportJson(world, signals), StandardCharsets.UTF_8);

        System.out.println("openlands.voxel world smoke PASS chunks="
                + world.loadedChunkCount()
                + " biomes=" + EchoVoxelOpenlandsBiomes.all().size()
                + " branchwood=" + signals.hasBranchwood
                + " oldRoad=" + signals.hasOldRoad
                + " waystone=" + signals.hasBrokenWaystone
                + " report=" + repoRoot.relativize(report));
    }

    private static StarterSignals scanStarterSignals(EchoVoxelWorld world) {
        StarterSignals signals = new StarterSignals();
        for (EchoVoxelBlockInstance instance : world.nonAirBlocks()) {
            EchoVoxelBlock block = instance.block();
            EchoVoxelBlockState state = instance.state();
            String id = block.id();
            if (id.toLowerCase().contains("minecraft")) {
                signals.minecraftReferences++;
            }
            if (id.equals("echoopenlandsprotocol:branchwood_log")) {
                signals.hasBranchwood = true;
            }
            if (state.property("resource").orElse("").equals("loose_fieldstone")) {
                signals.hasLooseStone = true;
            }
            if (state.property("resource").orElse("").equals("reed_fiber")) {
                signals.hasFiber = true;
            }
            if (state.property("resource").orElse("").equals("berries")) {
                signals.hasBerries = true;
            }
            if (state.property("landmark").orElse("").equals("water_or_well_hint")) {
                signals.hasWaterHint = true;
            }
            if (state.property("oldRoadSegment").orElse("").equals("true")) {
                signals.hasOldRoad = true;
            }
            if (state.property("landmark").orElse("").equals("old_road_marker")) {
                signals.hasRoadMarker = true;
            }
            if (state.property("landmark").orElse("").equals("cave_mouth")
                    || state.property("starterGuarantee").orElse("").equals("cave_or_ruin")) {
                signals.hasCaveOrRuin = true;
            }
            if (state.property("landmark").orElse("").equals("broken_waystone")
                    || state.property("landmark").orElse("").equals("broken_waystone_site")) {
                signals.hasBrokenWaystone = true;
            }
            for (String propertyValue : state.properties().values()) {
                if (propertyValue.toLowerCase().contains("minecraft")) {
                    signals.minecraftReferences++;
                }
            }
        }
        return signals;
    }

    private static Map<String, EchoVoxelBlock> defaultBlockMap(EchoVoxelOpenlandsWorldGeneration.Blocks blocks) {
        LinkedHashMap<String, EchoVoxelBlock> result = new LinkedHashMap<>();
        result.put(blocks.meadowGrass().id(), blocks.meadowGrass());
        result.put(blocks.forestSoil().id(), blocks.forestSoil());
        result.put(blocks.mud().id(), blocks.mud());
        result.put(blocks.clay().id(), blocks.clay());
        result.put(blocks.fieldstone().id(), blocks.fieldstone());
        result.put(blocks.limestone().id(), blocks.limestone());
        result.put(blocks.shale().id(), blocks.shale());
        result.put(blocks.deepstone().id(), blocks.deepstone());
        result.put(blocks.branchwoodLog().id(), blocks.branchwoodLog());
        result.put(blocks.branchwoodPlanks().id(), blocks.branchwoodPlanks());
        result.put(blocks.oldRoadBlock().id(), blocks.oldRoadBlock());
        result.put(blocks.oldRoadMarker().id(), blocks.oldRoadMarker());
        result.put(blocks.brokenWaystone().id(), blocks.brokenWaystone());
        result.put(blocks.waystonePlinth().id(), blocks.waystonePlinth());
        return Map.copyOf(result);
    }

    private static String reportJson(EchoVoxelWorld world, StarterSignals signals) {
        TreeMap<String, Object> starter = new TreeMap<>();
        starter.put("branchwood", signals.hasBranchwood);
        starter.put("looseStone", signals.hasLooseStone);
        starter.put("reedFiber", signals.hasFiber);
        starter.put("berries", signals.hasBerries);
        starter.put("waterHint", signals.hasWaterHint);
        starter.put("oldRoad", signals.hasOldRoad);
        starter.put("oldRoadMarker", signals.hasRoadMarker);
        starter.put("caveOrRuin", signals.hasCaveOrRuin);
        starter.put("brokenWaystone", signals.hasBrokenWaystone);

        return "{\n"
                + "  \"schema\": \"echo.standalone.openlands_voxel_world.v1\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"worldId\": \"" + world.worldId() + "\",\n"
                + "  \"seed\": " + world.seed() + ",\n"
                + "  \"loadedChunkCount\": " + world.loadedChunkCount() + ",\n"
                + "  \"biomeSource\": \"" + world.biomeSource().id() + "\",\n"
                + "  \"mvpBiomeCount\": " + EchoVoxelOpenlandsBiomes.all().size() + ",\n"
                + "  \"spawnBiome\": \"" + world.biomeAt(world.spawnX(), world.spawnZ()).id() + "\",\n"
                + "  \"minecraftReferences\": " + signals.minecraftReferences + ",\n"
                + "  \"starterGuarantees\": " + objectJson(starter) + "\n"
                + "}\n";
    }

    private static String objectJson(TreeMap<String, Object> values) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (!first) {
                builder.append(", ");
            }
            first = false;
            builder.append("\"").append(entry.getKey()).append("\": ");
            Object value = entry.getValue();
            if (value instanceof Boolean booleanValue) {
                builder.append(booleanValue);
            } else if (value instanceof Number numberValue) {
                builder.append(numberValue);
            } else {
                builder.append("\"").append(String.valueOf(value)).append("\"");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class StarterSignals {
        private boolean hasBranchwood;
        private boolean hasLooseStone;
        private boolean hasFiber;
        private boolean hasBerries;
        private boolean hasWaterHint;
        private boolean hasOldRoad;
        private boolean hasRoadMarker;
        private boolean hasCaveOrRuin;
        private boolean hasBrokenWaystone;
        private int minecraftReferences;
    }
}
