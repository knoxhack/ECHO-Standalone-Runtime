package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityReport;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;

import java.util.List;
import java.util.Objects;

public record EchoSaveProfileFlowResult(
        EchoSaveProfile profile,
        EchoSaveRuntimeResult saveRuntime,
        EchoUiRuntimeResult ui,
        List<EchoSaveProfileSlotSummary> slots,
        EchoSaveProfileContinueFlow continueFlow,
        EchoSaveCommitResult newGameCommit,
        EchoSaveCommitResult autosaveCommit,
        EchoSaveCommitResult manualSaveCommit,
        EchoSaveCorruptionReport corruptionWarning,
        EchoSaveProfileRestoreResult restoreResult,
        EchoSaveModSetCompatibilityReport incompatibleModSet,
        EchoSaveProfileMigrationPrompt migrationPrompt,
        EchoSaveProfileFlowSummary summary
) {
    public EchoSaveProfileFlowResult {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(saveRuntime, "saveRuntime");
        Objects.requireNonNull(ui, "ui");
        Objects.requireNonNull(slots, "slots");
        slots = List.copyOf(slots);
        Objects.requireNonNull(continueFlow, "continueFlow");
        Objects.requireNonNull(newGameCommit, "newGameCommit");
        Objects.requireNonNull(autosaveCommit, "autosaveCommit");
        Objects.requireNonNull(manualSaveCommit, "manualSaveCommit");
        Objects.requireNonNull(corruptionWarning, "corruptionWarning");
        Objects.requireNonNull(restoreResult, "restoreResult");
        Objects.requireNonNull(incompatibleModSet, "incompatibleModSet");
        Objects.requireNonNull(migrationPrompt, "migrationPrompt");
        Objects.requireNonNull(summary, "summary");
    }
}
