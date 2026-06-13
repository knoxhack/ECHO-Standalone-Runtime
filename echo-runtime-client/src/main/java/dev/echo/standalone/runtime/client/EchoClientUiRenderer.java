package dev.echo.standalone.runtime.client;

final class EchoClientUiRenderer {
    private static final int MAIN_MENU_TERRAIN_LAYERS = 2;
    private static final int MAIN_MENU_TERRAIN_STEPS = 6;
    private static final int MAIN_MENU_ATMOSPHERIC_STREAKS = 6;
    private static final int MAIN_MENU_PANORAMA_SEMANTIC_LAYERS = 6;
    private static final int MAIN_MENU_PANORAMA_LINE_BUDGET =
            MAIN_MENU_TERRAIN_LAYERS * MAIN_MENU_TERRAIN_STEPS
                    + MAIN_MENU_ATMOSPHERIC_STREAKS
                    + 1;

    private final EchoClientFontRenderer font = new EchoClientFontRenderer();
    private final EchoClientNineSliceRenderer panels = new EchoClientNineSliceRenderer();
    private final EchoClientSaveSlotThumbnailTextureCache saveSlotThumbnails =
            new EchoClientSaveSlotThumbnailTextureCache();

    void renderShell(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            int width,
            int height,
            EchoClientScreenSnapshot screen
    ) {
        drawScreenBackground(hud2d, width, height, screen);
        if (screen.loading()) {
            drawLoadingScreen(hud2d, width, height, screen);
        } else {
            drawMenuScreen(hud2d, textures, width, height, screen);
        }
        drawToast(hud2d, width, screen.toast());
        drawModal(hud2d, width, height, screen.modal());
    }

    void renderOverlay(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            int width,
            int height,
            EchoClientScreenSnapshot screen
    ) {
        hud2d.rect(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.55f);
        drawMenuScreen(hud2d, textures, width, height, screen);
        drawToast(hud2d, width, screen.toast());
        drawModal(hud2d, width, height, screen.modal());
    }

    EchoClientUiVisualPlan planVisuals(int width, int height, EchoClientScreenSnapshot screen) {
        String routeId = screenCoreRouteId(screen);
        boolean mainMenuPanorama = screen != null
                && !screen.loading()
                && screen.state() == EchoClientGameState.MAIN_MENU
                && "echoscreencore:main_menu".equals(routeId);
        int seed = visualSeed(width, height, screen, routeId);
        String loadingTip = loadingTip(screen);
        return new EchoClientUiVisualPlan(
                mainMenuPanorama,
                mainMenuPanorama ? MAIN_MENU_PANORAMA_SEMANTIC_LAYERS : 0,
                mainMenuPanorama ? MAIN_MENU_TERRAIN_LAYERS : 0,
                mainMenuPanorama ? MAIN_MENU_TERRAIN_STEPS : 0,
                mainMenuPanorama ? MAIN_MENU_ATMOSPHERIC_STREAKS : 0,
                mainMenuPanorama ? MAIN_MENU_PANORAMA_LINE_BUDGET : 0,
                seed,
                loadingTipKey(screen),
                loadingTip,
                routeId
        );
    }

    private void drawScreenBackground(EchoClientHud2D hud2d, int w, int h, EchoClientScreenSnapshot screen) {
        EchoClientUiVisualPlan plan = planVisuals(w, h, screen);
        if (plan.mainMenuPanorama()) {
            drawMainMenuPanorama(hud2d, w, h, plan);
        } else {
            hud2d.rect(0, 0, w, h, 0.025f, 0.04f, 0.055f, 1.0f);
            int horizon = Math.max(120, h / 3);
            hud2d.rect(0, horizon, w, 2, 0.15f, 0.8f, 0.70f, 0.35f);
        }
        hud2d.rect(0, 0, w, 48, 0.03f, 0.12f, 0.13f, 0.85f);
        hud2d.rect(0, h - 42, w, 42, 0.02f, 0.09f, 0.10f, 0.90f);
    }

