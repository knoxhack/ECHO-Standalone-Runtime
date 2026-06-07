package dev.echo.standalone.runtime.client;

import java.util.LinkedHashSet;
import java.util.Set;

final class EchoClientSlotGridController {
    private final EchoClientRuntimeServices runtimeServices;
    private final EchoClientScreenController screens;
    private final Set<Integer> distributedSlots = new LinkedHashSet<>();
    private int dragSlot = -1;
    private DistributionMode distributionMode = DistributionMode.NONE;

    EchoClientSlotGridController(
            EchoClientRuntimeServices runtimeServices,
            EchoClientScreenController screens
    ) {
        this.runtimeServices = runtimeServices;
        this.screens = screens;
    }

    int dragSlot() {
        return runtimeServices.cursorStackHeld() ? dragSlot : -1;
    }

    void clearDrag() {
        dragSlot = -1;
        clearDistribution();
    }

    void update(SlotGridInput input) {
        if (input == null) {
            return;
        }
        if (input.closeRequested()) {
            close();
            return;
        }
        if (runtimeServices.session() == null) {
            clearDrag();
            screens.showMainMenu(runtimeServices.hasContinuableSession());
            return;
        }
        if (!input.primaryDown() && !input.secondaryDown()) {
            clearDistribution();
        }
        if (!input.hasAction()) {
            return;
        }
        if (input.dropRequested()) {
            drop(input);
            return;
        }
        EchoClientArmorSlot equipmentSlot = screens.containerOpen()
                ? null
                : EchoClientInventoryLayout.equipmentSlotAt(
                        input.width(),
                        input.height(),
                        input.pointerX(),
                        input.pointerY()
                );
        if (equipmentSlot != null) {
            if (input.primaryClick() || input.secondaryClick()) {
                boolean changed = runtimeServices.clickEquipmentSlot(equipmentSlot);
                if (changed) {
                    if (runtimeServices.cursorStackHeld()) {
                        dragSlot = EchoClientInventoryLayout.equipmentSlotIndex(equipmentSlot);
                    } else {
                        clearDrag();
                    }
                    runtimeServices.updateWorldSessionFromGameplay();
                }
            }
            return;
        }
        boolean offhandSlot = !screens.containerOpen()
                && EchoClientInventoryLayout.offhandSlotAt(
                        input.width(),
                        input.height(),
                        input.pointerX(),
                        input.pointerY()
                );
        if (offhandSlot) {
            if (input.primaryClick() || input.secondaryClick()) {
                boolean changed = input.secondaryClick()
                        ? runtimeServices.secondaryClickOffhandSlot()
                        : runtimeServices.primaryClickOffhandSlot();
                if (changed) {
                    if (runtimeServices.cursorStackHeld()) {
                        dragSlot = EchoClientInventoryLayout.offhandSlotIndex();
                    } else {
                        clearDrag();
                    }
                    runtimeServices.updateWorldSessionFromGameplay();
                }
            }
            return;
        }
        SlotTarget target = targetAt(input);
        if (target == null) {
            if (!runtimeServices.cursorStackHeld()) {
                clearDrag();
            }
            return;
        }
        if (input.heldOnly()) {
            distributeHeldStack(input, target);
            return;
        }
        if (input.hotbarSlotKey() >= 0) {
            if (runtimeServices.cursorStackHeld()) {
                return;
            }
            clearDrag();
            if (target.container()) {
                runtimeServices.swapContainerSlotWithHotbar(target.slot(), input.hotbarSlotKey());
            } else {
                runtimeServices.swapInventorySlots(target.slot(), input.hotbarSlotKey());
            }
            runtimeServices.updateWorldSessionFromGameplay();
            return;
        }
        if (input.primaryClick() && input.shiftDown()) {
            if (runtimeServices.cursorStackHeld()) {
                return;
            }
            clearDrag();
            if (target.container()) {
                runtimeServices.quickMoveContainerSlotToPlayer(target.slot());
            } else if (screens.containerOpen()) {
                runtimeServices.quickMoveInventorySlotToContainer(target.slot());
            } else {
                runtimeServices.quickMoveInventorySlot(target.slot());
            }
            runtimeServices.updateWorldSessionFromGameplay();
            return;
        }
        boolean changed;
        if (input.secondaryClick()) {
            changed = target.container()
                    ? runtimeServices.secondaryClickContainerSlot(target.slot())
                    : runtimeServices.secondaryClickInventorySlot(target.slot());
        } else {
            changed = target.container()
                    ? runtimeServices.primaryClickContainerSlot(target.slot())
                    : runtimeServices.primaryClickInventorySlot(target.slot());
        }
        if (changed) {
            if (runtimeServices.cursorStackHeld()) {
                if (dragSlot < 0) {
                    dragSlot = target.dragSlotKey();
                }
                beginDistribution(activeDistributionMode(input), target);
            } else {
                clearDrag();
            }
            runtimeServices.updateWorldSessionFromGameplay();
        }
    }

