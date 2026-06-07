package dev.echo.standalone.runtime.compat;

import java.util.Locale;

public record EchoNeoForgeDependency(
        String ownerModId,
        String modId,
        String type,
        String versionRange,
        String ordering,
        String side,
        String reason
) {
    public EchoNeoForgeDependency {
        ownerModId = optional(ownerModId);
        modId = require(modId, "modId");
        type = optional(type).isBlank() ? "unspecified" : optional(type).toLowerCase(Locale.ROOT);
        versionRange = optional(versionRange);
        ordering = optional(ordering);
        side = optional(side);
        reason = optional(reason);
    }

    public boolean required() {
        return type.equals("required") || type.equals("mandatory");
    }

    public boolean optionalDependency() {
        return type.equals("optional");
    }

    public boolean platformDependency() {
        return modId.equals("minecraft") || modId.equals("neoforge");
    }

    public String summary() {
        return modId + ":" + type + (versionRange.isBlank() ? "" : ":" + versionRange);
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    private static String require(String value, String name) {
        String normalized = optional(value);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return normalized;
    }
}
