package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoVoxelMeshFace(
        int x,
        int y,
        int z,
        EchoVoxelMeshDirection direction,
        EchoVoxelMeshMaterial material
) {
    public EchoVoxelMeshFace {
        Objects.requireNonNull(direction, "direction");
        Objects.requireNonNull(material, "material");
    }

    public int vertexCount() {
        return 4;
    }

    public int indexCount() {
        return 6;
    }
}
