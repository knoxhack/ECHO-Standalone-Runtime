package dev.echo.standalone.runtime.world;

public record EchoVoxelHit(
        int x,
        int y,
        int z,
        int normalX,
        int normalY,
        int normalZ,
        EchoVoxelBlock block,
        double distance
) {
}
