package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoVoxelPlayerHotbar {
    public static final int HOTBAR_COUNT = 9;
    public static final int CARRY_COUNT = 27;
    public static final int SLOT_COUNT = HOTBAR_COUNT + CARRY_COUNT;
    public static final int CARRY_START = HOTBAR_COUNT;
    public static final int MAX_STACK = 64;
    private final EchoVoxelHotbarSlot[] slots = new EchoVoxelHotbarSlot[SLOT_COUNT];
    private int selectedSlot;

    public EchoVoxelPlayerHotbar(List<EchoVoxelHotbarSlot> initialSlots, int selectedSlot) {
        for (int index = 0; index < SLOT_COUNT; index++) {
            slots[index] = new EchoVoxelHotbarSlot(index, EchoVoxelBlock.AIR, 0);
        }
        for (EchoVoxelHotbarSlot slot : initialSlots) {
            if (slot.index() >= 0 && slot.index() < SLOT_COUNT) {
                slots[slot.index()] = slot;
            }
        }
        select(selectedSlot);
    }

    public int selectedSlot() {
        return selectedSlot;
    }

    public EchoVoxelHotbarSlot selected() {
        return slots[selectedSlot];
    }

    public EchoVoxelHotbarSlot slot(int index) {
        requireSlotIndex(index);
        return slots[index];
    }

    public List<EchoVoxelHotbarSlot> slots() {
        ArrayList<EchoVoxelHotbarSlot> result = new ArrayList<>();
        for (EchoVoxelHotbarSlot slot : slots) {
            result.add(slot);
        }
        return List.copyOf(result);
    }

    public List<EchoVoxelHotbarSlot> hotbarSlots() {
        ArrayList<EchoVoxelHotbarSlot> result = new ArrayList<>();
        for (int i = 0; i < HOTBAR_COUNT; i++) {
            result.add(slots[i]);
        }
        return List.copyOf(result);
    }

    public List<EchoVoxelHotbarSlot> carrySlots() {
        ArrayList<EchoVoxelHotbarSlot> result = new ArrayList<>();
        for (int i = CARRY_START; i < SLOT_COUNT; i++) {
            result.add(slots[i]);
        }
        return List.copyOf(result);
    }

    public EchoVoxelHotbarMutation select(int index) {
        if (index < 0 || index >= HOTBAR_COUNT) {
            throw new IllegalArgumentException("selected slot must be a hotbar index 0.." + (HOTBAR_COUNT - 1));
        }
        selectedSlot = index;
        return new EchoVoxelHotbarMutation(true, "selected_slot_" + (index + 1), selected());
    }

    public EchoVoxelHotbarMutation add(EchoVoxelBlock block, int amount) {
        Objects.requireNonNull(block, "block");
        if (block.air()) {
            return unchanged("ignored_air_pickup", selected());
        }
        if (amount <= 0) {
            return unchanged("ignored_empty_pickup", selected());
        }
        int remaining = amount;

        // 1) Fill matching stacks anywhere (hotbar first, then carry)
        for (int index = 0; index < SLOT_COUNT && remaining > 0; index++) {
            EchoVoxelHotbarSlot slot = slots[index];
            if (!slot.empty() && slot.block().id().equals(block.id()) && slot.count() < MAX_STACK) {
                int accepted = Math.min(remaining, MAX_STACK - slot.count());
                slots[index] = new EchoVoxelHotbarSlot(index, block, slot.count() + accepted);
                remaining -= accepted;
            }
        }
        // 2) Fill empty hotbar slots
        for (int index = 0; index < HOTBAR_COUNT && remaining > 0; index++) {
            EchoVoxelHotbarSlot slot = slots[index];
            if (slot.empty()) {
                int accepted = Math.min(remaining, MAX_STACK);
                slots[index] = new EchoVoxelHotbarSlot(index, block, accepted);
                remaining -= accepted;
            }
        }
        // 3) Fill empty carry slots
        for (int index = CARRY_START; index < SLOT_COUNT && remaining > 0; index++) {
            EchoVoxelHotbarSlot slot = slots[index];
            if (slot.empty()) {
                int accepted = Math.min(remaining, MAX_STACK);
                slots[index] = new EchoVoxelHotbarSlot(index, block, accepted);
                remaining -= accepted;
            }
        }
        boolean changed = remaining < amount;
        String reason = changed ? "picked_up_" + (amount - remaining) : "inventory_full";
        return new EchoVoxelHotbarMutation(changed, reason, selected());
    }

    public EchoVoxelHotbarMutation consumeSelected() {
        EchoVoxelHotbarSlot slot = selected();
        if (slot.empty()) {
            return unchanged("selected_slot_empty", slot);
        }
        int nextCount = slot.count() - 1;
        slots[selectedSlot] = nextCount == 0
                ? new EchoVoxelHotbarSlot(selectedSlot, EchoVoxelBlock.AIR, 0)
                : new EchoVoxelHotbarSlot(selectedSlot, slot.block(), nextCount);
        return new EchoVoxelHotbarMutation(true, "consumed_one", slots[selectedSlot]);
    }

    public EchoVoxelHotbarTransferResult moveOrMergeSlot(int sourceIndex, int targetIndex) {
        requireSlotIndex(sourceIndex);
        requireSlotIndex(targetIndex);
        EchoVoxelHotbarSlot source = slots[sourceIndex];
        EchoVoxelHotbarSlot target = slots[targetIndex];
        if (sourceIndex == targetIndex) {
            return transfer(false, "drag", "same_slot", sourceIndex, targetIndex, 0);
        }
        if (source.empty()) {
            return transfer(false, "drag", "empty_source", sourceIndex, targetIndex, 0);
        }
        if (target.empty()) {
            slots[targetIndex] = new EchoVoxelHotbarSlot(targetIndex, source.block(), source.count());
            slots[sourceIndex] = emptySlot(sourceIndex);
            return transfer(true, "drag", "moved_stack", sourceIndex, targetIndex, source.count());
        }
        if (target.block().id().equals(source.block().id())) {
            int accepted = Math.min(source.count(), MAX_STACK - target.count());
            if (accepted <= 0) {
                return transfer(false, "drag", "target_stack_full", sourceIndex, targetIndex, 0);
            }
            slots[targetIndex] = new EchoVoxelHotbarSlot(targetIndex, target.block(), target.count() + accepted);
            int remaining = source.count() - accepted;
            slots[sourceIndex] = remaining == 0
                    ? emptySlot(sourceIndex)
                    : new EchoVoxelHotbarSlot(sourceIndex, source.block(), remaining);
            return transfer(true, "drag", "merged_stack", sourceIndex, targetIndex, accepted);
        }
        slots[sourceIndex] = new EchoVoxelHotbarSlot(sourceIndex, target.block(), target.count());
        slots[targetIndex] = new EchoVoxelHotbarSlot(targetIndex, source.block(), source.count());
        return transfer(true, "drag", "swapped_slots", sourceIndex, targetIndex, source.count());
    }

    public EchoVoxelHotbarTransferResult splitSlotTo(int sourceIndex, int targetIndex) {
        requireSlotIndex(sourceIndex);
        requireSlotIndex(targetIndex);
        EchoVoxelHotbarSlot source = slots[sourceIndex];
        EchoVoxelHotbarSlot target = slots[targetIndex];
        if (sourceIndex == targetIndex) {
            return transfer(false, "split", "same_slot", sourceIndex, targetIndex, 0);
        }
        if (source.empty()) {
            return transfer(false, "split", "empty_source", sourceIndex, targetIndex, 0);
        }
        if (source.count() < 2) {
            return transfer(false, "split", "source_stack_too_small", sourceIndex, targetIndex, 0);
        }
        if (!target.empty()) {
            return transfer(false, "split", "target_occupied", sourceIndex, targetIndex, 0);
        }
        int moved = source.count() / 2;
        int remaining = source.count() - moved;
        slots[sourceIndex] = new EchoVoxelHotbarSlot(sourceIndex, source.block(), remaining);
        slots[targetIndex] = new EchoVoxelHotbarSlot(targetIndex, source.block(), moved);
        return transfer(true, "split", "split_stack", sourceIndex, targetIndex, moved);
    }

    public EchoVoxelHotbarMutation assignSlot(int index, EchoVoxelBlock block, int amount) {
        requireSlotIndex(index);
        Objects.requireNonNull(block, "block");
        if (block.air() || amount <= 0) {
            slots[index] = emptySlot(index);
            return new EchoVoxelHotbarMutation(true, "assigned_empty", slots[index]);
        }
        int clamped = Math.min(amount, MAX_STACK);
        slots[index] = new EchoVoxelHotbarSlot(index, block, clamped);
        return new EchoVoxelHotbarMutation(true, "assigned_" + block.id(), slots[index]);
    }

    public EchoVoxelPlayerHotbar copy() {
        return new EchoVoxelPlayerHotbar(slots(), selectedSlot);
    }

    private static EchoVoxelHotbarMutation unchanged(String reason, EchoVoxelHotbarSlot slot) {
        return new EchoVoxelHotbarMutation(false, reason, slot);
    }

    private EchoVoxelHotbarTransferResult transfer(
            boolean changed,
            String action,
            String reason,
            int sourceIndex,
            int targetIndex,
            int moved
    ) {
        return new EchoVoxelHotbarTransferResult(
                changed,
                action,
                reason,
                sourceIndex,
                targetIndex,
                moved,
                slots[sourceIndex],
                slots[targetIndex]
        );
    }

    private static EchoVoxelHotbarSlot emptySlot(int index) {
        return new EchoVoxelHotbarSlot(index, EchoVoxelBlock.AIR, 0);
    }

    private static void requireSlotIndex(int index) {
        if (index < 0 || index >= SLOT_COUNT) {
            throw new IllegalArgumentException("slot index must be 0.." + (SLOT_COUNT - 1) + ", was " + index);
        }
    }
}
