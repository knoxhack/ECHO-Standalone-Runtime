package dev.echo.standalone.runtime.client;

final class EchoClientUiScale {
    private EchoClientUiScale() {
    }

    static EchoClientUiViewport viewport(int uiScalePercent, int framebufferWidth, int framebufferHeight) {
        double scale = scaleFactor(uiScalePercent);
        return new EchoClientUiViewport(
                framebufferWidth,
                framebufferHeight,
                Math.max(1, (int) Math.ceil(framebufferWidth / scale)),
                Math.max(1, (int) Math.ceil(framebufferHeight / scale)),
                scale
        );
    }

    static double scaleFactor(int uiScalePercent) {
        int clamped = Math.max(0, Math.min(100, uiScalePercent));
        return 0.75D + clamped * 0.005D;
    }
}
