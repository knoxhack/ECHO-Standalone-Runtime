package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.ui.EchoUiFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

final class EchoClientScreenController {
    static final int MENU_BUTTON_WIDTH = 300;
    static final int MENU_BUTTON_HEIGHT = 34;
    static final int MENU_BUTTON_SPACING = 9;
    static final int MENU_SLIDER_TRACK_X_OFFSET = 136;
    static final int MENU_SLIDER_TRACK_WIDTH = 112;
    private static final int MENU_VERTICAL_SAFE_ZONE = 238;
    private static final double TOAST_SECONDS = 3.0D;
    private static final int WORLD_NAME_LIMIT = 48;
    private static final int WORLD_SEED_LIMIT = 24;
    private static final String[] LANGUAGE_CODES = {"en_us", "en_gb", "de_de", "es_es", "fr_fr", "ja_jp"};

    private final EchoClientWorldPresentation presentation;
    private final String defaultWorldName;
    private final EchoClientLoadingController loading;
    private final EchoClientUiBridge uiBridge = new EchoClientUiBridge();
    private final EchoClientUiInputMapper inputMapper = new EchoClientUiInputMapper();
    private final ArrayList<EchoClientScreenKind> screenBackStack = new ArrayList<>();
    private boolean returnToGameplayOnBack;
    private List<EchoClientSaveSlotSummary> saveSlots = List.of();
    private EchoClientModScanSummary modScan = EchoClientModScanSummary.empty();
    private EchoClientRuntimeContentSummary runtimeContent = EchoClientRuntimeContentSummary.empty();
    private EchoClientTechSurfaceModel techSurface = EchoClientTechSurfaceModel.empty();
    private List<EchoClientResourcePackSummary> resourcePacks = List.of();
    private List<EchoClientWorkbenchRecipeSummary> workbenchRecipes = List.of();
    private EchoClientScreenCatalog screenCatalog = EchoClientScreenCatalog.empty();
    private EchoClientRuntimeDiagnosticsSnapshot runtimeDiagnostics =
            EchoClientRuntimeDiagnosticsSnapshot.EMPTY;
    private EchoClientSupportBundleResult supportBundleResult = EchoClientSupportBundleResult.EMPTY;
    private List<EchoClientScreenOption> publishedOptions = List.of();
    private String registeredScreenId = "";
    private String selectedWorldSlotId = "";
    private String selectedManagedWorldSlotId = "";
    private String saveSlotRenameText = "";
    private String selectedResourcePackId = "";
    private String saveSlotError = "";
    private String resourcePackError = "";
    private String workbenchRecipeError = "";
    private String fatalErrorSummary = "The standalone runtime stopped safely.";
    private String fatalErrorDetail = "No fatal error detail has been recorded.";
    private EchoClientGameState state = EchoClientGameState.BOOT;
    private EchoClientScreenKind screenKind = EchoClientScreenKind.MAIN_MENU;
    private int selectedIndex;
    private int scrollOffset;
    private long snapshotRevision;
    private long cachedSnapshotRevision = -1L;
    private boolean cachedSnapshotHasSession;
    private EchoClientScreenSnapshot cachedSnapshot;
    private long snapshotBuildCount;
    private long snapshotCacheHitCount;
    private long optionListBuildCount;
    private String worldName;
    private String worldSeed = "42";
    private EditableTextField editingTextField = EditableTextField.NONE;
    private int mouseSensitivity = 50;
    private boolean rawMouseInput = true;
    private int fovDegrees = EchoClientSettings.DEFAULT_FOV_DEGREES;
    private int uiScale = 50;
    private boolean fullscreen;
    private boolean vSync = true;
    private int chunkViewDistance = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
    private int masterVolume = 80;
    private int musicVolume = 55;
    private int ambienceVolume = 70;
    private String languageCode = EchoClientSettings.DEFAULT_LANGUAGE_CODE;
    private boolean subtitles = true;
    private boolean highContrastUi;
    private boolean reducedMotion;
    private EchoClientKeyBindings keyBindings = EchoClientKeyBindings.defaults();
    private EchoClientKeyAction pendingKeyRebindAction;
    private boolean clientSettingsDirty;
    private EchoClientScreenCommand pendingModalCommand = EchoClientScreenCommand.NONE;
    private String modalTitle = "";
    private String modalMessage = "";
    private String modalConfirmLabel = "Confirm";
    private String modalCancelLabel = "Cancel";
    private boolean modalConfirmSelected = true;
    private String toastMessage = "";
    private double toastRemainingSeconds;
    private String footer = "Use arrow keys and Enter";
    private int uiFeedbackPulses;
    private double quitToTitleTimer;
    private boolean quitToTitleCanContinue;

    EchoClientScreenController() {
        this(EchoClientSettings.defaults(), EchoClientWorldTemplates.defaultTemplate());
    }

    EchoClientScreenController(EchoClientSettings settings) {
        this(settings, EchoClientWorldTemplates.defaultTemplate());
    }

    EchoClientScreenController(EchoClientSettings settings, EchoClientWorldTemplate template) {
        this(
                settings,
                template == null ? EchoClientWorldTemplates.defaultTemplate().presentation() : template.presentation(),
                template == null ? EchoClientWorldTemplates.defaultTemplate().displayName() : template.displayName()
        );
    }

    EchoClientScreenController(EchoClientSettings settings, EchoClientWorldPresentation presentation) {
        this(settings, presentation, "New World");
    }

    EchoClientScreenController(
            EchoClientSettings settings,
            EchoClientWorldPresentation presentation,
            String defaultWorldName
    ) {
        this.presentation = presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        this.defaultWorldName = normalizedWorldName(defaultWorldName);
        this.worldName = this.defaultWorldName;
        this.loading = new EchoClientLoadingController(this.presentation);
        applyClientSettings(settings);
    }

    EchoClientGameState state() {
        return state;
    }

    EchoClientScreenKind screenKind() {
        return screenKind;
    }

    boolean inventoryOpen() {
        return state == EchoClientGameState.SCREEN_OPEN && screenKind == EchoClientScreenKind.INVENTORY;
    }

    boolean containerOpen() {
        return state == EchoClientGameState.SCREEN_OPEN && screenKind == EchoClientScreenKind.CONTAINER;
    }

    boolean slotGridOpen() {
        return inventoryOpen() || containerOpen();
    }

    boolean modalOpen() {
        return pendingModalCommand != EchoClientScreenCommand.NONE;
    }

    boolean textEditing() {
        return editingTextField != EditableTextField.NONE;
    }

    boolean keyRebindActive() {
        return pendingKeyRebindAction != null;
    }

    String worldName() {
        return normalizedWorldName(worldName);
    }

    String worldSeed() {
        return worldSeed.isBlank() ? "42" : worldSeed.trim();
    }

    private String worldNameTextValue() {
        return editingTextField == EditableTextField.WORLD_NAME ? worldName : worldName();
    }

    private String worldSeedTextValue() {
        return editingTextField == EditableTextField.WORLD_SEED ? worldSeed : worldSeed();
    }

    private String saveSlotRenameTextValue() {
        return editingTextField == EditableTextField.SAVE_SLOT_RENAME
                ? saveSlotRenameText
                : normalizedSaveSlotRenameText();
    }

    EchoClientSettings clientSettings() {
        return new EchoClientSettings(
                mouseSensitivity,
                rawMouseInput,
                fovDegrees,
                uiScale,
                fullscreen,
                vSync,
                chunkViewDistance,
                masterVolume,
                musicVolume,
                ambienceVolume,
                languageCode,
                subtitles,
                highContrastUi,
                reducedMotion,
                keyBindings
        );
    }

    void applyClientSettings(EchoClientSettings settings) {
        EchoClientSettings next = settings == null ? EchoClientSettings.defaults() : settings;
        mouseSensitivity = next.mouseSensitivityPercent();
        rawMouseInput = next.rawMouseInput();
        fovDegrees = next.fovDegrees();
        uiScale = next.uiScalePercent();
        fullscreen = next.fullscreen();
        vSync = next.vSync();
        chunkViewDistance = next.chunkViewDistance();
        masterVolume = next.masterVolumePercent();
        musicVolume = next.musicVolumePercent();
        ambienceVolume = next.ambienceVolumePercent();
        languageCode = next.languageCode();
        subtitles = next.subtitles();
        highContrastUi = next.highContrastUi();
        reducedMotion = next.reducedMotion();
        keyBindings = next.keyBindings();
        clientSettingsDirty = false;
        markSnapshotDirty();
    }

    boolean consumeClientSettingsDirty() {
        boolean dirty = clientSettingsDirty;
        clientSettingsDirty = false;
        return dirty;
    }

    boolean toggleFullscreenPreference() {
        fullscreen = !fullscreen;
        clientSettingsDirty = true;
        markSnapshotDirty();
        return fullscreen;
    }

    void tick(double dt) {
        if (quitToTitleTimer > 0.0D) {
            quitToTitleTimer = Math.max(0.0D, quitToTitleTimer - dt);
            if (quitToTitleTimer == 0.0D) {
                showMainMenu(quitToTitleCanContinue);
            }
            return;
        }
        if (toastRemainingSeconds > 0.0D) {
            toastRemainingSeconds = Math.max(0.0D, toastRemainingSeconds - dt);
            if (toastRemainingSeconds == 0.0D) {
                toastMessage = "";
                markSnapshotDirty();
            }
        }
    }