    private void drawMainMenuPanorama(
            EchoClientHud2D hud2d,
            int w,
            int h,
            EchoClientUiVisualPlan plan
    ) {
        int horizon = Math.max(132, Math.round(h * 0.42f));
        hud2d.rect(0, 0, w, h, 0.025f, 0.038f, 0.050f, 1.0f);
        hud2d.rect(0, 0, w, horizon, 0.050f, 0.075f, 0.085f, 0.92f);
        hud2d.rect(0, horizon - 28, w, 64, 0.110f, 0.185f, 0.165f, 0.62f);
        hud2d.rect(0, horizon + 28, w, h - horizon - 28, 0.090f, 0.075f, 0.058f, 0.96f);
        hud2d.rect(0, horizon + 76, w, Math.max(40, h - horizon - 76), 0.045f, 0.060f, 0.052f, 0.78f);

        for (int layer = 0; layer < plan.panoramaTerrainLayers(); layer++) {
            float yBase = horizon - 64 + layer * 28;
            float r = 0.075f + layer * 0.025f;
            float g = 0.120f + layer * 0.030f;
            float b = 0.120f + layer * 0.018f;
            float previousX = 0.0f;
            float previousY = yBase + terrainOffset(plan.panoramaSeed(), layer, 0);
            for (int step = 1; step <= plan.panoramaTerrainSteps(); step++) {
                float x = w * (step / (float) plan.panoramaTerrainSteps());
                float y = yBase + terrainOffset(plan.panoramaSeed(), layer, step);
                hud2d.line(previousX, previousY, x, y, r, g, b, 0.72f - layer * 0.12f, 10.0f - layer * 2.0f);
                previousX = x;
                previousY = y;
            }
        }

        int crashW = Math.min(210, Math.max(118, w / 4));
        int crashX = Math.max(24, w / 2 - crashW / 2);
        int crashY = horizon + 22;
        hud2d.rect(crashX, crashY, crashW, 24, 0.22f, 0.22f, 0.20f, 0.82f);
        hud2d.rect(crashX + crashW - 40, crashY - 15, 52, 18, 0.34f, 0.25f, 0.18f, 0.72f);
        hud2d.rect(crashX + 16, crashY + 19, crashW - 38, 7, 0.08f, 0.10f, 0.10f, 0.74f);
        hud2d.rect(crashX + 34, crashY - 26, 18, 26, 0.14f, 0.20f, 0.19f, 0.55f);
        hud2d.rect(crashX + crashW - 76, crashY - 24, 12, 24, 0.14f, 0.20f, 0.19f, 0.50f);

        int beaconX = Math.min(w - 56, crashX + crashW + 42);
        hud2d.rect(beaconX, crashY - 18, 12, 52, 0.10f, 0.18f, 0.18f, 0.78f);
        hud2d.rect(beaconX - 10, crashY - 24, 32, 8, 0.26f, 0.74f, 0.66f, 0.62f);
        hud2d.line(beaconX + 6, crashY - 40, beaconX + 6, 52, 0.22f, 0.82f, 0.74f, 0.20f, 3.0f);

        for (int i = 0; i < plan.panoramaAtmosphericStreaks(); i++) {
            float x = (float) Math.floorMod(plan.panoramaSeed() + i * 89, Math.max(1, w));
            float y = 68 + (i % 7) * 31;
            hud2d.line(x, y, x + 24, y + 9, 0.62f, 0.68f, 0.62f, 0.22f, 2.0f);
        }
    }

