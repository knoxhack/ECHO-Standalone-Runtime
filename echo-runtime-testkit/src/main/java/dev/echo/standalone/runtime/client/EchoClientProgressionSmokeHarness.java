package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoClientProgressionSmokeHarness {
    private EchoClientProgressionSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireProgressionModelAndHud();
        requireCraftingMiningAndDebug();
        requireDiskRestore();
        System.out.println("client progression smoke PASS level=restored xp=live");
    }

    private static void requireProgressionModelAndHud() {
        EchoClientProgressionState empty = EchoClientProgressionState.empty();
        require(empty.level() == 1 && empty.experienceIntoLevel() == 0,
                "Empty live progression should start at level one");

        EchoClientProgressionState leveled = empty
                .awardExperience(75, "smoke:first")
                .awardExperience(0, "smoke:ignored");
        require(leveled.level() == 2,
                "Progression should level up after the configured experience threshold");
        require(leveled.experienceIntoLevel() == 25,
                "Progression should expose experience inside the current level");
        require(leveled.milestones().equals(List.of("smoke:first")),
                "Progression should record non-empty milestones without zero-award entries");
        require(EchoClientHud.experienceFillPixels(leveled, 100) == 50,
                "HUD XP bar should fill by current-level progress");
        require(EchoClientHud.experienceFillPixels(null, 100) == 0,
                "HUD XP bar should tolerate missing progression state");
    }

    private static void requireCraftingMiningAndDebug() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("progression-live").gameSession();
        require(session.progressionState().level() == 1,
                "Fresh client session should start with default progression");
        require(session.quickMoveContainerSlotToPlayer(1).success(),
                "Progression smoke should move scrap metal into the player inventory");

        int beforeCraft = session.progressionState().experience();
        require(session.craftWorkbenchRecipe("echoscreencore:starter_filter_patch").crafted(),
                "Successful workbench craft should use the live item-runtime crafting path");
        require(session.progressionState().experience() > beforeCraft,
                "Successful workbench craft should award live XP");
        require(session.progressionState().milestones().contains("craft:echoscreencore:starter_filter_patch"),
                "Crafting XP should record a recipe milestone");

        int beforeMine = session.progressionState().experience();
        session.awardBlockBreakExperience(session.bridge().runtimeMarkerBlock());
        require(session.progressionState().experience() > beforeMine,
                "Mining a live voxel block should award progression XP");
        require(session.progressionState().milestones().stream().anyMatch(milestone -> milestone.startsWith("mine:")),
                "Mining XP should record a block milestone");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("XP L " + session.progressionState().level()),
                "Debug overlay should expose live XP level");
        require(debug.contains(" TOTAL " + session.progressionState().experience()),
                "Debug overlay should expose total XP");
        require(!debug.contains(","),
                "Progression debug line should preserve the HUD font punctuation contract");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-progression-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_progression_profile.v1",
                "client-progression-smoke",
                "Client Progression Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-progression"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("progression-save-smoke");
        EchoClientGameSession session = worldSession.gameSession();
        session.awardExperience(52, "smoke:first_level");
        session.awardBlockBreakExperience(session.bridge().runtimeMarkerBlock());

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-progression-save", "progression-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.PROGRESSION_PATH).isPresent(),
                "Client save manifest should include progression state");
        require(manifest.metadata().getOrDefault("clientProgressionCodec", "").equals("echo.client.progression.v1"),
                "Client save manifest should advertise the progression codec");
        require(manifest.metadata().getOrDefault("progressionLevel", "").equals(Integer.toString(session.progressionState().level())),
                "Client save manifest should expose the saved progression level");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                saves,
                manifest
        );
        EchoClientWorldSession restored = EchoClientWorldSession.fromSavedSession(
                manifest.slotId(),
                manifest.metadata().getOrDefault("displayName", manifest.slotId()),
                restoredSnapshot
        );
        require(restored.gameSession().progressionState().experience() == session.progressionState().experience(),
                "Disk restore should preserve total progression XP");
        require(restored.gameSession().progressionState().lastAward() == session.progressionState().lastAward(),
                "Disk restore should preserve the last progression award");
        require(restored.gameSession().progressionState().milestones().contains("smoke:first_level"),
                "Disk restore should preserve progression milestones");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
