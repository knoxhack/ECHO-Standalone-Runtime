package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;

final class EchoClientSlotGridRenderer {
    private final EchoClientFontRenderer font = new EchoClientFontRenderer();
    private final EchoClientNineSliceRenderer panels = new EchoClientNineSliceRenderer();

    void drawHotbar(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            EchoClientLanguageService language,
            int w,
            int h,
            EchoClientInventoryScreenModel model
    ) {
        int slots = 9;
        int slotSize = 40;
        int spacing = 4;
        int totalWidth = slots * slotSize + (slots - 1) * spacing;
        int startX = (w - totalWidth) / 2;
        int y = h - slotSize - 16;

        for (int i = 0; i < slots; i++) {
            int x = startX + i * (slotSize + spacing);
            boolean selected = i == model.selectedSlot();
            drawSlot(hud2d, textures, icons, w, h, x, y, slotSize, model.slot(i), selected, false);
        }

        EchoClientSlotStack selected = model.slot(model.selectedSlot());
        if (!selected.empty()) {
            font.drawCentered(
                    hud2d,
                    localizedLabel(language, selected),
                    w / 2.0f,
                    y - 24,
                    1.0f,
                    0.84f,
                    1.0f,
                    0.96f,
                    0.88f
            );
        }
    }

    void drawInventory(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            EchoClientLanguageService language,
            int w,
            int h,
            EchoClientInventoryScreenModel model,
            EchoClientEquipmentScreenModel equipmentModel,
            int dragSlot,
            EchoClientSlotStack cursorStack,
            double pointerX,
            double pointerY
    ) {
        hud2d.rect(0, 0, w, h, 0.0f, 0.0f, 0.0f, 0.48f);

        int panelX = EchoClientInventoryLayout.panelX(w);
        int panelY = EchoClientInventoryLayout.panelY(h);
        int panelW = EchoClientInventoryLayout.panelWidth();
        int panelH = EchoClientInventoryLayout.panelHeight();
        hud2d.rect(panelX, panelY, panelW, panelH, 0.09f, 0.11f, 0.12f, 0.94f);
        hud2d.rect(panelX, panelY, panelW, 3, 0.58f, 0.92f, 0.86f, 0.72f);
        hud2d.rect(panelX, panelY + panelH - 3, panelW, 3, 0.02f, 0.04f, 0.05f, 1.0f);
        hud2d.rect(panelX, panelY, 3, panelH, 0.18f, 0.38f, 0.36f, 0.90f);
        hud2d.rect(panelX + panelW - 3, panelY, 3, panelH, 0.02f, 0.04f, 0.05f, 1.0f);
        font.drawCentered(hud2d, model.title().toUpperCase(java.util.Locale.ROOT), w / 2.0f, panelY + 18, 2.0f,
                0.84f, 1.0f, 0.96f, 1.0f);

        int gridX = EchoClientInventoryLayout.gridX(w);
        int carryY = EchoClientInventoryLayout.carryY(h);
        int hotbarY = EchoClientInventoryLayout.hotbarY(h);
        int slotSize = EchoClientInventoryLayout.SLOT_SIZE;
        int step = EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING;
        drawEquipmentSlots(hud2d, textures, icons, w, h, equipmentModel, dragSlot);
        drawOffhandSlot(hud2d, textures, icons, w, h, equipmentModel, dragSlot);

        for (int row = 0; row < EchoClientInventoryLayout.CARRY_ROWS; row++) {
            for (int column = 0; column < EchoClientInventoryLayout.COLUMNS; column++) {
                int slotIndex = EchoVoxelPlayerHotbar.CARRY_START + row * EchoClientInventoryLayout.COLUMNS + column;
                drawSlot(
                        hud2d,
                        textures,
                        icons,
                        w,
                        h,
                        gridX + column * step,
                        carryY + row * step,
                        slotSize,
                        model.slot(slotIndex),
                        false,
                        slotIndex == dragSlot
                );
            }
        }

        for (int column = 0; column < EchoClientInventoryLayout.COLUMNS; column++) {
            int slotIndex = column;
            drawSlot(
                    hud2d,
                    textures,
                    icons,
                    w,
                    h,
                    gridX + column * step,
                    hotbarY,
                    slotSize,
                    model.slot(slotIndex),
                    slotIndex == model.selectedSlot(),
                    slotIndex == dragSlot
            );
        }

        boolean cursorHeld = cursorStack != null && !cursorStack.empty();
        String status = cursorHeld
                ? "CURSOR " + localizedLabel(language, cursorStack)
                        + (cursorStack.count() > 1 ? " x" + cursorStack.count() : "")
                : model.title().toUpperCase(java.util.Locale.ROOT);
        font.drawCentered(hud2d, fit(status, Math.max(10, (panelW - 24) / 7)),
                w / 2.0f, panelY + panelH - 26, 1.0f,
                0.58f, 0.92f, 0.86f, 0.90f);

        if (cursorHeld) {
            drawCursorStack(hud2d, textures, icons, w, h, cursorStack, pointerX, pointerY);
        } else {
            drawHoveredSlotTooltip(hud2d, language, w, h, model, equipmentModel, pointerX, pointerY);
        }
    }

