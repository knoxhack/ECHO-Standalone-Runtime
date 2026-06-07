package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioVolumeProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientSettingsSmokeHarness {
    private static final int GLFW_TRUE = 1;
    private static final int GLFW_FALSE = 0;

    private EchoClientSettingsSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSettings defaults = screens.clientSettings();
        require(defaults.mouseSensitivityPercent() == 50,
                "Default ScreenCore mouse sensitivity should match Minecraft-like midpoint");
        require(defaults.fovDegrees() == EchoClientSettings.DEFAULT_FOV_DEGREES,
                "Default ScreenCore FOV should match the runtime camera default");
        require(defaults.chunkViewDistance() == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE,
                "Default ScreenCore chunk view should match the runtime streaming radius");
        require(defaults.languageCode().equals(EchoClientSettings.DEFAULT_LANGUAGE_CODE),
                "Default ScreenCore language should use the resource-pack en_us locale");
        require(defaults.subtitles(),
                "Default ScreenCore accessibility should keep subtitles enabled");

        screens.showMainMenu(false);
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_OPTIONS, false),
                "Options should open through the ScreenCore controller");
        EchoClientScreenSnapshot options = screens.snapshot(false);
        require(options.options().stream().anyMatch(option ->
                        option.command() == EchoClientScreenCommand.OPEN_ACCESSIBILITY_SETTINGS),
                "Options should expose the Accessibility ScreenCore route");
        require(options.options().stream().anyMatch(option ->
                        option.command() == EchoClientScreenCommand.OPEN_LANGUAGE_SETTINGS),
                "Options should expose the Language ScreenCore route");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open through the ScreenCore controller");
        EchoClientScreenSnapshot video = screens.snapshot(false);
        require(video.kind() == EchoClientScreenKind.VIDEO_SETTINGS,
                "Video settings route should be active");
        require(video.options().get(video.selectedIndex()).label().equals("FOV"),
                "FOV should be the primary adjustable video setting");
        screens.editSelectedControl(1, false);
        require(screens.clientSettings().fovDegrees() == EchoClientSettings.DEFAULT_FOV_DEGREES + 5,
                "Editing the ScreenCore FOV slider should update client settings");
        require(screens.snapshot(false).options().stream()
                        .anyMatch(option -> option.label().equals("FOV") && option.valueText().equals("75")),
                "FOV slider text should reflect the updated runtime value");
        require(screens.snapshot(false).options().stream()
                        .anyMatch(option -> option.label().equals("Chunk View") && option.valueText().equals("3")),
                "Chunk view slider should be exposed on the ScreenCore video settings route");
        require(screens.snapshot(false).options().stream()
                        .anyMatch(option -> option.label().equals("VSync") && option.valueText().equals("ON")),
                "VSync toggle should be exposed on the ScreenCore video settings route");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTROLS, false),
                "Controls should open through the ScreenCore controller");
        EchoClientScreenSnapshot controls = screens.snapshot(false);
        require(controls.kind() == EchoClientScreenKind.CONTROLS,
                "Controls route should be active");
        require(controls.options().get(controls.selectedIndex()).label().equals("Mouse Sensitivity"),
                "Mouse sensitivity should be the primary adjustable control setting");
        require(controls.options().stream().anyMatch(option -> option.label().equals("Move Forward: W")),
                "Controls should display the runtime move-forward key binding");
        require(controls.options().stream().anyMatch(option ->
                        option.command() == EchoClientScreenCommand.RESET_KEY_BINDINGS),
                "Controls should expose a keybinding reset action");
        screens.editSelectedControl(1, false);
        require(screens.clientSettings().mouseSensitivityPercent() == 55,
                "Editing the ScreenCore mouse slider should update client settings");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_ACCESSIBILITY_SETTINGS, false),
                "Accessibility settings should open through the ScreenCore controller");
        EchoClientScreenSnapshot accessibility = screens.snapshot(false);
        require(accessibility.kind() == EchoClientScreenKind.ACCESSIBILITY_SETTINGS,
                "Accessibility route should be active");
        require(accessibility.options().get(accessibility.selectedIndex()).label().equals("Subtitles"),
                "Subtitles should be the primary accessibility toggle");
        screens.editSelectedControl(1, false);
        require(!screens.clientSettings().subtitles(),
                "Editing subtitles should update client accessibility settings");
        screens.moveSelection(1, false, 720);
        screens.editSelectedControl(1, false);
        require(screens.clientSettings().highContrastUi(),
                "Editing high contrast should update client accessibility settings");
        screens.moveSelection(1, false, 720);
        screens.editSelectedControl(1, false);
        require(screens.clientSettings().reducedMotion(),
                "Editing reduced motion should update client accessibility settings");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_LANGUAGE_SETTINGS, false),
                "Language settings should open through the ScreenCore controller");
        EchoClientScreenSnapshot language = screens.snapshot(false);
        require(language.kind() == EchoClientScreenKind.LANGUAGE_SETTINGS,
                "Language route should be active");
        require(language.options().get(language.selectedIndex()).command() == EchoClientScreenCommand.CYCLE_LANGUAGE,
                "Language route should select the locale cycle action");
        screens.activateSelection(false);
        require(screens.clientSettings().languageCode().equals("en_gb"),
                "Activating the Language row should cycle the persisted locale");

        require(close(EchoClientInput.mouseLookSensitivity(50), 0.15D),
                "Default mouse sensitivity percent should preserve the existing look speed");
        require(EchoClientInput.mouseLookSensitivity(55) > EchoClientInput.mouseLookSensitivity(50),
                "Higher mouse sensitivity should increase look speed");
        require(EchoClientInput.yawDeltaFromMouseDelta(10.0D, 55)
                        > EchoClientInput.yawDeltaFromMouseDelta(10.0D, 50),
                "Higher sensitivity should produce a stronger right-turn yaw delta");
        require(EchoClientInput.pitchDeltaFromMouseDelta(-10.0D, 55)
                        > EchoClientInput.pitchDeltaFromMouseDelta(-10.0D, 50),
                "Higher sensitivity should produce a stronger upward pitch delta");
        require(EchoClientInput.rawMouseInputMode(true, true, true) == GLFW_TRUE,
                "Raw mouse input should enable GLFW raw motion only while locked and supported");
        require(EchoClientInput.rawMouseInputMode(false, true, true) == GLFW_FALSE,
                "Raw mouse input should stay disabled while cursor is unlocked");
        require(EchoClientInput.rawMouseInputMode(true, false, true) == GLFW_FALSE,
                "Raw mouse input preference should disable GLFW raw motion");
        require(EchoClientInput.rawMouseInputMode(true, true, false) == GLFW_FALSE,
                "Unsupported platforms should not enable GLFW raw motion");
        requireAudioSlidersMapToMixerProfile();
        requireUiScaleMapping();
        requirePointerDrivenSliderMapping();

        EchoVoxelPlayerState state = new EchoVoxelPlayerState(
                1.0D,
                2.0D,
                3.0D,
                0.0D,
                15.0D,
                -5.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        );
        require(state.camera(95.0D).fovDegrees() == 95.0D,
                "Player camera should accept the live ScreenCore FOV");
        requireSettingsPersistAcrossControllers();

        System.out.println("client settings smoke PASS fov="
                + screens.clientSettings().fovDegrees()
                + " sensitivity="
                + screens.clientSettings().mouseSensitivityPercent());
    }

    private static void requireSettingsPersistAcrossControllers() throws IOException {
        Path root = Files.createTempDirectory("echo-client-settings-smoke");
        EchoClientSettingsStore store = new EchoClientSettingsStore(root.resolve("options.properties"));
        require(store.load().fovDegrees() == EchoClientSettings.DEFAULT_FOV_DEGREES,
                "Missing options file should load default client settings");

        EchoClientScreenController first = new EchoClientScreenController(store.load());
        require(first.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open for persisted settings smoke");
        first.editSelectedControl(1, false);
        require(first.consumeClientSettingsDirty(),
                "Editing FOV should mark client settings dirty");
        store.save(first.clientSettings());
        require(store.lastError().isBlank(),
                "Saving client settings should not report an error");
        require(Files.isRegularFile(store.path()),
                "Saving client settings should create an options file");

        EchoClientScreenController restored = new EchoClientScreenController(store.load());
        require(restored.clientSettings().fovDegrees() == EchoClientSettings.DEFAULT_FOV_DEGREES + 5,
                "Reloaded ScreenCore controller should preserve persisted FOV");
        require(restored.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open for fullscreen settings smoke");
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().uiScalePercent() == 55,
                "UI scale slider should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing UI scale should mark client settings dirty");
        store.save(restored.clientSettings());
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().fullscreen(),
                "Fullscreen toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing fullscreen should mark client settings dirty");
        store.save(restored.clientSettings());
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(!restored.clientSettings().vSync(),
                "VSync toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing VSync should mark client settings dirty");
        store.save(restored.clientSettings());
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().chunkViewDistance() == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE + 1,
                "Chunk view slider should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing chunk view should mark client settings dirty");
        store.save(restored.clientSettings());

        require(restored.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTROLS, false),
                "Controls should open for persisted settings smoke");
        restored.moveSelection(2, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().rawMouseInput() == !first.clientSettings().rawMouseInput(),
                "Raw mouse input toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing raw mouse input should mark client settings dirty");
        store.save(restored.clientSettings());

        EchoClientScreenController finalLoad = new EchoClientScreenController(store.load());
        require(finalLoad.clientSettings().fovDegrees() == EchoClientSettings.DEFAULT_FOV_DEGREES + 5,
                "Persisted reload should keep the edited FOV");
        require(finalLoad.clientSettings().fullscreen(),
                "Persisted reload should keep the edited fullscreen preference");
        require(!finalLoad.clientSettings().vSync(),
                "Persisted reload should keep the edited VSync preference");
        require(finalLoad.clientSettings().uiScalePercent() == 55,
                "Persisted reload should keep the edited UI scale preference");
        require(finalLoad.clientSettings().chunkViewDistance() == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE + 1,
                "Persisted reload should keep the edited chunk view distance");
        require(!finalLoad.clientSettings().rawMouseInput(),
                "Persisted reload should keep the edited raw mouse input preference");

        require(restored.executeNavigationCommand(EchoClientScreenCommand.OPEN_ACCESSIBILITY_SETTINGS, false),
                "Accessibility settings should open for persisted settings smoke");
        restored.editSelectedControl(1, false);
        require(!restored.clientSettings().subtitles(),
                "Subtitles toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing subtitles should mark client settings dirty");
        store.save(restored.clientSettings());
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().highContrastUi(),
                "High contrast toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing high contrast should mark client settings dirty");
        store.save(restored.clientSettings());
        restored.moveSelection(1, false, 720);
        restored.editSelectedControl(1, false);
        require(restored.clientSettings().reducedMotion(),
                "Reduced motion toggle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing reduced motion should mark client settings dirty");
        store.save(restored.clientSettings());

        require(restored.executeNavigationCommand(EchoClientScreenCommand.OPEN_LANGUAGE_SETTINGS, false),
                "Language settings should open for persisted settings smoke");
        restored.activateSelection(false);
        require(restored.clientSettings().languageCode().equals("en_gb"),
                "Language cycle should update persisted client settings");
        require(restored.consumeClientSettingsDirty(),
                "Editing language should mark client settings dirty");
        store.save(restored.clientSettings());

        EchoClientScreenController localizedLoad = new EchoClientScreenController(store.load());
        require(!localizedLoad.clientSettings().subtitles(),
                "Persisted reload should keep subtitles disabled");
        require(localizedLoad.clientSettings().highContrastUi(),
                "Persisted reload should keep high contrast enabled");
        require(localizedLoad.clientSettings().reducedMotion(),
                "Persisted reload should keep reduced motion enabled");
        require(localizedLoad.clientSettings().languageCode().equals("en_gb"),
                "Persisted reload should keep the edited language locale");
        EchoGlfwWindowBounds fallback = EchoGlfwWindow.restoredWindowedBounds(11, 22, 0, -1, 1280, 720);
        require(fallback.x() == 11 && fallback.y() == 22,
                "Restored window bounds should preserve the saved position");
        require(fallback.width() == 1280 && fallback.height() == 720,
                "Restored window bounds should use safe fallback dimensions");
    }

    private static void requireAudioSlidersMapToMixerProfile() {
        EchoClientSettings settings = new EchoClientSettings(
                50,
                true,
                EchoClientSettings.DEFAULT_FOV_DEGREES,
                50,
                false,
                true,
                EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE,
                40,
                25,
                65,
                "en_us",
                true,
                false,
                false,
                EchoClientKeyBindings.defaults()
        );
        EchoAudioVolumeProfile profile = EchoClientAudio.volumeProfile(settings);
        require(close(profile.busVolumes().get(EchoAudioBus.MASTER), 0.40D),
                "Master volume slider should update the mixer master bus");
        require(close(profile.busVolumes().get(EchoAudioBus.MUSIC), 0.25D),
                "Music volume slider should update the mixer music bus");
        require(close(profile.busVolumes().get(EchoAudioBus.AMBIENCE), 0.65D),
                "Ambience volume slider should update the mixer ambience bus");
        require(close(profile.busVolumes().get(EchoAudioBus.SFX), 0.82D),
                "SFX bus should keep the Ashfall gameplay default until a dedicated slider exists");
        require(close(profile.gainFor(EchoAudioBus.MUSIC, 1.0D), 0.10D),
                "Music effective gain should combine master and music sliders");
    }

    private static void requireUiScaleMapping() {
        require(close(EchoClientUiScale.scaleFactor(50), 1.0D),
                "UI scale midpoint should preserve the existing HUD size");
        require(close(EchoClientUiScale.scaleFactor(0), 0.75D),
                "Minimum UI scale should shrink HUD elements");
        require(close(EchoClientUiScale.scaleFactor(100), 1.25D),
                "Maximum UI scale should enlarge HUD elements");

        EchoClientUiViewport defaultViewport = EchoClientUiScale.viewport(50, 1280, 720);
        require(defaultViewport.logicalWidth() == 1280 && defaultViewport.logicalHeight() == 720,
                "Default UI viewport should keep framebuffer dimensions as logical HUD coordinates");
        require(close(defaultViewport.logicalPointerX(225.0D), 225.0D)
                        && close(defaultViewport.logicalPointerY(112.5D), 112.5D),
                "Default UI viewport should preserve pointer coordinates");

        EchoClientUiViewport largeViewport = EchoClientUiScale.viewport(100, 1280, 720);
        require(largeViewport.logicalWidth() == 1024 && largeViewport.logicalHeight() == 576,
                "Large UI scale should reduce logical dimensions so elements render larger");
        require(close(largeViewport.logicalPointerX(250.0D), 200.0D),
                "Large UI scale should convert framebuffer pointer coordinates into logical hit-test coordinates");
    }

    private static void requirePointerDrivenSliderMapping() {
        EchoClientScreenController video = new EchoClientScreenController();
        video.showMainMenu(false);
        require(video.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open for pointer slider smoke");
        pointerSlider(video, 0, 0.0D);
        require(video.clientSettings().fovDegrees() == EchoClientSettings.MIN_FOV_DEGREES,
                "Pointer click at the left of the FOV track should set the minimum FOV");
        pointerSliderDrag(video, 0, 1.0D);
        require(video.clientSettings().fovDegrees() == EchoClientSettings.MAX_FOV_DEGREES,
                "Pointer drag at the right of the FOV track should set the maximum FOV");

        pointerSlider(video, 1, 0.25D);
        require(video.clientSettings().uiScalePercent() == 25,
                "Pointer click should map UI scale slider position to a percentage value");
        pointerSlider(video, 4, 1.0D);
        require(video.clientSettings().chunkViewDistance() == EchoClientSettings.MAX_CHUNK_VIEW_DISTANCE,
                "Pointer click should map chunk view slider position to the max chunk distance");

        EchoClientScreenController audio = new EchoClientScreenController();
        audio.showMainMenu(false);
        require(audio.executeNavigationCommand(EchoClientScreenCommand.OPEN_OPTIONS, false),
                "Options should open for pointer audio slider smoke");
        require(audio.executeNavigationCommand(EchoClientScreenCommand.OPEN_AUDIO_SETTINGS, false),
                "Audio settings should open for pointer audio slider smoke");
        pointerSlider(audio, 0, 0.35D);
        require(audio.clientSettings().masterVolumePercent() == 35,
                "Pointer click should map master volume slider position to a percentage value");
        pointerSliderDrag(audio, 1, 0.0D);
        require(audio.clientSettings().musicVolumePercent() == 0,
                "Pointer drag should map music volume slider position to zero");
        pointerSlider(audio, 2, 0.90D);
        require(audio.clientSettings().ambienceVolumePercent() == 90,
                "Pointer click should map ambience volume slider position to a percentage value");
    }

    private static void pointerSlider(EchoClientScreenController screens, int optionIndex, double percent) {
        pointerSlider(screens, optionIndex, percent, true, true);
    }

    private static void pointerSliderDrag(EchoClientScreenController screens, int optionIndex, double percent) {
        pointerSlider(screens, optionIndex, percent, false, true);
    }

    private static void pointerSlider(
            EchoClientScreenController screens,
            int optionIndex,
            double percent,
            boolean clicked,
            boolean primaryDown
    ) {
        int width = 1280;
        int height = 720;
        int visibleCount = EchoClientScreenController.menuVisibleCount(
                height,
                screens.snapshot(false).options().size()
        );
        int startY = EchoClientScreenController.menuStartY(height, visibleCount);
        int startX = (width - EchoClientScreenController.MENU_BUTTON_WIDTH) / 2;
        double pointerX = startX
                + EchoClientScreenController.MENU_SLIDER_TRACK_X_OFFSET
                + EchoClientScreenController.MENU_SLIDER_TRACK_WIDTH * percent;
        double pointerY = startY
                + optionIndex * (EchoClientScreenController.MENU_BUTTON_HEIGHT
                        + EchoClientScreenController.MENU_BUTTON_SPACING)
                + EchoClientScreenController.MENU_BUTTON_HEIGHT / 2.0D;
        screens.handlePointer(pointerX, pointerY, clicked, primaryDown, width, height, false);
    }

    private static boolean close(double actual, double expected) {
        return Math.abs(actual - expected) < 0.000_001D;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
