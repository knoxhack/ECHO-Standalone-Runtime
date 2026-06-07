package dev.echo.standalone.runtime.client;

public final class EchoClientInputSmokeHarness {
    private EchoClientInputSmokeHarness() {
    }

    public static void main(String[] args) {
        require(EchoClientInput.yawDeltaFromMouseDelta(12.0D) > 0.0D,
                "Moving mouse right should turn camera right using positive yaw delta");
        require(EchoClientInput.yawDeltaFromMouseDelta(-12.0D) < 0.0D,
                "Moving mouse left should turn camera left using negative yaw delta");
        require(EchoClientInput.yawDeltaFromMouseDelta(0.0D) == 0.0D,
                "No horizontal mouse movement should not change yaw");
        require(EchoClientInput.pitchDeltaFromMouseDelta(8.0D) < 0.0D,
                "Moving mouse down should preserve existing negative pitch delta behavior");
        require(EchoClientInput.pitchDeltaFromMouseDelta(-8.0D) > 0.0D,
                "Moving mouse up should preserve existing positive pitch delta behavior");
        System.out.println("client input smoke PASS mouseLook=right-positive-yaw");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
