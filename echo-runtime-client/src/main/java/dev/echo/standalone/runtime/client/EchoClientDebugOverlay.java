package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

import java.util.Locale;

final class EchoClientDebugOverlay {
    private EchoClientDebugOverlay() {
    }

    static String text(
            int fps,
            EchoClientGameState state,
            EchoClientScreenKind screenKind,
            EchoClientGameSession session,
            EchoClientGameplay gameplay
    ) {
        return text(fps, state, screenKind, session, gameplay, null);
    }

    static String text(
            int fps,
            EchoClientGameState state,
            EchoClientScreenKind screenKind,
            EchoClientGameSession session,
            EchoClientGameplay gameplay,
            EchoClientRenderer renderer
    ) {
        EchoVoxelPlayerState player = session.player().state();
        int chunkSize = session.world().chunkSize();
        int chunkX = Math.floorDiv((int) Math.floor(player.x()), chunkSize);
        int chunkZ = Math.floorDiv((int) Math.floor(player.z()), chunkSize);
        EchoClientBiomeEnvironment environment =
                EchoClientBiomeEnvironment.fromBiome(session.world().biomeAt(player.x(), player.z()));
        EchoClientEntitySpawnSummary spawn = session.entitySpawnSummary();
        EchoClientEntityAiSummary ai = session.entityAiSummary();
        EchoClientMachineStateSnapshot machines = session.machineStateSnapshot();
        return String.join("\n",
                "ECHO ASHFALL DEBUG",
                "FPS " + fps + " STATE " + state.name(),
                "SCREEN " + screenKind.name(),
                "XYZ " + fixed(player.x(), 2) + " " + fixed(player.y(), 2) + " " + fixed(player.z(), 2),
                "LOOK " + fixed(player.yawDegrees(), 1) + " " + fixed(player.pitchDegrees(), 1),
                "CHUNK " + chunkX + " " + chunkZ
                        + " SIZE " + chunkSize
                        + " LOADED " + session.world().loadedChunkCount()
                        + " CACHED " + session.cachedChunkCount(),
                "BIOME " + compact(session.world().biomeAt(player.x(), player.z()).id(), 44),
                "ENV " + compact(environment.ambienceClipId(), 36) + " " + environment.fogDebugText(),
                "HAZ " + compact(session.hazardState().hazardId(), 36)
                        + " EXP " + session.hazardState().exposurePercent()
                        + " DMG " + session.hazardState().lastDamage(),
                "HEALTH " + session.playerVitals().currentHealth()
                        + " OF " + session.playerVitals().maxHealth()
                        + " DMG " + session.playerVitals().lastDamage(),
                "FOOD " + session.playerVitals().foodLevel()
                        + " OF " + EchoClientPlayerVitals.DEFAULT_MAX_FOOD
                        + " SAT " + fixed(session.playerVitals().saturation(), 1)
                        + " EXH " + fixed(session.playerVitals().exhaustion(), 1),
                "MODE " + session.gameMode().name()
                        + " ARMOR " + session.playerCombatState().equipment().armorPoints()
                        + " SRC " + compact(session.playerCombatState().lastDamageSource().id(), 28),
                "XP L " + session.progressionState().level()
                        + " " + session.progressionState().experienceIntoLevel()
                        + "/" + session.progressionState().experienceForNextLevel()
                        + " TOTAL " + session.progressionState().experience(),
                toolText(session.selectedToolStatus(gameplay.target() == null
                        ? dev.echo.standalone.runtime.world.EchoVoxelBlock.AIR
                        : gameplay.target().block())),
                "MACHINE BE " + machines.blockEntities().size()
                        + " GRAPH " + (machines.graphConnected() ? "CONNECTED" : "DISCONNECTED"),
                "ENTITIES " + session.livingEntityCount()
                        + " HOSTILE " + session.hostileEntityCount()
                        + " R " + session.renderedEntityCount()
                        + " OF " + session.entityRenderCandidateCount(),
                "DROPS " + session.droppedItemCount() + " ITEMS " + session.droppedItemQuantity(),
                "DROP R " + session.droppedItemRenderCount()
                        + " OF " + session.droppedItemRenderCandidateCount()
                        + " PHYS " + session.droppedItemPhysicsStepCount()
                        + " WORK " + session.droppedItemPhysicsDropWorkCount()
                        + " LOOK " + session.droppedItemPhysicsBlockLookupCount()
                        + " IDX " + session.droppedItemPhysicsChunkIndexBuildCount(),
                renderText(renderer),
                atlasText(renderer),
                "SPAWN " + compact(spawn.definitionId(), 36)
                        + " " + compact(spawn.reason(), 24)
                        + " N " + spawn.spawned(),
                "AI I " + ai.idle()
                        + " P " + ai.pursuing()
                        + " A " + ai.attacking()
                        + " M " + ai.totalMovements()
                        + " HIT " + ai.totalAttacks()
                        + " " + compact(ai.reason(), 18),
                targetText(gameplay.target()),
                breakText(gameplay.breakProgress())
        );
    }

