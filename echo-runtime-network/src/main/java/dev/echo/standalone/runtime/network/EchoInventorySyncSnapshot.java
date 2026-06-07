package dev.echo.standalone.runtime.network;

import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoItemStack;

import java.util.List;
import java.util.Objects;

public record EchoInventorySyncSnapshot(
        String inventoryId,
        String label,
        String ownerEntityId,
        int capacity,
        int occupiedSlots,
        int itemStackCount,
        int totalQuantity,
        List<String> slotEntries
) {
    public EchoInventorySyncSnapshot {
        inventoryId = EchoNetworkText.requireText(inventoryId, "inventoryId");
        label = EchoNetworkText.requireText(label, "label");
        ownerEntityId = EchoNetworkText.requireText(ownerEntityId, "ownerEntityId");
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        if (occupiedSlots < 0 || occupiedSlots > capacity) {
            throw new IllegalArgumentException("occupiedSlots must be within capacity");
        }
        if (itemStackCount < 0) {
            throw new IllegalArgumentException("itemStackCount must not be negative");
        }
        if (totalQuantity < 0) {
            throw new IllegalArgumentException("totalQuantity must not be negative");
        }
        Objects.requireNonNull(slotEntries, "slotEntries");
        slotEntries = List.copyOf(slotEntries);
    }

    public static EchoInventorySyncSnapshot from(EchoInventoryContainer container) {
        Objects.requireNonNull(container, "container");
        List<String> slotEntries = container.slots().stream()
                .flatMap(slot -> slot.stack().stream()
                        .map(stack -> slot.index() + "=" + stack.itemId().value() + "x" + stack.quantity()))
                .toList();
        int totalQuantity = container.slots().stream()
                .flatMap(slot -> slot.stack().stream())
                .mapToInt(EchoItemStack::quantity)
                .sum();
        return new EchoInventorySyncSnapshot(
                container.id().value(),
                container.label(),
                container.ownerEntityId().map(owner -> owner.value()).orElse("none"),
                container.capacity(),
                container.occupiedSlots(),
                slotEntries.size(),
                totalQuantity,
                slotEntries
        );
    }

    public String payload() {
        return inventoryId
                + "|owner=" + ownerEntityId
                + "|slots=" + occupiedSlots + "/" + capacity
                + "|stacks=" + itemStackCount
                + "|quantity=" + totalQuantity
                + "|items=" + String.join(",", slotEntries);
    }
}
