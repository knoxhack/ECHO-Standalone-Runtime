package dev.echo.standalone.runtime.render;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoVoxelChunkMesher {
    private static final double DEFAULT_VISIBLE_DISTANCE = 96.0D;
    private final EchoVoxelRenderBackendTarget target;
    private final double visibleDistance;

    public EchoVoxelChunkMesher() {
        this(EchoVoxelRenderBackendTarget.OPENGL, DEFAULT_VISIBLE_DISTANCE);
    }

    public EchoVoxelChunkMesher(EchoVoxelRenderBackendTarget target, double visibleDistance) {
        this.target = Objects.requireNonNull(target, "target");
        if (visibleDistance <= 0.0D) {
            throw new IllegalArgumentException("visibleDistance must be positive");
        }
        this.visibleDistance = visibleDistance;
    }

    public EchoVoxelRenderPacket buildPacket(EchoVoxelWorld world, EchoVoxelCamera camera) {
        return buildPacket(world, camera, visibleDistance);
    }

    public EchoVoxelRenderPacket buildPacket(EchoVoxelWorld world, EchoVoxelCamera camera, double requestedVisibleDistance) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(camera, "camera");
        if (requestedVisibleDistance <= 0.0D) {
            throw new IllegalArgumentException("requestedVisibleDistance must be positive");
        }
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex(world);
        LinkedHashMap<EchoVoxelChunkId, MeshAccumulator> chunks = new LinkedHashMap<>();
        int sourceBlocks = 0;
        int chunkSize = world.chunkSize();
        for (EchoVoxelChunk chunk : world.chunks()) {
            EchoVoxelChunkId chunkId = chunk.id();
            boolean visible = chunkVisible(chunkId, chunkSize, camera, requestedVisibleDistance);
            MeshAccumulator accumulator = null;
            int baseX = chunkId.x() * chunkSize;
            int baseY = chunkId.y() * chunkSize;
            int baseZ = chunkId.z() * chunkSize;
            for (int y = 0; y < chunkSize; y++) {
                for (int z = 0; z < chunkSize; z++) {
                    for (int x = 0; x < chunkSize; x++) {
                        EchoVoxelBlockState state = chunk.stateAtLocal(x, y, z);
                        if (state.air()) {
                            continue;
                        }
                        sourceBlocks++;
                        if (!visible) {
                            continue;
                        }
                        if (accumulator == null) {
                            accumulator = chunks.computeIfAbsent(chunkId, ignored -> new MeshAccumulator());
                        }
                        accumulator.sourceBlockCount++;
                        addVisibleFaces(
                                world,
                                chunksById,
                                baseX + x,
                                baseY + y,
                                baseZ + z,
                                state,
                                accumulator.faces
                        );
                    }
                }
            }
        }
        ArrayList<EchoVoxelChunkMesh> meshes = new ArrayList<>();
        for (Map.Entry<EchoVoxelChunkId, MeshAccumulator> entry : chunks.entrySet()) {
            meshes.add(new EchoVoxelChunkMesh(
                    entry.getKey(),
                    target,
                    entry.getValue().faces,
                    entry.getValue().sourceBlockCount
            ));
        }
        meshes.sort(Comparator.comparingInt((EchoVoxelChunkMesh mesh) -> mesh.chunkId().x())
                .thenComparingInt(mesh -> mesh.chunkId().y())
                .thenComparingInt(mesh -> mesh.chunkId().z()));
        int culled = Math.max(0, world.loadedChunkCount() - meshes.size());
        return new EchoVoxelRenderPacket(target, meshes, sourceBlocks, culled);
    }

    public EchoVoxelChunkMesh buildChunkMesh(EchoVoxelWorld world, EchoVoxelChunkId chunkId) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(chunkId, "chunkId");
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex(world);
        EchoVoxelChunk chunk = chunksById.get(chunkId);
        if (chunk == null) {
            return new EchoVoxelChunkMesh(chunkId, target, List.of(), 0);
        }

        MeshAccumulator accumulator = new MeshAccumulator();
        int chunkSize = world.chunkSize();
        int baseX = chunkId.x() * chunkSize;
        int baseY = chunkId.y() * chunkSize;
        int baseZ = chunkId.z() * chunkSize;
        for (int y = 0; y < chunkSize; y++) {
            for (int z = 0; z < chunkSize; z++) {
                for (int x = 0; x < chunkSize; x++) {
                    EchoVoxelBlockState state = chunk.stateAtLocal(x, y, z);
                    if (state.air()) {
                        continue;
                    }
                    accumulator.sourceBlockCount++;
                    addVisibleFaces(
                            world,
                            chunksById,
                            baseX + x,
                            baseY + y,
                            baseZ + z,
                            state,
                            accumulator.faces
                    );
                }
            }
        }
        return new EchoVoxelChunkMesh(chunkId, target, accumulator.faces, accumulator.sourceBlockCount);
    }

    private void addVisibleFaces(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            int x,
            int y,
            int z,
            EchoVoxelBlockState state,
            List<EchoVoxelMeshFace> faces
    ) {
        EchoVoxelBiome biome = world.biomeAt(x, z);
        EchoVoxelMeshMaterial material = EchoVoxelMeshMaterial.fromBlockState(state, biome);
        for (EchoVoxelMeshDirection direction : EchoVoxelMeshDirection.values()) {
            EchoVoxelBlock neighbor = blockAt(
                    chunksById,
                    world.chunkSize(),
                    x + direction.normalX(),
                    y + direction.normalY(),
                    z + direction.normalZ()
            );
            if (neighbor.air() || !neighbor.opaque()) {
                faces.add(new EchoVoxelMeshFace(x, y, z, direction, material));
            }
        }
    }

    private static EchoVoxelBlock blockAt(
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            int chunkSize,
            int x,
            int y,
            int z
    ) {
        if (y < 0) {
            return EchoVoxelBlock.AIR;
        }
        EchoVoxelChunk chunk = chunksById.get(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
        if (chunk == null) {
            return EchoVoxelBlock.AIR;
        }
        return chunk.blockAtLocal(
                Math.floorMod(x, chunkSize),
                Math.floorMod(y, chunkSize),
                Math.floorMod(z, chunkSize)
        );
    }

    private static Map<EchoVoxelChunkId, EchoVoxelChunk> chunkIndex(EchoVoxelWorld world) {
        LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> result = new LinkedHashMap<>();
        for (EchoVoxelChunk chunk : world.chunks()) {
            result.putIfAbsent(chunk.id(), chunk);
        }
        return result;
    }

    private boolean chunkVisible(
            EchoVoxelChunkId chunkId,
            int chunkSize,
            EchoVoxelCamera camera,
            double requestedVisibleDistance
    ) {
        double centerX = chunkId.x() * chunkSize + chunkSize * 0.5D;
        double centerY = chunkId.y() * chunkSize + chunkSize * 0.5D;
        double centerZ = chunkId.z() * chunkSize + chunkSize * 0.5D;
        double dx = centerX - camera.x();
        double dy = centerY - camera.y();
        double dz = centerZ - camera.z();
        double radius = chunkSize * 0.866D;
        return dx * dx + dy * dy + dz * dz <= (requestedVisibleDistance + radius) * (requestedVisibleDistance + radius);
    }

    private static final class MeshAccumulator {
        private final ArrayList<EchoVoxelMeshFace> faces = new ArrayList<>();
        private int sourceBlockCount;
    }
}
