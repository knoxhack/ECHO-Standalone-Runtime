package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerStep;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimePlayerFluidMovementSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports", "echo", "standalone", "player-fluid-movement.json");
    private static final double TICK_SECONDS = 0.1D;

    private EchoRuntimePlayerFluidMovementSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoVoxelWorld waterWorld = waterWorld();
        EchoVoxelWorld airWorld = emptyWorld("test:player_fluid_air");

        EchoVoxelPlayerController waterFalling = new EchoVoxelPlayerController(playerState(2.5D, 1.0D, 2.5D, -6.0D, false));
        EchoVoxelPlayerStep waterIdle = waterFalling.tick(waterWorld, EchoVoxelPlayerInput.idle(), TICK_SECONDS);

        EchoVoxelPlayerController airFalling = new EchoVoxelPlayerController(playerState(2.5D, 1.0D, 2.5D, -6.0D, false));
        EchoVoxelPlayerStep airIdle = airFalling.tick(airWorld, EchoVoxelPlayerInput.idle(), TICK_SECONDS);

        require(waterIdle.current().velocityY() > airIdle.current().velocityY() + 4.0D,
                "Fluid drag and buoyancy should slow downward fall speed versus air");
        require(waterIdle.current().velocityY() >= -3.0D,
                "Fluid terminal velocity should clamp fast falling into a swimmable descent");

        EchoVoxelPlayerStep swimUp = waterFalling.tick(
                waterWorld,
                new EchoVoxelPlayerInput(false, false, false, false, true, false, false, 0.0D, 0.0D),
                TICK_SECONDS
        );
        require(swimUp.jumped() && swimUp.reason().equals("swim"),
                "Jump input while immersed should report a swim step");
        require(swimUp.current().velocityY() > 0.0D && swimUp.current().y() > waterIdle.current().y(),
                "Jump input while immersed should create upward swim movement");

        EchoVoxelPlayerController crouchSink = new EchoVoxelPlayerController(playerState(2.5D, 1.0D, 2.5D, 0.0D, false));
        EchoVoxelPlayerStep sink = crouchSink.tick(
                waterWorld,
                new EchoVoxelPlayerInput(false, false, false, false, false, true, false, 0.0D, 0.0D),
                TICK_SECONDS
        );
        require(sink.current().velocityY() < 0.0D,
                "Crouch input while immersed should create controlled sinking movement");

        EchoVoxelPlayerController waterForward = new EchoVoxelPlayerController(playerState(2.5D, 1.0D, 2.5D, 0.0D, false));
        EchoVoxelPlayerController airForward = new EchoVoxelPlayerController(playerState(2.5D, 1.0D, 2.5D, 0.0D, true));
        EchoVoxelPlayerInput forward = new EchoVoxelPlayerInput(true, false, false, false, false, false, false, 0.0D, 0.0D);
        EchoVoxelPlayerStep waterMove = waterForward.tick(waterWorld, forward, TICK_SECONDS);
        EchoVoxelPlayerStep airMove = airForward.tick(airWorld, forward, TICK_SECONDS);
        double waterForwardDelta = waterMove.current().z() - waterMove.previous().z();
        double airForwardDelta = airMove.current().z() - airMove.previous().z();
        require(waterForwardDelta > 0.0D,
                "Forward input should still move the player while immersed");
        require(waterForwardDelta < airForwardDelta,
                "Fluid movement should be slower than normal walking movement");

        writeReport(waterIdle, airIdle, swimUp, sink, waterForwardDelta, airForwardDelta);
        System.out.println("player fluid movement smoke PASS waterFallVelocity="
                + waterIdle.current().velocityY()
                + " airFallVelocity=" + airIdle.current().velocityY()
                + " swimUpVelocity=" + swimUp.current().velocityY()
                + " waterForwardDelta=" + waterForwardDelta
                + " airForwardDelta=" + airForwardDelta);
    }

    private static EchoVoxelWorld waterWorld() {
        EchoVoxelWorld world = emptyWorld("test:player_fluid_water");
        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();
        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 2, 1, 2).placed(),
                "Water source at player feet should place");
        require(fluids.placeSource(world, EchoVoxelFluidType.WATER, 2, 2, 2).placed(),
                "Water source at player torso should place");
        return world;
    }

    private static EchoVoxelWorld emptyWorld(String worldId) {
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        return new EchoVoxelWorld(
                worldId,
                20260610L,
                16,
                List.of(origin),
                2.5D,
                1.0D,
                2.5D,
                0.0D
        );
    }

    private static EchoVoxelPlayerState playerState(double x, double y, double z, double velocityY, boolean grounded) {
        return new EchoVoxelPlayerState(
                x,
                y,
                z,
                velocityY,
                0.0D,
                0.0D,
                grounded,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        );
    }

    private static void writeReport(
            EchoVoxelPlayerStep waterIdle,
            EchoVoxelPlayerStep airIdle,
            EchoVoxelPlayerStep swimUp,
            EchoVoxelPlayerStep sink,
            double waterForwardDelta,
            double airForwardDelta
    ) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.player_fluid_movement_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimePlayerFluidMovementSmokeHarness",
                  "status": "PASS",
                  "summary": "Player movement detects standalone fluid cells and applies swim drag, buoyancy, jump-to-swim, crouch sinking, and reduced horizontal movement speed.",
                  "falling": {
                    "waterVelocityY": %.4f,
                    "airVelocityY": %.4f,
                    "waterReason": "%s",
                    "airReason": "%s"
                  },
                  "swimming": {
                    "jumped": %s,
                    "reason": "%s",
                    "velocityY": %.4f,
                    "sinkVelocityY": %.4f
                  },
                  "horizontal": {
                    "waterForwardDelta": %.4f,
                    "airForwardDelta": %.4f
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "fluidCellsQueriedByPlayerController": true,
                    "swimUpInput": true,
                    "crouchSinkInput": true,
                    "movementSlowedInFluid": %s
                  }
                }
                """.formatted(
                waterIdle.current().velocityY(),
                airIdle.current().velocityY(),
                escape(waterIdle.reason()),
                escape(airIdle.reason()),
                Boolean.toString(swimUp.jumped()),
                escape(swimUp.reason()),
                swimUp.current().velocityY(),
                sink.current().velocityY(),
                waterForwardDelta,
                airForwardDelta,
                Boolean.toString(waterForwardDelta < airForwardDelta)
        );
        Files.writeString(REPORT_PATH, json);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
