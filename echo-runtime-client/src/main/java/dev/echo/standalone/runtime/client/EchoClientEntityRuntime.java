package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.entity.EchoEntityStore;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.ArrayList;
import java.util.List;

final class EchoClientEntityRuntime {
    static final int MAX_RENDERED_ENTITIES = 96;
    static final double MAX_RENDER_RADIUS_BLOCKS = 56.0D;

    private static final double RENDER_CACHE_POSITION_EPSILON_BLOCKS = 0.25D;
    private static final double RENDER_CACHE_POSITION_EPSILON_SQUARED =
            RENDER_CACHE_POSITION_EPSILON_BLOCKS * RENDER_CACHE_POSITION_EPSILON_BLOCKS;

    private final EchoEntityStore store = new EchoEntityStore();
    private EchoClientEntitySpawner spawner;
    private final EchoClientEntityAi ai = new EchoClientEntityAi();
    private long manualSpawnSequence;
    private int lastRenderCandidateCount;
    private int lastRenderReturnedCount;
    private boolean lastRenderCacheHit;
    private long renderCacheStoreVersion = Long.MIN_VALUE;
    private double renderCacheX;
    private double renderCacheY;
    private double renderCacheZ;
    private double renderCacheRadius;
    private int renderCacheLimit;
    private int renderCacheCandidateCount;
    private List<EchoEntityState> renderCacheEntities = List.of();
    private EchoEntityState[] renderSelectionEntities = new EchoEntityState[0];
    private double[] renderSelectionDistances = new double[0];

    EchoClientEntityRuntime(EchoClientEntityCatalog entityCatalog) {
        spawner = new EchoClientEntitySpawner(entityCatalog);
    }

    EchoEntityStore store() {
        return store;
    }

    void updateCatalog(EchoClientEntityCatalog entityCatalog) {
        spawner = new EchoClientEntitySpawner(entityCatalog);
    }

    int livingCount() {
        return store.living().size();
    }

    int hostileCount() {
        return (int) store.hostile().stream()
                .filter(EchoEntityState::alive)
                .count();
    }

    int lastRenderCandidateCount() {
        return lastRenderCandidateCount;
    }

    int lastRenderReturnedCount() {
        return lastRenderReturnedCount;
    }

    boolean lastRenderCacheHit() {
        return lastRenderCacheHit;
    }

    List<EchoEntityState> renderEntitiesNear(EchoVoxelCamera camera, double radius, int limit) {
        lastRenderCandidateCount = 0;
        lastRenderReturnedCount = 0;
        lastRenderCacheHit = false;
        if (camera == null || limit <= 0 || radius <= 0.0D) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(1, limit), MAX_RENDERED_ENTITIES);
        long storeVersion = store.version();
        if (renderCacheMatches(storeVersion, camera.x(), camera.y(), camera.z(), radius, safeLimit)) {
            lastRenderCandidateCount = renderCacheCandidateCount;
            lastRenderReturnedCount = renderCacheEntities.size();
            lastRenderCacheHit = true;
            return renderCacheEntities;
        }

        List<EchoEntityState> living = store.living();
        if (living.isEmpty()) {
            cacheRenderEntities(storeVersion, camera.x(), camera.y(), camera.z(), radius, safeLimit, 0, List.of());
            return List.of();
        }

        ensureRenderSelectionCapacity(safeLimit);
        double radiusSquared = radius * radius;
        int candidateCount = 0;
        int visibleCount = 0;
        for (EchoEntityState entity : living) {
            if (entity == null || !entity.alive()) {
                continue;
            }
            double distanceSquared = entityDistanceSquared(camera, entity);
            if (distanceSquared > radiusSquared) {
                continue;
            }
            candidateCount++;
            visibleCount = selectRenderEntity(entity, distanceSquared, visibleCount, safeLimit);
        }
        lastRenderCandidateCount = candidateCount;
        if (candidateCount == 0) {
            cacheRenderEntities(storeVersion, camera.x(), camera.y(), camera.z(), radius, safeLimit, 0, List.of());
            return List.of();
        }

