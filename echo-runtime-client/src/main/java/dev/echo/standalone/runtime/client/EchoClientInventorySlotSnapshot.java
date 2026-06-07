package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoInventorySlot;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;

import java.util.List;
import java.util.Objects;

record EchoClientInventorySlotSnapshot(
        int index,
        String itemId,
        String displayName,
        EchoItemCategory category,
        int maxStackSize,
        int count
) {
    EchoClientInventorySlotSnapshot {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        itemId = requireText(itemId, "itemId");
        displayName = requireText(displayName, "displayName");
        category = category == null ? EchoItemCategory.MATERIAL : category;
        if (maxStackSize <= 0) {
            throw new IllegalArgumentException("maxStackSize must be positive");
        }
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }

    static EchoClientInventorySlotSnapshot fromSlot(EchoInventorySlot slot) {
        Objects.requireNonNull(slot, "slot");
        EchoItemStack stack = slot.stack().orElseThrow();
        EchoItemDefinition definition = stack.definition();
        return new EchoClientInventorySlotSnapshot(
                slot.index(),
                definition.id().value(),
                definition.displayName(),
                definition.category(),
                definition.maxStackSize(),
                stack.quantity()
        );
    }

    EchoItemDefinition definition() {
        return new EchoItemDefinition(
                new EchoItemId(itemId),
                displayName,
                category,
                maxStackSize,
                1.0D,
                List.of("client-save"),
                List.of("Restored from client inventory save")
        );
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }
}
