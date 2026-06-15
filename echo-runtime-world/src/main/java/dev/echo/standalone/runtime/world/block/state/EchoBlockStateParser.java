package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;
import dev.echo.standalone.runtime.contracts.voxel.EchoBlockStateContract;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parses Minecraft-style blockstate strings such as {@code "minecraft:stone"} or
 * {@code "minecraft:stairs[facing=north,half=bottom]"} into runtime blockstates.
 */
public final class EchoBlockStateParser {

    private final EchoBlockRegistry registry;

    public EchoBlockStateParser(EchoBlockRegistry registry) {
        this.registry = registry;
    }

    public EchoBlockStateContract parse(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (trimmed.isEmpty()) {
            return registry.air();
        }
        int bracket = trimmed.indexOf('[');
        String blockId;
        Map<String, String> propertyStrings;
        if (bracket < 0) {
            blockId = normalizeId(trimmed);
            propertyStrings = Map.of();
        } else {
            if (!trimmed.endsWith("]")) {
                throw new IllegalArgumentException("Unclosed property list in blockstate: " + input);
            }
            blockId = normalizeId(trimmed.substring(0, bracket));
            String inner = trimmed.substring(bracket + 1, trimmed.length() - 1);
            propertyStrings = parseProperties(inner);
        }

        EchoBlock block = registry.require(blockId);
        if (propertyStrings.isEmpty()) {
            return block.defaultState();
        }

        LinkedHashMap<EchoBlockPropertyContract<?>, Object> values = new LinkedHashMap<>();
        for (EchoBlockPropertyContract<?> property : block.properties()) {
            String raw = propertyStrings.get(property.name());
            Object value = raw == null ? property.defaultValue() : property.parse(raw);
            values.put(property, value);
        }
        return block.state(values);
    }

    private Map<String, String> parseProperties(String inner) {
        if (inner.isBlank()) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (String pair : inner.split(",")) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            int eq = trimmed.indexOf('=');
            if (eq < 0) {
                throw new IllegalArgumentException("Invalid property pair: " + pair);
            }
            String key = trimmed.substring(0, eq).trim();
            String value = trimmed.substring(eq + 1).trim();
            result.put(key, value);
        }
        return Map.copyOf(result);
    }

    private String normalizeId(String id) {
        String trimmed = id.trim().toLowerCase();
        if (!trimmed.contains(":")) {
            return "minecraft:" + trimmed;
        }
        return trimmed;
    }
}
