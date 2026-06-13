package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveResult;
import dev.echo.standalone.runtime.app.EchoStandalonePlayableVoxelSaveRuntime;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

        writeReport(result);
        System.out.println("phase15.playable voxel save smoke PASS " + result.summary());
    }

    private static void writeReport(EchoStandalonePlayableVoxelSaveResult result) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "playable-voxel-save.json");
        Files.createDirectories(report.getParent());
        Files.writeString(report, """
                {
                  "schema": "echo.standalone.playable_voxel_save.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoRuntimePlayableVoxelSaveSmokeHarness",
                  "status": "PASS",
                  "slotId": "%s",
                  "filesWritten": %d,
                  "worldEdits": %s,
                  "player": %s,
                  "hotbar": %s,
                  "mission": %s,
                  "manifestTracksWorldEdits": %s,
                  "manifestTracksPlayer": %s,
                  "manifestTracksHotbar": %s,
                  "manifestTracksMission": %s,
                  "manifestTracksRender": %s,
                  "contractBacked": %s,
                  "contractVersioned": %s,
                  "restoredContractState": %s,
                  "uniqueMissionKeys": %s,
                  "midRouteSaveLoadReady": %s,
                  "restoredRenderChecksum": %s,
                  "savedRenderChecksum": "%s",
                  "restoredRenderChecksumValue": "%s"
                }
                """.formatted(
                jsonString(result.slotId()),
                result.filesWritten(),
                result.restoredWorldEdits(),
                result.restoredPlayer(),
                result.restoredHotbar(),
                result.restoredMission(),
                result.manifestTracksWorldEdits(),
                result.manifestTracksPlayer(),
                result.manifestTracksHotbar(),
                result.manifestTracksMission(),
                result.manifestTracksRender(),
                result.contractBacked(),
                result.contractVersioned(),
                result.restoredContractState(),
                result.uniqueMissionKeys(),
                result.midRouteSaveLoadReady(),
                result.restoredRenderChecksum(),
                Long.toUnsignedString(result.savedRenderChecksum(), 16),
                Long.toUnsignedString(result.restoredRenderChecksumValue(), 16)
        ));
    }

    private static String jsonString(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
