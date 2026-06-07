package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

final class EchoClientDroppedItemRuntime {
    static final int MAX_ACTIVE_DROPS = 256;
    static final int MAX_RENDERED_DROPS = 96;
    static final double MAX_RENDER_RADIUS_BLOCKS = 48.0D;

    private static final double MERGE_RADIUS_SQUARED = 1.25D * 1.25D;
    private static final double DESPAWN_SECONDS = 300.0D;
    private static final double DESPAWN_CHECK_SECONDS = 1.0D;
    private static final double SPATIAL_BUCKET_SIZE = 4.0D;
    private static final double MAX_PHYSICS_DELTA_SECONDS = 0.25D;
    private static final double PHYSICS_STEP_SECONDS = 0.05D;
    static final int MAX_PHYSICS_STEPS_PER_TICK = 2;
    private static final double GRAVITY_BLOCKS_PER_SECOND = 18.0D;
    private static final double MAX_FALL_SPEED_BLOCKS_PER_SECOND = -14.0D;
    private static final double GROUND_CLEARANCE = 0.08D;
    private static final double SETTLE_EPSILON = 0.002D;
    private static final double SETTLED_RECHECK_SECONDS = 0.75D;
    private static final double SETTLED_RECHECK_JITTER_SECONDS = 0.50D;
    private static final double SETTLED_RECHECK_DEFER_SECONDS = 0.05D;
    private static final int MAX_SETTLED_PROBES_PER_STEP = 32;
    static final int MAX_PHYSICS_DROPS_PER_STEP = 64;
    private static final int GROUND_PROBE_BLOCKS = 8;
    private static final double RENDER_CACHE_POSITION_EPSILON_BLOCKS = 0.25D;
    private static final double RENDER_CACHE_POSITION_EPSILON_SQUARED =
            RENDER_CACHE_POSITION_EPSILON_BLOCKS * RENDER_CACHE_POSITION_EPSILON_BLOCKS;
    private static final int BUCKET_XZ_BITS = 26;
    private static final int BUCKET_Y_BITS = 12;
    private static final int BUCKET_XZ_SHIFT = BUCKET_Y_BITS + BUCKET_XZ_BITS;
    private static final int BUCKET_Y_SHIFT = BUCKET_XZ_BITS;

    private final LinkedHashMap<String, TrackedDrop> drops = new LinkedHashMap<>();
    private final ArrayList<String> physicsDropOrder = new ArrayList<>();
    private final Map<Long, LinkedHashSet<String>> spatialBuckets = new HashMap<>();
    private final ArrayList<String> nearbyDropScratch = new ArrayList<>();
    private long nextSequence = 1L;
    private int totalQuantity;
    private double clockSeconds;
    private double despawnCheckSeconds;
    private double physicsAccumulatorSeconds;
    private int lastPhysicsStepCount;
    private int lastPhysicsBlockLookupCount;
    private int lastPhysicsChunkIndexBuildCount;
    private int lastPhysicsSettledProbeCount;
    private int lastPhysicsDropWorkCount;
    private int lastRenderCandidateCount;
    private int lastRenderReturnedCount;
    private int lastNearbyQueryBucketCount;
    private int lastNearbyQueryDropIdCount;
    private boolean lastRenderCacheHit;
    private int physicsCursorIndex;
    private WorldBlockLookup cachedPhysicsBlockLookup;
    private long renderSelectionVersion;
    private long renderGeometryVersion;
    private long renderCacheSelectionVersion = Long.MIN_VALUE;
    private long renderCacheGeometryVersion = Long.MIN_VALUE;
    private double renderCacheX;
    private double renderCacheY;
    private double renderCacheZ;
    private double renderCacheRadius;
    private int renderCacheLimit;
    private int renderCacheCandidateCount;
    private List<EchoClientDroppedItem> renderCacheDrops = List.of();
    private List<String> renderCacheDropIds = List.of();
    private EchoClientDroppedItem[] renderSelectionDrops = new EchoClientDroppedItem[0];
    private double[] renderSelectionDistances = new double[0];
    private int unsettledDropCount;
    private double nextSettledProbeAtSeconds = Double.POSITIVE_INFINITY;
    private boolean settledProbeScheduleDirty;

    EchoClientDroppedItem drop(EchoItemStack stack, double x, double y, double z) {
        if (stack == null || stack.quantity() <= 0) {
            return null;
        }
        EchoItemDefinition definition = stack.definition();
        int remaining = stack.quantity();
        EchoClientDroppedItem changed = null;
        MergeResult merge = mergeNearby(definition, remaining, x, y, z);
        remaining = merge.remainingQuantity();
        if (merge.changedDrop() != null) {
            changed = merge.changedDrop();
        }
        int maxStackSize = Math.max(1, definition.maxStackSize());
        while (remaining > 0) {
            int moved = Math.min(remaining, maxStackSize);
            String dropId = "drop-" + nextSequence++;
            EchoClientDroppedItem drop = new EchoClientDroppedItem(
                    dropId,
                    definition,
                    moved,
                    x,
                    y,
                    z,
                    0.0D
            );
            putDrop(drop);
            totalQuantity += moved;
            changed = drop;
            remaining -= moved;
            trimActiveDrops();
        }
        return changed;
    }

    int count() {
        return drops.size();
    }

    int physicsDropOrderSize() {
        return physicsDropOrder.size();
    }

    int totalQuantity() {
        return totalQuantity;
    }

