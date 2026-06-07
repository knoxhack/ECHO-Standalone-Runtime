package dev.echo.standalone.runtime.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientRuntimeInteractionCatalogBridge {
    private EchoClientRuntimeInteractionCatalogBridge() {
    }

    static EchoClientWorldInteractionCatalog merge(
            EchoClientWorldInteractionCatalog baseCatalog,
            List<Map<String, Object>> rows
    ) {
        EchoClientWorldInteractionCatalog base =
                baseCatalog == null ? EchoClientWorldInteractionCatalog.empty() : baseCatalog;
        if (rows == null || rows.isEmpty()) {
            return base;
        }

        ArrayList<EchoClientWorldInteractionCatalog.Rule> runtimeRules = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> metadata = map(row.get("metadata"));
            List<String> behaviorHooks = textList(
                    row.get("behaviorHooks"),
                    row.get("runtimeBehaviorHooks"),
                    row.get("hooks"),
                    metadata.get("behaviorHooks"),
                    metadata.get("runtimeBehaviorHooks"),
                    metadata.get("hooks"),
                    metadata.get("interactionHooks")
            );
            boolean interactionRow = isInteractionRow(row);
            boolean registryBehaviorRow = isRegistryBehaviorRow(row, behaviorHooks);
            if (!interactionRow && !registryBehaviorRow) {
                continue;
            }
            List<String> matchTokens = textList(
                    row.get("matchTokens"),
                    metadata.get("matchTokens"),
                    row.get("interactionTokens"),
                    metadata.get("interactionTokens"),
                    row.get("poiTokens"),
                    metadata.get("poiTokens")
            );
            EchoClientScreenCommand command = command(firstText(
                    row.get("command"),
                    metadata.get("command"),
                    row.get("interactionCommand"),
                    metadata.get("interactionCommand")
            ));
            if (command == EchoClientScreenCommand.NONE) {
                command = commandFromHooks(behaviorHooks);
            }
            String targetId = targetId(row, metadata, interactionRow);
            if (command == EchoClientScreenCommand.NONE && !targetId.isBlank()) {
                command = EchoClientScreenCommand.OPEN_REGISTERED_SCREEN;
            }
            if (command == EchoClientScreenCommand.OPEN_REGISTERED_SCREEN && targetId.isBlank()) {
                continue;
            }
            if (matchTokens.isEmpty() && registryBehaviorRow) {
                matchTokens = registryMatchTokens(row, metadata);
            }
            if (matchTokens.isEmpty()) {
                continue;
            }
            if (command != EchoClientScreenCommand.NONE) {
                runtimeRules.add(new EchoClientWorldInteractionCatalog.Rule(matchTokens, command, targetId));
            }
        }

        if (runtimeRules.isEmpty()) {
            return base;
        }
        ArrayList<EchoClientWorldInteractionCatalog.Rule> merged = new ArrayList<>(runtimeRules);
        merged.addAll(base.rules());
        return new EchoClientWorldInteractionCatalog(merged);
    }

    private static boolean isInteractionRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(text(row.get("contentKind")));
        String domain = text(row.get("domain")).toLowerCase(Locale.ROOT);
        return kind.equals("STRUCTURE")
                || kind.equals("WORLD_REGION")
                || domain.equals("structures")
                || domain.equals("worldgen")
                || domain.equals("maps");
    }

    private static boolean isRegistryBehaviorRow(Map<String, Object> row, List<String> behaviorHooks) {
        if (row == null) {
            return false;
        }
        String kind = normalizedEnumToken(text(row.get("contentKind")));
        String domain = text(row.get("domain")).toLowerCase(Locale.ROOT);
        return (kind.equals("BLOCK") || domain.equals("blocks")) && !behaviorHooks.isEmpty();
    }

    private static EchoClientScreenCommand command(String value) {
        String normalized = normalizedEnumToken(value);
        if (normalized.isBlank()) {
            return EchoClientScreenCommand.NONE;
        }
        try {
            return EchoClientScreenCommand.valueOf(normalized);
        } catch (IllegalArgumentException exception) {
            return EchoClientScreenCommand.NONE;
        }
    }

    private static EchoClientScreenCommand commandFromHooks(List<String> behaviorHooks) {
        if (behaviorHooks == null || behaviorHooks.isEmpty()) {
            return EchoClientScreenCommand.NONE;
        }
        for (String hook : behaviorHooks) {
            EchoClientScreenCommand command = command(hook);
            if (command != EchoClientScreenCommand.NONE) {
                return command;
            }
            String normalized = normalizedEnumToken(hook);
            if (normalized.equals("OPEN_REGISTERED_SCREEN")
                    || normalized.equals("SCREEN_OPEN_REGISTERED")
                    || normalized.equals("OPEN_SCREEN")
                    || normalized.equals("OPEN_UI_SCREEN")) {
                return EchoClientScreenCommand.OPEN_REGISTERED_SCREEN;
            }
            if (normalized.equals("OPEN_CONTAINER") || normalized.equals("CONTAINER_OPEN")) {
                return EchoClientScreenCommand.OPEN_CONTAINER;
            }
            if (normalized.equals("OPEN_WORKBENCH") || normalized.equals("WORKBENCH_OPEN")) {
                return EchoClientScreenCommand.OPEN_WORKBENCH;
            }
            if (normalized.equals("OPEN_MACHINE") || normalized.equals("MACHINE_OPEN")) {
                return EchoClientScreenCommand.OPEN_MACHINE;
            }
            if (normalized.equals("OPEN_TERMINAL") || normalized.equals("TERMINAL_OPEN")) {
                return EchoClientScreenCommand.OPEN_TERMINAL;
            }
            if (normalized.equals("OPEN_INVENTORY") || normalized.equals("INVENTORY_OPEN")) {
                return EchoClientScreenCommand.OPEN_INVENTORY;
            }
        }
        return EchoClientScreenCommand.NONE;
    }

    private static String targetId(
            Map<String, Object> row,
            Map<String, Object> metadata,
            boolean allowStandaloneRuntimeFallback
    ) {
        String targetId = firstText(
                row.get("targetId"),
                metadata.get("targetId"),
                row.get("screenId"),
                metadata.get("screenId"),
                row.get("registeredScreenId"),
                metadata.get("registeredScreenId"),
                row.get("targetScreenId"),
                metadata.get("targetScreenId"),
                metadata.get("standaloneScreenId"),
                metadata.get("screenRouteId")
        );
        if (!targetId.isBlank() || !allowStandaloneRuntimeFallback) {
            return targetId;
        }
        return firstText(row.get("standaloneRuntimeId"), metadata.get("standaloneRuntimeId"));
    }

    private static List<String> registryMatchTokens(Map<String, Object> row, Map<String, Object> metadata) {
        ArrayList<String> tokens = new ArrayList<>();
        addToken(tokens, metadata.get("liveVoxelId"));
        addToken(tokens, row.get("liveVoxelId"));
        addToken(tokens, row.get("standaloneRuntimeId"));
        addToken(tokens, metadata.get("standaloneRuntimeId"));
        addToken(tokens, row.get("neoForgeId"));
        addToken(tokens, metadata.get("neoForgeId"));
        addToken(tokens, row.get("contentId"));
        addToken(tokens, row.get("displayName"));
        return List.copyOf(tokens);
    }

    private static void addToken(ArrayList<String> tokens, Object value) {
        String token = text(value);
        if (token.isBlank()) {
            return;
        }
        String normalized = token.toLowerCase(Locale.ROOT);
        if (!tokens.contains(normalized)) {
            tokens.add(normalized);
        }
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
