package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreNativeContentRegistrations;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientRuntimeEntityCatalogBridge {
    private EchoClientRuntimeEntityCatalogBridge() {
    }

    static EchoClientEntityCatalog merge(
            EchoClientEntityCatalog baseCatalog,
            List<Map<String, Object>> rows
    ) {
        EchoClientEntityCatalog base = baseCatalog == null ? EchoClientEntityCatalog.empty() : baseCatalog;
        if (rows == null || rows.isEmpty()) {
            return base;
        }

        EchoEntityDefinition fallback = base.fallbackHostile();
        ArrayList<EchoClientEntityCatalog.SpawnRule> runtimeRules = new ArrayList<>();
        LinkedHashMap<String, EchoClientEntityCatalog.RenderProfile> renderProfiles =
                new LinkedHashMap<>(base.renderProfiles());
        LinkedHashMap<String, EchoEntityDefinition> definitions = new LinkedHashMap<>(base.definitions());

        for (Map<String, Object> row : rows) {
            if (!isEntityRow(row)) {
                continue;
            }
            Map<String, Object> metadata = map(row.get("metadata"));
            EchoEntityDefinition definition = EchoAdapterCoreNativeContentRegistrations.entityDefinitionFromRow(row);
            definitions.put(definition.definitionId(), definition);
            if (booleanValue(firstText(row.get("fallbackHostile"), metadata.get("fallbackHostile")), false)) {
                fallback = definition;
            }
            List<String> biomeTags = textList(
                    row.get("spawnBiomeTags"),
                    metadata.get("spawnBiomeTags"),
                    row.get("biomeTags"),
                    metadata.get("biomeTags"),
                    metadata.get("spawnTags")
            );
            if (!biomeTags.isEmpty()) {
                runtimeRules.add(new EchoClientEntityCatalog.SpawnRule(biomeTags, definition));
            }
            renderProfiles.put(definition.definitionId(), renderProfile(definition.definitionId(), row, metadata));
        }

        if (runtimeRules.isEmpty()
                && fallback == base.fallbackHostile()
                && renderProfiles.equals(base.renderProfiles())
                && definitions.equals(base.definitions())) {
            return base;
        }
        ArrayList<EchoClientEntityCatalog.SpawnRule> mergedRules = new ArrayList<>(runtimeRules);
        mergedRules.addAll(base.spawnRules());
        return new EchoClientEntityCatalog(fallback, mergedRules, renderProfiles, definitions);
    }

    private static boolean isEntityRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(text(row.get("contentKind")));
        String domain = text(row.get("domain")).toLowerCase(Locale.ROOT);
        return kind.equals("ENTITY") || domain.equals("entities");
    }

    private static EchoClientEntityCatalog.RenderProfile renderProfile(
            String definitionId,
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        int argb = intValue(firstText(
                row.get("renderArgb"),
                metadata.get("renderArgb"),
                row.get("argb"),
                metadata.get("argb"),
                row.get("color"),
                metadata.get("color")
        ), defaultEntityColor(definitionId));
        EchoClientEntityCatalog.RenderShape shape = renderShape(firstText(
                row.get("renderShape"),
                metadata.get("renderShape"),
                row.get("shape"),
                metadata.get("shape")
        ), definitionId);
        return new EchoClientEntityCatalog.RenderProfile(argb, shape);
    }

    private static EchoClientEntityCatalog.RenderShape renderShape(String value, String definitionId) {
        String normalized = normalizedEnumToken(value);
        if (!normalized.isBlank()) {
            try {
                return EchoClientEntityCatalog.RenderShape.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return inferShape(definitionId);
            }
        }
        return inferShape(definitionId);
    }

    private static EchoClientEntityCatalog.RenderShape inferShape(String definitionId) {
        String id = text(definitionId).toLowerCase(Locale.ROOT);
        if (id.contains("slime")) {
            return EchoClientEntityCatalog.RenderShape.SLIME;
        }
        if (id.contains("drone") || id.contains("flyer")) {
            return EchoClientEntityCatalog.RenderShape.DRONE;
        }
        return EchoClientEntityCatalog.RenderShape.HUMANOID;
    }

    private static int defaultEntityColor(String definitionId) {
        int hash = text(definitionId).hashCode();
        int red = 96 + Math.floorMod(hash >>> 16, 104);
        int green = 96 + Math.floorMod(hash >>> 8, 104);
        int blue = 96 + Math.floorMod(hash, 104);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String normalizedEnumToken(String value) {
        return text(value)
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_')
                .replace(' ', '_');
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean booleanValue(String value, boolean fallback) {
        String text = text(value);
        return text.isBlank() ? fallback : Boolean.parseBoolean(text);
    }

    private static int intValue(String value, int fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            if (text.startsWith("#")) {
                String hex = text.substring(1);
                if (hex.length() == 6) {
                    hex = "FF" + hex;
                }
                return (int) Long.parseLong(hex, 16);
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return (int) Long.parseLong(text.substring(2), 16);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static List<String> textList(Object... values) {
        if (values == null) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    String text = text(item);
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            } else if (value instanceof String text) {
                for (String item : text.split("[,;]")) {
                    String normalized = item.trim();
                    if (!normalized.isBlank()) {
                        result.add(normalized);
                    }
                }
            }
            if (!result.isEmpty()) {
                return List.copyOf(result);
            }
        }
        return List.of();
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                mapped.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(mapped);
    }
}