    int lastPhysicsStepCount() {
        return lastPhysicsStepCount;
    }

    int lastPhysicsBlockLookupCount() {
        return lastPhysicsBlockLookupCount;
    }

    int lastPhysicsChunkIndexBuildCount() {
        return lastPhysicsChunkIndexBuildCount;
    }

    int lastPhysicsSettledProbeCount() {
        return lastPhysicsSettledProbeCount;
    }

    int lastPhysicsDropWorkCount() {
        return lastPhysicsDropWorkCount;
    }

    int lastRenderCandidateCount() {
        return lastRenderCandidateCount;
    }

    int lastRenderReturnedCount() {
        return lastRenderReturnedCount;
    }

    int lastNearbyQueryBucketCount() {
        return lastNearbyQueryBucketCount;
    }

    int lastNearbyQueryDropIdCount() {
        return lastNearbyQueryDropIdCount;
    }

    int spatialBucketCount() {
        return spatialBuckets.size();
    }

    boolean lastRenderCacheHit() {
        return lastRenderCacheHit;
    }

    List<EchoClientDroppedItem> drops() {
        return drops.values().stream()
                .map(drop -> drop.current(clockSeconds))
                .toList();
    }

    List<EchoClientDroppedItem> renderDropsNear(
            double x,
            double y,
            double z,
            double radius,
            int limit
    ) {
        lastRenderCandidateCount = 0;
        lastRenderReturnedCount = 0;
        lastRenderCacheHit = false;
        if (drops.isEmpty() || limit <= 0 || radius <= 0.0D) {
            return List.of();
        }
        int safeLimit = Math.min(Math.max(1, limit), MAX_ACTIVE_DROPS);
        if (renderCacheMatches(x, y, z, radius, safeLimit)) {
            lastRenderCandidateCount = renderCacheCandidateCount;
            lastRenderCacheHit = true;
            if (renderCacheGeometryVersion != renderGeometryVersion) {
                List<EchoClientDroppedItem> refreshedDrops = refreshCachedRenderDrops();
                if (refreshedDrops == null) {
                    invalidateRenderCache();
                } else {
                    renderCacheDrops = refreshedDrops;
                    renderCacheGeometryVersion = renderGeometryVersion;
                }
            }
            if (lastRenderCacheHit) {
                lastRenderReturnedCount = renderCacheDrops.size();
                return renderCacheDrops;
            }
        }
        ensureRenderSelectionCapacity(safeLimit);
        double radiusSquared = radius * radius;
        int candidateCount = 0;
        int visibleCount = 0;
        for (String dropId : nearbyDropIds(x, y, z, radius)) {
            TrackedDrop tracked = drops.get(dropId);
            if (tracked == null) {
                continue;
            }
            EchoClientDroppedItem drop = tracked.drop();
            double distanceSquared = drop.distanceSquared(x, y, z);
            if (distanceSquared > radiusSquared) {
                continue;
            }
            candidateCount++;
            visibleCount = selectRenderDrop(drop, distanceSquared, visibleCount, safeLimit);
        }
        lastRenderCandidateCount = candidateCount;
        if (candidateCount == 0) {
            cacheRenderDrops(x, y, z, radius, safeLimit, 0, List.of());
            return List.of();
        }
        sortRenderSelectionByStableId(visibleCount);
        ArrayList<EchoClientDroppedItem> visible = new ArrayList<>(visibleCount);
        for (int index = 0; index < visibleCount; index++) {
            visible.add(renderSelectionDrops[index]);
            renderSelectionDrops[index] = null;
        }
        lastRenderReturnedCount = visible.size();
        List<EchoClientDroppedItem> result = List.copyOf(visible);
        cacheRenderDrops(x, y, z, radius, safeLimit, lastRenderCandidateCount, result);
        return result;
    }

    List<EchoClientDroppedItemSnapshot> snapshots() {
        return drops.values().stream()
                .map(drop -> drop.current(clockSeconds).snapshot())
                .toList();
    }

