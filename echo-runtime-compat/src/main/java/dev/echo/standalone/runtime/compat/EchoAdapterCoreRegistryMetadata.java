package dev.echo.standalone.runtime.compat;

import java.util.List;
import java.util.Map;

public record EchoAdapterCoreRegistryMetadata(
        List<String> tags,
        Map<String, String> defaultState,
        List<String> behaviorHooks,
        String saveCodecVersion,
        Map<String, String> compatibilityMetadata
) {
    public static final EchoAdapterCoreRegistryMetadata NONE =
            new EchoAdapterCoreRegistryMetadata(List.of(), Map.of(), List.of(), "", Map.of());

    public EchoAdapterCoreRegistryMetadata {
        tags = normalizeList(tags);
        defaultState = normalizeMap(defaultState);
        behaviorHooks = normalizeList(behaviorHooks);
        saveCodecVersion = saveCodecVersion == null ? "" : saveCodecVersion.trim();
        compatibilityMetadata = normalizeMap(compatibilityMetadata);
    }

    public boolean empty() {
        return tags.isEmpty()
                && defaultState.isEmpty()
                && behaviorHooks.isEmpty()
                && saveCodecVersion.isBlank()
                && compatibilityMetadata.isEmpty();
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static Map<String, String> normalizeMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        java.util.LinkedHashMap<String, String> normalized = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey().trim();
            String value = entry.getValue() == null ? "" : entry.getValue().trim();
            if (!key.isBlank() && !value.isBlank()) {
                normalized.put(key, value);
            }
        }
        return normalized.isEmpty() ? Map.of() : Map.copyOf(normalized);
    }
}
