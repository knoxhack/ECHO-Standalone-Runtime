package dev.echo.standalone.runtime.client;

import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

final class EchoClientScreenshotService {
    private static final DateTimeFormatter FILE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

    private final Path screenshotRoot;

    EchoClientScreenshotService() {
        this(Path.of("screenshots"));
    }

    EchoClientScreenshotService(Path screenshotRoot) {
        this.screenshotRoot = screenshotRoot.toAbsolutePath().normalize();
    }

    Path captureFramebuffer(int width, int height) throws IOException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("screenshot dimensions must be positive");
        }
        Path target = nextAvailablePath(screenshotRoot, LocalDateTime.now());
        Files.createDirectories(target.getParent());

        ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
        GL11.glReadBuffer(GL11.GL_BACK);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        BufferedImage image = imageFromRgbaFramebuffer(width, height, pixels);
        if (!ImageIO.write(image, "png", target.toFile())) {
            throw new IOException("No PNG writer available for screenshot");
        }
        return target;
    }

    static BufferedImage imageFromRgbaFramebuffer(int width, int height, ByteBuffer pixels) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("image dimensions must be positive");
        }
        if (pixels == null || pixels.capacity() < width * height * 4) {
            throw new IllegalArgumentException("pixel buffer is too small for image dimensions");
        }
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            int readY = height - 1 - y;
            for (int x = 0; x < width; x++) {
                int offset = (readY * width + x) * 4;
                int r = pixels.get(offset) & 0xFF;
                int g = pixels.get(offset + 1) & 0xFF;
                int b = pixels.get(offset + 2) & 0xFF;
                int a = pixels.get(offset + 3) & 0xFF;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return image;
    }

    static Path nextAvailablePath(Path root, LocalDateTime timestamp) throws IOException {
        Path normalizedRoot = root.toAbsolutePath().normalize();
        String baseName = fileName(timestamp, 0);
        Path first = normalizedRoot.resolve(baseName);
        if (!Files.exists(first)) {
            return first;
        }
        for (int sequence = 1; sequence < 10_000; sequence++) {
            Path candidate = normalizedRoot.resolve(fileName(timestamp, sequence));
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }
        throw new IOException("Unable to allocate screenshot filename for " + normalizedRoot);
    }

    static String fileName(LocalDateTime timestamp, int sequence) {
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
        String prefix = "echo-" + FILE_TIME.format(timestamp);
        return sequence == 0
                ? prefix + ".png"
                : prefix + "_" + sequence + ".png";
    }
}
