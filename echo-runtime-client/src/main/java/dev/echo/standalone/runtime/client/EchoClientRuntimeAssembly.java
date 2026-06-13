package dev.echo.standalone.runtime.client;

final class EchoClientRuntimeAssembly {
    private final EchoClientWorldTemplate worldTemplate;
    private final EchoGlfwWindow window;
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final EchoClientSettingsController settingsController;
    private final EchoClientSettingsRuntimeController settingsRuntime;
    private final EchoClientWorldSessionController worldSessions;
    private final EchoClientGameplayRuntimeController gameplayRuntime;
    private final EchoClientSlotGridController slotGrid;
    private final EchoClientCommandController commands;
    private final EchoClientParticleRuntimeController particleRuntime;
    private final EchoClientMusicRuntimeController musicRuntime;
    private final EchoClientFullscreenShortcutRuntimeController fullscreenShortcutRuntime;
    private final EchoClientRenderRuntimeController renderRuntime;
    private final EchoClientShellRuntimeController shellRuntime;
    private final EchoClientScreenRuntimeController screenRuntime;
    private final EchoClientSlotGridRuntimeController slotGridRuntime;
    private final EchoClientScreenshotRuntimeController screenshotRuntime;
    private final EchoClientFocusLossRuntimeController focusLossRuntime;
    private final EchoClientEngineRuntimeBridge runtimeBridge;

    private EchoClientInput input;
    private EchoClientRenderer renderer;
    private EchoClientHud hud;
    private EchoClientAudio audio;

    private EchoClientRuntimeAssembly(
            EchoClientWorldTemplate worldTemplate,
            EchoGlfwWindow window,
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens,
            EchoClientSettingsController settingsController,
            EchoClientSettingsRuntimeController settingsRuntime,
            EchoClientWorldSessionController worldSessions,
            EchoClientGameplayRuntimeController gameplayRuntime,
            EchoClientSlotGridController slotGrid,
            EchoClientCommandController commands,
            EchoClientParticleRuntimeController particleRuntime,
            EchoClientMusicRuntimeController musicRuntime,
            EchoClientFullscreenShortcutRuntimeController fullscreenShortcutRuntime,
            EchoClientRenderRuntimeController renderRuntime,
            EchoClientShellRuntimeController shellRuntime,
            EchoClientScreenRuntimeController screenRuntime,
            EchoClientSlotGridRuntimeController slotGridRuntime,
            EchoClientScreenshotRuntimeController screenshotRuntime,
            EchoClientFocusLossRuntimeController focusLossRuntime,
            EchoClientEngineRuntimeBridge runtimeBridge
    ) {
        this.worldTemplate = worldTemplate;
        this.window = window;
        this.runtimeServices = runtimeServices;
        this.screens = screens;
        this.settingsController = settingsController;
        this.settingsRuntime = settingsRuntime;
        this.worldSessions = worldSessions;
        this.gameplayRuntime = gameplayRuntime;
        this.slotGrid = slotGrid;
        this.commands = commands;
        this.particleRuntime = particleRuntime;
        this.musicRuntime = musicRuntime;
        this.fullscreenShortcutRuntime = fullscreenShortcutRuntime;
        this.renderRuntime = renderRuntime;
        this.shellRuntime = shellRuntime;
        this.screenRuntime = screenRuntime;
        this.slotGridRuntime = slotGridRuntime;
        this.screenshotRuntime = screenshotRuntime;
        this.focusLossRuntime = focusLossRuntime;
        this.runtimeBridge = runtimeBridge;
    }

    static EchoClientRuntimeAssembly create(int initialWidth, int initialHeight) {
        return create(initialWidth, initialHeight, EchoClientWorldTemplates.defaultTemplate());
    }

