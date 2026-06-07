package dev.echo.standalone.runtime.item;

import java.util.Objects;
import java.util.Optional;

public record EchoItemStack(EchoItemDefinition definition, int quantity) {
    public EchoItemStack {
        Objects.requireNonNull(definition, "definition");
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (quantity > definition.maxStackSize()) {
            throw new IllegalArgumentException("quantity must not exceed max stack size");
        }
    }

    public EchoItemId itemId() {
        return definition.id();
    }

    public boolean canMerge(EchoItemStack other) {
        Objects.requireNonNull(other, "other");
        return itemId().equals(other.itemId());
    }

    public int spaceRemaining() {
        return definition.maxStackSize() - quantity;
    }

    public EchoItemStack withQuantity(int nextQuantity) {
        return new EchoItemStack(definition, nextQuantity);
    }

    public EchoItemStack add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        return withQuantity(quantity + amount);
    }

    public Optional<EchoItemStack> remove(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        int remaining = quantity - amount;
        if (remaining <= 0) {
            return Optional.empty();
        }
        return Optional.of(withQuantity(remaining));
    }
}
