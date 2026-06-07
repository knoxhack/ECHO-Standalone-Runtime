package dev.echo.standalone.runtime.save;

import java.nio.file.Path;
import java.util.Objects;

public record EchoSaveBackup(
        String backupId,
        String slotId,
        Path root,
        String manifestChecksumSha256,
        int dataFileCount
) {
    public EchoSaveBackup {
        backupId = EchoSavePaths.requireText(backupId, "backupId");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        Objects.requireNonNull(root, "root");
        manifestChecksumSha256 = EchoSavePaths.requireText(manifestChecksumSha256, "manifestChecksumSha256");
        if (dataFileCount < 0) {
            throw new IllegalArgumentException("dataFileCount must not be negative");
        }
        root = root.toAbsolutePath().normalize();
    }
}
