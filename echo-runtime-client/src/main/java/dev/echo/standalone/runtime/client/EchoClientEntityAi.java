package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.entity.EchoEntityStore;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.List;
import java.util.Objects;

final class EchoClientEntityAi {
    private static final double AI_INTERVAL_SECONDS = 0.35D;
    private static final double CHASE_RANGE_BLOCKS = 48.0D;
    private static final double ATTACK_RANGE_BLOCKS = 1.35D;

    private double aiTimerSeconds;
    private long totalMovements;
    private long totalAttacks;
    private EchoClientEntityAiSummary lastSummary = EchoClientEntityAiSummary.EMPTY;

    EchoClientEntityAiSummary tick(
            EchoEntityStore store,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            double deltaSeconds
    ) {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(player, "player");
        if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0.0D) {
            return summarize(store, 0, 0, 0, "idle");
        }

        aiTimerSeconds += deltaSeconds;
        if (aiTimerSeconds < AI_INTERVAL_SECONDS) {
            return summarize(store, 0, 0, 0, "waiting");
        }
        int steps = Math.max(1, Math.min(3, (int) Math.floor(aiTimerSeconds / AI_INTERVAL_SECONDS)));
        aiTimerSeconds = Math.max(0.0D, aiTimerSeconds - steps * AI_INTERVAL_SECONDS);

        int movements = 0;
        int attacks = 0;
        int blocked = 0;
        for (int step = 0; step < steps; step++) {
            TickCounts counts = tickStep(store, world, player);
            movements += counts.movements();
            attacks += counts.attacks();
            blocked += counts.blocked();
        }
        totalMovements += movements;
        totalAttacks += attacks;

        String reason = attacks > 0 ? "attacked" : movements > 0 ? "moved" : blocked > 0 ? "blocked" : "idle";
        return summarize(store, movements, attacks, blocked, reason);
    }

    EchoClientEntityAiSummary lastSummary() {
        return lastSummary;
    }

    private TickCounts tickStep(EchoEntityStore store, EchoVoxelWorld world, EchoVoxelPlayerState player) {
        int movements = 0;
        int attacks = 0;
        int blocked = 0;
        List<EchoEntityState> hostiles = store.hostile();
        for (EchoEntityState hostile : hostiles) {
            if (!hostile.alive()) {
                continue;
            }
            EchoWorldPosition from = hostile.worldPosition();
            double centerX = from.x() + 0.5D;
            double centerZ = from.z() + 0.5D;
            double dx = player.x() - centerX;
            double dz = player.z() - centerZ;
            double distanceSquared = dx * dx + dz * dz;
            if (distanceSquared > CHASE_RANGE_BLOCKS * CHASE_RANGE_BLOCKS) {
                updateAi(store, hostile, EchoEntityAiState.IDLE);
                continue;
            }
            if (distanceSquared <= ATTACK_RANGE_BLOCKS * ATTACK_RANGE_BLOCKS) {
                updateAi(store, hostile, EchoEntityAiState.ATTACKING);
                attacks++;
                continue;
            }

            EchoWorldPosition next = nextStep(store, world, player, hostile);
            if (next == from) {
                updateAi(store, hostile, EchoEntityAiState.PURSUING);
                blocked++;
            } else {
                store.update(hostile.withPosition(next).withAi(hostile.ai().withState(EchoEntityAiState.PURSUING)));
                movements++;
            }
        }
        return new TickCounts(movements, attacks, blocked);
    }

    private static EchoWorldPosition nextStep(
            EchoEntityStore store,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            EchoEntityState hostile
    ) {
        EchoWorldPosition from = hostile.worldPosition();
        int playerX = (int) Math.floor(player.x());
        int playerZ = (int) Math.floor(player.z());
        int deltaX = Integer.compare(playerX, from.x());
        int deltaZ = Integer.compare(playerZ, from.z());

        int[][] candidates = Math.abs(playerX - from.x()) >= Math.abs(playerZ - from.z())
                ? new int[][] {{deltaX, deltaZ}, {deltaX, 0}, {0, deltaZ}, {deltaX, -deltaZ}, {-deltaX, deltaZ}}
                : new int[][] {{deltaX, deltaZ}, {0, deltaZ}, {deltaX, 0}, {-deltaX, deltaZ}, {deltaX, -deltaZ}};

        for (int[] candidate : candidates) {
            if (candidate[0] == 0 && candidate[1] == 0) {
                continue;
            }
            EchoWorldPosition next = candidatePosition(world, from, candidate[0], candidate[1]);
            if (next != from && !occupied(store, hostile, next)) {
                return next;
            }
        }
        return from;
    }

    private static EchoWorldPosition candidatePosition(EchoVoxelWorld world, EchoWorldPosition from, int dx, int dz) {
        int x = from.x() + dx;
        int z = from.z() + dz;
        if (!world.hasChunk(EchoVoxelChunkId.fromBlock(x, 0, z, world.chunkSize()))) {
            return from;
        }
        int y = surfaceSpawnY(world, x, z);
        if (y < 0 || Math.abs(y - from.y()) > 1) {
            return from;
        }
        return new EchoWorldPosition(x, y, z);
    }

    static int surfaceSpawnY(EchoVoxelWorld world, int x, int z) {
        for (int y = world.chunkSize() - 2; y >= 0; y--) {
            if (!world.blockStateAt(x, y, z).air()
                    && world.blockStateAt(x, y + 1, z).air()
                    && world.blockStateAt(x, y + 2, z).air()) {
                return y + 1;
            }
        }
        return -1;
    }

    private static boolean occupied(EchoEntityStore store, EchoEntityState moving, EchoWorldPosition next) {
        for (EchoEntityState entity : store.living()) {
            if (entity.id().equals(moving.id())) {
                continue;
            }
            EchoWorldPosition position = entity.worldPosition();
            if (position.x() == next.x() && position.y() == next.y() && position.z() == next.z()) {
                return true;
            }
        }
        return false;
    }

    private static void updateAi(EchoEntityStore store, EchoEntityState entity, EchoEntityAiState state) {
        if (entity.ai().state() == state) {
            return;
        }
        store.update(entity.withAi(entity.ai().withState(state)));
    }

    private EchoClientEntityAiSummary summarize(
            EchoEntityStore store,
            int movements,
            int attacks,
            int blocked,
            String reason
    ) {
        int idle = 0;
        int pursuing = 0;
        int attacking = 0;
        for (EchoEntityState entity : store.hostile()) {
            if (!entity.alive()) {
                continue;
            }
            if (entity.ai().state() == EchoEntityAiState.ATTACKING) {
                attacking++;
            } else if (entity.ai().state() == EchoEntityAiState.PURSUING) {
                pursuing++;
            } else {
                idle++;
            }
        }
        lastSummary = new EchoClientEntityAiSummary(
                idle,
                pursuing,
                attacking,
                movements,
                attacks,
                blocked,
                totalMovements,
                totalAttacks,
                reason
        );
        return lastSummary;
    }

    private record TickCounts(int movements, int attacks, int blocked) {
    }
}
