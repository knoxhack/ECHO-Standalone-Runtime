package dev.echo.standalone.runtime.client;

public final class EchoClientSlotGridRuntimeControllerSmokeHarness {
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private EchoClientSlotGridRuntimeControllerSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(services, screens, worldSessions);
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        EchoClientSlotGridRuntimeController slotGridRuntime =
                new EchoClientSlotGridRuntimeController(services, screens, slotGrid, gameplayRuntime);
        RecordingGameplayHost host = new RecordingGameplayHost();
        RecordingInputGate input = new RecordingInputGate();
        EchoClientUiViewport viewport = new EchoClientUiViewport(WIDTH, HEIGHT, WIDTH, HEIGHT, 1.0D);

        screens.showMainMenu(false);
        require(!slotGridRuntime.updateIfOpen(input, viewport, 1.0D / 20.0D, host),
                "Closed slot grid should not consume the engine update");
        require(input.cursorUnlocks == 0 && input.triggerClears == 0,
                "Closed slot grid should not touch input gates");

        services.startNewWorld("slot-grid-runtime-controller");
        screens.showInGame();
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_INVENTORY, true),
                "Inventory command should open the slot-grid screen");
        EchoClientSlotStack sourceBefore = services.inventoryScreenModel().slot(0);
        require(!sourceBefore.empty(), "Default hotbar slot 0 should provide a runtime drag source");

        input.pointAtSlot(0);
        input.primaryClick = true;
        require(slotGridRuntime.updateIfOpen(input, viewport, 1.0D / 20.0D, host),
                "Open slot grid should consume the engine update");
        require(input.cursorUnlocks == 1 && input.pointerUpdates == 1 && input.triggerClears == 1,
                "Open slot grid should unlock cursor, update pointer, and clear gameplay triggers");
        require(host.streamRefreshes == 1,
                "Open slot grid should keep passive gameplay/world streaming ticking");
        require(slotGrid.dragSlot() == 0,
                "Mapped primary click should begin dragging a runtime inventory slot");
        require(!services.cursorSlotStack().empty(),
                "Mapped primary click should lift the runtime inventory item onto the cursor stack");
        require(services.inventoryScreenModel().slot(0).empty(),
                "Mapped primary click pickup should clear the runtime source slot");

        input.resetActions();
        input.pointAtSlot(1);
        input.primaryClick = true;
        slotGridRuntime.updateIfOpen(input, viewport, 1.0D / 20.0D, host);
        require(slotGrid.dragSlot() == -1, "Mapped primary click target should complete the drag");
        require(services.cursorSlotStack().empty(),
                "Mapped primary click target should clear the runtime cursor stack");
        require(services.inventoryScreenModel().slot(0).empty(),
                "Runtime slot-grid controller should preserve inventory move side effects");
        require(services.inventoryScreenModel().slot(1).runtimeId().equals(sourceBefore.runtimeId()),
                "Runtime slot-grid controller should preserve moved item identity");

        input.resetActions();
        input.closeRequested = true;
        slotGridRuntime.updateIfOpen(input, viewport, 1.0D / 20.0D, host);
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Mapped close request should return the slot grid to gameplay");

        System.out.println("client slot grid runtime controller smoke PASS moved="
                + sourceBefore.runtimeId()
                + " passiveRefreshes=" + host.streamRefreshes);
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingInputGate implements EchoClientSlotGridRuntimeController.InputGate {
        private int cursorUnlocks;
        private int pointerUpdates;
        private int triggerClears;
        private boolean closeRequested;
        private boolean primaryClick;
        private boolean secondaryClick;
        private boolean primaryDown;
        private boolean secondaryDown;
        private boolean dropRequested;
        private boolean dropStackRequested;
        private int hotbarSlotKey = -1;
        private boolean shiftDown;
        private double pointerX;
        private double pointerY;

        void pointAtSlot(int slot) {
            pointerX = slotCenterX(slot);
            pointerY = slotCenterY(slot);
        }

        void resetActions() {
            closeRequested = false;
            primaryClick = false;
            secondaryClick = false;
            primaryDown = false;
            secondaryDown = false;
            dropRequested = false;
            dropStackRequested = false;
            hotbarSlotKey = -1;
            shiftDown = false;
        }

        @Override
        public void unlockCursor() {
            cursorUnlocks++;
        }

        @Override
        public void updatePointer() {
            pointerUpdates++;
        }

        @Override
        public boolean closeRequested() {
            return closeRequested;
        }

        @Override
        public boolean primaryClick() {
            boolean value = primaryClick;
            primaryClick = false;
            return value;
        }

        @Override
        public boolean secondaryClick() {
            boolean value = secondaryClick;
            secondaryClick = false;
            return value;
        }

        @Override
        public boolean primaryDown() {
            return primaryDown;
        }

        @Override
        public boolean secondaryDown() {
            return secondaryDown;
        }

        @Override
        public boolean dropRequested() {
            boolean value = dropRequested;
            dropRequested = false;
            return value;
        }

        @Override
        public boolean dropStackRequested() {
            return dropStackRequested;
        }

        @Override
        public int hotbarSlotKey() {
            int value = hotbarSlotKey;
            hotbarSlotKey = -1;
            return value;
        }

        @Override
        public boolean shiftDown() {
            return shiftDown;
        }

        @Override
        public double pointerX() {
            return pointerX;
        }

        @Override
        public double pointerY() {
            return pointerY;
        }

        @Override
        public void clearGameplayTriggers() {
            triggerClears++;
        }
    }

    private static final class RecordingGameplayHost implements EchoClientGameplayRuntimeController.Host {
        private int streamRefreshes;
        private int attachSessionRequests;
        private int dragClears;

        @Override
        public void clearInventoryDrag() {
            dragClears++;
        }

        @Override
        public void refreshWorldStreamingAndMeshes() {
            streamRefreshes++;
        }

        @Override
        public void attachSession() {
            attachSessionRequests++;
        }
    }
}
