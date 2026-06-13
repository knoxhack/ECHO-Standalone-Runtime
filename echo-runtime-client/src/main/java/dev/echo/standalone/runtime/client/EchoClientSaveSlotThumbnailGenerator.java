package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

final class EchoClientSaveSlotThumbnailGenerator {
    static final String THUMBNAIL_PATH = "client/thumbnail.png";
    static final String THUMBNAIL_CODEC = "echo.client.thumbnail.v1";
    static final String GENERATED_THUMBNAIL_SOURCE = "saved_world_camera";
    static final String FRAMEBUFFER_THUMBNAIL_SOURCE = "opengl_framebuffer";
    static final String THUMBNAIL_SOURCE = GENERATED_THUMBNAIL_SOURCE;
    static final int THUMBNAIL_WIDTH = 160;
    static final int THUMBNAIL_HEIGHT = 90;

    private EchoClientSaveSlotThumbnailGenerator() {
    }

    static Snapshot writeThumbnail(
            EchoSaveTransaction transaction,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player
    ) throws IOException {
        return writeThumbnail(transaction, world, player, EchoClientSaveSlotThumbnailCapture.EMPTY);
    }

    static Snapshot writeThumbnail(
            EchoSaveTransaction transaction,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            EchoClientSaveSlotThumbnailCapture capture
    ) throws IOException {
        if (transaction == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }
        EchoVoxelWorld safeWorld = world == null
                ? EchoClientWorldTemplates.defaultTemplate().newSession("42", java.util.List.of()).world()
                : world;
        EchoVoxelPlayerState safePlayer = player == null
                ? new EchoVoxelPlayerState(
                safeWorld.spawnX(),
                safeWorld.spawnY(),
                safeWorld.spawnZ(),
                0.0D,
                safeWorld.spawnYawDegrees(),
                0.0D,
                true,
                false,
                false,
                0,
                EchoVoxelPlayerState.SURVIVAL_REACH
        )
                : player;
        EchoVoxelBiome biome = safeWorld.biomeAt(safePlayer.x(), safePlayer.z());
        if (capture != null && capture.captured()) {
            transaction.writeBytes(THUMBNAIL_PATH, capture.pngBytes());
            return new Snapshot(
                    THUMBNAIL_PATH,
                    capture.source(),
                    capture.width(),
                    capture.height(),
                    biome.id(),
                    safePlayer.x(),
                    safePlayer.y(),
                    safePlayer.z(),
                    safePlayer.yawDegrees(),
                    safePlayer.pitchDegrees(),
                    capture.skyArgb(),
                    capture.terrainArgb(),
                    capture.accentArgb(),
                    capture.shadowArgb()
            );
        }
        EchoClientBiomeEnvironment environment = EchoClientBiomeEnvironment.fromBiome(biome);
        int skyArgb = skyColor(environment, biome);
        int terrainArgb = terrainColor(safeWorld, biome, safePlayer);
        int accentArgb = accentColor(biome);
        int shadowArgb = darken(terrainArgb, 0.38D);
        BufferedImage image = render(safeWorld, safePlayer, skyArgb, terrainArgb, accentArgb, shadowArgb);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(image, "png", output)) {
            throw new IOException("No PNG writer available for save slot thumbnail");
        }
        transaction.writeBytes(THUMBNAIL_PATH, output.toByteArray());
        return new Snapshot(
                THUMBNAIL_PATH,
                GENERATED_THUMBNAIL_SOURCE,
                THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT,
                biome.id(),
                safePlayer.x(),
                safePlayer.y(),
                safePlayer.z(),
                safePlayer.yawDegrees(),
                safePlayer.pitchDegrees(),
                skyArgb,
                terrainArgb,
                accentArgb,
                shadowArgb
        );
    }

    private static BufferedImage render(
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            int skyArgb,
            int terrainArgb,
            int accentArgb,
            int shadowArgb
    ) {
        BufferedImage image = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        int horizon = 48;
        for (int y = 0; y < THUMBNAIL_HEIGHT; y++) {
            double t = y / (double) Math.max(1, THUMBNAIL_HEIGHT - 1);
            int color = y < horizon
                    ? blend(lighten(skyArgb, 1.22D), skyArgb, t * 1.35D)
                    : blend(terrainArgb, shadowArgb, Math.min(1.0D, (y - horizon) / 42.0D));
            for (int x = 0; x < THUMBNAIL_WIDTH; x++) {
                image.setRGB(x, y, color);
            }
        }

        int seed = (world.worldId() + "|" + world.seed() + "|" + player.yawDegrees()).hashCode();
        drawRidge(image, world, player, horizon, terrainArgb, shadowArgb, seed);
        drawCameraBeacon(image, player, accentArgb, shadowArgb);
        drawFrame(image, accentArgb, shadowArgb);
        return image;
    }

    private static void drawRidge(
            BufferedImage image,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            int horizon,
            int terrainArgb,
            int shadowArgb,
            int seed
    ) {
        double yaw = Math.toRadians(player.yawDegrees());
        double forwardX = Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        double sideX = Math.cos(yaw);
        double sideZ = -Math.sin(yaw);
        int previousX = 0;
        int previousY = horizon + ridgeOffset(seed, 0);
        for (int step = 1; step <= 12; step++) {
            double centered = (step - 6.0D) / 6.0D;
            int sampleX = (int) Math.floor(player.x() + forwardX * 9.0D + sideX * centered * 18.0D);
            int sampleZ = (int) Math.floor(player.z() + forwardZ * 9.0D + sideZ * centered * 18.0D);
            int surfaceY = surfaceY(world, sampleX, sampleZ);
            int nextX = Math.min(THUMBNAIL_WIDTH - 1, Math.round(step * (THUMBNAIL_WIDTH - 1) / 12.0F));
            int nextY = horizon + ridgeOffset(seed, step) - Math.max(-8, Math.min(12, surfaceY - 5));
            drawLine(image, previousX, previousY, nextX, nextY, lighten(terrainArgb, 1.10D));
            drawLine(image, previousX, previousY + 10, nextX, nextY + 10, shadowArgb);
            previousX = nextX;
            previousY = nextY;
        }
    }

    private static void drawCameraBeacon(
            BufferedImage image,
            EchoVoxelPlayerState player,
            int accentArgb,
            int shadowArgb
    ) {
        int centerX = THUMBNAIL_WIDTH / 2
                + Math.max(-22, Math.min(22, (int) Math.round(player.pitchDegrees() * 0.35D)));
        int baseY = 61;
        fillRect(image, centerX - 3, baseY - 26, 6, 30, shadowArgb);
        fillRect(image, centerX - 12, baseY - 30, 24, 5, accentArgb);
        drawLine(image, centerX, baseY - 31, centerX, 10, translucent(accentArgb, 0.70D));
        fillRect(image, centerX - 2, baseY - 39, 4, 4, lighten(accentArgb, 1.25D));
    }

    private static void drawFrame(BufferedImage image, int accentArgb, int shadowArgb) {
        for (int x = 0; x < THUMBNAIL_WIDTH; x++) {
            image.setRGB(x, 0, accentArgb);
            image.setRGB(x, THUMBNAIL_HEIGHT - 1, shadowArgb);
        }
        for (int y = 0; y < THUMBNAIL_HEIGHT; y++) {
            image.setRGB(0, y, shadowArgb);
            image.setRGB(THUMBNAIL_WIDTH - 1, y, shadowArgb);
        }
    }

    private static int surfaceY(EchoVoxelWorld world, int x, int z) {
        for (int y = world.chunkSize() - 1; y >= 0; y--) {
            EchoVoxelBlockState state = world.blockStateAt(x, y, z);
            if (state != null && !state.air()) {
                return y;
            }
        }
        return 0;
    }

    private static int skyColor(EchoClientBiomeEnvironment environment, EchoVoxelBiome biome) {
        int fog = rgb(
                Math.round(environment.fogRed() * 255.0F),
                Math.round(environment.fogGreen() * 255.0F),
                Math.round(environment.fogBlue() * 255.0F)
        );
        int base = biome.hasTag("cold") ? 0xFF5F7D88 : 0xFF425D61;
        return blend(base, fog, 0.54D);
    }

    private static int terrainColor(EchoVoxelWorld world, EchoVoxelBiome biome, EchoVoxelPlayerState player) {
        int blockHash = 0;
        int samples = 0;
        int centerX = (int) Math.floor(player.x());
        int centerZ = (int) Math.floor(player.z());
        for (int dz = -2; dz <= 2; dz++) {
            for (int dx = -2; dx <= 2; dx++) {
                int y = surfaceY(world, centerX + dx, centerZ + dz);
                EchoVoxelBlockState state = world.blockStateAt(centerX + dx, y, centerZ + dz);
                if (state != null && !state.air()) {
                    blockHash += state.block().id().hashCode();
                    samples++;
                }
            }
        }
        int grass = 0xFF000000 | (biome.grassColor() & 0x00FFFFFF);
        int blockTone = rgb(
                62 + Math.floorMod(blockHash, 56),
                72 + Math.floorMod(blockHash >>> 7, 48),
                54 + Math.floorMod(blockHash >>> 15, 42)
        );
        return blend(grass, blockTone, samples == 0 ? 0.20D : 0.42D);
    }

    private static int accentColor(EchoVoxelBiome biome) {
        if (biome.hasTag("toxic")) {
            return 0xFF8FE64B;
        }
        if (biome.hasTag("radiation")) {
            return 0xFFD8D15B;
        }
        if (biome.hasTag("cold")) {
            return 0xFF9BD9FF;
        }
        if (biome.hasTag("nexus") || biome.hasTag("anomaly")) {
            return 0xFFB98BFF;
        }
        if (biome.hasTag("industrial") || biome.hasTag("city")) {
            return 0xFFE4B15F;
        }
        return 0xFF2AD8BC;
    }

    private static int ridgeOffset(int seed, int step) {
        int mixed = seed + step * 211;
        return Math.floorMod(mixed ^ (mixed >>> 9), 21) - 10;
    }

    private static void fillRect(BufferedImage image, int x, int y, int width, int height, int argb) {
        for (int yy = Math.max(0, y); yy < Math.min(THUMBNAIL_HEIGHT, y + height); yy++) {
            for (int xx = Math.max(0, x); xx < Math.min(THUMBNAIL_WIDTH, x + width); xx++) {
                image.setRGB(xx, yy, argb);
            }
        }
    }

    private static void drawLine(BufferedImage image, int x0, int y0, int x1, int y1, int argb) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int error = dx + dy;
        int x = x0;
        int y = y0;
        while (true) {
            if (x >= 0 && x < THUMBNAIL_WIDTH && y >= 0 && y < THUMBNAIL_HEIGHT) {
                image.setRGB(x, y, argb);
                if (y + 1 < THUMBNAIL_HEIGHT) {
                    image.setRGB(x, y + 1, argb);
                }
            }
            if (x == x1 && y == y1) {
                return;
            }
            int doubled = 2 * error;
            if (doubled >= dy) {
                error += dy;
                x += sx;
            }
            if (doubled <= dx) {
                error += dx;
                y += sy;
            }
        }
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clampColor(r) << 16) | (clampColor(g) << 8) | clampColor(b);
    }

    private static int blend(int left, int right, double ratio) {
        double t = Math.max(0.0D, Math.min(1.0D, ratio));
        int r = (int) Math.round(component(left, 16) * (1.0D - t) + component(right, 16) * t);
        int g = (int) Math.round(component(left, 8) * (1.0D - t) + component(right, 8) * t);
        int b = (int) Math.round(component(left, 0) * (1.0D - t) + component(right, 0) * t);
        return rgb(r, g, b);
    }

    private static int lighten(int argb, double factor) {
        return rgb(
                (int) Math.round(component(argb, 16) * factor),
                (int) Math.round(component(argb, 8) * factor),
                (int) Math.round(component(argb, 0) * factor)
        );
    }

    private static int darken(int argb, double factor) {
        return lighten(argb, factor);
    }

    private static int translucent(int argb, double alpha) {
        int a = clampColor((int) Math.round(255.0D * Math.max(0.0D, Math.min(1.0D, alpha))));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private static int component(int argb, int shift) {
        return (argb >>> shift) & 0xFF;
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    record Snapshot(
            String relativePath,
            String source,
            int width,
            int height,
            String biomeId,
            double cameraX,
            double cameraY,
            double cameraZ,
            double cameraYawDegrees,
            double cameraPitchDegrees,
            int skyArgb,
            int terrainArgb,
            int accentArgb,
            int shadowArgb
    ) {
    }
}
