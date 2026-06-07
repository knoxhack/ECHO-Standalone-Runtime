package dev.echo.standalone.runtime.client;

record EchoClientUiViewport(
        int framebufferWidth,
        int framebufferHeight,
        int logicalWidth,
        int logicalHeight,
        double scale
) {
    EchoClientUiViewport {
        framebufferWidth = Math.max(1, framebufferWidth);
        framebufferHeight = Math.max(1, framebufferHeight);
        logicalWidth = Math.max(1, logicalWidth);
        logicalHeight = Math.max(1, logicalHeight);
        if (scale <= 0.0D) {
            throw new IllegalArgumentException("scale must be positive");
        }
    }

    double logicalPointerX(double pointerX) {
        return pointerX / scale;
    }

    double logicalPointerY(double pointerY) {
        return pointerY / scale;
    }
}
