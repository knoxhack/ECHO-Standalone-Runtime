package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

final class EchoClientBlockOutlineRenderer {
    private static final float OUTLINE_EXPAND = 0.003f;
    private static final int[] OUTLINE_INDICES = {
            0, 1, 1, 2, 2, 3, 3, 0,
            4, 5, 5, 6, 6, 7, 7, 4,
            0, 4, 1, 5, 2, 6, 3, 7
    };

    private final EchoClientShader shader;
    private final int uProjection;
    private final int uView;
    private final int uColor;

    private int vao;
    private int vbo;
    private int ibo;
    private int crackVao;
    private int crackVbo;
    private int crackVertexCount;
    private int uploadedX = Integer.MIN_VALUE;
    private int uploadedY = Integer.MIN_VALUE;
    private int uploadedZ = Integer.MIN_VALUE;
    private int uploadedCrackX = Integer.MIN_VALUE;
    private int uploadedCrackY = Integer.MIN_VALUE;
    private int uploadedCrackZ = Integer.MIN_VALUE;
    private int uploadedCrackStage = Integer.MIN_VALUE;

    private static final float[][] CRACK_PATTERN = {
            {0.50f, 0.50f, 0.63f, 0.43f},
            {0.50f, 0.50f, 0.39f, 0.35f},
            {0.50f, 0.50f, 0.55f, 0.68f},
            {0.63f, 0.43f, 0.78f, 0.34f},
            {0.39f, 0.35f, 0.25f, 0.23f},
            {0.55f, 0.68f, 0.48f, 0.84f},
            {0.50f, 0.50f, 0.30f, 0.56f},
            {0.30f, 0.56f, 0.16f, 0.66f},
            {0.50f, 0.50f, 0.70f, 0.62f},
            {0.70f, 0.62f, 0.86f, 0.74f}
    };

    EchoClientBlockOutlineRenderer() {
        shader = new EchoClientShader("/shaders/outline.vert", "/shaders/outline.frag");
        shader.use();
        uProjection = shader.uniform("uProjection");
        uView = shader.uniform("uView");
        uColor = shader.uniform("uColor");
        GL20.glUseProgram(0);
    }

    void render(
            EchoVoxelCamera camera,
            EchoVoxelHit target,
            float[] projectionMatrix,
            float[] viewMatrix,
            double breakProgress
    ) {
        if (target == null || target.block().air()) {
            return;
        }
        uploadIfNeeded(target.x(), target.y(), target.z());
        uploadCracksIfNeeded(target.x(), target.y(), target.z(), crackStage(breakProgress));

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0f);

        shader.use();
        shader.setMat4(uProjection, projectionMatrix);
        shader.setMat4(uView, viewMatrix);
        shader.setVec4(uColor, 0.58f, 1.0f, 0.92f, 0.92f);

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_LINES, OUTLINE_INDICES.length, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        if (crackVertexCount > 0) {
            GL30.glBindVertexArray(crackVao);
            GL11.glLineWidth(4.0f);
            shader.setVec4(uColor, 0.02f, 0.02f, 0.02f, 0.78f);
            GL11.glDrawArrays(GL11.GL_LINES, 0, crackVertexCount);
            GL11.glLineWidth(2.0f);
            shader.setVec4(uColor, 0.88f, 1.0f, 0.94f, 0.52f);
            GL11.glDrawArrays(GL11.GL_LINES, 0, crackVertexCount);
            GL30.glBindVertexArray(0);
        }

