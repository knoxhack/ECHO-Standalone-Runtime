package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

import java.util.List;

/**
 * OpenGL HUD: crosshair, hotbar, pause overlay.
 * Uses {@link EchoClientHud2D} for core-profile compliance.
 */
final class EchoClientHud {
    private static final int SLOT_ICON_PREWARM_BUDGET_PER_FRAME = 4;

    private final EchoClientHud2D hud2d = new EchoClientHud2D();
    private final EchoClientHudTextureRenderer textures = new EchoClientHudTextureRenderer();
    private final EchoClientSlotIconCache icons = new EchoClientSlotIconCache();
    private final EchoClientUiRenderer uiRenderer = new EchoClientUiRenderer();
    private final EchoClientSlotGridRenderer slotGridRenderer = new EchoClientSlotGridRenderer();
    private EchoClientLanguageService language = new EchoClientLanguageService(null);

    void setMinecraftAssets(EchoMinecraftAssetResolver minecraftAssets) {
        icons.setMinecraftAssets(minecraftAssets);
    }

    void setLanguage(EchoClientLanguageService language) {
        this.language = language == null ? new EchoClientLanguageService(null) : language;
    }

    void render(int width, int height, EchoVoxelPlayerState player,
                EchoClientInventoryScreenModel hotbarModel, EchoClientPlayerVitals vitals, EchoClientPlayerCombatState combatState,
                EchoClientProgressionState progressionState, EchoClientHazardState hazardState,
                EchoClientToolStatus toolStatus, EchoVoxelHit target,
                boolean paused, String debugLine, List<EchoClientSubtitleLine> subtitles) {
        queueSlotIcons(hotbarModel);
        icons.prewarmQueuedIcons(SLOT_ICON_PREWARM_BUDGET_PER_FRAME);
        hud2d.begin(width, height);

        drawCrosshair(width, height);
        drawArmor(width, height, combatState);
        drawHealth(width, height, vitals);
        drawHunger(width, height, vitals);
        drawExperience(width, height, progressionState);
        drawHazard(width, height, hazardState);
        drawTool(width, height, toolStatus);
        drawHotbar(width, height, hotbarModel);
        drawHeldItem(width, height, hotbarModel);
        drawDebugOverlay(width, debugLine);
        drawSubtitles(width, height, subtitles);

        if (paused) {
            drawPauseOverlay(width, height);
        }

        hud2d.end();
    }

    void renderShell(int width, int height, EchoClientScreenSnapshot screen) {
        hud2d.begin(width, height);
        uiRenderer.renderShell(hud2d, textures, width, height, screen);
        hud2d.end();
    }

    void renderOverlay(
            int width,
            int height,
            EchoClientScreenSnapshot screen,
            EchoClientInventoryScreenModel inventoryModel,
            EchoClientInventoryScreenModel containerModel,
            EchoClientEquipmentScreenModel equipmentModel,
            EchoClientWorkbenchScreenModel workbenchModel,
            int inventoryDragSlot,
            EchoClientSlotStack cursorStack,
            double pointerX,
            double pointerY
    ) {
        queueSlotIcons(inventoryModel);
        queueSlotIcons(containerModel);
        queueSlotIcons(equipmentModel);
        queueSlotIcons(workbenchModel);
        icons.queueSlotIcon(cursorStack);
        icons.prewarmQueuedIcons(SLOT_ICON_PREWARM_BUDGET_PER_FRAME);
        hud2d.begin(width, height);
        if (screen.kind() == EchoClientScreenKind.CONTAINER
                && inventoryModel != null
                && containerModel != null) {
            slotGridRenderer.drawContainer(
                    hud2d,
                    textures,
                    icons,
                    language,
                    width,
                    height,
                    containerModel,
                    inventoryModel,
                    inventoryDragSlot,
                    cursorStack,
                    pointerX,
                    pointerY
            );
        } else if (screen.kind() == EchoClientScreenKind.INVENTORY && inventoryModel != null) {
            slotGridRenderer.drawInventory(
                    hud2d,
                    textures,
                    icons,
                    language,
                    width,
                    height,
                    inventoryModel,
                    equipmentModel,
                    inventoryDragSlot,
                    cursorStack,
                    pointerX,
                    pointerY
            );
        } else if (screen.kind() == EchoClientScreenKind.WORKBENCH && workbenchModel != null) {
            slotGridRenderer.drawWorkbench(
                    hud2d,
                    textures,
                    icons,
                    language,
                    width,
                    height,
                    screen,
                    workbenchModel
            );
        } else {
            uiRenderer.renderOverlay(hud2d, textures, width, height, screen);
        }
        hud2d.end();
    }

