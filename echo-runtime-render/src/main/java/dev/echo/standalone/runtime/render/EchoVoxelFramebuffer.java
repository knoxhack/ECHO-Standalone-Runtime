package dev.echo.standalone.runtime.render;

import java.util.Arrays;
import java.util.HashSet;

public record EchoVoxelFramebuffer(
        int width,
        int height,
        int[] argb,
        int blocksVisited,
        int facesDrawn,
        long checksum
) {
    public EchoVoxelFramebuffer {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("framebuffer dimensions must be positive");
        }
        if (argb.length != width * height) {
            throw new IllegalArgumentException("argb length does not match dimensions");
        }
        argb = Arrays.copyOf(argb, argb.length);
    }

    public int uniqueColorCount() {
        HashSet<Integer> colors = new HashSet<>();
        for (int color : argb) {
            colors.add(color);
        }
        return colors.size();
    }
}
