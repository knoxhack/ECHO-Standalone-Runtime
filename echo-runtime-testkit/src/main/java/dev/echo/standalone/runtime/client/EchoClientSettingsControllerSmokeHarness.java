package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientSettingsControllerSmokeHarness {
    private EchoClientSettingsControllerSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("build", "tmp", "client-settings-controller-smoke").toAbsolutePath();
        deleteRecursively(root);

        EchoClientSettingsStore store = new EchoClientSettingsStore(root.resolve("options.properties"));
        EchoClientScreenController screens = new EchoClientScreenController(store.load());
        RecordingHost host = new RecordingHost();
        EchoClientSettingsController controller = new EchoClientSettingsController(screens, store, host);

        controller.applyAndPersist();
        require(host.mouseSensitivityPercent == 50,
                "Settings controller should apply default input sensitivity to the host");
        require(host.rawMouseInput,
                "Settings controller should apply default raw mouse input to the host");
        require(host.chunkViewDistance == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE,
                "Settings controller should apply default chunk view distance to the renderer host");
        require(host.languageCode.equals(EchoClientSettings.DEFAULT_LANGUAGE_CODE),
                "Settings controller should apply default language locale to the host");
        require(host.subtitles,
                "Settings controller should apply default subtitle preference to the host");
        require(host.worldRefreshes == 1,
                "Settings controller should request one initial chunk streaming refresh");
        require(!Files.exists(store.path()),
                "Settings controller should not persist settings until ScreenCore marks them dirty");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should open for controller smoke");
        screens.moveSelection(4, false, 720);
        require(screens.snapshot(false).options().get(screens.snapshot(false).selectedIndex()).label().equals("Chunk View"),
                "Chunk View should be selectable in video settings");
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(host.chunkViewDistance == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE + 1,
                "Changed chunk view distance should apply to the renderer host");
        require(host.worldRefreshes == 2,
                "Changing chunk view distance should request another streaming refresh");
        require(Files.isRegularFile(store.path()),
                "Dirty settings should persist through the controller");
        require(store.load().chunkViewDistance() == EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE + 1,
                "Persisted settings should keep the edited chunk view distance");

        controller.applyAndPersist();
        require(host.worldRefreshes == 2,
                "Reapplying unchanged chunk view distance should not request another streaming refresh");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_CONTROLS, false),
                "Controls should open for controller smoke");
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(host.mouseSensitivityPercent == 55,
                "Changed mouse sensitivity should apply to the input host");
        require(store.load().mouseSensitivityPercent() == 55,
                "Persisted settings should keep the edited mouse sensitivity");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_AUDIO_SETTINGS, false),
                "Audio settings should open for controller smoke");
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(host.masterVolumePercent == 85,
                "Changed master volume should apply to the audio host");
        require(store.load().masterVolumePercent() == 85,
                "Persisted settings should keep the edited master volume");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_VIDEO_SETTINGS, false),
                "Video settings should reopen for fullscreen smoke");
        screens.moveSelection(2, false, 720);
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(host.fullscreen,
                "Changed fullscreen preference should apply to the window host");
        require(store.load().fullscreen(),
                "Persisted settings should keep the edited fullscreen preference");
        screens.moveSelection(1, false, 720);
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(!host.vSync,
                "Changed VSync preference should apply to the window host");
        require(!store.load().vSync(),
                "Persisted settings should keep the edited VSync preference");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_ACCESSIBILITY_SETTINGS, false),
                "Accessibility settings should open for controller smoke");
        screens.editSelectedControl(1, false);
        controller.applyAndPersist();
        require(!host.subtitles,
                "Changed subtitle preference should apply to the language/accessibility host");
        require(!store.load().subtitles(),
                "Persisted settings should keep the edited subtitle preference");

        require(screens.executeNavigationCommand(EchoClientScreenCommand.OPEN_LANGUAGE_SETTINGS, false),
                "Language settings should open for controller smoke");
        screens.activateSelection(false);
        controller.applyAndPersist();
        require(host.languageCode.equals("en_gb"),
                "Changed language preference should apply to the language host");
        require(store.load().languageCode().equals("en_gb"),
                "Persisted settings should keep the edited language preference");
        require(host.saveFailures == 0,
                "Settings controller should not report save failures on a writable options path");

        System.out.println("client settings controller smoke PASS chunkView="
                + host.chunkViewDistance
                + " sensitivity=" + host.mouseSensitivityPercent
                + " masterVolume=" + host.masterVolumePercent
                + " fullscreen=" + host.fullscreen
                + " vsync=" + host.vSync);
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingHost implements EchoClientSettingsController.Host {
        private int mouseSensitivityPercent;
        private boolean rawMouseInput;
        private int masterVolumePercent;
        private int chunkViewDistance;
        private String languageCode = "";
        private boolean subtitles;
        private boolean fullscreen;
        private boolean vSync = true;
        private int worldRefreshes;
        private int saveFailures;

        @Override
        public void applyInputSettings(EchoClientSettings settings) {
            mouseSensitivityPercent = settings.mouseSensitivityPercent();
            rawMouseInput = settings.rawMouseInput();
        }

        @Override
        public void applyAudioSettings(EchoClientSettings settings) {
            masterVolumePercent = settings.masterVolumePercent();
        }

        @Override
        public void applyLanguageSettings(EchoClientSettings settings) {
            languageCode = settings.languageCode();
            subtitles = settings.subtitles();
        }

        @Override
        public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
            this.chunkViewDistance = chunkViewDistance;
            if (chunkViewChanged) {
                worldRefreshes++;
            }
        }

        @Override
        public void applyWindowSettings(boolean fullscreen, boolean vSync) {
            this.fullscreen = fullscreen;
            this.vSync = vSync;
        }

        @Override
        public void settingsSaveFailed(Path path, String error) {
            saveFailures++;
        }
    }
}
