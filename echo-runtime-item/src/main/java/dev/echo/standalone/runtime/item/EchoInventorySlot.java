package dev.echo.standalone.runtime.item;

import java.util.Objects;
import java.util.Optional;

public final class EchoInventorySlot {
    private final int index;
    private EchoItemStack stack;

    public EchoInventorySlot(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        this.index = index;
    }

    public int index() {
        return index;
    }

    public Optional<EchoItemStack> stack() {
        return Optional.ofNullable(stack);
    }

    public boolean empty() {
        return stack == null;
    }

    public void setStack(EchoItemStack stack) {
        this.stack = Objects.requireNonNull(stack, "stack");
    }

    public void clear() {
        stack = null;
    }
}
