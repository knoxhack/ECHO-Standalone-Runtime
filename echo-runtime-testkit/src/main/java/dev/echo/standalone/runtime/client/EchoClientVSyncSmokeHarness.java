package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientVSyncSmokeHarness {
    private EchoClientVSyncSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireDefaultAndSwapInterval();
        requireEnginePacingPolicy();
        requireVideoSettingsToggleAndPersistence();
        requireRuntimeWindowTargetApplication();
        System.out.println("client vsync smoke PASS default=on toggled=off interval=1/0");
    }

    private static void requireDefaultAndSwapInterval() {
        EchoClientSettings defaults = EchoClientSettings.defaults();
        require(defaults.vSync(), "Default client settings should enable VSync");
        require(EchoGlfwWindow.swapIntervalForVSync(true) == 1,
                "Enabled VSync should request GLFW swap interval 1");
        require(EchoGlfwWindow.swapIntervalForVSync(false) == 0,
                "Disabled VSync should request GLFW swap interval 0");
    }

    private static void requireEnginePacingPolicy() {
        require(!EchoClientEngine.shouldManuallyPace(true),
                "The engine must not sleep after a VSync-blocked buffer swap");
        require(EchoClientEngine.shouldManuallyPace(false),
                "The engine should use its manual limiter when VSync is disabled");
    }

    private static void requireVideoSettingsToggleAndPersistence() throws IOException {
        Path root = Files.createTempDirectory("echo-client-vsync-smoke");
        EchoClientSettingsStore store = new EchoClientSettingsStore(root.resolve("options.properties"));
        EchoClientScreenController screens = new EchoClientScreenController(store.load());
        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open for VSync smoke");

        EchoClientScreenSnapshot video = screens.snapshot(false);
        require(video.options().stream().anyMatch(option ->
                        option.label().equals("VSync") && option.valueText().equals("ON")),
                "Video settings should expose VSync as an enabled default toggle");

        selectLabel(screens, "VSync");
        screens.editSelectedControl(1, false);
        require(!screens.clientSettings().vSync(),
                "Editing the VSync toggle should update client settings");
        require(screens.consumeClientSettingsDirty(),
                "Editing VSync should mark client settings dirty");
        store.save(screens.clientSettings());
        require(store.lastError().isBlank(), "Saving VSync settings should not report an error");
        require(!store.load().vSync(), "Persisted options should keep VSync disabled");

        EchoClientScreenController restored = new EchoClientScreenController(store.load());
        require(restored.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should reopen after VSync persistence");
        require(restored.snapshot(false).options().stream().anyMatch(option ->
                        option.label().equals("VSync") && option.valueText().equals("OFF")),
                "Reloaded Video settings should render the persisted VSync state");
    }

    private static void requireRuntimeWindowTargetApplication() {
        EchoClientScreenController screens = new EchoClientScreenController();
        RecordingWindowTarget window = new RecordingWindowTarget();
        EchoClientSettingsRuntimeController runtime = new EchoClientSettingsRuntimeController(
                screens,
                new NoopRenderTarget(),
                window
        );
        runtime.applyWindowSettings(true, false);
        require(window.fullscreen, "Runtime settings should apply fullscreen state to the window target");
        require(!window.vSync, "Runtime settings should apply disabled VSync to the window target");
        runtime.applyWindowSettings(false, true);
        require(!window.fullscreen, "Runtime settings should restore windowed state on the window target");
        require(window.vSync, "Runtime settings should re-enable VSync on the window target");
    }

    private static void selectLabel(EchoClientScreenController screens, String label) {
        for (int attempt = 0; attempt < 64; attempt++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(false);
            int selected = snapshot.selectedIndex();
            if (selected >= 0
                    && selected < snapshot.options().size()
                    && snapshot.options().get(selected).label().equals(label)) {
                return;
            }
            screens.moveSelection(1, false, 720);
        }
        throw new AssertionError("Option not found: " + label);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingWindowTarget implements EchoClientSettingsRuntimeController.WindowTarget {
        private boolean fullscreen;
        private boolean vSync = true;

        @Override
        public void setFullscreen(boolean fullscreen) {
            this.fullscreen = fullscreen;
        }

        @Override
        public void setVSync(boolean vSync) {
            this.vSync = vSync;
        }
    }

    private static final class NoopRenderTarget implements EchoClientSettingsRuntimeController.RenderTarget {
        @Override
        public void setChunkViewDistance(int chunkViewDistance) {
        }

        @Override
        public void refreshWorldStreamingAndMeshes() {
        }
    }
}
