package dev.echo.standalone.runtime.client;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

final class EchoClientSaveSlotThumbnailTextureCache {
    private final Map<String, Entry> textures = new HashMap<>();

    int textureId(EchoClientSaveSlotThumbnailSnapshot thumbnail) {
        if (!EchoClientUiRenderer.usesCapturedThumbnailTexture(thumbnail)) {
            return 0;
        }
        Path path;
        try {
            path = Path.of(thumbnail.resolvedPath()).toAbsolutePath().normalize();
        } catch (IllegalArgumentException exception) {
            return 0;
        }
        if (!Files.isRegularFile(path)) {
            return 0;
        }
        try {
            long modifiedMillis = Files.getLastModifiedTime(path).toMillis();
            long sizeBytes = Files.size(path);
            String key = path.toString();
            Entry cached = textures.get(key);
            if (cached != null
                    && cached.modifiedMillis() == modifiedMillis
                    && cached.sizeBytes() == sizeBytes
                    && cached.width() == thumbnail.width()
                    && cached.height() == thumbnail.height()) {
                return cached.textureId();
            }
            if (cached != null) {
                GL11.glDeleteTextures(cached.textureId());
            }
            Entry loaded = load(path, modifiedMillis, sizeBytes, thumbnail.width(), thumbnail.height());
            if (loaded == null) {
                textures.remove(key);
                return 0;
            }
            textures.put(key, loaded);
            return loaded.textureId();
        } catch (IOException | IllegalArgumentException exception) {
            return 0;
        }
    }

    void clear() {
        for (Entry entry : textures.values()) {
            GL11.glDeleteTextures(entry.textureId());
        }
        textures.clear();
    }

    private static Entry load(Path path, long modifiedMillis, long sizeBytes, int expectedWidth, int expectedHeight)
            throws IOException {
        BufferedImage image = ImageIO.read(path.toFile());
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
            return null;
        }
        if ((expectedWidth > 0 && image.getWidth() != expectedWidth)
                || (expectedHeight > 0 && image.getHeight() != expectedHeight)) {
            return null;
        }
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, image.getWidth(), image.getHeight(),
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, toRgba(image));
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        return new Entry(textureId, modifiedMillis, sizeBytes, image.getWidth(), image.getHeight());
    }

    private static ByteBuffer toRgba(BufferedImage image) {
        ByteBuffer pixels = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                pixels.put((byte) ((argb >>> 16) & 0xFF));
                pixels.put((byte) ((argb >>> 8) & 0xFF));
                pixels.put((byte) (argb & 0xFF));
                pixels.put((byte) ((argb >>> 24) & 0xFF));
            }
        }
        pixels.flip();
        return pixels;
    }

    private record Entry(int textureId, long modifiedMillis, long sizeBytes, int width, int height) {
    }
}
