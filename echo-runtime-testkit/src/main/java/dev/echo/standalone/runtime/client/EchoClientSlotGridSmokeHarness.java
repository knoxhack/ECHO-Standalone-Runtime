package dev.echo.standalone.runtime.client;

public final class EchoClientSlotGridSmokeHarness {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private EchoClientSlotGridSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);

        services.startNewWorld("slot-grid-runtime");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open the slot-grid screen");

        EchoClientSlotStack sourceBefore = services.inventoryScreenModel().slot(0);
        require(!sourceBefore.empty(), "Default hotbar slot 0 should provide a drag source");
        slotGrid.update(inputForSlot(0, true, false, -1, false));
        require(slotGrid.dragSlot() == 0, "Primary click on a filled slot should begin dragging that slot");
        require(!services.cursorSlotStack().empty(), "Primary click should lift the source item onto the cursor stack");
        require(services.inventoryScreenModel().slot(0).empty(),
                "Primary click pickup should clear the source slot while the cursor holds the stack");
        slotGrid.update(inputForSlot(1, true, false, -1, false));
        require(slotGrid.dragSlot() == -1, "Primary click target should complete the drag");
        require(services.cursorSlotStack().empty(), "Primary click target should clear the cursor stack");
        require(services.inventoryScreenModel().slot(0).empty(),
                "Drag move should clear the source slot");
        require(services.inventoryScreenModel().slot(1).runtimeId().equals(sourceBefore.runtimeId()),
                "Drag move should preserve the moved item identity");

        slotGrid.update(inputForSlot(1, true, false, -1, true));
        require(slotGrid.dragSlot() == -1, "Shift-click quick move should not leave a dragged slot");
        require(services.inventoryScreenModel().slot(1).empty(),
                "Shift-click quick move should move the item out of the selected hotbar slot");

        EchoClientSlotStack carryBefore = services.inventoryScreenModel().slot(9);
        require(!carryBefore.empty(), "Quick-moved item should land in the carry grid");
        slotGrid.update(inputForSlot(9, false, false, 2, false));
        require(slotGrid.dragSlot() == -1, "Number-key swap should not leave a dragged slot");
        require(services.inventoryScreenModel().slot(2).runtimeId().equals(carryBefore.runtimeId()),
                "Number-key swap should move the selected carry slot into the requested hotbar slot");

        EchoClientInventoryScreenModel beforeSplit = services.inventoryScreenModel();
        int splitSourceSlot = firstStackWithCountAtLeast(beforeSplit, 2);
        int splitTargetSlot = firstEmptySlot(beforeSplit);
        require(splitSourceSlot >= 0, "Slot grid smoke should find a stack large enough for secondary-click splitting");
        require(splitTargetSlot >= 0, "Slot grid smoke should find an empty target slot for cursor placement");
        EchoClientSlotStack splitSource = beforeSplit.slot(splitSourceSlot);
        int expectedCursorCount = (splitSource.count() + 1) / 2;
        slotGrid.update(inputForSlot(splitSourceSlot, false, true, -1, false));
        require(slotGrid.dragSlot() == splitSourceSlot,
                "Secondary click on a filled slot should begin holding a split cursor stack");
        require(services.cursorSlotStack().count() == expectedCursorCount,
                "Secondary click should lift half of the source stack onto the cursor");
        require(services.inventoryScreenModel().slot(splitSourceSlot).count()
                        == splitSource.count() - expectedCursorCount,
                "Secondary click should leave the other half in the source slot");
        slotGrid.update(inputForSlot(splitTargetSlot, false, true, -1, false));
        require(services.inventoryScreenModel().slot(splitTargetSlot).count() == 1,
                "Secondary click with a cursor stack should place one item into an empty target slot");
        int remainingCursor = Math.max(0, expectedCursorCount - 1);
        require(remainingCursor == 0
                        ? services.cursorSlotStack().empty()
                        : services.cursorSlotStack().count() == remainingCursor,
                "Secondary click placement should decrement the cursor stack");

        slotGrid.update(new EchoClientSlotGridController.SlotGridInput(
                true,
                false,
                false,
                -1,
                false,
                WIDTH,
                HEIGHT,
                0,
                0
        ));
        require(services.cursorSlotStack().empty(), "Closing the slot grid should return any cursor stack to inventory");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Closing an inventory slot-grid should return to gameplay");

        requireDragStackDistribution();
        requireEquipmentCursorSlots();
        requireInventoryDropShortcuts();
        requireContainerPlayerInventorySlots();

        System.out.println("client slot grid smoke PASS moved=" + sourceBefore.runtimeId());
    }

    private static void requireDragStackDistribution() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        services.startNewWorld("slot-grid-drag-distribution-runtime");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open before drag distribution tests");

        EchoClientInventoryScreenModel before = services.inventoryScreenModel();
        int sourceSlot = firstStackWithCountAtLeast(before, 4);
        int firstTargetSlot = firstEmptySlot(before);
        int secondTargetSlot = firstEmptySlotAfter(before, firstTargetSlot);
        require(sourceSlot >= 0, "Drag distribution smoke should find a stack with enough items");
        require(firstTargetSlot >= 0 && secondTargetSlot >= 0,
                "Drag distribution smoke should find two empty target slots");
        EchoClientSlotStack source = before.slot(sourceSlot);

        slotGrid.update(inputForSlot(sourceSlot, true, false, -1, false));
        require(services.cursorSlotStack().count() == source.count(),
                "Drag distribution pickup should lift the whole source stack to the cursor");
        slotGrid.update(inputHeldForSlot(firstTargetSlot, true, false));
        require(services.inventoryScreenModel().slot(firstTargetSlot).count() == 1,
                "Held primary drag should place one item into the first swept slot");
        require(services.cursorSlotStack().count() == source.count() - 1,
                "Held primary drag should decrement the cursor after the first swept slot");
        slotGrid.update(inputHeldForSlot(firstTargetSlot, true, false));
        require(services.inventoryScreenModel().slot(firstTargetSlot).count() == 1,
                "Held primary drag should not repeatedly drain into the same swept slot");
        require(services.cursorSlotStack().count() == source.count() - 1,
                "Repeated hover on one swept slot should preserve the cursor count");
        slotGrid.update(inputHeldForSlot(secondTargetSlot, true, false));
        require(services.inventoryScreenModel().slot(secondTargetSlot).count() == 1,
                "Held primary drag should place one item into the next swept slot");
        require(services.cursorSlotStack().count() == source.count() - 2,
                "Held primary drag should decrement the cursor for each newly swept slot");
        slotGrid.update(inputReleaseForSlot(secondTargetSlot));
    }

    private static void requireEquipmentCursorSlots() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        services.startNewWorld("slot-grid-equipment-runtime");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open before equipment slot cursor tests");
        require(services.session().quickMoveContainerSlotToPlayer(3).success(),
                "Equipment cursor smoke should move starter armor into the player inventory");
        require(services.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Equipment cursor smoke should expose starter armor in inventory slot 1");
        require(services.equipmentScreenModel().slot(EchoClientArmorSlot.CHEST).empty(),
                "Equipment model should start with an empty chest armor slot");

        slotGrid.update(inputForSlot(1, true, false, -1, false));
        require(services.cursorSlotStack().runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Primary click should lift armor from inventory onto the cursor");
        slotGrid.update(inputForEquipmentSlot(EchoClientArmorSlot.CHEST, true, false));
        require(services.cursorSlotStack().empty(),
                "Clicking a matching equipment slot should consume the cursor armor stack");
        require(services.equipmentScreenModel().slot(EchoClientArmorSlot.CHEST)
                        .runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Equipment model should show the equipped armor stack");
        require(services.session().playerCombatState().equipment().armorPoints() == 5,
                "Equipment slot click should update combat armor points");

        slotGrid.update(inputForEquipmentSlot(EchoClientArmorSlot.CHEST, true, false));
        require(services.cursorSlotStack().runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Clicking an occupied equipment slot with an empty cursor should unequip to cursor");
        require(services.equipmentScreenModel().slot(EchoClientArmorSlot.CHEST).empty(),
                "Equipment model should clear after unequipping armor");
        require(services.session().playerCombatState().equipment().armorPoints() == 0,
                "Unequipping through the equipment slot should remove armor points");
        slotGrid.update(inputForSlot(1, true, false, -1, false));
        require(services.cursorSlotStack().empty(),
                "Placing unequipped armor back into inventory should clear the cursor");
        require(services.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:scrap_vest"),
                "Unequipped armor should return to the selected inventory slot");

        EchoClientSlotStack offhandSource = services.inventoryScreenModel().slot(0);
        require(!offhandSource.empty(), "Offhand cursor smoke should find a starter stack in slot 0");
        slotGrid.update(inputForSlot(0, true, false, -1, false));
        require(services.cursorSlotStack().runtimeId().equals(offhandSource.runtimeId()),
                "Primary click should lift the offhand source stack onto the cursor");
        slotGrid.update(inputForOffhandSlot(true, false));
        require(services.cursorSlotStack().empty(),
                "Clicking the offhand slot should place the cursor stack into offhand");
        require(services.equipmentScreenModel().offhandSlot().runtimeId().equals(offhandSource.runtimeId()),
                "Equipment model should show the offhand stack after placement");
        require(services.inventoryScreenModel().slot(0).empty(),
                "Placing into offhand should clear the inventory source slot");
        slotGrid.update(inputForOffhandSlot(true, false));
        require(services.cursorSlotStack().runtimeId().equals(offhandSource.runtimeId()),
                "Clicking a filled offhand slot with an empty cursor should pick it back up");
        slotGrid.update(inputForSlot(0, true, false, -1, false));
        require(services.cursorSlotStack().empty(),
                "Returning the offhand stack to inventory should clear the cursor");
        require(services.equipmentScreenModel().offhandSlot().empty(),
                "Equipment model should clear the offhand slot after pickup");
    }

    private static void requireInventoryDropShortcuts() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        services.startNewWorld("slot-grid-drop-cursor-runtime");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open before cursor drop tests");
        EchoClientSlotStack source = services.inventoryScreenModel().slot(0);
        require(!source.empty(), "Cursor drop smoke should find a starter stack in slot 0");
        slotGrid.update(inputForSlot(0, true, false, -1, false));
        require(services.inventoryScreenModel().slot(0).empty(),
                "Cursor drop smoke should lift the stack out of the source slot");
        slotGrid.update(inputDropForSlot(0, false));
        require(services.session().droppedItemQuantity() == 1,
                "Q with a cursor stack should create a one-item dropped entity");
        int expectedCursorCount = source.count() - 1;
        require(expectedCursorCount == 0
                        ? services.cursorSlotStack().empty()
                        : services.cursorSlotStack().count() == expectedCursorCount,
                "Q with a cursor stack should remove one item from the cursor");

        EchoClientRuntimeServices stackServices = new EchoClientRuntimeServices();
        EchoClientScreenController stackScreens = new EchoClientScreenController();
        EchoClientSlotGridController stackGrid = new EchoClientSlotGridController(stackServices, stackScreens);
        stackServices.startNewWorld("slot-grid-drop-stack-runtime");
        stackScreens.showInGame();
        require(stackScreens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open before hovered stack drop tests");
        EchoClientSlotStack hovered = stackServices.inventoryScreenModel().slot(0);
        require(!hovered.empty(), "Hovered stack drop smoke should find a starter stack in slot 0");
        stackGrid.update(inputDropForSlot(0, true));
        require(stackServices.inventoryScreenModel().slot(0).empty(),
                "Stack drop should clear the hovered inventory slot");
        require(stackServices.session().droppedItemQuantity() == hovered.count(),
                "Stack drop should create a dropped entity with the full hovered stack count");
    }

    private static void requireContainerPlayerInventorySlots() {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        services.startNewWorld("slot-grid-container-runtime");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTAINER, true),
                "Container command should open the slot-grid container screen");
        require(screens.containerOpen(), "Container smoke should be on the container screen");

        EchoClientSlotStack containerWater = services.containerScreenModel().slot(0);
        require(containerWater.runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && containerWater.count() == 2,
                "Container smoke should start with clean water in container slot 0");
        require(services.inventoryScreenModel().slot(1).empty(),
                "Container smoke expects player inventory slot 1 to be available");

        slotGrid.update(inputForContainerSlot(0, true, false, -1, false));
        require(services.containerScreenModel().slot(0).empty(),
                "Primary click should lift the top container slot onto the cursor");
        require(services.cursorSlotStack().runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && services.cursorSlotStack().count() == 2,
                "Primary click on a container slot should hold the container stack");
        require(slotGrid.dragSlot() == EchoClientInventoryLayout.containerSlotIndex(0),
                "Dragging from the container should expose a container-scoped drag slot key");

        slotGrid.update(inputForContainerPlayerSlot(1, true, false, -1, false));
        require(services.cursorSlotStack().empty(),
                "Placing into the lower player hotbar should clear the cursor");
        require(services.inventoryScreenModel().slot(1).runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && services.inventoryScreenModel().slot(1).count() == 2,
                "Container screen should route lower-grid clicks to the player inventory");
        require(services.containerScreenModel().slot(0).empty(),
                "Moving a stack to the player inventory should leave the source container slot empty");

        slotGrid.update(inputForContainerPlayerSlot(1, true, false, -1, true));
        require(services.inventoryScreenModel().slot(1).empty(),
                "Shift-clicking the lower player inventory while a container is open should move into the container");
        require(services.containerScreenModel().slot(0).runtimeId().equals("echoashfallprotocol:clean_water_bottle")
                        && services.containerScreenModel().slot(0).count() == 2,
                "Player-to-container quick move should restore the water stack to the container");
    }

    private static EchoClientSlotGridController.SlotGridInput inputForSlot(
            int slot,
            boolean primaryClick,
            boolean secondaryClick,
            int hotbarSlotKey,
            boolean shiftDown
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                primaryClick,
                secondaryClick,
                hotbarSlotKey,
                shiftDown,
                WIDTH,
                HEIGHT,
                slotCenterX(slot),
                slotCenterY(slot)
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputForContainerSlot(
            int slot,
            boolean primaryClick,
            boolean secondaryClick,
            int hotbarSlotKey,
            boolean shiftDown
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                primaryClick,
                secondaryClick,
                hotbarSlotKey,
                shiftDown,
                WIDTH,
                HEIGHT,
                containerSlotCenterX(slot),
                containerSlotCenterY(slot)
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputForContainerPlayerSlot(
            int slot,
            boolean primaryClick,
            boolean secondaryClick,
            int hotbarSlotKey,
            boolean shiftDown
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                primaryClick,
                secondaryClick,
                hotbarSlotKey,
                shiftDown,
                WIDTH,
                HEIGHT,
                containerPlayerSlotCenterX(slot),
                containerPlayerSlotCenterY(slot)
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputForEquipmentSlot(
            EchoClientArmorSlot slot,
            boolean primaryClick,
            boolean secondaryClick
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                primaryClick,
                secondaryClick,
                -1,
                false,
                WIDTH,
                HEIGHT,
                EchoClientInventoryLayout.equipmentX(WIDTH) + EchoClientInventoryLayout.SLOT_SIZE / 2.0D,
                EchoClientInventoryLayout.equipmentY(HEIGHT, slot) + EchoClientInventoryLayout.SLOT_SIZE / 2.0D
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputForOffhandSlot(
            boolean primaryClick,
            boolean secondaryClick
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                primaryClick,
                secondaryClick,
                -1,
                false,
                WIDTH,
                HEIGHT,
                EchoClientInventoryLayout.equipmentX(WIDTH) + EchoClientInventoryLayout.SLOT_SIZE / 2.0D,
                EchoClientInventoryLayout.offhandY(HEIGHT) + EchoClientInventoryLayout.SLOT_SIZE / 2.0D
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputHeldForSlot(
            int slot,
            boolean primaryDown,
            boolean secondaryDown
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                false,
                false,
                primaryDown,
                secondaryDown,
                false,
                false,
                -1,
                false,
                WIDTH,
                HEIGHT,
                slotCenterX(slot),
                slotCenterY(slot)
        );
    }

    private static EchoClientSlotGridController.SlotGridInput inputReleaseForSlot(int slot) {
        return inputHeldForSlot(slot, false, false);
    }

    private static EchoClientSlotGridController.SlotGridInput inputDropForSlot(
            int slot,
            boolean wholeStack
    ) {
        return new EchoClientSlotGridController.SlotGridInput(
                false,
                false,
                false,
                true,
                wholeStack,
                -1,
                wholeStack,
                WIDTH,
                HEIGHT,
                slotCenterX(slot),
                slotCenterY(slot)
        );
    }

    private static double slotCenterX(int slot) {
        int column = slot % EchoClientInventoryLayout.COLUMNS;
        return EchoClientInventoryLayout.gridX(WIDTH)
                + column * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static double slotCenterY(int slot) {
        int row = slot < EchoClientInventoryLayout.COLUMNS
                ? 0
                : (slot - EchoClientInventoryLayout.COLUMNS) / EchoClientInventoryLayout.COLUMNS;
        int gridY = slot < EchoClientInventoryLayout.COLUMNS
                ? EchoClientInventoryLayout.hotbarY(HEIGHT)
                : EchoClientInventoryLayout.carryY(HEIGHT);
        return gridY
                + row * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static double containerSlotCenterX(int slot) {
        int column = slot % EchoClientInventoryLayout.COLUMNS;
        return EchoClientInventoryLayout.containerGridX(WIDTH)
                + column * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static double containerSlotCenterY(int slot) {
        int row = slot / EchoClientInventoryLayout.COLUMNS;
        return EchoClientInventoryLayout.containerGridY(HEIGHT)
                + row * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static double containerPlayerSlotCenterX(int slot) {
        int column = slot % EchoClientInventoryLayout.COLUMNS;
        return EchoClientInventoryLayout.containerGridX(WIDTH)
                + column * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static double containerPlayerSlotCenterY(int slot) {
        int row = slot < EchoClientInventoryLayout.COLUMNS
                ? 0
                : (slot - EchoClientInventoryLayout.COLUMNS) / EchoClientInventoryLayout.COLUMNS;
        int gridY = slot < EchoClientInventoryLayout.COLUMNS
                ? EchoClientInventoryLayout.containerPlayerHotbarY(HEIGHT)
                : EchoClientInventoryLayout.containerPlayerCarryY(HEIGHT);
        return gridY
                + row * (EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING)
                + EchoClientInventoryLayout.SLOT_SIZE / 2.0D;
    }

    private static int firstStackWithCountAtLeast(EchoClientInventoryScreenModel model, int minimumCount) {
        for (int slot = 0; slot < EchoClientInventoryScreenModel.SLOT_COUNT; slot++) {
            EchoClientSlotStack stack = model.slot(slot);
            if (!stack.empty() && stack.count() >= minimumCount) {
                return slot;
            }
        }
        return -1;
    }

    private static int firstEmptySlot(EchoClientInventoryScreenModel model) {
        for (int slot = 0; slot < EchoClientInventoryScreenModel.SLOT_COUNT; slot++) {
            if (model.slot(slot).empty()) {
                return slot;
            }
        }
        return -1;
    }

    private static int firstEmptySlotAfter(EchoClientInventoryScreenModel model, int previousSlot) {
        for (int slot = Math.max(0, previousSlot + 1); slot < EchoClientInventoryScreenModel.SLOT_COUNT; slot++) {
            if (model.slot(slot).empty()) {
                return slot;
            }
        }
        return -1;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
