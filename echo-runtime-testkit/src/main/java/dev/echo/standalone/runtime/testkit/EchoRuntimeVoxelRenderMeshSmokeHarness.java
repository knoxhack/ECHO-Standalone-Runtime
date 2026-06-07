package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelRenderBackendTarget;
import dev.echo.standalone.runtime.render.EchoVoxelRenderPacket;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;

import java.util.List;

public final class EchoRuntimeVoxelRenderMeshSmokeHarness {
    private EchoRuntimeVoxelRenderMeshSmokeHarness() {
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
        world = sessionProfile.streamer().streamAround(world, world.spawnX(), world.spawnZ());
        EchoVoxelCamera camera = new EchoVoxelCamera(
                world.spawnX(),
                world.spawnY() + 1.2D,
                world.spawnZ(),
                world.spawnYawDegrees(),
                -22.0D,
                70.0D
        );

        EchoVoxelChunkMesher mesher = new EchoVoxelChunkMesher();
        EchoVoxelRenderPacket packet = mesher.buildPacket(world, camera);
        EchoVoxelRenderPacket repeat = new EchoVoxelChunkMesher().buildPacket(world, camera);
        require(packet.target() == EchoVoxelRenderBackendTarget.OPENGL,
                "voxel mesh packet should target the OpenGL live client");
        require(packet.visibleChunkCount() == world.loadedChunkCount(),
                "default mesher should keep the streamed 3x3 region visible");
        require(packet.sourceBlockCount() == world.nonAirBlocks().size(),
                "mesh packet should account for every source block");
        require(packet.faceCount() > 0,
                "block meshing should emit visible faces");
        require(packet.faceCount() < packet.sourceBlockCount() * 6,
                "block meshing should omit buried neighbor faces");
        require(packet.vertexCount() == packet.faceCount() * 4,
                "mesh packet should expose renderer-ready vertex count");
        require(packet.indexCount() == packet.faceCount() * 6,
                "mesh packet should expose indexed-triangle count");
        require(packet.materialCount() >= 6,
                "mesh packet should preserve distinct block material ids");
        require(packet.materialAtlasKeys().contains("echoashfallprotocol/block/fallout_dust"),
                "mesh packet should carry AdapterCore atlas keys for Ashfall blocks");
        require(packet.materialPatternCount() >= 6,
                "mesh packet should preserve material pattern metadata");
        require(packet.patternedFaceCount() > 400,
                "mesh packet should preserve patterned material faces for the renderer");
        require(packet.faceCount() == repeat.faceCount()
                        && packet.vertexCount() == repeat.vertexCount()
                        && packet.indexCount() == repeat.indexCount()
                        && packet.materialCount() == repeat.materialCount()
                        && packet.materialPatternCount() == repeat.materialPatternCount(),
                "meshing should be deterministic for the same world and camera");
        EchoVoxelChunkMesh packetChunkMesh = packet.chunkMeshes().get(0);
        EchoVoxelChunkMesh singleChunkMesh = mesher.buildChunkMesh(world, packetChunkMesh.chunkId());
        require(singleChunkMesh.faceCount() == packetChunkMesh.faceCount()
                        && singleChunkMesh.vertexCount() == packetChunkMesh.vertexCount()
                        && singleChunkMesh.indexCount() == packetChunkMesh.indexCount()
                        && singleChunkMesh.sourceBlockCount() == packetChunkMesh.sourceBlockCount()
                        && singleChunkMesh.materialAtlasKeys().equals(packetChunkMesh.materialAtlasKeys())
                        && singleChunkMesh.materialPatterns().equals(packetChunkMesh.materialPatterns()),
                "chunk-local meshing should match the full packet for the same loaded chunk");
        EchoVoxelChunkMesh missingChunkMesh =
                mesher.buildChunkMesh(world, new EchoVoxelChunkId(99_999, 99_999, 99_999));
        require(missingChunkMesh.faceCount() == 0 && missingChunkMesh.sourceBlockCount() == 0,
                "chunk-local meshing should return an empty mesh for unloaded chunks");
        requireIndexedCrossChunkNeighborCulling(mesher);

        EchoVoxelRenderPacket culled = new EchoVoxelChunkMesher(EchoVoxelRenderBackendTarget.OPENGL, 0.5D)
                .buildPacket(world, camera);
        require(culled.visibleChunkCount() < packet.visibleChunkCount(),
                "tight culling should drop distant streamed chunks");
        require(culled.culledChunkCount() > 0,
                "mesh packet should report culled chunks");

        EchoVoxelFramebuffer framebuffer = new EchoVoxelSoftwareRenderer().render(world, camera, 640, 360);
        require(framebuffer.blocksVisited() == packet.sourceBlockCount(),
                "software compatibility renderer should consume the mesh source block count");
        require(framebuffer.facesDrawn() > 0,
                "software compatibility renderer should draw mesh faces");
        require(framebuffer.checksum() != 0L,
                "software compatibility framebuffer checksum should be non-zero");
        require(framebuffer.uniqueColorCount() > 48,
                "software compatibility framebuffer should include material pattern color variation");

        System.out.println("phase15.voxel render mesh smoke PASS target="
                + packet.target().id()
                + " chunks="
                + packet.visibleChunkCount()
                + " culled="
                + culled.culledChunkCount()
                + " blocks="
                + packet.sourceBlockCount()
                + " faces="
                + packet.faceCount()
                + " vertices="
                + packet.vertexCount()
                + " materials="
                + packet.materialCount()
                + " patterns="
                + packet.materialPatternCount()
                + " atlasKeys="
                + packet.materialAtlasKeys().size()
                + " checksum="
                + Long.toUnsignedString(framebuffer.checksum()));
    }

    private static void requireIndexedCrossChunkNeighborCulling(EchoVoxelChunkMesher mesher) {
        EchoVoxelBlock block = new EchoVoxelBlock(
                "echotest:mesher_boundary",
                "Mesher Boundary",
                0xFF7BB7A4,
                true,
                true,
                1.0D
        );
        EchoVoxelChunk left = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        EchoVoxelChunk right = new EchoVoxelChunk(new EchoVoxelChunkId(1, 0, 0), 16);
        left.setStateLocal(15, 0, 0, EchoVoxelBlockState.of(block));
        right.setStateLocal(0, 0, 0, EchoVoxelBlockState.of(block));
        EchoVoxelWorld world = new EchoVoxelWorld(
                "mesher-cross-chunk-neighbor",
                7L,
                16,
                List.of(left, right),
                15.5D,
                2.0D,
                0.5D,
                0.0D
        );
        EchoVoxelCamera camera = new EchoVoxelCamera(15.5D, 2.0D, 0.5D, 90.0D, -15.0D, 70.0D);
        EchoVoxelRenderPacket packet = mesher.buildPacket(world, camera, 64.0D);
        require(packet.sourceBlockCount() == 2,
                "Cross-chunk mesher fixture should contain two source blocks");
        require(packet.faceCount() == 10,
                "Indexed mesher should cull the two hidden faces across a chunk boundary");
        EchoVoxelChunkMesh leftMesh = mesher.buildChunkMesh(world, left.id());
        EchoVoxelChunkMesh rightMesh = mesher.buildChunkMesh(world, right.id());
        require(leftMesh.faceCount() == 5 && rightMesh.faceCount() == 5,
                "Chunk-local meshing should use indexed neighbor chunks for boundary culling");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