    private void drawLoadingScreen(EchoClientHud2D hud2d, int w, int h, EchoClientScreenSnapshot screen) {
        EchoClientUiVisualPlan plan = planVisuals(w, h, screen);
        font.drawCentered(hud2d, screen.title(), w / 2.0f, h * 0.30f, 4.0f, 0.84f, 1.0f, 0.96f, 1.0f);
        font.drawCentered(hud2d, screen.subtitle(), w / 2.0f, h * 0.43f, 2.0f, 0.58f, 0.92f, 0.86f, 0.95f);
        int barWidth = Math.min(520, Math.max(260, w - 220));
        int barHeight = 18;
        int x = (w - barWidth) / 2;
        int y = (int) (h * 0.52f);
        panels.panel(hud2d, x - 2, y - 2, barWidth + 4, barHeight + 4, 0.02f, 0.05f, 0.06f, 1.0f, false);
        hud2d.rect(x, y, (float) (barWidth * screen.loadingProgress()), barHeight, 0.18f, 0.78f, 0.70f, 1.0f);
        font.drawCentered(hud2d, Math.round(screen.loadingProgress() * 100.0D) + "%", w / 2.0f, y + 34, 2.0f,
                0.80f, 1.0f, 0.94f, 1.0f);
        if (plan.loadingTipVisible()) {
            font.drawCentered(hud2d, plan.loadingTip(), w / 2.0f, Math.min(h - 68, y + 58), 1.0f,
                    0.76f, 0.92f, 0.82f, 0.94f);
        }
        font.drawCentered(hud2d, screen.footer(), w / 2.0f, h - 28, 1.0f, 0.55f, 0.75f, 0.72f, 0.9f);
    }

    private void drawMenuScreen(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            int w,
            int h,
            EchoClientScreenSnapshot screen
    ) {
        font.drawCentered(hud2d, screen.title(), w / 2.0f, Math.max(76, h * 0.18f), 4.0f,
                0.84f, 1.0f, 0.96f, 1.0f);
        if (!screen.subtitle().isBlank()) {
            font.drawCentered(hud2d, screen.subtitle(), w / 2.0f, Math.max(122, h * 0.18f + 48), 2.0f,
                    0.50f, 0.90f, 0.84f, 0.95f);
        }
        drawSaveSlotThumbnail(hud2d, textures, w, h, screen);

        int buttonWidth = EchoClientScreenController.MENU_BUTTON_WIDTH;
        int buttonHeight = EchoClientScreenController.MENU_BUTTON_HEIGHT;
        int visibleCount = EchoClientScreenController.menuVisibleCount(h, screen.options().size());
        int startY = EchoClientScreenController.menuStartY(h, visibleCount);
        int start = Math.max(0, Math.min(screen.scrollOffset(), Math.max(0, screen.options().size() - visibleCount)));
        int end = Math.min(screen.options().size(), start + visibleCount);
        for (int i = start; i < end; i++) {
            int visibleIndex = i - start;
            EchoClientScreenOption option = screen.options().get(i);
            int x = (w - buttonWidth) / 2;
            int y = startY + visibleIndex * (buttonHeight + EchoClientScreenController.MENU_BUTTON_SPACING);
            boolean selected = i == screen.selectedIndex();
            float alpha = option.enabled() ? 0.86f : 0.36f;
            panels.panel(hud2d, x, y, buttonWidth, buttonHeight, 0.06f, 0.10f, 0.12f, alpha, selected);
            drawOptionContent(hud2d, option, x, y, buttonWidth, buttonHeight, selected, alpha);
        }
        if (screen.options().size() > visibleCount && visibleCount > 0) {
            drawScrollbar(hud2d, w, startY, visibleCount, screen.options().size(), start);
        }
        if (!screen.tooltip().isBlank()) {
            font.drawCentered(hud2d, screen.tooltip(), w / 2.0f, h - 52, 1.0f,
                    0.72f, 0.95f, 0.88f, 0.90f);
        }
        font.drawCentered(hud2d, screen.footer(), w / 2.0f, h - 28, 1.0f, 0.55f, 0.75f, 0.72f, 0.9f);
    }

