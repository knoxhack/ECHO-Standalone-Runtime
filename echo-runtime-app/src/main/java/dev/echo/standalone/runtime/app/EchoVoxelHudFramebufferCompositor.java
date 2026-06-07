package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;

public final class EchoVoxelHudFramebufferCompositor {
    private static final Color PANEL = new Color(4, 10, 11, 184);
    private static final Color PANEL_DARK = new Color(0, 0, 0, 92);
    private static final Color LINE = new Color(93, 194, 177, 136);
    private static final Color TEXT = new Color(230, 240, 232);
    private static final Color MUTED = new Color(151, 172, 167);
    private static final Color ACCENT = new Color(113, 211, 183);
    private static final Color WARNING = new Color(232, 184, 92);
    private static final Color DANGER = new Color(215, 91, 91);

    public EchoVoxelFramebuffer composite(EchoVoxelFramebuffer framebuffer, EchoVoxelHudOverlay overlay) {
        Objects.requireNonNull(framebuffer, "framebuffer");
        Objects.requireNonNull(overlay, "overlay");
        BufferedImage image = new BufferedImage(
                framebuffer.width(),
                framebuffer.height(),
                BufferedImage.TYPE_INT_ARGB
        );
        image.setRGB(
                0,
                0,
                framebuffer.width(),
                framebuffer.height(),
                framebuffer.argb(),
                0,
                framebuffer.width()
        );
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawOverlay(g, framebuffer, overlay);
        } finally {
            g.dispose();
        }
        int[] pixels = new int[framebuffer.width() * framebuffer.height()];
        image.getRGB(0, 0, framebuffer.width(), framebuffer.height(), pixels, 0, framebuffer.width());
        return new EchoVoxelFramebuffer(
                framebuffer.width(),
                framebuffer.height(),
                pixels,
                framebuffer.blocksVisited(),
                framebuffer.facesDrawn(),
                checksum(pixels)
        );
    }

    private static void drawOverlay(Graphics2D g, EchoVoxelFramebuffer framebuffer, EchoVoxelHudOverlay overlay) {
        int width = framebuffer.width();
        int height = framebuffer.height();
        boolean compact = width < 760 || height < 430;
        drawBottomBand(g, width, height);
        drawTopBanner(g, width, overlay, compact);
        if (compact) {
            drawCompactMissionStrip(g, width, overlay);
        } else {
            drawStatusPanel(g, width, overlay);
            drawMissionFeed(g, width, height, overlay);
        }
        drawActionPanel(g, width, height, framebuffer, overlay, compact);
        if (!overlay.shellVisible()) {
            drawBlockBreakFeedback(g, width, height, overlay, compact);
            drawActionParticles(g, width, height, overlay, compact);
            drawHeldItemPreview(g, width, height, overlay, compact);
        }
        drawHotbar(g, width, height, overlay.hotbar());
        if (overlay.shellVisible()) {
            if (overlay.missionLogVisible()) {
                drawMissionLogOverlay(g, width, height, overlay, compact);
            } else if (overlay.terminalVisible()) {
                drawTerminalOverlay(g, width, height, overlay, compact);
            } else if (overlay.inventoryVisible()) {
                drawInventoryOverlay(g, width, height, overlay, compact);
            } else {
                drawShellOverlay(g, width, height, overlay, compact);
            }
        }
    }

    private static void drawBottomBand(Graphics2D g, int width, int height) {
        int bandHeight = Math.min(height, 164);
        g.setColor(PANEL_DARK);
        g.fillRect(0, height - bandHeight, width, bandHeight);
    }

    private static void drawTopBanner(Graphics2D g, int width, EchoVoxelHudOverlay overlay, boolean compact) {
        int x = 18;
        int y = 14;
        int panelWidth = Math.min(width - 36, compact ? 420 : 436);
        int panelHeight = compact ? 48 : 54;
        panel(g, x, y, panelWidth, panelHeight, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compact ? 16 : 18));
        g.setColor(TEXT);
        g.drawString("ECHO Ashfall Standalone", x + 14, y + 21);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        g.setColor(overlay.rendererReady() ? ACCENT : WARNING);
        g.drawString(trim(overlay.adapterLine(), compact ? 52 : 64), x + 14, y + panelHeight - 14);
    }

    private static void drawCompactMissionStrip(Graphics2D g, int width, EchoVoxelHudOverlay overlay) {
        EchoAshfallPlayerFeedback feedback = feedback(overlay);
        int x = 18;
        int y = 70;
        int panelWidth = Math.min(width - 36, 604);
        panel(g, x, y, panelWidth, 52, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g.setColor(TEXT);
        g.drawString(trim(overlay.mission().status() + " / " + feedback.currentObjective(), 42), x + 14, y + 20);
        g.setColor(overlay.mission().playerHealth() > 0 ? ACCENT : DANGER);
        g.drawString("HP " + overlay.mission().playerHealth(), x + 14, y + 39);
        g.setColor(overlay.mission().hydration() > 25.0D ? ACCENT : WARNING);
        g.drawString("HYD " + Math.round(overlay.mission().hydration()), x + 78, y + 39);
        g.setColor(overlay.mission().hunger() > 20.0D ? ACCENT : WARNING);
        g.drawString("HNG " + Math.round(overlay.mission().hunger()), x + 154, y + 39);
        g.setColor(overlay.mission().ashExposure() < 70.0D ? WARNING : DANGER);
        g.drawString("ASH " + String.format("%.1f", overlay.mission().ashExposure()), x + 230, y + 39);
        g.setColor(overlay.rendererReady() ? ACCENT : WARNING);
        g.drawString(trim(feedback.selectedHotbarItem(), 24), x + 318, y + 39);
    }

    private static void drawStatusPanel(Graphics2D g, int width, EchoVoxelHudOverlay overlay) {
        EchoAshfallPlayerFeedback feedback = feedback(overlay);
        int panelWidth = 332;
        int x = Math.max(20, width - panelWidth - 20);
        int y = 78;
        panel(g, x, y, panelWidth, 258, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        g.setColor(TEXT);
        g.drawString(trim(overlay.mission().status() + " / " + feedback.currentObjective(), 32), x + 16, y + 25);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        metric(g, x + 16, y + 52, "Health", String.valueOf(overlay.mission().playerHealth()),
                overlay.mission().playerHealth() > 0 ? ACCENT : DANGER);
        metric(g, x + 16, y + 74, "Hydration", String.format("%.0f", overlay.mission().hydration()),
                overlay.mission().hydration() > 25.0D ? ACCENT : WARNING);
        metric(g, x + 16, y + 96, "Hunger", String.format("%.0f", overlay.mission().hunger()),
                overlay.mission().hunger() > 20.0D ? ACCENT : WARNING);
        metric(g, x + 16, y + 118, "Ash", String.format("%.1f", overlay.mission().ashExposure()),
                overlay.mission().ashExposure() < 70.0D ? WARNING : DANGER);
        metric(g, x + 16, y + 140, "Shelter", trim(feedback.shelterStatus(), 22),
                overlay.mission().shelterBuilt() ? ACCENT : WARNING);
        metric(g, x + 16, y + 162, "Selected", trim(feedback.selectedHotbarItem(), 22), ACCENT);
        metric(g, x + 16, y + 184, "Tool", trim(feedback.toolDurability(), 22), ACCENT);
        metric(g, x + 16, y + 206, "Mode", trim(overlay.playerModeLabel(), 22),
                overlay.playerGrounded() ? ACCENT : WARNING);
        metric(g, x + 16, y + 228, "Warnings", trim(String.join(",", feedback.warningStates()), 22),
                feedback.warningStates().isEmpty() ? ACCENT : WARNING);
        metric(g, x + 16, y + 250, "OpenGL", trim(overlay.rendererLabel(), 22),
                overlay.rendererReady() ? ACCENT : WARNING);
    }

    private static void drawMissionFeed(Graphics2D g, int width, int height, EchoVoxelHudOverlay overlay) {
        int panelWidth = Math.min(520, Math.max(280, width - 40));
        int x = 20;
        int y = Math.max(96, height - 236);
        panel(g, x, y, panelWidth, 72, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.setColor(TEXT);
        g.drawString("Ashfall Feed", x + 14, y + 23);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        List<String> steps = overlay.mission().feed();
        for (int i = 0; i < Math.min(2, steps.size()); i++) {
            int columnX = x + 14 + i * Math.max(220, (panelWidth - 28) / 2);
            g.setColor(ACCENT);
            g.drawString("0" + i + " " + trim(steps.get(i), 24), columnX, y + 48);
            g.setColor(MUTED);
            g.drawString(trim(overlay.mission().objectiveSummary(i), 24), columnX, y + 62);
        }
    }

    private static void drawActionPanel(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelFramebuffer framebuffer,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        EchoAshfallPlayerFeedback feedback = feedback(overlay);
        int actionWidth = Math.min(width - 40, compact ? 660 : 760);
        int actionY = Math.max(compact ? 128 : 92, height - 148);
        panel(g, 20, actionY, actionWidth, 64, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.setColor(ACCENT);
        g.drawString("ASH> " + trim(feedback.currentHint(), compact ? 58 : 78), 38, actionY + 21);
        g.setColor(TEXT);
        g.drawString("blocks=" + framebuffer.blocksVisited()
                + " faces=" + framebuffer.facesDrawn()
                + " chunks=" + overlay.loadedChunkCount(), 38, actionY + 41);
        g.setColor(overlay.targetAvailable() ? ACCENT : WARNING);
        g.drawString("target=" + trim(overlay.targetLabel(), compact ? 24 : 34)
                + " feedback=" + trim(feedback.actionFeedback(), compact ? 36 : 54), 38, actionY + 59);
    }

    private static void drawBlockBreakFeedback(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        double progress = blockBreakProgress(overlay);
        if (progress <= 0.0D) {
            return;
        }
        int centerX = width / 2;
        int centerY = height / 2;
        int size = compact ? 42 : 52;
        int x = centerX - size / 2;
        int y = centerY - size / 2;
        g.setColor(new Color(7, 10, 10, 74));
        g.fillRect(x, y, size, size);
        g.setStroke(new BasicStroke(compact ? 1.5f : 2.0f));
        g.setColor(new Color(16, 22, 21, 180));
        g.drawRect(x, y, size, size);
        g.setColor(new Color(236, 220, 160, 188));
        int cracks = Math.max(2, Math.min(7, 2 + (int) Math.round(progress * 5.0D)));
        for (int index = 0; index < cracks; index++) {
            int startX = x + 7 + index * Math.max(3, size / 8);
            int startY = y + 8 + Math.abs(hash(index * 31 + size)) % Math.max(5, size / 3);
            int midX = startX + 5 + Math.abs(hash(index * 17 + width)) % Math.max(6, size / 4);
            int midY = startY + 5 + Math.abs(hash(index * 23 + height)) % Math.max(6, size / 3);
            int endX = midX - 4 + Math.abs(hash(index * 43)) % Math.max(7, size / 3);
            int endY = midY + 4 + Math.abs(hash(index * 53)) % Math.max(6, size / 3);
            g.drawLine(startX, startY, midX, midY);
            g.drawLine(midX, midY, endX, endY);
        }
        int barWidth = compact ? 96 : 124;
        int barHeight = 7;
        int barX = centerX - barWidth / 2;
        int barY = y + size + 10;
        g.setColor(new Color(3, 8, 9, 180));
        g.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);
        g.setColor(WARNING);
        g.fillRoundRect(barX, barY, Math.max(6, (int) Math.round(barWidth * progress)), barHeight, 4, 4);
        g.setColor(new Color(236, 220, 160, 128));
        g.drawRoundRect(barX, barY, barWidth, barHeight, 4, 4);
    }

    private static void drawActionParticles(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        if (!actionParticlesActive(overlay)) {
            return;
        }
        int anchorX = width / 2 + (compact ? 58 : 76);
        int anchorY = height / 2 - (compact ? 34 : 42);
        int radius = compact ? 34 : 44;
        int count = compact ? 11 : 15;
        String seedText = overlay.actionLabel() + "|" + overlay.targetLabel();
        int seed = hash(seedText.hashCode());
        for (int index = 0; index < count; index++) {
            int angleSeed = Math.abs(hash(seed + index * 97));
            double angle = (angleSeed % 628) / 100.0D;
            int distance = 16 + Math.abs(hash(seed + index * 41)) % radius;
            int px = anchorX + (int) Math.round(Math.cos(angle) * distance);
            int py = anchorY + (int) Math.round(Math.sin(angle) * distance * 0.70D);
            int size = 4 + Math.abs(hash(seed + index * 19)) % (compact ? 4 : 6);
            Color color = particleColor(seed, index);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 186));
            g.fillOval(px, py, size, size);
            g.setColor(new Color(255, 245, 190, 88));
            g.drawLine(px + size / 2, py + size / 2, anchorX, anchorY);
        }
    }

    private static void drawHeldItemPreview(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        EchoVoxelHotbarSlot selected = overlay.hotbar().selected();
        if (selected.empty()) {
            return;
        }
        int previewWidth = compact ? 142 : 174;
        int previewHeight = compact ? 106 : 128;
        int x = Math.max(18, width - previewWidth - (compact ? 18 : 34));
        int y = Math.max(82, height - previewHeight - (compact ? 82 : 98));
        g.setColor(new Color(0, 0, 0, 86));
        g.fillRoundRect(x + 10, y + 14, previewWidth - 18, previewHeight - 18, 18, 18);
        g.setColor(new Color(36, 28, 22, 230));
        g.fillRoundRect(x + previewWidth - 68, y + 44, 48, 84, 16, 16);
        g.setColor(new Color(86, 62, 43, 238));
        g.fillRoundRect(x + previewWidth - 57, y + 52, 31, 70, 13, 13);
        g.setColor(new Color(selected.block().argb(), true));
        g.fillRoundRect(x + 18, y + 18, compact ? 58 : 70, compact ? 58 : 70, 9, 9);
        g.setColor(new Color(255, 255, 255, 54));
        g.fillRect(x + 26, y + 24, compact ? 42 : 52, 8);
        g.setColor(new Color(3, 8, 9, 160));
        g.setStroke(new BasicStroke(2.0f));
        g.drawRoundRect(x + 18, y + 18, compact ? 58 : 70, compact ? 58 : 70, 9, 9);
        g.setColor(ACCENT);
        g.setStroke(new BasicStroke(compact ? 4.0f : 5.0f));
        g.drawLine(x + 62, y + 72, x + previewWidth - 36, y + 36);
        g.setColor(new Color(230, 240, 232, 210));
        g.setStroke(new BasicStroke(compact ? 2.0f : 2.5f));
        g.drawLine(x + 69, y + 77, x + previewWidth - 28, y + 43);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 10 : 11));
        g.setColor(TEXT);
        g.drawString(trim(selected.label(), compact ? 14 : 18), x + 17, y + previewHeight - 18);
        g.setColor(MUTED);
        g.drawString("x" + selected.count(), x + previewWidth - 48, y + previewHeight - 18);
    }

    private static void drawHotbar(Graphics2D g, int width, int height, EchoVoxelPlayerHotbar hotbar) {
        int gap = 6;
        int slotSize = Math.max(42, Math.min(56, (width - gap * 8) / EchoVoxelPlayerHotbar.HOTBAR_COUNT));
        int totalWidth = slotSize * EchoVoxelPlayerHotbar.HOTBAR_COUNT + gap * 8;
        int y = Math.max(8, height - slotSize - 18);
        int startX = Math.max(0, (width - totalWidth) / 2);
        for (EchoVoxelHotbarSlot slot : hotbar.hotbarSlots()) {
            int slotX = startX + slot.index() * (slotSize + gap);
            boolean selected = slot.index() == hotbar.selectedSlot();
            g.setColor(selected ? new Color(113, 211, 183, 96) : new Color(4, 10, 11, 196));
            g.fillRoundRect(slotX, y, slotSize, slotSize, 8, 8);
            g.setColor(selected ? ACCENT : LINE);
            g.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
            g.drawRoundRect(slotX, y, slotSize, slotSize, 8, 8);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            g.setColor(selected ? ACCENT : MUTED);
            g.drawString(String.valueOf(slot.index() + 1), slotX + 6, y + 14);
            if (!slot.empty()) {
                g.setColor(new Color(slot.block().argb(), true));
                g.fillRect(slotX + slotSize / 2 - 8, y + 18, 16, 16);
                g.setColor(TEXT);
                g.drawString(String.valueOf(slot.count()), slotX + slotSize - 20, y + slotSize - 8);
            }
        }
    }

    private static void drawShellOverlay(Graphics2D g, int width, int height, EchoVoxelHudOverlay overlay, boolean compact) {
        g.setColor(new Color(0, 0, 0, 120));
        g.fillRect(0, 0, width, height);
        int panelWidth = Math.min(width - 48, compact ? 520 : 620);
        int lineCount = Math.max(1, overlay.shellLines().size());
        int panelHeight = Math.min(height - 64, 92 + lineCount * 26);
        int x = Math.max(24, (width - panelWidth) / 2);
        int y = Math.max(32, (height - panelHeight) / 2);
        panel(g, x, y, panelWidth, panelHeight, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compact ? 24 : 30));
        g.setColor(TEXT);
        g.drawString(trim(overlay.shellTitle(), 28), x + 24, y + 42);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 13 : 14));
        for (int index = 0; index < overlay.shellLines().size(); index++) {
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString(trim(overlay.shellLines().get(index), compact ? 50 : 62), x + 26, y + 78 + index * 26);
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        g.setColor(WARNING);
        g.drawString("Standalone OpenGL game shell", x + panelWidth - 246, y + panelHeight - 18);
    }

    private static void drawInventoryOverlay(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        g.setColor(new Color(0, 0, 0, 126));
        g.fillRect(0, 0, width, height);
        int slotSize = compact ? 38 : 46;
        int gap = compact ? 5 : 7;
        int gridWidth = slotSize * EchoVoxelPlayerHotbar.HOTBAR_COUNT + gap * 8;
        int panelWidth = Math.min(width - 48, Math.max(gridWidth + 52, compact ? 520 : 620));
        int panelHeight = compact ? 292 : 338;
        int x = Math.max(24, (width - panelWidth) / 2);
        int y = Math.max(28, (height - panelHeight) / 2);
        panel(g, x, y, panelWidth, panelHeight, 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, compact ? 22 : 28));
        g.setColor(TEXT);
        g.drawString("Inventory", x + 24, y + 38);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        g.setColor(MUTED);
        g.drawString(trim("AdapterCore hotbar + survival carry grid", compact ? 48 : 62), x + 24, y + 60);

        int gridX = x + Math.max(24, (panelWidth - gridWidth) / 2);
        int gridY = y + (compact ? 84 : 96);
        List<EchoVoxelHotbarSlot> carry = overlay.hotbar().carrySlots();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < EchoVoxelPlayerHotbar.HOTBAR_COUNT; column++) {
                int slotIndex = row * EchoVoxelPlayerHotbar.HOTBAR_COUNT + column;
                int slotX = gridX + column * (slotSize + gap);
                int slotY = gridY + row * (slotSize + gap);
                g.setColor(new Color(4, 10, 11, 196));
                g.fillRoundRect(slotX, slotY, slotSize, slotSize, 7, 7);
                g.setColor(LINE);
                g.drawRoundRect(slotX, slotY, slotSize, slotSize, 7, 7);
                if (slotIndex < carry.size() && !carry.get(slotIndex).empty()) {
                    EchoVoxelHotbarSlot slot = carry.get(slotIndex);
                    g.setColor(new Color(slot.block().argb(), true));
                    g.fillRect(slotX + slotSize / 2 - 8, slotY + slotSize / 2 - 8, 16, 16);
                    g.setColor(TEXT);
                    g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
                    g.drawString(String.valueOf(slot.count()), slotX + slotSize - 18, slotY + slotSize - 6);
                }
            }
        }

        int hotbarY = gridY + 3 * (slotSize + gap) + (compact ? 10 : 14);
        for (EchoVoxelHotbarSlot slot : overlay.hotbar().hotbarSlots()) {
            int slotX = gridX + slot.index() * (slotSize + gap);
            boolean selected = slot.index() == overlay.hotbar().selectedSlot();
            g.setColor(selected ? new Color(113, 211, 183, 108) : new Color(4, 10, 11, 218));
            g.fillRoundRect(slotX, hotbarY, slotSize, slotSize, 7, 7);
            g.setColor(selected ? ACCENT : LINE);
            g.setStroke(new BasicStroke(selected ? 2.0f : 1.0f));
            g.drawRoundRect(slotX, hotbarY, slotSize, slotSize, 7, 7);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
            g.setColor(selected ? ACCENT : MUTED);
            g.drawString(String.valueOf(slot.index() + 1), slotX + 6, hotbarY + 14);
            if (!slot.empty()) {
                g.setColor(new Color(slot.block().argb(), true));
                g.fillRect(slotX + slotSize / 2 - 8, hotbarY + slotSize / 2 - 8, 16, 16);
                g.setColor(TEXT);
                g.drawString(String.valueOf(slot.count()), slotX + slotSize - 20, hotbarY + slotSize - 8);
            }
        }

        drawIndexInventoryDrawer(g, x, y, panelWidth, panelHeight, overlay, compact);

        int textY = hotbarY + slotSize + 22;
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        for (int index = 0; index < Math.min(overlay.shellLines().size(), 4); index++) {
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString(trim(overlay.shellLines().get(index), compact ? 52 : 64), x + 24, textY + index * 18);
        }
    }

    private static void drawIndexInventoryDrawer(
            Graphics2D g,
            int inventoryX,
            int inventoryY,
            int inventoryWidth,
            int inventoryHeight,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        int drawerWidth = Math.min(compact ? 184 : 224, Math.max(150, inventoryWidth / 3));
        int drawerHeight = Math.min(inventoryHeight - 34, compact ? 218 : 252);
        int drawerX = inventoryX + inventoryWidth - drawerWidth - 18;
        int drawerY = inventoryY + 72;
        g.setColor(new Color(2, 16, 18, 222));
        g.fillRoundRect(drawerX, drawerY, drawerWidth, drawerHeight, 8, 8);
        g.setColor(ACCENT);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(drawerX, drawerY, drawerWidth, drawerHeight, 8, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 15 : 17));
        g.setColor(ACCENT);
        g.drawString("INDEX", drawerX + 12, drawerY + 24);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 10 : 11));
        g.setColor(MUTED);
        g.drawString("echoindex:inventory_overlay", drawerX + 12, drawerY + 42);
        List<String> lines = overlay.shellLines();
        int rowY = drawerY + 66;
        for (int index = 0; index < Math.min(4, lines.size()); index++) {
            g.setColor(index == 0 ? TEXT : MUTED);
            g.drawString(trim(lines.get(index), compact ? 25 : 32), drawerX + 12, rowY + index * 18);
        }
        int actionY = drawerY + drawerHeight - 42;
        g.setColor(new Color(113, 211, 183, 52));
        g.fillRoundRect(drawerX + 10, actionY - 12, drawerWidth - 20, 32, 6, 6);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 10 : 11));
        g.setColor(TEXT);
        g.drawString("R Recipes   U Uses", drawerX + 18, actionY);
        g.setColor(ACCENT);
        g.drawString("B Bookmark", drawerX + 18, actionY + 15);
    }

    private static void drawTerminalOverlay(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        EchoAshfallPlayerFeedback feedback = feedback(overlay);
        g.setColor(new Color(0, 0, 0, 148));
        g.fillRect(0, 0, width, height);
        int panelWidth = Math.min(width - 48, compact ? 560 : 700);
        int panelHeight = Math.min(height - 56, compact ? 282 : 336);
        int x = Math.max(24, (width - panelWidth) / 2);
        int y = Math.max(28, (height - panelHeight) / 2);
        panel(g, x, y, panelWidth, panelHeight, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 20 : 25));
        g.setColor(ACCENT);
        g.drawString("ASHFALL FIELD TERMINAL", x + 24, y + 38);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        g.setColor(MUTED);
        g.drawString("adaptercore://echoashfallprotocol/mission/terminal", x + 24, y + 60);

        int leftX = x + 24;
        int rightX = x + Math.max(300, panelWidth / 2 + 10);
        int textY = y + 92;
        terminalLine(g, leftX, textY, "STATUS", overlay.mission().status(), overlay.mission().extracted() ? ACCENT : WARNING);
        terminalLine(g, leftX, textY + 24, "TERMINAL", overlay.mission().terminalState(), ACCENT);
        terminalLine(g, leftX, textY + 48, "CACHE", overlay.mission().cacheRecovered() ? "RECOVERED" : "ROUTE UNLOCKED", WARNING);
        terminalLine(g, leftX, textY + 72, "POWER", overlay.mission().powerRepaired() ? "REPAIRED" : "DAMAGED", overlay.mission().powerRepaired() ? ACCENT : DANGER);
        terminalLine(g, leftX, textY + 96, "EXTRACT", overlay.mission().extractionStatus(), overlay.mission().powerRepaired() ? ACCENT : WARNING);
        terminalLine(g, leftX, textY + 120, "NEXT", trim(feedback.currentObjective(), compact ? 25 : 34), TEXT);

        g.setColor(TEXT);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.drawString("MISSION LOG", rightX, textY);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        List<String> feed = overlay.mission().terminalNotes();
        for (int index = 0; index < Math.min(5, feed.size()); index++) {
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString("> " + trim(feed.get(index), compact ? 26 : 34), rightX, textY + 24 + index * 22);
        }

        int controlsY = y + panelHeight - 54;
        g.setColor(new Color(113, 211, 183, 52));
        g.fillRoundRect(x + 18, controlsY - 18, panelWidth - 36, 42, 6, 6);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        for (int index = 0; index < Math.min(2, overlay.shellLines().size()); index++) {
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString(trim(overlay.shellLines().get(index), compact ? 54 : 68), x + 28, controlsY + index * 18);
        }
    }

    private static void drawMissionLogOverlay(
            Graphics2D g,
            int width,
            int height,
            EchoVoxelHudOverlay overlay,
            boolean compact
    ) {
        EchoAshfallPlayerFeedback feedback = feedback(overlay);
        g.setColor(new Color(0, 0, 0, 138));
        g.fillRect(0, 0, width, height);
        int panelWidth = Math.min(width - 48, compact ? 560 : 700);
        int panelHeight = Math.min(height - 56, compact ? 296 : 340);
        int x = Math.max(24, (width - panelWidth) / 2);
        int y = Math.max(28, (height - panelHeight) / 2);
        panel(g, x, y, panelWidth, panelHeight, 8);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 20 : 25));
        g.setColor(TEXT);
        g.drawString("ASHFALL MISSION LOG", x + 24, y + 38);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        g.setColor(MUTED);
        g.drawString("adaptercore://echoashfallprotocol/mission/live-state", x + 24, y + 60);

        int progressX = x + 24;
        int progressY = y + 82;
        int progressWidth = Math.min(panelWidth - 48, compact ? 420 : 520);
        int progressFill = (int) Math.round(
                progressWidth * (overlay.mission().completedObjectives() / (double) overlay.mission().totalObjectives())
        );
        g.setColor(new Color(4, 10, 11, 196));
        g.fillRoundRect(progressX, progressY, progressWidth, 12, 6, 6);
        g.setColor(ACCENT);
        g.fillRoundRect(progressX, progressY, Math.max(8, progressFill), 12, 6, 6);
        g.setColor(LINE);
        g.drawRoundRect(progressX, progressY, progressWidth, 12, 6, 6);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.setColor(TEXT);
        g.drawString(
                overlay.mission().status()
                        + " "
                        + overlay.mission().completedObjectives()
                        + "/"
                        + overlay.mission().totalObjectives()
                        + " NEXT "
                        + trim(overlay.mission().nextObjective(), compact ? 24 : 34),
                progressX,
                progressY + 32
        );

        int contentY = y + 122;
        int columnGap = compact ? 14 : 18;
        int columnWidth = Math.max(150, (panelWidth - 48 - columnGap * 2) / 3);
        drawRequiredObjectives(g, overlay.mission(), x + 24, contentY, columnWidth, compact);
        drawOptionalObjectives(g, overlay.mission(), x + 24 + columnWidth + columnGap, contentY, columnWidth, compact);
        drawMissionNotes(g, overlay.mission(), x + 24 + (columnWidth + columnGap) * 2, contentY, columnWidth, compact);

        int statusY = y + panelHeight - 86;
        int statGap = compact ? 124 : 136;
        terminalLine(g, x + 24, statusY, "HP", String.valueOf(overlay.mission().playerHealth()),
                overlay.mission().playerHealth() > 0 ? ACCENT : DANGER);
        terminalLine(g, x + 24 + statGap, statusY, "HYD", String.format("%.0f", overlay.mission().hydration()),
                overlay.mission().hydration() > 25.0D ? ACCENT : WARNING);
        terminalLine(g, x + 24 + statGap * 2, statusY, "HNG", String.format("%.0f", overlay.mission().hunger()),
                overlay.mission().hunger() > 20.0D ? ACCENT : WARNING);
        terminalLine(g, x + 24 + statGap * 3, statusY, "ASH", String.format("%.1f", overlay.mission().ashExposure()),
                overlay.mission().ashExposure() < 70.0D ? WARNING : DANGER);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        g.setColor(MUTED);
        g.drawString(trim("Hint: " + feedback.currentHint(), compact ? 60 : 82), x + 24, statusY + 24);
        g.drawString(trim("Terminal: " + overlay.mission().terminalState()
                + " / Extraction: " + overlay.mission().extractionStatus(), compact ? 60 : 82), x + 24, statusY + 42);

        int controlsY = y + panelHeight - 48;
        g.setColor(new Color(113, 211, 183, 52));
        g.fillRoundRect(x + 18, controlsY - 18, panelWidth - 36, 40, 6, 6);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 12 : 13));
        for (int index = 0; index < Math.min(2, overlay.shellLines().size()); index++) {
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString(trim(overlay.shellLines().get(index), compact ? 54 : 70), x + 28, controlsY + index * 17);
        }
    }

    private static void terminalLine(Graphics2D g, int x, int y, String label, String value, Color valueColor) {
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        g.setColor(MUTED);
        g.drawString(label, x, y);
        g.setColor(valueColor);
        g.drawString(value, x + 92, y);
    }

    private static void drawRequiredObjectives(
            Graphics2D g,
            EchoAshfallLiveMissionState mission,
            int x,
            int y,
            int width,
            boolean compact
    ) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.setColor(TEXT);
        g.drawString("REQUIRED", x, y);
        List<String> required = mission.requiredObjectiveLabels();
        int firstOpen = Math.min(Math.max(0, mission.completedObjectives()), required.size() - 1);
        int visibleRows = compact ? 5 : 6;
        int start = Math.max(0, Math.min(firstOpen - 2, Math.max(0, required.size() - visibleRows)));
        for (int row = 0; row < Math.min(visibleRows, required.size() - start); row++) {
            int index = start + row;
            boolean complete = index < mission.completedObjectives();
            int rowY = y + 22 + row * 18;
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 11 : 12));
            g.setColor(complete ? ACCENT : WARNING);
            g.drawString(complete ? "[x]" : "[ ]", x, rowY);
            g.setColor(complete ? TEXT : MUTED);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 11 : 12));
            g.drawString(trim(required.get(index), compact ? 18 : 22), x + 30, rowY);
        }
        g.setColor(MUTED);
        g.drawString(trim("Next: " + mission.nextObjective(), compact ? 24 : 30), x, y + 22 + visibleRows * 18);
    }

    private static void drawOptionalObjectives(
            Graphics2D g,
            EchoAshfallLiveMissionState mission,
            int x,
            int y,
            int width,
            boolean compact
    ) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.setColor(TEXT);
        g.drawString("OPTIONAL", x, y);
        List<String> optional = mission.optionalObjectiveLabels();
        for (int index = 0; index < optional.size(); index++) {
            int rowY = y + 22 + index * 18;
            g.setColor(optionalComplete(mission, index) ? ACCENT : MUTED);
            g.drawString(optionalComplete(mission, index) ? "[x]" : "[ ]", x, rowY);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 11 : 12));
            g.drawString(trim(optional.get(index), compact ? 19 : 24), x + 30, rowY);
            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        }
        g.setColor(TEXT);
        g.drawString("HISTORY", x, y + 104);
        List<String> history = mission.completedHistory();
        int historyStart = Math.max(0, history.size() - 3);
        for (int index = historyStart; index < history.size(); index++) {
            int row = index - historyStart;
            g.setColor(ACCENT);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 11 : 12));
            g.drawString(trim(history.get(index), compact ? 21 : 25), x, y + 126 + row * 17);
        }
    }

    private static void drawMissionNotes(
            Graphics2D g,
            EchoAshfallLiveMissionState mission,
            int x,
            int y,
            int width,
            boolean compact
    ) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.setColor(TEXT);
        g.drawString("TERMINAL", x, y);
        List<String> notes = mission.terminalNotes();
        for (int index = 0; index < Math.min(5, notes.size()); index++) {
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 11 : 12));
            g.setColor(index == 0 ? ACCENT : MUTED);
            g.drawString(trim(notes.get(index), compact ? 20 : 25), x, y + 22 + index * 18);
        }
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, compact ? 12 : 13));
        g.setColor(TEXT);
        g.drawString("EXTRACTION", x, y + 124);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, compact ? 11 : 12));
        g.setColor(mission.extractionArmed() || mission.extracted() ? ACCENT : WARNING);
        g.drawString(trim(mission.extractionStatus(), compact ? 20 : 25), x, y + 146);
    }

    private static boolean optionalComplete(EchoAshfallLiveMissionState mission, int index) {
        return switch (index) {
            case 0 -> mission.waterUsed();
            case 1 -> mission.foodUsed();
            case 2 -> mission.crossedAsh();
            case 3 -> mission.scavengedSupplies();
            default -> false;
        };
    }

    private static EchoAshfallPlayerFeedback feedback(EchoVoxelHudOverlay overlay) {
        return EchoAshfallPlayerFeedback.from(
                overlay.mission(),
                overlay.hotbar(),
                overlay.targetAvailable(),
                overlay.actionLabel()
        );
    }

    private static double blockBreakProgress(EchoVoxelHudOverlay overlay) {
        if (!overlay.targetAvailable()) {
            return 0.0D;
        }
        String text = (overlay.actionLabel() + " " + overlay.targetLabel()).toLowerCase(java.util.Locale.ROOT);
        if (!(text.contains("mine")
                || text.contains("mining")
                || text.contains("break")
                || text.contains("crack")
                || text.contains("harvest"))) {
            return 0.0D;
        }
        int hash = Math.abs(hash(text.hashCode()));
        return 0.35D + (hash % 50) / 100.0D;
    }

    private static boolean actionParticlesActive(EchoVoxelHudOverlay overlay) {
        String text = (overlay.actionLabel() + " " + overlay.targetLabel()).toLowerCase(java.util.Locale.ROOT);
        return text.contains("mine")
                || text.contains("mining")
                || text.contains("break")
                || text.contains("place")
                || text.contains("placed")
                || text.contains("harvest")
                || text.contains("pickup");
    }

    private static Color particleColor(int seed, int index) {
        int choice = Math.abs(hash(seed + index * 13)) % 4;
        return switch (choice) {
            case 0 -> new Color(236, 205, 132);
            case 1 -> new Color(173, 132, 93);
            case 2 -> new Color(112, 211, 183);
            default -> new Color(216, 104, 82);
        };
    }

    private static void panel(Graphics2D g, int x, int y, int width, int height, int radius) {
        g.setColor(PANEL);
        g.fillRoundRect(x, y, width, height, radius, radius);
        g.setColor(LINE);
        g.drawRoundRect(x, y, width, height, radius, radius);
    }

    private static void metric(Graphics2D g, int x, int y, String label, String value, Color valueColor) {
        g.setColor(MUTED);
        g.drawString(label, x, y);
        g.setColor(valueColor);
        g.drawString(value, x + 136, y);
    }

    private static String trim(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static int hash(int value) {
        int x = value;
        x = ((x >>> 16) ^ x) * 0x45D9F3B;
        x = ((x >>> 16) ^ x) * 0x45D9F3B;
        return (x >>> 16) ^ x;
    }

    private static long checksum(int[] pixels) {
        long hash = 0xcbf29ce484222325L;
        for (int pixel : pixels) {
            hash ^= pixel & 0xFFFFFFFFL;
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