    void drawContainer(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            EchoClientLanguageService language,
            int w,
            int h,
            EchoClientInventoryScreenModel containerModel,
            EchoClientInventoryScreenModel playerModel,
            int dragSlot,
            EchoClientSlotStack cursorStack,
            double pointerX,
            double pointerY
    ) {
        hud2d.rect(0, 0, w, h, 0.0f, 0.0f, 0.0f, 0.52f);

        int panelX = EchoClientInventoryLayout.containerPanelX(w);
        int panelY = EchoClientInventoryLayout.containerPanelY(h);
        int panelW = EchoClientInventoryLayout.containerPanelWidth();
        int panelH = EchoClientInventoryLayout.containerPanelHeight();
        hud2d.rect(panelX, panelY, panelW, panelH, 0.08f, 0.10f, 0.11f, 0.96f);
        hud2d.rect(panelX, panelY, panelW, 3, 0.58f, 0.92f, 0.86f, 0.72f);
        hud2d.rect(panelX, panelY + panelH - 3, panelW, 3, 0.02f, 0.04f, 0.05f, 1.0f);
        hud2d.rect(panelX, panelY, 3, panelH, 0.18f, 0.38f, 0.36f, 0.90f);
        hud2d.rect(panelX + panelW - 3, panelY, 3, panelH, 0.02f, 0.04f, 0.05f, 1.0f);
        font.drawCentered(
                hud2d,
                containerModel.title().toUpperCase(java.util.Locale.ROOT),
                w / 2.0f,
                panelY + 18,
                2.0f,
                0.84f,
                1.0f,
                0.96f,
                1.0f
        );

        int gridX = EchoClientInventoryLayout.containerGridX(w);
        int containerY = EchoClientInventoryLayout.containerGridY(h);
        int carryY = EchoClientInventoryLayout.containerPlayerCarryY(h);
        int hotbarY = EchoClientInventoryLayout.containerPlayerHotbarY(h);
        int slotSize = EchoClientInventoryLayout.SLOT_SIZE;
        int step = EchoClientInventoryLayout.SLOT_SIZE + EchoClientInventoryLayout.SPACING;
        hud2d.text("CONTAINER", gridX, containerY - 18, 0.85f, 0.58f, 0.92f, 0.86f, 0.92f);
        hud2d.text("INVENTORY", gridX, carryY - 18, 0.85f, 0.58f, 0.92f, 0.86f, 0.92f);

        for (int row = 0; row < EchoClientInventoryLayout.CONTAINER_ROWS; row++) {
            for (int column = 0; column < EchoClientInventoryLayout.COLUMNS; column++) {
                int slotIndex = row * EchoClientInventoryLayout.COLUMNS + column;
                drawSlot(
                        hud2d,
                        textures,
                        icons,
                        w,
                        h,
                        gridX + column * step,
                        containerY + row * step,
                        slotSize,
                        containerModel.slot(slotIndex),
                        false,
                        EchoClientInventoryLayout.containerSlotIndex(slotIndex) == dragSlot
                );
            }
        }

        for (int row = 0; row < EchoClientInventoryLayout.CARRY_ROWS; row++) {
            for (int column = 0; column < EchoClientInventoryLayout.COLUMNS; column++) {
                int slotIndex = EchoVoxelPlayerHotbar.CARRY_START + row * EchoClientInventoryLayout.COLUMNS + column;
                drawSlot(
                        hud2d,
                        textures,
                        icons,
                        w,
                        h,
                        gridX + column * step,
                        carryY + row * step,
                        slotSize,
                        playerModel.slot(slotIndex),
                        false,
                        slotIndex == dragSlot
                );
            }
        }

        for (int column = 0; column < EchoClientInventoryLayout.COLUMNS; column++) {
            int slotIndex = column;
            drawSlot(
                    hud2d,
                    textures,
                    icons,
                    w,
                    h,
                    gridX + column * step,
                    hotbarY,
                    slotSize,
                    playerModel.slot(slotIndex),
                    slotIndex == playerModel.selectedSlot(),
                    slotIndex == dragSlot
            );
        }

        boolean cursorHeld = cursorStack != null && !cursorStack.empty();
        String status = cursorHeld
                ? "CURSOR " + localizedLabel(language, cursorStack)
                        + (cursorStack.count() > 1 ? " x" + cursorStack.count() : "")
                : containerModel.title().toUpperCase(java.util.Locale.ROOT);
        font.drawCentered(hud2d, fit(status, Math.max(10, (panelW - 24) / 7)),
                w / 2.0f, panelY + panelH - 26, 1.0f,
                0.58f, 0.92f, 0.86f, 0.90f);

        if (cursorHeld) {
            drawCursorStack(hud2d, textures, icons, w, h, cursorStack, pointerX, pointerY);
        } else {
            drawHoveredContainerTooltip(hud2d, language, w, h, containerModel, playerModel, pointerX, pointerY);
        }
    }

