package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

final class EchoClientSettingsController {
    private final EchoClientScreenController screens;
    private final EchoClientSettingsStore settingsStore;
    private final Host host;
    private int appliedChunkViewDistance = Integer.MIN_VALUE;

    EchoClientSettingsController(
            EchoClientScreenController screens,
            EchoClientSettingsStore settingsStore,
            Host host
    ) {
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        if (settingsStore == null) {
            throw new IllegalArgumentException("settingsStore must not be null");
        }
        if (host == null) {
            throw new IllegalArgumentException("host must not be null");
        }
        this.screens = screens;
        this.settingsStore = settingsStore;
        this.host = host;
    }

    void applyAndPersist() {
        EchoClientSettings settings = screens.clientSettings();
        host.applyInputSettings(settings);
        host.applyAudioSettings(settings);
        host.applyLanguageSettings(settings);
        boolean chunkViewChanged = appliedChunkViewDistance != settings.chunkViewDistance();
        appliedChunkViewDistance = settings.chunkViewDistance();
        host.applyRenderSettings(settings.chunkViewDistance(), chunkViewChanged);
        host.applyWindowSettings(settings.fullscreen(), settings.vSync());
        persistIfDirty(settings);
    }

    private void persistIfDirty(EchoClientSettings settings) {
        if (!screens.consumeClientSettingsDirty()) {
            return;
        }
        settingsStore.save(settings);
        if (!settingsStore.lastError().isBlank()) {
            host.settingsSaveFailed(settingsStore.path(), settingsStore.lastError());
        }
    }

    interface Host {
        void applyInputSettings(EchoClientSettings settings);

        void applyAudioSettings(EchoClientSettings settings);

        default void applyLanguageSettings(EchoClientSettings settings) {
        }

        void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged);

        void applyWindowSettings(boolean fullscreen, boolean vSync);

        void settingsSaveFailed(Path path, String error);
    }
}
