package dev.echo.standalone.runtime.client;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

record EchoClientWorldPresentation(
        String windowTitle,
        String settingsFileComment,
        String createWorldActionLabel,
        String worldTypeLabel,
        String packLabel,
        String newWorldModalMessage,
        String loadingInitialDetail,
        String newWorldGenerationLabel,
        String newWorldDetailSuffix,
        String newWorldLoadingFooter,
        String savedWorldLoadingFooter,
        Map<String, String> moduleSourceLabels,
        String hostileDamageSourceId
) {
    EchoClientWorldPresentation {
        windowTitle = text(windowTitle, "ECHO Client");
        settingsFileComment = text(settingsFileComment, "ECHO client options");
        createWorldActionLabel = text(createWorldActionLabel, "Create World");
        worldTypeLabel = text(worldTypeLabel, "World Type");
        packLabel = text(packLabel, "Runtime Pack");
        newWorldModalMessage = text(newWorldModalMessage, "Start a new world session?");
        loadingInitialDetail = text(loadingInitialDetail, "Preparing world");
        newWorldGenerationLabel = text(newWorldGenerationLabel, "Generating world");
        newWorldDetailSuffix = text(newWorldDetailSuffix, "Runtime Pack");
        newWorldLoadingFooter = text(newWorldLoadingFooter, "Preparing runtime services");
        savedWorldLoadingFooter = text(savedWorldLoadingFooter, "Restoring save data");
        moduleSourceLabels = normalizeSourceLabels(moduleSourceLabels);
        hostileDamageSourceId = text(hostileDamageSourceId, "echo:hostile");
    }

    static EchoClientWorldPresentation generic() {
        return new EchoClientWorldPresentation(
                "ECHO Client",
                "ECHO client options",
                "Create World",
                "World Type: Runtime Template",
                "Runtime Pack",
                "Start a new world session?",
                "Preparing world",
                "Generating world",
                "Runtime Pack",
                "Preparing runtime services",
                "Restoring save data",
                Map.of("echoadaptercore", "adaptercore"),
                "echo:hostile"
        );
    }

    String sourceLabel(String moduleId) {
        String source = optionalText(moduleId).toLowerCase(Locale.ROOT);
        if (source.isBlank()) {
            return "";
        }
        String explicit = moduleSourceLabels.get(source);
        if (explicit != null) {
            return explicit;
        }
        return source.startsWith("echo") && source.length() > 4 ? source.substring(4) : source;
    }

    String newWorldDetail(String seedText) {
        String cleanSeed = seedText == null || seedText.isBlank() ? "" : seedText.trim();
        return cleanSeed.isBlank()
                ? newWorldDetailSuffix
                : "Seed " + cleanSeed + " | " + newWorldDetailSuffix;
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String optionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private static Map<String, String> normalizeSourceLabels(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            String key = optionalText(entry.getKey()).toLowerCase(Locale.ROOT);
            String value = optionalText(entry.getValue());
            if (!key.isBlank() && !value.isBlank()) {
                normalized.put(key, value);
            }
        }
        return Map.copyOf(normalized);
    }
}
