package dev.echo.standalone.runtime.item;

import java.util.Objects;

public final class EchoInventoryOperations {
    public EchoInventoryOperationResult add(EchoInventoryContainer container, EchoItemStack stack) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(stack, "stack");
        int remaining = stack.quantity();

        synchronized (container) {
            for (EchoInventorySlot slot : container.slots()) {
                if (remaining == 0) {
                    break;
                }
                if (slot.stack().isPresent() && slot.stack().orElseThrow().canMerge(stack)) {
                    EchoItemStack existing = slot.stack().orElseThrow();
                    int moved = Math.min(remaining, existing.spaceRemaining());
                    if (moved > 0) {
                        slot.setStack(existing.add(moved));
                        remaining -= moved;
                    }
                }
            }
            for (EchoInventorySlot slot : container.slots()) {
                if (remaining == 0) {
                    break;
                }
                if (slot.empty()) {
                    int moved = Math.min(remaining, stack.definition().maxStackSize());
                    slot.setStack(new EchoItemStack(stack.definition(), moved));
                    remaining -= moved;
                }
            }
        }

        int inserted = stack.quantity() - remaining;
        if (inserted == stack.quantity()) {
            return new EchoInventoryOperationResult("add", true, inserted, "added");
        }
        if (inserted > 0) {
            return new EchoInventoryOperationResult("add", false, inserted, "partial");
        }
        return new EchoInventoryOperationResult("add", false, 0, "full");
    }

    public EchoInventoryOperationResult consume(EchoInventoryContainer container, EchoItemId itemId, int quantity) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(itemId, "itemId");
        requirePositive(quantity, "quantity");
        synchronized (container) {
            if (container.totalQuantity(itemId) < quantity) {
                return new EchoInventoryOperationResult("consume", false, 0, "insufficient_items");
            }
            int remaining = quantity;
            for (EchoInventorySlot slot : container.slots()) {
                if (remaining == 0) {
                    break;
                }
                if (slot.stack().isEmpty() || !slot.stack().orElseThrow().itemId().equals(itemId)) {
                    continue;
                }
                EchoItemStack stack = slot.stack().orElseThrow();
                int removed = Math.min(remaining, stack.quantity());
                stack.remove(removed).ifPresentOrElse(slot::setStack, slot::clear);
                remaining -= removed;
            }
        }
        return new EchoInventoryOperationResult("consume", true, quantity, "consumed");
    }

    public EchoInventoryTransferResult transfer(
            EchoInventoryContainer source,
            int sourceSlot,
            EchoInventoryContainer target,
            int quantity
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        requirePositive(quantity, "quantity");
        EchoInventorySlot slot = source.slot(sourceSlot);
        if (slot.stack().isEmpty()) {
            return new EchoInventoryTransferResult(source.id(), sourceSlot, target.id(), false, 0, "empty_source");
        }
        EchoItemStack sourceStack = slot.stack().orElseThrow();
        int requested = Math.min(quantity, sourceStack.quantity());
        int available = availableSpace(target, sourceStack.definition());
        if (available == 0) {
            return new EchoInventoryTransferResult(source.id(), sourceSlot, target.id(), false, 0, "target_full");
        }
        int moved = Math.min(requested, available);
        EchoInventoryOperationResult added = add(target, sourceStack.withQuantity(moved));
        if (added.quantity() == 0) {
            return new EchoInventoryTransferResult(source.id(), sourceSlot, target.id(), false, 0, added.reason());
        }
        sourceStack.remove(added.quantity()).ifPresentOrElse(slot::setStack, slot::clear);
        return new EchoInventoryTransferResult(source.id(), sourceSlot, target.id(), true, added.quantity(), "transferred");
    }

    public EchoInventoryTransferResult moveOrMergeSlot(
            EchoInventoryContainer container,
            int sourceSlot,
            int targetSlot
    ) {
        Objects.requireNonNull(container, "container");
        synchronized (container) {
            EchoInventorySlot source = container.slot(sourceSlot);
            EchoInventorySlot target = container.slot(targetSlot);
            if (sourceSlot == targetSlot) {
                return transferResult(container, sourceSlot, false, 0, "same_slot");
            }
            if (source.empty()) {
                return transferResult(container, sourceSlot, false, 0, "empty_source");
            }
            EchoItemStack sourceStack = source.stack().orElseThrow();
            if (target.empty()) {
                target.setStack(sourceStack);
                source.clear();
                return transferResult(container, sourceSlot, true, sourceStack.quantity(), "moved_stack");
            }
            EchoItemStack targetStack = target.stack().orElseThrow();
            if (targetStack.canMerge(sourceStack)) {
                int moved = Math.min(sourceStack.quantity(), targetStack.spaceRemaining());
                if (moved <= 0) {
                    return transferResult(container, sourceSlot, false, 0, "target_stack_full");
                }
                target.setStack(targetStack.add(moved));
                sourceStack.remove(moved).ifPresentOrElse(source::setStack, source::clear);
                return transferResult(container, sourceSlot, true, moved, "merged_stack");
            }
            target.setStack(sourceStack);
            source.setStack(targetStack);
            return transferResult(container, sourceSlot, true, sourceStack.quantity(), "swapped_slots");
        }
    }

    public EchoInventoryTransferResult splitSlotTo(
            EchoInventoryContainer container,
            int sourceSlot,
            int targetSlot
    ) {
        Objects.requireNonNull(container, "container");
        synchronized (container) {
            EchoInventorySlot source = container.slot(sourceSlot);
            EchoInventorySlot target = container.slot(targetSlot);
            if (sourceSlot == targetSlot) {
                return transferResult(container, sourceSlot, false, 0, "same_slot");
            }
            if (source.empty()) {
                return transferResult(container, sourceSlot, false, 0, "empty_source");
            }
            if (!target.empty()) {
                return transferResult(container, sourceSlot, false, 0, "target_occupied");
            }
            EchoItemStack sourceStack = source.stack().orElseThrow();
            if (sourceStack.quantity() < 2) {
                return transferResult(container, sourceSlot, false, 0, "source_stack_too_small");
            }
            int moved = sourceStack.quantity() / 2;
            int remaining = sourceStack.quantity() - moved;
            source.setStack(sourceStack.withQuantity(remaining));
            target.setStack(sourceStack.withQuantity(moved));
            return transferResult(container, sourceSlot, true, moved, "split_stack");
        }
    }

    public EchoInventoryTransferResult swapSlots(
            EchoInventoryContainer container,
            int leftSlot,
            int rightSlot
    ) {
        Objects.requireNonNull(container, "container");
        synchronized (container) {
            EchoInventorySlot left = container.slot(leftSlot);
            EchoInventorySlot right = container.slot(rightSlot);
            if (leftSlot == rightSlot) {
                return transferResult(container, leftSlot, false, 0, "same_slot");
            }
            if (left.empty() && right.empty()) {
                return transferResult(container, leftSlot, false, 0, "empty_slots");
            }
            EchoItemStack leftStack = left.stack().orElse(null);
            EchoItemStack rightStack = right.stack().orElse(null);
            if (rightStack == null) {
                left.clear();
            } else {
                left.setStack(rightStack);
            }
            if (leftStack == null) {
                right.clear();
            } else {
                right.setStack(leftStack);
            }
            int quantity = leftStack == null ? rightStack.quantity() : leftStack.quantity();
            return transferResult(container, leftSlot, true, quantity, "swapped_slots");
        }
    }

    public EchoInventoryTransferResult swapSlots(
            EchoInventoryContainer source,
            int sourceSlot,
            EchoInventoryContainer target,
            int targetSlot
    ) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        if (source == target) {
            return swapSlots(source, sourceSlot, targetSlot);
        }
        EchoInventoryContainer first = orderedFirst(source, target);
        EchoInventoryContainer second = first == source ? target : source;
        synchronized (first) {
            synchronized (second) {
                EchoInventorySlot sourceInventorySlot = source.slot(sourceSlot);
                EchoInventorySlot targetInventorySlot = target.slot(targetSlot);
                if (sourceInventorySlot.empty() && targetInventorySlot.empty()) {
                    return new EchoInventoryTransferResult(
                            source.id(), sourceSlot, target.id(), false, 0, "empty_slots");
                }
                EchoItemStack sourceStack = sourceInventorySlot.stack().orElse(null);
                EchoItemStack targetStack = targetInventorySlot.stack().orElse(null);
                if (targetStack == null) {
                    sourceInventorySlot.clear();
                } else {
                    sourceInventorySlot.setStack(targetStack);
                }
                if (sourceStack == null) {
                    targetInventorySlot.clear();
                } else {
                    targetInventorySlot.setStack(sourceStack);
                }
                int quantity = sourceStack == null ? targetStack.quantity() : sourceStack.quantity();
                return new EchoInventoryTransferResult(
                        source.id(), sourceSlot, target.id(), true, quantity, "swapped_slots");
            }
        }
    }

    public EchoInventoryTransferResult quickMoveSlot(
            EchoInventoryContainer container,
            int sourceSlot,
            int targetStartInclusive,
            int targetEndExclusive
    ) {
        Objects.requireNonNull(container, "container");
        synchronized (container) {
            requireSlotRange(container, targetStartInclusive, targetEndExclusive);
            EchoInventorySlot source = container.slot(sourceSlot);
            if (source.empty()) {
                return transferResult(container, sourceSlot, false, 0, "empty_source");
            }

            EchoItemStack sourceStack = source.stack().orElseThrow();
            int moved = 0;
            for (int index = targetStartInclusive; index < targetEndExclusive; index++) {
                if (index == sourceSlot) {
                    continue;
                }
                EchoInventorySlot target = container.slot(index);
                if (target.empty()) {
                    continue;
                }
                EchoItemStack targetStack = target.stack().orElseThrow();
                if (!targetStack.canMerge(sourceStack)) {
                    continue;
                }
                int accepted = Math.min(sourceStack.quantity(), targetStack.spaceRemaining());
                if (accepted <= 0) {
                    continue;
                }
                target.setStack(targetStack.add(accepted));
                moved += accepted;
                var remaining = sourceStack.remove(accepted);
                if (remaining.isEmpty()) {
                    source.clear();
                    return transferResult(container, sourceSlot, true, moved, "quick_moved_stack");
                }
                sourceStack = remaining.orElseThrow();
                source.setStack(sourceStack);
            }

            for (int index = targetStartInclusive; index < targetEndExclusive; index++) {
                if (index == sourceSlot) {
                    continue;
                }
                EchoInventorySlot target = container.slot(index);
                if (!target.empty()) {
                    continue;
                }
                int quantity = sourceStack.quantity();
                target.setStack(sourceStack);
                source.clear();
                moved += quantity;
                return transferResult(container, sourceSlot, true, moved, "quick_moved_stack");
            }

            if (moved > 0) {
                return transferResult(container, sourceSlot, true, moved, "quick_moved_partial");
            }
            return transferResult(container, sourceSlot, false, 0, "target_range_full");
        }
    }

    public int count(EchoInventoryContainer container, EchoItemId itemId) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(itemId, "itemId");
        return container.totalQuantity(itemId);
    }

    public int availableSpace(EchoInventoryContainer container, EchoItemDefinition definition) {
        Objects.requireNonNull(container, "container");
        Objects.requireNonNull(definition, "definition");
        synchronized (container) {
            int available = 0;
            for (EchoInventorySlot slot : container.slots()) {
                if (slot.empty()) {
                    available += definition.maxStackSize();
                } else if (slot.stack().orElseThrow().itemId().equals(definition.id())) {
                    available += slot.stack().orElseThrow().spaceRemaining();
                }
            }
            return available;
        }
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static void requireSlotRange(EchoInventoryContainer container, int startInclusive, int endExclusive) {
        if (startInclusive < 0 || endExclusive > container.capacity() || startInclusive >= endExclusive) {
            throw new IllegalArgumentException("slot range out of bounds: " + startInclusive + ".." + endExclusive);
        }
    }

    private static EchoInventoryContainer orderedFirst(EchoInventoryContainer left, EchoInventoryContainer right) {
        int comparison = left.id().value().compareTo(right.id().value());
        if (comparison < 0) {
            return left;
        }
        if (comparison > 0) {
            return right;
        }
        return System.identityHashCode(left) <= System.identityHashCode(right) ? left : right;
    }

    private static EchoInventoryTransferResult transferResult(
            EchoInventoryContainer container,
            int sourceSlot,
            boolean success,
            int quantity,
            String reason
    ) {
        return new EchoInventoryTransferResult(container.id(), sourceSlot, container.id(), success, quantity, reason);
    }
}