    static EchoClientRuntimeAssembly create(int initialWidth, int initialHeight, EchoClientWorldTemplate worldTemplate) {
        EchoClientWorldTemplate safeTemplate = worldTemplate == null
                ? EchoClientWorldTemplates.defaultTemplate()
                : worldTemplate;
        EchoClientWorldPresentation presentation = safeTemplate.presentation();
        EchoGlfwWindow window = new EchoGlfwWindow(presentation.windowTitle(), initialWidth, initialHeight);
        EchoClientRuntimeServices runtimeServices = EchoClientRuntimeServices.forTemplate(safeTemplate);
        EchoClientSettingsStore settingsStore = EchoClientSettingsStore.openDefault(presentation);
        EchoClientScreenController screens =
                new EchoClientScreenController(settingsStore.load(), presentation, safeTemplate.displayName());
        EchoClientWorldSessionController worldSessions =
                new EchoClientWorldSessionController(runtimeServices, screens);
        EchoClientGameplayRuntimeController gameplayRuntime =
                new EchoClientGameplayRuntimeController(runtimeServices, screens, worldSessions);
        EchoClientParticleRuntimeController particleRuntime =
                new EchoClientParticleRuntimeController(runtimeServices);
        gameplayRuntime.attachParticles(particleRuntime);
        EchoClientMusicRuntimeController musicRuntime =
                new EchoClientMusicRuntimeController(screens);
        EchoClientFullscreenShortcutRuntimeController fullscreenShortcutRuntime =
                new EchoClientFullscreenShortcutRuntimeController(screens);
        EchoClientSlotGridController slotGrid = new EchoClientSlotGridController(runtimeServices, screens);
        EchoClientRenderRuntimeController renderRuntime = new EchoClientRenderRuntimeController(
                window,
                runtimeServices,
                screens,
                gameplayRuntime,
                slotGrid
        );
        renderRuntime.attachParticles(particleRuntime);
        EchoClientSettingsRuntimeController settingsRuntime =
                new EchoClientSettingsRuntimeController(screens, renderRuntime, window);
        EchoClientSettingsController settingsController =
                new EchoClientSettingsController(screens, settingsStore, settingsRuntime);
        EchoClientShellRuntimeController shellRuntime =
                new EchoClientShellRuntimeController(screens, worldSessions);
        EchoClientScreenRuntimeController screenRuntime =
                new EchoClientScreenRuntimeController(runtimeServices, screens, settingsController, renderRuntime);
        EchoClientScreenshotRuntimeController screenshotRuntime =
                new EchoClientScreenshotRuntimeController(screens, window);
        EchoClientFocusLossRuntimeController focusLossRuntime =
                new EchoClientFocusLossRuntimeController();
        EchoClientSlotGridRuntimeController slotGridRuntime = new EchoClientSlotGridRuntimeController(
                runtimeServices,
                screens,
                slotGrid,
                gameplayRuntime
        );
        EchoClientEngineRuntimeBridge runtimeBridge = new EchoClientEngineRuntimeBridge(
                renderRuntime,
                shellRuntime,
                slotGridRuntime,
                window
        );
        EchoClientCommandController commands = new EchoClientCommandController(
                runtimeServices,
                screens,
                worldSessions,
                gameplayRuntime,
                runtimeBridge.commandHost()
        );
        return new EchoClientRuntimeAssembly(
                worldTemplate,
                window,
                runtimeServices,
                screens,
                settingsController,
                settingsRuntime,
                worldSessions,
                gameplayRuntime,
                slotGrid,
                commands,
                particleRuntime,
                musicRuntime,
                fullscreenShortcutRuntime,
                renderRuntime,
                shellRuntime,
                screenRuntime,
                slotGridRuntime,
                screenshotRuntime,
                focusLossRuntime,
                runtimeBridge
        );
    }

    void initializeNativeResources() {
        window.create();
        input = new EchoClientInput(window.handle());
        runtimeBridge.attachInput(input);
        settingsRuntime.attachInput(input);
        renderer = new EchoClientRenderer();
        hud = new EchoClientHud();
        audio = new EchoClientAudio(worldTemplate.audioProfile());
        audio.init();
        settingsRuntime.attachAudio(audio);
        screenRuntime.attachAudio(audio);
        musicRuntime.attachAudio(audio);
        settingsRuntime.attachLanguage(runtimeServices.language());
        renderRuntime.attach(renderer, hud, audio);
        renderRuntime.reloadMinecraftAssets(false);
        renderer.resize(window.width(), window.height());
        runtimeServices.setAudio(audio);
        screenRuntime.showInitialMainMenu();
    }

