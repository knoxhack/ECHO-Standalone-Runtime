package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class EchoClientCombatEquipmentSmokeHarness {
    private EchoClientCombatEquipmentSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireArmorEquipMitigationAndHud();
        requireGameModeRules();
        requireDiskRestore();
        System.out.println("client combat equipment smoke PASS armor=5 mode=restored");
    }

    private static void requireArmorEquipMitigationAndHud() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("combat-equipment").gameSession();
        require(session.quickMoveContainerSlotToPlayer(3).success(),
                "Crash cache should expose a starter armor item through the live container flow");
        require(session.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Starter armor should move into the player inventory as an item stack");
        session.hotbar().select(1);
        session.player().selectSlot(1);
        require(session.equipSelectedArmor(),
                "Selected armor item should equip into the matching armor slot");
        require(session.inventoryScreenModel().slot(1).empty(),
                "Equipping armor should remove one item from the selected inventory slot");
        require(session.playerCombatState().equipment().armorPoints() == 5,
                "Scrap vest should infer chest armor points from item tags");

        int[] armor = EchoClientHud.armorFillStates(session.playerCombatState());
        require(armor.length == 10 && armor[0] == 2 && armor[1] == 2 && armor[2] == 1 && armor[3] == 0,
                "Armor HUD states should represent full half and empty armor icons");

        EchoClientArmorPiece before = session.playerCombatState().equipment()
                .piece(EchoClientArmorSlot.CHEST)
                .orElseThrow();
        session.damagePlayer(EchoClientDamageSource.hostile("test:raider"), 10);
        require(session.playerVitals().currentHealth() == 12,
                "Armor should mitigate hostile damage before health is reduced");
        EchoClientArmorPiece after = session.playerCombatState().equipment()
                .piece(EchoClientArmorSlot.CHEST)
                .orElseThrow();
        require(after.durability() == before.durability() - 1,
                "Armor should lose durability when it mitigates damage");
        require(session.playerCombatState().lastDamageSource().id().equals("echo:hostile/test:raider"),
                "Typed hostile damage source should be remembered");

        int durabilityAfterHostile = after.durability();
        session.damagePlayer(EchoClientDamageSource.starvation(), 2);
        require(session.playerVitals().currentHealth() == 10,
                "Starvation should bypass armor mitigation");
        require(session.playerCombatState().equipment().piece(EchoClientArmorSlot.CHEST).orElseThrow().durability()
                        == durabilityAfterHostile,
                "Armor durability should not change for bypass-armor sources");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("MODE SURVIVAL ARMOR 5 SRC minecraft:starve"),
                "Debug overlay should expose game mode armor and last damage source");
        require(!debug.contains(","),
                "Combat debug line should preserve the HUD font punctuation contract");
    }

    private static void requireGameModeRules() {
        EchoClientGameSession creative = EchoClientWorldSessionFactory.defaultFactory().newWorld("creative-mode").gameSession();
        creative.setGameMode(EchoClientGameMode.CREATIVE);
        creative.damagePlayer(EchoClientDamageSource.hostile("test:creative_guard"), 20);
        require(creative.playerVitals().currentHealth() == EchoClientPlayerVitals.DEFAULT_MAX_HEALTH,
                "Creative mode should ignore normal hostile damage");
        creative.tickPlayerSurvival(
                600.0D,
                new EchoVoxelPlayerInput(true, false, false, false, false, false, true, 0.0D, 0.0D)
        );
        require(creative.playerVitals().foodLevel() == EchoClientPlayerVitals.DEFAULT_MAX_FOOD,
                "Creative mode should not drain hunger");
        require(EchoClientGameMode.CREATIVE.allowsBlockBreaking()
                        && EchoClientGameMode.CREATIVE.allowsBlockPlacing()
                        && !EchoClientGameMode.CREATIVE.consumesPlacedItems(),
                "Creative mode should allow free break/place actions");
        require(!EchoClientGameMode.ADVENTURE.allowsBlockBreaking()
                        && !EchoClientGameMode.ADVENTURE.allowsBlockPlacing()
                        && EchoClientGameMode.ADVENTURE.takesDamage(),
                "Adventure mode should gate block edits while keeping survival damage");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-combat-equipment-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_combat_profile.v1",
                "client-combat-smoke",
                "Client Combat Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-combat"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("combat-save-smoke");
        EchoClientGameSession session = worldSession.gameSession();
        require(session.quickMoveContainerSlotToPlayer(3).success(),
                "Combat save smoke should move armor into player inventory before equip");
        session.hotbar().select(1);
        session.player().selectSlot(1);
        require(session.equipSelectedArmor(),
                "Combat save smoke should equip starter armor before saving");
        session.hotbar().select(0);
        session.player().selectSlot(0);
        String offhandItemId = session.inventoryScreenModel().slot(0).runtimeId();
        require(session.swapSelectedWithOffhand(),
                "Combat save smoke should support selected hotbar to offhand swap");
        require(session.playerCombatState().equipment().offhand().orElseThrow().itemId().value().equals(offhandItemId),
                "Offhand swap should move the selected stack into equipment state");
        session.setGameMode(EchoClientGameMode.ADVENTURE);
        session.damagePlayer(EchoClientDamageSource.hostile("test:save_guard"), 4);

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-combat-save", "combat-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.COMBAT_PATH).isPresent(),
                "Client save manifest should include combat state");
        require(manifest.file(EchoClientGameplaySaveCodec.EQUIPMENT_PATH).isPresent(),
                "Client save manifest should include equipment state");
        require(manifest.file(EchoClientGameplaySaveCodec.OFFHAND_PATH).isPresent(),
                "Client save manifest should include offhand state");
        require(manifest.metadata().getOrDefault("clientCombatCodec", "").equals("echo.client.combat.v1"),
                "Client save manifest should advertise the combat codec");
        require(manifest.metadata().getOrDefault("clientOffhandCodec", "").equals("echo.client.offhand.v1"),
                "Client save manifest should advertise the offhand codec");

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
        require(restored.gameSession().gameMode() == EchoClientGameMode.ADVENTURE,
                "Disk restore should preserve game mode");
        require(restored.gameSession().playerCombatState().equipment().armorPoints() == 5,
                "Disk restore should preserve equipped armor");
        require(restored.gameSession().playerCombatState().equipment().offhand().orElseThrow()
                        .itemId().value().equals(offhandItemId),
                "Disk restore should preserve offhand stack identity");
        require(restored.gameSession().playerCombatState().lastDamageSource().id().equals("echo:hostile/test:save_guard"),
                "Disk restore should preserve the last typed damage source");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
