package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandalonePlayableVoxelSaveResult(
        String slotId,
        int filesWritten,
        List<String> manifestFiles,
        boolean manifestTracksWorldEdits,
        boolean manifestTracksPlayer,
        boolean manifestTracksHotbar,
        boolean manifestTracksMission,
        boolean manifestTracksRender,
        boolean restoredWorldEdits,
        boolean restoredPlayer,
        boolean restoredHotbar,
        boolean restoredMission,
        boolean contractBacked,
        boolean contractVersioned,
        boolean restoredContractState,
        boolean uniqueMissionKeys,
        boolean midRouteSaveLoadReady,
        boolean restoredRenderChecksum,
        long savedRenderChecksum,
        long restoredRenderChecksumValue
) {
    public EchoStandalonePlayableVoxelSaveResult {
        slotId = EchoAppText.requireText(slotId, "slotId");
        Objects.requireNonNull(manifestFiles, "manifestFiles");
        manifestFiles = List.copyOf(manifestFiles);
    }

    public boolean ready() {
        return filesWritten >= 5
                && manifestTracksWorldEdits
                && manifestTracksPlayer
                && manifestTracksHotbar
                && manifestTracksMission
                && manifestTracksRender
                && restoredWorldEdits
                && restoredPlayer
                && restoredHotbar
                && restoredMission
                && contractBacked
                && contractVersioned
                && restoredContractState
                && uniqueMissionKeys
                && midRouteSaveLoadReady
                && restoredRenderChecksum;
    }

    public String summary() {
        return "slot=" + slotId
                + " files=" + filesWritten
                + " worldEdits=" + restoredWorldEdits
                + " player=" + restoredPlayer
                + " hotbar=" + restoredHotbar
                + " mission=" + restoredMission
                + " contract=" + contractBacked
                + " versioned=" + contractVersioned
                + " uniqueMissionKeys=" + uniqueMissionKeys
                + " midRoute=" + midRouteSaveLoadReady
                + " renderChecksum=" + Long.toUnsignedString(savedRenderChecksum, 16)
                + "->" + Long.toUnsignedString(restoredRenderChecksumValue, 16);
    }
}
