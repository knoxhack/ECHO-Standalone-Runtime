package dev.echo.standalone.runtime.item;

import dev.echo.standalone.runtime.entity.EchoEntityId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoInventoryContainer {
    private final EchoInventoryId id;
    private final Optional<EchoEntityId> ownerEntityId;
    private final String label;
    private final ArrayList<EchoInventorySlot> slots;

    public EchoInventoryContainer(
            EchoInventoryId id,
            Optional<EchoEntityId> ownerEntityId,
            String label,
            int capacity
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.ownerEntityId = Objects.requireNonNull(ownerEntityId, "ownerEntityId");
        this.label = EchoItemText.requireText(label, "label");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.slots = new ArrayList<>();
        for (int index = 0; index < capacity; index++) {
            slots.add(new EchoInventorySlot(index));
        }
    }

    public EchoInventoryId id() {
        return id;
    }

    public Optional<EchoEntityId> ownerEntityId() {
        return ownerEntityId;
    }

    public String label() {
        return label;
    }

    public synchronized int capacity() {
        return slots.size();
    }

    public synchronized EchoInventorySlot slot(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IllegalArgumentException("slot index out of range: " + index);
        }
        return slots.get(index);
    }

    public synchronized List<EchoInventorySlot> slots() {
        return List.copyOf(slots);
    }

    public synchronized int occupiedSlots() {
        return (int) slots.stream()
                .filter(slot -> !slot.empty())
                .count();
    }

    public synchronized int totalQuantity(EchoItemId itemId) {
        Objects.requireNonNull(itemId, "itemId");
        return slots.stream()
                .flatMap(slot -> slot.stack().stream())
                .filter(stack -> stack.itemId().equals(itemId))
                .mapToInt(EchoItemStack::quantity)
                .sum();
    }
}
