package dev.echo.standalone.runtime.client;

final class EchoClientScreenRuntimeController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientSettingsController settingsController;
    private final EchoClientRenderRuntimeController renderRuntime;
    private EchoClientAudio audio;

    EchoClientScreenRuntimeController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientSettingsController settingsController
    ) {
        this(runtimeServices, screens, settingsController, null);
    }

    EchoClientScreenRuntimeController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientSettingsController settingsController,
            EchoClientRenderRuntimeController renderRuntime
    ) {
        if (runtimeServices == null) {
            throw new IllegalArgumentException("runtimeServices must not be null");
        }
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        if (settingsController == null) {
            throw new IllegalArgumentException("settingsController must not be null");
        }
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.settingsController = settingsController;
        this.renderRuntime = renderRuntime;
    }

    void attachAudio(EchoClientAudio audio) {
        this.audio = audio;
    }

    void showInitialMainMenu() {
        refreshRuntimeSurfaces();
        screens.showMainMenu(runtimeServices.hasContinuableSession());
    }

    void refreshRuntimeSurfaces() {
        screens.updateSaveSlots(runtimeServices.saveSlotSummaries(), runtimeServices.saveSlotError());
        screens.updateModScan(runtimeServices.modScanSummary());
        screens.updateRuntimeContentSummary(runtimeServices.runtimeContentSummary());
        screens.updateTechSurfaceModel(runtimeServices.techSurfaceModel());
        screens.updateResourcePacks(runtimeServices.resourcePackSummaries(), runtimeServices.resourcePackError());
        screens.updateScreenCatalog(runtimeServices.screenCatalog());
        screens.updateRuntimeDiagnostics(EchoClientRuntimeDiagnosticsSnapshot.from(
                runtimeServices.worldSession(),
                renderRuntime == null ? null : renderRuntime.renderer()
        ));
        screens.updateWorkbenchRecipes(
                runtimeServices.workbenchRecipeSummaries(),
                runtimeServices.workbenchRecipeError()
        );
    }

    EchoClientScreenCommand updateScreen(EchoClientInput input, EchoClientUiViewport viewport) {
        if (input == null || viewport == null) {
            return EchoClientScreenCommand.NONE;
        }
        input.setCursorLocked(false);
        refreshRuntimeSurfaces();
        EchoClientScreenCommand command = screens.handleInput(
                input,
                runtimeServices.hasContinuableSession(),
                viewport.logicalWidth(),
                viewport.logicalHeight(),
                viewport.scale()
        );
        playPendingUiFeedback();
        settingsController.applyAndPersist();
        input.clearGameplayTriggers();
        return command;
    }

    boolean playPendingUiFeedback() {
        if (!screens.consumeUiFeedbackPulse()) {
            return false;
        }
        if (audio != null) {
            audio.playUiClick();
        }
        return true;
    }
}
