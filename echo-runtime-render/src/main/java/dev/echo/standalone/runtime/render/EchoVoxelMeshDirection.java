package dev.echo.standalone.runtime.render;

public enum EchoVoxelMeshDirection {
    UP(0, 1, 0, 1.00D),
    EAST(1, 0, 0, 1.00D),
    WEST(-1, 0, 0, 1.00D),
    SOUTH(0, 0, 1, 1.00D),
    NORTH(0, 0, -1, 1.00D),
    DOWN(0, -1, 0, 1.00D);

    private final int normalX;
    private final int normalY;
    private final int normalZ;
    private final double shade;

    EchoVoxelMeshDirection(int normalX, int normalY, int normalZ, double shade) {
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.shade = shade;
    }

    public int normalX() {
        return normalX;
    }

    public int normalY() {
        return normalY;
    }

    public int normalZ() {
        return normalZ;
    }

    public double shade() {
        return shade;
    }
}
