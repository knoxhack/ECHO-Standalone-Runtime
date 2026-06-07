package dev.echo.standalone.runtime.app;

public record EchoStandaloneSupportBundleEntry(
        String relativePath,
        boolean present,
        long byteSize
) {
    public EchoStandaloneSupportBundleEntry {
        relativePath = requireText(relativePath, "relativePath");
        if (byteSize < 0L) {
            throw new IllegalArgumentException("byteSize must not be negative");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim().replace('\\', '/');
    }
}
