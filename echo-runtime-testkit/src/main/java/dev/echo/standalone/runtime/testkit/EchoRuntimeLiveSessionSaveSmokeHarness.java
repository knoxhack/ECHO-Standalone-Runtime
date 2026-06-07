package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandaloneLiveSessionSaveResult;
import dev.echo.standalone.runtime.app.EchoStandaloneLiveSessionSaveRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;

public final class EchoRuntimeLiveSessionSaveSmokeHarness {
    private EchoRuntimeLiveSessionSaveSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoStandaloneLiveSessionSaveResult result = new EchoStandaloneLiveSessionSaveRuntime().run(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                Files.createTempDirectory("echo-live-session-save-smoke")
        );

        require(result.ready(), "live session save/autosave should be ready");
        require(result.manualFilesWritten() == 5, "manual live save should commit five state files");
        require(result.autosaveFilesWritten() == 5, "live autosave should commit five state files");
        require(result.manifestTracksWorldEdits(), "live save manifest should track world edits");
        require(result.manifestTracksPlayer(), "live save manifest should track player state");
        require(result.manifestTracksHotbar(), "live save manifest should track hotbar state");
        require(result.manifestTracksMission(), "live save manifest should track mission state");
        require(result.manifestTracksRender(), "live save manifest should track render state");
        require(result.backupCreated(), "second live save should create a backup from the first save");
        require(result.restoredHudState(), "autosave restore should preserve HUD objective, hint, hotbar, and extraction state");
        require(result.restoredMissionLogState(), "autosave restore should preserve mission log history and terminal notes");
        require(result.restoredTerminalPowerState(), "autosave restore should preserve terminal power repair state");
        require(result.restoredInventoryState(), "autosave restore should preserve inventory and hotbar state");
        require(result.uniqueMissionKeys(), "live autosave mission properties should not contain duplicate keys");
        require(result.finalSaveKind().equals("autosave"), "final live manifest should identify autosave kind");

        System.out.println("phase15.live session save smoke PASS " + result.summary());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
