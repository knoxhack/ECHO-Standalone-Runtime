package dev.echo.standalone.runtime.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

final class EchoClientHudTextureRenderer {
    private static final int STRIDE = 8 * Float.BYTES;
    private static final int QUAD_FLOATS = 6 * 8;
    private static final int QUAD_BYTES = QUAD_FLOATS * Float.BYTES;

    private final EchoClientShader shader;
    private final int vao;
    private final int vbo;
    private final int uScreen;
    private final int uTexture;
    private final float[] quad = new float[QUAD_FLOATS];
    private final FloatBuffer uploadBuffer = ByteBuffer
            .allocateDirect(QUAD_BYTES)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();

    EchoClientHudTextureRenderer() {
        shader = new EchoClientShader("/shaders/hud_texture.vert", "/shaders/hud_texture.frag");
        uScreen = shader.uniform("uScreen");
        uTexture = shader.uniform("uTexture");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();

        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (long) QUAD_BYTES, GL15.GL_STREAM_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, STRIDE, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, STRIDE, 2 * Float.BYTES);
        GL20.glEnableVertexAttribArray(2);
        GL20.glVertexAttribPointer(2, 4, GL11.GL_FLOAT, false, STRIDE, 4 * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    void begin(int screenWidth, int screenHeight) {
        shader.use();
        shader.setVec3(uScreen, screenWidth, screenHeight, 0.0f);
        shader.setInt(uTexture, 0);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    void draw(int textureId, float x, float y, float width, float height, float alpha) {
        if (textureId == 0 || width <= 0.0f || height <= 0.0f || alpha <= 0.0f) {
            return;
        }
        int offset = 0;
        offset = vertex(offset, x, y, 0.0f, 0.0f, alpha);
        offset = vertex(offset, x + width, y, 1.0f, 0.0f, alpha);
        offset = vertex(offset, x + width, y + height, 1.0f, 1.0f, alpha);
        offset = vertex(offset, x, y, 0.0f, 0.0f, alpha);
        offset = vertex(offset, x + width, y + height, 1.0f, 1.0f, alpha);
        vertex(offset, x, y + height, 0.0f, 1.0f, alpha);

        uploadBuffer.clear();
        uploadBuffer.put(quad).flip();

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0L, uploadBuffer);
        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
        GL30.glBindVertexArray(0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    void end() {
        GL20.glUseProgram(0);
    }

    void delete() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        shader.delete();
    }

    private int vertex(int offset, float x, float y, float u, float v, float alpha) {
        quad[offset++] = x;
        quad[offset++] = y;
        quad[offset++] = u;
        quad[offset++] = v;
        quad[offset++] = 1.0f;
        quad[offset++] = 1.0f;
        quad[offset++] = 1.0f;
        quad[offset++] = alpha;
        return offset;
    }
}
