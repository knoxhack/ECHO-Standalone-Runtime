package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.assets.EchoMinecraftAssetResolver;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.render.EchoVoxelMeshMaterial;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesh;
import dev.echo.standalone.runtime.render.EchoVoxelChunkMesher;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSource;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

/**
 * GPU voxel renderer: atlas, chunk VBO meshes, shader with directional light + AO + depth.
 */
final class EchoClientRenderer {
    static final int MAX_CHUNK_UPLOADS_PER_FRAME = 8;

    private final EchoClientShader shader;
    private final EchoClientTextureAtlas atlas;
    private final EchoClientBlockOutlineRenderer outlineRenderer;
    private final EchoClientEntityRenderer entityRenderer;
    private final Map<EchoVoxelChunkId, EchoClientChunkMesh> chunkMeshes = new LinkedHashMap<>();
    private final Map<EchoVoxelChunkId, CachedCpuChunkMesh> cpuChunkMeshCache = new HashMap<>();
    private final EchoVoxelChunkMesher mesher;
    private final AtlasSourceCache atlasSourceCache = new AtlasSourceCache();

    // Uniform locations
    private final int uProjection;
    private final int uView;
    private final int uAtlas;
    private final int uLightDir;
    private final int uFogColor;
    private final int uFogDensity;

    private float[] projectionMatrix;
    private int windowWidth;
    private int windowHeight;
    private int chunkViewDistance = EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE;
    private int activeChunkSize = 16;
    private boolean fogEnabled = true;
    private EchoClientBiomeEnvironment environment = EchoClientBiomeEnvironment.DEFAULT;
    private float projectionFovDegrees = Float.NaN;
    private float projectionAspect = Float.NaN;
    private float projectionFarPlane = Float.NaN;
    private int projectionRebuildCount;
    private int lastFullChunkUpdateCount;
    private int lastDirtyChunkUpdateCount;
    private boolean atlasSourceReady;
    private int atlasSourceSignature;
    private int atlasRebuildCount;
    private int atlasReuseCount;
    private int lastChunkUploadBudget;
    private int lastChunkUploadCount;
    private int lastPendingChunkUploadCount;
    private int lastCpuChunkMeshCacheHitCount;
    private int lastCpuChunkMeshCacheBuildCount;
    private int lastCpuChunkMeshCacheEvictionCount;

    EchoClientRenderer() {
        shader = new EchoClientShader("/shaders/voxel.vert", "/shaders/voxel.frag");
        atlas = new EchoClientTextureAtlas();
        outlineRenderer = new EchoClientBlockOutlineRenderer();
        entityRenderer = new EchoClientEntityRenderer();
        mesher = new EchoVoxelChunkMesher();

        shader.use();
        uProjection = shader.uniform("uProjection");
        uView = shader.uniform("uView");
        uAtlas = shader.uniform("uAtlas");
        uLightDir = shader.uniform("uLightDir");
        uFogColor = shader.uniform("uFogColor");
        uFogDensity = shader.uniform("uFogDensity");
        GL20.glUseProgram(0);
    }

    void setMinecraftAssets(EchoMinecraftAssetResolver minecraftAssets) {
        atlas.setMinecraftAssets(minecraftAssets);
        atlasSourceReady = false;
    }

    void resize(int width, int height) {
        windowWidth = width;
        windowHeight = height;
        projectionMatrix = null;
        projectionFovDegrees = Float.NaN;
        projectionAspect = Float.NaN;
        projectionFarPlane = Float.NaN;
    }

    void rebuildAtlas(EchoVoxelWorld world) {
        rebuildAtlas(world, EchoClientEntityCatalog.empty());
    }

    void rebuildAtlas(EchoVoxelWorld world, EchoClientEntityCatalog entityCatalog) {
        rebuildAtlas(atlasSourceCache.source(world, entityCatalog));
    }

    boolean rebuildAtlasIfSourceChanged(EchoVoxelWorld world) {
        return rebuildAtlasIfSourceChanged(world, EchoClientEntityCatalog.empty());
    }

    boolean rebuildAtlasIfSourceChanged(EchoVoxelWorld world, EchoClientEntityCatalog entityCatalog) {
        AtlasSource source = atlasSourceCache.source(world, entityCatalog);
        if (atlasSourceReady && atlasSourceSignature == source.signature()) {
            atlasReuseCount++;
            return false;
        }
        rebuildAtlas(source);
        return true;
    }