    private void drawCrosshair(int w, int h) {
        hud2d.cross(w / 2.0f, h / 2.0f, 10.0f, 1.0f, 1.0f, 1.0f, 0.85f, 2.0f);
    }

    private void drawHotbar(int w, int h, EchoClientInventoryScreenModel hotbarModel) {
        slotGridRenderer.drawHotbar(hud2d, textures, icons, language, w, h, hotbarModel);
    }

    private void drawHeldItem(int w, int h, EchoClientInventoryScreenModel hotbarModel) {
        EchoClientHeldItemOverlayPlan plan = EchoClientHeldItemOverlayPlan.from(w, h, hotbarModel);
        if (!plan.visible()) {
            return;
        }
        hud2d.rect(plan.x() + 8, plan.y() + 10, plan.size() - 10, plan.size() - 4,
                0.0f, 0.0f, 0.0f, 0.26f);
        hud2d.rect(plan.x() + 4, plan.y() + 4, plan.size() - 8, plan.size() - 8,
                0.06f, 0.07f, 0.08f, 0.52f);
        hud2d.rect(plan.x() + 4, plan.y() + 4, plan.size() - 8, 2,
                0.58f, 0.92f, 0.86f, 0.34f);
        if (plan.emptyHand()) {
            drawHandSilhouette(plan);
        } else if (plan.blockSlot()) {
            drawHeldBlock(plan);
        } else if (plan.itemSlot()) {
            drawHeldItemIcon(plan);
        }
    }

    private void drawHeldBlock(EchoClientHeldItemOverlayPlan plan) {
        int textureId = icons.cachedOrQueueBlockIcon(plan.stack().block());
        if (textureId != 0) {
            hud2d.flush();
            textures.begin(plan.screenWidth(), plan.screenHeight());
            textures.draw(textureId, plan.iconX(), plan.iconY(), plan.iconSize(), plan.iconSize(), 1.0f);
            textures.end();
            return;
        }
        int argb = plan.stack().block().argb();
        float r = ((argb >>> 16) & 0xFF) / 255.0f;
        float g = ((argb >>> 8) & 0xFF) / 255.0f;
        float b = (argb & 0xFF) / 255.0f;
        hud2d.rect(plan.iconX(), plan.iconY(), plan.iconSize(), plan.iconSize(), r, g, b, 0.96f);
        hud2d.rect(plan.iconX() + 5, plan.iconY() + 5, plan.iconSize() - 10, 6,
                1.0f, 1.0f, 1.0f, 0.16f);
    }

