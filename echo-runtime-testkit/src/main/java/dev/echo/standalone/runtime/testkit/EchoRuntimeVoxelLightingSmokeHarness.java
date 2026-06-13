package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelLightRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelLightRuntime.EchoVoxelLightSnapshot;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeVoxelLightingSmokeHarness {
    private EchoRuntimeVoxelLightingSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoVoxelBlock floor = new EchoVoxelBlock("test:light_floor", "Light Floor", 0xFF42464D, true, true, 2.0D);
        EchoVoxelBlock roof = new EchoVoxelBlock("test:opaque_roof", "Opaque Roof", 0xFF1F242A, true, true, 4.0D);
        EchoVoxelBlock glass = new EchoVoxelBlock(
                "test:smoke_glass",
                "Smoke Glass",
                0xAA9AD8FF,
                0xCCBDEBFF,
                "test/block/smoke_glass",
                EchoVoxelMaterialPattern.FLAT,
                true,
                false,
                0.3D
        );
        EchoVoxelBlock lightCrystal = new EchoVoxelBlock(
                "test:light_crystal",
                "Light Crystal",
                0xFFFFF0A8,
                0xFFFFFFFF,
                "test/block/light_crystal",
                EchoVoxelMaterialPattern.FLAT,
                false,
                false,
                0.1D
        );
        EchoVoxelBlock barrier = new EchoVoxelBlock("test:opaque_barrier", "Opaque Barrier", 0xFF080A0F, true, true, 6.0D);

        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        EchoVoxelChunk east = new EchoVoxelChunk(new EchoVoxelChunkId(1, 0, 0), 16);
        fillFloor(origin, floor);
        fillFloor(east, floor);
        origin.setBlockLocal(4, 8, 4, roof);
        origin.setStateLocal(6, 8, 4, EchoVoxelBlockState.of(glass).withProperty("lightOpacity", "1"));
        origin.setStateLocal(15, 2, 3, EchoVoxelBlockState.of(lightCrystal).withProperty("lightEmission", "15"));
        fillBarrierPlane(origin, barrier);

        EchoVoxelWorld world = new EchoVoxelWorld(
                "test:voxel_lighting",
                20260611L,
                16,
                List.of(origin, east),
                15.5D,
                2.0D,
                3.5D,
                90.0D
        );

        EchoVoxelLightRuntime lighting = new EchoVoxelLightRuntime();
        EchoVoxelLightSnapshot blocked = lighting.bake(world);
        require(blocked.loadedCellCount() == 8192, "two loaded 16^3 chunks should be included in lighting");
        require(blocked.skyLightAt(1, 15, 1) == 15, "open sky column should receive full sky light");
        require(blocked.skyLightAt(4, 7, 4) == 0, "opaque roof should shadow cells below it");
        require(blocked.skyLightAt(6, 7, 4) == 14, "translucent glass should attenuate sky light by one");
        require(blocked.blockLightAt(15, 2, 3) == 15, "emissive block should seed level 15 block light");
        require(blocked.blockLightAt(16, 2, 3) == 14, "emissive block light should cross into the loaded east chunk");
        require(blocked.blockLightAt(17, 2, 3) == 13, "block light should attenuate with distance");
        require(blocked.blockLightAt(14, 2, 3) == 0, "opaque barrier should block light entry");
        require(blocked.blockLightAt(13, 2, 3) == 0, "cells behind the opaque barrier should remain dark");
        require(blocked.opaqueBlockedSteps() > 0, "lighting runtime should count opaque blocker steps");
        require(blocked.crossChunkBlockLightWrites() > 0, "lighting runtime should count cross-chunk propagation");

        world.setBlockStateAt(14, 2, 3, EchoVoxelBlockState.AIR);
        EchoVoxelLightSnapshot unblocked = lighting.bake(world);
        require(unblocked.blockLightAt(14, 2, 3) == 14, "removing a blocker should allow light into the former barrier cell");
        require(unblocked.blockLightAt(13, 2, 3) == 13, "recomputed light should propagate past the removed blocker");

        writeReport(blocked, unblocked);
        System.out.println("voxel lighting smoke PASS sky="
                + blocked.skyLightAt(1, 15, 1)
                + " roofShadow=" + blocked.skyLightAt(4, 7, 4)
                + " glass=" + blocked.skyLightAt(6, 7, 4)
                + " crossChunk=" + blocked.blockLightAt(16, 2, 3)
                + " dynamic=" + unblocked.blockLightAt(13, 2, 3));
    }

    private static void fillFloor(EchoVoxelChunk chunk, EchoVoxelBlock floor) {
        for (int z = 0; z < chunk.size(); z++) {
            for (int x = 0; x < chunk.size(); x++) {
                chunk.setBlockLocal(x, 0, z, floor);
            }
        }
    }

    private static void fillBarrierPlane(EchoVoxelChunk chunk, EchoVoxelBlock barrier) {
        for (int y = 1; y < chunk.size(); y++) {
            for (int z = 0; z < chunk.size(); z++) {
                chunk.setBlockLocal(14, y, z, barrier);
            }
        }
    }

    private static void writeReport(EchoVoxelLightSnapshot blocked, EchoVoxelLightSnapshot unblocked)
            throws IOException {
        Path report = Path.of("reports/echo/standalone/world-lighting.json");
        Files.createDirectories(report.getParent());
        String json = """
                {
                  "schema": "echo.standalone.voxel_lighting_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimeVoxelLightingSmokeHarness",
                  "status": "PASS",
                  "summary": "Voxel lighting covers sky light, translucent attenuation, opaque shadows, emissive block light, cross-chunk propagation, and recompute after block changes.",
                  "loadedCellCount": %d,
                  "skyLitCellCount": %d,
                  "blockLitCellCount": %d,
                  "maxSkyLight": %d,
                  "maxBlockLight": %d,
                  "emissiveCellCount": %d,
                  "opaqueBlockedSteps": %d,
                  "crossChunkBlockLightWrites": %d,
                  "samples": {
                    "openSky": %d,
                    "underOpaqueRoof": %d,
                    "underTranslucentGlass": %d,
                    "emissiveSource": %d,
                    "crossChunkNeighbor": %d,
                    "crossChunkSecondCell": %d,
                    "blockedBarrierCell": %d,
                    "blockedBehindBarrier": %d,
                    "unblockedFormerBarrier": %d,
                    "unblockedBehindBarrier": %d
                  },
                  "evidence": {
                    "skyLightPropagatesDownOpenColumn": %s,
                    "opaqueRoofBlocksSkyLight": %s,
                    "translucentBlockAttenuatesSkyLight": %s,
                    "emissiveBlockSeedsBlockLight": %s,
                    "blockLightCrossesLoadedChunkBoundary": %s,
                    "opaqueBlockStopsBlockLight": %s,
                    "dynamicRecomputeAfterBlockChange": %s,
                    "nativeModLoaderCommandUsed": false
                  }
                }
                """.formatted(
                blocked.loadedCellCount(),
                blocked.skyLitCellCount(),
                blocked.blockLitCellCount(),
                blocked.maxSkyLight(),
                blocked.maxBlockLight(),
                blocked.emissiveCellCount(),
                blocked.opaqueBlockedSteps(),
                blocked.crossChunkBlockLightWrites(),
                blocked.skyLightAt(1, 15, 1),
                blocked.skyLightAt(4, 7, 4),
                blocked.skyLightAt(6, 7, 4),
                blocked.blockLightAt(15, 2, 3),
                blocked.blockLightAt(16, 2, 3),
                blocked.blockLightAt(17, 2, 3),
                blocked.blockLightAt(14, 2, 3),
                blocked.blockLightAt(13, 2, 3),
                unblocked.blockLightAt(14, 2, 3),
                unblocked.blockLightAt(13, 2, 3),
                Boolean.toString(blocked.skyLightAt(1, 15, 1) == 15),
                Boolean.toString(blocked.skyLightAt(4, 7, 4) == 0),
                Boolean.toString(blocked.skyLightAt(6, 7, 4) == 14),
                Boolean.toString(blocked.blockLightAt(15, 2, 3) == 15),
                Boolean.toString(blocked.blockLightAt(16, 2, 3) == 14),
                Boolean.toString(blocked.blockLightAt(14, 2, 3) == 0 && blocked.blockLightAt(13, 2, 3) == 0),
                Boolean.toString(unblocked.blockLightAt(14, 2, 3) == 14
                        && unblocked.blockLightAt(13, 2, 3) == 13)
        );
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
