package dev.echo.standalone.runtime.compat;

final class EchoCompatText {
    private EchoCompatText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    static String optionalText(String value) {
        if (value == null || value.isBlank()) {
            return "none";
        }
        return value.trim();
    }
}