    private void close() {
        if (runtimeServices.cursorStackHeld() && !runtimeServices.returnCursorStackToInventory()) {
            screens.showToast("Inventory full");
            return;
        }
        clearDrag();
        screens.closeSlotGridScreen(runtimeServices.hasActiveWorld());
    }

    private void drop(SlotGridInput input) {
        int quantity = input.dropStackRequested() ? Integer.MAX_VALUE : 1;
        EchoClientDroppedItem drop = null;
        if (runtimeServices.cursorStackHeld()) {
            drop = runtimeServices.dropCursorStack(quantity);
        } else if (!screens.containerOpen()
                && EchoClientInventoryLayout.offhandSlotAt(
                        input.width(),
                        input.height(),
                        input.pointerX(),
                        input.pointerY()
                )) {
            drop = runtimeServices.dropOffhandStack(quantity);
        } else if (!screens.containerOpen()) {
            EchoClientArmorSlot armorSlot = EchoClientInventoryLayout.equipmentSlotAt(
                    input.width(),
                    input.height(),
                    input.pointerX(),
                    input.pointerY()
            );
            if (armorSlot != null) {
                drop = runtimeServices.dropEquipmentSlot(armorSlot);
            }
        }
        if (drop == null) {
            SlotTarget target = targetAt(input);
            if (target != null) {
                drop = target.container()
                        ? runtimeServices.dropContainerSlotStack(target.slot(), quantity)
                        : runtimeServices.dropInventorySlotStack(target.slot(), quantity);
            }
        }
        if (drop != null) {
            if (!runtimeServices.cursorStackHeld()) {
                clearDrag();
            }
            screens.showToast("Dropped " + drop.definition().displayName());
            runtimeServices.updateWorldSessionFromGameplay();
        }
    }

    private void distributeHeldStack(SlotGridInput input, SlotTarget target) {
        if (!runtimeServices.cursorStackHeld()) {
            clearDistribution();
            return;
        }
        DistributionMode mode = activeDistributionMode(input);
        if (mode == DistributionMode.NONE) {
            clearDistribution();
            return;
        }
        if (distributionMode != mode) {
            beginDistributionFromDragSlot(mode);
        }
        int slotKey = target.distributionSlotKey();
        if (distributedSlots.contains(slotKey)) {
            return;
        }
        boolean changed = target.container()
                ? runtimeServices.secondaryClickContainerSlot(target.slot())
                : runtimeServices.secondaryClickInventorySlot(target.slot());
        if (!changed) {
            return;
        }
        distributedSlots.add(slotKey);
        if (dragSlot < 0) {
            dragSlot = target.dragSlotKey();
        }
        if (!runtimeServices.cursorStackHeld()) {
            clearDistribution();
        }
        runtimeServices.updateWorldSessionFromGameplay();
    }

    private void beginDistribution(DistributionMode mode, SlotTarget source) {
        if (mode == DistributionMode.NONE) {
            return;
        }
        if (distributionMode != mode) {
            distributedSlots.clear();
            distributionMode = mode;
        }
        if (source != null) {
            distributedSlots.add(source.distributionSlotKey());
        }
    }

    private void beginDistributionFromDragSlot(DistributionMode mode) {
        if (mode == DistributionMode.NONE) {
            return;
        }
        if (distributionMode != mode) {
            distributedSlots.clear();
            distributionMode = mode;
        }
        if (dragSlot >= 0) {
            distributedSlots.add(dragSlot);
        }
    }

