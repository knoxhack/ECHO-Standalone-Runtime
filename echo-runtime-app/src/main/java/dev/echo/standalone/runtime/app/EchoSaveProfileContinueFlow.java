package dev.echo.standalone.runtime.app;

public record EchoSaveProfileContinueFlow(
        String selectedSlotId,
        String selectedSaveKind,
        boolean newGameAvailable,
        boolean continueAvailable,
        boolean autosaveAvailable,
        boolean manualSaveAvailable
) {
    public EchoSaveProfileContinueFlow {
        selectedSlotId = EchoAppText.requireText(selectedSlotId, "selectedSlotId");
        selectedSaveKind = EchoAppText.requireText(selectedSaveKind, "selectedSaveKind");
    }
}
