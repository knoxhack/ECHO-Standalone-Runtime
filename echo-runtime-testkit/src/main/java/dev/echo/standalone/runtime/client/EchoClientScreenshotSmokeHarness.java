package dev.echo.standalone.runtime.client;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public final class EchoClientScreenshotSmokeHarness {
    private EchoClientScreenshotSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        LocalDateTime timestamp = LocalDateTime.of(2026, 6, 3, 14, 5, 6);
        require(EchoClientScreenshotService.fileName(timestamp, 0).equals("echo-2026-06-03_14.05.06.png"),
                "Screenshot filename should use a stable Minecraft-like timestamp");
        require(EchoClientScreenshotService.fileName(timestamp, 7).equals("echo-2026-06-03_14.05.06_7.png"),
                "Screenshot collision suffix should be stable");

        Path root = Files.createTempDirectory("echo-client-screenshot-smoke");
        Files.write(root.resolve(EchoClientScreenshotService.fileName(timestamp, 0)), new byte[] {1});
        Path next = EchoClientScreenshotService.nextAvailablePath(root, timestamp);
        require(next.getFileName().toString().equals("echo-2026-06-03_14.05.06_1.png"),
                "Screenshot path allocator should skip existing files");

        ByteBuffer rgba = ByteBuffer.allocateDirect(2 * 2 * 4);
        putRgba(rgba, 255, 0, 0, 255);
        putRgba(rgba, 0, 255, 0, 255);
        putRgba(rgba, 0, 0, 255, 255);
        putRgba(rgba, 255, 255, 255, 255);
        var image = EchoClientScreenshotService.imageFromRgbaFramebuffer(2, 2, rgba);
        require((image.getRGB(0, 0) & 0x00FFFFFF) == 0x000000FF,
                "Screenshot conversion should flip OpenGL bottom row to image top row");
        require((image.getRGB(1, 0) & 0x00FFFFFF) == 0x00FFFFFF,
                "Screenshot conversion should preserve top-right color after flip");
        require((image.getRGB(0, 1) & 0x00FFFFFF) == 0x00FF0000,
                "Screenshot conversion should preserve bottom-left color after flip");

        System.out.println("client screenshot smoke PASS file=" + next.getFileName());
    }

    private static void putRgba(ByteBuffer buffer, int r, int g, int b, int a) {
        buffer.put((byte) r);
        buffer.put((byte) g);
        buffer.put((byte) b);
        buffer.put((byte) a);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
