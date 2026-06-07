package dev.echo.standalone.runtime.client;

import java.util.Locale;
import java.util.Map;

record EchoClientRuntimeContentRowSummary(
        String contentId,
        String domain,
        String contentKind,
        String displayName,
        String moduleId,
        String nativeLoaderId,
        String standaloneRuntimeId
) {
    EchoClientRuntimeContentRowSummary {
        contentId = text(contentId);
        domain = normalizeDomain(domain, contentKind);
        contentKind = normalizeKind(contentKind);
        displayName = firstText(displayName, displayName(contentId), contentId);
        moduleId = firstText(moduleId, moduleFromContentId(contentId));
        nativeLoaderId = text(nativeLoaderId);
        standaloneRuntimeId = firstText(standaloneRuntimeId, contentId);
    }

    static EchoClientRuntimeContentRowSummary fromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : row;
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = firstText(safeRow.get("contentId"), metadata.get("contentId"));
        String kind = firstText(safeRow.get("contentKind"), metadata.get("contentKind"), metadata.get("kind"));
        return new EchoClientRuntimeContentRowSummary(
                contentId,
                firstText(safeRow.get("domain"), metadata.get("domain")),
                kind,
                firstText(safeRow.get("displayName"), metadata.get("displayName")),
                firstText(safeRow.get("moduleId"), metadata.get("moduleId")),
                firstText(safeRow.get("nativeLoaderId"), metadata.get("nativeLoaderId")),
                firstText(safeRow.get("standaloneRuntimeId"), metadata.get("standaloneRuntimeId"))
        );
    }

    boolean valid() {
        return !contentId.isBlank();
    }

    String menuLabel() {
        return "Runtime " + kindLabel(contentKind) + ": " + displayName;
    }

    String detailLabel() {
        return contentId
                + " | domain=" + domain
                + " | module=" + moduleId
                + (nativeLoaderId.isBlank() ? "" : " | native=" + nativeLoaderId)
                + (standaloneRuntimeId.isBlank() ? "" : " | standalone=" + standaloneRuntimeId);
    }

    static String domainLabel(String domain) {
        return titleLabel(normalizeDomain(domain, ""));
    }

    private static String normalizeDomain(String domain, String contentKind) {
        String normalized = text(domain).toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_');
        if (!normalized.isBlank()) {
            return normalized;
        }
        return switch (normalizeKind(contentKind)) {
            case "BLOCK" -> "blocks";
            case "ITEM" -> "items";
            case "ENTITY" -> "entities";
            case "RECIPE" -> "recipes";
            case "LOOT_TABLE" -> "loot";
            case "STRUCTURE" -> "structures";
            case "UI_SCREEN" -> "ui_screens";
            case "SOUND_EVENT" -> "sounds";
            case "WORLD_REGION", "WORLDGEN_DEFINITION" -> "worldgen";
            case "WORLD_HAZARD" -> "hazards";
            case "NETWORK_HOOK" -> "networking";
            case "COMMAND" -> "commands";
            default -> "runtime";
        };
    }

    private static String normalizeKind(String contentKind) {
        String normalized = text(contentKind).toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_')
                .replace(' ', '_');
        return normalized.isBlank() ? "CONTENT" : normalized;
    }

    private static String kindLabel(String contentKind) {
        return titleLabel(normalizeKind(contentKind).toLowerCase(Locale.ROOT));
    }

    private static String titleLabel(String value) {
        String normalized = text(value).replace('_', ' ').replace('-', ' ');
        if (normalized.isBlank()) {
            return "Runtime";
        }
        StringBuilder builder = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            String lower = part.toLowerCase(Locale.ROOT);
            if (lower.equals("ui")) {
                builder.append("UI");
            } else {
                builder.append(Character.toUpperCase(lower.charAt(0))).append(lower.substring(1));
            }
        }
        return builder.length() == 0 ? "Runtime" : builder.toString();
    }

    private static String displayName(String contentId) {
        String value = text(contentId);
        int slash = value.lastIndexOf('/');
        int colon = value.lastIndexOf(':');
        int start = Math.max(slash, colon) + 1;
        if (start > 0 && start < value.length()) {
            value = value.substring(start);
        }
        return titleLabel(value);
    }

    private static String moduleFromContentId(String contentId) {
        String value = text(contentId);
        int colon = value.indexOf(':');
        return colon > 0 ? value.substring(0, colon) : "";
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

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return Map.of();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
