package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoClientPlayerSurvivalSmokeHarness {
    private EchoClientPlayerSurvivalSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireSurvivalModelAndHud();
        requireSelectedConsumableUseAndDebug();
        requireDiskRestore();
        System.out.println("client player survival smoke PASS food=restored hungerIcons=10");
    }

    private static void requireSurvivalModelAndHud() {
        EchoClientPlayerVitals regenerating =
                new EchoClientPlayerVitals(10, 20, 0, 18, 5.0D, 0.0D, 0.0D)
                        .tickSurvival(4.0D, false, false, false);
        require(regenerating.currentHealth() == 11,
                "High food should regenerate health on survival tick");
        require(regenerating.exhaustion() >= 1.5D,
                "Regeneration should add exhaustion");

        EchoClientPlayerVitals starving =
                new EchoClientPlayerVitals(3, 20, 0, 0, 0.0D, 0.0D, 0.0D)
                        .tickSurvival(4.0D, false, false, false);
        require(starving.currentHealth() == 2 && starving.lastDamage() == 1,
                "Empty food should apply starvation damage");

        EchoClientPlayerVitals depleted =
                new EchoClientPlayerVitals(20, 20, 0, 2, 0.0D, 0.0D, 0.0D)
                        .exhaust(4.1D);
        require(depleted.foodLevel() == 1,
                "Exhaustion should drain food when saturation is empty");

        int[] hunger = EchoClientHud.hungerFillStates(
                new EchoClientPlayerVitals(20, 20, 0, 15, 0.0D, 0.0D, 0.0D)
        );
        require(hunger.length == 10,
                "Default player hunger should render ten food slots");
        require(hunger[0] == 2 && hunger[6] == 2 && hunger[7] == 1 && hunger[8] == 0 && hunger[9] == 0,
                "Food fill states should represent full half and empty hunger icons");
    }

    private static void requireSelectedConsumableUseAndDebug() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("survival-use").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        session.damagePlayer(2);
        session.playerInventory().slot(1).setStack(new EchoItemStack(cleanWater(), 2));
        session.hotbar().select(1);
        session.player().selectSlot(1);

        require(session.consumeSelectedConsumable(),
                "Selected consumable item should be usable from the selected inventory slot");
        require(session.playerVitals().currentHealth() == 19,
                "Hydration consumable should heal a small amount when health is not full");
        require(session.playerInventory().slot(1).stack().orElseThrow().quantity() == 1,
                "Consuming an item should decrement the selected stack");

        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("FOOD 20 OF 20 SAT"),
                "Debug overlay should expose food and saturation state");
        require(!debug.contains(","),
                "Food debug line should preserve the HUD font punctuation contract");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-survival-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_survival_profile.v1",
                "client-survival-smoke",
                "Client Survival Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-survival"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("survival-save-smoke");
        worldSession.gameSession().tickPlayerSurvival(
                480.0D,
                new EchoVoxelPlayerInput(true, false, false, false, false, false, true, 0.0D, 0.0D)
        );

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-survival-save", "survival-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.metadata().getOrDefault("clientVitalsCodec", "").equals("echo.client.vitals.v2"),
                "Client save manifest should advertise the survival-aware vitals codec");

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
        require(restored.gameSession().playerVitals().foodLevel() == 19,
                "Disk save restore should preserve depleted food level");
        require(restored.gameSession().playerVitals().saturation() == 0.0D,
                "Disk save restore should preserve depleted saturation");
    }

    private static EchoItemDefinition cleanWater() {
        return new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:clean_water_bottle"),
                "Clean Water Bottle",
                EchoItemCategory.CONSUMABLE,
                4,
                1.0D,
                List.of("consumable", "hydration"),
                List.of("Restores hydration and a little hunger")
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
