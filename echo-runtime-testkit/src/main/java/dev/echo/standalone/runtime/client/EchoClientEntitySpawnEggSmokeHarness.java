package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;

public final class EchoClientEntitySpawnEggSmokeHarness {
    private static final String SPAWN_EGG_ID = "echoashfallprotocol:rad_zombie_spawn_egg";
    private static final String ENTITY_ID = "echoashfallprotocol:rad_zombie";

    private EchoClientEntitySpawnEggSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDirectSessionSpawnEgg();
        requireGameplayRightClickSpawnEgg();
        System.out.println("client entity spawn egg smoke PASS entity=rad_zombie input=right_click");
    }

    private static void requireDirectSessionSpawnEgg() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-spawn-egg-direct").gameSession();
        putSpawnEggsInSelectedSlot(session, 1, 2);

        int beforeLiving = session.entityStore().living().size();
        int beforeEggs = itemCount(session, SPAWN_EGG_ID);
        require(session.spawnSelectedEntity(null),
                "Direct session spawn egg use should create an entity");

        require(session.entityStore().living().size() == beforeLiving + 1,
                "Direct session spawn egg use should increase the live entity count");
        EchoEntityState spawned = session.entityStore().living().getLast();
        require(spawned.definition().definitionId().equals(ENTITY_ID),
                "Spawn egg should resolve the matching catalog entity definition");
        require(session.entityStore().hostile().stream().anyMatch(entity -> entity.id().equals(spawned.id())),
                "Spawned rad zombie should be hostile and present in the hostile view");
        require(itemCount(session, SPAWN_EGG_ID) == beforeEggs - 1,
                "Survival spawn egg use should consume exactly one item");
    }

    private static void requireGameplayRightClickSpawnEgg() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-spawn-egg-gameplay").gameSession();
        putSpawnEggsInSelectedSlot(session, 2, 1);

        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        int beforeLiving = session.entityStore().living().size();

        gameplay.tick(EchoVoxelPlayerInput.idle(), new PlaceOnceInput(2), 1.0D / 60.0D, session);

        require(session.entityStore().living().size() == beforeLiving + 1,
                "Gameplay right-click should route spawn eggs before block placement");
        require(itemCount(session, SPAWN_EGG_ID) == 0,
                "Gameplay right-click spawn egg use should consume the selected stack");
        EchoClientSelectedItemUse selectedItemUse = gameplay.consumeSelectedItemUse();
        require(selectedItemUse.active()
                        && selectedItemUse.action().equals("spawn")
                        && selectedItemUse.toastText().equals("Spawned Rad Zombie Spawn Egg"),
                "Gameplay spawn egg use should emit a spawn toast action");
        require(!gameplay.isWorldDirty(),
                "Spawning an entity with an item should not mark the voxel block world dirty");
    }

    private static void putSpawnEggsInSelectedSlot(EchoClientGameSession session, int slot, int quantity) {
        session.playerInventory().slot(slot).setStack(new EchoItemStack(spawnEggDefinition(), quantity));
        session.hotbar().assignSlot(slot, EchoVoxelBlock.AIR, 0);
        session.hotbar().select(slot);
        session.player().selectSlot(slot);
        require(session.inventoryScreenModel().slot(slot).runtimeId().equals(SPAWN_EGG_ID),
                "Spawn egg should be visible as an item-runtime selected hotbar stack");
    }

    private static int itemCount(EchoClientGameSession session, String itemId) {
        return session.playerInventory().totalQuantity(new EchoItemId(itemId));
    }

    private static EchoItemDefinition spawnEggDefinition() {
        return new EchoItemDefinition(
                new EchoItemId(SPAWN_EGG_ID),
                "Rad Zombie Spawn Egg",
                EchoItemCategory.QUEST,
                16,
                1.0D,
                List.of("entity:" + ENTITY_ID, "spawn_egg"),
                List.of("Spawns a catalog-backed hostile")
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class PlaceOnceInput implements EchoClientGameplayInput {
        private final int selectedSlot;
        private boolean placePressed = true;

        private PlaceOnceInput(int selectedSlot) {
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
            boolean value = placePressed;
            placePressed = false;
            return value;
        }
    }
}
