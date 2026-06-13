package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidPlacement;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidTickResult;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeVoxelFluidSmokeHarness {
    private EchoRuntimeVoxelFluidSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoVoxelBlock floor = new EchoVoxelBlock("test:fluid_floor", "Fluid Floor", 0xFF4F5358, true, true, 2.0D);
        EchoVoxelBlock barrier = new EchoVoxelBlock("test:fluid_barrier", "Fluid Barrier", 0xFF20242A, true, true, 3.0D);
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        EchoVoxelChunk east = new EchoVoxelChunk(new EchoVoxelChunkId(1, 0, 0), 16);
        fillFloor(origin, floor);
        fillFloor(east, floor);
        origin.setBlockLocal(15, 1, 2, barrier);
        EchoVoxelWorld world = new EchoVoxelWorld(
                "test:fluid_mechanics",
                20260610L,
                16,
                List.of(origin, east),
                15.5D,
                2.0D,
                3.5D,
                90.0D
        );

        EchoVoxelFluidRuntime fluids = new EchoVoxelFluidRuntime();
        EchoVoxelFluidPlacement waterSource = fluids.placeSource(world, EchoVoxelFluidType.WATER, 15, 1, 3);
        EchoVoxelFluidPlacement lavaSource = fluids.placeSource(world, EchoVoxelFluidType.LAVA, 17, 1, 3);
        EchoVoxelFluidPlacement fallingSource = fluids.placeSource(world, EchoVoxelFluidType.WATER, 3, 4, 3);
        require(waterSource.placed(), "water source should place into loaded chunk");
        require(lavaSource.placed(), "lava source should place into loaded chunk");
        require(fallingSource.placed(), "falling water source should place above air");

        EchoVoxelFluidTickResult firstTick = fluids.tick(world, 1L);
        require(firstTick.downwardWrites() >= 1, "fluid tick should flow down through air");
        require(firstTick.horizontalWrites() >= 4, "fluid tick should spread horizontally across supported cells");
        require(firstTick.hardenedCells() >= 1, "water/lava contact should harden a fluid cell");
        require(firstTick.blockedBySolid() >= 1, "solid barrier should block horizontal fluid spread");
        require(firstTick.crossChunkWrites() >= 1, "fluid should flow across loaded chunk boundaries");

        EchoVoxelBlockState crossChunkCell = world.blockStateAt(16, 1, 3);
        require(crossChunkCell.block().id().equals(EchoVoxelFluidRuntime.HARDENED_FLUID_STONE.id()),
                "water/lava contact across the chunk edge should leave hardened stone");
        require(crossChunkCell.property("interaction").orElseThrow().equals("fluid_hardening"),
                "hardened cell should record fluid interaction metadata");
        require(world.blockStateAt(15, 1, 2).block().id().equals(barrier.id()),
                "barrier cell should not be overwritten by fluid");
        require(world.blockStateAt(3, 3, 3).block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "falling source should create a lower water cell");
        require(world.blockStateAt(14, 1, 3).property("fluidLevel").orElseThrow().equals("1"),
                "horizontal water flow should record level one");
        require(world.blockStateAt(15, 1, 3).property("fluidSource").orElseThrow().equals("true"),
                "source water cell should remain marked as a source");

        EchoVoxelFluidTickResult secondTick = fluids.tick(world, 2L);
        require(secondTick.totalWrites() > 0, "second fluid tick should continue deterministic propagation");
        require(world.blockStateAt(3, 2, 3).block().id().equals(EchoVoxelFluidRuntime.WATER.id()),
                "falling water should continue descending on the next tick");

        writeReport(firstTick, secondTick, world);

        System.out.println("phase15.voxel fluid smoke PASS first="
                + firstTick.summary()
                + " second="
                + secondTick.summary()
                + " hardened="
                + world.blockStateAt(16, 1, 3).block().id());
    }

    private static void fillFloor(EchoVoxelChunk chunk, EchoVoxelBlock floor) {
        for (int z = 0; z < chunk.size(); z++) {
            for (int x = 0; x < chunk.size(); x++) {
                chunk.setBlockLocal(x, 0, z, floor);
            }
        }
    }

    private static void writeReport(
            EchoVoxelFluidTickResult firstTick,
            EchoVoxelFluidTickResult secondTick,
            EchoVoxelWorld world
    ) throws IOException {
        Path report = Path.of("reports/echo/standalone/world-fluids.json");
        Files.createDirectories(report.getParent());
        String json = """
                {
                  "schema": "echo.standalone.voxel_fluid_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVoxelFluidSmokeHarness",
                  "status": "PASS",
                  "summary": "Voxel fluid mechanics cover source placement, downward flow, horizontal spread, loaded chunk-boundary flow, solid blocking, and water/lava hardening.",
                  "sourcePlacements": 3,
                  "firstTick": {
                    "fluidCellsBeforeTick": %d,
                    "sourceCells": %d,
                    "downwardWrites": %d,
                    "horizontalWrites": %d,
                    "hardenedCells": %d,
                    "blockedBySolid": %d,
                    "outsideLoadedChunk": %d,
                    "crossChunkWrites": %d
                  },
                  "secondTick": {
                    "fluidCellsBeforeTick": %d,
                    "downwardWrites": %d,
                    "horizontalWrites": %d,
                    "hardenedCells": %d,
                    "crossChunkWrites": %d
                  },
                  "evidence": {
                    "waterSourcePreserved": %s,
                    "fallingWaterDescended": %s,
                    "solidBarrierPreserved": %s,
                    "crossChunkHardenedCell": "%s",
                    "nativeModLoaderCommandUsed": false
                  }
                }
                """.formatted(
                firstTick.fluidCellsBeforeTick(),
                firstTick.sourceCells(),
                firstTick.downwardWrites(),
                firstTick.horizontalWrites(),
                firstTick.hardenedCells(),
                firstTick.blockedBySolid(),
                firstTick.outsideLoadedChunk(),
                firstTick.crossChunkWrites(),
                secondTick.fluidCellsBeforeTick(),
                secondTick.downwardWrites(),
                secondTick.horizontalWrites(),
                secondTick.hardenedCells(),
                secondTick.crossChunkWrites(),
                Boolean.toString(world.blockStateAt(15, 1, 3).property("fluidSource").orElse("").equals("true")),
                Boolean.toString(world.blockStateAt(3, 2, 3).block().id().equals(EchoVoxelFluidRuntime.WATER.id())),
                Boolean.toString(world.blockStateAt(15, 1, 2).block().id().equals("test:fluid_barrier")),
                escape(world.blockStateAt(16, 1, 3).block().id())
        );
        Files.writeString(report, json);
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
