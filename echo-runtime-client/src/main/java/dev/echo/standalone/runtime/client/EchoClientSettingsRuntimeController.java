package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

final class EchoClientSettingsRuntimeController implements EchoClientSettingsController.Host {
    private final EchoClientScreenController screens;
    private final RenderTarget renderTarget;
    private final WindowTarget windowTarget;
    private InputTarget inputTarget;
    private AudioTarget audioTarget;
    private LanguageTarget languageTarget;

    EchoClientSettingsRuntimeController(
            EchoClientScreenController screens,
            EchoClientRenderRuntimeController renderRuntime,
            EchoGlfwWindow window
    ) {
        this(
                screens,
                new RenderTarget() {
                    @Override
                    public void setChunkViewDistance(int chunkViewDistance) {
                        renderRuntime.setChunkViewDistance(chunkViewDistance);
                    }

                    @Override
                    public void refreshWorldStreamingAndMeshes() {
                        renderRuntime.refreshWorldStreamingAndMeshes();
                    }
                },
                new WindowTarget() {
                    @Override
                    public void setFullscreen(boolean fullscreen) {
                        window.setFullscreen(fullscreen);
                    }

                    @Override
                    public void setVSync(boolean vSync) {
                        window.setVSync(vSync);
                    }
                }
        );
    }

    EchoClientSettingsRuntimeController(
            EchoClientScreenController screens,
            RenderTarget renderTarget,
            WindowTarget windowTarget
    ) {
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        if (renderTarget == null) {
            throw new IllegalArgumentException("renderTarget must not be null");
        }
        if (windowTarget == null) {
            throw new IllegalArgumentException("windowTarget must not be null");
        }
        this.screens = screens;
        this.renderTarget = renderTarget;
        this.windowTarget = windowTarget;
    }

    void attachInput(EchoClientInput input) {
        inputTarget = input == null
                ? null
                : new InputTarget() {
                    @Override
                    public void setMouseSensitivityPercent(int mouseSensitivityPercent) {
                        input.setMouseSensitivityPercent(mouseSensitivityPercent);
                    }

                    @Override
                    public void setRawMouseInput(boolean rawMouseInput) {
                        input.setRawMouseInput(rawMouseInput);
                    }

                    @Override
                    public void setKeyBindings(EchoClientKeyBindings keyBindings) {
                        input.setKeyBindings(keyBindings);
                    }
                };
    }

    void attachAudio(EchoClientAudio audio) {
        audioTarget = audio == null ? null : audio::applySettings;
    }

    void attachLanguage(EchoClientLanguageService language) {
        languageTarget = language == null ? null : language::applySettings;
    }

    void attachInputTarget(InputTarget inputTarget) {
        this.inputTarget = inputTarget;
    }

    void attachAudioTarget(AudioTarget audioTarget) {
        this.audioTarget = audioTarget;
    }

    void attachLanguageTarget(LanguageTarget languageTarget) {
        this.languageTarget = languageTarget;
    }

    @Override
    public void applyInputSettings(EchoClientSettings settings) {
        if (inputTarget == null || settings == null) {
            return;
        }
        inputTarget.setMouseSensitivityPercent(settings.mouseSensitivityPercent());
        inputTarget.setRawMouseInput(settings.rawMouseInput());
        inputTarget.setKeyBindings(settings.keyBindings());
    }

    @Override
    public void applyAudioSettings(EchoClientSettings settings) {
        if (audioTarget != null && settings != null) {
            audioTarget.applySettings(settings);
        }
    }

    @Override
    public void applyLanguageSettings(EchoClientSettings settings) {
        if (languageTarget != null && settings != null) {
            languageTarget.applySettings(settings);
        }
    }

    @Override
    public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
        renderTarget.setChunkViewDistance(chunkViewDistance);
        if (chunkViewChanged) {
            renderTarget.refreshWorldStreamingAndMeshes();
        }
    }

    @Override
    public void applyWindowSettings(boolean fullscreen, boolean vSync) {
        windowTarget.setFullscreen(fullscreen);
        windowTarget.setVSync(vSync);
    }

    @Override
    public void settingsSaveFailed(Path path, String error) {
        screens.showToast("Options save failed");
        System.out.println("[echo-client] options save failed at " + path + ": " + error);
    }

    interface InputTarget {
        void setMouseSensitivityPercent(int mouseSensitivityPercent);

        void setRawMouseInput(boolean rawMouseInput);

        void setKeyBindings(EchoClientKeyBindings keyBindings);
    }

    interface AudioTarget {
        void applySettings(EchoClientSettings settings);
    }

    interface LanguageTarget {
        void applySettings(EchoClientSettings settings);
    }

    interface RenderTarget {
        void setChunkViewDistance(int chunkViewDistance);

        void refreshWorldStreamingAndMeshes();
    }

    interface WindowTarget {
        void setFullscreen(boolean fullscreen);

        void setVSync(boolean vSync);
    }
}
