package dev.echo.standalone.runtime.audio;

public record EchoAudioDeviceSettings(
        boolean deviceEnabled,
        boolean forceDeviceFailure,
        float sampleRate,
        int sampleSizeBits,
        int channels,
        int writeMillis,
        String requestedDeviceName
) {
    public EchoAudioDeviceSettings {
        if (sampleRate <= 0.0F) {
            throw new IllegalArgumentException("sampleRate must be positive");
        }
        if (sampleSizeBits != 16) {
            throw new IllegalArgumentException("only 16-bit sample output is supported");
        }
        if (channels != 1) {
            throw new IllegalArgumentException("only mono sample output is supported");
        }
        if (writeMillis < 0 || writeMillis > 250) {
            throw new IllegalArgumentException("writeMillis must be between 0 and 250");
        }
        requestedDeviceName = requestedDeviceName == null ? "" : requestedDeviceName.trim();
    }

    public static EchoAudioDeviceSettings forcedFallback() {
        return new EchoAudioDeviceSettings(true, true, 22050.0F, 16, 1, 12, "");
    }
}
