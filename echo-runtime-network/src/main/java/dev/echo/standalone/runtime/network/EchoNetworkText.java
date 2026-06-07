package dev.echo.standalone.runtime.network;

final class EchoNetworkText {
    private EchoNetworkText() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
