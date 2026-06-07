package dev.echo.standalone.runtime.client;

final class EchoClientSlotGridRuntimeController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientSlotGridController slotGrid;
    private final EchoClientGameplayRuntimeController gameplayRuntime;

    EchoClientSlotGridRuntimeController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientSlotGridController slotGrid,
            EchoClientGameplayRuntimeController gameplayRuntime
    ) {
        if (runtimeServices == null) {
            throw new IllegalArgumentException("runtimeServices must not be null");
        }
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        if (slotGrid == null) {
            throw new IllegalArgumentException("slotGrid must not be null");
        }
        if (gameplayRuntime == null) {
            throw new IllegalArgumentException("gameplayRuntime must not be null");
        }
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.slotGrid = slotGrid;
        this.gameplayRuntime = gameplayRuntime;
    }

    void clearDrag() {
        slotGrid.clearDrag();
    }

    boolean updateIfOpen(
            InputGate input,
            EchoClientUiViewport viewport,
            double dt,
            EchoClientGameplayRuntimeController.Host host
    ) {
        if (!screens.slotGridOpen()) {
            return false;
        }
        updateSlotGrid(input, viewport);
        tickPassiveGameplay(input, dt, host);
        return true;
    }

    private void updateSlotGrid(InputGate input, EchoClientUiViewport viewport) {
        if (input == null || viewport == null) {
            return;
        }
        input.unlockCursor();
        input.updatePointer();
        slotGrid.update(new EchoClientSlotGridController.SlotGridInput(
                input.closeRequested(),
                input.primaryClick(),
                input.secondaryClick(),
                input.primaryDown(),
                input.secondaryDown(),
                input.dropRequested(),
                input.dropStackRequested(),
                input.hotbarSlotKey(),
                input.shiftDown(),
                viewport.logicalWidth(),
                viewport.logicalHeight(),
                viewport.logicalPointerX(input.pointerX()),
                viewport.logicalPointerY(input.pointerY())
        ));
        input.clearGameplayTriggers();
    }

    private void tickPassiveGameplay(
            InputGate input,
            double dt,
            EchoClientGameplayRuntimeController.Host host
    ) {
        if (!runtimeServices.hasActiveWorld()) {
            return;
        }
        gameplayRuntime.tickPassiveWorld(dt, host);
        if (screens.state() == EchoClientGameState.DEAD && input != null) {
            input.clearGameplayTriggers();
        }
    }

    interface InputGate {
        void unlockCursor();

        void updatePointer();

        boolean closeRequested();

        boolean primaryClick();

        boolean secondaryClick();

        boolean primaryDown();

        boolean secondaryDown();

        boolean dropRequested();

        boolean dropStackRequested();

        int hotbarSlotKey();

        boolean shiftDown();

        double pointerX();

        double pointerY();

        void clearGameplayTriggers();
    }
}
