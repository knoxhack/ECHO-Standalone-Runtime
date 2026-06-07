package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;

import java.util.Objects;

public final class EchoPlayerInventoryShortcuts {
    private static final EchoInventoryId PLAYER_PACK = new EchoInventoryId("inventory:player-001");
    private static final EchoItemId WATER_RATION = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);

    private final EchoGameplayRuntimeResult gameplay;
    private final EchoItemRuntimeResult items;
    private final EchoEntityId playerId;

    public EchoPlayerInventoryShortcuts(
            EchoGameplayRuntimeResult gameplay,
            EchoItemRuntimeResult items,
            EchoEntityId playerId
    ) {
        this.gameplay = Objects.requireNonNull(gameplay, "gameplay");
        this.items = Objects.requireNonNull(items, "items");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public EchoPlayerInventoryShortcutResult useSlot(int slotIndex) {
        if (slotIndex != 0) {
            return new EchoPlayerInventoryShortcutResult(slotIndex, "", false, "unmapped_slot");
        }
        int before = items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION);
        EchoGameplayInteractionResult water = gameplay.interactionSystem().drinkWater(playerId);
        int after = items.operations().count(items.inventoryStore().require(PLAYER_PACK), WATER_RATION);
        return new EchoPlayerInventoryShortcutResult(
                slotIndex,
                WATER_RATION.value(),
                water.success() && after < before,
                water.reason()
        );
    }
}
