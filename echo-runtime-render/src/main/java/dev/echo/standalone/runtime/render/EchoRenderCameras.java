package dev.echo.standalone.runtime.render;

public final class EchoRenderCameras {
    private EchoRenderCameras() {
    }

    public static EchoRenderCamera ashfallPreview() {
        return new EchoRenderCamera("ashfall-debug-camera", 1.5D, 3.0D, 1.5D, 1.0D, 55.0D);
    }
}
