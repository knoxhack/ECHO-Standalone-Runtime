package dev.echo.standalone.runtime.render;

public record EchoRenderViewport(int width, int height) {
    public EchoRenderViewport {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
    }
}
