package dev.echo.standalone.runtime.contracts.story;

final class EchoStoryText {
    private EchoStoryText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
