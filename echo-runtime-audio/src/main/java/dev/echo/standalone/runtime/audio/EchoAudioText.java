package dev.echo.standalone.runtime.audio;

final class EchoAudioText {
    private EchoAudioText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
