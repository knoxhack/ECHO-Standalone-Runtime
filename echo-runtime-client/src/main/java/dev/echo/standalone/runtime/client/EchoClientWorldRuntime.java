package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class EchoClientWorldRuntime {
    static final int CLEAN_CACHE_EXTRA_CHUNKS = 96;

    private EchoVoxelWorldStreamer streamer;
    private EchoClientHazardCatalog hazardCatalog;
    private EchoVoxelWorld world;
    private final LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> chunkCache = new LinkedHashMap<>();
    private final LinkedHashMap<EchoVoxelChunkId, Long> generatedFingerprints = new LinkedHashMap<>();
    private final LinkedHashSet<EchoVoxelChunkId> protectedChunkIds = new LinkedHashSet<>();
    private final IdentityHashMap<EchoVoxelChunk, CachedChunkInspection> chunkInspectionCache =
            new IdentityHashMap<>();
    private int lastPlayerChunkX = Integer.MIN_VALUE;
    private int lastPlayerChunkZ = Integer.MIN_VALUE;
    private int lastStreamRadius = Integer.MIN_VALUE;
    private int lastChunkInspectionScanCount;
    private int lastChunkInspectionCellScanCount;

    EchoClientWorldRuntime(
            EchoVoxelWorld world,
            EchoVoxelWorldStreamer streamer,
            EchoClientHazardCatalog hazardCatalog
    ) {
        this.world = world;
        this.streamer = streamer;
        this.hazardCatalog = hazardCatalog == null ? EchoClientHazardCatalog.empty() : hazardCatalog;
        cacheLoadedChunks(world);
    }

    EchoVoxelWorld world() {
        return world;
    }

    EchoClientHazardCatalog hazardCatalog() {
        return hazardCatalog;
    }

    void updateHazardCatalog(EchoClientHazardCatalog hazardCatalog) {
        this.hazardCatalog = hazardCatalog == null ? EchoClientHazardCatalog.empty() : hazardCatalog;
    }

    void updateStreamer(EchoVoxelWorldStreamer streamer) {
        if (streamer != null) {
            cacheLoadedChunks(world);
            Map<EchoVoxelChunkId, EchoVoxelChunk> previousCache = new LinkedHashMap<>(chunkCache);
            Set<EchoVoxelChunkId> previouslyProtected = new LinkedHashSet<>(protectedChunkIds);
            this.streamer = streamer;
            world = streamer.applyBiomeSource(world);
            rebuildCacheForStreamerUpdate(previousCache, previouslyProtected);
        }
    }

    void setWorld(EchoVoxelWorld world) {
        if (this.world == world) {
            return;
        }
        this.world = world;
        cacheLoadedChunks(world);
    }

    int cachedChunkCount() {
        cacheLoadedChunks(world);
        return chunkCache.size();
    }

    int cachedProtectedChunkCount() {
        cacheLoadedChunks(world);
        int count = 0;
        for (EchoVoxelChunkId chunkId : chunkCache.keySet()) {
            if (protectedChunkIds.contains(chunkId)) {
                count++;
            }
        }
        return count;
    }

    int lastChunkInspectionScanCount() {
        return lastChunkInspectionScanCount;
    }

    int lastChunkInspectionCellScanCount() {
        return lastChunkInspectionCellScanCount;
    }

    EchoVoxelWorld persistedWorld() {
        cacheLoadedChunks(world);
        return world.withLoadedChunks(sortedCachedChunks());
    }

    boolean setBlockStateAt(int x, int y, int z, EchoVoxelBlockState state) {
        EchoVoxelChunkId chunkId = EchoVoxelChunkId.fromBlock(x, y, z, world.chunkSize());
        cacheLoadedChunks(world);
        EchoVoxelChunk chunk = chunkCache.get(chunkId);
        if (chunk == null) {
            return false;
        }
        chunk.setStateLocal(
                Math.floorMod(x, world.chunkSize()),
                Math.floorMod(y, world.chunkSize()),
                Math.floorMod(z, world.chunkSize()),
                state
        );
        chunkInspectionCache.remove(chunk);
        protectedChunkIds.add(chunkId);
        return true;
    }

    EchoClientBiomeHazardResult tickBiomeHazards(
            EchoClientHazardState hazardState,
            EchoVoxelPlayerState player,
            double deltaSeconds
    ) {
        EchoClientHazardState safeState = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        var tick = safeState.tick(world.biomeAt(player.x(), player.z()), deltaSeconds, hazardCatalog);
        return new EchoClientBiomeHazardResult(tick.state(), tick.damage(), tick.source());
    }

    EchoClientWorldStreamResult streamAroundPlayer(EchoVoxelPlayerState player, int chunkViewDistance) {
        int streamRadius = EchoClientSettings.clampChunkViewDistance(chunkViewDistance);
        int chunkX = Math.floorDiv((int) Math.floor(player.x()), world.chunkSize());
        int chunkZ = Math.floorDiv((int) Math.floor(player.z()), world.chunkSize());
        boolean playerChunkChanged = chunkX != lastPlayerChunkX || chunkZ != lastPlayerChunkZ;
        boolean viewDistanceChanged = streamRadius != lastStreamRadius;
        if (!playerChunkChanged && !viewDistanceChanged) {
            return EchoClientWorldStreamResult.NONE;
        }
        lastPlayerChunkX = chunkX;
        lastPlayerChunkZ = chunkZ;
        lastStreamRadius = streamRadius;
        cacheLoadedChunks(world);
        int before = world.loadedChunkCount();
        Set<EchoVoxelChunkId> beforeIds = loadedChunkIds(world);
        Set<EchoVoxelChunkId> cachedIdsBefore = new LinkedHashSet<>(chunkCache.keySet());
        ArrayList<EchoVoxelChunk> loadedChunks = new ArrayList<>();
        Set<EchoVoxelChunkId> requiredChunkIds = new LinkedHashSet<>(
                streamer.requiredChunkIds(world, player.x(), player.z(), streamRadius)
        );
        for (EchoVoxelChunkId chunkId : requiredChunkIds) {
            EchoVoxelChunk chunk = chunkCache.get(chunkId);
            if (chunk == null) {
                chunk = streamer.generateChunk(world, chunkId);
                cacheGeneratedChunk(chunk);
            }
            loadedChunks.add(chunk);
        }
        world = world.withLoadedChunks(loadedChunks);
        Set<EchoVoxelChunkId> afterIds = loadedChunkIds(world);
        boolean activeChunksChanged = world.loadedChunkCount() != before || !afterIds.equals(beforeIds);
        evictCleanCachedChunks(afterIds);
        boolean cachedChunksChanged = !chunkCache.keySet().equals(cachedIdsBefore);
        return new EchoClientWorldStreamResult(
                playerChunkChanged,
                activeChunksChanged,
                cachedChunksChanged,
                viewDistanceChanged
        );
    }

    private void cacheLoadedChunks(EchoVoxelWorld source) {
        resetChunkInspectionCounters();
        if (source == null) {
            return;
        }
        for (EchoVoxelChunk chunk : source.chunks()) {
            chunkCache.put(chunk.id(), chunk);
            observeChunkAgainstGeneratedSource(source, chunk);
        }
    }

    private void rebuildCacheForStreamerUpdate(
            Map<EchoVoxelChunkId, EchoVoxelChunk> previousCache,
            Set<EchoVoxelChunkId> previouslyProtected
    ) {
        LinkedHashMap<EchoVoxelChunkId, EchoVoxelChunk> nextCache = new LinkedHashMap<>();
        LinkedHashSet<EchoVoxelChunkId> nextProtected = new LinkedHashSet<>();
        ArrayList<EchoVoxelChunk> nextLoadedChunks = new ArrayList<>();
        for (EchoVoxelChunk chunk : world.chunks()) {
            EchoVoxelChunkId chunkId = chunk.id();
            boolean protectedChunk = protectedAfterStreamerUpdate(chunkId, chunk, previouslyProtected);
            EchoVoxelChunk nextChunk = protectedChunk ? chunk : regenerateChunkForStreamerUpdate(chunk);
            nextLoadedChunks.add(nextChunk);
            nextCache.put(chunkId, nextChunk);
            if (protectedChunk || containsProtectedBlockState(nextChunk)) {
                nextProtected.add(chunkId);
            }
        }
        for (Map.Entry<EchoVoxelChunkId, EchoVoxelChunk> entry : previousCache.entrySet()) {
            EchoVoxelChunkId chunkId = entry.getKey();
            EchoVoxelChunk chunk = entry.getValue();
            if (chunkId == null || chunk == null || nextCache.containsKey(chunkId)) {
                continue;
            }
            if (!protectedAfterStreamerUpdate(chunkId, chunk, previouslyProtected)) {
                continue;
            }
            nextCache.put(chunkId, chunk);
            nextProtected.add(chunkId);
        }

        world = world.withLoadedChunks(nextLoadedChunks);
        chunkCache.clear();
        generatedFingerprints.clear();
        protectedChunkIds.clear();
        chunkInspectionCache.clear();
        for (Map.Entry<EchoVoxelChunkId, EchoVoxelChunk> entry : nextCache.entrySet()) {
            if (nextProtected.contains(entry.getKey())) {
                chunkCache.put(entry.getKey(), entry.getValue());
                protectedChunkIds.add(entry.getKey());
            } else {
                cacheGeneratedChunk(entry.getValue());
            }
        }
        resetStreamCursor();
    }

    private boolean protectedAfterStreamerUpdate(
            EchoVoxelChunkId chunkId,
            EchoVoxelChunk chunk,
            Set<EchoVoxelChunkId> previouslyProtected
    ) {
        return chunkId != null
                && (previouslyProtected.contains(chunkId) || containsProtectedBlockState(chunk));
    }

    private EchoVoxelChunk regenerateChunkForStreamerUpdate(EchoVoxelChunk fallbackChunk) {
        if (streamer == null || fallbackChunk == null) {
            return fallbackChunk;
        }
        try {
            return streamer.generateChunk(world, fallbackChunk.id());
        } catch (RuntimeException ignored) {
            return fallbackChunk;
        }
    }

    private void resetStreamCursor() {
        lastPlayerChunkX = Integer.MIN_VALUE;
        lastPlayerChunkZ = Integer.MIN_VALUE;
        lastStreamRadius = Integer.MIN_VALUE;
    }

    private void cacheGeneratedChunk(EchoVoxelChunk chunk) {
        if (chunk == null) {
            return;
        }
        ChunkInspection inspection = chunkInspection(chunk);
        chunkCache.put(chunk.id(), chunk);
        generatedFingerprints.put(chunk.id(), inspection.fingerprint());
        if (inspection.containsProtectedBlockState()) {
            protectedChunkIds.add(chunk.id());
        }
    }

    private void observeChunkAgainstGeneratedSource(EchoVoxelWorld source, EchoVoxelChunk chunk) {
        if (source == null || chunk == null) {
            return;
        }
        EchoVoxelChunkId chunkId = chunk.id();
        ChunkInspection inspection = chunkInspection(chunk);
        if (inspection.containsProtectedBlockState()) {
            protectedChunkIds.add(chunkId);
            return;
        }
        long currentFingerprint = inspection.fingerprint();
        long generatedFingerprint = generatedFingerprints.computeIfAbsent(
                chunkId,
                ignored -> generatedChunkFingerprint(source, chunkId, currentFingerprint)
        );
        if (currentFingerprint != generatedFingerprint) {
            protectedChunkIds.add(chunkId);
        }
    }

    private long generatedChunkFingerprint(
            EchoVoxelWorld source,
            EchoVoxelChunkId chunkId,
            long fallbackFingerprint
    ) {
        if (streamer == null || source == null || chunkId == null) {
            return fallbackFingerprint;
        }
        try {
            return scanChunk(streamer.generateChunk(source, chunkId)).fingerprint();
        } catch (RuntimeException ignored) {
            return fallbackFingerprint;
        }
    }

    private int evictCleanCachedChunks(Set<EchoVoxelChunkId> activeChunkIds) {
        Set<EchoVoxelChunkId> activeIds = activeChunkIds == null ? Set.of() : activeChunkIds;
        int cleanNonActiveChunks = 0;
        for (Map.Entry<EchoVoxelChunkId, EchoVoxelChunk> entry : chunkCache.entrySet()) {
            if (evictableCleanChunk(entry.getKey(), entry.getValue(), activeIds)) {
                cleanNonActiveChunks++;
            }
        }
        int overflow = cleanNonActiveChunks - CLEAN_CACHE_EXTRA_CHUNKS;
        if (overflow <= 0) {
            return 0;
        }

        int evicted = 0;
        var iterator = chunkCache.entrySet().iterator();
        while (iterator.hasNext() && overflow > 0) {
            Map.Entry<EchoVoxelChunkId, EchoVoxelChunk> entry = iterator.next();
            EchoVoxelChunkId chunkId = entry.getKey();
            if (!evictableCleanChunk(chunkId, entry.getValue(), activeIds)) {
                continue;
            }
            chunkInspectionCache.remove(entry.getValue());
            iterator.remove();
            generatedFingerprints.remove(chunkId);
            overflow--;
            evicted++;
        }
        return evicted;
    }

    private boolean evictableCleanChunk(
            EchoVoxelChunkId chunkId,
            EchoVoxelChunk chunk,
            Set<EchoVoxelChunkId> activeChunkIds
    ) {
        if (chunkId == null || chunk == null || activeChunkIds.contains(chunkId) || protectedChunkIds.contains(chunkId)) {
            return false;
        }
        Long generatedFingerprint = generatedFingerprints.get(chunkId);
        if (generatedFingerprint == null) {
            return false;
        }
        ChunkInspection inspection = chunkInspection(chunk);
        if (inspection.containsProtectedBlockState()) {
            protectedChunkIds.add(chunkId);
            return false;
        }
        long currentFingerprint = inspection.fingerprint();
        if (currentFingerprint != generatedFingerprint) {
            protectedChunkIds.add(chunkId);
            return false;
        }
        return true;
    }

    private static Set<EchoVoxelChunkId> loadedChunkIds(EchoVoxelWorld source) {
        return source.chunks().stream()
                .map(EchoVoxelChunk::id)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private boolean containsProtectedBlockState(EchoVoxelChunk chunk) {
        return chunkInspection(chunk).containsProtectedBlockState();
    }

    private long chunkFingerprint(EchoVoxelChunk chunk) {
        return chunkInspection(chunk).fingerprint();
    }

    private ChunkInspection chunkInspection(EchoVoxelChunk chunk) {
        if (chunk == null) {
            return ChunkInspection.EMPTY;
        }
        CachedChunkInspection cached = chunkInspectionCache.get(chunk);
        if (cached != null && cached.matches(chunk)) {
            return cached.inspection();
        }
        ChunkInspection inspection = scanChunk(chunk);
        chunkInspectionCache.put(chunk, new CachedChunkInspection(chunk, chunk.version(), inspection));
        return inspection;
    }

    private ChunkInspection scanChunk(EchoVoxelChunk chunk) {
        if (chunk == null) {
            return ChunkInspection.EMPTY;
        }
        lastChunkInspectionScanCount++;
        long hash = 0xCBF29CE484222325L;
        hash = mix(hash, chunk.id().x());
        hash = mix(hash, chunk.id().y());
        hash = mix(hash, chunk.id().z());
        hash = mix(hash, chunk.size());
        boolean containsProtectedBlockState = false;
        for (int y = 0; y < chunk.size(); y++) {
            for (int z = 0; z < chunk.size(); z++) {
                for (int x = 0; x < chunk.size(); x++) {
                    lastChunkInspectionCellScanCount++;
                    EchoVoxelBlockState state = chunk.stateAtLocal(x, y, z);
                    hash = mix(hash, state.block().id().hashCode());
                    hash = mix(hash, state.properties().hashCode());
                    hash = mix(hash, state.tickVersion());
                    if (state.properties().containsKey("blockEntityId")
                            || state.properties().containsKey("canonicalId")
                            || state.properties().containsKey("machineKind")) {
                        containsProtectedBlockState = true;
                    }
                }
            }
        }
        return new ChunkInspection(hash, containsProtectedBlockState);
    }

    private void resetChunkInspectionCounters() {
        lastChunkInspectionScanCount = 0;
        lastChunkInspectionCellScanCount = 0;
    }

    private static long mix(long hash, long value) {
        long mixed = hash ^ value;
        return mixed * 0x100000001B3L;
    }

    private List<EchoVoxelChunk> sortedCachedChunks() {
        return chunkCache.entrySet().stream()
                .sorted(Map.Entry.<EchoVoxelChunkId, EchoVoxelChunk>comparingByKey(
                        java.util.Comparator.comparingInt(EchoVoxelChunkId::x)
                                .thenComparingInt(EchoVoxelChunkId::y)
                                .thenComparingInt(EchoVoxelChunkId::z)
                ))
                .map(Map.Entry::getValue)
                .toList();
    }

    private record CachedChunkInspection(
            EchoVoxelChunk chunk,
            long version,
            ChunkInspection inspection
    ) {
        private boolean matches(EchoVoxelChunk candidate) {
            return chunk == candidate && candidate != null && version == candidate.version();
        }
    }

    private record ChunkInspection(
            long fingerprint,
            boolean containsProtectedBlockState
    ) {
        static final ChunkInspection EMPTY = new ChunkInspection(0L, false);
    }

    record EchoClientBiomeHazardResult(
            EchoClientHazardState state,
            int damage,
            EchoClientDamageSource source
    ) {
    }
}
