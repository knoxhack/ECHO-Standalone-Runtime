package dev.echo.standalone.runtime.assets;

import java.util.List;
import java.util.Objects;

public record EchoAssetConflict(String logicalId, List<EchoAssetEntry> entries) {
    public EchoAssetConflict {
        logicalId = requireText(logicalId, "logicalId");
        Objects.requireNonNull(entries, "entries");
        entries = List.copyOf(entries);
        if (entries.size() < 2) {
            throw new IllegalArgumentException("conflict entries must contain at least two entries");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
