package dev.echo.standalone.runtime.world;

public record EchoVoxelCollisionBox(
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {
    public static final EchoVoxelCollisionBox EMPTY = new EchoVoxelCollisionBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
    public static final EchoVoxelCollisionBox FULL_BLOCK = new EchoVoxelCollisionBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    public EchoVoxelCollisionBox {
        validate(minX, "minX");
        validate(minY, "minY");
        validate(minZ, "minZ");
        validate(maxX, "maxX");
        validate(maxY, "maxY");
        validate(maxZ, "maxZ");
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("collision box minimums must not exceed maximums");
        }
    }

    /**
     * Returns a collision box for the given block. Blocks that are solid or block motion use the
     * full-block box; air and non-solid/non-motion-blocking blocks use the empty box.
     */
    public static EchoVoxelCollisionBox forBlock(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return EMPTY;
        }
        return block.solid() || block.blocksMotion() ? FULL_BLOCK : EMPTY;
    }

    public boolean empty() {
        return minX == maxX || minY == maxY || minZ == maxZ;
    }

    public boolean intersectsWorldBox(
            int blockX,
            int blockY,
            int blockZ,
            double otherMinX,
            double otherMinY,
            double otherMinZ,
            double otherMaxX,
            double otherMaxY,
            double otherMaxZ
    ) {
        if (empty()) {
            return false;
        }
        double worldMinX = blockX + minX;
        double worldMinY = blockY + minY;
        double worldMinZ = blockZ + minZ;
        double worldMaxX = blockX + maxX;
        double worldMaxY = blockY + maxY;
        double worldMaxZ = blockZ + maxZ;
        return otherMaxX > worldMinX
                && otherMinX < worldMaxX
                && otherMaxY > worldMinY
                && otherMinY < worldMaxY
                && otherMaxZ > worldMinZ
                && otherMinZ < worldMaxZ;
    }

    private static void validate(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " must be finite and within 0..1");
        }
    }
}
