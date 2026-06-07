package dev.echo.standalone.runtime.assets;

public record EchoAssetNamespace(String id) {
    public EchoAssetNamespace {
        if (id == null || !id.matches("[a-z][a-z0-9_]{1,63}")) {
            throw new IllegalArgumentException("Invalid ECHO asset namespace: " + id);
        }
    }
}
