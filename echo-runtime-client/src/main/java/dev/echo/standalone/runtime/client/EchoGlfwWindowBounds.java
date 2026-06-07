package dev.echo.standalone.runtime.client;

record EchoGlfwWindowBounds(int x, int y, int width, int height) {
    EchoGlfwWindowBounds {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
    }
}
