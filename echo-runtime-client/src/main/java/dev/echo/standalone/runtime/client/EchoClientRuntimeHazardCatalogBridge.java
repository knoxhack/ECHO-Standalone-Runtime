package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientRuntimeHazardCatalogBridge {
    private EchoClientRuntimeHazardCatalogBridge() {
    }

    static EchoClientHazardCatalog merge(
            EchoClientHazardCatalog baseCatalog,
            List<Map<String, Object>> rows
    ) {
        EchoClientHazardCatalog base = baseCatalog == null ? EchoClientHazardCatalog.empty() : baseCatalog;
        if (rows == null || rows.isEmpty()) {
            return base;
        }

        ArrayList<EchoClientHazardCatalog.Rule> runtimeRules = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (!isHazardRow(row)) {
                continue;
            }
            Map<String, Object> metadata = map(row.get("metadata"));
            List<String> biomeTags = textList(
                    row.get("biomeTags"),
                    metadata.get("biomeTags"),
                    row.get("hazardBiomeTags"),
                    metadata.get("hazardBiomeTags"),
                    metadata.get("matchTags")
            );
            if (biomeTags.isEmpty()) {
                continue;
            }
            runtimeRules.add(new EchoClientHazardCatalog.Rule(biomeTags, profile(row, metadata)));
        }

        if (runtimeRules.isEmpty()) {
            return base;
        }
        ArrayList<EchoClientHazardCatalog.Rule> merged = new ArrayList<>(runtimeRules);
        merged.addAll(base.rules());
        return new EchoClientHazardCatalog(merged);
    }

    private static EchoClientHazardCatalog.HazardProfile profile(
            Map<String, Object> row,
            Map<String, Object> metadata
    ) {
        String contentId = text(row.get("contentId"));
        String hazardId = firstText(
                row.get("hazardId"),
                metadata.get("hazardId"),
                row.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        String label = firstText(row.get("displayName"), metadata.get("displayName"), displayName(contentId));
        double exposurePerSecond = doubleValue(firstText(
                row.get("exposurePerSecond"),
                metadata.get("exposurePerSecond"),
                row.get("exposure"),
                metadata.get("exposure")
        ), 4.0D);
        int damage = Math.max(0, intValue(firstText(row.get("damage"), metadata.get("damage")), 1));
        return new EchoClientHazardCatalog.HazardProfile(hazardId, label, exposurePerSecond, damage);
    }

    private static boolean isHazardRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(text(row.get("contentKind")));
        String domain = text(row.get("domain")).toLowerCase(Locale.ROOT);
        return kind.equals("WORLD_HAZARD") || domain.equals("hazards") || domain.equals("weather");
    }

    private static String displayName(String contentId) {
        String token = text(contentId);
        int separator = token.indexOf(':');
        if (separator >= 0 && separator + 1 < token.length()) {
            token = token.substring(separator + 1);
        }
        token = token.replace('/', ' ').replace('_', ' ').replace('-', ' ').trim();
        if (token.isBlank()) {
            return "Runtime Hazard";
        }
        StringBuilder result = new StringBuilder(token.length());
        boolean nextUpper = true;
        for (int index = 0; index < token.length(); index++) {
            char ch = token.charAt(index);
            if (Character.isWhitespace(ch)) {
                result.append(ch);
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(ch));
                nextUpper = false;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
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

    private static int intValue(String value, int fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double doubleValue(String value, double fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(0.0D, Double.parseDouble(text));
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
        java.util.LinkedHashMap<String, Object> mapped = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                mapped.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(mapped);
    }
}
