package dev.echo.standalone.runtime.client;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

record EchoClientSaveSlotThumbnailCapture(
        String source,
        byte[] pngBytes,
        int width,
        int height,
        int skyArgb,
        int terrainArgb,
        int accentArgb,
        int shadowArgb
) {
    static final EchoClientSaveSlotThumbnailCapture EMPTY =
            new EchoClientSaveSlotThumbnailCapture("", new byte[0], 0, 0, 0, 0, 0, 0);

    EchoClientSaveSlotThumbnailCapture {
        source = source == null ? "" : source.trim();
        pngBytes = pngBytes == null ? new byte[0] : pngBytes.clone();
        width = Math.max(0, width);
        height = Math.max(0, height);
        skyArgb = opaque(skyArgb);
        terrainArgb = opaque(terrainArgb);
        accentArgb = opaque(accentArgb);
        shadowArgb = opaque(shadowArgb);
        if (source.isBlank() || pngBytes.length == 0 || width <= 0 || height <= 0) {
            source = "";
            pngBytes = new byte[0];
            width = 0;
            height = 0;
            skyArgb = 0;
            terrainArgb = 0;
            accentArgb = 0;
            shadowArgb = 0;
        }
    }

    boolean captured() {
        return !source.isBlank() && pngBytes.length > 0 && width > 0 && height > 0;
    }

    public byte[] pngBytes() {
        return pngBytes.clone();
    }

    static EchoClientSaveSlotThumbnailCapture fromImage(String source, BufferedImage image) throws IOException {
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return EMPTY;
        }
        BufferedImage thumbnail = scaleToThumbnail(image);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(thumbnail, "png", output)) {
            throw new IOException("No PNG writer available for save slot thumbnail");
        }
        return new EchoClientSaveSlotThumbnailCapture(
                source,
                output.toByteArray(),
                thumbnail.getWidth(),
                thumbnail.getHeight(),
                averageBand(thumbnail, 0.0D, 0.34D),
                averageBand(thumbnail, 0.62D, 1.0D),
                accentColor(thumbnail),
                averageBand(thumbnail, 0.82D, 1.0D)
        );
    }

    private static BufferedImage scaleToThumbnail(BufferedImage source) {
        int targetWidth = EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_WIDTH;
        int targetHeight = EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_HEIGHT;
        double scale = Math.max(
                targetWidth / (double) source.getWidth(),
                targetHeight / (double) source.getHeight()
        );
        int scaledWidth = Math.max(targetWidth, (int) Math.round(source.getWidth() * scale));
        int scaledHeight = Math.max(targetHeight, (int) Math.round(source.getHeight() * scale));
        int offsetX = (targetWidth - scaledWidth) / 2;
        int offsetY = (targetHeight - scaledHeight) / 2;
        BufferedImage thumbnail = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = thumbnail.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(source, offsetX, offsetY, scaledWidth, scaledHeight, null);
        } finally {
            graphics.dispose();
        }
        return thumbnail;
    }

    private static int averageBand(BufferedImage image, double from, double to) {
        int startY = Math.max(0, Math.min(image.getHeight() - 1, (int) Math.floor(image.getHeight() * from)));
        int endY = Math.max(startY + 1, Math.min(image.getHeight(), (int) Math.ceil(image.getHeight() * to)));
        long r = 0L;
        long g = 0L;
        long b = 0L;
        long samples = 0L;
        for (int y = startY; y < endY; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                r += (argb >>> 16) & 0xFF;
                g += (argb >>> 8) & 0xFF;
                b += argb & 0xFF;
                samples++;
            }
        }
        if (samples == 0L) {
            return 0xFF000000;
        }
        return rgb((int) (r / samples), (int) (g / samples), (int) (b / samples));
    }

    private static int accentColor(BufferedImage image) {
        int best = image.getRGB(image.getWidth() / 2, image.getHeight() / 2);
        int bestScore = -1;
        for (int y = 0; y < image.getHeight(); y += 3) {
            for (int x = 0; x < image.getWidth(); x += 3) {
                int argb = image.getRGB(x, y);
                int r = (argb >>> 16) & 0xFF;
                int g = (argb >>> 8) & 0xFF;
                int b = argb & 0xFF;
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                int score = (max - min) * 2 + max;
                if (score > bestScore) {
                    bestScore = score;
                    best = argb;
                }
            }
        }
        return opaque(best);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (clampColor(r) << 16) | (clampColor(g) << 8) | clampColor(b);
    }

    private static int opaque(int argb) {
        return argb == 0 ? 0 : 0xFF000000 | (argb & 0x00FFFFFF);
    }

    private static int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
