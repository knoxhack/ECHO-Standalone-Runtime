package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveResult;
import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;

public final class EchoRuntimePlayableVoxelSaveSmokeHarness {
    private EchoRuntimePlayableVoxelSaveSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoStandalonePlayableVoxelSaveResult result = new EchoStandalonePlayableVoxelSaveRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                Files.createTempDirectory("echo-playable-voxel-save-smoke")
        );

        require(result.ready(), "playable voxel save/restore should be ready: " + result.summary());
        require(result.filesWritten() == 5, "playable voxel save should commit five state files");
        require(result.manifestTracksWorldEdits(), "manifest should track world edits");
        require(result.manifestTracksPlayer(), "manifest should track player state");
        require(result.manifestTracksHotbar(), "manifest should track hotbar state");
        require(result.manifestTracksMission(), "manifest should track mission state");
        require(result.restoredWorldEdits(), "restore should apply mined/placed block edits");
        require(result.restoredPlayer(), "restore should reconstruct player position and movement state");
        require(result.restoredHotbar(), "restore should reconstruct hotbar contents");
        require(result.restoredMission(), "restore should reconstruct shared Ashfall mission state");
        require(result.uniqueMissionKeys(), "mission save properties should not contain duplicate keys");
        require(result.midRouteSaveLoadReady(), "mid-route save/load should preserve terminal, cache, repair kit, hotbar, and objective state");
        require(result.restoredRenderChecksum(), "restored world/player should render the same voxel frame checksum");

        System.out.println("phase15.playable voxel save smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
