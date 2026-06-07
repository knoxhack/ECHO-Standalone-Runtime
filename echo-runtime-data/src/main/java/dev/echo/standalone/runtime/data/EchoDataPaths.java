package dev.echo.standalone.runtime.data;

final class EchoDataPaths {
    private EchoDataPaths() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.replace('\\', '/');
    }

    static String stripJsonSuffix(String value) {
        String normalized = requireText(value, "value");
        if (normalized.endsWith(".json")) {
            return normalized.substring(0, normalized.length() - ".json".length());
        }
        return normalized;
    }

    static String logicalId(String namespace, String path) {
        return requireText(namespace, "namespace") + ":" + stripJsonSuffix(path);
    }
}
