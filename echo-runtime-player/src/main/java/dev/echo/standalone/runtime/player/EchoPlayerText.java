package dev.echo.standalone.runtime.player;

final class EchoPlayerText {
    private EchoPlayerText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
