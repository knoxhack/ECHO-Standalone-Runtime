package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;

import java.util.Objects;

record EchoClientDroppedItem(
        String dropId,
        EchoItemDefinition definition,
        int quantity,
        double x,
        double y,
        double z,
        double ageSeconds
) {
    EchoClientDroppedItem {
        dropId = requireText(dropId, "dropId");
        definition = Objects.requireNonNull(definition, "definition");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        x = finite(x);
        y = finite(y);
        z = finite(z);
        ageSeconds = Math.max(0.0D, finite(ageSeconds));
    }

    EchoItemId itemId() {
        return definition.id();
    }

    double distanceSquared(double otherX, double otherY, double otherZ) {
        double dx = x - otherX;
        double dy = y - otherY;
        double dz = z - otherZ;
        return dx * dx + dy * dy + dz * dz;
    }

    EchoClientDroppedItem withQuantity(int nextQuantity) {
        return new EchoClientDroppedItem(dropId, definition, nextQuantity, x, y, z, ageSeconds);
    }

    EchoClientDroppedItem withQuantityAndAge(int nextQuantity, double nextAgeSeconds) {
        return new EchoClientDroppedItem(dropId, definition, nextQuantity, x, y, z, nextAgeSeconds);
    }

    EchoClientDroppedItem withPositionAndAge(double nextX, double nextY, double nextZ, double nextAgeSeconds) {
        return new EchoClientDroppedItem(dropId, definition, quantity, nextX, nextY, nextZ, nextAgeSeconds);
    }

    EchoClientDroppedItem withAge(double nextAgeSeconds) {
        return new EchoClientDroppedItem(dropId, definition, quantity, x, y, z, nextAgeSeconds);
    }

    EchoClientDroppedItemSnapshot snapshot() {
        return EchoClientDroppedItemSnapshot.fromDrop(this);
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
