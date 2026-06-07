package dev.echo.standalone.runtime.assets;

public final class EchoMissingTexture {
    public static final String LOGICAL_ID = "echocore:missing_texture";
    private static final int MAGENTA_R = 255;
    private static final int MAGENTA_G = 0;
    private static final int MAGENTA_B = 255;

    private EchoMissingTexture() {
    }

    public static byte[] rgbaChecker(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        byte[] pixels = new byte[size * size * 4];
        int cell = Math.max(1, size / 4);
        int index = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean magenta = ((x / cell) + (y / cell)) % 2 == 0;
                pixels[index++] = (byte) (magenta ? MAGENTA_R : 0);
                pixels[index++] = (byte) (magenta ? MAGENTA_G : 0);
                pixels[index++] = (byte) (magenta ? MAGENTA_B : 0);
                pixels[index++] = (byte) 255;
            }
        }
        return pixels;
    }
}