    private void drawHeldItemIcon(EchoClientHeldItemOverlayPlan plan) {
        int textureId = icons.cachedOrQueueItemIcon(plan.stack().runtimeId(), plan.stack().itemModelPredicates());
        if (textureId != 0) {
            hud2d.flush();
            textures.begin(plan.screenWidth(), plan.screenHeight());
            textures.draw(textureId, plan.iconX(), plan.iconY(), plan.iconSize(), plan.iconSize(), 1.0f);
            textures.end();
        } else {
            int argb = fallbackItemColor(plan.stack().runtimeId());
            float r = ((argb >>> 16) & 0xFF) / 255.0f;
            float g = ((argb >>> 8) & 0xFF) / 255.0f;
            float b = (argb & 0xFF) / 255.0f;
            hud2d.rect(plan.iconX(), plan.iconY(), plan.iconSize(), plan.iconSize(), r, g, b, 0.96f);
            hud2d.rect(plan.iconX() + 6, plan.iconY() + 6, plan.iconSize() - 12, 6,
                    1.0f, 1.0f, 1.0f, 0.18f);
        }
        if (plan.stack().durabilityTracked()) {
            int barW = plan.iconSize() - 10;
            int fillW = Math.max(0, Math.min(barW,
                    Math.round(barW * (plan.stack().durability() / (float) plan.stack().maxDurability()))));
            int barX = plan.iconX() + 5;
            int barY = plan.iconY() + plan.iconSize() - 7;
            hud2d.rect(barX, barY, barW, 4, 0.01f, 0.01f, 0.01f, 0.82f);
            if (fillW > 0) {
                hud2d.rect(barX, barY, fillW, 4, 0.28f, 0.88f, 0.36f, 0.94f);
            }
        }
    }

    private void drawHandSilhouette(EchoClientHeldItemOverlayPlan plan) {
        int x = plan.x();
        int y = plan.y();
        hud2d.rect(x + 30, y + 28, 20, 38, 0.62f, 0.57f, 0.52f, 0.94f);
        hud2d.rect(x + 24, y + 34, 12, 30, 0.56f, 0.52f, 0.49f, 0.94f);
        hud2d.rect(x + 45, y + 34, 10, 28, 0.55f, 0.51f, 0.48f, 0.92f);
        hud2d.rect(x + 20, y + 54, 40, 14, 0.48f, 0.45f, 0.43f, 0.96f);
        hud2d.rect(x + 27, y + 31, 8, 4, 0.78f, 0.74f, 0.68f, 0.30f);
    }

    private static int fallbackItemColor(String itemId) {
        int hash = itemId == null ? 0 : itemId.hashCode();
        int r = 72 + Math.floorMod(hash, 96);
        int g = 96 + Math.floorMod(hash >>> 8, 96);
        int b = 112 + Math.floorMod(hash >>> 16, 80);
        return (r << 16) | (g << 8) | b;
    }

    private void queueSlotIcons(EchoClientInventoryScreenModel model) {
        icons.queueSlotIcons(model);
    }

    private void queueSlotIcons(EchoClientEquipmentScreenModel model) {
        icons.queueSlotIcons(model);
    }

    private void queueSlotIcons(EchoClientWorkbenchScreenModel model) {
        icons.queueSlotIcons(model);
    }

    private void drawHealth(int w, int h, EchoClientPlayerVitals vitals) {
        int[] hearts = heartFillStates(vitals);
        if (hearts.length == 0) {
            return;
        }
        float x = Math.max(8.0f, w / 2.0f - EchoClientInventoryLayout.gridWidth() / 2.0f);
        float y = Math.max(8.0f, h - 94.0f);
        for (int i = 0; i < hearts.length; i++) {
            float hx = x + i * 12.0f;
            hud2d.rect(hx, y, 9.0f, 9.0f, 0.06f, 0.02f, 0.02f, 0.78f);
            hud2d.rect(hx + 1.0f, y + 1.0f, 7.0f, 7.0f, 0.18f, 0.08f, 0.08f, 0.88f);
            if (hearts[i] >= 2) {
                hud2d.rect(hx + 2.0f, y + 2.0f, 5.0f, 5.0f, 0.88f, 0.12f, 0.12f, 0.95f);
                hud2d.rect(hx + 3.0f, y + 3.0f, 2.0f, 2.0f, 1.0f, 0.46f, 0.40f, 0.92f);
            } else if (hearts[i] == 1) {
                hud2d.rect(hx + 2.0f, y + 2.0f, 2.5f, 5.0f, 0.88f, 0.12f, 0.12f, 0.95f);
            }
        }
    }