    static int atlasSourceSignature(EchoVoxelWorld world) {
        return atlasSource(world).signature();
    }

    private void rebuildAtlas(AtlasSource source) {
        atlas.delete();
        atlas.build(
                source.colors(),
                source.atlasKeys(),
                source.blockIdsByAtlasKey(),
                source.blockModelRequests(),
                source.explicitTextureIdsByAtlasKey()
        );
        atlasSourceSignature = source.signature();
        atlasSourceReady = true;
        atlasRebuildCount++;

        // Atlas layout changed — force re-upload of all chunk meshes so UVs are correct
        for (EchoClientChunkMesh mesh : chunkMeshes.values()) {
            mesh.markDirty();
        }
    }

    private static AtlasSource atlasSource(EchoVoxelWorld world) {
        return new AtlasSourceCache().source(world);
    }

    void updateChunks(EchoVoxelWorld world, EchoVoxelCamera camera) {
        activeChunkSize = world.chunkSize();
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex(world);
        List<EchoVoxelChunkId> visibleChunkIds = visibleChunkIds(
                world,
                chunksById,
                camera,
                EchoClientSettings.visibleDistanceBlocks(chunkViewDistance, world.chunkSize())
        );
        lastCpuChunkMeshCacheHitCount = 0;
        lastCpuChunkMeshCacheBuildCount = 0;
        lastCpuChunkMeshCacheEvictionCount = evictCpuChunkMeshes(chunksById.keySet());
        lastFullChunkUpdateCount = 0;
        lastDirtyChunkUpdateCount = 0;

        java.util.Set<EchoVoxelChunkId> seen = new java.util.HashSet<>();
        for (EchoVoxelChunkId id : visibleChunkIds) {
            EchoVoxelChunkMesh mesh = cachedCpuChunkMesh(world, chunksById, id);
            if (mesh.sourceBlockCount() <= 0) {
                continue;
            }
            lastFullChunkUpdateCount++;
            seen.add(id);
            EchoClientChunkMesh gpuMesh = chunkMeshes.get(id);
            if (gpuMesh == null) {
                gpuMesh = new EchoClientChunkMesh(id);
                chunkMeshes.put(id, gpuMesh);
            }
            gpuMesh.setSource(mesh);
        }

        Iterator<Map.Entry<EchoVoxelChunkId, EchoClientChunkMesh>> it = chunkMeshes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<EchoVoxelChunkId, EchoClientChunkMesh> e = it.next();
            if (!seen.contains(e.getKey())) {
                e.getValue().delete();
                it.remove();
            }
        }
    }

    void updateDirtyChunks(EchoVoxelWorld world, Set<EchoVoxelChunkId> dirtyChunkIds) {
        Objects.requireNonNull(world, "world");
        if (dirtyChunkIds == null || dirtyChunkIds.isEmpty()) {
            lastFullChunkUpdateCount = 0;
            lastDirtyChunkUpdateCount = 0;
            lastCpuChunkMeshCacheHitCount = 0;
            lastCpuChunkMeshCacheBuildCount = 0;
            lastCpuChunkMeshCacheEvictionCount = 0;
            return;
        }
        activeChunkSize = world.chunkSize();
        lastFullChunkUpdateCount = 0;
        lastDirtyChunkUpdateCount = 0;
        lastCpuChunkMeshCacheHitCount = 0;
        lastCpuChunkMeshCacheBuildCount = 0;
        lastCpuChunkMeshCacheEvictionCount = 0;
        Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById = chunkIndex(world);
        for (EchoVoxelChunkId chunkId : dirtyChunkIds) {
            EchoClientChunkMesh gpuMesh = chunkMeshes.get(chunkId);
            if (gpuMesh == null) {
                continue;
            }
            gpuMesh.setSource(rebuildCpuChunkMesh(world, chunksById, chunkId));
            lastDirtyChunkUpdateCount++;
        }
    }

    void render(EchoVoxelCamera camera) {
        render(camera, null);
    }

    void render(EchoVoxelCamera camera, EchoVoxelHit target) {
        render(camera, target, 0.0D);
    }

    void render(EchoVoxelCamera camera, EchoVoxelHit target, double breakProgress) {
        render(camera, target, breakProgress, List.of(), EchoClientEntityCatalog.empty());
    }

    void render(
            EchoVoxelCamera camera,
            EchoVoxelHit target,
            double breakProgress,
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog
    ) {
        render(camera, target, breakProgress, entities, entityCatalog, List.of());
    }

    void render(
            EchoVoxelCamera camera,
            EchoVoxelHit target,
            double breakProgress,
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems
    ) {
        render(camera, target, breakProgress, entities, entityCatalog, droppedItems, List.of());
    }

    void render(
            EchoVoxelCamera camera,
            EchoVoxelHit target,
            double breakProgress,
            List<EchoEntityState> entities,
            EchoClientEntityCatalog entityCatalog,
            List<EchoClientDroppedItem> droppedItems,
            List<EchoClientParticle> particles
    ) {
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);

        GL11.glClearColor(environment.fogRed(), environment.fogGreen(), environment.fogBlue(), 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        projectionMatrix = projectionMatrix(camera);

        shader.use();
        shader.setMat4(uProjection, projectionMatrix);

        float[] view = viewMatrix(camera);
        shader.setMat4(uView, view);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.textureId());
        shader.setInt(uAtlas, 0);

        shader.setVec3(uLightDir, 0.3f, 0.8f, 0.2f);
        shader.setVec3(uFogColor, environment.fogRed(), environment.fogGreen(), environment.fogBlue());
        shader.setFloat(uFogDensity, fogEnabled ? environment.fogDensity() : 0.0f);

        ChunkUploadFrameBudget uploadBudget = new ChunkUploadFrameBudget(MAX_CHUNK_UPLOADS_PER_FRAME);
        for (EchoClientChunkMesh mesh : chunkMeshes.values()) {
            if (mesh.dirty() && uploadBudget.tryAcquireUploadSlot()) {
                mesh.uploadIfDirty(atlas);
            }
            mesh.draw();
        }
        lastChunkUploadBudget = uploadBudget.limit();
        lastChunkUploadCount = uploadBudget.uploadCount();
        lastPendingChunkUploadCount = uploadBudget.pendingCount();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        GL20.glUseProgram(0);

        entityRenderer.render(
                camera,
                entities,
                entityCatalog,
                droppedItems,
                particles,
                atlas,
                projectionMatrix,
                view,
                environment,
                fogEnabled
        );
        outlineRenderer.render(camera, target, projectionMatrix, view, breakProgress);
    }

    void clearShell() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glClearColor(0.025f, 0.04f, 0.055f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    void delete() {
        for (EchoClientChunkMesh mesh : chunkMeshes.values()) {
            mesh.delete();
        }
        chunkMeshes.clear();
        atlas.delete();
        outlineRenderer.delete();
        entityRenderer.delete();
        shader.delete();
    }

    EchoClientTextureAtlas atlas() {
        return atlas;
    }

    void setChunkViewDistance(int chunkViewDistance) {
        this.chunkViewDistance = EchoClientSettings.clampChunkViewDistance(chunkViewDistance);
    }

    void setBiomeEnvironment(EchoClientBiomeEnvironment environment) {
        this.environment = environment == null ? EchoClientBiomeEnvironment.DEFAULT : environment;
    }

    EchoClientBiomeEnvironment biomeEnvironment() {
        return environment;
    }

    int projectionRebuildCount() {
        return projectionRebuildCount;
    }

    int lastFullChunkUpdateCount() {
        return lastFullChunkUpdateCount;
    }

    int lastDirtyChunkUpdateCount() {
        return lastDirtyChunkUpdateCount;
    }

    int atlasRebuildCount() {
        return atlasRebuildCount;
    }

    int atlasReuseCount() {
        return atlasReuseCount;
    }

    int lastChunkUploadBudget() {
        return lastChunkUploadBudget;
    }

    int lastChunkUploadCount() {
        return lastChunkUploadCount;
    }

    int lastPendingChunkUploadCount() {
        return lastPendingChunkUploadCount;
    }

    int lastCpuChunkMeshCacheHitCount() {
        return lastCpuChunkMeshCacheHitCount;
    }

    int lastCpuChunkMeshCacheBuildCount() {
        return lastCpuChunkMeshCacheBuildCount;
    }

    int lastCpuChunkMeshCacheEvictionCount() {
        return lastCpuChunkMeshCacheEvictionCount;
    }

    private EchoVoxelChunkMesh cachedCpuChunkMesh(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId
    ) {
        CpuChunkMeshKey key = cpuChunkMeshKey(world, chunksById, chunkId);
        CachedCpuChunkMesh cached = cpuChunkMeshCache.get(chunkId);
        if (cached != null && cached.key().equals(key)) {
            lastCpuChunkMeshCacheHitCount++;
            return cached.mesh();
        }
        return rebuildCpuChunkMesh(world, chunksById, chunkId, key);
    }

    private EchoVoxelChunkMesh rebuildCpuChunkMesh(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId
    ) {
        return rebuildCpuChunkMesh(world, chunksById, chunkId, cpuChunkMeshKey(world, chunksById, chunkId));
    }

    private EchoVoxelChunkMesh rebuildCpuChunkMesh(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId,
            CpuChunkMeshKey key
    ) {
        EchoVoxelChunkMesh mesh = mesher.buildChunkMesh(world, chunkId);
        cpuChunkMeshCache.put(chunkId, new CachedCpuChunkMesh(key, mesh));
        lastCpuChunkMeshCacheBuildCount++;
        return mesh;
    }

    private int evictCpuChunkMeshes(Set<EchoVoxelChunkId> loadedChunkIds) {
        int before = cpuChunkMeshCache.size();
        cpuChunkMeshCache.keySet().removeIf(chunkId -> !loadedChunkIds.contains(chunkId));
        return before - cpuChunkMeshCache.size();
    }

    private List<EchoVoxelChunkId> visibleChunkIds(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelCamera camera,
            double visibleDistance
    ) {
        ArrayList<EchoVoxelChunkId> result = new ArrayList<>();
        int chunkSize = world.chunkSize();
        for (EchoVoxelChunk chunk : chunksById.values()) {
            EchoVoxelChunkId chunkId = chunk.id();
            if (chunkVisible(chunkId, chunkSize, camera, visibleDistance)) {
                result.add(chunkId);
            }
        }
        result.sort(Comparator.comparingInt(EchoVoxelChunkId::x)
                .thenComparingInt(EchoVoxelChunkId::y)
                .thenComparingInt(EchoVoxelChunkId::z));
        return result;
    }

    private static Map<EchoVoxelChunkId, EchoVoxelChunk> chunkIndex(EchoVoxelWorld world) {
        LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> result = new LinkedHashMap<>();
        for (EchoVoxelChunk chunk : world.chunks()) {
            result.putIfAbsent(chunk.id(), chunk);
        }
        return result;
    }

    private static boolean chunkVisible(
            EchoVoxelChunkId chunkId,
            int chunkSize,
            EchoVoxelCamera camera,
            double visibleDistance
    ) {
        double centerX = chunkId.x() * chunkSize + chunkSize * 0.5D;
        double centerY = chunkId.y() * chunkSize + chunkSize * 0.5D;
        double centerZ = chunkId.z() * chunkSize + chunkSize * 0.5D;
        double dx = centerX - camera.x();
        double dy = centerY - camera.y();
        double dz = centerZ - camera.z();
        double radius = chunkSize * 0.866D;
        return dx * dx + dy * dy + dz * dz <= (visibleDistance + radius) * (visibleDistance + radius);
    }

    private static CpuChunkMeshKey cpuChunkMeshKey(
            EchoVoxelWorld world,
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId
    ) {
        EchoVoxelChunk chunk = chunksById.get(chunkId);
        return new CpuChunkMeshKey(
                chunk,
                chunk == null ? Long.MIN_VALUE : chunk.version(),
                world.chunkSize(),
                world.seed(),
                world.biomeSource(),
                neighbor(chunksById, chunkId, -1, 0, 0),
                neighborVersion(chunksById, chunkId, -1, 0, 0),
                neighbor(chunksById, chunkId, 1, 0, 0),
                neighborVersion(chunksById, chunkId, 1, 0, 0),
                neighbor(chunksById, chunkId, 0, -1, 0),
                neighborVersion(chunksById, chunkId, 0, -1, 0),
                neighbor(chunksById, chunkId, 0, 1, 0),
                neighborVersion(chunksById, chunkId, 0, 1, 0),
                neighbor(chunksById, chunkId, 0, 0, -1),
                neighborVersion(chunksById, chunkId, 0, 0, -1),
                neighbor(chunksById, chunkId, 0, 0, 1),
                neighborVersion(chunksById, chunkId, 0, 0, 1)
        );
    }

    private static EchoVoxelChunk neighbor(
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId,
            int dx,
            int dy,
            int dz
    ) {
        return chunksById.get(new EchoVoxelChunkId(chunkId.x() + dx, chunkId.y() + dy, chunkId.z() + dz));
    }

    private static long neighborVersion(
            Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById,
            EchoVoxelChunkId chunkId,
            int dx,
            int dy,
            int dz
    ) {
        EchoVoxelChunk neighbor = neighbor(chunksById, chunkId, dx, dy, dz);
        return neighbor == null ? Long.MIN_VALUE : neighbor.version();
    }

    private float[] projectionMatrix(EchoVoxelCamera camera) {
        float fovDegrees = (float) camera.fovDegrees();
        float aspect = windowWidth / (float) Math.max(1, windowHeight);
        float farPlane = (float) Math.max(
                256.0D,
                EchoClientSettings.visibleDistanceBlocks(chunkViewDistance, activeChunkSize) + 96.0D
        );
        if (projectionMatrix == null
                || Float.compare(projectionFovDegrees, fovDegrees) != 0
                || Float.compare(projectionAspect, aspect) != 0
                || Float.compare(projectionFarPlane, farPlane) != 0) {
            projectionMatrix = EchoClientMath.perspective(fovDegrees, aspect, 0.1f, farPlane);
            projectionFovDegrees = fovDegrees;
            projectionAspect = aspect;
            projectionFarPlane = farPlane;
            projectionRebuildCount++;
        }
        return projectionMatrix;
    }

    static float[] viewMatrix(EchoVoxelCamera camera) {
        return EchoClientMath.lookAt(
                (float) camera.x(),
                (float) camera.y(),
                (float) camera.z(),
                (float) (camera.x()
                        + Math.sin(Math.toRadians(camera.yawDegrees()))
                        * Math.cos(Math.toRadians(camera.pitchDegrees()))),
                (float) (camera.y() + Math.sin(Math.toRadians(camera.pitchDegrees()))),
                (float) (camera.z()
                        + Math.cos(Math.toRadians(camera.yawDegrees()))
                        * Math.cos(Math.toRadians(camera.pitchDegrees()))),
                0.0f,
                1.0f,
                0.0f
        );
    }

    private record CachedCpuChunkMesh(CpuChunkMeshKey key, EchoVoxelChunkMesh mesh) {
    }

    private record CpuChunkMeshKey(
            EchoVoxelChunk chunk,
            long version,
            int chunkSize,
            long seed,
            EchoVoxelBiomeSource biomeSource,
            EchoVoxelChunk negativeXNeighbor,
            long negativeXNeighborVersion,
            EchoVoxelChunk positiveXNeighbor,
            long positiveXNeighborVersion,
            EchoVoxelChunk negativeYNeighbor,
            long negativeYNeighborVersion,
            EchoVoxelChunk positiveYNeighbor,
            long positiveYNeighborVersion,
            EchoVoxelChunk negativeZNeighbor,
            long negativeZNeighborVersion,
            EchoVoxelChunk positiveZNeighbor,
            long positiveZNeighborVersion
    ) {
    }

    private record AtlasSource(
            int signature,
            Map<String, Integer> colors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey,
            Map<String, String> explicitTextureIdsByAtlasKey,
            List<EchoClientTextureAtlas.BlockModelRequest> blockModelRequests
    ) {
    }

    static final class AtlasSourceCache {
        private final Map<EchoVoxelChunkId, CachedChunkAtlasSource> chunks = new HashMap<>();
        private int lastChunkScanCount;
        private int lastCellScanCount;

        int sourceSignature(EchoVoxelWorld world) {
            return source(world, EchoClientEntityCatalog.empty()).signature();
        }

        int sourceSignature(EchoVoxelWorld world, EchoClientEntityCatalog entityCatalog) {
            return source(world, entityCatalog).signature();
        }

        int lastChunkScanCount() {
            return lastChunkScanCount;
        }

        int lastCellScanCount() {
            return lastCellScanCount;
        }

        private AtlasSource source(EchoVoxelWorld world) {
            return source(world, EchoClientEntityCatalog.empty());
        }

        private AtlasSource source(EchoVoxelWorld world, EchoClientEntityCatalog entityCatalog) {
            Objects.requireNonNull(world, "world");
            lastChunkScanCount = 0;
            lastCellScanCount = 0;
            AtlasSourceBuilder builder = new AtlasSourceBuilder();
            java.util.HashSet<EchoVoxelChunkId> seen = new java.util.HashSet<>();
            for (EchoVoxelChunk chunk : world.chunks()) {
                seen.add(chunk.id());
                builder.add(chunkSource(world, chunk));
            }
            chunks.keySet().removeIf(chunkId -> !seen.contains(chunkId));
            builder.addEntityVisuals(entityCatalog);
            return builder.build();
        }

        private ChunkAtlasSource chunkSource(EchoVoxelWorld world, EchoVoxelChunk chunk) {
            CachedChunkAtlasSource cached = chunks.get(chunk.id());
            if (cached != null && cached.matches(world, chunk)) {
                return cached.source();
            }
            lastChunkScanCount++;
            ChunkAtlasSource source = buildChunkSource(world, chunk);
            chunks.put(chunk.id(), new CachedChunkAtlasSource(
                    chunk,
                    chunk.version(),
                    world.seed(),
                    world.biomeSource(),
                    source
            ));
            return source;
        }

        private ChunkAtlasSource buildChunkSource(EchoVoxelWorld world, EchoVoxelChunk chunk) {
            AtlasSourceBuilder builder = new AtlasSourceBuilder();
            int chunkSize = chunk.size();
            int baseX = chunk.id().x() * chunkSize;
            int baseY = chunk.id().y() * chunkSize;
            int baseZ = chunk.id().z() * chunkSize;
            int sourceBlockCount = 0;
            for (int y = 0; y < chunkSize; y++) {
                for (int z = 0; z < chunkSize; z++) {
                    for (int x = 0; x < chunkSize; x++) {
                        lastCellScanCount++;
                        EchoVoxelBlockState state = chunk.stateAtLocal(x, y, z);
                        if (state.air()) {
                            continue;
                        }
                        sourceBlockCount++;
                        builder.addState(baseX + x, baseY + y, baseZ + z, state, world);
                    }
                }
            }
            return builder.buildChunk(sourceBlockCount);
        }
    }

    private static final class AtlasSourceBuilder {
        private final TreeMap<String, Integer> colors = new TreeMap<>();
        private final TreeMap<String, String> atlasKeys = new TreeMap<>();
        private final TreeMap<String, String> blockIdsByAtlasKey = new TreeMap<>();
        private final TreeMap<String, EchoClientTextureAtlas.BlockModelRequest> blockModelRequestsByKey =
                new TreeMap<>();
        private final TreeMap<String, String> explicitTextureIdsByAtlasKey = new TreeMap<>();

        private void addState(
                int x,
                int y,
                int z,
                EchoVoxelBlockState state,
                EchoVoxelWorld world
        ) {
            EchoVoxelMeshMaterial material = EchoVoxelMeshMaterial.fromBlockState(
                    state,
                    world.biomeAt(x, z)
            );
            addMaterial(state.block().id(), material);
        }

        private void add(ChunkAtlasSource source) {
            if (source == null) {
                return;
            }
            source.colors().forEach(colors::putIfAbsent);
            source.atlasKeys().forEach(atlasKeys::putIfAbsent);
            source.blockIdsByAtlasKey().forEach(blockIdsByAtlasKey::putIfAbsent);
            source.blockModelRequestsByKey().forEach(blockModelRequestsByKey::putIfAbsent);
        }

        private void addMaterial(String blockId, EchoVoxelMeshMaterial material) {
            String materialId = material.materialId();
            String atlasKey = material.atlasKey();
            colors.putIfAbsent(atlasKey, material.argb());
            atlasKeys.putIfAbsent(materialId, atlasKey);
            blockIdsByAtlasKey.putIfAbsent(atlasKey, blockId);
            String requestKey = blockId + "\n" + material.stateProperties() + "\n" + atlasKey;
            blockModelRequestsByKey.putIfAbsent(requestKey, new EchoClientTextureAtlas.BlockModelRequest(
                    blockId,
                    material.stateProperties(),
                    atlasKey
            ));
        }

        private void addEntityVisuals(EchoClientEntityCatalog entityCatalog) {
            EchoClientEntityCatalog catalog = entityCatalog == null
                    ? EchoClientEntityCatalog.empty()
                    : entityCatalog;
            addEntityTile(
                    EchoClientTextureAtlas.ENTITY_FALLBACK_ATLAS_KEY,
                    0xFFFFFFFF,
                    ""
            );
            for (EchoClientEntityCatalog.EntityVisualProfile profile : catalog.graphBackedVisualProfiles()) {
                EchoClientEntityCatalog.RenderProfile renderProfile = profile.renderProfile();
                String textureId = renderProfile.textureId();
                if (textureId == null || textureId.isBlank()) {
                    continue;
                }
                addEntityTile(
                        EchoClientTextureAtlas.textureAtlasKey(textureId),
                        renderProfile.argb(),
                        textureId
                );
            }
        }

        private void addEntityTile(String atlasKey, int argb, String textureId) {
            if (atlasKey == null || atlasKey.isBlank()) {
                return;
            }
            String materialId = "entity:" + atlasKey;
            colors.putIfAbsent(atlasKey, argb);
            atlasKeys.putIfAbsent(materialId, atlasKey);
            if (textureId != null && !textureId.isBlank()) {
                explicitTextureIdsByAtlasKey.putIfAbsent(atlasKey, textureId.trim());
            }
        }

        private AtlasSource build() {
            List<EchoClientTextureAtlas.BlockModelRequest> blockModelRequests =
                    List.copyOf(blockModelRequestsByKey.values());
            int signature = 17;
            signature = 31 * signature + colors.hashCode();
            signature = 31 * signature + atlasKeys.hashCode();
            signature = 31 * signature + blockIdsByAtlasKey.hashCode();
            signature = 31 * signature + explicitTextureIdsByAtlasKey.hashCode();
            signature = 31 * signature + blockModelRequests.hashCode();
            return new AtlasSource(
                    signature,
                    new LinkedHashMap<>(colors),
                    new LinkedHashMap<>(atlasKeys),
                    new LinkedHashMap<>(blockIdsByAtlasKey),
                    new LinkedHashMap<>(explicitTextureIdsByAtlasKey),
                    blockModelRequests
            );
        }

        private ChunkAtlasSource buildChunk(int sourceBlockCount) {
            return new ChunkAtlasSource(
                    new LinkedHashMap<>(colors),
                    new LinkedHashMap<>(atlasKeys),
                    new LinkedHashMap<>(blockIdsByAtlasKey),
                    new LinkedHashMap<>(blockModelRequestsByKey),
                    sourceBlockCount
            );
        }
    }

    private record CachedChunkAtlasSource(
            EchoVoxelChunk chunk,
            long version,
            long seed,
            EchoVoxelBiomeSource biomeSource,
            ChunkAtlasSource source
    ) {
        private boolean matches(EchoVoxelWorld world, EchoVoxelChunk candidate) {
            return chunk == candidate
                    && version == candidate.version()
                    && seed == world.seed()
                    && Objects.equals(biomeSource, world.biomeSource());
        }
    }

    private record ChunkAtlasSource(
            Map<String, Integer> colors,
            Map<String, String> atlasKeys,
            Map<String, String> blockIdsByAtlasKey,
            Map<String, EchoClientTextureAtlas.BlockModelRequest> blockModelRequestsByKey,
            int sourceBlockCount
    ) {
    }

    static final class ChunkUploadFrameBudget {
        private final int limit;
        private int uploadCount;
        private int pendingCount;

        ChunkUploadFrameBudget(int limit) {
            this.limit = Math.max(0, limit);
        }

        boolean tryAcquireUploadSlot() {
            if (uploadCount >= limit) {
                pendingCount++;
                return false;
            }
            uploadCount++;
            return true;
        }

        int limit() {
            return limit;
        }

        int uploadCount() {
            return uploadCount;
        }

        int pendingCount() {
            return pendingCount;
        }
    }
}
