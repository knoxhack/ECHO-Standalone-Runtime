package dev.echo.standalone.runtime.client;

public final class EchoClientBlockOutlineSmokeHarness {
    private EchoClientBlockOutlineSmokeHarness() {
    }

    public static void main(String[] args) {
        float[] vertices = EchoClientBlockOutlineRenderer.outlineVertices(2, 4, -3);
        int[] indices = EchoClientBlockOutlineRenderer.outlineIndices();

        require(vertices.length == 24, "Outline should emit eight xyz vertices");
        require(indices.length == 24, "Outline should emit twelve GL_LINES edges");
        requireUniqueEdges(indices);
        requireExpandedBounds(vertices, 2, 4, -3);
        requireAllIndicesInRange(indices, 8);
        requireCrackStagesGrow();
        requireCrackVerticesStayOnExpandedFaces();

        System.out.println("client block outline smoke PASS vertices=8 edges=12 crackStages=10");
    }

    private static void requireUniqueEdges(int[] indices) {
        java.util.HashSet<String> edges = new java.util.HashSet<>();
        for (int i = 0; i < indices.length; i += 2) {
            int a = Math.min(indices[i], indices[i + 1]);
            int b = Math.max(indices[i], indices[i + 1]);
            require(a != b, "Outline edge should not point to the same vertex");
            require(edges.add(a + ":" + b), "Outline should not repeat an edge " + a + ":" + b);
        }
        require(edges.size() == 12, "Outline should describe twelve unique cube edges");
    }

    private static void requireExpandedBounds(float[] vertices, int x, int y, int z) {
        float minX = Float.POSITIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < vertices.length; i += 3) {
            minX = Math.min(minX, vertices[i]);
            minY = Math.min(minY, vertices[i + 1]);
            minZ = Math.min(minZ, vertices[i + 2]);
            maxX = Math.max(maxX, vertices[i]);
            maxY = Math.max(maxY, vertices[i + 1]);
            maxZ = Math.max(maxZ, vertices[i + 2]);
        }
        require(minX < x && minY < y && minZ < z,
                "Outline should expand slightly below the target block bounds");
        require(maxX > x + 1.0f && maxY > y + 1.0f && maxZ > z + 1.0f,
                "Outline should expand slightly above the target block bounds");
    }

    private static void requireAllIndicesInRange(int[] indices, int vertexCount) {
        for (int index : indices) {
            require(index >= 0 && index < vertexCount, "Outline index out of range: " + index);
        }
    }

    private static void requireCrackStagesGrow() {
        require(EchoClientBlockOutlineRenderer.crackVertices(0, 0, 0, 0.0D).length == 0,
                "No break progress should not emit cracks");
        float[] early = EchoClientBlockOutlineRenderer.crackVertices(0, 0, 0, 0.1D);
        float[] late = EchoClientBlockOutlineRenderer.crackVertices(0, 0, 0, 0.9D);
        require(early.length > 0, "Early break progress should emit crack lines");
        require(late.length > early.length, "Later break progress should emit more crack lines");
        require(EchoClientBlockOutlineRenderer.crackStage(1.5D) == 10,
                "Crack stage should clamp at the final stage");
    }

    private static void requireCrackVerticesStayOnExpandedFaces() {
        float[] cracks = EchoClientBlockOutlineRenderer.crackVertices(2, 4, -3, 1.0D);
        require(cracks.length == 6 * 10 * 2 * 3, "Final crack stage should cover six faces");
        for (int i = 0; i < cracks.length; i += 3) {
            boolean onXFace = close(cracks[i], 1.994f) || close(cracks[i], 3.006f);
            boolean onYFace = close(cracks[i + 1], 3.994f) || close(cracks[i + 1], 5.006f);
            boolean onZFace = close(cracks[i + 2], -3.006f) || close(cracks[i + 2], -1.994f);
            require(onXFace || onYFace || onZFace, "Crack vertex should sit on an expanded cube face");
        }
    }

    private static boolean close(float actual, float expected) {
        return Math.abs(actual - expected) <= 0.0001f;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
