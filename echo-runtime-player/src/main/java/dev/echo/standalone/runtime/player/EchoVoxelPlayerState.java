package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.render.EchoVoxelCamera;

public record EchoVoxelPlayerState(
        double x,
        double y,
        double z,
        double velocityY,
        double yawDegrees,
        double pitchDegrees,
        boolean grounded,
        boolean crouching,
        boolean sprinting,
        int selectedSlot,
        double reach
) {
    public static final double PLAYER_RADIUS = 0.32D;
    public static final double SURVIVAL_REACH = 4.5D;

    public EchoVoxelPlayerState {
        if (selectedSlot < 0) {
            throw new IllegalArgumentException("selectedSlot must not be negative");
        }
        if (reach <= 0.0D) {
            throw new IllegalArgumentException("reach must be positive");
        }
    }

    public double eyeHeight() {
        return crouching ? 1.24D : 1.62D;
    }

    public double bodyHeight() {
        return crouching ? 1.35D : 1.82D;
    }

    public double bodyMinX() {
        return x - PLAYER_RADIUS;
    }

    public double bodyMinY() {
        return y + 0.02D;
    }

    public double bodyMinZ() {
        return z - PLAYER_RADIUS;
    }

    public double bodyMaxX() {
        return x + PLAYER_RADIUS;
    }

    public double bodyMaxY() {
        return y + bodyHeight() - 0.02D;
    }

    public double bodyMaxZ() {
        return z + PLAYER_RADIUS;
    }

    public boolean intersectsBlock(int blockX, int blockY, int blockZ) {
        return bodyMaxX() > blockX
                && bodyMinX() < blockX + 1.0D
                && bodyMaxY() > blockY
                && bodyMinY() < blockY + 1.0D
                && bodyMaxZ() > blockZ
                && bodyMinZ() < blockZ + 1.0D;
    }

    public double eyeY() {
        return y + eyeHeight();
    }

    public EchoVoxelCamera camera() {
        return camera(70.0D);
    }

    public EchoVoxelCamera camera(double fovDegrees) {
        return new EchoVoxelCamera(x, eyeY(), z, yawDegrees, pitchDegrees, fovDegrees);
    }

    public String blockPosition() {
        return (int) Math.floor(x) + "," + (int) Math.floor(y) + "," + (int) Math.floor(z);
    }
}