    void drawWorkbench(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            EchoClientLanguageService language,
            int width,
            int height,
            EchoClientScreenSnapshot screen,
            EchoClientWorkbenchScreenModel model
    ) {
        hud2d.rect(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.55f);
        int panelW = Math.min(width - 48, 760);
        int panelH = Math.min(height - 96, 430);
        int panelX = (width - panelW) / 2;
        int panelY = Math.max(54, (height - panelH) / 2);
        panels.panel(hud2d, panelX, panelY, panelW, panelH, 0.06f, 0.09f, 0.10f, 0.96f, true);
        font.drawCentered(hud2d, model.title().toUpperCase(java.util.Locale.ROOT),
                width / 2.0f, panelY + 20, 2.0f, 0.84f, 1.0f, 0.96f, 1.0f);

        int splitX = panelX + Math.min(310, panelW / 2);
        drawRecipeList(hud2d, panelX + 18, panelY + 54, splitX - panelX - 28, panelH - 104, screen, model);
        drawRecipeDetail(hud2d, textures, icons, language,
                width, height, splitX + 18, panelY + 56, panelX + panelW - splitX - 36, panelH - 108,
                model.selectedRecipe());

        font.drawCentered(hud2d, screen.footer(), width / 2.0f, height - 28, 1.0f,
                0.55f, 0.75f, 0.72f, 0.9f);
        if (screen.toast().visible()) {
            drawToast(hud2d, width, screen.toast());
        }
    }

