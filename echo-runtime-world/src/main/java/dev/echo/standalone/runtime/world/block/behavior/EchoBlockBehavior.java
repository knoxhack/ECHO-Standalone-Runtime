package dev.echo.standalone.runtime.world.block.behavior;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockBehaviorContract;

/**
 * Default implementation of {@link EchoBlockBehaviorContract}.
 */
public record EchoBlockBehavior(
        String blockId,
        double hardness,
        double blastResistance,
        String harvestTool,
        int harvestLevel,
        int lightEmission,
        int lightOpacity,
        double friction,
        double jumpFactor,
        double speedFactor,
        boolean randomTick,
        boolean solid,
        boolean opaque,
        boolean requiresTool,
        boolean blocksMotion,
        boolean flammable,
        int fireSpreadSpeed
) implements EchoBlockBehaviorContract {

    public EchoBlockBehavior(String blockId) {
        this(blockId, 0.0D, 0.0D, "", 0, 0, 0, 0.6D, 1.0D, 1.0D, false, false, false, false, false, false, 0);
    }

    public EchoBlockBehavior {
        if (blockId == null || blockId.isBlank()) {
            throw new IllegalArgumentException("blockId must not be blank");
        }
        if (hardness < 0.0D) {
            throw new IllegalArgumentException("hardness must not be negative");
        }
        if (blastResistance < 0.0D) {
            throw new IllegalArgumentException("blastResistance must not be negative");
        }
        if (lightEmission < 0 || lightEmission > 15) {
            throw new IllegalArgumentException("lightEmission must be in [0, 15]");
        }
        if (lightOpacity < 0 || lightOpacity > 15) {
            throw new IllegalArgumentException("lightOpacity must be in [0, 15]");
        }
        if (friction < 0.0D) {
            throw new IllegalArgumentException("friction must not be negative");
        }
        if (harvestTool == null) {
            harvestTool = "";
        }
        if (fireSpreadSpeed < 0) {
            fireSpreadSpeed = 0;
        }
    }

    @Override
    public double destroyTime() {
        return hardness;
    }

    @Override
    public double explosionResistance() {
        return blastResistance;
    }

    @Override
    public boolean requiresTool() {
        return requiresTool;
    }

    @Override
    public boolean blocksMotion() {
        return blocksMotion;
    }

    @Override
    public boolean flammable() {
        return flammable;
    }

    @Override
    public int fireSpreadSpeed() {
        return fireSpreadSpeed;
    }

    /**
     * Returns a behavior with air-like defaults for the given block ID.
     */
    public static EchoBlockBehavior air(String blockId) {
        return new EchoBlockBehavior(blockId, 0.0D, 0.0D, "", 0, 0, 0, 0.6D, 1.0D, 1.0D, false, false, false, false, false, false, 0);
    }

    /**
     * Returns a behavior with typical stone defaults.
     */
    public static EchoBlockBehavior stone(String blockId) {
        return new EchoBlockBehavior(blockId, 1.5D, 6.0D, "pickaxe", 0, 0, 15, 0.6D, 1.0D, 1.0D, false, true, true, true, true, false, 0);
    }

    /**
     * Returns a behavior with typical dirt defaults.
     */
    public static EchoBlockBehavior dirt(String blockId) {
        return new EchoBlockBehavior(blockId, 0.5D, 0.5D, "shovel", 0, 0, 15, 0.6D, 1.0D, 1.0D, false, true, true, false, true, false, 0);
    }

    /**
     * Returns a behavior with typical wood defaults.
     */
    public static EchoBlockBehavior wood(String blockId) {
        return new EchoBlockBehavior(blockId, 2.0D, 3.0D, "axe", 0, 0, 15, 0.6D, 1.0D, 1.0D, false, true, true, false, true, true, 5);
    }
}
