package dev.echo.standalone.runtime.world;

import java.util.Objects;

public record EchoVoxelBlock(
        String id,
        String displayName,
        int argb,
        int detailArgb,
        String atlasKey,
        EchoVoxelMaterialPattern materialPattern,
        boolean solid,
        boolean opaque,
        double hardness
) {
    public static final EchoVoxelBlock AIR = new EchoVoxelBlock(
            "echo:air",
            "Air",
            0x00000000,
            0x00000000,
            "echo/block/air",
            EchoVoxelMaterialPattern.FLAT,
            false,
            false,
            0.0D
    );

    public EchoVoxelBlock {
        id = requireText(id, "id");
        displayName = requireText(displayName, "displayName");
        atlasKey = requireText(atlasKey, "atlasKey");
        materialPattern = Objects.requireNonNull(materialPattern, "materialPattern");
        if (hardness < 0.0D) {
            throw new IllegalArgumentException("hardness must not be negative");
        }
    }

    public EchoVoxelBlock(
            String id,
            String displayName,
            int argb,
            boolean solid,
            boolean opaque,
            double hardness
    ) {
        this(
                id,
                displayName,
                argb,
                defaultDetailArgb(argb),
                defaultAtlasKey(id),
                EchoVoxelMaterialPattern.infer(id),
                solid,
                opaque,
                hardness
        );
    }

    public boolean air() {
        return this == AIR || Objects.equals(id, AIR.id);
    }

    public EchoVoxelCollisionBox collisionBox() {
        return EchoVoxelCollisionBox.forBlock(this);
    }

    private static int defaultDetailArgb(int color) {
        int alpha = (color >>> 24) & 0xFF;
        int red = clamp((int) Math.round(((color >>> 16) & 0xFF) * 1.24D), 0, 255);
        int green = clamp((int) Math.round(((color >>> 8) & 0xFF) * 1.16D), 0, 255);
        int blue = clamp((int) Math.round((color & 0xFF) * 0.92D), 0, 255);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static String defaultAtlasKey(String id) {
        return requireText(id, "id").replace(':', '/');
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
