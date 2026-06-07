package dev.echo.standalone.runtime.client;

record EchoClientHeldItemOverlayPlan(
        int screenWidth,
        int screenHeight,
        int x,
        int y,
        int size,
        int iconX,
        int iconY,
        int iconSize,
        EchoClientSlotStack stack,
        boolean emptyHand
) {
    private static final int OVERLAY_SIZE = 78;
    private static final int ICON_SIZE = 54;

    EchoClientHeldItemOverlayPlan {
        screenWidth = Math.max(1, screenWidth);
        screenHeight = Math.max(1, screenHeight);
        size = Math.max(48, size);
        iconSize = Math.max(24, Math.min(size - 16, iconSize));
        x = clamp(x, 8, Math.max(8, screenWidth - size - 8));
        y = clamp(y, 8, Math.max(8, screenHeight - size - 8));
        iconX = clamp(iconX, x, x + size - iconSize);
        iconY = clamp(iconY, y, y + size - iconSize);
        stack = stack == null ? EchoClientSlotStack.empty(0) : stack;
        emptyHand = emptyHand || stack.empty();
    }

    static EchoClientHeldItemOverlayPlan from(int screenWidth, int screenHeight, EchoClientInventoryScreenModel model) {
        int safeWidth = Math.max(1, screenWidth);
        int safeHeight = Math.max(1, screenHeight);
        EchoClientSlotStack selected = selectedStack(model);
        int hotbarSlotSize = 40;
        int hotbarSpacing = 4;
        int hotbarWidth = EchoClientInventoryLayout.COLUMNS * hotbarSlotSize
                + (EchoClientInventoryLayout.COLUMNS - 1) * hotbarSpacing;
        int hotbarX = (safeWidth - hotbarWidth) / 2;
        int hotbarRight = hotbarX + hotbarWidth;
        int hotbarY = safeHeight - hotbarSlotSize - 16;
        int preferredX = hotbarRight + 22;
        if (preferredX + OVERLAY_SIZE + 12 > safeWidth) {
            preferredX = safeWidth - OVERLAY_SIZE - 24;
        }
        int preferredY = hotbarY - OVERLAY_SIZE - 18;
        if (preferredY < 12) {
            preferredY = Math.max(12, safeHeight - OVERLAY_SIZE - hotbarSlotSize - 26);
        }
        int iconOffset = (OVERLAY_SIZE - ICON_SIZE) / 2;
        return new EchoClientHeldItemOverlayPlan(
                safeWidth,
                safeHeight,
                preferredX,
                preferredY,
                OVERLAY_SIZE,
                preferredX + iconOffset,
                preferredY + iconOffset,
                ICON_SIZE,
                selected,
                selected.empty()
        );
    }

    boolean visible() {
        return screenWidth > 0 && screenHeight > 0;
    }

    boolean itemSlot() {
        return !emptyHand && stack.itemSlot();
    }

    boolean blockSlot() {
        return !emptyHand && stack.blockSlot();
    }

    int bottom() {
        return y + size;
    }

    private static EchoClientSlotStack selectedStack(EchoClientInventoryScreenModel model) {
        if (model == null) {
            return EchoClientSlotStack.empty(0);
        }
        return model.slot(model.selectedSlot());
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
