package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class EchoClientSettingsStore {
    private final Path path;
    private final String fileComment;
    private String lastError = "";

    EchoClientSettingsStore(Path path) {
        this(path, EchoClientWorldTemplates.defaultTemplate().presentation().settingsFileComment());
    }

    EchoClientSettingsStore(Path path, String fileComment) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null");
        }
        this.path = path.toAbsolutePath().normalize();
        this.fileComment = fileComment == null || fileComment.isBlank()
                ? EchoClientWorldPresentation.generic().settingsFileComment()
                : fileComment.trim();
    }

    static EchoClientSettingsStore openDefault() {
        return openDefault(EchoClientWorldTemplates.defaultTemplate().presentation());
    }

    static EchoClientSettingsStore openDefault(EchoClientWorldPresentation presentation) {
        EchoClientWorldPresentation safePresentation =
                presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        return new EchoClientSettingsStore(
                Path.of("saves").resolve("client").resolve("options.properties"),
                safePresentation.settingsFileComment()
        );
    }

    EchoClientSettings load() {
        lastError = "";
        if (!Files.isRegularFile(path)) {
            return EchoClientSettings.defaults();
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            return new EchoClientSettings(
                    intValue(properties, "mouseSensitivityPercent", 50),
                    booleanValue(properties, "rawMouseInput", true),
                    intValue(properties, "fovDegrees", EchoClientSettings.DEFAULT_FOV_DEGREES),
                    intValue(properties, "uiScalePercent", 50),
                    booleanValue(properties, "fullscreen", false),
                    booleanValue(properties, "vSync", true),
                    intValue(properties, "chunkViewDistance", EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE),
                    intValue(properties, "masterVolumePercent", 80),
                    intValue(properties, "musicVolumePercent", 55),
                    intValue(properties, "ambienceVolumePercent", 70),
                    stringValue(properties, "languageCode", EchoClientSettings.DEFAULT_LANGUAGE_CODE),
                    booleanValue(properties, "subtitles", true),
                    booleanValue(properties, "highContrastUi", false),
                    booleanValue(properties, "reducedMotion", false),
                    EchoClientKeyBindings.decode(properties.getProperty("keyBindings"))
            );
        } catch (IOException | IllegalArgumentException exception) {
            lastError = exception.getMessage();
            return EchoClientSettings.defaults();
        }
    }

    void save(EchoClientSettings settings) {
        if (settings == null) {
            return;
        }
        lastError = "";
        Properties properties = new Properties();
        properties.setProperty("mouseSensitivityPercent", Integer.toString(settings.mouseSensitivityPercent()));
        properties.setProperty("rawMouseInput", Boolean.toString(settings.rawMouseInput()));
        properties.setProperty("fovDegrees", Integer.toString(settings.fovDegrees()));
        properties.setProperty("uiScalePercent", Integer.toString(settings.uiScalePercent()));
        properties.setProperty("fullscreen", Boolean.toString(settings.fullscreen()));
        properties.setProperty("vSync", Boolean.toString(settings.vSync()));
        properties.setProperty("chunkViewDistance", Integer.toString(settings.chunkViewDistance()));
        properties.setProperty("masterVolumePercent", Integer.toString(settings.masterVolumePercent()));
        properties.setProperty("musicVolumePercent", Integer.toString(settings.musicVolumePercent()));
        properties.setProperty("ambienceVolumePercent", Integer.toString(settings.ambienceVolumePercent()));
        properties.setProperty("languageCode", settings.languageCode());
        properties.setProperty("subtitles", Boolean.toString(settings.subtitles()));
        properties.setProperty("highContrastUi", Boolean.toString(settings.highContrastUi()));
        properties.setProperty("reducedMotion", Boolean.toString(settings.reducedMotion()));
        properties.setProperty("keyBindings", settings.keyBindings().encode());
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (OutputStream output = Files.newOutputStream(path)) {
                properties.store(output, fileComment);
            }
        } catch (IOException exception) {
            lastError = exception.getMessage();
        }
    }

    String lastError() {
        return lastError;
    }

    Path path() {
        return path;
    }

    private static int intValue(Properties properties, String key, int fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value.trim());
    }

    private static boolean booleanValue(Properties properties, String key, boolean fallback) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(value.trim());
    }

    private static String stringValue(Properties properties, String key, String fallback) {
        String value = properties.getProperty(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
