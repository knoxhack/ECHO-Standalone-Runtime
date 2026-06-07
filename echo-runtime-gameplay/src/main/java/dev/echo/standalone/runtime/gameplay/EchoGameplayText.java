package dev.echo.standalone.runtime.gameplay;

final class EchoGameplayText {
    private EchoGameplayText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
