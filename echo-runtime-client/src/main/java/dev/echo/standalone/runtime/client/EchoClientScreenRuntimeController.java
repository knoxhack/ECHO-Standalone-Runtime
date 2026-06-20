package dev.echo.standalone.runtime.client;

final class EchoClientScreenRuntimeController {
    static final int PASSIVE_SURFACE_REFRESH_INTERVAL_TICKS = 120;

    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientSettingsController settingsController;
    private final EchoClientRenderRuntimeController renderRuntime;
    private EchoClientAudio audio;
    private EchoClientGameState lastSurfaceRefreshState;
    private EchoClientScreenKind lastSurfaceRefreshKind;
    private int ticksUntilPassiveSurfaceRefresh;
    private int surfaceRefreshCount;
    private int lightweightTitleRefreshCount;

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
        rememberSurfaceRefreshRoute();
    }

    void refreshRuntimeSurfaces() {
        screens.updateSaveSlots(runtimeServices.saveSlotSummaries(), runtimeServices.saveSlotError());
        screens.updateModScan(runtimeServices.modScanSummary());
        screens.updateRuntimeContentSummary(runtimeServices.runtimeContentSummary());
        screens.updateCreativeInventoryModel(runtimeServices.creativeInventoryModel());
        screens.updateTechSurfaceModel(runtimeServices.techSurfaceModel());
        screens.updateResourcePacks(runtimeServices.resourcePackSummaries(), runtimeServices.resourcePackError());
        screens.updateScreenCatalog(runtimeServices.screenCatalog());
        screens.updateRuntimeDiagnostics(EchoClientRuntimeDiagnosticsSnapshot.from(
                runtimeServices.worldSession(),
                renderRuntime == null ? null : renderRuntime.renderer(),
                renderRuntime == null ? EchoClientFramePacingSnapshot.EMPTY : renderRuntime.framePacingSnapshot(),
                audio == null ? EchoClientAudioDiagnosticsSnapshot.EMPTY : audio.diagnosticsSnapshot()
        ));
        screens.updateSupportBundleResult(runtimeServices.lastSupportBundleResult());
        screens.updateWorkbenchRecipes(
                runtimeServices.workbenchRecipeSummaries(),
                runtimeServices.workbenchRecipeError()
        );
        surfaceRefreshCount++;
        ticksUntilPassiveSurfaceRefresh = PASSIVE_SURFACE_REFRESH_INTERVAL_TICKS;
        rememberSurfaceRefreshRoute();
    }

    boolean refreshRuntimeSurfacesIfNeeded() {
        if (lastSurfaceRefreshState == null
                || lastSurfaceRefreshKind == null
                || lastSurfaceRefreshState != screens.state()
                || lastSurfaceRefreshKind != screens.screenKind()) {
            refreshCurrentRouteSurfaces();
            return true;
        }
        if (ticksUntilPassiveSurfaceRefresh > 0) {
            ticksUntilPassiveSurfaceRefresh--;
            return false;
        }
        refreshCurrentRouteSurfaces();
        return true;
    }

    int surfaceRefreshCount() {
        return surfaceRefreshCount;
    }

    int lightweightTitleRefreshCount() {
        return lightweightTitleRefreshCount;
    }

    EchoClientScreenCommand updateScreen(EchoClientInput input, EchoClientUiViewport viewport) {
        if (input == null || viewport == null) {
            return EchoClientScreenCommand.NONE;
        }
        input.setCursorLocked(false);
        if (screens.state() != EchoClientGameState.FATAL_ERROR) {
            refreshRuntimeSurfacesIfNeeded();
        }
        EchoClientScreenCommand command = screens.handleInput(
                input,
                runtimeServices.hasContinuableSession(),
                viewport.logicalWidth(),
                viewport.logicalHeight(),
                viewport.scale()
        );
        playPendingUiFeedback();
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

    private void rememberSurfaceRefreshRoute() {
        lastSurfaceRefreshState = screens.state();
        lastSurfaceRefreshKind = screens.screenKind();
    }

    private void refreshCurrentRouteSurfaces() {
        if (isTitleMenuRoute()) {
            refreshLightweightTitleSurfaces();
        } else {
            refreshRuntimeSurfaces();
        }
    }

    private boolean isTitleMenuRoute() {
        return screens.state() == EchoClientGameState.MAIN_MENU
                && screens.screenKind() == EchoClientScreenKind.MAIN_MENU;
    }

    private void refreshLightweightTitleSurfaces() {
        lightweightTitleRefreshCount++;
        ticksUntilPassiveSurfaceRefresh = PASSIVE_SURFACE_REFRESH_INTERVAL_TICKS;
        rememberSurfaceRefreshRoute();
    }
}