    private void clearDistribution() {
        distributedSlots.clear();
        distributionMode = DistributionMode.NONE;
    }

    private static DistributionMode activeDistributionMode(SlotGridInput input) {
        if (input == null) {
            return DistributionMode.NONE;
        }
        if (input.secondaryDown()) {
            return DistributionMode.SECONDARY;
        }
        if (input.primaryDown()) {
            return DistributionMode.PRIMARY;
        }
        return DistributionMode.NONE;
    }

    private SlotTarget targetAt(SlotGridInput input) {
        if (input == null) {
            return null;
        }
        if (screens.containerOpen()) {
            int containerSlot = EchoClientInventoryLayout.containerSlotAt(
                    input.width(),
                    input.height(),
                    input.pointerX(),
                    input.pointerY()
            );
            if (containerSlot >= 0) {
                return new SlotTarget(SlotSurface.CONTAINER, containerSlot);
            }
            int playerSlot = EchoClientInventoryLayout.containerPlayerSlotAt(
                    input.width(),
                    input.height(),
                    input.pointerX(),
                    input.pointerY()
            );
            return playerSlot >= 0 ? new SlotTarget(SlotSurface.PLAYER, playerSlot) : null;
        }
        int playerSlot = EchoClientInventoryLayout.slotAt(
                input.width(),
                input.height(),
                input.pointerX(),
                input.pointerY()
        );
        return playerSlot >= 0 ? new SlotTarget(SlotSurface.PLAYER, playerSlot) : null;
    }

    record SlotGridInput(
            boolean closeRequested,
            boolean primaryClick,
            boolean secondaryClick,
            boolean primaryDown,
            boolean secondaryDown,
            boolean dropRequested,
            boolean dropStackRequested,
            int hotbarSlotKey,
            boolean shiftDown,
            int width,
            int height,
            double pointerX,
            double pointerY
    ) {
        SlotGridInput {
            primaryDown = primaryDown || primaryClick;
            secondaryDown = secondaryDown || secondaryClick;
            hotbarSlotKey = hotbarSlotKey < 0 ? -1 : Math.min(8, hotbarSlotKey);
            width = Math.max(1, width);
            height = Math.max(1, height);
        }

        SlotGridInput(
                boolean closeRequested,
                boolean primaryClick,
                boolean secondaryClick,
                boolean dropRequested,
                boolean dropStackRequested,
                int hotbarSlotKey,
                boolean shiftDown,
                int width,
                int height,
                double pointerX,
                double pointerY
        ) {
            this(
                    closeRequested,
                    primaryClick,
                    secondaryClick,
                    primaryClick,
                    secondaryClick,
                    dropRequested,
                    dropStackRequested,
                    hotbarSlotKey,
                    shiftDown,
                    width,
                    height,
                    pointerX,
                    pointerY
            );
        }

        SlotGridInput(
                boolean closeRequested,
                boolean primaryClick,
                boolean secondaryClick,
                int hotbarSlotKey,
                boolean shiftDown,
                int width,
                int height,
                double pointerX,
                double pointerY
        ) {
            this(
                    closeRequested,
                    primaryClick,
                    secondaryClick,
                    primaryClick,
                    secondaryClick,
                    false,
                    false,
                    hotbarSlotKey,
                    shiftDown,
                    width,
                    height,
                    pointerX,
                    pointerY
            );
        }

        boolean hasAction() {
            return primaryClick || secondaryClick || primaryDown || secondaryDown || dropRequested || hotbarSlotKey >= 0;
        }

        boolean heldOnly() {
            return (primaryDown || secondaryDown)
                    && !primaryClick
                    && !secondaryClick
                    && !dropRequested
                    && hotbarSlotKey < 0;
        }
    }

    private enum DistributionMode {
        NONE,
        PRIMARY,
        SECONDARY
    }

    private enum SlotSurface {
        PLAYER,
        CONTAINER
    }

    private record SlotTarget(SlotSurface surface, int slot) {
        private boolean container() {
            return surface == SlotSurface.CONTAINER;
        }

        private int dragSlotKey() {
            return container() ? EchoClientInventoryLayout.containerSlotIndex(slot) : slot;
        }

        private int distributionSlotKey() {
            return dragSlotKey();
        }
    }
}