    private void drawRecipeList(
            EchoClientHud2D hud2d,
            int x,
            int y,
            int width,
            int height,
            EchoClientScreenSnapshot screen,
            EchoClientWorkbenchScreenModel model
    ) {
        hud2d.text("RECIPES", x, y - 18, 1.0f, 0.58f, 0.92f, 0.86f, 0.95f);
        if (model.recipes().isEmpty()) {
            hud2d.text("No recipes loaded", x, y + 12, 1.0f, 0.62f, 0.76f, 0.72f, 0.90f);
            return;
        }
        int rowH = 30;
        int gap = 7;
        int visibleRows = Math.max(1, Math.min(model.recipes().size(), height / (rowH + gap)));
        int start = Math.max(0, Math.min(screen.scrollOffset(), Math.max(0, model.recipes().size() - visibleRows)));
        int end = Math.min(model.recipes().size(), start + visibleRows);
        for (int i = start; i < end; i++) {
            EchoClientWorkbenchRecipeSummary recipe = model.recipes().get(i);
            int rowY = y + (i - start) * (rowH + gap);
            boolean selected = recipe.recipeId().equals(model.selectedRecipe().recipeId())
                    || i == screen.selectedIndex();
            float alpha = recipe.craftable() ? 0.90f : 0.38f;
            panels.panel(hud2d, x, rowY, width, rowH, 0.06f, 0.10f, 0.12f, alpha, selected);
            String label = fit(recipe.craftable() ? recipe.label() : recipe.label() + " (missing)", Math.max(8, width / 7));
            font.drawCentered(hud2d, label, x + width / 2.0f, rowY + 8, 1.0f,
                    recipe.craftable() ? 0.84f : 0.50f,
                    recipe.craftable() ? 1.0f : 0.58f,
                    recipe.craftable() ? 0.96f : 0.58f,
                    alpha);
        }
    }

    private void drawRecipeDetail(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            EchoClientLanguageService language,
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int width,
            int height,
            EchoClientWorkbenchRecipeDetail recipe
    ) {
        hud2d.text("DETAIL", x, y - 18, 1.0f, 0.58f, 0.92f, 0.86f, 0.95f);
        panels.panel(hud2d, x, y, width, height, 0.035f, 0.065f, 0.075f, 0.82f, false);
        hud2d.text(fit(recipe.label(), Math.max(10, (width - 24) / 7)), x + 14, y + 16, 1.0f,
                0.84f, 1.0f, 0.96f, 0.98f);
        hud2d.text(fit(recipe.recipeId(), Math.max(10, (width - 24) / 7)), x + 14, y + 34, 0.75f,
                0.52f, 0.82f, 0.78f, 0.92f);

        int slotSize = 40;
        int ingredientX = x + 16;
        int ingredientY = y + 76;
        hud2d.text("INGREDIENTS", ingredientX, ingredientY - 18, 0.75f, 0.62f, 0.88f, 0.82f, 0.92f);
        int columns = Math.max(1, Math.min(3, Math.max(1, (width - 92) / (slotSize + 8))));
        for (int i = 0; i < recipe.ingredients().size(); i++) {
            int column = i % columns;
            int row = i / columns;
            drawSlot(
                    hud2d,
                    textures,
                    icons,
                    screenWidth,
                    screenHeight,
                    ingredientX + column * (slotSize + 8),
                    ingredientY + row * (slotSize + 8),
                    slotSize,
                    recipe.ingredients().get(i),
                    false,
                    false
            );
        }

        int outputX = x + Math.max(16, width - 58);
        int outputY = ingredientY + 22;
        hud2d.text("OUTPUT", outputX - 2, outputY - 40, 0.75f, 0.62f, 0.88f, 0.82f, 0.92f);
        drawSlot(hud2d, textures, icons, screenWidth, screenHeight, outputX, outputY, slotSize,
                recipe.output(), recipe.craftable(), false);

        int statusY = y + height - 58;
        hud2d.rect(x + 12, statusY - 8, width - 24, 30,
                recipe.craftable() ? 0.04f : 0.12f,
                recipe.craftable() ? 0.14f : 0.07f,
                recipe.craftable() ? 0.12f : 0.07f,
                0.82f);
        hud2d.text(fit(recipe.status(), Math.max(10, (width - 36) / 7)), x + 18, statusY, 1.0f,
                recipe.craftable() ? 0.70f : 0.90f,
                recipe.craftable() ? 1.0f : 0.62f,
                recipe.craftable() ? 0.90f : 0.58f,
                0.96f);
    }

