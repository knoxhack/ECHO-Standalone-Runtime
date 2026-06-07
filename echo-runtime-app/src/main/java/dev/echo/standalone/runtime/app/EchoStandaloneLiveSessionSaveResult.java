package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneLiveSessionSaveResult(
        String slotId,
        int manualFilesWritten,
        int autosaveFilesWritten,
        List<String> manifestFiles,
        boolean manifestTracksWorldEdits,
        boolean manifestTracksPlayer,
        boolean manifestTracksHotbar,
        boolean manifestTracksMission,
        boolean manifestTracksRender,
        boolean manualCommitted,
        boolean autosaveCommitted,
        boolean backupCreated,
        boolean restoredHudState,
        boolean restoredMissionLogState,
        boolean restoredTerminalPowerState,
        boolean restoredInventoryState,
        boolean uniqueMissionKeys,
        String finalSaveKind,
        long savedRenderChecksum
) {
    public EchoStandaloneLiveSessionSaveResult {
        slotId = EchoAppText.requireText(slotId, "slotId");
        Objects.requireNonNull(manifestFiles, "manifestFiles");
        manifestFiles = List.copyOf(manifestFiles);
        finalSaveKind = EchoAppText.requireText(finalSaveKind, "finalSaveKind");
    }

    public boolean ready() {
        return manualFilesWritten == 5
                && autosaveFilesWritten == 5
                && manifestTracksWorldEdits
                && manifestTracksPlayer
                && manifestTracksHotbar
                && manifestTracksMission
                && manifestTracksRender
                && manualCommitted
                && autosaveCommitted
                && backupCreated
                && restoredHudState
                && restoredMissionLogState
                && restoredTerminalPowerState
                && restoredInventoryState
                && uniqueMissionKeys
                && finalSaveKind.equals("autosave")
                && savedRenderChecksum != 0L;
    }

    public String summary() {
        return "slot=" + slotId
                + " manualFiles=" + manualFilesWritten
                + " autosaveFiles=" + autosaveFilesWritten
                + " backup=" + backupCreated
                + " restoredHud=" + restoredHudState
                + " restoredMissionLog=" + restoredMissionLogState
                + " restoredTerminal=" + restoredTerminalPowerState
                + " restoredInventory=" + restoredInventoryState
                + " uniqueMissionKeys=" + uniqueMissionKeys
                + " files=" + manifestFiles.size()
                + " saveKind=" + finalSaveKind
                + " renderChecksum=" + Long.toUnsignedString(savedRenderChecksum, 16);
    }
}
