package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.item.EchoItemDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAdapterCoreRuntimeContentRegistry {
    private final LinkedHashMap<String, Map<String, Object>> registrations = new LinkedHashMap<>();

    public boolean register(String contentId, Map<String, Object> registration) {
        String safeContentId = requireText(contentId, "contentId");
        LinkedHashMap<String, Object> normalized = new LinkedHashMap<>(copyMap(registration));
        normalized.putIfAbsent("contentId", safeContentId);
        Map<String, Object> row = Map.copyOf(normalized);
        Map<String, Object> previous = registrations.put(safeContentId, row);
        return !row.equals(previous);
    }

    public int registerAll(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return 0;
        }
        int changed = 0;
        for (Map<String, Object> row : rows) {
            String contentId = text(row == null ? null : row.get("contentId"));
            if (contentId.isBlank()) {
                continue;
            }
            if (register(contentId, row)) {
                changed++;
            }
        }
        return changed;
    }

    public List<Map<String, Object>> registrations(String domain) {
        String requestedDomain = text(domain);
        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> registration : registrations.values()) {
            if (requestedDomain.isBlank()
                    || requestedDomain.equalsIgnoreCase(Objects.toString(registration.get("domain"), ""))) {
                rows.add(registration);
            }
        }
        return List.copyOf(rows);
    }

    public List<EchoAdapterCoreRegistryEntry> entries() {
        return EchoAdapterCoreNativeContentRegistrations.entriesFromRows(registrations(""));
    }

    public List<EchoAdapterCoreRegistryEntry> entries(String domain) {
        return EchoAdapterCoreNativeContentRegistrations.entriesFromRows(registrations(domain));
    }

    public List<EchoItemDefinition> itemDefinitions() {
        return EchoAdapterCoreNativeContentRegistrations.itemDefinitionsFromRows(registrations(""));
    }

    public List<EchoRecipeDefinition> recipeDefinitions() {
        return EchoAdapterCoreNativeContentRegistrations.recipeDefinitionsFromRows(registrations(""));
    }

    public List<EchoLootDefinition> lootDefinitions() {
        return EchoAdapterCoreNativeContentRegistrations.lootDefinitionsFromRows(registrations(""));
    }

    public List<EchoEntityDefinition> entityDefinitions() {
        return EchoAdapterCoreNativeContentRegistrations.entityDefinitionsFromRows(registrations(""));
    }

    public int size() {
        return registrations.size();
    }

    private static Map<String, Object> copyMap(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Object> copied = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            if (entry.getKey() != null) {
                copied.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(copied);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