    private void drawSlot(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int slotSize,
            EchoClientSlotStack slot,
            boolean selected,
            boolean dragSource
    ) {
        if (selected) {
            hud2d.rect(x, y, slotSize, slotSize, 0.9f, 0.9f, 0.9f, 0.9f);
        } else if (dragSource) {
            hud2d.rect(x, y, slotSize, slotSize, 0.18f, 0.74f, 0.66f, 0.95f);
        } else if (!slot.empty()) {
            hud2d.rect(x, y, slotSize, slotSize, 0.2f, 0.2f, 0.2f, 0.7f);
        } else {
            hud2d.rect(x, y, slotSize, slotSize, 0.1f, 0.1f, 0.1f, 0.5f);
        }

        float br = selected ? 0.4f : dragSource ? 0.15f : 0.3f;
        float bg = selected ? 0.8f : dragSource ? 0.95f : 0.3f;
        float bb = selected ? 0.6f : dragSource ? 0.82f : 0.3f;
        float ba = selected || dragSource ? 1.0f : 0.6f;
        hud2d.rect(x, y, slotSize, 2, br, bg, bb, ba);
        hud2d.rect(x, y + slotSize - 2, slotSize, 2, br, bg, bb, ba);
        hud2d.rect(x, y, 2, slotSize, br, bg, bb, ba);
        hud2d.rect(x + slotSize - 2, y, 2, slotSize, br, bg, bb, ba);

        if (slot.blockSlot()) {
            drawBlockSlotContent(hud2d, textures, icons, screenWidth, screenHeight, x, y, slotSize, slot);
        } else if (slot.itemSlot()) {
            drawItemSlotContent(hud2d, textures, icons, screenWidth, screenHeight, x, y, slotSize, slot);
        }
    }

    private void drawEquipmentSlots(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            EchoClientEquipmentScreenModel equipmentModel,
            int dragSlot
    ) {
        if (equipmentModel == null) {
            return;
        }
        int x = EchoClientInventoryLayout.equipmentX(screenWidth);
        for (EchoClientArmorSlot armorSlot : EchoClientArmorSlot.values()) {
            int y = EchoClientInventoryLayout.equipmentY(screenHeight, armorSlot);
            int slotIndex = EchoClientInventoryLayout.equipmentSlotIndex(armorSlot);
            drawSlot(
                    hud2d,
                    textures,
                    icons,
                    screenWidth,
                    screenHeight,
                    x,
                    y,
                    EchoClientInventoryLayout.SLOT_SIZE,
                    equipmentModel.slot(armorSlot),
                    false,
                    slotIndex == dragSlot
            );
            if (equipmentModel.slot(armorSlot).empty()) {
                font.drawCentered(hud2d, armorSlot.id().substring(0, 1).toUpperCase(java.util.Locale.ROOT),
                        x + EchoClientInventoryLayout.SLOT_SIZE / 2.0f,
                        y + 13,
                        1.0f,
                        0.40f,
                        0.56f,
                        0.54f,
                        0.72f);
            }
        }
    }

