package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoStandaloneLiveSessionSaveRuntime {
    public static final String LIVE_SLOT_ID = "ashfall-live-01";

    public EchoStandaloneLiveSessionSaveResult run(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(saveRoot, "saveRoot");
        EchoStandalonePlayableVoxelSession session = new EchoStandalonePlayableVoxelRuntime().play(bridge);
        EchoSaveRuntimeResult save = openSave(saveRoot);
        EchoVoxelFramebuffer frame = new EchoVoxelSoftwareRenderer().render(
                session.world(),
                session.player().camera(),
                960,
                540
        );
        EchoSaveCommitResult manual = EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                save,
                LIVE_SLOT_ID,
                "tx-live-manual-001",
                session.player(),
                session.hotbar(),
                session.mission(),
                session.edits(),
                frame,
                Map.of(
                        "saveKind", "manual",
                        "runtime", "standalone",
                        "scenario", "live_game_window",
                        "adaptercore", "multi_runtime"
                )
        );
        EchoSaveCommitResult autosave = EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                save,
                LIVE_SLOT_ID,
                "tx-live-autosave-002",
                session.player(),
                session.hotbar(),
                session.mission(),
                session.edits(),
                frame,
                Map.of(
                        "saveKind", "autosave",
                        "runtime", "standalone",
                        "scenario", "live_game_window",
                        "adaptercore", "multi_runtime"
                )
        );
        EchoStandalonePlayableVoxelSaveSnapshot restored = EchoStandalonePlayableVoxelSaveCodec.restoreSnapshot(
                bridge,
                save,
                autosave.manifest()
        );
        String autosaveMissionText = java.nio.file.Files.readString(
                save.profile().slot(LIVE_SLOT_ID).dataRoot().resolve(EchoStandalonePlayableVoxelSaveCodec.MISSION_PATH));
        EchoAshfallPlayerFeedback savedFeedback = EchoAshfallPlayerFeedback.from(
                session.mission(),
                session.hotbar(),
                true,
                session.mission().lastMessage()
        );
        EchoAshfallPlayerFeedback restoredFeedback = EchoAshfallPlayerFeedback.from(
                restored.mission(),
                restored.hotbar(),
                true,
                restored.mission().lastMessage()
        );
        boolean restoredHudState = savedFeedback.currentObjective().equals(restoredFeedback.currentObjective())
                && savedFeedback.currentHint().equals(restoredFeedback.currentHint())
                && savedFeedback.selectedHotbarItem().equals(restoredFeedback.selectedHotbarItem())
                && session.mission().extractionStatus().equals(restored.mission().extractionStatus());
        boolean restoredMissionLogState = restored.mission().completedObjectives() == session.mission().completedObjectives()
                && restored.mission().totalObjectives() == session.mission().totalObjectives()
                && restored.mission().completedHistory().equals(session.mission().completedHistory())
                && restored.mission().terminalNotes().equals(session.mission().terminalNotes());
        boolean restoredTerminalPowerState = session.mission().powerRepaired()
                && restored.mission().powerRepaired()
                && restored.mission().terminalState().equals(session.mission().terminalState());
        boolean restoredInventoryState = sameHotbar(session.hotbar(), restored.hotbar());

        List<String> manifestFiles = autosave.manifest().files().stream()
                .map(file -> file.relativePath())
                .toList();
        EchoStandaloneLiveSessionSaveResult result = new EchoStandaloneLiveSessionSaveResult(
                LIVE_SLOT_ID,
                manual.filesWritten(),
                autosave.filesWritten(),
                manifestFiles,
                autosave.manifest().file(EchoStandalonePlayableVoxelSaveCodec.WORLD_EDITS_PATH).isPresent(),
                autosave.manifest().file(EchoStandalonePlayableVoxelSaveCodec.PLAYER_PATH).isPresent(),
                autosave.manifest().file(EchoStandalonePlayableVoxelSaveCodec.HOTBAR_PATH).isPresent(),
                autosave.manifest().file(EchoStandalonePlayableVoxelSaveCodec.MISSION_PATH).isPresent(),
                autosave.manifest().file(EchoStandalonePlayableVoxelSaveCodec.RENDER_PATH).isPresent(),
                EchoStandalonePlayableVoxelSaveCodec.manifestTracksGameplayFiles(manual.manifest()),
                EchoStandalonePlayableVoxelSaveCodec.manifestTracksGameplayFiles(autosave.manifest()),
                autosave.backup().isPresent() && !autosave.manifest().backupIds().isEmpty(),
                restoredHudState,
                restoredMissionLogState,
                restoredTerminalPowerState,
                restoredInventoryState,
                EchoStandalonePlayableVoxelSaveCodec.hasUniquePropertyKeys(autosaveMissionText),
                autosave.manifest().metadata().getOrDefault("saveKind", "none"),
                frame.checksum()
        );
        return result;
    }

    private static boolean sameHotbar(
            dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar expected,
            dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar actual
    ) {
        if (expected.selectedSlot() != actual.selectedSlot() || expected.slots().size() != actual.slots().size()) {
            return false;
        }
        for (int index = 0; index < expected.slots().size(); index++) {
            var expectedSlot = expected.slots().get(index);
            var actualSlot = actual.slots().get(index);
            if (expectedSlot.empty() != actualSlot.empty() || expectedSlot.count() != actualSlot.count()) {
                return false;
            }
            if (!expectedSlot.empty() && !expectedSlot.block().id().equals(actualSlot.block().id())) {
                return false;
            }
        }
        return true;
    }

    static EchoSaveRuntimeResult openSave(Path saveRoot) throws IOException {
        return openSave(new EchoDefaultRuntimeServiceRegistry(), saveRoot);
    }

    static EchoSaveRuntimeResult openSave(EchoRuntimeServiceRegistry services, Path saveRoot) throws IOException {
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.live_save.v1",
                "profile-ashfall-live",
                "Ashfall Live",
                "echoashfallprotocol:standalone_beta",
                1,
                saveRoot,
                Map.of("runtime", "standalone", "mode", "live_game_window")
        );
        return new EchoSaveRuntime().open(services, profile);
    }
}