    private void drawSaveSlotThumbnail(
            EchoClientHud2D hud2d,
            EchoClientHudTextureRenderer textures,
            int width,
            int height,
            EchoClientScreenSnapshot screen
    ) {
        if (screen == null
                || screen.kind() != EchoClientScreenKind.WORLD_SELECT
                || screen.saveSlotThumbnail() == null
                || !screen.saveSlotThumbnail().visible()
                || width < 760) {
            return;
        }
        EchoClientSaveSlotThumbnailSnapshot thumbnail = screen.saveSlotThumbnail();
        int menuRight = width / 2 + EchoClientScreenController.MENU_BUTTON_WIDTH / 2;
        int panelW = Math.min(236, Math.max(188, width - menuRight - 56));
        if (panelW < 176) {
            return;
        }
        int panelH = 164;
        int panelX = Math.min(width - panelW - 24, menuRight + 24);
        int visibleCount = EchoClientScreenController.menuVisibleCount(height, screen.options().size());
        int panelY = Math.max(178, EchoClientScreenController.menuStartY(height, visibleCount));
        panelY = Math.min(panelY, Math.max(96, height - panelH - 58));

        panels.panel(hud2d, panelX, panelY, panelW, panelH, 0.045f, 0.075f, 0.083f, 0.93f, false);
        int imageX = panelX + 12;
        int imageY = panelY + 12;
        int imageW = panelW - 24;
        int imageH = 86;
        float[] sky = color(thumbnail.skyArgb());
        float[] terrain = color(thumbnail.terrainArgb());
        float[] accent = color(thumbnail.accentArgb());
        float[] shadow = color(thumbnail.shadowArgb());
        int textureId = textures == null ? 0 : saveSlotThumbnails.textureId(thumbnail);

        if (textureId != 0) {
            hud2d.rect(imageX, imageY, imageW, imageH, 0.0f, 0.0f, 0.0f, 1.0f);
            hud2d.flush();
            textures.begin(width, height);
            textures.draw(textureId, imageX, imageY, imageW, imageH, 1.0f);
            textures.end();
            hud2d.rect(imageX, imageY + imageH - 18, imageW, 18, shadow[0], shadow[1], shadow[2], 0.30f);
        } else {
            hud2d.rect(imageX, imageY, imageW, imageH, sky[0], sky[1], sky[2], 1.0f);
            hud2d.rect(imageX, imageY + imageH * 0.55f, imageW, imageH * 0.45f,
                    terrain[0], terrain[1], terrain[2], 1.0f);
            hud2d.rect(imageX, imageY + imageH - 18, imageW, 18, shadow[0], shadow[1], shadow[2], 0.72f);
            int ridgeBase = imageY + Math.round(imageH * 0.58f);
            drawThumbnailRidge(hud2d, thumbnail.slotId().hashCode(), imageX, ridgeBase, imageW, terrain, shadow);
            int beaconX = imageX + Math.floorMod(thumbnail.slotId().hashCode(), Math.max(1, imageW - 32)) + 12;
            hud2d.rect(beaconX, imageY + 28, 8, 40, shadow[0], shadow[1], shadow[2], 0.82f);
            hud2d.rect(beaconX - 8, imageY + 24, 24, 5, accent[0], accent[1], accent[2], 0.86f);
            hud2d.line(beaconX + 4, imageY + 23, beaconX + 4, imageY + 6,
                    accent[0], accent[1], accent[2], 0.32f, 3.0f);
        }
        hud2d.rect(imageX, imageY, imageW, 2, accent[0], accent[1], accent[2], 0.72f);
        hud2d.rect(imageX, imageY + imageH - 2, imageW, 2, 0.0f, 0.0f, 0.0f, 0.42f);

        font.drawCentered(hud2d, thumbnailTitle(thumbnail.displayName()), panelX + panelW / 2.0f, panelY + 108, 1.0f,
                0.78f, 1.0f, 0.94f, 0.98f);
        font.drawCentered(hud2d, thumbnailMeta(thumbnail), panelX + panelW / 2.0f, panelY + 130, 0.75f,
                accent[0], Math.min(1.0f, accent[1] + 0.12f), Math.min(1.0f, accent[2] + 0.12f), 0.96f);
        font.drawCentered(hud2d, thumbnailSlot(thumbnail.slotId()), panelX + panelW / 2.0f, panelY + 145, 0.75f,
                0.54f, 0.72f, 0.70f, 0.88f);
    }