        sortRenderSelectionByStableId(visibleCount);
        ArrayList<EchoEntityState> visible = new ArrayList<>(visibleCount);
        for (int index = 0; index < visibleCount; index++) {
            visible.add(renderSelectionEntities[index]);
            renderSelectionEntities[index] = null;
        }
        lastRenderReturnedCount = visible.size();
        List<EchoEntityState> result = List.copyOf(visible);
        cacheRenderEntities(storeVersion, camera.x(), camera.y(), camera.z(), radius, safeLimit, candidateCount, result);
        return result;
    }

    EchoClientEntitySpawnSummary spawnSummary() {
        return spawner.lastSummary();
    }

    EchoClientEntityAiSummary aiSummary() {
        return ai.lastSummary();
    }

    EchoClientEntityAttackResult attackNearest(EchoVoxelPlayerState player, double maxDistance, int damage) {
        if (player == null || damage <= 0) {
            return EchoClientEntityAttackResult.miss("invalid_attack");
        }
        TargetCandidate target = nearestAttackTarget(player, maxDistance);
        if (target == null) {
            return EchoClientEntityAttackResult.miss("no_target");
        }
        EchoEntityState before = target.entity();
        EchoEntityState after = before.withHealth(before.health().damage(damage));
        if (after.alive()) {
            store.update(after);
        } else {
            store.remove(before.id());
        }
        return EchoClientEntityAttackResult.hit(before, after, damage, target.distance());
    }

    EchoClientEntityInteractionResult interactNearest(EchoVoxelPlayerState player, double maxDistance) {
        if (player == null) {
            return EchoClientEntityInteractionResult.miss("invalid_interaction");
        }
        TargetCandidate target = nearestInteractionTarget(player, maxDistance);
        if (target == null) {
            return EchoClientEntityInteractionResult.miss("no_target");
        }
        return EchoClientEntityInteractionResult.hit(target.entity(), target.distance());
    }

    EchoEntityState spawn(EchoEntityDefinition definition, EchoWorldPosition position) {
        if (definition == null || position == null) {
            return null;
        }
        EchoEntityState entity = new EchoEntityState(
                nextManualSpawnId(),
                definition,
                new EchoEntityPositionComponent(position),
                new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
        );
        store.register(entity);
        return entity;
    }

    EntityTickResult tick(EchoVoxelWorld world, EchoVoxelPlayerState player, double deltaSeconds) {
        EchoClientEntitySpawnSummary spawnSummary = spawner.tick(store, world, player, deltaSeconds);
        EchoClientEntityAiSummary aiSummary = ai.tick(store, world, player, deltaSeconds);
        return new EntityTickResult(spawnSummary, aiSummary.attacks());
    }

    List<EchoClientEntitySnapshot> snapshots() {
        return store.all().stream()
                .map(EchoClientEntitySnapshot::fromEntity)
                .toList();
    }

    void applySnapshots(List<EchoClientEntitySnapshot> snapshots) {
        store.clear();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }
        for (EchoClientEntitySnapshot snapshot : snapshots) {
            if (snapshot != null) {
                store.register(snapshot.entity());
            }
        }
    }

    void clear() {
        store.clear();
        clearRenderCache();
    }

    private void ensureRenderSelectionCapacity(int limit) {
        if (renderSelectionEntities.length >= limit) {
            return;
        }
        renderSelectionEntities = new EchoEntityState[limit];
        renderSelectionDistances = new double[limit];
    }

    private int selectRenderEntity(
            EchoEntityState entity,
            double distanceSquared,
            int visibleCount,
            int limit
    ) {
        int insertionIndex = visibleCount;
        while (insertionIndex > 0
                && compareRenderEntity(
                        entity,
                        distanceSquared,
                        renderSelectionEntities[insertionIndex - 1],
                        renderSelectionDistances[insertionIndex - 1]) < 0) {
            insertionIndex--;
        }
        if (visibleCount >= limit && insertionIndex >= limit) {
            return visibleCount;
        }
        int nextVisibleCount = Math.min(visibleCount + 1, limit);
        for (int index = nextVisibleCount - 1; index > insertionIndex; index--) {
            renderSelectionEntities[index] = renderSelectionEntities[index - 1];
            renderSelectionDistances[index] = renderSelectionDistances[index - 1];
        }
        renderSelectionEntities[insertionIndex] = entity;
        renderSelectionDistances[insertionIndex] = distanceSquared;
        return nextVisibleCount;
    }

    private void sortRenderSelectionByStableId(int visibleCount) {
        for (int index = 1; index < visibleCount; index++) {
            EchoEntityState entity = renderSelectionEntities[index];
            double distanceSquared = renderSelectionDistances[index];
            int insertionIndex = index;
            while (insertionIndex > 0
                    && compareEntityId(entity, renderSelectionEntities[insertionIndex - 1]) < 0) {
                renderSelectionEntities[insertionIndex] = renderSelectionEntities[insertionIndex - 1];
                renderSelectionDistances[insertionIndex] = renderSelectionDistances[insertionIndex - 1];
                insertionIndex--;
            }
            renderSelectionEntities[insertionIndex] = entity;
            renderSelectionDistances[insertionIndex] = distanceSquared;
        }
    }

    private boolean renderCacheMatches(
            long storeVersion,
            double x,
            double y,
            double z,
            double radius,
            int limit
    ) {
        return renderCacheStoreVersion == storeVersion
                && renderCachePositionMatches(x, y, z)
                && sameDouble(renderCacheRadius, radius)
                && renderCacheLimit == limit;
    }

    private boolean renderCachePositionMatches(double x, double y, double z) {
        if (sameDouble(renderCacheX, x) && sameDouble(renderCacheY, y) && sameDouble(renderCacheZ, z)) {
            return true;
        }
        double dx = renderCacheX - x;
        double dy = renderCacheY - y;
        double dz = renderCacheZ - z;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        return distanceSquared <= RENDER_CACHE_POSITION_EPSILON_SQUARED;
    }

    private void cacheRenderEntities(
            long storeVersion,
            double x,
            double y,
            double z,
            double radius,
            int limit,
            int candidateCount,
            List<EchoEntityState> entities
    ) {
        renderCacheStoreVersion = storeVersion;
        renderCacheX = x;
        renderCacheY = y;
        renderCacheZ = z;
        renderCacheRadius = radius;
        renderCacheLimit = limit;
        renderCacheCandidateCount = candidateCount;
        renderCacheEntities = entities == null ? List.of() : entities;
    }

    private void clearRenderCache() {
        renderCacheStoreVersion = Long.MIN_VALUE;
        renderCacheEntities = List.of();
        renderCacheCandidateCount = 0;
        lastRenderCacheHit = false;
    }

    private static double entityDistanceSquared(EchoVoxelCamera camera, EchoEntityState entity) {
        EchoWorldPosition position = entity.worldPosition();
        double dx = position.x() + 0.5D - camera.x();
        double dy = position.y() + entityHeight(entity) * 0.5D - camera.y();
        double dz = position.z() + 0.5D - camera.z();
        return dx * dx + dy * dy + dz * dz;
    }

    private static int compareRenderEntity(
            EchoEntityState left,
            double leftDistanceSquared,
            EchoEntityState right,
            double rightDistanceSquared
    ) {
        int distanceComparison = Double.compare(leftDistanceSquared, rightDistanceSquared);
        if (distanceComparison != 0) {
            return distanceComparison;
        }
        return compareEntityId(left, right);
    }

    private static int compareEntityId(EchoEntityState left, EchoEntityState right) {
        String leftId = left == null ? "" : left.id().value();
        String rightId = right == null ? "" : right.id().value();
        return leftId.compareTo(rightId);
    }

    private static boolean sameDouble(double left, double right) {
        return Double.doubleToLongBits(left) == Double.doubleToLongBits(right);
    }

    private TargetCandidate nearestAttackTarget(EchoVoxelPlayerState player, double maxDistance) {
        double reach = Double.isFinite(maxDistance)
                ? Math.max(0.0D, Math.min(player.reach(), maxDistance))
                : player.reach();
        if (reach <= 0.0D) {
            return null;
        }
        double yawRadians = Math.toRadians(player.yawDegrees());
        double pitchRadians = Math.toRadians(player.pitchDegrees());
        double forwardX = Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        TargetCandidate nearest = null;
        for (EchoEntityState entity : store.living()) {
            if (!attackable(entity)) {
                continue;
            }
            EchoWorldPosition position = entity.worldPosition();
            double centerX = position.x() + 0.5D;
            double centerY = position.y() + entityHeight(entity) * 0.5D;
            double centerZ = position.z() + 0.5D;
            double dx = centerX - player.x();
            double dy = centerY - player.eyeY();
            double dz = centerZ - player.z();
            double along = dx * forwardX + dy * forwardY + dz * forwardZ;
            if (along < 0.15D || along > reach) {
                continue;
            }
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            double perpendicularSquared = Math.max(0.0D, distanceSquared - along * along);
            double radius = entityAttackRadius(entity);
            if (perpendicularSquared > radius * radius) {
                continue;
            }
            if (nearest == null || along < nearest.distance()) {
                nearest = new TargetCandidate(entity, along);
            }
        }
        return nearest;
    }

    private TargetCandidate nearestInteractionTarget(EchoVoxelPlayerState player, double maxDistance) {
        double reach = Double.isFinite(maxDistance)
                ? Math.max(0.0D, Math.min(player.reach(), maxDistance))
                : player.reach();
        if (reach <= 0.0D) {
            return null;
        }
        double yawRadians = Math.toRadians(player.yawDegrees());
        double pitchRadians = Math.toRadians(player.pitchDegrees());
        double forwardX = Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);

        TargetCandidate nearest = null;
        for (EchoEntityState entity : store.living()) {
            if (!interactable(entity)) {
                continue;
            }
            EchoWorldPosition position = entity.worldPosition();
            double centerX = position.x() + 0.5D;
            double centerY = position.y() + entityHeight(entity) * 0.5D;
            double centerZ = position.z() + 0.5D;
            double dx = centerX - player.x();
            double dy = centerY - player.eyeY();
            double dz = centerZ - player.z();
            double along = dx * forwardX + dy * forwardY + dz * forwardZ;
            if (along < 0.15D || along > reach) {
                continue;
            }
            double distanceSquared = dx * dx + dy * dy + dz * dz;
            double perpendicularSquared = Math.max(0.0D, distanceSquared - along * along);
            double radius = entityInteractionRadius(entity);
            if (perpendicularSquared > radius * radius) {
                continue;
            }
            if (nearest == null || along < nearest.distance()) {
                nearest = new TargetCandidate(entity, along);
            }
        }
        return nearest;
    }

    private static boolean attackable(EchoEntityState entity) {
        return entity != null
                && entity.alive()
                && entity.definition().kind() != EchoEntityKind.PLAYER
                && entity.definition().kind() != EchoEntityKind.PROP;
    }

    private static boolean interactable(EchoEntityState entity) {
        return entity != null
                && entity.alive()
                && (entity.definition().kind() == EchoEntityKind.NPC
                || entity.definition().kind() == EchoEntityKind.FAMILIAR);
    }

    private static double entityAttackRadius(EchoEntityState entity) {
        return entity.definition().kind() == EchoEntityKind.HOSTILE ? 0.85D : 0.65D;
    }

    private static double entityInteractionRadius(EchoEntityState entity) {
        return entity.definition().kind() == EchoEntityKind.NPC ? 0.95D : 0.75D;
    }

    private static double entityHeight(EchoEntityState entity) {
        return entity.definition().kind() == EchoEntityKind.HOSTILE ? 1.72D : 1.20D;
    }

    private EchoEntityId nextManualSpawnId() {
        EchoEntityId id;
        do {
            id = new EchoEntityId("client:spawn_egg_" + (++manualSpawnSequence));
        } while (store.find(id).isPresent());
        return id;
    }

    record EntityTickResult(
            EchoClientEntitySpawnSummary spawnSummary,
            int hostileAttacks
    ) {
    }

    private record TargetCandidate(
            EchoEntityState entity,
            double distance
    ) {
    }
}
