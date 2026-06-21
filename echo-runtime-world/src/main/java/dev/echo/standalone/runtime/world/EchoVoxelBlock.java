package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockBehaviorContract;
import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehavior;
import dev.echo.standalone.runtime.world.block.behavior.EchoBlockBehaviorRegistry;

import java.util.Objects;
import java.util.Optional;

public record EchoVoxelBlock(
        String id,
        String displayName,
        int argb,
        int detailArgb,
        String atlasKey,
        EchoVoxelMaterialPattern materialPattern,
        boolean solid,
        boolean opaque,
        double hardness,
        Optional<EchoBlockBehaviorContract> behavior
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
            0.0D,
            Optional.of(EchoBlockBehavior.air("echo:air"))
    );

    public EchoVoxelBlock {
        id = requireText(id, "id");
        displayName = requireText(displayName, "displayName");
        atlasKey = requireText(atlasKey, "atlasKey");
        materialPattern = Objects.requireNonNull(materialPattern, "materialPattern");
        behavior = behavior == null ? Optional.empty() : behavior;
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
                hardness,
                Optional.empty()
        );
    }

    public EchoVoxelBlock(
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
        this(
                id,
                displayName,
                argb,
                detailArgb,
                atlasKey,
                materialPattern,
                solid,
                opaque,
                hardness,
                Optional.empty()
        );
    }

    public EchoVoxelBlock(
            String id,
            String displayName,
            int argb,
            int detailArgb,
            String atlasKey,
            EchoVoxelMaterialPattern materialPattern,
            boolean solid,
            boolean opaque,
            double hardness,
            EchoBlockBehaviorContract behavior
    ) {
        this(
                id,
                displayName,
                argb,
                detailArgb,
                atlasKey,
                materialPattern,
                solid,
                opaque,
                hardness,
                Optional.ofNullable(behavior)
        );
    }

    public boolean air() {
        return this == AIR || Objects.equals(id, AIR.id());
    }

    public EchoVoxelBlock withBehavior(EchoBlockBehaviorContract behavior) {
        return new EchoVoxelBlock(
                id,
                displayName,
                argb,
                detailArgb,
                atlasKey,
                materialPattern,
                solid,
                opaque,
                hardness,
                behavior
        );
    }

    public EchoVoxelBlock withBehavior(EchoBlockBehaviorRegistry registry) {
        return withBehavior(registry.get(id));
    }

    /**
     * Returns the attached behavior contract, if any.
     */
    public Optional<EchoBlockBehaviorContract> behavior() {
        return behavior;
    }

    /**
     * Returns the gameplay hardness. Uses the attached behavior contract when available,
     * otherwise falls back to the block's intrinsic hardness.
     */
    public double hardness() {
        return behavior.map(EchoBlockBehaviorContract::destroyTime).orElse(hardness);
    }

    /**
     * Returns whether this block is solid. Uses the attached behavior contract when available.
     */
    public boolean solid() {
        return behavior.map(EchoBlockBehaviorContract::solid).orElse(solid);
    }

    /**
     * Returns whether this block is opaque. Uses the attached behavior contract when available.
     */
    public boolean opaque() {
        return behavior.map(EchoBlockBehaviorContract::opaque).orElse(opaque);
    }

    /**
     * Returns whether this block blocks motion. Uses the attached behavior contract when available.
     */
    public boolean blocksMotion() {
        return behavior.map(EchoBlockBehaviorContract::blocksMotion).orElse(solid);
    }

    /**
     * Returns whether this block requires a tool to harvest. Uses the attached behavior contract when available.
     */
    public boolean requiresTool() {
        return behavior.map(EchoBlockBehaviorContract::requiresTool).orElse(false);
    }

    /**
     * Returns the harvest tool class for this block. Uses the attached behavior contract when available.
     */
    public String harvestTool() {
        return behavior.map(EchoBlockBehaviorContract::harvestTool).orElse("");
    }

    /**
     * Returns the harvest level required for this block. Uses the attached behavior contract when available.
     */
    public int harvestLevel() {
        return behavior.map(EchoBlockBehaviorContract::harvestLevel).orElse(0);
    }

    /**
     * Returns the light emission level [0,15]. Uses the attached behavior contract when available.
     */
    public int lightEmission() {
        return behavior.map(EchoBlockBehaviorContract::lightEmission).orElse(0);
    }

    /**
     * Returns the light opacity level [0,15]. Uses the attached behavior contract when available.
     */
    public int lightOpacity() {
        return behavior.map(EchoBlockBehaviorContract::lightOpacity).orElse(opaque ? 15 : 0);
    }

    /**
     * Returns whether this block is flammable. Uses the attached behavior contract when available.
     */
    public boolean flammable() {
        return behavior.map(EchoBlockBehaviorContract::flammable).orElse(false);
    }

    /**
     * Returns the explosion resistance. Uses the attached behavior contract when available.
     */
    public double explosionResistance() {
        return behavior.map(EchoBlockBehaviorContract::explosionResistance).orElse(0.0D);
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