    private void drawOffhandSlot(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            EchoClientEquipmentScreenModel equipmentModel,
            int dragSlot
    ) {
        if (equipmentModel == null) {
            return;
        }
        int x = EchoClientInventoryLayout.equipmentX(screenWidth);
        int y = EchoClientInventoryLayout.offhandY(screenHeight);
        drawSlot(
                hud2d,
                textures,
                icons,
                screenWidth,
                screenHeight,
                x,
                y,
                EchoClientInventoryLayout.SLOT_SIZE,
                equipmentModel.offhandSlot(),
                false,
                EchoClientInventoryLayout.offhandSlotIndex() == dragSlot
        );
        if (equipmentModel.offhandSlot().empty()) {
            font.drawCentered(hud2d, "O",
                    x + EchoClientInventoryLayout.SLOT_SIZE / 2.0f,
                    y + 13,
                    1.0f,
                    0.40f,
                    0.56f,
                    0.54f,
                    0.72f);
        }
    }

    private void drawBlockSlotContent(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int slotSize,
        EchoClientSlotStack slot
    ) {
        int pad = 6;
        int textureId = icons == null ? 0 : icons.cachedOrQueueBlockIcon(slot.block());
        if (textureId != 0 && textures != null) {
            hud2d.flush();
            textures.begin(screenWidth, screenHeight);
            textures.draw(textureId, x + pad, y + pad, slotSize - pad * 2, slotSize - pad * 2, 1.0f);
            textures.end();
        } else {
            int argb = slot.block().argb();
            float r = ((argb >>> 16) & 0xFF) / 255.0f;
            float g = ((argb >>> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;
            hud2d.rect(x + pad, y + pad, slotSize - pad * 2, slotSize - pad * 2, r, g, b, 1.0f);
            hud2d.rect(x + pad + 3, y + pad + 3, slotSize - pad * 2 - 6, 4,
                    1.0f, 1.0f, 1.0f, 0.16f);
        }
        drawCount(hud2d, x, y, slotSize, slot.count());
    }

    private void drawItemSlotContent(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            int x,
            int y,
            int slotSize,
        EchoClientSlotStack slot
    ) {
        int pad = 6;
        int textureId = icons == null ? 0 : icons.cachedOrQueueItemIcon(slot.runtimeId(), slot.itemModelPredicates());
        if (textureId != 0 && textures != null) {
            hud2d.flush();
            textures.begin(screenWidth, screenHeight);
            textures.draw(textureId, x + pad, y + pad, slotSize - pad * 2, slotSize - pad * 2, 1.0f);
            textures.end();
        } else {
            int argb = fallbackItemColor(slot.runtimeId());
            float r = ((argb >>> 16) & 0xFF) / 255.0f;
            float g = ((argb >>> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;
            hud2d.rect(x + pad, y + pad, slotSize - pad * 2, slotSize - pad * 2, r, g, b, 1.0f);
            hud2d.rect(x + pad + 4, y + pad + 4, slotSize - pad * 2 - 8, 4,
                    1.0f, 1.0f, 1.0f, 0.18f);
        }
        drawDurabilityBar(hud2d, x, y, slotSize, slot);
        drawCount(hud2d, x, y, slotSize, slot.count());
    }

    private void drawDurabilityBar(EchoClientHud2D hud2d, int x, int y, int slotSize, EchoClientSlotStack slot) {
        if (slot == null || !slot.durabilityTracked()) {
            return;
        }
        int barX = x + 6;
        int barY = y + slotSize - 7;
        int barW = slotSize - 12;
        int fillW = Math.max(0, Math.min(barW,
                Math.round(barW * (slot.durability() / (float) slot.maxDurability()))));
        float ratio = slot.durability() / (float) slot.maxDurability();
        float r = ratio > 0.5f ? 0.32f : 0.92f;
        float g = ratio > 0.5f ? 0.88f : ratio > 0.25f ? 0.76f : 0.20f;
        float b = ratio > 0.5f ? 0.36f : 0.16f;
        hud2d.rect(barX, barY, barW, 3, 0.02f, 0.02f, 0.02f, 0.82f);
        if (fillW > 0) {
            hud2d.rect(barX, barY, fillW, 3, r, g, b, 0.96f);
        }
    }

    private void drawCount(EchoClientHud2D hud2d, int x, int y, int slotSize, int countValue) {
        if (countValue <= 1) {
            return;
        }
        String count = Integer.toString(countValue);
        hud2d.text(count, x + slotSize - 7 - count.length() * 6, y + slotSize - 12,
                1.0f, 1.0f, 1.0f, 1.0f, 0.95f);
    }

    private void drawCursorStack(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            EchoClientSlotIconCache icons,
            int screenWidth,
            int screenHeight,
            EchoClientSlotStack cursorStack,
            double pointerX,
            double pointerY
    ) {
        if (cursorStack == null || cursorStack.empty()) {
            return;
        }
        int slotSize = EchoClientInventoryLayout.SLOT_SIZE;
        int x = (int) Math.round(pointerX) - slotSize / 2;
        int y = (int) Math.round(pointerY) - slotSize / 2;
        hud2d.rect(x + 3, y + 3, slotSize, slotSize, 0.0f, 0.0f, 0.0f, 0.36f);
        drawSlot(
                hud2d,
                textures,
                icons,
                screenWidth,
                screenHeight,
                x,
                y,
                slotSize,
                cursorStack,
                false,
                true
        );
    }

    private void drawHoveredSlotTooltip(
            EchoClientHud2D hud2d,
            EchoClientLanguageService language,
            int width,
            int height,
            EchoClientInventoryScreenModel model,
            EchoClientEquipmentScreenModel equipmentModel,
            double pointerX,
            double pointerY
    ) {
        if (equipmentModel != null) {
            if (EchoClientInventoryLayout.offhandSlotAt(width, height, pointerX, pointerY)) {
                EchoClientSlotStack offhandStack = equipmentModel.offhandSlot();
                if (!offhandStack.empty()) {
                    drawTooltipForSlot(hud2d, language, width, height, offhandStack, pointerX, pointerY);
                }
                return;
            }
            EchoClientArmorSlot armorSlot =
                    EchoClientInventoryLayout.equipmentSlotAt(width, height, pointerX, pointerY);
            if (armorSlot != null) {
                EchoClientSlotStack equipmentStack = equipmentModel.slot(armorSlot);
                if (!equipmentStack.empty()) {
                    drawTooltipForSlot(hud2d, language, width, height, equipmentStack, pointerX, pointerY);
                }
                return;
            }
        }
        int slotIndex = EchoClientInventoryLayout.slotAt(width, height, pointerX, pointerY);
        if (slotIndex < 0) {
            return;
        }
        EchoClientSlotStack slot = model.slot(slotIndex);
        if (slot.empty()) {
            return;
        }
        drawTooltipForSlot(hud2d, language, width, height, slot, pointerX, pointerY);
    }

    private void drawHoveredContainerTooltip(
            EchoClientHud2D hud2d,
            EchoClientLanguageService language,
            int width,
            int height,
            EchoClientInventoryScreenModel containerModel,
            EchoClientInventoryScreenModel playerModel,
            double pointerX,
            double pointerY
    ) {
        int containerSlot = EchoClientInventoryLayout.containerSlotAt(width, height, pointerX, pointerY);
        if (containerSlot >= 0) {
            EchoClientSlotStack slot = containerModel.slot(containerSlot);
            if (!slot.empty()) {
                drawTooltipForSlot(hud2d, language, width, height, slot, pointerX, pointerY);
            }
            return;
        }
        int playerSlot = EchoClientInventoryLayout.containerPlayerSlotAt(width, height, pointerX, pointerY);
        if (playerSlot < 0) {
            return;
        }
        EchoClientSlotStack slot = playerModel.slot(playerSlot);
        if (slot.empty()) {
            return;
        }
        drawTooltipForSlot(hud2d, language, width, height, slot, pointerX, pointerY);
    }

    private void drawTooltipForSlot(
            EchoClientHud2D hud2d,
            EchoClientLanguageService language,
            int width,
            int height,
            EchoClientSlotStack slot,
            double pointerX,
            double pointerY
    ) {
        String label = localizedLabel(language, slot);
        if (slot.count() > 1) {
            label += " x" + slot.count();
        }
        java.util.ArrayList<String> lines = new java.util.ArrayList<>();
        lines.add(label);
        lines.addAll(slot.tooltipLines());
        int maxWidth = Math.max(96, width - 16);
        int maxChars = Math.max(8, (maxWidth - 18) / 7);
        int longest = lines.stream()
                .map(line -> fit(line, maxChars))
                .mapToInt(String::length)
                .max()
                .orElse(label.length());
        int tooltipW = Math.min(maxWidth, Math.max(104, longest * 7 + 18));
        int lineH = 13;
        int tooltipH = 14 + lines.size() * lineH;
        int x = Math.min(width - tooltipW - 8, Math.max(8, (int) pointerX + 14));
        int y = Math.min(height - tooltipH - 8, Math.max(8, (int) pointerY - 10));
        hud2d.rect(x, y, tooltipW, tooltipH, 0.02f, 0.04f, 0.05f, 0.96f);
        hud2d.rect(x, y, tooltipW, 1, 0.42f, 0.86f, 0.76f, 0.88f);
        hud2d.rect(x, y + tooltipH - 1, tooltipW, 1, 0.08f, 0.20f, 0.19f, 0.96f);
        hud2d.rect(x, y, 1, tooltipH, 0.18f, 0.42f, 0.38f, 0.92f);
        hud2d.rect(x + tooltipW - 1, y, 1, tooltipH, 0.08f, 0.20f, 0.19f, 0.96f);
        for (int index = 0; index < lines.size(); index++) {
            boolean title = index == 0;
            hud2d.text(fit(lines.get(index), maxChars), x + 9, y + 8 + index * lineH, 1.0f,
                    title ? 0.84f : 0.62f,
                    title ? 1.0f : 0.80f,
                    title ? 0.96f : 0.76f,
                    0.96f);
        }
    }

    private static String localizedLabel(EchoClientLanguageService language, EchoClientSlotStack slot) {
        if (language == null) {
            return slot.label();
        }
        if (slot.blockSlot()) {
            return language.blockName(slot.block());
        }
        if (slot.itemSlot()) {
            return language.itemName(slot.runtimeId(), slot.label());
        }
        return slot.label();
    }

    private static int fallbackItemColor(String itemId) {
        int hash = itemId == null ? 0 : itemId.hashCode();
        int r = 72 + Math.floorMod(hash, 96);
        int g = 96 + Math.floorMod(hash >>> 8, 96);
        int b = 112 + Math.floorMod(hash >>> 16, 80);
        return (r << 16) | (g << 8) | b;
    }

    private void drawToast(EchoClientHud2D hud2d, int width, EchoClientToastSnapshot toast) {
        int toastW = Math.min(360, Math.max(260, width / 3));
        int toastH = 42;
        int x = width - toastW - 24;
        int y = 64;
        panels.panel(hud2d, x, y, toastW, toastH, 0.04f, 0.08f, 0.09f, 0.94f, false);
        hud2d.rect(x + 4, y + toastH - 6, (toastW - 8) * (float) toast.progress(), 2,
                0.22f, 0.82f, 0.72f, 0.88f);
        font.drawCentered(hud2d, toast.message(), x + toastW / 2.0f, y + 13, 1.0f,
                0.76f, 1.0f, 0.94f, 0.98f);
    }

    private static String fit(String text, int maxChars) {
        String normalized = text == null ? "" : text.strip();
        if (normalized.length() <= maxChars) {
            return normalized;
        }
        if (maxChars <= 3) {
            return normalized.substring(0, Math.max(0, maxChars));
        }
        return normalized.substring(0, maxChars - 3) + "...";
    }
}
