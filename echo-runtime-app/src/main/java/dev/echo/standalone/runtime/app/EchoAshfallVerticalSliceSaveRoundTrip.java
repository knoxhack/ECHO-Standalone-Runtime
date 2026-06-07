package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import java.util.Objects;

public record EchoAshfallVerticalSliceSaveRoundTrip(
        EchoSaveRuntimeResult saveRuntime,
        EchoSaveCommitResult commit,
        EchoSaveManifest loadedManifest,
        EchoSaveCorruptionReport corruptionReport,
        String loadedSummary
) {
    public EchoAshfallVerticalSliceSaveRoundTrip {
        Objects.requireNonNull(saveRuntime, "saveRuntime");
        Objects.requireNonNull(commit, "commit");
        Objects.requireNonNull(loadedManifest, "loadedManifest");
        Objects.requireNonNull(corruptionReport, "corruptionReport");
        if (loadedSummary == null || loadedSummary.isBlank()) {
            throw new IllegalArgumentException("loadedSummary must not be blank");
        }
        loadedSummary = loadedSummary.trim();
    }
}
