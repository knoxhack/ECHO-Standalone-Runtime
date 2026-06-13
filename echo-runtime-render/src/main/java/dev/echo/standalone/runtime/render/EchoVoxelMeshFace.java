package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoVoxelMeshFace(
        int x,
        int y,
        int z,
        EchoVoxelMeshDirection direction,
        EchoVoxelMeshMaterial material,
        double minX,
        double minY,
        double minZ,
        double maxX,
        double maxY,
        double maxZ
) {
    public EchoVoxelMeshFace(int x, int y, int z, EchoVoxelMeshDirection direction, EchoVoxelMeshMaterial material) {
        this(x, y, z, direction, material, 0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    }

    public EchoVoxelMeshFace {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(material, "material");
        validateUnitBounds(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public int vertexCount() {
        return 4;
    }

    public int indexCount() {
        return 6;
    }

    public boolean fullCubeBounds() {
        return minX == 0.0D
                && minY == 0.0D
                && minZ == 0.0D
                && maxX == 1.0D
                && maxY == 1.0D
                && maxZ == 1.0D;
    }

    private static void validateUnitBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        validateUnitBound("minX", minX);
        validateUnitBound("minY", minY);
        validateUnitBound("minZ", minZ);
        validateUnitBound("maxX", maxX);
        validateUnitBound("maxY", maxY);
        validateUnitBound("maxZ", maxZ);
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("mesh face min bounds must be <= max bounds");
        }
    }

    private static void validateUnitBound(String name, double value) {
        if (!Double.isFinite(value) || value < 0.0D || value > 1.0D) {
            throw new IllegalArgumentException(name + " must be finite and within [0, 1]");
        }
    }
}
