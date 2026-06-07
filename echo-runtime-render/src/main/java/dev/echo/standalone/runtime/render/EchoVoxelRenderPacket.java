package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoVoxelRenderPacket(
        EchoVoxelRenderBackendTarget target,
        List<EchoVoxelChunkMesh> chunkMeshes,
        int sourceBlockCount,
        int culledChunkCount
) {
    public EchoVoxelRenderPacket {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(chunkMeshes, "chunkMeshes");
        chunkMeshes = List.copyOf(chunkMeshes);
        if (sourceBlockCount < 0) {
            throw new IllegalArgumentException("sourceBlockCount must not be negative");
        }
        if (culledChunkCount < 0) {
            throw new IllegalArgumentException("culledChunkCount must not be negative");
        }
    }

    public int visibleChunkCount() {
        return chunkMeshes.size();
    }

    public int faceCount() {
        int count = 0;
        for (EchoVoxelChunkMesh mesh : chunkMeshes) {
            count += mesh.faceCount();
        }
        return count;
    }

    public int vertexCount() {
        return faceCount() * 4;
    }

    public int indexCount() {
        return faceCount() * 6;
    }

    public int materialCount() {
        HashSet<String> materials = new HashSet<>();
        for (EchoVoxelChunkMesh mesh : chunkMeshes) {
            for (EchoVoxelMeshFace face : mesh.faces()) {
                materials.add(face.material().materialId());
            }
        }
        return materials.size();
    }

    public Set<String> materialAtlasKeys() {
        TreeSet<String> atlasKeys = new TreeSet<>();
        for (EchoVoxelChunkMesh mesh : chunkMeshes) {
            for (EchoVoxelMeshFace face : mesh.faces()) {
                atlasKeys.add(face.material().atlasKey());
            }
        }
        return Set.copyOf(atlasKeys);
    }

    public Set<EchoVoxelMaterialPattern> materialPatterns() {
        HashSet<EchoVoxelMaterialPattern> patterns = new HashSet<>();
        for (EchoVoxelChunkMesh mesh : chunkMeshes) {
            for (EchoVoxelMeshFace face : mesh.faces()) {
                patterns.add(face.material().pattern());
            }
        }
        return Set.copyOf(patterns);
    }

    public int materialPatternCount() {
        return materialPatterns().size();
    }

    public int patternedFaceCount() {
        int count = 0;
        for (EchoVoxelChunkMesh mesh : chunkMeshes) {
            for (EchoVoxelMeshFace face : mesh.faces()) {
                if (face.material().pattern() != EchoVoxelMaterialPattern.FLAT
                        && face.material().argb() != face.material().detailArgb()) {
                    count++;
                }
            }
        }
        return count;
    }
}
