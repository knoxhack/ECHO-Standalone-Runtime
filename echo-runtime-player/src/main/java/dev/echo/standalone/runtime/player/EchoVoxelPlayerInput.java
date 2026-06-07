package dev.echo.standalone.runtime.player;

public record EchoVoxelPlayerInput(
        boolean forward,
        boolean backward,
        boolean strafeLeft,
        boolean strafeRight,
        boolean jump,
        boolean crouch,
        boolean sprint,
        double yawDeltaDegrees,
        double pitchDeltaDegrees
) {
    public static EchoVoxelPlayerInput idle() {
        return new EchoVoxelPlayerInput(false, false, false, false, false, false, false, 0.0D, 0.0D);
    }

    public static EchoVoxelPlayerInput look(double yawDeltaDegrees, double pitchDeltaDegrees) {
        return new EchoVoxelPlayerInput(
                false,
                false,
                false,
                false,
                false,
                false,
                false,
                yawDeltaDegrees,
                pitchDeltaDegrees
        );
    }

    public boolean wantsHorizontalMovement() {
        return forward || backward || strafeLeft || strafeRight;
    }

    public boolean wantsLook() {
        return yawDeltaDegrees != 0.0D || pitchDeltaDegrees != 0.0D;
    }

    public boolean active() {
        return wantsHorizontalMovement() || wantsLook() || jump || crouch || sprint;
    }
}
