package dev.echo.standalone.runtime.client;

final class EchoClientInventoryLayout {
    static final int COLUMNS = 9;
    static final int CARRY_ROWS = 3;
    static final int SLOT_SIZE = 40;
    static final int SPACING = 5;
    static final int HOTBAR_GAP = 14;
    static final int EQUIPMENT_SLOT_BASE = 100;
    static final int OFFHAND_SLOT_INDEX = 110;
    static final int CONTAINER_ROWS = 4;
    static final int CONTAINER_SLOT_BASE = 200;

    private EchoClientInventoryLayout() {
    }

    static int gridWidth() {
        return COLUMNS * SLOT_SIZE + (COLUMNS - 1) * SPACING;
    }

    static int panelWidth() {
        return gridWidth() + equipmentColumnWidth() + 46;
    }

    static int containerPanelWidth() {
        return gridWidth() + 48;
    }

    static int panelHeight() {
        return 316;
    }

    static int containerPanelHeight() {
        return 506;
    }

    static int panelX(int width) {
        return (width - panelWidth()) / 2;
    }

    static int containerPanelX(int width) {
        return (width - containerPanelWidth()) / 2;
    }

    static int panelY(int height) {
        return Math.max(84, (height - panelHeight()) / 2);
    }

    static int containerPanelY(int height) {
        return Math.max(32, (height - containerPanelHeight()) / 2);
    }

    static int gridX(int width) {
        return panelX(width) + equipmentColumnWidth() + 28;
    }

    static int containerGridX(int width) {
        return containerPanelX(width) + 24;
    }

    static int containerGridY(int height) {
        return containerPanelY(height) + 58;
    }

    static int containerPlayerCarryY(int height) {
        return containerGridY(height) + CONTAINER_ROWS * (SLOT_SIZE + SPACING) + 42;
    }

    static int containerPlayerHotbarY(int height) {
        return containerPlayerCarryY(height) + CARRY_ROWS * (SLOT_SIZE + SPACING) + HOTBAR_GAP;
    }

    static int carryY(int height) {
        return panelY(height) + 58;
    }

    static int hotbarY(int height) {
        return carryY(height) + CARRY_ROWS * (SLOT_SIZE + SPACING) + HOTBAR_GAP;
    }

    static int equipmentColumnWidth() {
        return SLOT_SIZE + 18;
    }

    static int equipmentX(int width) {
        return panelX(width) + 16;
    }

    static int equipmentY(int height, EchoClientArmorSlot slot) {
        int index = slot == null ? 0 : slot.ordinal();
        return carryY(height) + index * (SLOT_SIZE + SPACING);
    }

    static int equipmentSlotIndex(EchoClientArmorSlot slot) {
        return EQUIPMENT_SLOT_BASE + (slot == null ? 0 : slot.ordinal());
    }

    static int offhandSlotIndex() {
        return OFFHAND_SLOT_INDEX;
    }

    static int offhandY(int height) {
        return carryY(height) + EchoClientArmorSlot.values().length * (SLOT_SIZE + SPACING) + 10;
    }

    static boolean offhandSlotAt(int width, int height, double pointerX, double pointerY) {
        int x = equipmentX(width);
        int y = offhandY(height);
        return pointerX >= x
                && pointerX <= x + SLOT_SIZE
                && pointerY >= y
                && pointerY <= y + SLOT_SIZE;
    }

    static EchoClientArmorSlot equipmentSlotAt(int width, int height, double pointerX, double pointerY) {
        int x = equipmentX(width);
        for (EchoClientArmorSlot slot : EchoClientArmorSlot.values()) {
            int y = equipmentY(height, slot);
            if (pointerX >= x
                    && pointerX <= x + SLOT_SIZE
                    && pointerY >= y
                    && pointerY <= y + SLOT_SIZE) {
                return slot;
            }
        }
        return null;
    }

    static int slotAt(int width, int height, double pointerX, double pointerY) {
        int carrySlot = slotInInventoryGrid(width, carryY(height), pointerX, pointerY, CARRY_ROWS);
        if (carrySlot >= 0) {
            return 9 + carrySlot;
        }
        int hotbarSlot = slotInInventoryGrid(width, hotbarY(height), pointerX, pointerY, 1);
        return hotbarSlot >= 0 ? hotbarSlot : -1;
    }

    static int containerSlotIndex(int slot) {
        return CONTAINER_SLOT_BASE + Math.max(0, slot);
    }

    static int containerSlotAt(int width, int height, double pointerX, double pointerY) {
        return slotInGrid(containerGridX(width), containerGridY(height), pointerX, pointerY, CONTAINER_ROWS);
    }

    static int containerPlayerSlotAt(int width, int height, double pointerX, double pointerY) {
        int carrySlot = slotInGrid(
                containerGridX(width),
                containerPlayerCarryY(height),
                pointerX,
                pointerY,
                CARRY_ROWS
        );
        if (carrySlot >= 0) {
            return 9 + carrySlot;
        }
        int hotbarSlot = slotInGrid(
                containerGridX(width),
                containerPlayerHotbarY(height),
                pointerX,
                pointerY,
                1
        );
        return hotbarSlot >= 0 ? hotbarSlot : -1;
    }

    private static int slotInInventoryGrid(int width, int gridY, double pointerX, double pointerY, int rows) {
        return slotInGrid(gridX(width), gridY, pointerX, pointerY, rows);
    }

    private static int slotInGrid(int gridX, int gridY, double pointerX, double pointerY, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int x = gridX + column * (SLOT_SIZE + SPACING);
                int y = gridY + row * (SLOT_SIZE + SPACING);
                if (pointerX >= x
                        && pointerX <= x + SLOT_SIZE
                        && pointerY >= y
                        && pointerY <= y + SLOT_SIZE) {
                    return row * COLUMNS + column;
                }
            }
        }
        return -1;
    }
}
