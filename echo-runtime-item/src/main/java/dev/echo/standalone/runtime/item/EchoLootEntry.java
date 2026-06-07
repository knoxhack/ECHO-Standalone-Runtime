package dev.echo.standalone.runtime.item;

import java.util.Objects;

public record EchoLootEntry(EchoItemId itemId, int quantity) {
    public EchoLootEntry {
        Objects.requireNonNull(itemId, "itemId");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