    private void drawArmor(int w, int h, EchoClientPlayerCombatState combatState) {
        int[] armor = armorFillStates(combatState);
        if (armor.length == 0) {
            return;
        }
        float x = Math.max(8.0f, w / 2.0f - EchoClientInventoryLayout.gridWidth() / 2.0f);
        float y = Math.max(8.0f, h - 108.0f);
        for (int i = 0; i < armor.length; i++) {
            float ax = x + i * 12.0f;
            hud2d.rect(ax, y, 9.0f, 9.0f, 0.03f, 0.05f, 0.07f, 0.74f);
            hud2d.rect(ax + 1.0f, y + 1.0f, 7.0f, 7.0f, 0.10f, 0.16f, 0.20f, 0.84f);
            if (armor[i] >= 2) {
                hud2d.rect(ax + 2.0f, y + 2.0f, 5.0f, 5.0f, 0.58f, 0.82f, 0.92f, 0.95f);
                hud2d.rect(ax + 3.0f, y + 3.0f, 2.0f, 2.0f, 0.86f, 1.0f, 1.0f, 0.90f);
            } else if (armor[i] == 1) {
                hud2d.rect(ax + 2.0f, y + 2.0f, 2.5f, 5.0f, 0.58f, 0.82f, 0.92f, 0.95f);
            }
        }
    }

    private void drawHunger(int w, int h, EchoClientPlayerVitals vitals) {
        int[] hunger = hungerFillStates(vitals);
        if (hunger.length == 0) {
            return;
        }
        float x = Math.min(w - 8.0f - hunger.length * 12.0f,
                w / 2.0f + EchoClientInventoryLayout.gridWidth() / 2.0f - hunger.length * 12.0f);
        float y = Math.max(8.0f, h - 94.0f);
        for (int i = 0; i < hunger.length; i++) {
            float hx = x + i * 12.0f;
            hud2d.rect(hx, y, 9.0f, 9.0f, 0.07f, 0.05f, 0.02f, 0.78f);
            hud2d.rect(hx + 1.0f, y + 1.0f, 7.0f, 7.0f, 0.20f, 0.13f, 0.04f, 0.88f);
            if (hunger[i] >= 2) {
                hud2d.rect(hx + 2.0f, y + 2.0f, 5.0f, 5.0f, 0.96f, 0.66f, 0.20f, 0.95f);
                hud2d.rect(hx + 3.0f, y + 3.0f, 2.0f, 2.0f, 1.0f, 0.88f, 0.48f, 0.92f);
            } else if (hunger[i] == 1) {
                hud2d.rect(hx + 2.0f, y + 2.0f, 2.5f, 5.0f, 0.96f, 0.66f, 0.20f, 0.95f);
            }
        }
    }

    private void drawExperience(int w, int h, EchoClientProgressionState progressionState) {
        EchoClientProgressionState safeProgression =
                progressionState == null ? EchoClientProgressionState.empty() : progressionState;
        int barWidth = Math.min(EchoClientInventoryLayout.gridWidth(), 364);
        int innerWidth = barWidth - 4;
        int fill = experienceFillPixels(safeProgression, innerWidth);
        float x = (w - barWidth) / 2.0f;
        float y = h - 70.0f;
        hud2d.rect(x, y, barWidth, 6.0f, 0.02f, 0.04f, 0.03f, 0.82f);
        hud2d.rect(x + 1.0f, y + 1.0f, barWidth - 2.0f, 4.0f, 0.08f, 0.16f, 0.10f, 0.88f);
        if (fill > 0) {
            hud2d.rect(x + 2.0f, y + 2.0f, fill, 2.0f, 0.38f, 0.96f, 0.34f, 0.95f);
        }
        String levelText = Integer.toString(safeProgression.level());
        float scale = 0.9f;
        float textX = w / 2.0f - levelText.length() * 2.7f * scale;
        hud2d.text(levelText, textX, y + 9.0f, scale, 0.54f, 1.0f, 0.42f, 0.95f);
    }

