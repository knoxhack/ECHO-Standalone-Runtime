package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;

import java.util.ArrayList;
import java.util.List;

record EchoClientRuntimeDiagnosticsSnapshot(
        boolean activeWorld,
        String slotId,
        String displayName,
        int loadedChunks,
        int cachedChunks,
        String biomeId,
        String hazardId,
        int hazardExposurePercent,
        int currentHealth,
        int maxHealth,
        int livingEntities,
        int hostileEntities,
        int renderedEntities,
        int renderCandidateEntities,
        int droppedItems,
        int droppedItemQuantity,
        int renderedDroppedItems,
        int renderCandidateDroppedItems,
        int droppedItemPhysicsSteps,
        int droppedItemPhysicsDropWork,
        int droppedItemPhysicsBlockLookups,
        int droppedItemPhysicsChunkIndexBuilds,
        int machineBlockEntities,
        EchoClientRenderDiagnosticsSnapshot renderDiagnostics,
        EchoClientFramePacingSnapshot framePacing,
        EchoClientAudioDiagnosticsSnapshot audioDiagnostics
) {
    static final EchoClientRuntimeDiagnosticsSnapshot EMPTY = new EchoClientRuntimeDiagnosticsSnapshot(
            false,
            "",
            "",
            0,
            0,
            "",
            "",
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            EchoClientRenderDiagnosticsSnapshot.EMPTY,
            EchoClientFramePacingSnapshot.EMPTY,
            EchoClientAudioDiagnosticsSnapshot.EMPTY
    );

    EchoClientRuntimeDiagnosticsSnapshot {
        slotId = clean(slotId);
        displayName = clean(displayName);
        biomeId = clean(biomeId);
        hazardId = clean(hazardId);
        loadedChunks = Math.max(0, loadedChunks);
        cachedChunks = Math.max(0, cachedChunks);
        hazardExposurePercent = Math.max(0, Math.min(100, hazardExposurePercent));
        currentHealth = Math.max(0, currentHealth);
        maxHealth = Math.max(0, maxHealth);
        livingEntities = Math.max(0, livingEntities);
        hostileEntities = Math.max(0, hostileEntities);
        renderedEntities = Math.max(0, renderedEntities);
        renderCandidateEntities = Math.max(0, renderCandidateEntities);
        droppedItems = Math.max(0, droppedItems);
        droppedItemQuantity = Math.max(0, droppedItemQuantity);
        renderedDroppedItems = Math.max(0, renderedDroppedItems);
        renderCandidateDroppedItems = Math.max(0, renderCandidateDroppedItems);
        droppedItemPhysicsSteps = Math.max(0, droppedItemPhysicsSteps);
        droppedItemPhysicsDropWork = Math.max(0, droppedItemPhysicsDropWork);
        droppedItemPhysicsBlockLookups = Math.max(0, droppedItemPhysicsBlockLookups);
        droppedItemPhysicsChunkIndexBuilds = Math.max(0, droppedItemPhysicsChunkIndexBuilds);
        machineBlockEntities = Math.max(0, machineBlockEntities);
        renderDiagnostics = renderDiagnostics == null
                ? EchoClientRenderDiagnosticsSnapshot.EMPTY
                : renderDiagnostics;
        framePacing = framePacing == null
                ? EchoClientFramePacingSnapshot.EMPTY
                : framePacing;
        audioDiagnostics = audioDiagnostics == null
                ? EchoClientAudioDiagnosticsSnapshot.EMPTY
                : audioDiagnostics;
        activeWorld = activeWorld && !slotId.isBlank();
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(EchoClientWorldSession worldSession) {
        return from(
                worldSession,
                EchoClientRenderDiagnosticsSnapshot.EMPTY,
                EchoClientFramePacingSnapshot.EMPTY,
                EchoClientAudioDiagnosticsSnapshot.EMPTY
        );
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderer renderer
    ) {
        return from(
                worldSession,
                EchoClientRenderDiagnosticsSnapshot.from(renderer),
                EchoClientFramePacingSnapshot.EMPTY,
                EchoClientAudioDiagnosticsSnapshot.EMPTY
        );
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderer renderer,
            EchoClientFramePacingSnapshot framePacing
    ) {
        return from(
                worldSession,
                EchoClientRenderDiagnosticsSnapshot.from(renderer),
                framePacing,
                EchoClientAudioDiagnosticsSnapshot.EMPTY
        );
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderer renderer,
            EchoClientFramePacingSnapshot framePacing,
            EchoClientAudioDiagnosticsSnapshot audioDiagnostics
    ) {
        return from(worldSession, EchoClientRenderDiagnosticsSnapshot.from(renderer), framePacing, audioDiagnostics);
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderDiagnosticsSnapshot renderDiagnostics
    ) {
        return from(
                worldSession,
                renderDiagnostics,
                EchoClientFramePacingSnapshot.EMPTY,
                EchoClientAudioDiagnosticsSnapshot.EMPTY
        );
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderDiagnosticsSnapshot renderDiagnostics,
            EchoClientFramePacingSnapshot framePacing
    ) {
        return from(worldSession, renderDiagnostics, framePacing, EchoClientAudioDiagnosticsSnapshot.EMPTY);
    }

    static EchoClientRuntimeDiagnosticsSnapshot from(
            EchoClientWorldSession worldSession,
            EchoClientRenderDiagnosticsSnapshot renderDiagnostics,
            EchoClientFramePacingSnapshot framePacing,
            EchoClientAudioDiagnosticsSnapshot audioDiagnostics
    ) {
        if (worldSession == null || worldSession.gameSession() == null) {
            return new EchoClientRuntimeDiagnosticsSnapshot(
                    false,
                    "",
                    "",
                    0,
                    0,
                    "",
                    "",
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    renderDiagnostics,
                    framePacing,
                    audioDiagnostics
            );
        }
        EchoClientGameSession session = worldSession.gameSession();
        EchoVoxelPlayerState player = session.player().state();
        return new EchoClientRuntimeDiagnosticsSnapshot(
                true,
                worldSession.slotId(),
                worldSession.displayName(),
                session.world().loadedChunkCount(),
                session.cachedChunkCount(),
                session.world().biomeAt(player.x(), player.z()).id(),
                session.hazardState().hazardId(),
                session.hazardState().exposurePercent(),
                session.playerVitals().currentHealth(),
                session.playerVitals().maxHealth(),
                session.livingEntityCount(),
                session.hostileEntityCount(),
                session.renderedEntityCount(),
                session.entityRenderCandidateCount(),
                session.droppedItemCount(),
                session.droppedItemQuantity(),
                session.droppedItemRenderCount(),
                session.droppedItemRenderCandidateCount(),
                session.droppedItemPhysicsStepCount(),
                session.droppedItemPhysicsDropWorkCount(),
                session.droppedItemPhysicsBlockLookupCount(),
                session.droppedItemPhysicsChunkIndexBuildCount(),
                session.machineStateSnapshot().blockEntities().size(),
                renderDiagnostics,
                framePacing,
                audioDiagnostics
        );
    }

    List<String> lines() {
        if (!activeWorld) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("World: No active world");
            lines.add(framePacing.diagnosticsLine());
            lines.addAll(audioDiagnostics.lines());
            return List.copyOf(lines);
        }
        ArrayList<String> lines = new ArrayList<>();
        lines.add("World Slot: " + slotId);
        lines.add("World Name: " + displayName);
        lines.add("Chunks Loaded: " + loadedChunks + " Cached: " + cachedChunks);
        lines.add("Biome: " + compact(biomeId, 42));
        lines.add("Hazard: " + compact(hazardId, 30) + " Exposure: " + hazardExposurePercent + "%");
        lines.add("Vitals: " + currentHealth + "/" + maxHealth);
        lines.add("Entities: " + livingEntities
                + " Hostile: " + hostileEntities
                + " Rendered " + renderedEntities
                + " Nearby " + renderCandidateEntities);
        lines.add("Items: Drops " + droppedItems
                + " Quantity " + droppedItemQuantity
                + " Rendered " + renderedDroppedItems
                + " Nearby " + renderCandidateDroppedItems);
        lines.add("Item Physics: Steps " + droppedItemPhysicsSteps
                + " Work " + droppedItemPhysicsDropWork
                + " Lookups " + droppedItemPhysicsBlockLookups
                + " Chunk Indexes " + droppedItemPhysicsChunkIndexBuilds);
        lines.add(framePacing.diagnosticsLine());
        lines.addAll(audioDiagnostics.lines());
        lines.add(renderDiagnostics.rendererLine());
        lines.add(renderDiagnostics.atlasLine());
        lines.add("Machines: Block Entities " + machineBlockEntities);
        return List.copyOf(lines);
    }

    private static String compact(String text, int maxLength) {
        String clean = clean(text);
        if (clean.isBlank()) {
            return "UNKNOWN";
        }
        if (clean.length() <= maxLength) {
            return clean;
        }
        return clean.substring(0, maxLength);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

record EchoClientRenderDiagnosticsSnapshot(
        int fullChunkUpdates,
        int dirtyChunkUpdates,
        int chunkUploads,
        int chunkUploadBudget,
        int pendingChunkUploads,
        int cpuMeshCacheHits,
        int cpuMeshCacheBuilds,
        int cpuMeshCacheEvictions,
        int projectionRebuilds,
        int atlasRebuilds,
        int atlasReuse,
        int atlasCachedBlockTextureResolutions,
        int atlasCachedResourcePackTiles,
        int atlasResourcePackTileDecodes,
        int atlasRemovedDuplicateBaseTiles
) {
    static final EchoClientRenderDiagnosticsSnapshot EMPTY = new EchoClientRenderDiagnosticsSnapshot(
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
    );

    EchoClientRenderDiagnosticsSnapshot {
        fullChunkUpdates = Math.max(0, fullChunkUpdates);
        dirtyChunkUpdates = Math.max(0, dirtyChunkUpdates);
        chunkUploads = Math.max(0, chunkUploads);
        chunkUploadBudget = Math.max(0, chunkUploadBudget);
        pendingChunkUploads = Math.max(0, pendingChunkUploads);
        cpuMeshCacheHits = Math.max(0, cpuMeshCacheHits);
        cpuMeshCacheBuilds = Math.max(0, cpuMeshCacheBuilds);
        cpuMeshCacheEvictions = Math.max(0, cpuMeshCacheEvictions);
        projectionRebuilds = Math.max(0, projectionRebuilds);
        atlasRebuilds = Math.max(0, atlasRebuilds);
        atlasReuse = Math.max(0, atlasReuse);
        atlasCachedBlockTextureResolutions = Math.max(0, atlasCachedBlockTextureResolutions);
        atlasCachedResourcePackTiles = Math.max(0, atlasCachedResourcePackTiles);
        atlasResourcePackTileDecodes = Math.max(0, atlasResourcePackTileDecodes);
        atlasRemovedDuplicateBaseTiles = Math.max(0, atlasRemovedDuplicateBaseTiles);
    }

    static EchoClientRenderDiagnosticsSnapshot from(EchoClientRenderer renderer) {
        if (renderer == null) {
            return EMPTY;
        }
        EchoClientTextureAtlas atlas = renderer.atlas();
        return new EchoClientRenderDiagnosticsSnapshot(
                renderer.lastFullChunkUpdateCount(),
                renderer.lastDirtyChunkUpdateCount(),
                renderer.lastChunkUploadCount(),
                renderer.lastChunkUploadBudget(),
                renderer.lastPendingChunkUploadCount(),
                renderer.lastCpuChunkMeshCacheHitCount(),
                renderer.lastCpuChunkMeshCacheBuildCount(),
                renderer.lastCpuChunkMeshCacheEvictionCount(),
                renderer.projectionRebuildCount(),
                renderer.atlasRebuildCount(),
                renderer.atlasReuseCount(),
                atlas.cachedBlockTextureResolutionCount(),
                atlas.cachedResourcePackTileCount(),
                atlas.resourcePackTileDecodeCount(),
                atlas.lastRemovedBaseAtlasRequestCount()
        );
    }

    String rendererLine() {
        return "Renderer: Full Chunks " + fullChunkUpdates
                + " Dirty Chunks " + dirtyChunkUpdates
                + " Uploads " + chunkUploads
                + "/" + chunkUploadBudget
                + " Pending " + pendingChunkUploads
                + " Mesh Hits " + cpuMeshCacheHits
                + " Builds " + cpuMeshCacheBuilds
                + " Evictions " + cpuMeshCacheEvictions
                + " Projection " + projectionRebuilds;
    }

    String atlasLine() {
        return "Atlas: Rebuilds " + atlasRebuilds
                + " Reuse " + atlasReuse
                + " Resolutions " + atlasCachedBlockTextureResolutions
                + " Tiles " + atlasCachedResourcePackTiles
                + " Decodes " + atlasResourcePackTileDecodes
                + " Duplicate Base Tiles " + atlasRemovedDuplicateBaseTiles;
    }
}
