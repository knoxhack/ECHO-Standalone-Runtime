package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoClientToolMiningSmokeHarness {
    private EchoClientToolMiningSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireToolModelAndHud();
        requireLiveToolMiningAndDebug();
        requireToolCrafting();
        requireDiskRestore();
        System.out.println("client tool mining smoke PASS tool=restored durability=live");
    }

    private static void requireToolModelAndHud() {
        EchoVoxelBlock hardBlock = new EchoVoxelBlock(
                "echoashfallprotocol:debug_scrap_ore",
                "Debug Scrap Ore",
                0xFF777777,
                true,
                true,
                3.0D
        );
        EchoClientToolStatus status = EchoClientToolState.empty().status(salvagePick(), hardBlock);
        require(status.activeTool(),
                "Salvage pick should be detected as a live mining tool");
        require(status.miningSpeed() > EchoClientToolStatus.hand().miningSpeed(),
                "Salvage pick should mine faster than hand speed");
        require(EchoClientHud.toolDurabilityFillPixels(status, 100) == 100,
                "Full durability tool should fill the HUD durability meter");

        EchoClientToolState damaged = EchoClientToolState.empty().damage(salvagePick(), 16);
        EchoClientToolStatus damagedStatus = damaged.status(salvagePick(), hardBlock);
        require(damagedStatus.durability() == status.maxDurability() - 16,
                "Tool durability state should track item wear by item id");
        require(EchoClientHud.toolDurabilityFillPixels(damagedStatus, 64) < 64,
                "Damaged tool should partially fill the HUD durability meter");
        EchoClientToolState oneWear = EchoClientToolState.empty().damage(salvagePick(), 1);
        EchoClientToolState twoWear = EchoClientToolState.empty().damage(salvagePick(), 2);
        EchoClientSlotStack oneWearSlot =
                EchoClientSlotStack.fromItemStack(1, new dev.echo.standalone.runtime.item.EchoItemStack(salvagePick(), 1), oneWear);
        EchoClientSlotStack twoWearSlot =
                EchoClientSlotStack.fromItemStack(1, new dev.echo.standalone.runtime.item.EchoItemStack(salvagePick(), 1), twoWear);
        require(oneWearSlot.itemModelPredicates().equals(twoWearSlot.itemModelPredicates()),
                "Near-identical tool wear should share item icon predicate buckets");
        require(oneWearSlot.durability() != twoWearSlot.durability(),
                "Slot model should keep exact durability while bucketing item icon predicates");
    }

    private static void requireLiveToolMiningAndDebug() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("tool-live").gameSession();
        require(session.quickMoveContainerSlotToPlayer(4).success(),
                "Crash cache should expose a starter mining tool");
        session.hotbar().select(1);
        session.player().selectSlot(1);
        EchoVoxelBlock target = session.bridge().runtimeMarkerBlock();
        EchoClientToolStatus before = session.selectedToolStatus(target);
        require(before.activeTool() && before.itemId().equals("echoashfallprotocol:salvage_pick"),
                "Selected starter tool should become the live mining tool");
        require(session.selectedMiningSpeed(target) > 1.0D,
                "Selected starter tool should increase live mining speed");
        EchoClientSlotStack fullToolSlot = session.inventoryScreenModel().slot(1);
        Map<String, Double> fullToolPredicates = fullToolSlot.itemModelPredicates();
        require(fullToolPredicates.getOrDefault("damage", -1.0D) == 0.0D
                        && fullToolPredicates.getOrDefault("damaged", -1.0D) == 0.0D,
                "Inventory model should expose undamaged item model predicates for live tools");
        require(fullToolSlot.durabilityTracked()
                        && fullToolSlot.durability() == before.durability()
                        && fullToolSlot.maxDurability() == before.maxDurability(),
                "Inventory model should expose live tool durability metadata for slot bars");
        require(fullToolSlot.tooltipLines().contains("Durability 64/64"),
                "Inventory model should include full durability in live tool tooltip lines");

        int beforeXp = session.progressionState().experience();
        int beforeBlockCount = session.inventoryScreenModel().slot(0).count();
        session.recordBlockBroken(target);
        EchoClientToolStatus after = session.selectedToolStatus(target);
        EchoClientSlotStack wornToolSlot = session.inventoryScreenModel().slot(1);
        Map<String, Double> wornToolPredicates = wornToolSlot.itemModelPredicates();
        require(after.durability() < before.durability(),
                "Mining a block should wear the selected tool");
        require(wornToolPredicates.getOrDefault("damage", 0.0D) > 0.0D
                        && wornToolPredicates.getOrDefault("damage", 1.0D) < 1.0D
                        && wornToolPredicates.getOrDefault("damaged", 0.0D) == 1.0D,
                "Inventory model should convert live tool wear into item model override predicates");
        require(wornToolSlot.durability() == after.durability()
                        && wornToolSlot.maxDurability() == after.maxDurability()
                        && wornToolSlot.tooltipLines().contains(
                                "Durability " + after.durability() + "/" + after.maxDurability()),
                "Inventory model should refresh live tool durability tooltip lines after mining wear");
        require(session.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:salvage_pick"),
                "Mining with an item-only selected slot should not overwrite the tool with block drops");
        require(session.inventoryScreenModel().slot(0).count() == beforeBlockCount + 1,
                "Mined block drops should merge into the item-runtime inventory");
        require(session.droppedItemCount() == 0 && session.droppedItemQuantity() == 0,
                "Mined block pickup should not churn transient dropped item entities");
        require(session.progressionState().experience() > beforeXp,
                "Tool-assisted mining should still award progression XP");
        require(session.inventoryScreenModel().slot(1).itemSlot()
                        && session.inventoryScreenModel().selectedSlot() == 1,
                "In-world hotbar model should expose selected item-runtime tools");

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("TOOL echoashfallprotocol:salvage_pick DUR " + after.durability()),
                "Debug overlay should expose selected tool durability");
        require(debug.contains(" SPD "),
                "Debug overlay should expose selected tool speed");
        require(!debug.contains(","),
                "Tool debug line should preserve the HUD font punctuation contract");
    }

    private static void requireToolCrafting() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("tool-craft").gameSession();
        require(session.quickMoveContainerSlotToPlayer(1).success(),
                "Tool crafting smoke should move scrap metal into player inventory");
        EchoItemCraftResult result = session.craftWorkbenchRecipe("echoscreencore:starter_salvage_pick");
        require(result.crafted(),
                "Workbench should craft the starter salvage pick recipe");
        require(session.inventoryScreenModel().slots().stream()
                        .anyMatch(slot -> slot.runtimeId().equals("echoashfallprotocol:salvage_pick")),
                "Crafted salvage pick should appear in the item-runtime inventory model");
    }

    private static void requireDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-tool-mining-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_tool_profile.v1",
                "client-tool-smoke",
                "Client Tool Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-tool"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("tool-save-smoke");
        EchoClientGameSession session = worldSession.gameSession();
        require(session.quickMoveContainerSlotToPlayer(4).success(),
                "Tool save smoke should move starter tool into player inventory");
        session.hotbar().select(1);
        session.player().selectSlot(1);
        session.recordBlockBroken(session.bridge().runtimeMarkerBlock());
        int savedDurability = session.selectedToolStatus(session.bridge().runtimeMarkerBlock()).durability();

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-tool-save", "tool-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.TOOLS_PATH).isPresent(),
                "Client save manifest should include tool durability state");
        require(manifest.metadata().getOrDefault("clientToolsCodec", "").equals("echo.client.tools.v1"),
                "Client save manifest should advertise the tools codec");

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
        EchoClientToolStatus restoredTool =
                restored.gameSession().selectedToolStatus(restored.gameSession().bridge().runtimeMarkerBlock());
        require(restoredTool.itemId().equals("echoashfallprotocol:salvage_pick"),
                "Disk restore should preserve the selected tool item");
        require(restoredTool.durability() == savedDurability,
                "Disk restore should preserve selected tool durability");
    }

    private static EchoItemDefinition salvagePick() {
        return new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:salvage_pick"),
                "Salvage Pick",
                EchoItemCategory.TOOL,
                1,
                1.0D,
                List.of("tool", "mining", "pickaxe", "salvage"),
                List.of("Mines hard Ashfall debris faster")
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
