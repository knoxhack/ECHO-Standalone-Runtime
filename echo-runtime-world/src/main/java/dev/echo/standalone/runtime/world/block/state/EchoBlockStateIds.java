package dev.echo.standalone.runtime.world.block.state;

import java.util.Locale;

/**
 * Shared helpers for blockstate identifiers and validation.
 */
final class EchoBlockStateIds {

    private EchoBlockStateIds() {
    }

    static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }

    static String normalizeId(String id) {
        String trimmed = requireText(id, "id").toLowerCase(Locale.ROOT);
        if (!trimmed.contains(":")) {
            throw new IllegalArgumentException("Block ID must be namespaced: " + id);
        }
        return trimmed;
    }
}