    static boolean usesCapturedThumbnailTexture(EchoClientSaveSlotThumbnailSnapshot thumbnail) {
        return thumbnail != null
                && thumbnail.captured()
                && !thumbnail.relativePath().isBlank()
                && !thumbnail.resolvedPath().isBlank()
                && thumbnail.width() > 0
                && thumbnail.height() > 0;
    }

    void delete() {
        saveSlotThumbnails.clear();
    }

    private static void drawThumbnailRidge(
            EchoClientHud2D hud2d,
            int seed,
            int x,
            int baseY,
            int width,
            float[] terrain,
            float[] shadow
    ) {
        float previousX = x;
        float previousY = baseY + thumbnailOffset(seed, 0);
        for (int step = 1; step <= 6; step++) {
            float nextX = x + width * (step / 6.0f);
            float nextY = baseY + thumbnailOffset(seed, step);
            hud2d.line(previousX, previousY, nextX, nextY,
                    Math.min(1.0f, terrain[0] + 0.06f),
                    Math.min(1.0f, terrain[1] + 0.05f),
                    Math.min(1.0f, terrain[2] + 0.04f),
                    0.88f,
                    8.0f);
            hud2d.line(previousX, previousY + 12, nextX, nextY + 12,
                    shadow[0], shadow[1], shadow[2], 0.48f, 9.0f);
            previousX = nextX;
            previousY = nextY;
        }
    }

    private static float thumbnailOffset(int seed, int step) {
        int mixed = seed + step * 131;
        return Math.floorMod(mixed ^ (mixed >>> 8), 25) - 12.0f;
    }

