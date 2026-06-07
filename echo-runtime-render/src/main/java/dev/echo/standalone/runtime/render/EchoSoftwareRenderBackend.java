package dev.echo.standalone.runtime.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoSoftwareRenderBackend implements EchoRenderBackend {
    private static final List<EchoSoftwareRenderPass> PIPELINE = List.of(
            EchoSoftwareRenderPass.CLEAR,
            EchoSoftwareRenderPass.TILES,
            EchoSoftwareRenderPass.SPRITES,
            EchoSoftwareRenderPass.UI,
            EchoSoftwareRenderPass.LIGHTING,
            EchoSoftwareRenderPass.PARTICLES,
            EchoSoftwareRenderPass.DEBUG_OVERLAY
    );

    private final ArrayList<EchoRenderFrame> frames = new ArrayList<>();
    private final ArrayList<EchoSoftwareFramebuffer> framebuffers = new ArrayList<>();
    private final ArrayList<EchoRenderWindowEvent> events = new ArrayList<>();
    private EchoRenderWindowState window;
    private int eventSequence;

    @Override
    public String backendId() {
        return "echo:software_renderer";
    }

    @Override
    public synchronized EchoRenderWindowState openWindow(EchoRenderWindowSettings settings) {
        Objects.requireNonNull(settings, "settings");
        window = new EchoRenderWindowState(
                "window:software-debug",
                settings.title(),
                settings.viewport(),
                settings.mode(),
                true,
                false
        );
        record(EchoRenderWindowEventType.CREATED, "software window created");
        return window;
    }

    @Override
    public synchronized EchoRenderFrame render(EchoRenderScene scene) {
        Objects.requireNonNull(scene, "scene");
        requireOpenWindow();
        RenderCounters counters = new RenderCounters();
        int width = window.viewport().width();
        int height = window.viewport().height();
        int[] pixels = new int[width * height];
        int background = drawClear(pixels, width, height, scene, counters);
        drawTiles(pixels, width, height, scene, counters);
        drawSprites(pixels, width, height, scene, counters);
        drawUi(pixels, width, height, scene, counters);
        counters.litPixels = applyLighting(pixels, width, height, scene);
        drawParticles(pixels, width, height, scene, counters);
        drawDebugOverlay(pixels, width, height, scene, counters);

        long checksum = checksum(pixels);
        int nonBackgroundPixels = countNonBackgroundPixels(pixels, background);
        EchoSoftwareRenderStats stats = counters.stats(nonBackgroundPixels, checksum);
        EchoSoftwareFramebuffer framebuffer = new EchoSoftwareFramebuffer(width, height, pixels, stats);
        framebuffers.add(framebuffer);

        EchoRenderFrame frame = new EchoRenderFrame(
                frames.size(),
                window,
                scene,
                scene.commands().size(),
                List.of(
                        new EchoRenderDiagnostic(
                                EchoRenderDiagnosticSeverity.INFO,
                                "software pixels=" + nonBackgroundPixels + " checksum=" + Long.toUnsignedString(checksum)
                        ),
                        new EchoRenderDiagnostic(
                                EchoRenderDiagnosticSeverity.INFO,
                                "software passes=" + stats.passes().size()
                        )
                )
        );
        frames.add(frame);
        return frame;
    }

    @Override
    public synchronized EchoRenderWindowState resizeWindow(EchoRenderViewport viewport) {
        Objects.requireNonNull(viewport, "viewport");
        requireOpenWindow();
        window = window.withViewport(viewport);
        record(EchoRenderWindowEventType.RESIZED, "software window resized to " + viewport.width() + "x" + viewport.height());
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState setWindowMode(EchoRenderWindowMode mode) {
        Objects.requireNonNull(mode, "mode");
        requireOpenWindow();
        window = window.withMode(mode);
        record(mode == EchoRenderWindowMode.FULLSCREEN
                        ? EchoRenderWindowEventType.FULLSCREEN_ENTERED
                        : EchoRenderWindowEventType.WINDOWED_ENTERED,
                "software window mode set to " + mode.name());
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState requestClose(String reason) {
        requireOpenWindow();
        window = window.withCloseRequested(true);
        record(EchoRenderWindowEventType.CLOSE_REQUESTED, reason == null || reason.isBlank() ? "close requested" : reason);
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState closeAfterCrash(String operation) {
        if (window == null) {
            window = new EchoRenderWindowState(
                    "window:software-debug",
                    EchoRenderWindowSettings.windowedGame().title(),
                    EchoRenderWindowSettings.windowedGame().viewport(),
                    EchoRenderWindowMode.WINDOWED,
                    false,
                    true
            );
            record(EchoRenderWindowEventType.CRASH_SAFE_CLOSED, "software window closed after crash before creation");
            return window;
        }
        window = window.withCloseRequested(true).withOpen(false);
        record(EchoRenderWindowEventType.CRASH_SAFE_CLOSED, operation == null || operation.isBlank()
                ? "software window closed after crash"
                : "software window closed after crash:" + operation);
        return window;
    }

    @Override
    public synchronized Optional<EchoRenderWindowState> windowState() {
        return Optional.ofNullable(window);
    }

    @Override
    public synchronized List<EchoRenderWindowEvent> windowEvents() {
        return List.copyOf(events);
    }

    @Override
    public synchronized List<EchoRenderFrame> frames() {
        return List.copyOf(frames);
    }

    public synchronized List<EchoSoftwareFramebuffer> framebuffers() {
        return List.copyOf(framebuffers);
    }

    public synchronized Optional<EchoSoftwareFramebuffer> lastFramebuffer() {
        if (framebuffers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(framebuffers.get(framebuffers.size() - 1));
    }

    @Override
    public synchronized void close() {
        if (window == null || !window.open()) {
            return;
        }
        window = window.withOpen(false);
        record(EchoRenderWindowEventType.CLOSED, "software window closed");
    }

    private int drawClear(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int color = EchoRenderMaterialPalette.argb(255, 6, 16, 20);
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.BACKGROUND)) {
            if (command.type() == EchoRenderCommandType.CLEAR) {
                color = EchoRenderMaterialPalette.colorFor(command);
                counters.clearCommands += 1;
            }
        }
        fillRect(pixels, width, height, 0, 0, width, height, color);
        return color;
    }

    private void drawTiles(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int cellSize = cellSize(width, height);
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.WORLD)) {
            if (command.type() != EchoRenderCommandType.TILE) {
                continue;
            }
            int x = worldToScreenX(scene, command.x(), cellSize, width);
            int y = worldToScreenY(scene, command.y(), cellSize, height);
            fillRect(
                    pixels,
                    width,
                    height,
                    x,
                    y,
                    Math.max(1, (int) Math.round(command.width() * cellSize)),
                    Math.max(1, (int) Math.round(command.height() * cellSize)),
                    EchoRenderMaterialPalette.colorFor(command)
            );
            counters.tileCommands += 1;
        }
    }

    private void drawSprites(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int cellSize = cellSize(width, height);
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.ENTITY)) {
            if (command.type() != EchoRenderCommandType.ENTITY) {
                continue;
            }
            int spriteWidth = Math.max(1, (int) Math.round(command.width() * cellSize));
            int spriteHeight = Math.max(1, (int) Math.round(command.height() * cellSize));
            int centerX = worldToScreenX(scene, command.x(), cellSize, width);
            int baseY = worldToScreenY(scene, command.y(), cellSize, height);
            fillRect(
                    pixels,
                    width,
                    height,
                    centerX - spriteWidth / 2,
                    baseY - spriteHeight,
                    spriteWidth,
                    spriteHeight,
                    EchoRenderMaterialPalette.colorFor(command)
            );
            counters.spriteCommands += 1;
        }
    }

    private void drawUi(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int unitX = Math.max(6, width / 96);
        int unitY = Math.max(10, height / 45);
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.UI)) {
            int x = 16 + (int) Math.round(command.x() * unitX);
            int y = 16 + (int) Math.round(command.y() * unitY);
            int color = EchoRenderMaterialPalette.colorFor(command);
            if (command.type() == EchoRenderCommandType.UI_SURFACE) {
                fillRect(
                        pixels,
                        width,
                        height,
                        x,
                        y,
                        Math.max(1, (int) Math.round(command.width() * unitX)),
                        Math.max(1, (int) Math.round(command.height() * unitY)),
                        color
                );
            } else if (command.type() == EchoRenderCommandType.TEXT) {
                int textWidth = Math.max(8, Math.min(width - x - 16, command.label().length() * 6));
                fillRect(pixels, width, height, x, y, textWidth, Math.max(2, unitY / 4), color);
            }
            counters.uiCommands += 1;
        }
    }

    private int applyLighting(int[] pixels, int width, int height, EchoRenderScene scene) {
        int centerX = worldToScreenX(scene, scene.camera().x(), cellSize(width, height), width);
        int centerY = worldToScreenY(scene, scene.camera().z(), cellSize(width, height), height);
        double maxDistance = Math.max(width, height) * 0.65D;
        int litPixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                double dx = x - centerX;
                double dy = y - centerY;
                double distance = Math.sqrt(dx * dx + dy * dy);
                double light = Math.max(0.48D, 1.0D - (distance / maxDistance) * 0.38D);
                pixels[index] = multiply(pixels[index], light);
                litPixels += 1;
            }
        }
        return litPixels;
    }

    private void drawParticles(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int cellSize = cellSize(width, height);
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.PARTICLE)) {
            if (command.type() != EchoRenderCommandType.PARTICLE) {
                continue;
            }
            int particleSize = Math.max(3, (int) Math.round(Math.max(command.width(), command.height()) * cellSize));
            fillRect(
                    pixels,
                    width,
                    height,
                    worldToScreenX(scene, command.x(), cellSize, width),
                    worldToScreenY(scene, command.y(), cellSize, height),
                    particleSize,
                    particleSize,
                    EchoRenderMaterialPalette.colorFor(command)
            );
            counters.particleCommands += 1;
        }
    }

    private void drawDebugOverlay(int[] pixels, int width, int height, EchoRenderScene scene, RenderCounters counters) {
        int y = 8;
        for (EchoRenderCommand command : scene.commandsForLayer(EchoRenderLayer.DIAGNOSTIC)) {
            int barWidth = Math.max(24, Math.min(width / 3, command.label().length() * 7));
            fillRect(
                    pixels,
                    width,
                    height,
                    width - barWidth - 12,
                    y,
                    barWidth,
                    6,
                    EchoRenderMaterialPalette.colorFor(command)
            );
            y += 10;
            counters.debugCommands += 1;
        }
    }

    private void requireOpenWindow() {
        if (window == null || !window.open()) {
            throw new IllegalStateException("Software renderer has no open window");
        }
    }

    private void record(EchoRenderWindowEventType type, String detail) {
        eventSequence += 1;
        events.add(new EchoRenderWindowEvent(
                "software-window-event-" + eventSequence,
                type,
                window,
                detail
        ));
    }

    private static int cellSize(int width, int height) {
        return Math.max(8, Math.min(width, height) / 8);
    }

    private static int worldToScreenX(EchoRenderScene scene, double worldX, int cellSize, int width) {
        return width / 2 - (int) Math.round(scene.camera().x() * cellSize) + (int) Math.round(worldX * cellSize);
    }

    private static int worldToScreenY(EchoRenderScene scene, double worldZ, int cellSize, int height) {
        return height / 2 - (int) Math.round(scene.camera().z() * cellSize) + (int) Math.round(worldZ * cellSize);
    }

    private static void fillRect(int[] pixels, int width, int height, int x, int y, int rectWidth, int rectHeight, int color) {
        int startX = Math.max(0, x);
        int startY = Math.max(0, y);
        int endX = Math.min(width, x + rectWidth);
        int endY = Math.min(height, y + rectHeight);
        for (int py = startY; py < endY; py++) {
            for (int px = startX; px < endX; px++) {
                int index = py * width + px;
                pixels[index] = blend(pixels[index], color);
            }
        }
    }

    private static int multiply(int color, double factor) {
        int alpha = (color >>> 24) & 0xFF;
        int red = (color >>> 16) & 0xFF;
        int green = (color >>> 8) & 0xFF;
        int blue = color & 0xFF;
        return EchoRenderMaterialPalette.argb(
                alpha,
                (int) Math.round(red * factor),
                (int) Math.round(green * factor),
                (int) Math.round(blue * factor)
        );
    }

    private static int blend(int destination, int source) {
        int alpha = (source >>> 24) & 0xFF;
        if (alpha >= 255) {
            return source;
        }
        int inverse = 255 - alpha;
        int red = (((source >>> 16) & 0xFF) * alpha + ((destination >>> 16) & 0xFF) * inverse) / 255;
        int green = (((source >>> 8) & 0xFF) * alpha + ((destination >>> 8) & 0xFF) * inverse) / 255;
        int blue = ((source & 0xFF) * alpha + (destination & 0xFF) * inverse) / 255;
        return EchoRenderMaterialPalette.argb(255, red, green, blue);
    }

    private static long checksum(int[] pixels) {
        long hash = 0xcbf29ce484222325L;
        for (int pixel : pixels) {
            hash ^= pixel & 0xFFFFFFFFL;
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static int countNonBackgroundPixels(int[] pixels, int background) {
        int count = 0;
        for (int pixel : pixels) {
            if (pixel != background) {
                count += 1;
            }
        }
        return count;
    }

    private static final class RenderCounters {
        private int clearCommands;
        private int tileCommands;
        private int spriteCommands;
        private int uiCommands;
        private int particleCommands;
        private int debugCommands;
        private int litPixels;

        private EchoSoftwareRenderStats stats(int nonBackgroundPixels, long checksum) {
            return new EchoSoftwareRenderStats(
                    PIPELINE,
                    clearCommands,
                    tileCommands,
                    spriteCommands,
                    uiCommands,
                    particleCommands,
                    debugCommands,
                    litPixels,
                    nonBackgroundPixels,
                    checksum
            );
        }
    }
}
