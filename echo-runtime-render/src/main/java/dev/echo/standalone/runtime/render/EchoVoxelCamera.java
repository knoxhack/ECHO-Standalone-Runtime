package dev.echo.standalone.runtime.render;

public record EchoVoxelCamera(
        double x,
        double y,
        double z,
        double yawDegrees,
        double pitchDegrees,
        double fovDegrees
) {
    public EchoVoxelCamera {
        if (fovDegrees <= 1.0D || fovDegrees >= 170.0D) {
            throw new IllegalArgumentException("fovDegrees must be between 1 and 170");
        }
    }
}
