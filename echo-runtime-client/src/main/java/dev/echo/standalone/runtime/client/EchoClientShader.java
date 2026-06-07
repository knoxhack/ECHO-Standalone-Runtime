package dev.echo.standalone.runtime.client;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Lightweight shader compilation helper.
 */
final class EchoClientShader {
    private final int program;

    public EchoClientShader(String vertexResource, String fragmentResource) {
        String vertSrc = loadResource(vertexResource);
        String fragSrc = loadResource(fragmentResource);
        int vert = compile(GL20.GL_VERTEX_SHADER, vertSrc, vertexResource);
        int frag = compile(GL20.GL_FRAGMENT_SHADER, fragSrc, fragmentResource);
        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vert);
        GL20.glAttachShader(program, frag);
        GL20.glLinkProgram(program);
        if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL20.GL_FALSE) {
            String log = GL20.glGetProgramInfoLog(program);
            GL20.glDeleteProgram(program);
            throw new RuntimeException("Shader link failed: " + log);
        }
        GL20.glDetachShader(program, vert);
        GL20.glDetachShader(program, frag);
        GL20.glDeleteShader(vert);
        GL20.glDeleteShader(frag);
    }

    public void use() {
        GL20.glUseProgram(program);
    }

    public void delete() {
        GL20.glDeleteProgram(program);
    }

    public int uniform(String name) {
        int loc = GL20.glGetUniformLocation(program, name);
        return loc;
    }

    public void setMat4(int location, float[] values) {
        GL20.glUniformMatrix4fv(location, false, values);
    }

    public void setInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    public void setVec3(int location, float x, float y, float z) {
        GL20.glUniform3f(location, x, y, z);
    }

    public void setVec4(int location, float x, float y, float z, float w) {
        GL20.glUniform4f(location, x, y, z, w);
    }

    public void setFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    private static String loadResource(String path) {
        InputStream in = EchoClientShader.class.getResourceAsStream(path);
        if (in == null) {
            throw new RuntimeException("Shader resource not found: " + path);
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read shader: " + path, e);
        }
    }

    private static int compile(int type, String source, String name) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            String log = GL20.glGetShaderInfoLog(shader);
            GL20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed (" + name + "): " + log);
        }
        return shader;
    }
}
