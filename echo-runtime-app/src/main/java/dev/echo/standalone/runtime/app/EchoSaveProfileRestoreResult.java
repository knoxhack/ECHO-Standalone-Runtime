package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;

import java.util.Objects;

public record EchoSaveProfileRestoreResult(
        String slotId,
        EchoSaveBackup restoredBackup,
        EchoSaveCorruptionReport beforeRestore,
        EchoSaveCorruptionReport afterRestore,
        boolean warningShown,
        boolean restored
) {
    public EchoSaveProfileRestoreResult {
        slotId = EchoAppText.requireText(slotId, "slotId");
        Objects.requireNonNull(restoredBackup, "restoredBackup");
        Objects.requireNonNull(beforeRestore, "beforeRestore");
        Objects.requireNonNull(afterRestore, "afterRestore");
    }
}
