package dev.echo.standalone.runtime.save;

public record EchoSaveFileState(
        String relativePath,
        String checksumSha256,
        long bytes
) {
    public EchoSaveFileState {
        relativePath = EchoSavePaths.requireRelativePath(relativePath, "relativePath");
        checksumSha256 = EchoSavePaths.requireText(checksumSha256, "checksumSha256");
        if (bytes < 0) {
            throw new IllegalArgumentException("bytes must not be negative");
        }
    }
}
