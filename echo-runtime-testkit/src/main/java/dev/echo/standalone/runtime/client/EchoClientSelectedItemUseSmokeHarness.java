package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;

import java.util.List;

public final class EchoClientSelectedItemUseSmokeHarness {
    private EchoClientSelectedItemUseSmokeHarness() {
    }

    public static void main(String[] args) {
        requireRightClickConsumableUseEvent();
        requireRightClickArmorEquipEvent();
        System.out.println("client selected item use smoke PASS consumable=used armor=equipped");
    }

    private static void requireRightClickConsumableUseEvent() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("selected-item-use-consumable").gameSession();
        session.damagePlayer(2);
        session.playerInventory().slot(1).clear();
        session.playerInventory().slot(1).setStack(new EchoItemStack(cleanWater(), 2));
        EchoClientGameplay gameplay = gameplayFor(session);

        gameplay.tick(EchoVoxelPlayerInput.idle(), new FakeGameplayInput(1), 1.0D / 60.0D, session);

        EchoClientSelectedItemUse use = gameplay.consumeSelectedItemUse();
        require(use.active() && use.action().equals("consume"),
                "Right-clicking a selected consumable should emit a consume use event");
        require(use.toastText().equals("Used Clean Water Bottle"),
                "Consumable use event should provide the live HUD toast text");
        require(session.playerVitals().currentHealth() == 19,
                "Right-clicking a consumable through gameplay should apply the consumable effect");
        require(session.playerInventory().slot(1).stack().orElseThrow().quantity() == 1,
                "Right-clicking a consumable through gameplay should decrement the selected stack");
        require(!gameplay.consumeSelectedItemUse().active(),
                "Selected item use events should be single-consume");
    }

    private static void requireRightClickArmorEquipEvent() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("selected-item-use-armor").gameSession();
        require(session.quickMoveContainerSlotToPlayer(3).success(),
                "Selected item armor smoke should move starter armor into player inventory");
        require(session.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Selected item armor smoke should expose Scrap Vest in hotbar slot 1");
        EchoClientGameplay gameplay = gameplayFor(session);

        gameplay.tick(EchoVoxelPlayerInput.idle(), new FakeGameplayInput(1), 1.0D / 60.0D, session);

        EchoClientSelectedItemUse use = gameplay.consumeSelectedItemUse();
        require(use.active() && use.action().equals("equip"),
                "Right-clicking selected armor should emit an equip use event");
        require(use.toastText().equals("Equipped Scrap Vest"),
                "Armor use event should provide the live HUD toast text");
        require(session.inventoryScreenModel().slot(1).empty(),
                "Right-clicking selected armor through gameplay should remove it from inventory");
        require(session.playerCombatState().equipment().armorPoints() == 5,
                "Right-clicking selected armor through gameplay should update equipped armor state");
    }

    private static EchoClientGameplay gameplayFor(EchoClientGameSession session) {
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        return gameplay;
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

    private static final class FakeGameplayInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean place = true;

        private FakeGameplayInput(int selectedSlot) {
            this.selectedSlot = selectedSlot;
        }

        @Override
        public int selectedHotbarSlot(int current) {
            return selectedSlot;
        }

        @Override
        public boolean consumeBreak() {
            return false;
        }

        @Override
        public boolean isCursorLocked() {
            return true;
        }

        @Override
        public boolean consumePlace() {
            boolean value = place;
            place = false;
            return value;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
