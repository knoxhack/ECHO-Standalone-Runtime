package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

public final class EchoClientSettingsRuntimeControllerSmokeHarness {
    private EchoClientSettingsRuntimeControllerSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientScreenController screens = new EchoClientScreenController();
        RecordingInputTarget input = new RecordingInputTarget();
        RecordingAudioTarget audio = new RecordingAudioTarget();
        RecordingLanguageTarget language = new RecordingLanguageTarget();
        RecordingRenderTarget render = new RecordingRenderTarget();
        RecordingWindowTarget window = new RecordingWindowTarget();
        EchoClientSettingsRuntimeController settingsRuntime =
                new EchoClientSettingsRuntimeController(screens, render, window);
        settingsRuntime.attachInputTarget(input);
        settingsRuntime.attachAudioTarget(audio);
        settingsRuntime.attachLanguageTarget(language);

        EchoClientSettings settings = new EchoClientSettings(
                67,
                false,
                82,
                55,
                true,
                false,
                5,
                71,
                33,
                44,
                "de_de",
                false,
                true,
                true,
                EchoClientKeyBindings.decode("open_inventory=I")
        );
        settingsRuntime.applyInputSettings(settings);
        settingsRuntime.applyAudioSettings(settings);
        settingsRuntime.applyLanguageSettings(settings);
        settingsRuntime.applyRenderSettings(settings.chunkViewDistance(), false);
        settingsRuntime.applyWindowSettings(settings.fullscreen(), settings.vSync());

        require(input.mouseSensitivityPercent == 67, "Settings runtime should apply mouse sensitivity");
        require(!input.rawMouseInput, "Settings runtime should apply raw mouse input");
        require(input.keyBindings.label(EchoClientKeyAction.OPEN_INVENTORY).equals("I"),
                "Settings runtime should apply key bindings");
        require(audio.appliedSettings == settings, "Settings runtime should apply audio settings");
        require(language.appliedSettings == settings, "Settings runtime should apply language/accessibility settings");
        require(language.locale.equals("de_de"), "Settings runtime should apply the selected language locale");
        require(render.chunkViewDistance == 5, "Settings runtime should apply chunk view distance");
        require(render.refreshes == 0, "Unchanged chunk view should not refresh streaming meshes");
        require(window.fullscreen, "Settings runtime should apply fullscreen state");
        require(!window.vSync, "Settings runtime should apply VSync state");

        settingsRuntime.applyRenderSettings(6, true);
        require(render.chunkViewDistance == 6, "Changed render settings should update chunk view");
        require(render.refreshes == 1, "Changed chunk view should refresh streaming meshes");

        settingsRuntime.settingsSaveFailed(Path.of("build", "tmp", "missing.properties"), "denied");
        require(screens.snapshot(false).toast().visible(),
                "Settings save failure should publish a ScreenCore toast");

        System.out.println("client settings runtime controller smoke PASS sensitivity="
                + input.mouseSensitivityPercent
                + " chunkView=" + render.chunkViewDistance
                + " refreshes=" + render.refreshes);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingInputTarget implements EchoClientSettingsRuntimeController.InputTarget {
        private int mouseSensitivityPercent;
        private boolean rawMouseInput = true;
        private EchoClientKeyBindings keyBindings = EchoClientKeyBindings.defaults();

        @Override
        public void setMouseSensitivityPercent(int mouseSensitivityPercent) {
            this.mouseSensitivityPercent = mouseSensitivityPercent;
        }

        @Override
        public void setRawMouseInput(boolean rawMouseInput) {
            this.rawMouseInput = rawMouseInput;
        }

        @Override
        public void setKeyBindings(EchoClientKeyBindings keyBindings) {
            this.keyBindings = keyBindings;
        }
    }

    private static final class RecordingAudioTarget implements EchoClientSettingsRuntimeController.AudioTarget {
        private EchoClientSettings appliedSettings;

        @Override
        public void applySettings(EchoClientSettings settings) {
            appliedSettings = settings;
        }
    }

    private static final class RecordingLanguageTarget implements EchoClientSettingsRuntimeController.LanguageTarget {
        private EchoClientSettings appliedSettings;
        private String locale = "";

        @Override
        public void applySettings(EchoClientSettings settings) {
            appliedSettings = settings;
            locale = settings.languageCode();
        }
    }

    private static final class RecordingRenderTarget implements EchoClientSettingsRuntimeController.RenderTarget {
        private int chunkViewDistance;
        private int refreshes;

        @Override
        public void setChunkViewDistance(int chunkViewDistance) {
            this.chunkViewDistance = chunkViewDistance;
        }

        @Override
        public void refreshWorldStreamingAndMeshes() {
            refreshes++;
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
}
