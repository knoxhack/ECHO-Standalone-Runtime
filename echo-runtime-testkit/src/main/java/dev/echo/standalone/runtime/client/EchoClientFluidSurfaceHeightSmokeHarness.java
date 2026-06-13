package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelMeshDirection;
import dev.echo.standalone.runtime.render.EchoVoxelMeshFace;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoClientFluidSurfaceHeightSmokeHarness {
    private static final Path REPORT_PATH =
            Path.of("reports", "echo", "standalone", "world-fluid-surface-height.json");
    private static final int FLOATS_PER_VERTEX = 12;

    private EchoClientFluidSurfaceHeightSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        FluidSurfaceHeightResult result = requireFluidSurfaceHeights();
        writeReport(result);
        System.out.println("client fluid surface height smoke PASS source="
                + format(result.sourceHeight())
                + " flow="
                + format(result.flowHeight())
                + " maxLevel="
                + format(result.maxLevelHeight())
                + " falling="
                + format(result.fallingHeight())
                + " vertices=" + result.vertexCount());
    }

    private static FluidSurfaceHeightResult requireFluidSurfaceHeights() {
        EchoVoxelWorld world = surfaceHeightWorld();
        EchoVoxelChunkMesh mesh = new EchoVoxelChunkMesher().buildChunkMesh(world, new EchoVoxelChunkId(0, 0, 0));
        require(mesh.faceCount() > 0, "Fluid surface-height fixture should emit mesh faces");

        EchoVoxelBlockState sourceState = world.blockStateAt(2, 1, 2);
        EchoVoxelBlockState flowState = world.blockStateAt(4, 1, 2);
        EchoVoxelBlockState maxLevelState = world.blockStateAt(6, 1, 2);
        EchoVoxelBlockState fallingState = world.blockStateAt(8, 1, 2);
        double sourceHeight = EchoVoxelFluidRuntime.fluidSurfaceHeight(sourceState);
        double flowHeight = EchoVoxelFluidRuntime.fluidSurfaceHeight(flowState);
        double maxLevelHeight = EchoVoxelFluidRuntime.fluidSurfaceHeight(maxLevelState);
        double fallingHeight = EchoVoxelFluidRuntime.fluidSurfaceHeight(fallingState);

        require(sourceHeight > flowHeight,
                "Source fluid should render with a higher surface than level-3 flowing water");
        require(flowHeight > maxLevelHeight,
                "Level-3 flowing water should render higher than max-level water");
        require(close(fallingHeight, 1.0D),
                "Falling fluid should render as a full-height vertical column");

        EchoVoxelMeshFace sourceUp = requireFace(mesh, 2, 1, 2, EchoVoxelMeshDirection.UP);
        EchoVoxelMeshFace flowUp = requireFace(mesh, 4, 1, 2, EchoVoxelMeshDirection.UP);
        EchoVoxelMeshFace flowEast = requireFace(mesh, 4, 1, 2, EchoVoxelMeshDirection.EAST);
        EchoVoxelMeshFace maxLevelUp = requireFace(mesh, 6, 1, 2, EchoVoxelMeshDirection.UP);
        EchoVoxelMeshFace fallingUp = requireFace(mesh, 8, 1, 2, EchoVoxelMeshDirection.UP);
        EchoVoxelMeshFace solidUp = requireFace(mesh, 10, 1, 2, EchoVoxelMeshDirection.UP);

        require(close(sourceUp.maxY(), sourceHeight),
                "Source top face should use fluid surface height");
        require(close(flowUp.maxY(), flowHeight),
                "Flowing top face should use fluid surface height");
        require(close(flowEast.maxY(), flowHeight) && close(flowEast.minY(), 0.0D),
                "Flowing side face should stop at the same surface height");
        require(close(maxLevelUp.maxY(), maxLevelHeight),
                "Max-level fluid top face should use the shallow surface height");
        require(close(fallingUp.maxY(), 1.0D),
                "Falling fluid top face should remain full height");
        require(solidUp.fullCubeBounds(),
                "Non-fluid block faces should keep full-cube bounds");

        EchoClientChunkMesh.MeshData meshData = EchoClientChunkMesh.meshData(mesh, null);
        require(meshData.vertexCount() == mesh.faceCount() * 4,
                "Client OpenGL mesh data should retain one quad per bounded mesh face");
        require(meshData.indexCount() == mesh.faceCount() * 6,
                "Client OpenGL mesh data should retain indexed triangles per bounded mesh face");
        requireTopFaceVertexHeight(mesh, meshData, flowUp, 1.0D + flowHeight);
        requireSideFaceVertexHeights(mesh, meshData, flowEast, 1.0D, 1.0D + flowHeight);
        requireTopFaceVertexHeight(mesh, meshData, fallingUp, 2.0D);

        EchoVoxelCamera camera = new EchoVoxelCamera(4.5D, 4.2D, -5.5D, 0.0D, -18.0D, 70.0D);
        EchoVoxelFramebuffer framebuffer = new EchoVoxelSoftwareRenderer().render(world, camera, 320, 180);
        require(framebuffer.facesDrawn() > 0 && framebuffer.checksum() != 0L,
                "Software compatibility renderer should draw the bounded fluid mesh");

        return new FluidSurfaceHeightResult(
                sourceHeight,
                flowHeight,
                maxLevelHeight,
                fallingHeight,
                mesh.faceCount(),
                meshData.vertexCount(),
                meshData.indexCount(),
                framebuffer.facesDrawn(),
                framebuffer.checksum()
        );
    }

    private static EchoVoxelWorld surfaceHeightWorld() {
        EchoVoxelBlock solid = new EchoVoxelBlock(
                "echotest:surface_height_solid",
                "Surface Height Solid",
                0xFF4E5963,
                true,
                true,
                2.0D
        );
        EchoVoxelChunk origin = new EchoVoxelChunk(new EchoVoxelChunkId(0, 0, 0), 16);
        origin.setStateLocal(2, 1, 2, waterState(0, false, true));
        origin.setStateLocal(4, 1, 2, waterState(3, false, false));
        origin.setStateLocal(6, 1, 2, waterState(7, false, false));
        origin.setStateLocal(8, 1, 2, waterState(3, true, false));
        origin.setBlockLocal(10, 1, 2, solid);
        return new EchoVoxelWorld(
                "test:fluid_surface_height",
                20260610L,
                16,
                List.of(origin),
                4.5D,
                2.0D,
                2.5D,
                0.0D
        );
    }

    private static EchoVoxelBlockState waterState(int level, boolean falling, boolean source) {
        EchoVoxelBlockState state = EchoVoxelBlockState.of(EchoVoxelFluidRuntime.WATER)
                .withProperty("fluid", "water")
                .withProperty("fluidLevel", Integer.toString(level))
                .withProperty("fluidFalling", Boolean.toString(falling))
                .withProperty("fluidTick", "77");
        return source ? state.withProperty("fluidSource", "true") : state;
    }

    private static EchoVoxelMeshFace requireFace(
            EchoVoxelChunkMesh mesh,
            int x,
            int y,
            int z,
            EchoVoxelMeshDirection direction
    ) {
        return mesh.faces().stream()
                .filter(face -> face.x() == x
                        && face.y() == y
                        && face.z() == z
                        && face.direction() == direction)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing mesh face " + x + "," + y + "," + z
                        + " " + direction));
    }

    private static void requireTopFaceVertexHeight(
            EchoVoxelChunkMesh mesh,
            EchoClientChunkMesh.MeshData meshData,
            EchoVoxelMeshFace face,
            double expectedY
    ) {
        float[] vertices = meshData.vertices();
        int faceIndex = mesh.faces().indexOf(face);
        require(faceIndex >= 0, "Face should be present in mesh before vertex inspection");
        int base = faceIndex * 4 * FLOATS_PER_VERTEX;
        for (int vertex = 0; vertex < 4; vertex++) {
            double actualY = vertices[base + vertex * FLOATS_PER_VERTEX + 1];
            require(close(actualY, expectedY),
                    "Top face OpenGL vertices should use bounded fluid surface height");
        }
    }

    private static void requireSideFaceVertexHeights(
            EchoVoxelChunkMesh mesh,
            EchoClientChunkMesh.MeshData meshData,
            EchoVoxelMeshFace face,
            double expectedMinY,
            double expectedMaxY
    ) {
        float[] vertices = meshData.vertices();
        int faceIndex = mesh.faces().indexOf(face);
        require(faceIndex >= 0, "Face should be present in mesh before side vertex inspection");
        int base = faceIndex * 4 * FLOATS_PER_VERTEX;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (int vertex = 0; vertex < 4; vertex++) {
            double actualY = vertices[base + vertex * FLOATS_PER_VERTEX + 1];
            minY = Math.min(minY, actualY);
            maxY = Math.max(maxY, actualY);
        }
        require(close(minY, expectedMinY) && close(maxY, expectedMaxY),
                "Side face OpenGL vertices should span from block bottom to bounded surface height");
    }

    private static void writeReport(FluidSurfaceHeightResult result) throws IOException {
        Files.createDirectories(REPORT_PATH.getParent());
        String json = """
                {
                  "schema": "echo.standalone.client_fluid_surface_height_smoke.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientFluidSurfaceHeightSmokeHarness",
                  "status": "PASS",
                  "summary": "Fluid render mesh faces and OpenGL upload vertices use fluidLevel metadata for bounded surface heights while falling fluids remain full-height columns.",
                  "heights": {
                    "source": %s,
                    "flowLevel3": %s,
                    "maxLevel7": %s,
                    "falling": %s
                  },
                  "mesh": {
                    "faces": %d,
                    "vertices": %d,
                    "indices": %d,
                    "softwareFacesDrawn": %d,
                    "softwareChecksum": "%s"
                  },
                  "evidence": {
                    "nativeModLoaderCommandUsed": false,
                    "fluidLevelSurfaceHeight": true,
                    "sourceHigherThanFlow": true,
                    "fallingFluidFullHeight": true,
                    "openglVertexBounds": true,
                    "softwareRendererBounds": true,
                    "nonFluidFullCubePreserved": true
                  }
                }
                """.formatted(
                format(result.sourceHeight()),
                format(result.flowHeight()),
                format(result.maxLevelHeight()),
                format(result.fallingHeight()),
                result.faceCount(),
                result.vertexCount(),
                result.indexCount(),
                result.softwareFacesDrawn(),
                Long.toUnsignedString(result.softwareChecksum())
        );
        Files.writeString(REPORT_PATH, json);
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.0001D;
    }

    private static String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.6f", value);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private record FluidSurfaceHeightResult(
            double sourceHeight,
            double flowHeight,
            double maxLevelHeight,
            double fallingHeight,
            int faceCount,
            int vertexCount,
            int indexCount,
            int softwareFacesDrawn,
            long softwareChecksum
    ) {
    }
}