    private void drawHazard(int w, int h, EchoClientHazardState hazardState) {
        EchoClientHazardState safeHazard = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        if (!safeHazard.active()) {
            return;
        }
        int barWidth = 120;
        int fill = hazardFillPixels(safeHazard, barWidth - 4);
        float x = Math.min(w - barWidth - 8.0f,
                w / 2.0f + EchoClientInventoryLayout.gridWidth() / 2.0f - barWidth);
        float y = Math.max(8.0f, h - 108.0f);
        float[] color = hazardColor(safeHazard.hazardId());
        hud2d.rect(x, y, barWidth, 12.0f, 0.02f, 0.03f, 0.03f, 0.82f);
        hud2d.rect(x + 1.0f, y + 1.0f, barWidth - 2.0f, 10.0f, 0.07f, 0.10f, 0.10f, 0.88f);
        hud2d.rect(x + 2.0f, y + 8.0f, fill, 2.0f, color[0], color[1], color[2], 0.95f);
        hud2d.text("HAZ " + safeHazard.exposurePercent(), x + 4.0f, y + 2.0f, 0.75f,
                color[0], color[1], color[2], 0.96f);
    }

    private void drawTool(int w, int h, EchoClientToolStatus toolStatus) {
        EchoClientToolStatus safeTool = toolStatus == null ? EchoClientToolStatus.hand() : toolStatus;
        if (!safeTool.activeTool()) {
            return;
        }
        int barWidth = 120;
        int fill = toolDurabilityFillPixels(safeTool, barWidth - 4);
        float x = Math.max(8.0f, w / 2.0f - EchoClientInventoryLayout.gridWidth() / 2.0f);
        float y = Math.max(8.0f, h - 122.0f);
        hud2d.rect(x, y, barWidth, 12.0f, 0.02f, 0.03f, 0.04f, 0.82f);
        hud2d.rect(x + 1.0f, y + 1.0f, barWidth - 2.0f, 10.0f, 0.07f, 0.10f, 0.12f, 0.88f);
        hud2d.rect(x + 2.0f, y + 8.0f, fill, 2.0f, 0.42f, 0.78f, 1.0f, 0.95f);
        hud2d.text("TOOL " + safeTool.durability(), x + 4.0f, y + 2.0f, 0.75f,
                0.62f, 0.90f, 1.0f, 0.96f);
    }

    private void drawPauseOverlay(int w, int h) {
        hud2d.rect(0, 0, w, h, 0.0f, 0.0f, 0.0f, 0.5f);
    }

