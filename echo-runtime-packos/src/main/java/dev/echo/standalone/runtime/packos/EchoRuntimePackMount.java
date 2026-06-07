package dev.echo.standalone.runtime.packos;

public record EchoRuntimePackMount(int order, String kind, String path, String source) {
    public EchoRuntimePackMount {
        if (order < 0) {
            throw new IllegalArgumentException("order must not be negative");
        }
        kind = requireText(kind, "kind");
        path = requireText(path, "path");
        source = requireText(source, "source");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
