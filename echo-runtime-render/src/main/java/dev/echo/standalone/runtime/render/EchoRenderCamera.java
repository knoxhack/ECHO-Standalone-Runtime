package dev.echo.standalone.runtime.render;

public record EchoRenderCamera(
        String cameraId,
        double x,
        double y,
        double z,
        double zoom,
        double pitchDegrees
) {
    public EchoRenderCamera {
        cameraId = EchoRenderText.requireText(cameraId, "cameraId");
        if (zoom <= 0.0D) {
            throw new IllegalArgumentException("zoom must be positive");
        }
    }

}
