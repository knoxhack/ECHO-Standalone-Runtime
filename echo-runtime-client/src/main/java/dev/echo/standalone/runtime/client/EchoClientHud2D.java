package dev.echo.standalone.runtime.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Core-profile compliant 2D HUD renderer.
 * Builds a single dynamic VBO each frame for all HUD elements.
 */
final class EchoClientHud2D {
    private final EchoClientShader shader;
    private final int vao;
    private final int vbo;

    // Vertex layout for 2D colored primitives: x, y, r, g, b, a
    private static final int FLOATS_PER_VERTEX = 6;
    private static final int VERTICES_PER_QUAD = 6;
    private static final int STRIDE = FLOATS_PER_VERTEX * Float.BYTES;
    private float[] buffer = new float[1024];
    private int count = 0;
    private ByteBuffer uploadBytes;
    private FloatBuffer uploadBuffer;

    EchoClientHud2D() {
        shader = new EchoClientShader("/shaders/hud.vert", "/shaders/hud.frag");
        vao = GL30.glGenVertexArrays();
        vbo = GL15.glGenBuffers();
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, STRIDE, 0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, STRIDE, 2 * Float.BYTES);
        GL30.glBindVertexArray(0);
    }

    void begin(int screenWidth, int screenHeight) {
        count = 0;
        shader.use();
        int uScreen = shader.uniform("uScreen");
        shader.setVec3(uScreen, screenWidth, screenHeight, 0.0f);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    void end() {
        if (count > 0) {
            flush();
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL20.glUseProgram(0);
    }

    void rect(float x, float y, float w, float h, float r, float g, float b, float a) {
        ensure(VERTICES_PER_QUAD * FLOATS_PER_VERTEX);
        // Tri 1
        v(x, y, r, g, b, a);
        v(x + w, y, r, g, b, a);
        v(x + w, y + h, r, g, b, a);
        // Tri 2
        v(x, y, r, g, b, a);
        v(x + w, y + h, r, g, b, a);
        v(x, y + h, r, g, b, a);
    }

    void line(float x1, float y1, float x2, float y2, float r, float g, float b, float a, float width) {
        ensure(VERTICES_PER_QUAD * FLOATS_PER_VERTEX);
        // Simple thick line as a small quad
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.001f) return;
        float nx = -dy / len * width * 0.5f;
        float ny = dx / len * width * 0.5f;
        v(x1 + nx, y1 + ny, r, g, b, a);
        v(x2 + nx, y2 + ny, r, g, b, a);
        v(x2 - nx, y2 - ny, r, g, b, a);
        v(x1 + nx, y1 + ny, r, g, b, a);
        v(x2 - nx, y2 - ny, r, g, b, a);
        v(x1 - nx, y1 - ny, r, g, b, a);
    }

    void cross(float cx, float cy, float size, float r, float g, float b, float a, float thickness) {
        line(cx - size, cy, cx + size, cy, r, g, b, a, thickness);
        line(cx, cy - size, cx, cy + size, r, g, b, a, thickness);
    }

    void text(String text, float x, float y, float scale, float r, float g, float b, float a) {
        if (text == null || text.isBlank()) {
            return;
        }
        float cursor = x;
        String upper = text.toUpperCase(java.util.Locale.ROOT);
        for (int i = 0; i < upper.length(); i++) {
            char ch = upper.charAt(i);
            if (ch == '\n') {
                cursor = x;
                y += 8.0f * scale;
                continue;
            }
            String[] pattern = glyph(ch);
            if (pattern.length == 0) {
                cursor += 4.0f * scale;
                continue;
            }
            for (int row = 0; row < pattern.length; row++) {
                String line = pattern[row];
                for (int col = 0; col < line.length(); col++) {
                    if (line.charAt(col) == '1') {
                        rect(cursor + col * scale, y + row * scale, scale, scale, r, g, b, a);
                    }
                }
            }
            cursor += (pattern[0].length() + 1.0f) * scale;
        }
    }

    void delete() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        shader.delete();
    }

    private void v(float x, float y, float r, float g, float b, float a) {
        if (count + FLOATS_PER_VERTEX > buffer.length) {
            flush();
        }
        buffer[count++] = x;
        buffer[count++] = y;
        buffer[count++] = r;
        buffer[count++] = g;
        buffer[count++] = b;
        buffer[count++] = a;
    }

    private void ensure(int need) {
        if (count + need > buffer.length) {
            flush();
        }
    }

    void flush() {
        if (count == 0) return;
        shader.use();
        FloatBuffer buf = uploadBuffer(count);
        buf.put(buffer, 0, count).flip();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STREAM_DRAW);
        GL30.glBindVertexArray(vao);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, count / 6);
        GL30.glBindVertexArray(0);
        count = 0;
    }

    private FloatBuffer uploadBuffer(int floats) {
        int bytes = Math.max(Float.BYTES, floats * Float.BYTES);
        if (uploadBytes == null || uploadBytes.capacity() < bytes) {
            uploadBytes = ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder());
            uploadBuffer = uploadBytes.asFloatBuffer();
        }
        uploadBuffer.clear();
        return uploadBuffer;
    }

    private static String[] glyph(char ch) {
        return switch (ch) {
            case 'A' -> lines("01110", "10001", "10001", "11111", "10001", "10001", "10001");
            case 'B' -> lines("11110", "10001", "10001", "11110", "10001", "10001", "11110");
            case 'C' -> lines("01111", "10000", "10000", "10000", "10000", "10000", "01111");
            case 'D' -> lines("11110", "10001", "10001", "10001", "10001", "10001", "11110");
            case 'E' -> lines("11111", "10000", "10000", "11110", "10000", "10000", "11111");
            case 'F' -> lines("11111", "10000", "10000", "11110", "10000", "10000", "10000");
            case 'G' -> lines("01111", "10000", "10000", "10111", "10001", "10001", "01110");
            case 'H' -> lines("10001", "10001", "10001", "11111", "10001", "10001", "10001");
            case 'I' -> lines("11111", "00100", "00100", "00100", "00100", "00100", "11111");
            case 'J' -> lines("00111", "00010", "00010", "00010", "10010", "10010", "01100");
            case 'K' -> lines("10001", "10010", "10100", "11000", "10100", "10010", "10001");
            case 'L' -> lines("10000", "10000", "10000", "10000", "10000", "10000", "11111");
            case 'M' -> lines("10001", "11011", "10101", "10101", "10001", "10001", "10001");
            case 'N' -> lines("10001", "11001", "10101", "10011", "10001", "10001", "10001");
            case 'O' -> lines("01110", "10001", "10001", "10001", "10001", "10001", "01110");
            case 'P' -> lines("11110", "10001", "10001", "11110", "10000", "10000", "10000");
            case 'Q' -> lines("01110", "10001", "10001", "10001", "10101", "10010", "01101");
            case 'R' -> lines("11110", "10001", "10001", "11110", "10100", "10010", "10001");
            case 'S' -> lines("01111", "10000", "10000", "01110", "00001", "00001", "11110");
            case 'T' -> lines("11111", "00100", "00100", "00100", "00100", "00100", "00100");
            case 'U' -> lines("10001", "10001", "10001", "10001", "10001", "10001", "01110");
            case 'V' -> lines("10001", "10001", "10001", "10001", "10001", "01010", "00100");
            case 'W' -> lines("10001", "10001", "10001", "10101", "10101", "10101", "01010");
            case 'X' -> lines("10001", "10001", "01010", "00100", "01010", "10001", "10001");
            case 'Y' -> lines("10001", "10001", "01010", "00100", "00100", "00100", "00100");
            case 'Z' -> lines("11111", "00001", "00010", "00100", "01000", "10000", "11111");
            case '0' -> lines("01110", "10001", "10011", "10101", "11001", "10001", "01110");
            case '1' -> lines("00100", "01100", "00100", "00100", "00100", "00100", "01110");
            case '2' -> lines("01110", "10001", "00001", "00010", "00100", "01000", "11111");
            case '3' -> lines("11110", "00001", "00001", "01110", "00001", "00001", "11110");
            case '4' -> lines("00010", "00110", "01010", "10010", "11111", "00010", "00010");
            case '5' -> lines("11111", "10000", "10000", "11110", "00001", "00001", "11110");
            case '6' -> lines("01110", "10000", "10000", "11110", "10001", "10001", "01110");
            case '7' -> lines("11111", "00001", "00010", "00100", "01000", "01000", "01000");
            case '8' -> lines("01110", "10001", "10001", "01110", "10001", "10001", "01110");
            case '9' -> lines("01110", "10001", "10001", "01111", "00001", "00001", "01110");
            case ':' -> lines("0", "1", "0", "0", "1", "0", "0");
            case '.' -> lines("0", "0", "0", "0", "0", "0", "1");
            case '-' -> lines("000", "000", "000", "111", "000", "000", "000");
            case '_' -> lines("00000", "00000", "00000", "00000", "00000", "00000", "11111");
            case '%' -> lines("10001", "00010", "00100", "01000", "10001", "00000", "00000");
            case '|' -> lines("1", "1", "1", "1", "1", "1", "1");
            case '/' -> lines("00001", "00010", "00010", "00100", "01000", "01000", "10000");
            case ' ' -> new String[0];
            default -> lines("111", "101", "001", "010", "000", "010", "000");
        };
    }

    private static String[] lines(String... lines) {
        return lines;
    }
}
