package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

record EchoClientInventoryScreenModel(
        String screenId,
        String title,
        List<EchoClientSlotStack> slots,
        int selectedSlot
) {
    static final int SLOT_COUNT = EchoVoxelPlayerHotbar.SLOT_COUNT;

    EchoClientInventoryScreenModel {
        screenId = screenId == null || screenId.isBlank() ? "echoscreencore:inventory" : screenId;
        title = title == null || title.isBlank() ? "Inventory" : title;
        Objects.requireNonNull(slots, "slots");
        ArrayList<EchoClientSlotStack> padded = new ArrayList<>(SLOT_COUNT);
        for (int index = 0; index < SLOT_COUNT; index++) {
            padded.add(EchoClientSlotStack.empty(index));
        }
        for (EchoClientSlotStack slot : slots) {
            if (slot.index() >= 0 && slot.index() < SLOT_COUNT) {
                padded.set(slot.index(), slot);
            }
        }
        slots = List.copyOf(padded);
        if (selectedSlot < 0 || selectedSlot >= EchoVoxelPlayerHotbar.HOTBAR_COUNT) {
            selectedSlot = 0;
        }
    }

    static EchoClientInventoryScreenModel fromHotbar(EchoVoxelPlayerHotbar hotbar) {
        Objects.requireNonNull(hotbar, "hotbar");
        return new EchoClientInventoryScreenModel(
                "echoscreencore:inventory",
                "Inventory",
                hotbar.slots().stream()
                        .map(EchoClientSlotStack::fromHotbarSlot)
                        .toList(),
                hotbar.selectedSlot()
        );
    }

    static EchoClientInventoryScreenModel fromItemContainer(EchoInventoryContainer container) {
        return fromItemContainer(container, 0);
    }

    static EchoClientInventoryScreenModel fromItemContainer(EchoInventoryContainer container, int selectedSlot) {
        return fromItemContainer(container, selectedSlot, EchoClientToolState.empty());
    }

    static EchoClientInventoryScreenModel fromItemContainer(
            EchoInventoryContainer container,
            int selectedSlot,
            EchoClientToolState toolState
    ) {
        Objects.requireNonNull(container, "container");
        EchoClientToolState safeToolState = toolState == null ? EchoClientToolState.empty() : toolState;
        return new EchoClientInventoryScreenModel(
                "echoscreencore:inventory/" + container.id().fileSafeKey(),
                container.label(),
                container.slots().stream()
                        .map(slot -> EchoClientSlotStack.fromInventorySlot(slot, safeToolState))
                        .toList(),
                selectedSlot
        );
    }

    EchoClientSlotStack slot(int index) {
        if (index < 0 || index >= slots.size()) {
            throw new IllegalArgumentException("slot index must be 0.." + (slots.size() - 1) + ", was " + index);
        }
        return slots.get(index);
    }
}
