package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoBlockTextureResolver.EchoBlockModelBounds;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import dev.echo.standalone.runtime.render.EchoVoxelMeshMaterial;
import dev.echo.standalone.runtime.render.EchoVoxelRenderBackendTarget;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.List;

public final class EchoClientMeshGeometrySmokeHarness {
    private EchoClientMeshGeometrySmokeHarness() {
    }

    public static void main(String[] args) {
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            float[] corners = EchoClientChunkMesh.cornerOffsets(direction);
            require(corners.length == 12, direction + " should have four xyz corners");
            requireUnitCoordinates(direction, corners);
            requireDirectionPlane(direction, corners);
            requireOutwardWinding(direction, corners);
        }
        EchoBlockModelBounds insetBounds = new EchoBlockModelBounds(2.0D, 0.0D, 4.0D, 14.0D, 8.0D, 12.0D);
        requireBoundedDirectionPlane(EchoVoxelMeshDirection.UP,
                EchoClientChunkMesh.cornerOffsets(EchoVoxelMeshDirection.UP, insetBounds), 0.5f);
        requireBoundedDirectionPlane(EchoVoxelMeshDirection.EAST,
                EchoClientChunkMesh.cornerOffsets(EchoVoxelMeshDirection.EAST, insetBounds), 0.875f);
        requireBoundedDirectionPlane(EchoVoxelMeshDirection.NORTH,
                EchoClientChunkMesh.cornerOffsets(EchoVoxelMeshDirection.NORTH, insetBounds), 0.25f);
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            requireOutwardWinding(direction, EchoClientChunkMesh.cornerOffsets(direction, insetBounds));
        }
        requireChunkMeshSetSourceSkipsEquivalentUploads();
        requireChunkUploadFrameBudgetCapsDirtyWork();
        requireChunkMeshUploadBufferPlanReusesCapacity();
        System.out.println("client mesh geometry smoke PASS directions=" + EchoVoxelMeshDirection.values().length);
    }

    private static void requireChunkMeshSetSourceSkipsEquivalentUploads() {
        EchoVoxelChunkId chunkId = new EchoVoxelChunkId(0, 0, 0);
        EchoVoxelMeshMaterial material = new EchoVoxelMeshMaterial(
                "voxel:block/echotest:mesh_reuse",
                "echotest/block/mesh_reuse",
                0xFF66AA88,
                0xFF88CCAA,
                EchoVoxelMaterialPattern.FLAT,
                true
        );
        EchoVoxelChunkMesh first = chunkMesh(chunkId, material, EchoVoxelMeshDirection.UP);
        EchoVoxelChunkMesh equivalent = chunkMesh(chunkId, material, EchoVoxelMeshDirection.UP);
        EchoVoxelChunkMesh changed = chunkMesh(chunkId, material, EchoVoxelMeshDirection.NORTH);
        EchoClientChunkMesh gpuMesh = new EchoClientChunkMesh(chunkId);
        require(gpuMesh.setSource(first),
                "First chunk mesh source should require a GPU upload");
        require(!gpuMesh.setSource(equivalent),
                "Equivalent chunk mesh source should not require another GPU upload");
        require(gpuMesh.setSource(changed),
                "Changed chunk mesh source should still require a GPU upload");
    }

    private static void requireChunkUploadFrameBudgetCapsDirtyWork() {
        EchoClientRenderer.ChunkUploadFrameBudget budget =
                new EchoClientRenderer.ChunkUploadFrameBudget(3);
        require(budget.tryAcquireUploadSlot(), "First dirty chunk should acquire an upload slot");
        require(budget.tryAcquireUploadSlot(), "Second dirty chunk should acquire an upload slot");
        require(budget.tryAcquireUploadSlot(), "Third dirty chunk should acquire an upload slot");
        require(!budget.tryAcquireUploadSlot(), "Fourth dirty chunk should be deferred by the frame budget");
        require(!budget.tryAcquireUploadSlot(), "Fifth dirty chunk should also remain pending");
        require(budget.limit() == 3, "Chunk upload frame budget should expose the configured limit");
        require(budget.uploadCount() == 3, "Chunk upload frame budget should cap uploads");
        require(budget.pendingCount() == 2, "Chunk upload frame budget should count deferred dirty chunks");

        EchoClientRenderer.ChunkUploadFrameBudget zeroBudget =
                new EchoClientRenderer.ChunkUploadFrameBudget(0);
        require(!zeroBudget.tryAcquireUploadSlot(),
                "Zero chunk upload budget should defer dirty chunks instead of uploading");
        require(zeroBudget.uploadCount() == 0 && zeroBudget.pendingCount() == 1,
                "Zero chunk upload budget should report one pending dirty chunk");
    }

    private static void requireChunkMeshUploadBufferPlanReusesCapacity() {
        EchoClientChunkMesh.DynamicBufferUploadPlan first =
                EchoClientChunkMesh.dynamicBufferUploadPlan(0, 192);
        require(first.grow(), "First chunk mesh upload should allocate GPU buffer capacity");
        require(first.requestedBytes() == 192,
                "Chunk mesh upload plan should preserve requested byte count");
        require(first.capacityBytes() >= 1024,
                "Chunk mesh upload plan should allocate a reusable minimum capacity");

        EchoClientChunkMesh.DynamicBufferUploadPlan reused =
                EchoClientChunkMesh.dynamicBufferUploadPlan(first.capacityBytes(), 256);
        require(!reused.grow(),
                "Chunk mesh upload plan should reuse existing capacity for smaller updates");
        require(reused.capacityBytes() == first.capacityBytes(),
                "Chunk mesh upload plan should keep existing capacity when it fits");

        EchoClientChunkMesh.DynamicBufferUploadPlan grown =
                EchoClientChunkMesh.dynamicBufferUploadPlan(reused.capacityBytes(), reused.capacityBytes() + 1);
        require(grown.grow() && grown.capacityBytes() >= reused.capacityBytes() + 1,
                "Chunk mesh upload plan should grow only when requested bytes exceed capacity");
    }

    private static EchoVoxelChunkMesh chunkMesh(
            EchoVoxelChunkId chunkId,
            EchoVoxelMeshMaterial material,
            EchoVoxelMeshDirection direction
    ) {
        return new EchoVoxelChunkMesh(
                chunkId,
                EchoVoxelRenderBackendTarget.OPENGL,
                List.of(new EchoVoxelMeshFace(0, 0, 0, direction, material)),
                1
        );
    }

    private static void requireUnitCoordinates(EchoVoxelMeshDirection direction, float[] corners) {
        for (float corner : corners) {
            require(corner == 0.0f || corner == 1.0f,
                    direction + " corner coordinates should stay on the unit cube");
        }
    }

    private static void requireDirectionPlane(EchoVoxelMeshDirection direction, float[] corners) {
        int axis = direction.normalX() != 0 ? 0 : direction.normalY() != 0 ? 1 : 2;
        float expected = axisValue(direction) > 0 ? 1.0f : 0.0f;
        for (int i = axis; i < corners.length; i += 3) {
            require(corners[i] == expected,
                    direction + " face should be emitted on its outward cube plane");
        }
    }

    private static int axisValue(EchoVoxelMeshDirection direction) {
        if (direction.normalX() != 0) {
            return direction.normalX();
        }
        if (direction.normalY() != 0) {
            return direction.normalY();
        }
        return direction.normalZ();
    }

    private static void requireBoundedDirectionPlane(
            EchoVoxelMeshDirection direction,
            float[] corners,
            float expectedPlane
    ) {
        int axis = direction.normalX() != 0 ? 0 : direction.normalY() != 0 ? 1 : 2;
        for (int i = axis; i < corners.length; i += 3) {
            require(corners[i] == expectedPlane,
                    direction + " bounded face should be emitted on the JSON model element plane");
        }
        for (float corner : corners) {
            require(corner >= 0.0f && corner <= 1.0f,
                    direction + " bounded model coordinates should stay normalized");
        }
    }

    private static void requireOutwardWinding(EchoVoxelMeshDirection direction, float[] c) {
        float ax = c[3] - c[0];
        float ay = c[4] - c[1];
        float az = c[5] - c[2];
        float bx = c[6] - c[0];
        float by = c[7] - c[1];
        float bz = c[8] - c[2];
        float crossX = ay * bz - az * by;
        float crossY = az * bx - ax * bz;
        float crossZ = ax * by - ay * bx;
        float dot = crossX * direction.normalX()
                + crossY * direction.normalY()
                + crossZ * direction.normalZ();
        require(dot > 0.0f, direction + " triangle winding should face outward for back-face culling");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