    void applySnapshots(List<EchoClientDroppedItemSnapshot> snapshots) {
        drops.clear();
        physicsDropOrder.clear();
        spatialBuckets.clear();
        nextSequence = 1L;
        totalQuantity = 0;
        clockSeconds = 0.0D;
        despawnCheckSeconds = 0.0D;
        physicsAccumulatorSeconds = 0.0D;
        lastPhysicsStepCount = 0;
        lastPhysicsBlockLookupCount = 0;
        lastPhysicsChunkIndexBuildCount = 0;
        lastPhysicsSettledProbeCount = 0;
        lastPhysicsDropWorkCount = 0;
        lastRenderCandidateCount = 0;
        lastRenderReturnedCount = 0;
        lastNearbyQueryBucketCount = 0;
        lastNearbyQueryDropIdCount = 0;
        lastRenderCacheHit = false;
        physicsCursorIndex = 0;
        cachedPhysicsBlockLookup = null;
        unsettledDropCount = 0;
        nextSettledProbeAtSeconds = Double.POSITIVE_INFINITY;
        settledProbeScheduleDirty = false;
        invalidateRenderCache();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }
        for (EchoClientDroppedItemSnapshot snapshot : snapshots) {
            if (snapshot == null) {
                continue;
            }
            EchoClientDroppedItem drop = snapshot.drop();
            putDrop(drop);
            totalQuantity += drop.quantity();
            nextSequence = Math.max(nextSequence, sequenceAfter(drop.dropId()));
            trimActiveDrops();
        }
    }

    void tick(double deltaSeconds) {
        tick(deltaSeconds, null);
    }

    void tick(double deltaSeconds, EchoVoxelWorld world) {
        if (drops.isEmpty() || deltaSeconds <= 0.0D) {
            lastPhysicsStepCount = 0;
            lastPhysicsBlockLookupCount = 0;
            lastPhysicsChunkIndexBuildCount = 0;
            lastPhysicsSettledProbeCount = 0;
            lastPhysicsDropWorkCount = 0;
            if (drops.isEmpty()) {
                cachedPhysicsBlockLookup = null;
                physicsCursorIndex = 0;
                unsettledDropCount = 0;
                nextSettledProbeAtSeconds = Double.POSITIVE_INFINITY;
                settledProbeScheduleDirty = false;
            }
            return;
        }
        clockSeconds += deltaSeconds;
        if (world != null) {
            tickPhysics(world, Math.min(deltaSeconds, MAX_PHYSICS_DELTA_SECONDS));
        } else {
            physicsAccumulatorSeconds = 0.0D;
            lastPhysicsStepCount = 0;
            lastPhysicsBlockLookupCount = 0;
            lastPhysicsChunkIndexBuildCount = 0;
            lastPhysicsSettledProbeCount = 0;
            lastPhysicsDropWorkCount = 0;
            cachedPhysicsBlockLookup = null;
            physicsCursorIndex = 0;
        }
        despawnCheckSeconds += deltaSeconds;
        if (despawnCheckSeconds < DESPAWN_CHECK_SECONDS) {
            return;
        }
        despawnCheckSeconds = 0.0D;
        Iterator<Map.Entry<String, TrackedDrop>> iterator = drops.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, TrackedDrop> entry = iterator.next();
            TrackedDrop drop = entry.getValue();
            if (drop.ageSeconds(clockSeconds) >= DESPAWN_SECONDS) {
                totalQuantity -= drop.quantity();
                removeFromBucket(entry.getKey(), drop.bucketKey());
                iterator.remove();
                removeFromPhysicsOrder(entry.getKey());
                untrackPhysicsState(drop);
                invalidateRenderCache();
            }
        }
    }

    PickupResult pickupNearby(
            PickupSink sink,
            double x,
            double y,
            double z,
            double radius
    ) {
        return pickupNearby(sink, x, y, z, radius, 0.0D);
    }

    PickupResult pickupNearby(
            PickupSink sink,
            double x,
            double y,
            double z,
            double radius,
            double minimumAgeSeconds
    ) {
        if (sink == null || drops.isEmpty()) {
            return new PickupResult(0, drops.size(), "no_drops");
        }
        double safeRadius = Math.max(0.0D, radius);
        double safeMinimumAgeSeconds = Math.max(0.0D, minimumAgeSeconds);
        double radiusSquared = safeRadius * safeRadius;
        int pickedQuantity = 0;
        String reason = "no_nearby_drops";
        for (String dropId : nearbyDropIds(x, y, z, safeRadius)) {
            TrackedDrop tracked = drops.get(dropId);
            if (tracked == null) {
                continue;
            }
            EchoClientDroppedItem drop = tracked.current(clockSeconds);
            if (drop.ageSeconds() < safeMinimumAgeSeconds) {
                reason = "pickup_delayed";
                continue;
            }
            if (drop.distanceSquared(x, y, z) > radiusSquared) {
                continue;
            }
            int pickedFromDrop = collect(drop, sink);
            if (pickedFromDrop <= 0) {
                reason = "inventory_full";
                break;
            }
            pickedQuantity += pickedFromDrop;
            totalQuantity -= pickedFromDrop;
            int remaining = drop.quantity() - pickedFromDrop;
            if (remaining <= 0) {
                removeDrop(drop.dropId());
            } else {
                tracked.update(drop.withQuantityAndAge(remaining, drop.ageSeconds()), clockSeconds);
                invalidateRenderCache();
                reason = "partial";
                break;
            }
            reason = "picked_up";
        }
        return new PickupResult(pickedQuantity, drops.size(), reason);
    }

    private void tickPhysics(EchoVoxelWorld world, double deltaSeconds) {
        lastPhysicsStepCount = 0;
        lastPhysicsBlockLookupCount = 0;
        lastPhysicsChunkIndexBuildCount = 0;
        lastPhysicsSettledProbeCount = 0;
        lastPhysicsDropWorkCount = 0;
        physicsAccumulatorSeconds = Math.min(
                MAX_PHYSICS_DELTA_SECONDS,
                physicsAccumulatorSeconds + Math.max(0.0D, deltaSeconds)
        );
        if (physicsAccumulatorSeconds + 1.0E-9D < PHYSICS_STEP_SECONDS) {
            return;
        }
        WorldBlockLookup blockLookup = null;
        while (physicsAccumulatorSeconds + 1.0E-9D >= PHYSICS_STEP_SECONDS
                && lastPhysicsStepCount < MAX_PHYSICS_STEPS_PER_TICK) {
            if (hasDropNeedingPhysicsProbe()) {
                if (blockLookup == null) {
                    blockLookup = physicsBlockLookupFor(world);
                    blockLookup.resetBlockLookups();
                }
                tickPhysicsStep(blockLookup, PHYSICS_STEP_SECONDS);
            }
            lastPhysicsStepCount++;
            physicsAccumulatorSeconds -= PHYSICS_STEP_SECONDS;
        }
        if (physicsAccumulatorSeconds < 1.0E-9D) {
            physicsAccumulatorSeconds = 0.0D;
        }
        lastPhysicsBlockLookupCount = blockLookup == null ? 0 : blockLookup.blockLookups();
    }

    private WorldBlockLookup physicsBlockLookupFor(EchoVoxelWorld world) {
        if (cachedPhysicsBlockLookup == null || !cachedPhysicsBlockLookup.matches(world)) {
            cachedPhysicsBlockLookup = new WorldBlockLookup(world);
            lastPhysicsChunkIndexBuildCount++;
        }
        return cachedPhysicsBlockLookup;
    }

    private void tickPhysicsStep(WorldBlockLookup world, double deltaSeconds) {
        int dropCount = physicsDropOrder.size();
        int settledProbeBudget = Math.min(MAX_SETTLED_PROBES_PER_STEP, Math.max(1, dropCount));
        if (dropCount <= 0) {
            physicsCursorIndex = 0;
            return;
        }
        int startIndex = Math.floorMod(physicsCursorIndex, dropCount);
        int scanned = 0;
        int worked = 0;
        while (scanned < dropCount && worked < MAX_PHYSICS_DROPS_PER_STEP) {
            String dropId = physicsDropOrder.get((startIndex + scanned) % dropCount);
            scanned++;
            TrackedDrop tracked = drops.get(dropId);
            if (tracked == null) {
                continue;
            }
            if (tracked.settled()) {
                if (clockSeconds < tracked.nextSettledProbeAtSeconds()) {
                    continue;
                }
                if (settledProbeBudget <= 0) {
                    tracked.deferSettledProbe(clockSeconds);
                    settledProbeScheduleDirty = true;
                    continue;
                }
                settledProbeBudget--;
                lastPhysicsSettledProbeCount++;
            }
            worked++;
            lastPhysicsDropWorkCount++;
            EchoClientDroppedItem current = tracked.current(clockSeconds);
            double groundY = groundSurfaceYBelow(world, tracked, current, tracked.settled());
            if (tracked.settled()) {
                if (Double.isFinite(groundY) && Math.abs(current.y() - groundY) <= SETTLE_EPSILON) {
                    markTrackedSettled(tracked);
                    continue;
                }
                markTrackedUnsettled(tracked);
            }
            if (Double.isFinite(groundY) && current.y() <= groundY + SETTLE_EPSILON) {
                tracked.setVelocityY(0.0D);
                if (Math.abs(current.y() - groundY) > SETTLE_EPSILON) {
                    moveTrackedDrop(dropId, tracked, current.withPositionAndAge(
                            current.x(),
                            groundY,
                            current.z(),
                            current.ageSeconds()
                    ));
                }
                markTrackedSettled(tracked);
                continue;
            }

            double nextVelocityY = Math.max(
                    MAX_FALL_SPEED_BLOCKS_PER_SECOND,
                    tracked.velocityY() - GRAVITY_BLOCKS_PER_SECOND * deltaSeconds
            );
            double nextY = current.y() + nextVelocityY * deltaSeconds;
            if (Double.isFinite(groundY) && nextY <= groundY) {
                nextY = groundY;
                nextVelocityY = 0.0D;
            }
            tracked.setVelocityY(nextVelocityY);
            if (Math.abs(nextY - current.y()) > SETTLE_EPSILON) {
                moveTrackedDrop(dropId, tracked, current.withPositionAndAge(
                        current.x(),
                        nextY,
                        current.z(),
                        current.ageSeconds()
                ));
            }
        }
        if (physicsDropOrder.isEmpty()) {
            physicsCursorIndex = 0;
        } else {
            physicsCursorIndex = (startIndex + Math.max(1, scanned)) % physicsDropOrder.size();
        }
    }

    private boolean hasDropNeedingPhysicsProbe() {
        if (unsettledDropCount > 0) {
            return true;
        }
        if (settledProbeScheduleDirty) {
            rebuildNextSettledProbeAtSeconds();
        }
        return nextSettledProbeAtSeconds <= clockSeconds;
    }

    private MergeResult mergeNearby(
            EchoItemDefinition definition,
            int quantity,
            double x,
            double y,
            double z
    ) {
        if (definition == null || quantity <= 0 || drops.isEmpty()) {
            return new MergeResult(quantity, null);
        }
        int remaining = quantity;
        EchoClientDroppedItem changed = null;
        int maxStackSize = Math.max(1, definition.maxStackSize());
        for (String dropId : nearbyDropIds(x, y, z, 1.25D)) {
            if (remaining <= 0) {
                break;
            }
            TrackedDrop tracked = drops.get(dropId);
            if (tracked == null) {
                continue;
            }
            EchoClientDroppedItem drop = tracked.drop();
            if (!drop.itemId().equals(definition.id())
                    || drop.quantity() >= maxStackSize
                    || drop.distanceSquared(x, y, z) > MERGE_RADIUS_SQUARED) {
                continue;
            }
            int moved = Math.min(remaining, maxStackSize - drop.quantity());
            if (moved <= 0) {
                continue;
            }
            changed = drop.withQuantityAndAge(drop.quantity() + moved, 0.0D);
            tracked.update(changed, clockSeconds);
            invalidateRenderCache();
            totalQuantity += moved;
            remaining -= moved;
        }
        return new MergeResult(remaining, changed);
    }

    private int collect(EchoClientDroppedItem drop, PickupSink sink) {
        int remaining = drop.quantity();
        int picked = 0;
        int maxStackSize = Math.max(1, drop.definition().maxStackSize());
        while (remaining > 0) {
            int requested = Math.min(remaining, maxStackSize);
            EchoInventoryOperationResult result =
                    sink.collect(new EchoItemStack(drop.definition(), requested));
            if (result.quantity() <= 0) {
                break;
            }
            picked += result.quantity();
            remaining -= result.quantity();
            if (result.quantity() < requested) {
                break;
            }
        }
        return picked;
    }

    private void putDrop(EchoClientDroppedItem drop) {
        long bucketKey = bucketKey(drop.x(), drop.y(), drop.z());
        TrackedDrop previous = drops.put(drop.dropId(), new TrackedDrop(drop, clockSeconds, bucketKey));
        if (previous == null) {
            physicsDropOrder.add(drop.dropId());
            unsettledDropCount++;
        } else {
            removeFromBucket(drop.dropId(), previous.bucketKey());
            if (previous.settled()) {
                unsettledDropCount++;
                settledProbeScheduleDirty = true;
            }
        }
        spatialBuckets.computeIfAbsent(bucketKey, ignored -> new LinkedHashSet<>()).add(drop.dropId());
        invalidateRenderCache();
    }

    private void moveTrackedDrop(String dropId, TrackedDrop tracked, EchoClientDroppedItem next) {
        long nextBucketKey = bucketKey(next.x(), next.y(), next.z());
        boolean spatialBucketChanged = nextBucketKey != tracked.bucketKey();
        if (nextBucketKey != tracked.bucketKey()) {
            removeFromBucket(dropId, tracked.bucketKey());
            spatialBuckets.computeIfAbsent(nextBucketKey, ignored -> new LinkedHashSet<>()).add(dropId);
            tracked.setBucketKey(nextBucketKey);
        }
        if (blockColumnChanged(tracked.drop(), next)) {
            tracked.clearGroundSurfaceCache();
        }
        tracked.update(next, clockSeconds);
        if (spatialBucketChanged) {
            invalidateRenderCache();
        } else {
            noteRenderGeometryChanged();
        }
    }

    private void removeDrop(String dropId) {
        TrackedDrop removed = drops.remove(dropId);
        if (removed != null) {
            removeFromBucket(dropId, removed.bucketKey());
            removeFromPhysicsOrder(dropId);
            untrackPhysicsState(removed);
            invalidateRenderCache();
        }
    }

    private void trimActiveDrops() {
        if (drops.size() <= MAX_ACTIVE_DROPS) {
            return;
        }
        Iterator<Map.Entry<String, TrackedDrop>> iterator = drops.entrySet().iterator();
        while (drops.size() > MAX_ACTIVE_DROPS && iterator.hasNext()) {
            Map.Entry<String, TrackedDrop> entry = iterator.next();
            totalQuantity -= entry.getValue().quantity();
            removeFromBucket(entry.getKey(), entry.getValue().bucketKey());
            untrackPhysicsState(entry.getValue());
            iterator.remove();
            removeFromPhysicsOrder(entry.getKey());
            invalidateRenderCache();
        }
        if (totalQuantity < 0) {
            totalQuantity = 0;
        }
    }

    private void removeFromPhysicsOrder(String dropId) {
        if (physicsDropOrder.remove(dropId)) {
            normalizePhysicsCursor();
        }
    }

    private void untrackPhysicsState(TrackedDrop tracked) {
        if (tracked == null) {
            return;
        }
        if (tracked.settled()) {
            settledProbeScheduleDirty = true;
        } else {
            unsettledDropCount = Math.max(0, unsettledDropCount - 1);
        }
    }

    private void markTrackedSettled(TrackedDrop tracked) {
        if (tracked == null) {
            return;
        }
        boolean wasSettled = tracked.settled();
        tracked.markSettled(clockSeconds);
        if (wasSettled) {
            settledProbeScheduleDirty = true;
        } else {
            unsettledDropCount = Math.max(0, unsettledDropCount - 1);
            noteSettledProbeAt(tracked.nextSettledProbeAtSeconds());
        }
    }

    private void markTrackedUnsettled(TrackedDrop tracked) {
        if (tracked == null || !tracked.settled()) {
            return;
        }
        tracked.markUnsettled();
        unsettledDropCount++;
        settledProbeScheduleDirty = true;
    }

    private void noteSettledProbeAt(double probeAtSeconds) {
        if (settledProbeScheduleDirty || !Double.isFinite(probeAtSeconds)) {
            return;
        }
        nextSettledProbeAtSeconds = Math.min(nextSettledProbeAtSeconds, probeAtSeconds);
    }

    private void rebuildNextSettledProbeAtSeconds() {
        double next = Double.POSITIVE_INFINITY;
        int unsettled = 0;
        for (TrackedDrop tracked : drops.values()) {
            if (tracked == null) {
                continue;
            }
            if (tracked.settled()) {
                next = Math.min(next, tracked.nextSettledProbeAtSeconds());
            } else {
                unsettled++;
            }
        }
        unsettledDropCount = unsettled;
        nextSettledProbeAtSeconds = next;
        settledProbeScheduleDirty = false;
    }

    private void normalizePhysicsCursor() {
        if (physicsDropOrder.isEmpty()) {
            physicsCursorIndex = 0;
        } else if (physicsCursorIndex < 0 || physicsCursorIndex >= physicsDropOrder.size()) {
            physicsCursorIndex = Math.floorMod(physicsCursorIndex, physicsDropOrder.size());
        }
    }

    private void ensureRenderSelectionCapacity(int limit) {
        if (renderSelectionDrops.length >= limit) {
            return;
        }
        renderSelectionDrops = new EchoClientDroppedItem[limit];
        renderSelectionDistances = new double[limit];
    }

    private int selectRenderDrop(
            EchoClientDroppedItem drop,
            double distanceSquared,
            int visibleCount,
            int limit
    ) {
        int insertionIndex = visibleCount;
        while (insertionIndex > 0
                && compareRenderDrop(
                        drop,
                        distanceSquared,
                        renderSelectionDrops[insertionIndex - 1],
                        renderSelectionDistances[insertionIndex - 1]) < 0) {
            insertionIndex--;
        }
        if (visibleCount >= limit && insertionIndex >= limit) {
            return visibleCount;
        }
        int nextVisibleCount = Math.min(visibleCount + 1, limit);
        for (int index = nextVisibleCount - 1; index > insertionIndex; index--) {
            renderSelectionDrops[index] = renderSelectionDrops[index - 1];
            renderSelectionDistances[index] = renderSelectionDistances[index - 1];
        }
        renderSelectionDrops[insertionIndex] = drop;
        renderSelectionDistances[insertionIndex] = distanceSquared;
        return nextVisibleCount;
    }

    private void sortRenderSelectionByStableId(int visibleCount) {
        for (int index = 1; index < visibleCount; index++) {
            EchoClientDroppedItem drop = renderSelectionDrops[index];
            double distanceSquared = renderSelectionDistances[index];
            int insertionIndex = index;
            while (insertionIndex > 0
                    && compareDropId(drop, renderSelectionDrops[insertionIndex - 1]) < 0) {
                renderSelectionDrops[insertionIndex] = renderSelectionDrops[insertionIndex - 1];
                renderSelectionDistances[insertionIndex] = renderSelectionDistances[insertionIndex - 1];
                insertionIndex--;
            }
            renderSelectionDrops[insertionIndex] = drop;
            renderSelectionDistances[insertionIndex] = distanceSquared;
        }
    }

    private static int compareRenderDrop(
            EchoClientDroppedItem left,
            double leftDistanceSquared,
            EchoClientDroppedItem right,
            double rightDistanceSquared
    ) {
        int distanceComparison = Double.compare(leftDistanceSquared, rightDistanceSquared);
        if (distanceComparison != 0) {
            return distanceComparison;
        }
        String leftId = left == null ? "" : left.dropId();
        String rightId = right == null ? "" : right.dropId();
        return leftId.compareTo(rightId);
    }

    private static int compareDropId(EchoClientDroppedItem left, EchoClientDroppedItem right) {
        String leftId = left == null ? "" : left.dropId();
        String rightId = right == null ? "" : right.dropId();
        return leftId.compareTo(rightId);
    }

    private boolean renderCacheMatches(double x, double y, double z, double radius, int limit) {
        return renderCacheSelectionVersion == renderSelectionVersion
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

    private void cacheRenderDrops(
            double x,
            double y,
            double z,
            double radius,
            int limit,
            int candidateCount,
            List<EchoClientDroppedItem> drops
    ) {
        renderCacheSelectionVersion = renderSelectionVersion;
        renderCacheGeometryVersion = renderGeometryVersion;
        renderCacheX = x;
        renderCacheY = y;
        renderCacheZ = z;
        renderCacheRadius = radius;
        renderCacheLimit = limit;
        renderCacheCandidateCount = candidateCount;
        renderCacheDrops = drops == null ? List.of() : drops;
        renderCacheDropIds = renderCacheDrops.stream()
                .map(EchoClientDroppedItem::dropId)
                .toList();
    }

    private List<EchoClientDroppedItem> refreshCachedRenderDrops() {
        if (renderCacheDropIds.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoClientDroppedItem> refreshed = new ArrayList<>(renderCacheDropIds.size());
        for (String dropId : renderCacheDropIds) {
            TrackedDrop tracked = drops.get(dropId);
            if (tracked == null) {
                lastRenderCacheHit = false;
                return null;
            }
            refreshed.add(tracked.current(clockSeconds));
        }
        return List.copyOf(refreshed);
    }

    private void invalidateRenderCache() {
        renderSelectionVersion++;
        renderGeometryVersion++;
        renderCacheSelectionVersion = Long.MIN_VALUE;
        renderCacheGeometryVersion = Long.MIN_VALUE;
        renderCacheDrops = List.of();
        renderCacheDropIds = List.of();
        renderCacheCandidateCount = 0;
        lastRenderCacheHit = false;
    }

    private void noteRenderGeometryChanged() {
        renderGeometryVersion++;
    }

    private static boolean sameDouble(double left, double right) {
        return Double.doubleToLongBits(left) == Double.doubleToLongBits(right);
    }

    private List<String> nearbyDropIds(
            double x,
            double y,
            double z,
            double radius
    ) {
        nearbyDropScratch.clear();
        lastNearbyQueryBucketCount = 0;
        lastNearbyQueryDropIdCount = 0;
        if (drops.isEmpty()) {
            return nearbyDropScratch;
        }
        int minX = bucketIndex(x - radius);
        int maxX = bucketIndex(x + radius);
        int minY = bucketIndex(y - radius);
        int maxY = bucketIndex(y + radius);
        int minZ = bucketIndex(z - radius);
        int maxZ = bucketIndex(z + radius);
        long queriedBucketVolume = (long) (maxX - minX + 1)
                * (long) (maxY - minY + 1)
                * (long) (maxZ - minZ + 1);
        if (queriedBucketVolume > spatialBuckets.size()) {
            addOccupiedNearbyDropIds(minX, maxX, minY, maxY, minZ, maxZ);
            lastNearbyQueryDropIdCount = nearbyDropScratch.size();
            return nearbyDropScratch;
        }
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    lastNearbyQueryBucketCount++;
                    LinkedHashSet<String> bucket = spatialBuckets.get(bucketKey(bx, by, bz));
                    if (bucket != null) {
                        nearbyDropScratch.addAll(bucket);
                    }
                }
            }
        }
        lastNearbyQueryDropIdCount = nearbyDropScratch.size();
        return nearbyDropScratch;
    }

    private void addOccupiedNearbyDropIds(
            int minX,
            int maxX,
            int minY,
            int maxY,
            int minZ,
            int maxZ
    ) {
        for (Map.Entry<Long, LinkedHashSet<String>> entry : spatialBuckets.entrySet()) {
            lastNearbyQueryBucketCount++;
            long bucketKey = entry.getKey();
            int bx = unpackBucketCoordinate(bucketKey >> BUCKET_XZ_SHIFT, BUCKET_XZ_BITS);
            if (bx < minX || bx > maxX) {
                continue;
            }
            int by = unpackBucketCoordinate(bucketKey >> BUCKET_Y_SHIFT, BUCKET_Y_BITS);
            if (by < minY || by > maxY) {
                continue;
            }
            int bz = unpackBucketCoordinate(bucketKey, BUCKET_XZ_BITS);
            if (bz < minZ || bz > maxZ) {
                continue;
            }
            nearbyDropScratch.addAll(entry.getValue());
        }
    }

    private void removeFromBucket(String dropId, long bucketKey) {
        LinkedHashSet<String> bucket = spatialBuckets.get(bucketKey);
        if (bucket == null) {
            return;
        }
        bucket.remove(dropId);
        if (bucket.isEmpty()) {
            spatialBuckets.remove(bucketKey);
        }
    }

    private static long bucketKey(double x, double y, double z) {
        return bucketKey(bucketIndex(x), bucketIndex(y), bucketIndex(z));
    }

    private static long bucketKey(int x, int y, int z) {
        return (packBucketCoordinate(x, BUCKET_XZ_BITS, "x") << BUCKET_XZ_SHIFT)
                | (packBucketCoordinate(y, BUCKET_Y_BITS, "y") << BUCKET_Y_SHIFT)
                | packBucketCoordinate(z, BUCKET_XZ_BITS, "z");
    }

    private static long packBucketCoordinate(int value, int bits, String axis) {
        int min = -(1 << (bits - 1));
        int max = (1 << (bits - 1)) - 1;
        if (value < min || value > max) {
            throw new IllegalArgumentException("dropped item bucket " + axis + " out of range: " + value);
        }
        return value & ((1L << bits) - 1L);
    }

    private static int unpackBucketCoordinate(long packed, int bits) {
        int value = (int) (packed & ((1L << bits) - 1L));
        int signBit = 1 << (bits - 1);
        if ((value & signBit) != 0) {
            value -= 1 << bits;
        }
        return value;
    }

    private static int bucketIndex(double value) {
        return (int) Math.floor(value / SPATIAL_BUCKET_SIZE);
    }

    private static double groundSurfaceYBelow(
            WorldBlockLookup world,
            TrackedDrop tracked,
            EchoClientDroppedItem current,
            boolean forceProbe
    ) {
        if (current == null || tracked == null) {
            return Double.NaN;
        }
        int blockX = (int) Math.floor(current.x());
        int blockZ = (int) Math.floor(current.z());
        double cached = tracked.cachedGroundSurfaceY(blockX, blockZ);
        if (!forceProbe && Double.isFinite(cached) && current.y() >= cached - SETTLE_EPSILON) {
            return cached;
        }
        double groundY = groundSurfaceYBelow(world, current.x(), current.y(), current.z());
        if (Double.isFinite(groundY)) {
            tracked.cacheGroundSurface(blockX, blockZ, groundY);
        } else if (Double.isFinite(cached)) {
            tracked.clearGroundSurfaceCache();
        }
        return groundY;
    }

    private static double groundSurfaceYBelow(WorldBlockLookup world, double x, double y, double z) {
        if (world == null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z)) {
            return Double.NaN;
        }
        int blockX = (int) Math.floor(x);
        int blockZ = (int) Math.floor(z);
        int startY = Math.min(world.chunkSize() - 1, (int) Math.floor(y - GROUND_CLEARANCE));
        int stopY = Math.max(0, startY - GROUND_PROBE_BLOCKS);
        for (int blockY = startY; blockY >= stopY; blockY--) {
            if (!world.blockStateAt(blockX, blockY, blockZ).air()) {
                return blockY + 1.0D + GROUND_CLEARANCE;
            }
        }
        return Double.NaN;
    }

    private static boolean blockColumnChanged(EchoClientDroppedItem previous, EchoClientDroppedItem next) {
        if (previous == null || next == null) {
            return true;
        }
        return (int) Math.floor(previous.x()) != (int) Math.floor(next.x())
                || (int) Math.floor(previous.z()) != (int) Math.floor(next.z());
    }

    private static long sequenceAfter(String dropId) {
        if (dropId == null || !dropId.startsWith("drop-")) {
            return 1L;
        }
        try {
            return Long.parseLong(dropId.substring("drop-".length())) + 1L;
        } catch (NumberFormatException ignored) {
            return 1L;
        }
    }

    @FunctionalInterface
    interface PickupSink {
        EchoInventoryOperationResult collect(EchoItemStack stack);
    }

    record PickupResult(
            int pickedQuantity,
            int remainingDrops,
            String reason
    ) {
    }

    private record MergeResult(
            int remainingQuantity,
            EchoClientDroppedItem changedDrop
    ) {
    }

    private static final class WorldBlockLookup {
        private final EchoVoxelWorld world;
        private final int chunkSize;
        private final int loadedChunkCount;
        private final Map<EchoVoxelChunkId, EchoVoxelChunk> chunksById;
        private int blockLookups;

        private WorldBlockLookup(EchoVoxelWorld world) {
            this.world = world;
            chunkSize = world.chunkSize();
            loadedChunkCount = world.chunks().size();
            chunksById = new HashMap<>(Math.max(1, world.chunks().size() * 2));
            for (EchoVoxelChunk chunk : world.chunks()) {
                if (chunk != null) {
                    chunksById.putIfAbsent(chunk.id(), chunk);
                }
            }
        }

        private boolean matches(EchoVoxelWorld world) {
            return this.world == world
                    && world != null
                    && chunkSize == world.chunkSize()
                    && loadedChunkCount == world.chunks().size();
        }

        private int chunkSize() {
            return chunkSize;
        }

        private int blockLookups() {
            return blockLookups;
        }

        private void resetBlockLookups() {
            blockLookups = 0;
        }

        private EchoVoxelBlockState blockStateAt(int x, int y, int z) {
            blockLookups++;
            if (y < 0) {
                return EchoVoxelBlockState.AIR;
            }
            EchoVoxelChunk chunk = chunksById.get(EchoVoxelChunkId.fromBlock(x, y, z, chunkSize));
            if (chunk == null) {
                return EchoVoxelBlockState.AIR;
            }
            return chunk.stateAtLocal(
                    Math.floorMod(x, chunkSize),
                    Math.floorMod(y, chunkSize),
                    Math.floorMod(z, chunkSize)
            );
        }
    }

    private static final class TrackedDrop {
        private EchoClientDroppedItem drop;
        private double updatedAtSeconds;
        private long bucketKey;
        private double velocityY;
        private boolean settled;
        private double nextSettledProbeAtSeconds;
        private int cachedGroundBlockX = Integer.MIN_VALUE;
        private int cachedGroundBlockZ = Integer.MIN_VALUE;
        private double cachedGroundSurfaceY = Double.NaN;

        private TrackedDrop(EchoClientDroppedItem drop, double updatedAtSeconds, long bucketKey) {
            this.drop = drop;
            this.updatedAtSeconds = updatedAtSeconds;
            this.bucketKey = bucketKey;
        }

        private EchoClientDroppedItem drop() {
            return drop;
        }

        private int quantity() {
            return drop.quantity();
        }

        private long bucketKey() {
            return bucketKey;
        }

        private void setBucketKey(long bucketKey) {
            this.bucketKey = bucketKey;
        }

        private double velocityY() {
            return velocityY;
        }

        private void setVelocityY(double velocityY) {
            this.velocityY = velocityY;
        }

        private boolean settled() {
            return settled;
        }

        private double nextSettledProbeAtSeconds() {
            return nextSettledProbeAtSeconds;
        }

        private void markSettled(double clockSeconds) {
            settled = true;
            velocityY = 0.0D;
            nextSettledProbeAtSeconds = clockSeconds + SETTLED_RECHECK_SECONDS + settledProbeJitterSeconds();
        }

        private void deferSettledProbe(double clockSeconds) {
            nextSettledProbeAtSeconds = clockSeconds + SETTLED_RECHECK_DEFER_SECONDS;
        }

        private void markUnsettled() {
            settled = false;
            nextSettledProbeAtSeconds = 0.0D;
        }

        private double cachedGroundSurfaceY(int blockX, int blockZ) {
            if (cachedGroundBlockX == blockX && cachedGroundBlockZ == blockZ) {
                return cachedGroundSurfaceY;
            }
            return Double.NaN;
        }

        private void cacheGroundSurface(int blockX, int blockZ, double groundSurfaceY) {
            cachedGroundBlockX = blockX;
            cachedGroundBlockZ = blockZ;
            cachedGroundSurfaceY = groundSurfaceY;
        }

        private void clearGroundSurfaceCache() {
            cachedGroundBlockX = Integer.MIN_VALUE;
            cachedGroundBlockZ = Integer.MIN_VALUE;
            cachedGroundSurfaceY = Double.NaN;
        }

        private double ageSeconds(double clockSeconds) {
            return drop.ageSeconds() + Math.max(0.0D, clockSeconds - updatedAtSeconds);
        }

        private EchoClientDroppedItem current(double clockSeconds) {
            return drop.withAge(ageSeconds(clockSeconds));
        }

        private void update(EchoClientDroppedItem drop, double clockSeconds) {
            this.drop = drop;
            this.updatedAtSeconds = clockSeconds;
        }

        private double settledProbeJitterSeconds() {
            String dropId = drop == null ? "" : drop.dropId();
            return Math.floorMod(dropId.hashCode(), 1000) / 1000.0D * SETTLED_RECHECK_JITTER_SECONDS;
        }
    }
}
