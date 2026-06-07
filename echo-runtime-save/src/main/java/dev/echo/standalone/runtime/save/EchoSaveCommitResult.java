package dev.echo.standalone.runtime.save;

import java.util.Objects;
import java.util.Optional;

public record EchoSaveCommitResult(
        EchoSaveSlot slot,
        EchoSaveManifest manifest,
        Optional<EchoSaveBackup> backup,
        int filesWritten
) {
    public EchoSaveCommitResult {
        Objects.requireNonNull(slot, "slot");
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(backup, "backup");
        if (filesWritten < 0) {
            throw new IllegalArgumentException("filesWritten must not be negative");
        }
    }
}
