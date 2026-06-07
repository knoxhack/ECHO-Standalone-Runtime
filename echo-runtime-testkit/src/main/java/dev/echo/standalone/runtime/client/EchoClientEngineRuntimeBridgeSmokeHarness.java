package dev.echo.standalone.runtime.client;

public final class EchoClientEngineRuntimeBridgeSmokeHarness {
    private EchoClientEngineRuntimeBridgeSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(services, screens, worldSessions);
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(services, screens);
        EchoGlfwWindow window = new EchoGlfwWindow("bridge-smoke", 320, 240);
        EchoClientRenderRuntimeController renderRuntime = new EchoClientRenderRuntimeController(
                window,
                services,
                screens,
                gameplayRuntime,
                slotGrid
        );
        EchoClientShellRuntimeController shellRuntime = new EchoClientShellRuntimeController(screens, worldSessions);
        EchoClientSlotGridRuntimeController slotGridRuntime =
                new EchoClientSlotGridRuntimeController(services, screens, slotGrid, gameplayRuntime);
        RecordingCloseTarget closeTarget = new RecordingCloseTarget();
        EchoClientEngineRuntimeBridge bridge = new EchoClientEngineRuntimeBridge(
                renderRuntime,
                shellRuntime,
                slotGridRuntime,
                closeTarget
        );
        RecordingInputSource input = new RecordingInputSource();
        bridge.attachInputSource(input);

        input.screenshot = true;
        require(bridge.screenshotInputGate().consumeScreenshotRequest(),
                "Bridge should route screenshot input to screenshot runtime gate");
        require(!input.screenshot, "Bridge screenshot gate should consume the request");

        bridge.shellInputGate().unlockCursor();
        bridge.shellInputGate().clearGameplayTriggers();
        require(input.unlocks == 1 && input.clears == 1,
                "Bridge should route shell input gates to the active input source");

        input.close = true;
        input.primary = true;
        input.secondary = true;
        input.primaryDown = true;
        input.secondaryDown = true;
        input.drop = true;
        input.hotbarSlot = 4;
        input.shift = true;
        input.control = true;
        input.pointerX = 12.5D;
        input.pointerY = 42.25D;
        bridge.slotGridInputGate().updatePointer();
        require(bridge.slotGridInputGate().closeRequested(), "Bridge should route slot-grid close requests");
        require(bridge.slotGridInputGate().primaryClick(), "Bridge should route slot-grid primary click");
        require(bridge.slotGridInputGate().secondaryClick(), "Bridge should route slot-grid secondary click");
        require(bridge.slotGridInputGate().primaryDown(), "Bridge should route slot-grid primary held state");
        require(bridge.slotGridInputGate().secondaryDown(), "Bridge should route slot-grid secondary held state");
        require(bridge.slotGridInputGate().dropRequested(), "Bridge should route slot-grid drop requests");
        require(bridge.slotGridInputGate().dropStackRequested(), "Bridge should route slot-grid drop-stack modifiers");
        require(bridge.slotGridInputGate().hotbarSlotKey() == 4, "Bridge should route hotbar slot keys");
        require(bridge.slotGridInputGate().shiftDown(), "Bridge should route shift state");
        require(bridge.slotGridInputGate().pointerX() == 12.5D
                        && bridge.slotGridInputGate().pointerY() == 42.25D,
                "Bridge should route logical pointer coordinates");
        bridge.slotGridInputGate().clearGameplayTriggers();
        require(input.pointerUpdates == 1 && input.clears == 2,
                "Bridge should route slot-grid pointer updates and trigger clearing");

        bridge.commandHost().unlockCursor();
        bridge.commandHost().requestClose();
        bridge.commandHost().reloadMinecraftAssets(false);
        bridge.commandHost().attachSession();
        require(input.unlocks == 2, "Bridge should route command cursor unlocks");
        require(closeTarget.closeRequests == 1, "Bridge should route command close requests");

        screens.showSaving();
        bridge.commandHost().beginSaving();
        require(shellRuntime.updateBlockingFlow(bridge.shellInputGate(), 0.30D, bridge.shellRuntimeHost()),
                "Bridge command save transition should feed the shell runtime");
        require(screens.state() == EchoClientGameState.PAUSED,
                "Bridge shell host should let saving return to pause after its overlay");

        bridge.gameplayRuntimeHost().clearInventoryDrag();
        bridge.gameplayRuntimeHost().refreshWorldStreamingAndMeshes();
        bridge.gameplayRuntimeHost().attachSession();

        System.out.println("client engine runtime bridge smoke PASS closeRequests="
                + closeTarget.closeRequests
                + " unlocks=" + input.unlocks
                + " clears=" + input.clears);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingInputSource implements EchoClientEngineRuntimeBridge.InputSource {
        private boolean screenshot;
        private boolean close;
        private boolean primary;
        private boolean secondary;
        private boolean primaryDown;
        private boolean secondaryDown;
        private boolean drop;
        private int hotbarSlot = -1;
        private boolean shift;
        private boolean control;
        private double pointerX;
        private double pointerY;
        private int unlocks;
        private int pointerUpdates;
        private int clears;

        @Override
        public boolean consumeScreenshot() {
            boolean value = screenshot;
            screenshot = false;
            return value;
        }

        @Override
        public void unlockCursor() {
            unlocks++;
        }

        @Override
        public void updatePointer() {
            pointerUpdates++;
        }

        @Override
        public boolean consumeSlotGridClose() {
            boolean value = close;
            close = false;
            return value;
        }

        @Override
        public boolean consumeUiPrimaryClick() {
            boolean value = primary;
            primary = false;
            return value;
        }

        @Override
        public boolean consumeUiSecondaryClick() {
            boolean value = secondary;
            secondary = false;
            return value;
        }

        @Override
        public boolean uiPrimaryDown() {
            return primaryDown;
        }

        @Override
        public boolean uiSecondaryDown() {
            return secondaryDown;
        }

        @Override
        public int consumeHotbarSlotKeyPress() {
            int value = hotbarSlot;
            hotbarSlot = -1;
            return value;
        }

        @Override
        public boolean consumeInventoryDrop() {
            boolean value = drop;
            drop = false;
            return value;
        }

        @Override
        public boolean shiftDown() {
            return shift;
        }

        @Override
        public boolean controlDown() {
            return control;
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
            clears++;
        }
    }

    private static final class RecordingCloseTarget implements EchoClientEngineRuntimeBridge.CloseTarget {
        private int closeRequests;

        @Override
        public void requestClose() {
            closeRequests++;
        }
    }
}
