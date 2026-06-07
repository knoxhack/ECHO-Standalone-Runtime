package dev.echo.standalone.runtime.render;

import java.util.Arrays;
import java.util.Objects;

public record EchoSoftwareFramebuffer(
        int width,
        int height,
        int[] argb,
        EchoSoftwareRenderStats stats
) {
    public EchoSoftwareFramebuffer {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be positive");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height must be positive");
        }
        Objects.requireNonNull(argb, "argb");
        if (argb.length != width * height) {
            throw new IllegalArgumentException("argb length must match framebuffer dimensions");
        }
        Objects.requireNonNull(stats, "stats");
        argb = Arrays.copyOf(argb, argb.length);
    }

    @Override
    public int[] argb() {
        return Arrays.copyOf(argb, argb.length);
    }

    public int pixel(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            throw new IllegalArgumentException("pixel outside framebuffer: " + x + "," + y);
        }
        return argb[y * width + x];
    }
}