    void resizeRendererIfNeeded() {
        if (renderer != null && window.consumeFramebufferResized()) {
            renderer.resize(window.width(), window.height());
        }
    }

    void renderFrame(int fps, long frames, EchoClientFramePacingSnapshot framePacing) {
        musicRuntime.update(frames);
        renderRuntime.render(fps, frames, input, framePacing);
    }

    void showFatalError(Throwable failure) {
        if (input != null) {
            input.setCursorLocked(false);
            input.clearGameplayTriggers();
        }
        screens.showFatalError(failure);
    }

    void update(double dt) {
        screens.tick(dt);
        handleWindowFocusLossIfNeeded();
        fullscreenShortcutRuntime.update(input::consumeToggleFullscreen);
        settingsController.applyAndPersist();
        screenshotRuntime.updateInput(runtimeBridge.screenshotInputGate());

        if (shellRuntime.updateBlockingFlow(
                runtimeBridge.shellInputGate(),
                dt,
                runtimeBridge.shellRuntimeHost()
        )) {
            return;
        }

        if (slotGridRuntime.updateIfOpen(
                runtimeBridge.slotGridInputGate(),
                renderRuntime.viewport(),
                dt,
                runtimeBridge.gameplayRuntimeHost()
        )) {
            return;
        }

        if (screens.state() != EchoClientGameState.IN_GAME) {
            updateScreen();
            return;
        }

        updateGameplay(dt);
    }

    private void updateScreen() {
        commands.execute(screenRuntime.updateScreen(input, renderRuntime.viewport()));
    }

    private void updateGameplay(double dt) {
        gameplayRuntime.updateActiveGameplay(input, dt, runtimeBridge.gameplayRuntimeHost());
    }

    private void handleWindowFocusLossIfNeeded() {
        if (input == null || !window.consumeFocusLost()) {
            return;
        }
        focusLossRuntime.handleFocusLost(screens, input::releaseForFocusLoss, runtimeServices.hasActiveWorld());
    }

    void close() {
        if (renderer != null) {
            renderer.delete();
        }
        if (hud != null) {
            hud.delete();
        }
        if (audio != null) {
            musicRuntime.stop(0L);
            audio.close();
        }
        window.close();
    }

    EchoGlfwWindow window() {
        return window;
    }

    EchoClientScreenController screens() {
        return screens;
    }

    EchoClientRuntimeServices runtimeServices() {
        return runtimeServices;
    }

    EchoClientSettingsController settingsController() {
        return settingsController;
    }

    EchoClientGameplayRuntimeController gameplayRuntime() {
        return gameplayRuntime;
    }

    EchoClientCommandController commands() {
        return commands;
    }

    EchoClientParticleRuntimeController particleRuntime() {
        return particleRuntime;
    }

    EchoClientMusicRuntimeController musicRuntime() {
        return musicRuntime;
    }

    EchoClientFullscreenShortcutRuntimeController fullscreenShortcutRuntime() {
        return fullscreenShortcutRuntime;
    }

    EchoClientRenderRuntimeController renderRuntime() {
        return renderRuntime;
    }

    EchoClientShellRuntimeController shellRuntime() {
        return shellRuntime;
    }

    EchoClientScreenRuntimeController screenRuntime() {
        return screenRuntime;
    }

    EchoClientSlotGridRuntimeController slotGridRuntime() {
        return slotGridRuntime;
    }

    EchoClientScreenshotRuntimeController screenshotRuntime() {
        return screenshotRuntime;
    }

    EchoClientFocusLossRuntimeController focusLossRuntime() {
        return focusLossRuntime;
    }

    EchoClientEngineRuntimeBridge runtimeBridge() {
        return runtimeBridge;
    }

    EchoClientInput input() {
        return input;
    }
}
