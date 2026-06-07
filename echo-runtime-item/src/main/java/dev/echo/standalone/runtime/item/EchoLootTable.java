package dev.echo.standalone.runtime.item;

import java.util.List;
import java.util.Objects;

public record EchoLootTable(String tableId, List<EchoLootEntry> entries) {
    public EchoLootTable {
        tableId = EchoItemText.requireText(tableId, "tableId");
        Objects.requireNonNull(entries, "entries");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("entries must not be empty");
        }
        entries = List.copyOf(entries);
    }
}
