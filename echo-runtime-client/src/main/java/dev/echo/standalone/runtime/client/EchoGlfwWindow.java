package dev.echo.standalone.runtime.client;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Owns the native GLFW window and its OpenGL 3.3 core context. This is the single
 * presenter for the standalone client: there is exactly one surface and one swap,
 * so there is no legacy dual-presenter conflict and no tearing (vsync on).
 */
public final class EchoGlfwWindow implements AutoCloseable {
    private final String title;
    private long handle;
    private int width;
    private int height;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private boolean fullscreen;
    private boolean vSync = true;
    private boolean framebufferResized;
    private boolean focused = true;
    private boolean focusLostPending;

    public EchoGlfwWindow(String title, int width, int height) {
        this.title = title;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.windowedWidth = this.width;
        this.windowedHeight = this.height;
    }

    public void create() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetFramebufferSizeCallback(handle, (win, w, h) -> {
            if (w > 0 && h > 0) {
                width = w;
                height = h;
                framebufferResized = true;
            }
        });
        glfwSetWindowFocusCallback(handle, (win, nextFocused) -> {
            if (focused && !nextFocused) {
                focusLostPending = true;
            }
            focused = nextFocused;
        });

        centerOnPrimaryMonitor();

        glfwMakeContextCurrent(handle);
        applyVSync();
        glfwShowWindow(handle);

        GL.createCapabilities();
        System.out.println("[echo-client] OpenGL " + glGetString(GL_VERSION)
                + " | GLSL " + glGetString(GL_SHADING_LANGUAGE_VERSION)
                + " | " + glGetString(GL_RENDERER));

        // Capture the real framebuffer size (HiDPI can differ from requested size).
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, w, h);
            width = Math.max(1, w.get(0));
            height = Math.max(1, h.get(0));
        }
        focused = glfwGetWindowAttrib(handle, GLFW_FOCUSED) == GLFW_TRUE;
    }

    private void centerOnPrimaryMonitor() {
        long monitor = glfwGetPrimaryMonitor();
        if (monitor == NULL) {
            return;
        }
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) {
            return;
        }
        glfwSetWindowPos(handle, (mode.width() - width) / 2, (mode.height() - height) / 2);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(handle);
    }

    public void requestClose() {
        glfwSetWindowShouldClose(handle, true);
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    public void setFullscreen(boolean fullscreen) {
        if (handle == NULL || this.fullscreen == fullscreen) {
            return;
        }
        if (fullscreen) {
            long monitor = glfwGetPrimaryMonitor();
            if (monitor == NULL) {
                return;
            }
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            if (mode == null) {
                return;
            }
            captureWindowedBounds();
            this.fullscreen = true;
            glfwSetWindowMonitor(handle, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());
        } else {
            this.fullscreen = false;
            EchoGlfwWindowBounds bounds = restoredWindowedBounds(
                    windowedX,
                    windowedY,
                    windowedWidth,
                    windowedHeight,
                    1280,
                    720
            );
            glfwSetWindowMonitor(handle, NULL, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0);
        }
        refreshFramebufferSize();
        framebufferResized = true;
    }

    public void setVSync(boolean vSync) {
        if (this.vSync == vSync) {
            return;
        }
        this.vSync = vSync;
        if (handle != NULL) {
            applyVSync();
        }
    }

    public boolean consumeFramebufferResized() {
        boolean was = framebufferResized;
        framebufferResized = false;
        return was;
    }

    public boolean fullscreen() {
        return fullscreen;
    }

    public boolean vSync() {
        return vSync;
    }

    public boolean focused() {
        return focused;
    }

    public boolean consumeFocusLost() {
        boolean was = focusLostPending;
        focusLostPending = false;
        return was;
    }

    public long handle() {
        return handle;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    private void captureWindowedBounds() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetWindowPos(handle, x, y);
            glfwGetWindowSize(handle, w, h);
            windowedX = x.get(0);
            windowedY = y.get(0);
            windowedWidth = Math.max(1, w.get(0));
            windowedHeight = Math.max(1, h.get(0));
        }
    }

    private void refreshFramebufferSize() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(handle, w, h);
            width = Math.max(1, w.get(0));
            height = Math.max(1, h.get(0));
        }
    }

    private void applyVSync() {
        glfwSwapInterval(swapIntervalForVSync(vSync));
    }

    static int swapIntervalForVSync(boolean vSync) {
        return vSync ? 1 : 0;
    }

    static EchoGlfwWindowBounds restoredWindowedBounds(
            int x,
            int y,
            int width,
            int height,
            int fallbackWidth,
            int fallbackHeight
    ) {
        int safeWidth = width > 0 ? width : Math.max(1, fallbackWidth);
        int safeHeight = height > 0 ? height : Math.max(1, fallbackHeight);
        return new EchoGlfwWindowBounds(x, y, safeWidth, safeHeight);
    }

    @Override
    public void close() {
        if (handle != NULL) {
            glfwFreeCallbacks(handle);
            glfwDestroyWindow(handle);
            handle = NULL;
        }
        glfwTerminate();
        GLFWErrorCallback previous = glfwSetErrorCallback(null);
        if (previous != null) {
            previous.free();
        }
    }
}