    void showToast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        toastMessage = message.trim();
        toastRemainingSeconds = TOAST_SECONDS;
        markSnapshotDirty();
    }

    boolean consumeUiFeedbackPulse() {
        boolean hadPulse = uiFeedbackPulses > 0;
        uiFeedbackPulses = 0;
        return hadPulse;
    }

    boolean blocksGameplay() {
        return state != EchoClientGameState.IN_GAME;
    }

    boolean hasWorldBehind() {
        return state == EchoClientGameState.PAUSED || state == EchoClientGameState.SCREEN_OPEN
                || state == EchoClientGameState.DEAD
                || state == EchoClientGameState.SAVING
                || state == EchoClientGameState.QUITTING_TO_TITLE;
    }

    void showMainMenu(boolean canContinue) {
        pendingKeyRebindAction = null;
        state = EchoClientGameState.MAIN_MENU;
        screenKind = EchoClientScreenKind.MAIN_MENU;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(canContinue), canContinue ? 0 : 1);
        footer = "Enter selects, Esc quits";
        publishMenu(canContinue);
    }

    void startLoadingNewGame() {
        loading.startNewWorld(worldSeed, worldName());
        state = loading.state();
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        footer = presentation.newWorldLoadingFooter();
        publishLoading();
    }

    void startLoadingSavedWorld(String slotLabel) {
        loading.startSavedWorld(slotLabel);
        state = loading.state();
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        footer = presentation.savedWorldLoadingFooter();
        publishLoading();
    }

    boolean updateLoading(double dt) {
        if (!loading.active()) {
            return false;
        }
        boolean complete = loading.update(dt);
        state = complete ? EchoClientGameState.IN_GAME : loading.state();
        if (complete) {
            returnToGameplayOnBack = false;
        }
        if (!complete) {
            publishLoading();
        }
        return complete;
    }

    void showInGame() {
        pendingKeyRebindAction = null;
        state = EchoClientGameState.IN_GAME;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        footer = "";
        uiBridge.showStatic("echoscreencore:hud", "ECHO HUD", List.of("Gameplay active"), "hud.crosshair");
        markSnapshotDirty();
    }

    void showInventory() {
        pendingKeyRebindAction = null;
        state = EchoClientGameState.SCREEN_OPEN;
        screenKind = EchoClientScreenKind.INVENTORY;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        publishInventory();
    }

    private void openInventory(boolean hasSession) {
        if (!hasSession) {
            return;
        }
        if (state == EchoClientGameState.IN_GAME) {
            returnToGameplayOnBack = true;
        } else if (screenKind != EchoClientScreenKind.INVENTORY) {
            screenBackStack.add(screenKind);
        }
        state = EchoClientGameState.SCREEN_OPEN;
        screenKind = EchoClientScreenKind.INVENTORY;
        registeredScreenId = "";
        scrollOffset = 0;
        selectedIndex = -1;
        publishInventory();
    }

    private void publishInventory() {
        footer = "E closes, Esc returns";
        uiBridge.showStatic("echoscreencore:inventory", "INVENTORY", List.of(
                "Player carry slots",
                "Route: screencore.inventory",
                "Focus: inventory.slots"
        ), "inventory.slots");
        markSnapshotDirty();
    }

    private void openContainer(boolean hasSession) {
        if (!hasSession) {
            return;
        }
        if (state == EchoClientGameState.IN_GAME) {
            returnToGameplayOnBack = true;
        } else if (screenKind != EchoClientScreenKind.CONTAINER) {
            screenBackStack.add(screenKind);
        }
        state = EchoClientGameState.SCREEN_OPEN;
        screenKind = EchoClientScreenKind.CONTAINER;
        registeredScreenId = "";
        scrollOffset = 0;
        selectedIndex = -1;
        publishContainer();
    }

    private void publishContainer() {
        footer = "E closes, Esc returns";
        uiBridge.showStatic("echoscreencore:container", "CONTAINER", List.of(
                "Crash cache slots",
                "Route: screencore.container",
                "Focus: container.slots"
        ), "container.slots");
        markSnapshotDirty();
    }

    void showPauseMenu() {
        pendingKeyRebindAction = null;
        state = EchoClientGameState.PAUSED;
        screenKind = EchoClientScreenKind.PAUSE_MENU;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(true), 0);
        footer = "Esc resumes";
        publishMenu(true);
    }

    void beginQuitToTitle(boolean canContinue) {
        quitToTitleTimer = 0.15D;
        quitToTitleCanContinue = canContinue;
        state = EchoClientGameState.QUITTING_TO_TITLE;
        screenKind = EchoClientScreenKind.MAIN_MENU;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        footer = "Returning to title";
        uiBridge.showStatic("echoscreencore:quitting_to_title", "QUITTING TO TITLE", List.of(
                "Unloading world session",
                "Route: screencore.quit_to_title",
                "Focus: quit_to_title.status"
        ), "quit_to_title.status");
        markSnapshotDirty();
    }

    void showDeathScreen() {
        state = EchoClientGameState.DEAD;
        screenKind = EchoClientScreenKind.DEATH_SCREEN;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(true), 0);
        footer = "Enter respawns";
        publishMenu(true);
    }

    void showSaving() {
        state = EchoClientGameState.SAVING;
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = -1;
        footer = "Writing world session";
        uiBridge.showStatic("echoscreencore:saving", "SAVING", List.of(
                "Session snapshot is staged",
                "Route: screencore.saving",
                "Focus: saving.status"
        ), "saving.status");
        markSnapshotDirty();
    }

    void showFatalError(Throwable failure) {
        fatalErrorSummary = fatalErrorSummary(failure);
        fatalErrorDetail = fatalErrorDetail(failure);
        state = EchoClientGameState.FATAL_ERROR;
        screenKind = EchoClientScreenKind.FATAL_ERROR;
        screenBackStack.clear();
        returnToGameplayOnBack = false;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(true), 0);
        footer = "Export a support bundle, return to title, or quit";
        publishMenu(true);
    }

    EchoClientScreenCommand handleInput(
            EchoClientInput input,
            boolean hasSession,
            int width,
            int height,
            double uiScale
    ) {
        if (state == EchoClientGameState.IN_GAME || isLoading() || state == EchoClientGameState.QUITTING_TO_TITLE) {
            return EchoClientScreenCommand.NONE;
        }
        return inputMapper.poll(input, this, hasSession, width, height, uiScale);
    }

    EchoClientScreenCommand escapeCommand() {
        if (modalOpen()) {
            closeModal();
            markUiFeedback();
            return EchoClientScreenCommand.NONE;
        }
        if (state == EchoClientGameState.SCREEN_OPEN) {
            markUiFeedback();
            return EchoClientScreenCommand.BACK;
        }
        if (state == EchoClientGameState.PAUSED) {
            markUiFeedback();
            return EchoClientScreenCommand.RESUME_GAME;
        }
        if (state == EchoClientGameState.DEAD) {
            return EchoClientScreenCommand.NONE;
        }
        if (state == EchoClientGameState.FATAL_ERROR) {
            return EchoClientScreenCommand.NONE;
        }
        if (state == EchoClientGameState.MAIN_MENU) {
            markUiFeedback();
            return EchoClientScreenCommand.QUIT_CLIENT;
        }
        return EchoClientScreenCommand.NONE;
    }

    void updateSaveSlots(List<EchoClientSaveSlotSummary> saveSlots, String saveSlotError) {
        boolean hadLoadableSaveSlot = this.saveSlots.stream().anyMatch(EchoClientSaveSlotSummary::loadableInMemory);
        this.saveSlots = saveSlots == null ? List.of() : List.copyOf(saveSlots);
        this.saveSlotError = saveSlotError == null ? "" : saveSlotError;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.WORLD_SELECT) {
            boolean hasLoadableSaveSlot = this.saveSlots.stream().anyMatch(EchoClientSaveSlotSummary::loadableInMemory);
            selectedIndex = !hadLoadableSaveSlot && hasLoadableSaveSlot
                    ? firstLoadableSaveSlotIndex()
                    : firstEnabledIndex(options(false), selectedIndex);
            rememberSelectedSaveSlot();
            publishMenu(false);
        }
    }

    void updateModScan(EchoClientModScanSummary modScan) {
        this.modScan = modScan == null ? EchoClientModScanSummary.empty() : modScan;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.MODS || screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        }
    }

    void updateRuntimeContentSummary(EchoClientRuntimeContentSummary runtimeContent) {
        this.runtimeContent = runtimeContent == null ? EchoClientRuntimeContentSummary.empty() : runtimeContent;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.MODS || screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        }
    }

    void updateTechSurfaceModel(EchoClientTechSurfaceModel techSurface) {
        this.techSurface = techSurface == null ? EchoClientTechSurfaceModel.empty() : techSurface;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.MACHINE || screenKind == EchoClientScreenKind.TERMINAL) {
            selectedIndex = firstEnabledIndex(options(true), selectedIndex);
            publishMenu(true);
        } else if (screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        }
    }

    void updateResourcePacks(List<EchoClientResourcePackSummary> resourcePacks, String resourcePackError) {
        boolean hadResourcePacks = !this.resourcePacks.isEmpty();
        this.resourcePacks = resourcePacks == null ? List.of() : List.copyOf(resourcePacks);
        this.resourcePackError = resourcePackError == null ? "" : resourcePackError;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.RESOURCE_PACKS) {
            selectedIndex = !hadResourcePacks && !this.resourcePacks.isEmpty()
                    ? firstResourcePackIndex()
                    : firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        } else if (screenKind == EchoClientScreenKind.RESOURCE_PACK_DETAIL
                && selectedResourcePack() == null) {
            back(false);
        }
    }

    void updateScreenCatalog(EchoClientScreenCatalog screenCatalog) {
        this.screenCatalog = screenCatalog == null ? EchoClientScreenCatalog.empty() : screenCatalog;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.MODS || screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        } else if (screenKind == EchoClientScreenKind.REGISTERED_SCREEN
                && screenCatalog.findScreen(registeredScreenId).isEmpty()) {
            registeredScreenId = "";
            back(false);
        }
    }

    void updateRuntimeDiagnostics(EchoClientRuntimeDiagnosticsSnapshot runtimeDiagnostics) {
        this.runtimeDiagnostics = runtimeDiagnostics == null
                ? EchoClientRuntimeDiagnosticsSnapshot.EMPTY
                : runtimeDiagnostics;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        }
    }

    EchoClientRuntimeDiagnosticsSnapshot runtimeDiagnosticsSnapshot() {
        return runtimeDiagnostics;
    }

    void updateSupportBundleResult(EchoClientSupportBundleResult supportBundleResult) {
        this.supportBundleResult = supportBundleResult == null
                ? EchoClientSupportBundleResult.EMPTY
                : supportBundleResult;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.DIAGNOSTICS) {
            selectedIndex = firstEnabledIndex(options(false), selectedIndex);
            publishMenu(false);
        }
    }

    void updateWorkbenchRecipes(List<EchoClientWorkbenchRecipeSummary> recipes, String recipeError) {
        this.workbenchRecipes = recipes == null ? List.of() : List.copyOf(recipes);
        this.workbenchRecipeError = recipeError == null ? "" : recipeError;
        markSnapshotDirty();
        if (screenKind == EchoClientScreenKind.WORKBENCH) {
            selectedIndex = firstEnabledIndex(options(true), selectedIndex);
            publishMenu(true);
        }
    }

    String selectedSaveSlotId() {
        if (screenKind != EchoClientScreenKind.WORLD_SELECT) {
            return "";
        }
        EchoClientSaveSlotSummary managedSlot = saveSlot(selectedManagedWorldSlotId);
        if (managedSlot != null && managedSlot.loadableInMemory()) {
            return managedSlot.slotId();
        }
        if (!selectedWorldSlotId.isBlank() && saveSlotLoadable(selectedWorldSlotId)) {
            return selectedWorldSlotId;
        }
        return firstLoadableSaveSlotId();
    }

    String selectedManageSaveSlotId() {
        EchoClientSaveSlotSummary selectedSlot = selectedSaveSlotSummary();
        return selectedSlot == null ? "" : selectedSlot.slotId();
    }

    String selectedSaveSlotLabel() {
        if (screenKind == EchoClientScreenKind.WORLD_SELECT) {
            EchoClientSaveSlotSummary managedSlot = saveSlot(selectedManagedWorldSlotId);
            if (managedSlot != null && managedSlot.loadableInMemory()) {
                return managedSlot.displayName();
            }
            EchoClientSaveSlotSummary loadableSlot = saveSlot(selectedWorldSlotId);
            if (loadableSlot != null && loadableSlot.loadableInMemory()) {
                return loadableSlot.displayName();
            }
        }
        return "";
    }

    String selectedManageSaveSlotLabel() {
        EchoClientSaveSlotSummary selectedSlot = selectedSaveSlotSummary();
        return selectedSlot == null ? "" : selectedSlot.displayName();
    }

    String saveSlotRenameText() {
        return normalizedSaveSlotRenameText();
    }

    String selectedWorkbenchRecipeId() {
        if (screenKind != EchoClientScreenKind.WORKBENCH) {
            return "";
        }
        return selectedTargetId(true);
    }

    String selectedMachineInputTargetId() {
        if (screenKind != EchoClientScreenKind.MACHINE) {
            return "";
        }
        return selectedTargetId(true);
    }

    String selectedMachineOutputTargetId() {
        if (screenKind != EchoClientScreenKind.MACHINE) {
            return "";
        }
        return selectedTargetId(true);
    }

    String selectedMachineRecipeTargetId() {
        if (screenKind != EchoClientScreenKind.MACHINE) {
            return "";
        }
        return selectedTargetId(true);
    }

    boolean executeNavigationCommand(EchoClientScreenCommand command, boolean hasSession) {
        switch (command) {
            case OPEN_WORLD_SELECT -> openScreen(EchoClientScreenKind.WORLD_SELECT, hasSession);
            case OPEN_CREATE_WORLD -> openScreen(EchoClientScreenKind.CREATE_WORLD, hasSession);
            case OPEN_OPTIONS -> openScreen(EchoClientScreenKind.OPTIONS, hasSession);
            case OPEN_CONTROLS -> openScreen(EchoClientScreenKind.CONTROLS, hasSession);
            case OPEN_VIDEO_SETTINGS -> openScreen(EchoClientScreenKind.VIDEO_SETTINGS, hasSession);
            case OPEN_AUDIO_SETTINGS -> openScreen(EchoClientScreenKind.AUDIO_SETTINGS, hasSession);
            case OPEN_ACCESSIBILITY_SETTINGS -> openScreen(EchoClientScreenKind.ACCESSIBILITY_SETTINGS, hasSession);
            case OPEN_LANGUAGE_SETTINGS -> openScreen(EchoClientScreenKind.LANGUAGE_SETTINGS, hasSession);
            case OPEN_MODS -> openScreen(EchoClientScreenKind.MODS, hasSession);
            case OPEN_RESOURCE_PACKS -> openScreen(EchoClientScreenKind.RESOURCE_PACKS, hasSession);
            case OPEN_INVENTORY -> {
                if (!hasSession) {
                    return false;
                }
                openInventory(hasSession);
            }
            case OPEN_CONTAINER -> {
                if (!hasSession) {
                    return false;
                }
                openContainer(hasSession);
            }
            case OPEN_WORKBENCH -> {
                if (!hasSession) {
                    return false;
                }
                openScreen(EchoClientScreenKind.WORKBENCH, hasSession);
            }
            case OPEN_MACHINE -> {
                if (!hasSession) {
                    return false;
                }
                openScreen(EchoClientScreenKind.MACHINE, hasSession);
            }
            case OPEN_TERMINAL -> {
                if (!hasSession) {
                    return false;
                }
                openScreen(EchoClientScreenKind.TERMINAL, hasSession);
            }
            case OPEN_DIAGNOSTICS -> openScreen(EchoClientScreenKind.DIAGNOSTICS, hasSession);
            case OPEN_REGISTERED_SCREEN -> {
                return openSelectedRegisteredScreen(hasSession);
            }
            case OPEN_RESOURCE_PACK_DETAIL -> {
                return openSelectedResourcePackDetail(hasSession);
            }
            case BACK -> back(hasSession);
            default -> {
                return false;
            }
        }
        return true;
    }

    void closeSlotGridScreen(boolean hasSession) {
        registeredScreenId = "";
        if (!screenBackStack.isEmpty()) {
            back(hasSession);
            return;
        }
        if (hasSession) {
            showInGame();
        } else {
            showMainMenu(false);
        }
    }

    void moveSelection(int direction, boolean hasSession, int height) {
        List<EchoClientScreenOption> options = options(hasSession);
        int before = selectedIndex;
        moveSelection(options, direction);
        rememberSelectedSaveSlot();
        ensureSelectedVisible(options.size(), height);
        if (before != selectedIndex) {
            markUiFeedback();
        }
        publishMenu(hasSession, options);
    }

    void scrollSelection(int direction, boolean hasSession, int height) {
        List<EchoClientScreenOption> options = options(hasSession);
        if (options.isEmpty()) {
            return;
        }
        scrollOffset = clampScroll(scrollOffset + direction, options.size(), height);
        selectedIndex = Math.max(scrollOffset, Math.min(selectedIndex, scrollOffset + menuVisibleCount(height, options.size()) - 1));
        if (selectedIndex < 0 || selectedIndex >= options.size() || !options.get(selectedIndex).enabled()) {
            selectedIndex = firstEnabledIndex(options, scrollOffset);
        }
        rememberSelectedSaveSlot();
        markUiFeedback();
        publishMenu(hasSession, options);
    }

    EchoClientScreenCommand activateSelection(boolean hasSession) {
        List<EchoClientScreenOption> options = options(hasSession);
        if (options.isEmpty()) {
            return EchoClientScreenCommand.NONE;
        }
        EchoClientScreenOption selected = options.get(selectedIndex);
        if (!selected.enabled()) {
            return EchoClientScreenCommand.NONE;
        }
        return activateSelectedOption(selected, hasSession);
    }

    private EchoClientScreenCommand activateSelectedOption(EchoClientScreenOption selected, boolean hasSession) {
        markUiFeedback();
        if (activateControl(selected)) {
            publishMenu(hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (selected.command() == EchoClientScreenCommand.CYCLE_LANGUAGE) {
            cycleLanguage(1);
            publishMenu(hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (selected.command() == EchoClientScreenCommand.RESET_KEY_BINDINGS) {
            pendingKeyRebindAction = null;
            keyBindings = EchoClientKeyBindings.defaults();
            clientSettingsDirty = true;
            showToast("Key bindings reset");
            publishMenu(hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (selected.command() == EchoClientScreenCommand.START_KEY_REBIND) {
            beginKeyRebind(selected.targetId(), hasSession);
            return EchoClientScreenCommand.NONE;
        }
        if (requiresConfirmation(selected.command())) {
            openModal(selected.command());
            return EchoClientScreenCommand.NONE;
        }
        if (uiBridge.dispatchCommand(selected.command()).handled()) {
            return selected.command();
        }
        return EchoClientScreenCommand.NONE;
    }

    void editSelectedControl(int direction, boolean hasSession) {
        if (direction == 0) {
            return;
        }
        List<EchoClientScreenOption> options = options(hasSession);
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            return;
        }
        EchoClientScreenOption selected = options.get(selectedIndex);
        boolean changed = false;
        switch (selected.kind()) {
            case TOGGLE -> {
                toggleSelectedControl(selected);
                showToast(selected.label() + " " + currentValueText(selected.label()));
                changed = true;
            }
            case SLIDER -> {
                adjustSelectedSlider(selected.label(), direction);
                showToast(selected.label() + " " + currentValueText(selected.label()));
                changed = true;
            }
            case TEXT -> {
                beginTextEditing(selected.label());
                changed = true;
            }
            case BUTTON -> {
                if (selected.command() == EchoClientScreenCommand.CYCLE_LANGUAGE) {
                    cycleLanguage(direction);
                    showToast("Language " + languageDisplayName(languageCode));
                    changed = true;
                }
            }
        }
        if (changed) {
            markUiFeedback();
        }
        publishMenu(hasSession);
    }

    void handleTextInput(String characters, boolean backspace, boolean hasSession) {
        if (!textEditing()) {
            return;
        }
        String value = activeTextValue();
        if (backspace && !value.isEmpty()) {
            value = value.substring(0, Math.max(0, value.length() - 1));
        }
        if (characters != null && !characters.isEmpty()) {
            String normalized = characters.replaceAll("[^A-Za-z0-9 _-]", "");
            value = (value + normalized).stripLeading();
        }
        setActiveTextValue(value);
        publishMenu(hasSession);
    }

    void stopTextEditing(boolean hasSession) {
        EditableTextField activeField = editingTextField;
        if (activeField == EditableTextField.NONE) {
            return;
        }
        editingTextField = EditableTextField.NONE;
        if (activeField == EditableTextField.WORLD_NAME && worldName.isBlank()) {
            worldName = defaultWorldName;
        }
        if (activeField == EditableTextField.WORLD_SEED && worldSeed.isBlank()) {
            worldSeed = "42";
        }
        if (activeField == EditableTextField.SAVE_SLOT_RENAME && saveSlotRenameText.isBlank()) {
            saveSlotRenameText = selectedManageSaveSlotLabel();
        }
        showToast(activeField.toastPrefix() + " set to " + activeField.value(this));
        publishMenu(hasSession);
    }

    private void beginKeyRebind(String actionId, boolean hasSession) {
        EchoClientKeyAction action = EchoClientKeyAction.byId(actionId);
        if (action == null) {
            showToast("Key binding unavailable");
            return;
        }
        editingTextField = EditableTextField.NONE;
        pendingKeyRebindAction = action;
        publishMenu(hasSession);
    }

    void cancelKeyRebind(boolean hasSession) {
        if (pendingKeyRebindAction == null) {
            return;
        }
        String label = pendingKeyRebindAction.displayName();
        pendingKeyRebindAction = null;
        showToast(label + " binding canceled");
        publishMenu(hasSession);
    }

    boolean finishKeyRebind(int glfwKey, boolean hasSession) {
        if (pendingKeyRebindAction == null) {
            return false;
        }
        EchoClientKeyAction action = pendingKeyRebindAction;
        EchoClientKeyBindings next = keyBindings.withKey(action, glfwKey);
        if (next == keyBindings) {
            showToast("Unsupported key");
            publishMenu(hasSession);
            return false;
        }
        keyBindings = next;
        pendingKeyRebindAction = null;
        clientSettingsDirty = true;
        showToast(action.displayName() + " set to " + keyBindings.label(action));
        publishMenu(hasSession);
        return true;
    }

    EchoClientScreenCommand handleModalPointer(
            double pointerX,
            double pointerY,
            boolean clicked,
            int width,
            int height
    ) {
        if (!modalOpen()) {
            return EchoClientScreenCommand.NONE;
        }
        int confirmHit = modalButtonHit(pointerX, pointerY, width, height);
        if (confirmHit >= 0) {
            modalConfirmSelected = confirmHit == 1;
            if (clicked) {
                markUiFeedback();
                return finishModal(modalConfirmSelected);
            }
        } else if (clicked) {
            closeModal();
            markUiFeedback();
        }
        return EchoClientScreenCommand.NONE;
    }

    EchoClientScreenCommand handleModalNavigation(int direction) {
        if (!modalOpen()) {
            return EchoClientScreenCommand.NONE;
        }
        if (direction != 0) {
            modalConfirmSelected = direction > 0;
            markUiFeedback();
            markSnapshotDirty();
        }
        return EchoClientScreenCommand.NONE;
    }

    EchoClientScreenCommand confirmModalSelection() {
        markUiFeedback();
        return finishModal(modalConfirmSelected);
    }

    EchoClientScreenCommand handlePointer(
            double pointerX,
            double pointerY,
            boolean clicked,
            boolean primaryDown,
            int width,
            int height,
            boolean hasSession
    ) {
        List<EchoClientScreenOption> options = currentPublishedOptions(hasSession);
        int beforeScrollOffset = scrollOffset;
        int hoverIndex = hitOption(pointerX, pointerY, width, height, options.size());
        if (hoverIndex < 0 || hoverIndex >= options.size()) {
            return EchoClientScreenCommand.NONE;
        }
        int before = selectedIndex;
        selectedIndex = hoverIndex;
        rememberSelectedSaveSlot();
        boolean menuChanged = before != selectedIndex || beforeScrollOffset != scrollOffset;
        if (before != selectedIndex) {
            markUiFeedback();
        }
        EchoClientScreenOption hovered = options.get(hoverIndex);
        boolean optionContentChanged = false;
        if (hovered.enabled()
                && hovered.kind() == EchoClientScreenOptionKind.SLIDER
                && (clicked || primaryDown)
                && setSelectedSliderFromPointer(hovered.label(), pointerX, width)) {
            showToast(hovered.label() + " " + currentValueText(hovered.label()));
            markUiFeedback();
            menuChanged = true;
            optionContentChanged = true;
        }
        if (menuChanged) {
            if (optionContentChanged) {
                publishMenu(hasSession);
            } else {
                publishMenu(hasSession, options);
            }
        }
        if (clicked && hovered.enabled() && hovered.kind() != EchoClientScreenOptionKind.SLIDER) {
            return activateSelectedOption(hovered, hasSession);
        }
        return EchoClientScreenCommand.NONE;
    }

    EchoClientScreenSnapshot snapshot(boolean hasSession) {
        if (cachedSnapshot != null
                && cachedSnapshotHasSession == hasSession
                && cachedSnapshotRevision == snapshotRevision) {
            snapshotCacheHitCount++;
            return cachedSnapshot;
        }
        EchoClientScreenSnapshot snapshot = buildSnapshot(hasSession);
        snapshotBuildCount++;
        if (canCacheSnapshot(snapshot)) {
            cachedSnapshot = snapshot;
            cachedSnapshotHasSession = hasSession;
            cachedSnapshotRevision = snapshotRevision;
        } else {
            cachedSnapshot = null;
            cachedSnapshotRevision = -1L;
        }
        return snapshot;
    }

    long snapshotRevision() {
        return snapshotRevision;
    }

    long snapshotBuildCount() {
        return snapshotBuildCount;
    }

    long snapshotCacheHitCount() {
        return snapshotCacheHitCount;
    }

    long optionListBuildCount() {
        return optionListBuildCount;
    }

    private EchoClientScreenSnapshot buildSnapshot(boolean hasSession) {
        if (isLoading()) {
            EchoUiFrame frame = uiBridge.frame();
            return new EchoClientScreenSnapshot(
                    state,
                    screenKind,
                    frame.screen().title(),
                    String.join("  ", frame.screen().lines()),
                    List.of(),
                    -1,
                    0,
                    true,
                    loading.progress(),
                    loading.tip(),
                    modalSnapshot(),
                    toastSnapshot(),
                    footer + " | ScreenCore " + frame.screen().id(),
                    EchoClientSaveSlotThumbnailSnapshot.EMPTY
            );
        }
        String title = switch (state) {
            case MAIN_MENU -> "ECHO ASHFALL";
            case PAUSED -> "GAME PAUSED";
            case DEAD -> "YOU DIED";
            case SAVING -> "SAVING";
            case FATAL_ERROR -> "RUNTIME ERROR";
            case SCREEN_OPEN -> screenTitle();
            default -> "ECHO";
        };
        String subtitle = switch (state) {
            case MAIN_MENU -> "Standalone Client";
            case PAUSED -> "Runtime shell";
            case DEAD -> "Respawn available";
            case SAVING -> "Please wait";
            case FATAL_ERROR -> "Standalone client halted safely";
            case SCREEN_OPEN -> screenSubtitle();
            default -> "";
        };
        EchoUiFrame frame = uiBridge.frame();
        List<EchoClientScreenOption> screenOptions = options(hasSession);
        return new EchoClientScreenSnapshot(
                state,
                screenKind,
                frame.screen().title().isBlank() ? title : frame.screen().title(),
                subtitle,
                screenOptions,
                selectedIndex,
                scrollOffset,
                false,
                0.0D,
                selectedTooltip(screenOptions),
                modalSnapshot(),
                toastSnapshot(),
                footer + " | ScreenCore " + frame.screen().id(),
                saveSlotThumbnailSnapshot()
        );
    }

    private static boolean canCacheSnapshot(EchoClientScreenSnapshot snapshot) {
        return snapshot != null
                && !snapshot.loading()
                && !snapshot.modal().visible()
                && !snapshot.toast().visible();
    }

    private boolean isLoading() {
        return state == EchoClientGameState.MOD_SCAN
                || state == EchoClientGameState.LOADING_ASSETS
                || state == EchoClientGameState.LOADING_DATA
                || state == EchoClientGameState.LOADING_WORLD;
    }

    private List<EchoClientScreenOption> options(boolean hasSession) {
        optionListBuildCount++;
        ArrayList<EchoClientScreenOption> result = new ArrayList<>();
        switch (screenKind) {
            case MAIN_MENU -> {
                result.add(new EchoClientScreenOption("Continue", EchoClientScreenCommand.CONTINUE_GAME, hasSession));
                result.add(new EchoClientScreenOption("New Game", EchoClientScreenCommand.OPEN_CREATE_WORLD, true));
                result.add(new EchoClientScreenOption("Load Game", EchoClientScreenCommand.OPEN_WORLD_SELECT, true));
                result.add(new EchoClientScreenOption("Options", EchoClientScreenCommand.OPEN_OPTIONS, true));
                result.add(new EchoClientScreenOption("Quit", EchoClientScreenCommand.QUIT_CLIENT, true));
            }
            case PAUSE_MENU -> {
                result.add(new EchoClientScreenOption("Resume", EchoClientScreenCommand.RESUME_GAME, true));
                result.add(new EchoClientScreenOption("Inventory", EchoClientScreenCommand.OPEN_INVENTORY, hasSession));
                result.add(new EchoClientScreenOption("Container", EchoClientScreenCommand.OPEN_CONTAINER, hasSession));
                result.add(new EchoClientScreenOption("Workbench", EchoClientScreenCommand.OPEN_WORKBENCH, hasSession));
                result.add(new EchoClientScreenOption("Machine", EchoClientScreenCommand.OPEN_MACHINE, hasSession));
                result.add(new EchoClientScreenOption("Terminal", EchoClientScreenCommand.OPEN_TERMINAL, hasSession));
                result.add(new EchoClientScreenOption("Save Game", EchoClientScreenCommand.SAVE_GAME, true));
                result.add(new EchoClientScreenOption("Options", EchoClientScreenCommand.OPEN_OPTIONS, true));
                result.add(new EchoClientScreenOption("Diagnostics", EchoClientScreenCommand.OPEN_DIAGNOSTICS, true));
                result.add(new EchoClientScreenOption(
                        "Export Support Bundle",
                        EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE,
                        true,
                        "Write a local client diagnostics zip for support"
                ));
                result.add(new EchoClientScreenOption("Mods", EchoClientScreenCommand.OPEN_MODS, true));
                result.add(new EchoClientScreenOption("Resource Packs", EchoClientScreenCommand.OPEN_RESOURCE_PACKS, true));
                result.add(new EchoClientScreenOption("Quit To Title", EchoClientScreenCommand.QUIT_TO_TITLE, true));
            }
            case DEATH_SCREEN -> {
                result.add(new EchoClientScreenOption("Respawn", EchoClientScreenCommand.RESPAWN, hasSession));
                result.add(new EchoClientScreenOption("Options", EchoClientScreenCommand.OPEN_OPTIONS, true));
                result.add(new EchoClientScreenOption("Quit To Title", EchoClientScreenCommand.QUIT_TO_TITLE, true));
            }
            case FATAL_ERROR -> {
                result.add(new EchoClientScreenOption(
                        "Export Support Bundle",
                        EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE,
                        true,
                        "Write a local client diagnostics zip with the fatal error screen snapshot"
                ));
                result.add(new EchoClientScreenOption(
                        "Open Diagnostics",
                        EchoClientScreenCommand.OPEN_DIAGNOSTICS,
                        true,
                        "Inspect runtime telemetry after the fatal error"
                ));
                result.add(new EchoClientScreenOption(
                        "Error: " + limitText(fatalErrorSummary, 52),
                        EchoClientScreenCommand.NONE,
                        false,
                        fatalErrorDetail
                ));
                result.add(new EchoClientScreenOption(
                        "Quit To Title",
                        EchoClientScreenCommand.QUIT_TO_TITLE,
                        hasSession,
                        hasSession
                                ? "Unload the active world and return to the main menu"
                                : "No active world session is loaded"
                ));
                result.add(new EchoClientScreenOption("Quit Client", EchoClientScreenCommand.QUIT_CLIENT, true));
            }
            case WORLD_SELECT -> {
                if (saveSlots.isEmpty()) {
                    result.add(new EchoClientScreenOption("No Saved Worlds Found", EchoClientScreenCommand.NONE, false));
                } else {
                    for (EchoClientSaveSlotSummary slot : saveSlots) {
                        result.add(EchoClientScreenOption.target(
                                slot.menuLabel(),
                                slot.loadableInMemory() ? EchoClientScreenCommand.CONTINUE_GAME : EchoClientScreenCommand.NONE,
                                slot.slotId(),
                                saveSlotRowTooltip(slot),
                                true
                        ));
                    }
                }
                result.add(new EchoClientScreenOption("Create New World", EchoClientScreenCommand.OPEN_CREATE_WORLD, true));
                EchoClientSaveSlotSummary selectedSlot = selectedSaveSlotSummary();
                boolean canInspectSlot = selectedSlot != null;
                boolean canReadManifest = canInspectSlot && !selectedSlot.recoveryRequired();
                String managedSlotId = selectedSlot == null ? "" : selectedSlot.slotId();
                if (selectedSlot != null) {
                    addSaveSlotReviewOptions(result, selectedSlot);
                }
                result.add(EchoClientScreenOption.text(
                        "Rename To",
                        saveSlotRenameTextValue(),
                        editingTextField == EditableTextField.SAVE_SLOT_RENAME,
                        canReadManifest
                                ? "Edit the display name for " + managedSlotId
                                : canInspectSlot
                                        ? "Recovery saves must be repaired before rename"
                                        : "Select a saved world first"
                ));
                result.add(new EchoClientScreenOption(
                        "Rename World",
                        EchoClientScreenCommand.RENAME_SELECTED_WORLD,
                        canReadManifest && !saveSlotRenameText().isBlank(),
                        canReadManifest
                                ? "Persist a new display name for " + managedSlotId
                                : canInspectSlot
                                        ? "Recovery saves must be repaired before rename"
                                        : "Select a saved world first"
                ));
                result.add(new EchoClientScreenOption(
                        "Backup And Migration",
                        EchoClientScreenCommand.BACKUP_SELECTED_WORLD,
                        canReadManifest,
                        canReadManifest
                                ? "Create a backup and check migration readiness for " + managedSlotId
                                : canInspectSlot
                                        ? "Recovery saves must be repaired before backup"
                                        : "Select a saved world first"
                ));
                result.add(new EchoClientScreenOption(
                        "Delete World",
                        EchoClientScreenCommand.DELETE_SELECTED_WORLD,
                        canInspectSlot,
                        canInspectSlot
                                ? "Delete " + managedSlotId + " from disk"
                                : "Select a saved world first"
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case CREATE_WORLD -> {
                result.add(new EchoClientScreenOption(presentation.createWorldActionLabel(), EchoClientScreenCommand.START_NEW_GAME, true));
                result.add(EchoClientScreenOption.text(
                        "World Name",
                        worldNameTextValue(),
                        editingTextField == EditableTextField.WORLD_NAME,
                        "Enter edits, Backspace removes, Esc/Enter commits"
                ));
                result.add(new EchoClientScreenOption(presentation.worldTypeLabel(), EchoClientScreenCommand.NONE, false));
                result.add(EchoClientScreenOption.text(
                        "Seed",
                        worldSeedTextValue(),
                        editingTextField == EditableTextField.WORLD_SEED,
                        "Enter edits, Backspace removes, Esc/Enter commits"
                ));
                result.add(new EchoClientScreenOption(presentation.packLabel(), EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case OPTIONS -> {
                result.add(new EchoClientScreenOption("Controls", EchoClientScreenCommand.OPEN_CONTROLS, true));
                result.add(new EchoClientScreenOption("Video Settings", EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, true));
                result.add(new EchoClientScreenOption("Audio Settings", EchoClientScreenCommand.OPEN_AUDIO_SETTINGS, true));
                result.add(new EchoClientScreenOption("Accessibility", EchoClientScreenCommand.OPEN_ACCESSIBILITY_SETTINGS, true));
                result.add(new EchoClientScreenOption("Language", EchoClientScreenCommand.OPEN_LANGUAGE_SETTINGS, true));
                result.add(new EchoClientScreenOption("Mods", EchoClientScreenCommand.OPEN_MODS, true));
                result.add(new EchoClientScreenOption("Resource Packs", EchoClientScreenCommand.OPEN_RESOURCE_PACKS, true));
                result.add(new EchoClientScreenOption("Diagnostics", EchoClientScreenCommand.OPEN_DIAGNOSTICS, true));
                result.add(new EchoClientScreenOption(
                        "Export Support Bundle",
                        EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE,
                        true,
                        "Write a local client diagnostics zip for support"
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case CONTROLS -> {
                result.add(EchoClientScreenOption.slider("Mouse Sensitivity", mouseSensitivity, 0, 100, "Use left/right to adjust camera sensitivity"));
                result.add(new EchoClientScreenOption("Key Bindings", EchoClientScreenCommand.NONE, false));
                result.add(EchoClientScreenOption.toggle("Raw Mouse Input", rawMouseInput, "Use left/right or Enter to toggle raw mouse input"));
                for (EchoClientKeyAction action : EchoClientKeyAction.controlsScreenActions()) {
                    boolean rebinding = action == pendingKeyRebindAction;
                    result.add(EchoClientScreenOption.target(
                            action.displayName() + ": " + (rebinding ? "Press a key" : keyBindings.label(action)),
                            EchoClientScreenCommand.START_KEY_REBIND,
                            action.id(),
                            rebinding
                                    ? "Press any supported key to bind " + action.displayName() + ", or Esc to cancel"
                                    : "Enter selects " + action.displayName() + " for rebinding"
                    ));
                }
                result.add(new EchoClientScreenOption(
                        "Hotbar: " + keyBindings.hotbarSummary(),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Hotbar slots are bound to " + keyBindings.hotbarSummary()
                ));
                result.add(new EchoClientScreenOption(
                        "Reset Key Bindings",
                        EchoClientScreenCommand.RESET_KEY_BINDINGS,
                        true,
                        "Restore Minecraft-like runtime key defaults"
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case VIDEO_SETTINGS -> {
                result.add(EchoClientScreenOption.slider(
                        "FOV",
                        fovDegrees,
                        EchoClientSettings.MIN_FOV_DEGREES,
                        EchoClientSettings.MAX_FOV_DEGREES,
                        "Use left/right to adjust camera field of view"
                ));
                result.add(EchoClientScreenOption.slider("UI Scale", uiScale, 0, 100, "Use left/right to adjust UI scale target"));
                result.add(EchoClientScreenOption.toggle("Fullscreen", fullscreen, "Use left/right or Enter to toggle fullscreen preference"));
                result.add(EchoClientScreenOption.toggle("VSync", vSync, "Use left/right or Enter to toggle display sync"));
                result.add(EchoClientScreenOption.slider(
                        "Chunk View",
                        chunkViewDistance,
                        EchoClientSettings.MIN_CHUNK_VIEW_DISTANCE,
                        EchoClientSettings.MAX_CHUNK_VIEW_DISTANCE,
                        "Use left/right to adjust streamed and rendered chunk distance"
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case AUDIO_SETTINGS -> {
                result.add(EchoClientScreenOption.slider("Master Volume", masterVolume, 0, 100, "Use left/right to adjust master volume"));
                result.add(EchoClientScreenOption.slider("Music Volume", musicVolume, 0, 100, "Use left/right to adjust music volume"));
                result.add(EchoClientScreenOption.slider("Ambience Volume", ambienceVolume, 0, 100, "Use left/right to adjust ambience volume"));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case ACCESSIBILITY_SETTINGS -> {
                result.add(EchoClientScreenOption.toggle(
                        "Subtitles",
                        subtitles,
                        "Show sound event subtitles through the runtime subtitle overlay"
                ));
                result.add(EchoClientScreenOption.toggle(
                        "High Contrast UI",
                        highContrastUi,
                        "Use the high-contrast ScreenCore palette when that theme is available"
                ));
                result.add(EchoClientScreenOption.toggle(
                        "Reduced Motion",
                        reducedMotion,
                        "Reduce nonessential UI and particle motion where runtime renderers support it"
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case LANGUAGE_SETTINGS -> {
                long langFiles = resourcePacks.stream().mapToLong(EchoClientResourcePackSummary::langCount).sum();
                result.add(new EchoClientScreenOption(
                        "Language: " + languageDisplayName(languageCode),
                        EchoClientScreenCommand.CYCLE_LANGUAGE,
                        true,
                        "Locale " + languageCode + " resolves resource-pack lang files with en_us fallback"
                ));
                result.add(new EchoClientScreenOption("Locale: " + languageCode, EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Lang Files: " + langFiles, EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Resource Packs", EchoClientScreenCommand.OPEN_RESOURCE_PACKS, true));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case MODS -> {
                result.add(new EchoClientScreenOption(
                        "Module Scan: " + modScan.summaryLabel(),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Roots: " + modScan.roots().size() + " | " + modScan.issueLabel()
                ));
                result.add(new EchoClientScreenOption(
                        "AdapterCore Declarations: " + modScan.adapterCoreDeclaredCount(),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Native entrypoints: " + modScan.nativeEntrypointCount()
                                + " | graph issues: " + modScan.graphIssueCount()
                ));
                result.add(new EchoClientScreenOption(
                        "Runtime Content: " + runtimeContent.summaryLabel(),
                        EchoClientScreenCommand.NONE,
                        false,
                        runtimeContent.domainBreakdownLabel(8)
                ));
                for (String domainSummary : runtimeContent.topDomainSummaries(6)) {
                    result.add(new EchoClientScreenOption(domainSummary, EchoClientScreenCommand.NONE, false));
                }
                for (EchoClientRuntimeContentRowSummary row : runtimeContent.recentRows(8)) {
                    result.add(new EchoClientScreenOption(
                            row.menuLabel(),
                            EchoClientScreenCommand.NONE,
                            false,
                            row.detailLabel()
                    ));
                }
                if (runtimeContent.rowCount() > 8) {
                    result.add(new EchoClientScreenOption(
                            "More Runtime Content: " + (runtimeContent.rowCount() - 8),
                            EchoClientScreenCommand.NONE,
                            false,
                            "Open Diagnostics for full runtime import telemetry"
                    ));
                }
                for (EchoClientModSummary module : modScan.modules().stream().limit(12).toList()) {
                    result.add(new EchoClientScreenOption(
                            module.menuLabel(),
                            EchoClientScreenCommand.NONE,
                            false,
                            module.detailLabel()
                    ));
                }
                if (modScan.modules().size() > 12) {
                    result.add(new EchoClientScreenOption(
                            "More Modules: " + (modScan.modules().size() - 12),
                            EchoClientScreenCommand.NONE,
                            false,
                            "Open Diagnostics for domain and route details"
                    ));
                }
                result.add(new EchoClientScreenOption(
                        "AdapterCore UI: " + screenCatalog.modSummary(),
                        EchoClientScreenCommand.NONE,
                        false,
                        screenCatalog.diagnosticsSummary()
                ));
                result.add(new EchoClientScreenOption(
                        "ScreenCore: " + screenCatalog.screenCoreSummary(),
                        EchoClientScreenCommand.NONE,
                        false,
                        screenCatalog.diagnosticsSummary()
                ));
                if (screenCatalog.adapterCoreScreens().isEmpty()) {
                    result.add(new EchoClientScreenOption("No AdapterCore UI Screens", EchoClientScreenCommand.NONE, false));
                } else {
                    for (EchoClientScreenCatalogEntry screen : screenCatalog.adapterCoreScreens()) {
                        result.add(EchoClientScreenOption.target(
                                screen.menuLabel(),
                                EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                                screen.screenId(),
                                screen.tooltip()
                        ));
                    }
                }
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case RESOURCE_PACKS -> {
                if (resourcePacks.isEmpty()) {
                    result.add(new EchoClientScreenOption("No Resource Packs Found", EchoClientScreenCommand.NONE, false));
                } else {
                    for (EchoClientResourcePackSummary pack : resourcePacks) {
                        result.add(EchoClientScreenOption.target(
                                pack.menuLabel(),
                                EchoClientScreenCommand.OPEN_RESOURCE_PACK_DETAIL,
                                pack.id(),
                                pack.detailLabel()
                        ));
                    }
                }
                result.add(new EchoClientScreenOption("Refresh Resource Packs", EchoClientScreenCommand.REFRESH_RESOURCE_PACKS, true));
                result.add(new EchoClientScreenOption("Texture Atlas Reload", EchoClientScreenCommand.RELOAD_TEXTURE_ATLAS, true));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case RESOURCE_PACK_DETAIL -> {
                EchoClientResourcePackSummary pack = selectedResourcePack();
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
                if (pack == null) {
                    result.add(new EchoClientScreenOption("No Resource Pack Selected", EchoClientScreenCommand.NONE, false));
                } else {
                    result.add(new EchoClientScreenOption("Pack: " + pack.id(), EchoClientScreenCommand.NONE, false,
                            pack.detailLabel()));
                    result.add(new EchoClientScreenOption("Root: " + pack.root(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Namespaces: " + String.join(", ", new TreeSet<>(pack.namespaces())),
                            EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Textures: " + pack.textureCount(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Animated Textures: "
                            + pack.animatedTextureMetadataCount(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Models: " + pack.modelCount(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Blockstates: " + pack.blockstateCount(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Lang Files: " + pack.langCount(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Sound Events: " + pack.soundEventCount(), EchoClientScreenCommand.NONE, false));
                }
            }
            case INVENTORY -> {
            }
            case CONTAINER -> {
                result.add(new EchoClientScreenOption("Crash Cache", EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Player Inventory", EchoClientScreenCommand.OPEN_INVENTORY, hasSession));
                result.add(new EchoClientScreenOption("Workbench", EchoClientScreenCommand.OPEN_WORKBENCH, hasSession));
                result.add(new EchoClientScreenOption("Transfer All", EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Sort Container", EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case WORKBENCH -> {
                if (!workbenchRecipeError.isBlank()) {
                    result.add(new EchoClientScreenOption("Recipe Data Error", EchoClientScreenCommand.NONE, false,
                            workbenchRecipeError));
                }
                if (workbenchRecipes.isEmpty()) {
                    result.add(new EchoClientScreenOption("No Recipes Loaded", EchoClientScreenCommand.NONE, false));
                } else {
                    for (EchoClientWorkbenchRecipeSummary recipe : workbenchRecipes) {
                        result.add(EchoClientScreenOption.target(
                                recipe.craftable() ? "Craft " + recipe.label() : recipe.label(),
                                EchoClientScreenCommand.CRAFT_WORKBENCH_RECIPE,
                                recipe.recipeId(),
                                recipe.tooltip(),
                                recipe.craftable()
                        ));
                    }
                }
                result.add(new EchoClientScreenOption("Player Inventory", EchoClientScreenCommand.OPEN_INVENTORY, hasSession));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case MACHINE -> {
                result.add(new EchoClientScreenOption("Open Workbench", EchoClientScreenCommand.OPEN_WORKBENCH, hasSession,
                        "Open item-runtime recipes and craftable machine parts"));
                result.add(new EchoClientScreenOption("Container IO", EchoClientScreenCommand.OPEN_CONTAINER, hasSession,
                        "Open the current machine/container item slots"));
                result.addAll(techSurface.machineOptions());
                result.add(new EchoClientScreenOption(
                        "AdapterCore Machines: " + screenCatalog.domainCount(EchoAdapterCoreDomain.MACHINES),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Machine contracts declared by AdapterCore modules"
                ));
                result.add(new EchoClientScreenOption(
                        "AdapterCore Power: " + screenCatalog.domainCount(EchoAdapterCoreDomain.POWER),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Power-network and energy storage contracts declared by AdapterCore modules"
                ));
                result.add(new EchoClientScreenOption(
                        "AdapterCore Recipes: " + screenCatalog.domainCount(EchoAdapterCoreDomain.RECIPES),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Recipe contracts available to standalone machine UIs"
                ));
                result.add(new EchoClientScreenOption(
                        "Runtime Machine Rows: " + runtimeDomainCount("machines"),
                        EchoClientScreenCommand.NONE,
                        false,
                        runtimeContent.domainBreakdownLabel(8)
                ));
                result.add(new EchoClientScreenOption(
                        "Runtime Power Rows: " + runtimeDomainCount("power"),
                        EchoClientScreenCommand.NONE,
                        false,
                        runtimeContent.domainBreakdownLabel(8)
                ));
                result.add(new EchoClientScreenOption(
                        "Workbench Recipes: " + workbenchRecipes.size(),
                        EchoClientScreenCommand.NONE,
                        false,
                        workbenchRecipeError.isBlank()
                                ? "Loaded item-runtime recipes backing machine part crafting"
                                : workbenchRecipeError
                ));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case TERMINAL -> {
                EchoClientScreenCatalogEntry terminalScreen = firstAdapterCoreScreenContaining("terminal");
                if (terminalScreen == null) {
                    result.add(new EchoClientScreenOption(
                            "No Field Terminal Route",
                            EchoClientScreenCommand.NONE,
                            false,
                            "No AdapterCore terminal screen registration is available"
                    ));
                } else {
                    result.add(EchoClientScreenOption.target(
                            "Open " + terminalScreen.title(),
                            EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                            terminalScreen.screenId(),
                            terminalScreen.tooltip()
                    ));
                }
                result.addAll(techSurface.terminalOptions());
                result.add(new EchoClientScreenOption(
                        "AdapterCore UI Screens: " + screenCatalog.domainCount(EchoAdapterCoreDomain.UI_SCREENS),
                        EchoClientScreenCommand.NONE,
                        false,
                        screenCatalog.modSummary()
                ));
                result.add(new EchoClientScreenOption(
                        "AdapterCore Commands: " + screenCatalog.domainCount(EchoAdapterCoreDomain.COMMANDS),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Command contracts available to terminal shells"
                ));
                result.add(new EchoClientScreenOption(
                        "Runtime Terminal Rows: " + runtimeRowsContaining("terminal"),
                        EchoClientScreenCommand.NONE,
                        false,
                        runtimeContent.domainBreakdownLabel(8)
                ));
                result.add(new EchoClientScreenOption(
                        "Runtime UI Imports: " + runtimeDomainCount("ui_screens"),
                        EchoClientScreenCommand.NONE,
                        false,
                        "Native-imported UI surface rows available to ScreenCore"
                ));
                result.add(new EchoClientScreenOption("Diagnostics", EchoClientScreenCommand.OPEN_DIAGNOSTICS, true));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case DIAGNOSTICS -> {
                result.add(new EchoClientScreenOption(
                        "Export Support Bundle",
                        EchoClientScreenCommand.EXPORT_SUPPORT_BUNDLE,
                        true,
                        "Write a local client diagnostics zip for support"
                ));
                result.add(new EchoClientScreenOption(
                        supportBundleResult.menuLabel(),
                        EchoClientScreenCommand.NONE,
                        false,
                        supportBundleResult.exported()
                                ? supportBundleResult.archivePath()
                                : supportBundleResult.message()
                ));
                result.add(new EchoClientScreenOption("Runtime State: " + state, EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Screen: " + screenKind, EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Save Slots: " + saveSlots.size(), EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Modules: " + modScan.summaryLabel(),
                        EchoClientScreenCommand.NONE, false, modScan.issueLabel()));
                result.add(new EchoClientScreenOption("Resource Packs: " + resourcePacks.size(), EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Workbench Recipes: " + workbenchRecipes.size(),
                        EchoClientScreenCommand.NONE, false));
                result.add(new EchoClientScreenOption("Runtime Content: " + runtimeContent.summaryLabel(),
                        EchoClientScreenCommand.NONE, false, runtimeContent.domainBreakdownLabel(8)));
                result.add(new EchoClientScreenOption(
                        "ScreenCore Routes: " + screenCatalog.screenCount(),
                        EchoClientScreenCommand.NONE,
                        false,
                        screenCatalog.diagnosticsSummary()
                ));
                result.add(new EchoClientScreenOption(
                        "AdapterCore UI Screens: " + screenCatalog.adapterCoreScreenCount(),
                        EchoClientScreenCommand.NONE,
                        false,
                        screenCatalog.modSummary()
                ));
                for (String line : runtimeDiagnostics.lines()) {
                    result.add(new EchoClientScreenOption(
                            line,
                            EchoClientScreenCommand.NONE,
                            false,
                            "Live standalone world diagnostic"
                    ));
                }
                for (String domainSummary : screenCatalog.topDomainSummaries(8)) {
                    result.add(new EchoClientScreenOption(domainSummary, EchoClientScreenCommand.NONE, false));
                }
                for (EchoClientScreenCatalogEntry screen : screenCatalog.adapterCoreScreens()) {
                    result.add(EchoClientScreenOption.target(
                            "Route " + screen.diagnosticLabel(),
                            EchoClientScreenCommand.OPEN_REGISTERED_SCREEN,
                            screen.screenId(),
                            screen.tooltip()
                    ));
                }
                result.add(new EchoClientScreenOption("Refresh Resource Packs", EchoClientScreenCommand.REFRESH_RESOURCE_PACKS, true));
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
            }
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                result.add(new EchoClientScreenOption("Back", EchoClientScreenCommand.BACK, true));
                if (screen == null) {
                    result.add(new EchoClientScreenOption("No Registered Screen Selected", EchoClientScreenCommand.NONE, false));
                } else {
                    result.add(new EchoClientScreenOption("Source: " + screen.source(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Route: " + screen.route().route(), EchoClientScreenCommand.NONE, false));
                    result.add(new EchoClientScreenOption("Focus: " + screen.route().focusPath(), EchoClientScreenCommand.NONE, false));
                    for (String line : screen.lines()) {
                        result.add(new EchoClientScreenOption(line, EchoClientScreenCommand.NONE, false));
                    }
                }
            }
        }
        return result;
    }

    private void moveSelection(List<EchoClientScreenOption> options, int direction) {
        if (options.isEmpty()) {
            selectedIndex = -1;
            return;
        }
        if (selectedIndex < 0) {
            selectedIndex = firstEnabledIndex(options, 0);
            return;
        }
        int next = selectedIndex;
        for (int attempt = 0; attempt < options.size(); attempt++) {
            next += direction;
            if (next < 0) {
                next = options.size() - 1;
            } else if (next >= options.size()) {
                next = 0;
            }
            if (options.get(next).enabled()) {
                selectedIndex = next;
                return;
            }
        }
    }

    private void openScreen(EchoClientScreenKind nextScreenKind, boolean hasSession) {
        if (nextScreenKind != EchoClientScreenKind.CONTROLS) {
            pendingKeyRebindAction = null;
        }
        if (state == EchoClientGameState.IN_GAME) {
            returnToGameplayOnBack = true;
        } else if (screenKind != nextScreenKind) {
            screenBackStack.add(screenKind);
        }
        if (nextScreenKind != EchoClientScreenKind.REGISTERED_SCREEN) {
            registeredScreenId = "";
        }
        screenKind = nextScreenKind;
        state = EchoClientGameState.SCREEN_OPEN;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(hasSession), 0);
        footer = "Esc backs out";
        publishMenu(hasSession);
    }

    private boolean openSelectedRegisteredScreen(boolean hasSession) {
        return openRegisteredScreen(selectedTargetId(hasSession), hasSession);
    }

    private boolean openSelectedResourcePackDetail(boolean hasSession) {
        String targetId = selectedTargetId(hasSession);
        if (targetId.isBlank() || resourcePack(targetId) == null) {
            showToast("Resource pack unavailable");
            return false;
        }
        selectedResourcePackId = targetId;
        openScreen(EchoClientScreenKind.RESOURCE_PACK_DETAIL, hasSession);
        return true;
    }

    boolean openRegisteredScreen(String targetId, boolean hasSession) {
        EchoClientScreenCatalogEntry screen = screenCatalog.findScreen(targetId).orElse(null);
        if (screen == null) {
            showToast("Registered screen unavailable");
            return false;
        }
        if (state == EchoClientGameState.IN_GAME) {
            returnToGameplayOnBack = true;
        } else if (screenKind != EchoClientScreenKind.REGISTERED_SCREEN) {
            screenBackStack.add(screenKind);
        }
        registeredScreenId = screen.screenId();
        screenKind = EchoClientScreenKind.REGISTERED_SCREEN;
        state = EchoClientGameState.SCREEN_OPEN;
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(hasSession), 0);
        footer = "Esc backs out";
        publishMenu(hasSession);
        return true;
    }

    private void back(boolean hasSession) {
        pendingKeyRebindAction = null;
        if (screenBackStack.isEmpty()) {
            registeredScreenId = "";
            if (hasSession && returnToGameplayOnBack) {
                showInGame();
            } else if (hasSession) {
                showPauseMenu();
            } else {
                showMainMenu(false);
            }
            return;
        }
        screenKind = screenBackStack.removeLast();
        if (screenKind != EchoClientScreenKind.REGISTERED_SCREEN) {
            registeredScreenId = "";
        }
        state = stateForScreenKind(screenKind);
        scrollOffset = 0;
        selectedIndex = firstEnabledIndex(options(hasSession), 0);
        footer = footerForScreenKind();
        publishMenu(hasSession);
    }

    private int hitOption(double pointerX, double pointerY, int width, int height, int optionCount) {
        if (optionCount <= 0 || state == EchoClientGameState.SAVING) {
            return -1;
        }
        int visibleCount = menuVisibleCount(height, optionCount);
        scrollOffset = clampScroll(scrollOffset, optionCount, height);
        int startX = (width - MENU_BUTTON_WIDTH) / 2;
        int startY = menuStartY(height, visibleCount);
        for (int visibleIndex = 0; visibleIndex < visibleCount; visibleIndex++) {
            int y = startY + visibleIndex * (MENU_BUTTON_HEIGHT + MENU_BUTTON_SPACING);
            if (pointerX >= startX
                    && pointerX <= startX + MENU_BUTTON_WIDTH
                    && pointerY >= y
                    && pointerY <= y + MENU_BUTTON_HEIGHT) {
                return scrollOffset + visibleIndex;
            }
        }
        return -1;
    }

    static int menuStartY(int height, int optionCount) {
        return Math.max(178, height / 2 - optionCount * 22);
    }

    static int menuVisibleCount(int height, int optionCount) {
        if (optionCount <= 0) {
            return 0;
        }
        int rows = Math.max(3, (height - MENU_VERTICAL_SAFE_ZONE) / (MENU_BUTTON_HEIGHT + MENU_BUTTON_SPACING));
        return Math.max(1, Math.min(optionCount, rows));
    }

    private int firstEnabledIndex(List<EchoClientScreenOption> options, int preferredIndex) {
        if (options.isEmpty()) {
            return -1;
        }
        if (preferredIndex >= 0 && preferredIndex < options.size() && options.get(preferredIndex).enabled()) {
            return preferredIndex;
        }
        for (int index = 0; index < options.size(); index++) {
            if (options.get(index).enabled()) {
                return index;
            }
        }
        return 0;
    }

    private int firstLoadableSaveSlotIndex() {
        for (int index = 0; index < saveSlots.size(); index++) {
            if (saveSlots.get(index).loadableInMemory()) {
                return index;
            }
        }
        return firstEnabledIndex(options(false), 0);
    }

    private EchoClientSaveSlotSummary selectedSaveSlotSummary() {
        if (screenKind != EchoClientScreenKind.WORLD_SELECT) {
            return null;
        }
        EchoClientSaveSlotSummary managedSlot = saveSlot(selectedManagedWorldSlotId);
        if (managedSlot != null) {
            return managedSlot;
        }
        if (selectedIndex >= 0 && selectedIndex < saveSlots.size()) {
            return saveSlots.get(selectedIndex);
        }
        EchoClientSaveSlotSummary loadableSlot = saveSlot(selectedWorldSlotId);
        if (loadableSlot != null) {
            return loadableSlot;
        }
        return null;
    }

    private EchoClientSaveSlotThumbnailSnapshot saveSlotThumbnailSnapshot() {
        if (screenKind != EchoClientScreenKind.WORLD_SELECT) {
            return EchoClientSaveSlotThumbnailSnapshot.EMPTY;
        }
        return EchoClientSaveSlotThumbnailSnapshot.from(selectedSaveSlotSummary());
    }

    private void addSaveSlotReviewOptions(
            ArrayList<EchoClientScreenOption> result,
            EchoClientSaveSlotSummary slot
    ) {
        List<String> reviewLines = slot.reviewLines();
        int maxLines = Math.min(8, reviewLines.size());
        for (int index = 0; index < maxLines; index++) {
            String line = reviewLines.get(index);
            result.add(new EchoClientScreenOption(
                    "Review " + line,
                    EchoClientScreenCommand.NONE,
                    false,
                    line
            ));
        }
    }

    private static String saveSlotRowTooltip(EchoClientSaveSlotSummary slot) {
        if (slot == null) {
            return "";
        }
        if (slot.recoveryRequired()) {
            return "Inspect recovery details for " + slot.slotId();
        }
        if (!slot.loadableInMemory()) {
            return "Inspect compatibility warnings for " + slot.slotId();
        }
        return "Load " + slot.slotId();
    }

    private void rememberSelectedSaveSlot() {
        String previousSlotId = selectedWorldSlotId;
        String previousManagedSlotId = selectedManagedWorldSlotId;
        if (screenKind == EchoClientScreenKind.WORLD_SELECT
                && selectedIndex >= 0
                && selectedIndex < saveSlots.size()) {
            EchoClientSaveSlotSummary selectedSlot = saveSlots.get(selectedIndex);
            selectedManagedWorldSlotId = selectedSlot.slotId();
            if (selectedSlot.loadableInMemory()) {
                selectedWorldSlotId = selectedSlot.slotId();
            }
        } else if (!selectedManagedWorldSlotId.isBlank() && saveSlot(selectedManagedWorldSlotId) == null) {
            selectedManagedWorldSlotId = firstSaveSlotId();
        } else if (!selectedWorldSlotId.isBlank() && !saveSlotLoadable(selectedWorldSlotId)) {
            selectedWorldSlotId = firstLoadableSaveSlotId();
        }
        if (editingTextField != EditableTextField.SAVE_SLOT_RENAME
                && !selectedManagedWorldSlotId.isBlank()
                && (!selectedManagedWorldSlotId.equals(previousManagedSlotId)
                        || !selectedWorldSlotId.equals(previousSlotId)
                        || saveSlotRenameText.isBlank())) {
            saveSlotRenameText = labelForSaveSlotId(selectedManagedWorldSlotId);
        }
    }

    private boolean saveSlotLoadable(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return false;
        }
        for (EchoClientSaveSlotSummary slot : saveSlots) {
            if (slot.slotId().equals(slotId) && slot.loadableInMemory()) {
                return true;
            }
        }
        return false;
    }

    private String firstLoadableSaveSlotId() {
        for (EchoClientSaveSlotSummary slot : saveSlots) {
            if (slot.loadableInMemory()) {
                return slot.slotId();
            }
        }
        return "";
    }

    private String firstSaveSlotId() {
        return saveSlots.isEmpty() ? "" : saveSlots.get(0).slotId();
    }

    private EchoClientSaveSlotSummary saveSlot(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return null;
        }
        for (EchoClientSaveSlotSummary slot : saveSlots) {
            if (slot.slotId().equals(slotId)) {
                return slot;
            }
        }
        return null;
    }

    private String labelForSaveSlotId(String slotId) {
        if (slotId == null || slotId.isBlank()) {
            return "";
        }
        for (EchoClientSaveSlotSummary slot : saveSlots) {
            if (slot.slotId().equals(slotId)) {
                return slot.displayName();
            }
        }
        return "";
    }

    private int firstResourcePackIndex() {
        return resourcePacks.isEmpty() ? firstEnabledIndex(options(false), 0) : 0;
    }

    private int runtimeDomainCount(String domain) {
        if (domain == null || domain.isBlank()) {
            return 0;
        }
        return runtimeContent.domainCounts().getOrDefault(domain.trim().toLowerCase(java.util.Locale.ROOT), 0);
    }

    private int runtimeRowsContaining(String token) {
        if (token == null || token.isBlank()) {
            return 0;
        }
        String normalizedToken = token.trim().toLowerCase(java.util.Locale.ROOT);
        int count = 0;
        for (EchoClientRuntimeContentRowSummary row : runtimeContent.rows()) {
            String haystack = (row.contentId() + " " + row.displayName() + " "
                    + row.moduleId() + " " + row.nativeLoaderId() + " "
                    + row.standaloneRuntimeId()).toLowerCase(java.util.Locale.ROOT);
            if (haystack.contains(normalizedToken)) {
                count++;
            }
        }
        return count;
    }

    private EchoClientScreenCatalogEntry firstAdapterCoreScreenContaining(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String normalizedToken = token.trim().toLowerCase(java.util.Locale.ROOT);
        for (EchoClientScreenCatalogEntry screen : screenCatalog.adapterCoreScreens()) {
            String haystack = (screen.screenId() + " " + screen.title() + " "
                    + screen.contentId() + " " + screen.nativeLoaderId() + " "
                    + screen.standaloneRuntimeId()).toLowerCase(java.util.Locale.ROOT);
            if (haystack.contains(normalizedToken)) {
                return screen;
            }
        }
        return null;
    }

    private void ensureSelectedVisible(int optionCount, int height) {
        if (selectedIndex < 0 || optionCount <= 0) {
            scrollOffset = 0;
            return;
        }
        int visibleCount = menuVisibleCount(height, optionCount);
        if (selectedIndex < scrollOffset) {
            scrollOffset = selectedIndex;
        } else if (selectedIndex >= scrollOffset + visibleCount) {
            scrollOffset = selectedIndex - visibleCount + 1;
        }
        scrollOffset = clampScroll(scrollOffset, optionCount, height);
    }

    private int clampScroll(int requestedOffset, int optionCount, int height) {
        int visibleCount = menuVisibleCount(height, optionCount);
        int maxOffset = Math.max(0, optionCount - visibleCount);
        return Math.max(0, Math.min(maxOffset, requestedOffset));
    }

    private String selectedTooltip(List<EchoClientScreenOption> options) {
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            return screenDescription();
        }
        EchoClientScreenOption selected = options.get(selectedIndex);
        if (!selected.tooltip().isBlank()) {
            return selected.tooltip();
        }
        if (!selected.enabled()) {
            return "Unavailable";
        }
        return switch (selected.command()) {
            case START_NEW_GAME -> "Create and load a standalone world session";
            case CONTINUE_GAME -> "Load the selected save or in-memory session";
            case RESUME_GAME -> "Return to the active world";
            case RESPAWN -> "Respawn at the world spawn with restored health";
            case SAVE_GAME -> "Write the current world session";
            case OPEN_WORLD_SELECT -> "Browse discovered save slots";
            case OPEN_CREATE_WORLD -> "Choose world setup options";
            case OPEN_OPTIONS -> "Open client settings";
            case OPEN_CONTROLS -> "View input settings";
            case START_KEY_REBIND -> "Select a control and press a new key";
            case OPEN_VIDEO_SETTINGS -> "View renderer settings";
            case OPEN_AUDIO_SETTINGS -> "View audio settings";
            case OPEN_ACCESSIBILITY_SETTINGS -> "View subtitle and accessibility settings";
            case OPEN_LANGUAGE_SETTINGS -> "Choose the runtime language locale";
            case CYCLE_LANGUAGE -> "Cycle the active lang locale";
            case RESET_KEY_BINDINGS -> "Restore the default runtime keyboard controls";
            case OPEN_MODS -> "Inspect runtime mod surfaces";
            case OPEN_RESOURCE_PACKS -> "Inspect mounted Minecraft resource packs";
            case OPEN_INVENTORY -> "Open the live player inventory";
            case OPEN_CONTAINER -> "Open a container screen route";
            case OPEN_WORKBENCH -> "Open the ScreenCore workbench recipe route";
            case OPEN_MACHINE -> "Open the ScreenCore machine and power surface";
            case INSERT_MACHINE_INPUT -> "Insert one Scrap Metal into the selected machine input";
            case EXTRACT_MACHINE_OUTPUT -> "Extract one Compressed Scrap from the selected machine buffer";
            case SELECT_MACHINE_RECIPE -> "Select the active recipe for the selected machine";
            case OPEN_TERMINAL -> "Open the ScreenCore terminal surface";
            case CRAFT_WORKBENCH_RECIPE -> "Craft the selected item-runtime recipe";
            case OPEN_DIAGNOSTICS -> "Inspect runtime and ScreenCore state";
            case EXPORT_SUPPORT_BUNDLE -> "Export a local client diagnostics support bundle";
            case OPEN_REGISTERED_SCREEN -> "Open the registered AdapterCore ScreenCore route";
            case REFRESH_RESOURCE_PACKS -> "Rescan pack roots and rebuild texture caches";
            case OPEN_RESOURCE_PACK_DETAIL -> "Inspect namespaces, textures, models, lang, and sound events for this pack";
            case RELOAD_TEXTURE_ATLAS -> "Reload the current resource-pack-backed texture atlas";
            case BACKUP_SELECTED_WORLD -> "Create a backup and check migration readiness for the selected world";
            case RENAME_SELECTED_WORLD -> "Rename the selected saved world without changing its slot id";
            case DELETE_SELECTED_WORLD -> "Delete the selected saved world";
            case BACK -> "Return to the previous screen";
            case QUIT_TO_TITLE -> "Unload the world and return to title";
            case QUIT_CLIENT -> "Close the standalone client";
            case NONE -> screenDescription();
        };
    }

    private boolean activateControl(EchoClientScreenOption selected) {
        return switch (selected.kind()) {
            case TOGGLE -> {
                toggleSelectedControl(selected);
                showToast(selected.label() + " " + currentValueText(selected.label()));
                yield true;
            }
            case SLIDER -> {
                adjustSelectedSlider(selected.label(), 1);
                showToast(selected.label() + " " + currentValueText(selected.label()));
                yield true;
            }
            case TEXT -> {
                EditableTextField selectedField = textFieldForLabel(selected.label());
                if (editingTextField == selectedField) {
                    stopTextEditing(false);
                } else {
                    beginTextEditing(selected.label());
                }
                yield true;
            }
            case BUTTON -> false;
        };
    }

    private void beginTextEditing(String label) {
        editingTextField = textFieldForLabel(label);
    }

    private EditableTextField textFieldForLabel(String label) {
        if ("World Name".equals(label)) {
            return EditableTextField.WORLD_NAME;
        }
        if ("Seed".equals(label)) {
            return EditableTextField.WORLD_SEED;
        }
        if ("Rename To".equals(label)) {
            return EditableTextField.SAVE_SLOT_RENAME;
        }
        return EditableTextField.NONE;
    }

    private String activeTextValue() {
        return editingTextField.value(this);
    }

    private void setActiveTextValue(String value) {
        switch (editingTextField) {
            case WORLD_NAME -> worldName = limitText(value, WORLD_NAME_LIMIT);
            case WORLD_SEED -> worldSeed = limitText(value, WORLD_SEED_LIMIT);
            case SAVE_SLOT_RENAME -> saveSlotRenameText = limitText(value, WORLD_NAME_LIMIT);
            case NONE -> {
            }
        }
    }

    private void toggleSelectedControl(EchoClientScreenOption selected) {
        switch (selected.label()) {
            case "Raw Mouse Input" -> {
                rawMouseInput = !rawMouseInput;
                clientSettingsDirty = true;
            }
            case "Fullscreen" -> {
                toggleFullscreenPreference();
            }
            case "VSync" -> {
                vSync = !vSync;
                clientSettingsDirty = true;
            }
            case "Subtitles" -> {
                subtitles = !subtitles;
                clientSettingsDirty = true;
            }
            case "High Contrast UI" -> {
                highContrastUi = !highContrastUi;
                clientSettingsDirty = true;
            }
            case "Reduced Motion" -> {
                reducedMotion = !reducedMotion;
                clientSettingsDirty = true;
            }
            default -> {
            }
        }
    }

    private void adjustSelectedSlider(String label, int direction) {
        int step = direction > 0 ? 5 : -5;
        switch (label) {
            case "Mouse Sensitivity" -> {
                mouseSensitivity = clampPercent(mouseSensitivity + step);
                clientSettingsDirty = true;
            }
            case "FOV" -> {
                fovDegrees = EchoClientSettings.clampFov(fovDegrees + step);
                clientSettingsDirty = true;
            }
            case "UI Scale" -> {
                uiScale = clampPercent(uiScale + step);
                clientSettingsDirty = true;
            }
            case "Chunk View" -> {
                chunkViewDistance = EchoClientSettings.clampChunkViewDistance(
                        chunkViewDistance + (direction > 0 ? 1 : -1)
                );
                clientSettingsDirty = true;
            }
            case "Master Volume" -> {
                masterVolume = clampPercent(masterVolume + step);
                clientSettingsDirty = true;
            }
            case "Music Volume" -> {
                musicVolume = clampPercent(musicVolume + step);
                clientSettingsDirty = true;
            }
            case "Ambience Volume" -> {
                ambienceVolume = clampPercent(ambienceVolume + step);
                clientSettingsDirty = true;
            }
            default -> {
            }
        }
    }

    private boolean setSelectedSliderFromPointer(String label, double pointerX, int width) {
        int startX = (width - MENU_BUTTON_WIDTH) / 2;
        double trackStart = startX + MENU_SLIDER_TRACK_X_OFFSET;
        double rawPercent = (pointerX - trackStart) / MENU_SLIDER_TRACK_WIDTH;
        double percent = Math.max(0.0D, Math.min(1.0D, rawPercent));
        switch (label) {
            case "Mouse Sensitivity" -> {
                return setPercentSliderValue(label, Math.round((float) (percent * 100.0D)));
            }
            case "FOV" -> {
                int min = EchoClientSettings.MIN_FOV_DEGREES;
                int max = EchoClientSettings.MAX_FOV_DEGREES;
                return setIntSliderValue(label, Math.round((float) (min + percent * (max - min))));
            }
            case "UI Scale" -> {
                return setPercentSliderValue(label, Math.round((float) (percent * 100.0D)));
            }
            case "Chunk View" -> {
                int min = EchoClientSettings.MIN_CHUNK_VIEW_DISTANCE;
                int max = EchoClientSettings.MAX_CHUNK_VIEW_DISTANCE;
                return setIntSliderValue(label, Math.round((float) (min + percent * (max - min))));
            }
            case "Master Volume" -> {
                return setPercentSliderValue(label, Math.round((float) (percent * 100.0D)));
            }
            case "Music Volume" -> {
                return setPercentSliderValue(label, Math.round((float) (percent * 100.0D)));
            }
            case "Ambience Volume" -> {
                return setPercentSliderValue(label, Math.round((float) (percent * 100.0D)));
            }
            default -> {
                return false;
            }
        }
    }

    private boolean setPercentSliderValue(String label, int value) {
        return setIntSliderValue(label, clampPercent(value));
    }

    private boolean setIntSliderValue(String label, int value) {
        switch (label) {
            case "Mouse Sensitivity" -> {
                int next = clampPercent(value);
                if (mouseSensitivity == next) {
                    return false;
                }
                mouseSensitivity = next;
            }
            case "FOV" -> {
                int next = EchoClientSettings.clampFov(value);
                if (fovDegrees == next) {
                    return false;
                }
                fovDegrees = next;
            }
            case "UI Scale" -> {
                int next = clampPercent(value);
                if (uiScale == next) {
                    return false;
                }
                uiScale = next;
            }
            case "Chunk View" -> {
                int next = EchoClientSettings.clampChunkViewDistance(value);
                if (chunkViewDistance == next) {
                    return false;
                }
                chunkViewDistance = next;
            }
            case "Master Volume" -> {
                int next = clampPercent(value);
                if (masterVolume == next) {
                    return false;
                }
                masterVolume = next;
            }
            case "Music Volume" -> {
                int next = clampPercent(value);
                if (musicVolume == next) {
                    return false;
                }
                musicVolume = next;
            }
            case "Ambience Volume" -> {
                int next = clampPercent(value);
                if (ambienceVolume == next) {
                    return false;
                }
                ambienceVolume = next;
            }
            default -> {
                return false;
            }
        }
        clientSettingsDirty = true;
        return true;
    }

    private String currentValueText(String label) {
        return switch (label) {
            case "Raw Mouse Input" -> rawMouseInput ? "ON" : "OFF";
            case "Fullscreen" -> fullscreen ? "ON" : "OFF";
            case "VSync" -> vSync ? "ON" : "OFF";
            case "Mouse Sensitivity" -> mouseSensitivity + "%";
            case "FOV" -> Integer.toString(fovDegrees);
            case "UI Scale" -> uiScale + "%";
            case "Chunk View" -> chunkViewDistance + " chunks";
            case "Master Volume" -> masterVolume + "%";
            case "Music Volume" -> musicVolume + "%";
            case "Ambience Volume" -> ambienceVolume + "%";
            case "Subtitles" -> subtitles ? "ON" : "OFF";
            case "High Contrast UI" -> highContrastUi ? "ON" : "OFF";
            case "Reduced Motion" -> reducedMotion ? "ON" : "OFF";
            default -> "";
        };
    }

    private void cycleLanguage(int direction) {
        int current = 0;
        for (int index = 0; index < LANGUAGE_CODES.length; index++) {
            if (LANGUAGE_CODES[index].equals(languageCode)) {
                current = index;
                break;
            }
        }
        int next = Math.floorMod(current + (direction >= 0 ? 1 : -1), LANGUAGE_CODES.length);
        languageCode = LANGUAGE_CODES[next];
        clientSettingsDirty = true;
        showToast("Language " + languageDisplayName(languageCode));
    }

    private static String languageDisplayName(String code) {
        return switch (EchoClientSettings.normalizeLanguageCode(code)) {
            case "en_gb" -> "English UK";
            case "de_de" -> "Deutsch";
            case "es_es" -> "Espanol";
            case "fr_fr" -> "Francais";
            case "ja_jp" -> "Japanese";
            default -> "English US";
        };
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String normalizedWorldName(String value) {
        String normalized = value == null || value.isBlank() ? "New World" : value.trim();
        return limitText(normalized, WORLD_NAME_LIMIT);
    }

    private String normalizedSaveSlotRenameText() {
        String selectedLabel = selectedManageSaveSlotLabel();
        String normalized = saveSlotRenameText == null || saveSlotRenameText.isBlank()
                ? selectedLabel
                : saveSlotRenameText.trim().replaceAll("\\s+", " ");
        return limitText(normalized, WORLD_NAME_LIMIT);
    }

    private static String limitText(String value, int maxLength) {
        String safe = value == null ? "" : value.stripLeading();
        if (safe.length() <= maxLength) {
            return safe;
        }
        return safe.substring(0, maxLength).stripTrailing();
    }

    private static boolean requiresConfirmation(EchoClientScreenCommand command) {
        return command == EchoClientScreenCommand.QUIT_TO_TITLE
                || command == EchoClientScreenCommand.QUIT_CLIENT
                || command == EchoClientScreenCommand.START_NEW_GAME
                || command == EchoClientScreenCommand.DELETE_SELECTED_WORLD;
    }

    private void openModal(EchoClientScreenCommand command) {
        pendingModalCommand = command;
        modalConfirmSelected = true;
        switch (command) {
            case QUIT_TO_TITLE -> {
                modalTitle = "QUIT TO TITLE";
                modalMessage = "Unload the active world and return to the main menu?";
                modalConfirmLabel = "Title";
                modalCancelLabel = "Cancel";
            }
            case QUIT_CLIENT -> {
                modalTitle = "QUIT CLIENT";
                modalMessage = "Close the standalone runtime window?";
                modalConfirmLabel = "Quit";
                modalCancelLabel = "Cancel";
            }
            case START_NEW_GAME -> {
                modalTitle = "CREATE WORLD";
                modalMessage = "Create '" + worldName() + "'? " + presentation.newWorldModalMessage();
                modalConfirmLabel = "Create";
                modalCancelLabel = "Back";
            }
            case DELETE_SELECTED_WORLD -> {
                modalTitle = "DELETE WORLD";
                modalMessage = "Delete selected save '" + selectedManageSaveSlotId() + "' from disk?";
                modalConfirmLabel = "Delete";
                modalCancelLabel = "Cancel";
            }
            default -> {
                modalTitle = "CONFIRM";
                modalMessage = "Run selected action?";
                modalConfirmLabel = "Confirm";
                modalCancelLabel = "Cancel";
            }
        }
        markSnapshotDirty();
    }

    private EchoClientScreenCommand finishModal(boolean confirm) {
        EchoClientScreenCommand command = confirm ? pendingModalCommand : EchoClientScreenCommand.NONE;
        closeModal();
        return command;
    }

    private void closeModal() {
        pendingModalCommand = EchoClientScreenCommand.NONE;
        modalTitle = "";
        modalMessage = "";
        modalConfirmLabel = "Confirm";
        modalCancelLabel = "Cancel";
        modalConfirmSelected = true;
        markSnapshotDirty();
    }

    private void markUiFeedback() {
        uiFeedbackPulses++;
    }

    private void markSnapshotDirty() {
        snapshotRevision++;
        cachedSnapshot = null;
        cachedSnapshotRevision = -1L;
    }

    private int modalButtonHit(double pointerX, double pointerY, int width, int height) {
        int panelW = Math.min(460, Math.max(320, width - 160));
        int panelH = 168;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        int buttonW = 132;
        int buttonH = 34;
        int gap = 18;
        int buttonY = panelY + panelH - 54;
        int cancelX = panelX + panelW / 2 - buttonW - gap / 2;
        int confirmX = panelX + panelW / 2 + gap / 2;
        if (pointerY < buttonY || pointerY > buttonY + buttonH) {
            return -1;
        }
        if (pointerX >= cancelX && pointerX <= cancelX + buttonW) {
            return 0;
        }
        if (pointerX >= confirmX && pointerX <= confirmX + buttonW) {
            return 1;
        }
        return -1;
    }

    private EchoClientModalSnapshot modalSnapshot() {
        if (!modalOpen()) {
            return EchoClientModalSnapshot.EMPTY;
        }
        return new EchoClientModalSnapshot(
                true,
                modalTitle,
                modalMessage,
                modalConfirmLabel,
                modalCancelLabel,
                modalConfirmSelected
        );
    }

    private EchoClientToastSnapshot toastSnapshot() {
        if (toastRemainingSeconds <= 0.0D || toastMessage.isBlank()) {
            return EchoClientToastSnapshot.EMPTY;
        }
        return new EchoClientToastSnapshot(
                true,
                toastMessage,
                toastRemainingSeconds / TOAST_SECONDS
        );
    }

    private void publishLoading() {
        uiBridge.showStatic("echoscreencore:loading", "ECHO ASHFALL", List.of(
                loading.label(),
                loading.detail(),
                "Tip " + loading.tip(),
                "Progress " + Math.round(loading.progress() * 100.0D) + "%",
                "Route: screencore.loading",
                "Focus: loading.progress"
        ), "loading.progress");
        markSnapshotDirty();
    }

    private void publishMenu(boolean hasSession) {
        publishMenuInternal(options(hasSession));
    }

    private void publishMenu(boolean hasSession, List<EchoClientScreenOption> options) {
        if (options == null || screenOptionContentChangesWithSelection()) {
            publishMenuInternal(options(hasSession));
            return;
        }
        publishMenuInternal(options);
    }

    private void publishMenuInternal(List<EchoClientScreenOption> options) {
        publishedOptions = options == null ? List.of() : List.copyOf(options);
        String screenId = screenId();
        String title = screenTitle();
        String route = route();
        String focus = focusPath();
        List<String> lines = new ArrayList<>();
        lines.add(screenSubtitle());
        lines.add(screenDescription());
        lines.add("Route: " + route);
        lines.add("Focus: " + focus);
        uiBridge.showMenu(screenId, title, options, selectedIndex, lines);
        markSnapshotDirty();
    }

    private List<EchoClientScreenOption> currentPublishedOptions(boolean hasSession) {
        if (!publishedOptions.isEmpty()) {
            return publishedOptions;
        }
        return options(hasSession);
    }

    private boolean screenOptionContentChangesWithSelection() {
        return screenKind == EchoClientScreenKind.WORLD_SELECT;
    }

    private String selectedTargetId(boolean hasSession) {
        List<EchoClientScreenOption> currentOptions = options(hasSession);
        if (selectedIndex < 0 || selectedIndex >= currentOptions.size()) {
            return "";
        }
        return currentOptions.get(selectedIndex).targetId();
    }

    private EchoClientScreenCatalogEntry registeredScreen() {
        return screenCatalog.findScreen(registeredScreenId).orElse(null);
    }

    private EchoClientResourcePackSummary selectedResourcePack() {
        return resourcePack(selectedResourcePackId);
    }

    private EchoClientResourcePackSummary resourcePack(String packId) {
        if (packId == null || packId.isBlank()) {
            return null;
        }
        for (EchoClientResourcePackSummary pack : resourcePacks) {
            if (pack.id().equals(packId)) {
                return pack;
            }
        }
        return null;
    }

    private EchoClientGameState stateForScreenKind(EchoClientScreenKind kind) {
        return switch (kind) {
            case MAIN_MENU -> EchoClientGameState.MAIN_MENU;
            case PAUSE_MENU -> EchoClientGameState.PAUSED;
            case DEATH_SCREEN -> EchoClientGameState.DEAD;
            case FATAL_ERROR -> EchoClientGameState.FATAL_ERROR;
            default -> EchoClientGameState.SCREEN_OPEN;
        };
    }

    private String footerForScreenKind() {
        return switch (screenKind) {
            case MAIN_MENU -> "Enter selects, Esc quits";
            case PAUSE_MENU -> "Esc resumes";
            case DEATH_SCREEN -> "Enter respawns";
            case FATAL_ERROR -> "Export a support bundle, return to title, or quit";
            default -> "Esc backs out";
        };
    }

    private String screenId() {
        return switch (screenKind) {
            case MAIN_MENU -> "echoscreencore:main_menu";
            case PAUSE_MENU -> "echoscreencore:pause_flow";
            case DEATH_SCREEN -> "echoscreencore:death_screen";
            case WORLD_SELECT -> "echoscreencore:world_select";
            case CREATE_WORLD -> "echoscreencore:create_world";
            case OPTIONS -> "echoscreencore:settings";
            case CONTROLS -> "echoscreencore:controls";
            case VIDEO_SETTINGS -> "echoscreencore:video_settings";
            case AUDIO_SETTINGS -> "echoscreencore:audio_settings";
            case ACCESSIBILITY_SETTINGS -> "echoscreencore:accessibility";
            case LANGUAGE_SETTINGS -> "echoscreencore:language";
            case MODS -> "echoscreencore:mods";
            case RESOURCE_PACKS -> "echoscreencore:resource_packs";
            case RESOURCE_PACK_DETAIL -> "echoscreencore:resource_pack_detail";
            case INVENTORY -> "echoscreencore:inventory";
            case CONTAINER -> "echoscreencore:container";
            case WORKBENCH -> "echoscreencore:workbench";
            case MACHINE -> "echoscreencore:machine";
            case TERMINAL -> "echoscreencore:terminal";
            case DIAGNOSTICS -> "echoscreencore:diagnostics";
            case FATAL_ERROR -> "echoscreencore:fatal_error";
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null ? "echoscreencore:registered_screen" : screen.screenId();
            }
        };
    }

    private String screenTitle() {
        return switch (screenKind) {
            case MAIN_MENU -> "ECHO ASHFALL";
            case PAUSE_MENU -> "GAME PAUSED";
            case DEATH_SCREEN -> "YOU DIED";
            case WORLD_SELECT -> "WORLD SELECT";
            case CREATE_WORLD -> "CREATE WORLD";
            case OPTIONS -> "OPTIONS";
            case CONTROLS -> "CONTROLS";
            case VIDEO_SETTINGS -> "VIDEO SETTINGS";
            case AUDIO_SETTINGS -> "AUDIO SETTINGS";
            case ACCESSIBILITY_SETTINGS -> "ACCESSIBILITY";
            case LANGUAGE_SETTINGS -> "LANGUAGE";
            case MODS -> "MODS";
            case RESOURCE_PACKS -> "RESOURCE PACKS";
            case RESOURCE_PACK_DETAIL -> {
                EchoClientResourcePackSummary pack = selectedResourcePack();
                yield pack == null ? "RESOURCE PACK" : pack.id();
            }
            case INVENTORY -> "INVENTORY";
            case CONTAINER -> "CONTAINER";
            case WORKBENCH -> "WORKBENCH";
            case MACHINE -> "MACHINE";
            case TERMINAL -> "TERMINAL";
            case DIAGNOSTICS -> "DIAGNOSTICS";
            case FATAL_ERROR -> "RUNTIME ERROR";
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null ? "REGISTERED SCREEN" : screen.title();
            }
        };
    }

    private String screenSubtitle() {
        return switch (screenKind) {
            case MAIN_MENU -> "Standalone Client";
            case PAUSE_MENU -> "Runtime shell";
            case DEATH_SCREEN -> "Respawn";
            case WORLD_SELECT -> "Save slots";
            case CREATE_WORLD -> "World setup";
            case OPTIONS -> "ScreenCore settings";
            case CONTROLS -> "Input mapping";
            case VIDEO_SETTINGS -> "Renderer settings";
            case AUDIO_SETTINGS -> "Sound mix";
            case ACCESSIBILITY_SETTINGS -> "Readable feedback";
            case LANGUAGE_SETTINGS -> "Locale selection";
            case MODS -> "Loader registry";
            case RESOURCE_PACKS -> "Asset packs";
            case RESOURCE_PACK_DETAIL -> "Mounted pack detail";
            case INVENTORY -> "Player inventory";
            case CONTAINER -> "Container slots";
            case WORKBENCH -> "Crafting recipes";
            case MACHINE -> "Machine status";
            case TERMINAL -> "Field terminal";
            case DIAGNOSTICS -> "Runtime telemetry";
            case FATAL_ERROR -> "Standalone client halted safely";
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null ? "AdapterCore screen" : screen.source() + " screen";
            }
        };
    }

    private String screenDescription() {
        if (screenKind == EchoClientScreenKind.WORLD_SELECT) {
            if (!saveSlotError.isBlank()) {
                return "Save scan error: " + saveSlotError;
            }
            if (saveSlots.isEmpty()) {
                return "No manifests found yet; create a world to seed the save profile";
            }
            return saveSlots.size() + " save slot manifest(s) discovered";
        }
        if (screenKind == EchoClientScreenKind.RESOURCE_PACKS) {
            if (!resourcePackError.isBlank()) {
                return "Resource pack scan error: " + resourcePackError;
            }
            if (resourcePacks.isEmpty()) {
                return "No pack.mcmeta or assets/<namespace> resource roots found";
            }
            long textures = resourcePacks.stream().mapToLong(EchoClientResourcePackSummary::textureCount).sum();
            long animated = resourcePacks.stream()
                    .mapToLong(EchoClientResourcePackSummary::animatedTextureMetadataCount)
                    .sum();
            long models = resourcePacks.stream().mapToLong(EchoClientResourcePackSummary::modelCount).sum();
            long sounds = resourcePacks.stream().mapToLong(EchoClientResourcePackSummary::soundEventCount).sum();
            return resourcePacks.size() + " pack root(s), " + textures + " texture(s), "
                    + animated + " animated metadata file(s), "
                    + models + " model(s), " + sounds + " sound event(s)";
        }
        if (screenKind == EchoClientScreenKind.RESOURCE_PACK_DETAIL) {
            EchoClientResourcePackSummary pack = selectedResourcePack();
            return pack == null
                    ? "Selected resource pack is no longer mounted"
                    : pack.detailLabel();
        }
        return switch (screenKind) {
            case MAIN_MENU -> "AdapterCore-ready runtime shell";
            case PAUSE_MENU -> "World simulation is paused";
            case DEATH_SCREEN -> "Respawn restores health and returns to the crash site";
            case WORLD_SELECT -> "Named saves, backups, and migrations mount here";
            case CREATE_WORLD -> "Create '" + worldName() + "' with seed " + worldSeed();
            case OPTIONS -> "Client, controls, video, audio, and packs mount here";
            case CONTROLS -> "Keyboard and mouse bindings will mirror Minecraft-style controls";
            case VIDEO_SETTINGS -> "Chunk renderer, UI scale, fullscreen, VSync, and view controls";
            case AUDIO_SETTINGS -> "Master, music, ambience, blocks, mobs, and UI sound channels";
            case ACCESSIBILITY_SETTINGS -> "Subtitles, contrast, and reduced motion preferences";
            case LANGUAGE_SETTINGS -> "Active locale " + languageCode + " with resource-pack lang fallback";
            case MODS -> "ECHO Native Loader and AdapterCore mod inventory surface: "
                    + screenCatalog.modSummary() + " | " + runtimeContent.summaryLabel();
            case RESOURCE_PACKS -> "Minecraft resource packs and runtime texture atlas management";
            case RESOURCE_PACK_DETAIL -> "Mounted Minecraft resource pack namespace, model, lang, and sound inventory";
            case INVENTORY -> "Player hotbar and carry slots";
            case CONTAINER -> "Container UI route and slot commands for AdapterCore attachments";
            case WORKBENCH -> workbenchRecipes.size() + " item-runtime recipe(s) loaded from data packs";
            case MACHINE -> screenCatalog.domainCount(EchoAdapterCoreDomain.MACHINES)
                    + " AdapterCore machine contract(s), "
                    + screenCatalog.domainCount(EchoAdapterCoreDomain.POWER)
                    + " power contract(s), "
                    + techSurface.machineSummary()
                    + ", and "
                    + runtimeDomainCount("machines")
                    + " native runtime machine import(s)";
            case TERMINAL -> "Field terminal route with "
                    + screenCatalog.domainCount(EchoAdapterCoreDomain.UI_SCREENS)
                    + " AdapterCore UI screen(s), "
                    + screenCatalog.domainCount(EchoAdapterCoreDomain.COMMANDS)
                    + " command contract(s), "
                    + techSurface.terminalSummary()
                    + ", and "
                    + runtimeRowsContaining("terminal")
                    + " native runtime terminal import(s)";
            case DIAGNOSTICS -> "Runtime state, save profile, resource packs, and ScreenCore route inspection: "
                    + screenCatalog.diagnosticsSummary();
            case FATAL_ERROR -> fatalErrorSummary;
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null
                        ? "A registered AdapterCore screen route was not found"
                        : screen.tooltip();
            }
        };
    }

    private String route() {
        return switch (screenKind) {
            case MAIN_MENU -> "screencore.main_menu";
            case PAUSE_MENU -> "screencore.pause_flow";
            case DEATH_SCREEN -> "screencore.death_screen";
            case WORLD_SELECT -> "screencore.world_select";
            case CREATE_WORLD -> "screencore.create_world";
            case OPTIONS -> "screencore.settings";
            case CONTROLS -> "screencore.settings.controls";
            case VIDEO_SETTINGS -> "screencore.settings.video";
            case AUDIO_SETTINGS -> "screencore.settings.audio";
            case ACCESSIBILITY_SETTINGS -> "screencore.settings.accessibility";
            case LANGUAGE_SETTINGS -> "screencore.settings.language";
            case MODS -> "screencore.mods";
            case RESOURCE_PACKS -> "screencore.resource_packs";
            case RESOURCE_PACK_DETAIL -> "screencore.resource_pack_detail";
            case INVENTORY -> "screencore.inventory";
            case CONTAINER -> "screencore.container";
            case WORKBENCH -> "screencore.workbench";
            case MACHINE -> "screencore.machine";
            case TERMINAL -> "screencore.terminal";
            case DIAGNOSTICS -> "screencore.diagnostics";
            case FATAL_ERROR -> "screencore.fatal_error";
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null ? "screencore.registered_screen" : screen.route().route();
            }
        };
    }

    private String focusPath() {
        return switch (screenKind) {
            case MAIN_MENU -> "main_menu.primary";
            case PAUSE_MENU -> "pause.resume";
            case DEATH_SCREEN -> "death.respawn";
            case WORLD_SELECT -> "world_select.primary";
            case CREATE_WORLD -> "create_world.primary";
            case OPTIONS -> "settings.controls";
            case CONTROLS -> "settings.controls.back";
            case VIDEO_SETTINGS -> "settings.video.back";
            case AUDIO_SETTINGS -> "settings.audio.back";
            case ACCESSIBILITY_SETTINGS -> "settings.accessibility.subtitles";
            case LANGUAGE_SETTINGS -> "settings.language.locale";
            case MODS -> "mods.back";
            case RESOURCE_PACKS -> "resource_packs.back";
            case RESOURCE_PACK_DETAIL -> "resource_pack_detail.back";
            case INVENTORY -> "inventory.slots";
            case CONTAINER -> "container.slots";
            case WORKBENCH -> "workbench.recipes";
            case MACHINE -> "machine.status";
            case TERMINAL -> "terminal.primary";
            case DIAGNOSTICS -> "diagnostics.back";
            case FATAL_ERROR -> "fatal_error.export_support_bundle";
            case REGISTERED_SCREEN -> {
                EchoClientScreenCatalogEntry screen = registeredScreen();
                yield screen == null ? "registered_screen.back" : screen.route().focusPath();
            }
        };
    }

    private static String fatalErrorSummary(Throwable failure) {
        if (failure == null) {
            return "Unknown runtime failure";
        }
        String type = failure.getClass().getSimpleName();
        String message = cleanFatalLine(failure.getMessage());
        if (message.isBlank()) {
            return type;
        }
        return type + ": " + message;
    }

    private static String fatalErrorDetail(Throwable failure) {
        if (failure == null) {
            return "No throwable was provided.";
        }
        StringBuilder detail = new StringBuilder(fatalErrorSummary(failure));
        Throwable cause = failure.getCause();
        if (cause != null) {
            detail.append(" | Cause: ").append(fatalErrorSummary(cause));
        }
        StackTraceElement[] stack = failure.getStackTrace();
        if (stack.length > 0) {
            detail.append(" | At ").append(stack[0]);
        }
        return limitFatalDetail(detail.toString());
    }

    private static String cleanFatalLine(String value) {
        return value == null ? "" : value.trim().replace('\r', ' ').replace('\n', ' ');
    }

    private static String limitFatalDetail(String value) {
        String safe = value == null ? "" : value.trim();
        return safe.length() <= 360 ? safe : safe.substring(0, 357) + "...";
    }

    private enum EditableTextField {
        NONE,
        WORLD_NAME,
        WORLD_SEED,
        SAVE_SLOT_RENAME;

        String value(EchoClientScreenController screens) {
            return switch (this) {
                case WORLD_NAME -> screens.worldName;
                case WORLD_SEED -> screens.worldSeed;
                case SAVE_SLOT_RENAME -> screens.saveSlotRenameText;
                case NONE -> "";
            };
        }

        String toastPrefix() {
            return switch (this) {
                case WORLD_NAME -> "World name";
                case WORLD_SEED -> "Seed";
                case SAVE_SLOT_RENAME -> "Save name";
                case NONE -> "Text";
            };
        }
    }
}