    static String targetText(EchoVoxelHit target) {
        if (target == null || target.block().air()) {
            return "TARGET NONE";
        }
        return "TARGET "
                + compact(target.block().id(), 44)
                + " AT " + target.x() + " " + target.y() + " " + target.z()
                + " D " + fixed(target.distance(), 1);
    }

    static String breakText(double breakProgress) {
        int percent = (int) Math.round(clamp(breakProgress, 0.0D, 1.0D) * 100.0D);
        return "BREAK " + percent + "%";
    }

    private static String toolText(EchoClientToolStatus tool) {
        EchoClientToolStatus safeTool = tool == null ? EchoClientToolStatus.hand() : tool;
        if (!safeTool.activeTool()) {
            return "TOOL HAND SPD 1.0";
        }
        return "TOOL " + compact(safeTool.itemId(), 36)
                + " DUR " + safeTool.durability()
                + "/" + safeTool.maxDurability()
                + " SPD " + fixed(safeTool.miningSpeed(), 1);
    }

    private static String renderText(EchoClientRenderer renderer) {
        if (renderer == null) {
            return "RENDER CHUNK FULL 0 DIRTY 0 UP 0/0 PEND 0 MESH H 0 B 0 E 0 PROJ 0";
        }
        return "RENDER CHUNK FULL " + renderer.lastFullChunkUpdateCount()
                + " DIRTY " + renderer.lastDirtyChunkUpdateCount()
                + " UP " + renderer.lastChunkUploadCount()
                + "/" + renderer.lastChunkUploadBudget()
                + " PEND " + renderer.lastPendingChunkUploadCount()
                + " MESH H " + renderer.lastCpuChunkMeshCacheHitCount()
                + " B " + renderer.lastCpuChunkMeshCacheBuildCount()
                + " E " + renderer.lastCpuChunkMeshCacheEvictionCount()
                + " PROJ " + renderer.projectionRebuildCount();
    }

    private static String atlasText(EchoClientRenderer renderer) {
        if (renderer == null) {
            return "ATLAS REBUILD 0 REUSE 0 RES 0 TILE 0 DEC 0 DUP 0";
        }
        EchoClientTextureAtlas atlas = renderer.atlas();
        return "ATLAS REBUILD " + renderer.atlasRebuildCount()
                + " REUSE " + renderer.atlasReuseCount()
                + " RES " + atlas.cachedBlockTextureResolutionCount()
                + " TILE " + atlas.cachedResourcePackTileCount()
                + " DEC " + atlas.resourcePackTileDecodeCount()
                + " DUP " + atlas.lastRemovedBaseAtlasRequestCount();
    }

    private static String compact(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            return "UNKNOWN";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        int head = Math.max(4, maxLength / 2 - 2);
        int tail = Math.max(4, maxLength - head - 3);
        return text.substring(0, head) + "..." + text.substring(text.length() - tail);
    }

    private static String fixed(double value, int decimals) {
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }

    private static double clamp(double value, double minimum, double maximum) {
        if (!Double.isFinite(value)) {
            return minimum;
        }
        return Math.max(minimum, Math.min(maximum, value));
    }
}
