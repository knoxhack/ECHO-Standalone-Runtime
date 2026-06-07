package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Map;

public final class EchoClientHeldItemOverlaySmokeHarness {
    private EchoClientHeldItemOverlaySmokeHarness() {
    }

    public static void main(String[] args) {
        requireEmptyHandPlan();
        requireSelectedItemPlan();
        requireSelectedBlockPlan();
        System.out.println("client held item overlay smoke PASS hand=item_renderer selected=stable");
    }

    private static void requireEmptyHandPlan() {
        EchoClientHeldItemOverlayPlan plan = EchoClientHeldItemOverlayPlan.from(1280, 720, null);
        require(plan.visible(), "Held item overlay should render an empty-hand plan without an inventory model");
        require(plan.emptyHand(), "Missing selected stack should become an empty hand overlay");
        require(plan.size() == 78 && plan.iconSize() == 54,
                "Held item overlay should use stable hand and icon dimensions");
        require(plan.bottom() < 720 - 40,
                "Held item overlay should stay above the hotbar band on the default viewport");
    }

    private static void requireSelectedItemPlan() {
        EchoClientSlotStack item = EchoClientSlotStack.fromItemStack(
                2,
                new EchoItemStack(toolDefinition(), 1),
                new EchoClientToolState(Map.of("echoashfallprotocol:salvage_pick", 47))
        );
        EchoClientInventoryScreenModel model =
                new EchoClientInventoryScreenModel("test:inventory", "Inventory", List.of(item), 2);

        EchoClientHeldItemOverlayPlan plan = EchoClientHeldItemOverlayPlan.from(960, 540, model);

        require(!plan.emptyHand(), "Selected item stack should replace the empty hand silhouette");
        require(plan.itemSlot(), "Selected item stack should be classified as an item overlay");
        require(plan.stack().runtimeId().equals("echoashfallprotocol:salvage_pick"),
                "Held item overlay should preserve the selected item runtime id");
        require(plan.stack().itemModelPredicates().containsKey("damage"),
                "Held item overlay should carry tool model predicates for damaged handheld models");
        require(plan.iconX() >= plan.x() && plan.iconX() + plan.iconSize() <= plan.x() + plan.size(),
                "Held item icon should fit inside the overlay frame");
    }

    private static void requireSelectedBlockPlan() {
        EchoVoxelBlock block = new EchoVoxelBlock(
                "echoashfallprotocol:ashfall_scrap_panel",
                "Scrap Panel",
                0xFF7A756C,
                true,
                true,
                1.0D
        );
        EchoClientSlotStack blockStack = new EchoClientSlotStack(
                3,
                EchoClientSlotStackKind.BLOCK,
                block.id(),
                block.displayName(),
                12,
                block,
                Map.of(),
                List.of("Placeable voxel block"),
                0,
                0
        );
        EchoClientInventoryScreenModel model =
                new EchoClientInventoryScreenModel("test:inventory", "Inventory", List.of(blockStack), 3);

        EchoClientHeldItemOverlayPlan plan = EchoClientHeldItemOverlayPlan.from(640, 360, model);

        require(!plan.emptyHand(), "Selected block stack should replace the empty hand silhouette");
        require(plan.blockSlot(), "Selected block stack should be classified as a block overlay");
        require(plan.stack().block().id().equals(block.id()),
                "Held block overlay should preserve the selected block id");
        require(plan.x() >= 8 && plan.x() + plan.size() <= 640 - 8,
                "Held block overlay should stay inside narrow viewport bounds");
    }

    private static EchoItemDefinition toolDefinition() {
        return new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:salvage_pick"),
                "Salvage Pick",
                EchoItemCategory.TOOL,
                1,
                1.0D,
                List.of("tool", "pickaxe"),
                List.of("Starter mining tool")
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
