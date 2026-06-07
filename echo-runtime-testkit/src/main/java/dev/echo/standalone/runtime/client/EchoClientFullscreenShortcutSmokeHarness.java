package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EchoClientFullscreenShortcutSmokeHarness {
    private EchoClientFullscreenShortcutSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireShortcutTogglesAndPersists();
        System.out.println("client fullscreen shortcut smoke PASS key=F11 toggles=persisted");
    }

    private static void requireShortcutTogglesAndPersists() throws IOException {
        Path root = Files.createTempDirectory("echo-client-fullscreen-shortcut-smoke");
        EchoClientSettingsStore store = new EchoClientSettingsStore(root.resolve("options.properties"));
        EchoClientScreenController screens = new EchoClientScreenController(EchoClientSettings.defaults());
        EchoClientFullscreenShortcutRuntimeController shortcut =
                new EchoClientFullscreenShortcutRuntimeController(screens);
        RecordingSettingsHost host = new RecordingSettingsHost();
        EchoClientSettingsController settings = new EchoClientSettingsController(screens, store, host);

        OneShotInput input = new OneShotInput();
        require(!screens.clientSettings().fullscreen(),
                "Fullscreen shortcut smoke should start windowed by default");
        input.fire();
        require(shortcut.update(input),
                "Fullscreen shortcut runtime should consume the configured toggle key");
        require(screens.clientSettings().fullscreen(),
                "Fullscreen shortcut should flip ScreenCore fullscreen preference on");
        require(screens.snapshot(false).toast().message().equals("Fullscreen ON"),
                "Fullscreen shortcut should publish an ON toast");
        settings.applyAndPersist();
        require(host.fullscreen && host.vSync,
                "Settings controller should apply fullscreen shortcut state to the window host");
        require(store.load().fullscreen(),
                "Settings store should persist fullscreen shortcut state");

        require(!shortcut.update(input),
                "Fullscreen shortcut should be single-shot without a new key press");
        input.fire();
        require(shortcut.update(input),
                "Fullscreen shortcut should handle a second key press");
        require(!screens.clientSettings().fullscreen(),
                "Second fullscreen shortcut press should return to windowed preference");
        require(screens.snapshot(false).toast().message().equals("Fullscreen OFF"),
                "Fullscreen shortcut should publish an OFF toast");
        settings.applyAndPersist();
        require(!host.fullscreen,
                "Settings controller should restore windowed state after the second shortcut");
        require(!store.load().fullscreen(),
                "Settings store should persist the restored windowed state");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class OneShotInput implements EchoClientFullscreenShortcutRuntimeController.InputGate {
        private boolean pending;

        private void fire() {
            pending = true;
        }

        @Override
        public boolean consumeToggleFullscreen() {
            boolean consumed = pending;
            pending = false;
            return consumed;
        }
    }

    private static final class RecordingSettingsHost implements EchoClientSettingsController.Host {
        private boolean fullscreen;
        private boolean vSync;

        @Override
        public void applyInputSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyAudioSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
        }

        @Override
        public void applyWindowSettings(boolean fullscreen, boolean vSync) {
            this.fullscreen = fullscreen;
            this.vSync = vSync;
        }

        @Override
        public void settingsSaveFailed(Path path, String error) {
            throw new AssertionError("Settings save failed at " + path + ": " + error);
        }
    }
}