    private static String thumbnailTitle(String value) {
        if (value == null || value.isBlank()) {
            return "SAVE SLOT";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 20 ? trimmed : trimmed.substring(0, 20);
    }

    private static String thumbnailMeta(EchoClientSaveSlotThumbnailSnapshot thumbnail) {
        String pack = thumbnail.packId() == null ? "" : thumbnail.packId().trim();
        if (pack.length() > 15) {
            pack = pack.substring(0, 15);
        }
        return thumbnail.statusLabel() + " " + pack;
    }

    private static String thumbnailSlot(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= 22 ? trimmed : trimmed.substring(0, 22);
    }

    private static float[] color(int argb) {
        return new float[] {
                ((argb >>> 16) & 0xFF) / 255.0f,
                ((argb >>> 8) & 0xFF) / 255.0f,
                (argb & 0xFF) / 255.0f
        };
    }

    private void drawOptionContent(
            EchoClientHud2D hud2d,
            EchoClientScreenOption option,
            int x,
            int y,
            int width,
            int height,
            boolean selected,
            float alpha
    ) {
        float tr = option.enabled() ? 0.86f : 0.45f;
        float tg = option.enabled() ? 1.00f : 0.55f;
        float tb = option.enabled() ? 0.95f : 0.55f;
        switch (option.kind()) {
            case BUTTON -> font.drawCentered(hud2d, option.label(), x + width / 2.0f, y + 9, 2.0f, tr, tg, tb, alpha);
            case TOGGLE -> {
                hud2d.text(option.label(), x + 16, y + 12, 1.0f, tr, tg, tb, alpha);
                int toggleX = x + width - 70;
                int toggleY = y + 9;
                hud2d.rect(toggleX, toggleY, 48, 16, 0.03f, 0.06f, 0.07f, alpha);
                hud2d.rect(toggleX + (option.active() ? 26 : 2), toggleY + 2, 20, 12,
                        option.active() ? 0.28f : 0.35f,
                        option.active() ? 0.86f : 0.35f,
                        option.active() ? 0.72f : 0.35f,
                        alpha);
                hud2d.text(option.valueText(), toggleX + 8, y + 13, 0.75f,
                        0.88f, 1.0f, 0.96f, alpha);
            }
            case SLIDER -> {
                hud2d.text(option.label(), x + 16, y + 12, 1.0f, tr, tg, tb, alpha);
                int trackX = x + EchoClientScreenController.MENU_SLIDER_TRACK_X_OFFSET;
                int trackY = y + 16;
                int trackW = EchoClientScreenController.MENU_SLIDER_TRACK_WIDTH;
                hud2d.rect(trackX, trackY, trackW, 4, 0.03f, 0.08f, 0.09f, alpha);
                hud2d.rect(trackX, trackY, (float) (trackW * option.sliderValue()), 4,
                        0.22f, 0.82f, 0.72f, alpha);
                int thumbX = trackX + Math.round((trackW - 6) * (float) option.sliderValue());
                hud2d.rect(thumbX, trackY - 5, 6, 14,
                        selected ? 0.86f : 0.56f,
                        selected ? 1.0f : 0.78f,
                        selected ? 0.94f : 0.76f,
                        alpha);
                hud2d.text(option.valueText() + "%", x + width - 52, y + 12, 1.0f, tr, tg, tb, alpha);
            }
            case TEXT -> {
                hud2d.text(option.label(), x + 16, y + 12, 1.0f, tr, tg, tb, alpha);
                int fieldX = x + 92;
                int fieldY = y + 8;
                int fieldW = width - 112;
                hud2d.rect(fieldX, fieldY, fieldW, 18, 0.02f, 0.05f, 0.06f, alpha);
                hud2d.rect(fieldX, fieldY + 17, fieldW, 1,
                        option.active() ? 0.26f : 0.16f,
                        option.active() ? 0.86f : 0.38f,
                        option.active() ? 0.72f : 0.36f,
                        alpha);
                String suffix = option.active() ? "_" : "";
                hud2d.text(option.valueText() + suffix, fieldX + 8, y + 12, 1.0f, tr, tg, tb, alpha);
            }
        }
    }

    private static void drawScrollbar(
            EchoClientHud2D hud2d,
            int width,
            int startY,
            int visibleCount,
            int totalCount,
            int scrollOffset
    ) {
        int buttonWidth = EchoClientScreenController.MENU_BUTTON_WIDTH;
        int buttonHeight = EchoClientScreenController.MENU_BUTTON_HEIGHT;
        int spacing = EchoClientScreenController.MENU_BUTTON_SPACING;
        int trackX = (width + buttonWidth) / 2 + 12;
        int trackY = startY;
        int trackH = visibleCount * buttonHeight + Math.max(0, visibleCount - 1) * spacing;
        hud2d.rect(trackX, trackY, 4, trackH, 0.04f, 0.10f, 0.11f, 0.80f);
        float thumbRatio = visibleCount / (float) Math.max(visibleCount, totalCount);
        int thumbH = Math.max(24, Math.round(trackH * thumbRatio));
        int maxOffset = Math.max(1, totalCount - visibleCount);
        int thumbY = trackY + Math.round((trackH - thumbH) * (scrollOffset / (float) maxOffset));
        hud2d.rect(trackX - 1, thumbY, 6, thumbH, 0.26f, 0.78f, 0.70f, 0.92f);
    }

    private void drawModal(EchoClientHud2D hud2d, int width, int height, EchoClientModalSnapshot modal) {
        if (modal == null || !modal.visible()) {
            return;
        }
        hud2d.rect(0, 0, width, height, 0.0f, 0.0f, 0.0f, 0.36f);
        int panelW = Math.min(460, Math.max(320, width - 160));
        int panelH = 168;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;
        panels.panel(hud2d, panelX, panelY, panelW, panelH, 0.05f, 0.09f, 0.10f, 0.98f, true);
        font.drawCentered(hud2d, modal.title(), width / 2.0f, panelY + 24, 2.0f,
                0.84f, 1.0f, 0.96f, 1.0f);
        font.drawCentered(hud2d, modal.message(), width / 2.0f, panelY + 68, 1.0f,
                0.70f, 0.92f, 0.88f, 0.95f);

        int buttonW = 132;
        int buttonH = 34;
        int gap = 18;
        int buttonY = panelY + panelH - 54;
        int cancelX = panelX + panelW / 2 - buttonW - gap / 2;
        int confirmX = panelX + panelW / 2 + gap / 2;
        drawModalButton(hud2d, cancelX, buttonY, buttonW, buttonH, modal.cancelLabel(), !modal.confirmSelected());
        drawModalButton(hud2d, confirmX, buttonY, buttonW, buttonH, modal.confirmLabel(), modal.confirmSelected());
    }

    private void drawModalButton(
            EchoClientHud2D hud2d,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean selected
    ) {
        panels.panel(hud2d, x, y, width, height, 0.06f, 0.10f, 0.12f, selected ? 0.98f : 0.74f, selected);
        font.drawCentered(hud2d, label, x + width / 2.0f, y + 9, 2.0f,
                selected ? 0.86f : 0.58f,
                selected ? 1.0f : 0.78f,
                selected ? 0.95f : 0.72f,
                1.0f);
    }

    private void drawToast(EchoClientHud2D hud2d, int width, EchoClientToastSnapshot toast) {
        if (toast == null || !toast.visible()) {
            return;
        }
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

    private static String screenCoreRouteId(EchoClientScreenSnapshot screen) {
        if (screen == null || screen.footer() == null) {
            return "";
        }
        String marker = "ScreenCore ";
        int markerIndex = screen.footer().lastIndexOf(marker);
        if (markerIndex < 0) {
            return "";
        }
        return screen.footer().substring(markerIndex + marker.length()).trim();
    }

    private static String loadingTip(EchoClientScreenSnapshot screen) {
        if (screen == null || !screen.loading()) {
            return "";
        }
        if (!screen.tooltip().isBlank()) {
            return screen.tooltip();
        }
        return switch (screen.state()) {
            case MOD_SCAN -> "TIP ADAPTERCORE MODULES LOAD FIRST";
            case LOADING_ASSETS -> "TIP RESOURCE PACKS SHAPE MODELS";
            case LOADING_DATA -> "TIP REGISTRIES LOCK SAVE IDS";
            case LOADING_WORLD -> "TIP CHUNKS RESTORE CAMP STATE";
            default -> "TIP ASHFALL SAVES STAY LOCAL";
        };
    }

    private static String loadingTipKey(EchoClientScreenSnapshot screen) {
        if (screen == null || !screen.loading()) {
            return "";
        }
        return switch (screen.state()) {
            case MOD_SCAN -> "loading.mod_scan";
            case LOADING_ASSETS -> "loading.assets";
            case LOADING_DATA -> "loading.data";
            case LOADING_WORLD -> "loading.world";
            default -> "loading.complete";
        };
    }

    private static int visualSeed(int width, int height, EchoClientScreenSnapshot screen, String routeId) {
        String title = screen == null ? "" : screen.title();
        String subtitle = screen == null ? "" : screen.subtitle();
        int seed = 17;
        seed = 31 * seed + Math.max(1, width);
        seed = 31 * seed + Math.max(1, height);
        seed = 31 * seed + title.hashCode();
        seed = 31 * seed + subtitle.hashCode();
        seed = 31 * seed + routeId.hashCode();
        return seed & 0x7fffffff;
    }

    private static float terrainOffset(int seed, int layer, int step) {
        int mixed = seed + layer * 211 + step * 97;
        int value = Math.floorMod(mixed ^ (mixed >>> 7), 41);
        return value - 20.0f;
    }
}
