package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;
import dev.echo.standalone.runtime.world.EchoVoxelWorldTickResult;

import java.util.List;

public final class EchoRuntimeVoxelWorldKernelSmokeHarness {
    private EchoRuntimeVoxelWorldKernelSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(42L, 0);

        require(world.loadedChunkCount() == 1, "origin world should begin as one loaded chunk");
        require(world.hasChunk(new EchoVoxelChunkId(0, 0, 0)), "origin chunk should be loaded");
        require(world.blockAt(-1, 0, -1).air(), "negative terrain should be air before streaming");

        EchoVoxelWorldStreamer streamer = sessionProfile.streamer();
        world = streamer.streamAround(world, world.spawnX(), 1.5D);
        require(world.loadedChunkCount() == 9, "streaming around spawn should load a 3x3 chunk region");
        require(world.hasChunk(new EchoVoxelChunkId(-1, 0, -1)), "streaming should include negative chunk coordinates");
        require(!world.blockAt(-1, 0, -1).air(), "negative streamed chunk should contain deterministic terrain");
        require(world.blockStateAt(-1, 0, -1).property("source").orElseThrow().equals("terrain"),
                "streamed terrain should carry generated block metadata");

        EchoVoxelBlockState terminal = world.blockStateAt(3, 4, 3);
        require(terminal.block().id().equals(EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID),
                "crash site terminal should survive chunk-region generation");
        require(terminal.property("source").orElseThrow().equals("structure"),
                "crash site terminal should carry structure metadata");

        EchoVoxelBlockState taggedMarker = EchoVoxelBlockState.of(bridge.runtimeMarkerBlock())
                .withProperty("owner", "kernel_smoke")
                .ticked();
        require(world.setBlockStateAt(20, 4, 0, taggedMarker),
                "streamed positive neighbor chunk should accept block state edits");
        require(world.blockStateAt(20, 4, 0).property("owner").orElseThrow().equals("kernel_smoke"),
                "block metadata should round-trip through world state");
        require(world.blockStateAt(20, 4, 0).tickVersion() == 1L,
                "block tick version should round-trip through world state");

        EchoVoxelWorld expanded = streamer.streamAround(world, 33.0D, 1.0D);
        require(expanded.loadedChunkCount() == 15,
                "moving two chunks east should append the next streamed chunk band");
        require(expanded.blockStateAt(20, 4, 0).property("owner").orElseThrow().equals("kernel_smoke"),
                "streaming should preserve existing edited chunks");
        require(!expanded.blockAt(34, 0, 0).air(),
                "newly streamed east chunk should contain deterministic terrain");
        long preTickVersion = expanded.blockStateAt(10, 4, 2).tickVersion();
        EchoVoxelWorldTickResult tick = expanded.tickLoadedBlocks(77L);
        require(tick.deterministicTickApplied(), "world tick should visit loaded non-air blocks");
        require(tick.hazardBlocks() > 0, "world tick should classify Ashfall hazard blocks");
        require(expanded.blockStateAt(10, 4, 2).tickVersion() == preTickVersion + 1L,
                "block tick version should increment on deterministic world tick");
        require(expanded.blockStateAt(10, 4, 2).property("lastTick").orElseThrow().equals("77"),
                "block tick metadata should record deterministic game tick");
        require(expanded.blockStateAt(10, 4, 2).property("hazardActive").orElseThrow().equals("true"),
                "hazard block tick metadata should mark active hazard state");
        require(expanded.collidesWithBox(34.1D, 0.1D, 0.1D, 34.9D, 0.9D, 0.9D),
                "world collision query should detect solid voxel collision boxes");
        require(!expanded.collidesWithBox(34.1D, 8.1D, 0.1D, 34.9D, 8.9D, 0.9D),
                "world collision query should ignore empty air above terrain");
        var downwardHit = expanded.raycast(34.5D, 8.5D, 0.5D, 0.0D, -90.0D, 12.0D);
        require(downwardHit.isPresent() && !downwardHit.orElseThrow().block().air(),
                "world raycast should find streamed terrain through the indexed chunk query path");

        EchoVoxelPlayerController player = EchoVoxelPlayerController.spawnAt(
                expanded,
                expanded.spawnX(),
                expanded.spawnZ(),
                expanded.spawnYawDegrees(),
                -22.0D
        );
        requireClose(player.state().reach(), EchoVoxelPlayerState.SURVIVAL_REACH,
                "voxel player survival reach");
        require(player.state().intersectsBlock(
                        (int) Math.floor(player.state().x()),
                        (int) Math.floor(player.state().y()),
                        (int) Math.floor(player.state().z())),
                "player body should expose occupied block volume for placement collision checks");
        requireFootprintGroundingKeepsLedgeWalkStable();

        System.out.println("phase15.voxel world kernel smoke PASS chunks="
                + expanded.loadedChunkCount()
                + " negativeChunk="
                + expanded.hasChunk(new EchoVoxelChunkId(-1, 0, -1))
                + " metadata="
                + expanded.blockStateAt(20, 4, 0).property("owner").orElseThrow()
                + " tick="
                + expanded.blockStateAt(20, 4, 0).tickVersion()
                + " worldTick="
                + tick.summary());
    }

    private static void requireFootprintGroundingKeepsLedgeWalkStable() {
        EchoVoxelBlock floor = new EchoVoxelBlock("test:floor", "Test Floor", 0xFF606060, true, true, 1.0D);
        EchoVoxelBlock ledge = new EchoVoxelBlock("test:ledge", "Test Ledge", 0xFF909090, true, true, 1.0D);
        EchoVoxelChunk chunk = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                chunk.setBlockLocal(x, 0, z, floor);
            }
        }
        for (int z = 4; z <= 5; z++) {
            for (int x = 4; x <= 5; x++) {
                chunk.setBlockLocal(x, 1, z, ledge);
            }
        }
        EchoVoxelWorld ledgeWorld = new EchoVoxelWorld(
                "test:ledge_walk",
                1L,
                16,
                List.of(chunk),
                5.5D,
                2.0D,
                4.5D,
                90.0D
        );
        EchoVoxelPlayerController ledgePlayer = new EchoVoxelPlayerController(new EchoVoxelPlayerState(
                5.55D,
                2.0D,
                4.5D,
                0.0D,
                90.0D,
                0.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        ));
        double previousX = ledgePlayer.state().x();
        boolean crossedCenterEdge = false;
        for (int tick = 0; tick < 8; tick++) {
            EchoVoxelPlayerStep step = ledgePlayer.tick(
                    ledgeWorld,
                    new EchoVoxelPlayerInput(true, false, false, false, false, false, true, 0.0D, 0.0D),
                    1.0D / 60.0D
            );
            EchoVoxelPlayerState current = step.current();
            require(current.x() >= previousX - 0.0001D,
                    "ledge walk should not rubber-band backward at tick " + tick);
            if (current.x() >= 6.0D && current.x() <= 6.30D) {
                crossedCenterEdge = true;
                requireClose(current.y(), 2.0D, "ledge footprint support y at tick " + tick);
                require(current.grounded(), "ledge footprint support should stay grounded at tick " + tick);
            }
            previousX = current.x();
        }
        require(crossedCenterEdge, "ledge smoke should cross the center-column edge while feet are still supported");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireClose(double actual, double expected, String label) {
        if (Math.abs(actual - expected) > 0.0001D) {
            throw new AssertionError(label + " expected " + expected + " but was " + actual);
        }
    }
}
