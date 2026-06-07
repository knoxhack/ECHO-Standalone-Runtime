package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoVoxelChunkMesh(
        EchoVoxelChunkId chunkId,
        EchoVoxelRenderBackendTarget target,
        List<EchoVoxelMeshFace> faces,
        int sourceBlockCount
) {
    public EchoVoxelChunkMesh {
        Objects.requireNonNull(chunkId, "chunkId");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(faces, "faces");
        faces = List.copyOf(faces);
        if (sourceBlockCount < 0) {
            throw new IllegalArgumentException("sourceBlockCount must not be negative");
        }
    }

    public int faceCount() {
        return faces.size();
    }

    public int vertexCount() {
        return faceCount() * 4;
    }

    public int indexCount() {
        return faceCount() * 6;
    }

    public int materialCount() {
        HashSet<String> materials = new HashSet<>();
        for (EchoVoxelMeshFace face : faces) {
            materials.add(face.material().materialId());
        }
        return materials.size();
    }

    public Set<String> materialAtlasKeys() {
        TreeSet<String> atlasKeys = new TreeSet<>();
        for (EchoVoxelMeshFace face : faces) {
            atlasKeys.add(face.material().atlasKey());
        }
        return Set.copyOf(atlasKeys);
    }

    public Set<EchoVoxelMaterialPattern> materialPatterns() {
        HashSet<EchoVoxelMaterialPattern> patterns = new HashSet<>();
        for (EchoVoxelMeshFace face : faces) {
            patterns.add(face.material().pattern());
        }
        return Set.copyOf(patterns);
    }
}