        GL11.glLineWidth(1.0f);
        GL11.glDisable(GL11.GL_BLEND);
        GL20.glUseProgram(0);
    }

    void delete() {
        if (vao != 0) {
            GL30.glDeleteVertexArrays(vao);
            GL15.glDeleteBuffers(vbo);
            GL15.glDeleteBuffers(ibo);
            vao = 0;
            vbo = 0;
            ibo = 0;
        }
        if (crackVao != 0) {
            GL30.glDeleteVertexArrays(crackVao);
            GL15.glDeleteBuffers(crackVbo);
            crackVao = 0;
            crackVbo = 0;
        }
        shader.delete();
    }

    private void uploadIfNeeded(int x, int y, int z) {
        if (vao != 0 && x == uploadedX && y == uploadedY && z == uploadedZ) {
            return;
        }
        uploadedX = x;
        uploadedY = y;
        uploadedZ = z;

        if (vao == 0) {
            vao = GL30.glGenVertexArrays();
            vbo = GL15.glGenBuffers();
            ibo = GL15.glGenBuffers();
        }

        GL30.glBindVertexArray(vao);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        float[] vertices = outlineVertices(x, y, z);
        FloatBuffer vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        IntBuffer indexBuffer = java.nio.ByteBuffer.allocateDirect(OUTLINE_INDICES.length * Integer.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asIntBuffer();
        indexBuffer.put(OUTLINE_INDICES).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);

        GL30.glBindVertexArray(0);
    }

    private void uploadCracksIfNeeded(int x, int y, int z, int stage) {
        if (crackVao != 0
                && x == uploadedCrackX
                && y == uploadedCrackY
                && z == uploadedCrackZ
                && stage == uploadedCrackStage) {
            return;
        }
        uploadedCrackX = x;
        uploadedCrackY = y;
        uploadedCrackZ = z;
        uploadedCrackStage = stage;

        if (stage <= 0) {
            crackVertexCount = 0;
            return;
        }

        if (crackVao == 0) {
            crackVao = GL30.glGenVertexArrays();
            crackVbo = GL15.glGenBuffers();
        }

        float[] vertices = crackVertices(x, y, z, stage);
        crackVertexCount = vertices.length / 3;
        FloatBuffer vertexBuffer = java.nio.ByteBuffer.allocateDirect(vertices.length * Float.BYTES)
                .order(java.nio.ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).flip();

        GL30.glBindVertexArray(crackVao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, crackVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_DYNAMIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL30.glBindVertexArray(0);
    }

    static float[] outlineVertices(int x, int y, int z) {
        float minX = x - OUTLINE_EXPAND;
        float minY = y - OUTLINE_EXPAND;
        float minZ = z - OUTLINE_EXPAND;
        float maxX = x + 1.0f + OUTLINE_EXPAND;
        float maxY = y + 1.0f + OUTLINE_EXPAND;
        float maxZ = z + 1.0f + OUTLINE_EXPAND;
        return new float[] {
                minX, minY, minZ,
                maxX, minY, minZ,
                maxX, maxY, minZ,
                minX, maxY, minZ,
                minX, minY, maxZ,
                maxX, minY, maxZ,
                maxX, maxY, maxZ,
                minX, maxY, maxZ
        };
    }

    static int[] outlineIndices() {
        return OUTLINE_INDICES.clone();
    }

    static float[] crackVertices(int x, int y, int z, double progress) {
        return crackVertices(x, y, z, crackStage(progress));
    }

    static int crackStage(double progress) {
        if (!Double.isFinite(progress) || progress <= 0.0D) {
            return 0;
        }
        return Math.max(1, Math.min(CRACK_PATTERN.length, (int) Math.ceil(progress * CRACK_PATTERN.length)));
    }

    private static float[] crackVertices(int x, int y, int z, int stage) {
        if (stage <= 0) {
            return new float[0];
        }
        int segments = Math.min(stage, CRACK_PATTERN.length);
        float[] vertices = new float[6 * segments * 2 * 3];
        int offset = 0;
        for (int face = 0; face < 6; face++) {
            for (int segment = 0; segment < segments; segment++) {
                float[] line = CRACK_PATTERN[segment];
                offset = crackPoint(vertices, offset, x, y, z, face, line[0], line[1]);
                offset = crackPoint(vertices, offset, x, y, z, face, line[2], line[3]);
            }
        }
        return vertices;
    }

    private static int crackPoint(float[] vertices, int offset, int x, int y, int z, int face, float u, float v) {
        float minX = x - OUTLINE_EXPAND * 2.0f;
        float minY = y - OUTLINE_EXPAND * 2.0f;
        float minZ = z - OUTLINE_EXPAND * 2.0f;
        float maxX = x + 1.0f + OUTLINE_EXPAND * 2.0f;
        float maxY = y + 1.0f + OUTLINE_EXPAND * 2.0f;
        float maxZ = z + 1.0f + OUTLINE_EXPAND * 2.0f;
        switch (face) {
            case 0 -> {
                vertices[offset++] = minX + u;
                vertices[offset++] = maxY;
                vertices[offset++] = minZ + v;
            }
            case 1 -> {
                vertices[offset++] = minX + u;
                vertices[offset++] = minY;
                vertices[offset++] = minZ + v;
            }
            case 2 -> {
                vertices[offset++] = maxX;
                vertices[offset++] = minY + v;
                vertices[offset++] = minZ + u;
            }
            case 3 -> {
                vertices[offset++] = minX;
                vertices[offset++] = minY + v;
                vertices[offset++] = minZ + u;
            }
            case 4 -> {
                vertices[offset++] = minX + u;
                vertices[offset++] = minY + v;
                vertices[offset++] = maxZ;
            }
            default -> {
                vertices[offset++] = minX + u;
                vertices[offset++] = minY + v;
                vertices[offset++] = minZ;
            }
        }
        return offset;
    }
}