    private void drawDebugOverlay(int width, String debugLine) {
        if (debugLine == null || debugLine.isBlank()) {
            return;
        }
        String[] lines = debugLine.split("\\R");
        int maxChars = 0;
        for (String line : lines) {
            maxChars = Math.max(maxChars, line.length());
        }
        float scale = 0.85f;
        float panelX = 10.0f;
        float panelY = 10.0f;
        float panelW = Math.min(width - 20.0f, Math.max(210.0f, maxChars * 5.2f + 20.0f));
        float panelH = 14.0f + lines.length * 10.0f;
        hud2d.rect(panelX, panelY, panelW, panelH, 0.0f, 0.0f, 0.0f, 0.58f);
        hud2d.rect(panelX, panelY, panelW, 2.0f, 0.58f, 1.0f, 0.92f, 0.72f);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            hud2d.text(line, panelX + 9.0f, panelY + 8.0f + i * 10.0f,
                    scale, 0.80f, 1.0f, 0.94f, i == 0 ? 1.0f : 0.92f);
        }
    }

    private void drawSubtitles(int width, int height, List<EchoClientSubtitleLine> subtitles) {
        EchoClientSubtitleOverlayPlan plan = EchoClientSubtitleOverlayPlan.from(width, height, subtitles);
        if (!plan.visible()) {
            return;
        }
        hud2d.rect(plan.x(), plan.y(), plan.width(), plan.height(),
                0.0f, 0.0f, 0.0f, 0.58f);
        hud2d.rect(plan.x(), plan.y(), plan.width(), 2.0f,
                0.58f, 1.0f, 0.92f, 0.72f);
        for (int i = 0; i < plan.lines().size(); i++) {
            EchoClientSubtitleLine line = plan.lines().get(i);
            hud2d.text(line.text(), plan.x() + 10.0f, plan.y() + 8.0f + i * 14.0f,
                    0.92f, 0.84f, 0.92f, 1.0f, 0.95f);
        }
    }

    void delete() {
        uiRenderer.delete();
        icons.clear();
        textures.delete();
        hud2d.delete();
    }

    static int[] heartFillStates(EchoClientPlayerVitals vitals) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        int[] hearts = new int[safeVitals.heartSlots()];
        for (int i = 0; i < hearts.length; i++) {
            if (i < safeVitals.filledHeartSlots()) {
                hearts[i] = 2;
            } else if (safeVitals.halfHeartAt(i)) {
                hearts[i] = 1;
            } else {
                hearts[i] = 0;
            }
        }
        return hearts;
    }

    static int[] hungerFillStates(EchoClientPlayerVitals vitals) {
        EchoClientPlayerVitals safeVitals = vitals == null ? EchoClientPlayerVitals.full() : vitals;
        int[] hunger = new int[safeVitals.foodSlots()];
        for (int i = 0; i < hunger.length; i++) {
            if (i < safeVitals.filledFoodSlots()) {
                hunger[i] = 2;
            } else if (safeVitals.halfFoodAt(i)) {
                hunger[i] = 1;
            } else {
                hunger[i] = 0;
            }
        }
        return hunger;
    }

    static int[] armorFillStates(EchoClientPlayerCombatState combatState) {
        EchoClientPlayerCombatState safeCombat =
                combatState == null ? EchoClientPlayerCombatState.defaults() : combatState;
        int points = safeCombat.equipment().armorPoints();
        if (points <= 0) {
            return new int[0];
        }
        int[] armor = new int[10];
        int full = points / 2;
        boolean half = points % 2 == 1;
        for (int i = 0; i < armor.length; i++) {
            if (i < full) {
                armor[i] = 2;
            } else if (half && i == full) {
                armor[i] = 1;
            } else {
                armor[i] = 0;
            }
        }
        return armor;
    }

    static int experienceFillPixels(EchoClientProgressionState progressionState, int barWidth) {
        if (barWidth <= 0) {
            return 0;
        }
        EchoClientProgressionState safeProgression =
                progressionState == null ? EchoClientProgressionState.empty() : progressionState;
        double progress = Math.max(0.0D, Math.min(1.0D, safeProgression.progressToNextLevel()));
        return (int) Math.round(progress * barWidth);
    }

    static int hazardFillPixels(EchoClientHazardState hazardState, int barWidth) {
        if (barWidth <= 0) {
            return 0;
        }
        EchoClientHazardState safeHazard = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        double progress = Math.max(0.0D, Math.min(1.0D, safeHazard.exposure() / EchoClientHazardState.MAX_EXPOSURE));
        return (int) Math.round(progress * barWidth);
    }

    static int toolDurabilityFillPixels(EchoClientToolStatus toolStatus, int barWidth) {
        if (barWidth <= 0) {
            return 0;
        }
        EchoClientToolStatus safeTool = toolStatus == null ? EchoClientToolStatus.hand() : toolStatus;
        if (!safeTool.activeTool()) {
            return 0;
        }
        double progress = safeTool.maxDurability() <= 0
                ? 0.0D
                : safeTool.durability() / (double) safeTool.maxDurability();
        return (int) Math.round(Math.max(0.0D, Math.min(1.0D, progress)) * barWidth);
    }

    private static float[] hazardColor(String hazardId) {
        String id = hazardId == null ? "" : hazardId;
        if (id.contains("radiation")) {
            return new float[]{0.66f, 1.0f, 0.24f};
        }
        if (id.contains("cold")) {
            return new float[]{0.48f, 0.86f, 1.0f};
        }
        if (id.contains("nexus")) {
            return new float[]{0.82f, 0.42f, 1.0f};
        }
        return new float[]{0.38f, 0.96f, 0.54f};
    }
}
