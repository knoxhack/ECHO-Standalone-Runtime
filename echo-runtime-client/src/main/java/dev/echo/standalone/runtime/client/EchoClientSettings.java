package dev.echo.standalone.runtime.client;

record EchoClientSettings(
        int mouseSensitivityPercent,
        boolean rawMouseInput,
        int fovDegrees,
        int uiScalePercent,
        boolean fullscreen,
        boolean vSync,
        int chunkViewDistance,
        int masterVolumePercent,
        int musicVolumePercent,
        int ambienceVolumePercent,
        String languageCode,
        boolean subtitles,
        boolean highContrastUi,
        boolean reducedMotion,
        EchoClientKeyBindings keyBindings
) {
    static final String DEFAULT_LANGUAGE_CODE = "en_us";
    static final int DEFAULT_FOV_DEGREES = 70;
    static final int MIN_FOV_DEGREES = 30;
    static final int MAX_FOV_DEGREES = 110;
    static final int DEFAULT_CHUNK_VIEW_DISTANCE = 3;
    static final int MIN_CHUNK_VIEW_DISTANCE = 1;
    static final int MAX_CHUNK_VIEW_DISTANCE = 8;

    EchoClientSettings {
        mouseSensitivityPercent = clampPercent(mouseSensitivityPercent);
        fovDegrees = clampFov(fovDegrees);
        uiScalePercent = clampPercent(uiScalePercent);
        chunkViewDistance = clampChunkViewDistance(chunkViewDistance);
        masterVolumePercent = clampPercent(masterVolumePercent);
        musicVolumePercent = clampPercent(musicVolumePercent);
        ambienceVolumePercent = clampPercent(ambienceVolumePercent);
        languageCode = normalizeLanguageCode(languageCode);
        keyBindings = keyBindings == null ? EchoClientKeyBindings.defaults() : keyBindings.normalized();
    }

    static int clampFov(int value) {
        return Math.max(MIN_FOV_DEGREES, Math.min(MAX_FOV_DEGREES, value));
    }

    static int clampChunkViewDistance(int value) {
        return Math.max(MIN_CHUNK_VIEW_DISTANCE, Math.min(MAX_CHUNK_VIEW_DISTANCE, value));
    }

    static double visibleDistanceBlocks(int chunkViewDistance, int chunkSize) {
        int safeChunkSize = Math.max(1, chunkSize);
        return clampChunkViewDistance(chunkViewDistance) * safeChunkSize * 2.0D;
    }

    static String normalizeLanguageCode(String value) {
        String normalized = value == null || value.isBlank() ? DEFAULT_LANGUAGE_CODE : value.trim();
        normalized = normalized.toLowerCase(java.util.Locale.ROOT).replace('-', '_');
        return normalized.matches("[a-z]{2}_[a-z]{2}") ? normalized : DEFAULT_LANGUAGE_CODE;
    }

    static EchoClientSettings defaults() {
        return new EchoClientSettings(
                50,
                true,
                DEFAULT_FOV_DEGREES,
                50,
                false,
                true,
                DEFAULT_CHUNK_VIEW_DISTANCE,
                80,
                55,
                70,
                DEFAULT_LANGUAGE_CODE,
                true,
                false,
                false,
                EchoClientKeyBindings.defaults()
        );
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
