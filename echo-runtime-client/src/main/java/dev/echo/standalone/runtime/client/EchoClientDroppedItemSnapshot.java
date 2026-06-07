package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;

import java.util.List;

record EchoClientDroppedItemSnapshot(
        String dropId,
        String itemId,
        String displayName,
        EchoItemCategory category,
        int maxStackSize,
        int quantity,
        double x,
        double y,
        double z,
        double ageSeconds
) {
    EchoClientDroppedItemSnapshot {
        dropId = requireText(dropId, "dropId");
        itemId = requireText(itemId, "itemId");
        displayName = displayName == null || displayName.isBlank() ? itemId : displayName;
        category = category == null ? EchoItemCategory.MATERIAL : category;
        maxStackSize = Math.max(1, maxStackSize);
        quantity = Math.max(1, quantity);
        x = finite(x);
        y = finite(y);
        z = finite(z);
        ageSeconds = Math.max(0.0D, finite(ageSeconds));
    }

    static EchoClientDroppedItemSnapshot fromDrop(EchoClientDroppedItem drop) {
        EchoItemDefinition definition = drop.definition();
        return new EchoClientDroppedItemSnapshot(
                drop.dropId(),
                definition.id().value(),
                definition.displayName(),
                definition.category(),
                definition.maxStackSize(),
                drop.quantity(),
                drop.x(),
                drop.y(),
                drop.z(),
                drop.ageSeconds()
        );
    }

    EchoItemDefinition definition() {
        return new EchoItemDefinition(
                new EchoItemId(itemId),
                displayName,
                category,
                maxStackSize,
                1.0D,
                List.of("dropped_item"),
                List.of("Restored dropped item")
        );
    }

    EchoClientDroppedItem drop() {
        return new EchoClientDroppedItem(dropId, definition(), quantity, x, y, z, ageSeconds);
    }

    private static double finite(double value) {
        return Double.isFinite(value) ? value : 0.0D;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
