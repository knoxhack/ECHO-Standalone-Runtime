package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityAiComponent;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityHealthComponent;
import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityMovementComponent;
import dev.echo.standalone.runtime.entity.EchoEntityPositionComponent;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.entity.EchoEntityStore;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Objects;

final class EchoClientEntitySpawner {
    private static final double SPAWN_INTERVAL_SECONDS = 1.0D;
    private static final double MIN_SPAWN_DISTANCE = 12.0D;
    private static final double MAX_SPAWN_DISTANCE = 34.0D;
    private static final int CANDIDATES_PER_TICK = 16;

    private final EchoClientEntityCatalog entityCatalog;
    private double spawnTimerSeconds;
    private long attempts;
    private long spawned;
    private int nextEntityNumber;
    private EchoClientEntitySpawnSummary lastSummary = EchoClientEntitySpawnSummary.EMPTY;

    EchoClientEntitySpawner(EchoClientEntityCatalog entityCatalog) {
        this.entityCatalog = entityCatalog == null ? EchoClientEntityCatalog.empty() : entityCatalog;
    }

    EchoClientEntitySpawnSummary tick(
            EchoEntityStore store,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            double deltaSeconds
    ) {
        Objects.requireNonNull(store, "store");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(player, "player");
        if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0.0D) {
            return summarize(store, world, player, "idle");
        }
        spawnTimerSeconds += deltaSeconds;
        if (spawnTimerSeconds < SPAWN_INTERVAL_SECONDS) {
            return summarize(store, world, player, "waiting");
        }
        spawnTimerSeconds = Math.min(0.25D, spawnTimerSeconds - SPAWN_INTERVAL_SECONDS);

        int maxHostiles = maxHostiles(world);
        int currentHostiles = livingHostiles(store);
        if (currentHostiles >= maxHostiles) {
            return summarize(store, world, player, "cap_reached");
        }

        for (int i = 0; i < CANDIDATES_PER_TICK; i++) {
            attempts++;
            SpawnCandidate candidate = candidate(world, player, attempts);
            if (!loaded(world, candidate.x(), candidate.z())) {
                continue;
            }
            if (tooClose(player, candidate.x(), candidate.z())) {
                continue;
            }
            int y = surfaceSpawnY(world, candidate.x(), candidate.z());
            if (y < 0) {
                continue;
            }
            if (occupiedNear(store, candidate.x(), y, candidate.z())) {
                continue;
            }
            EchoVoxelBiome biome = world.biomeAt(candidate.x(), candidate.z());
            EchoEntityDefinition definition = entityCatalog.definitionForBiome(biome);
            store.register(new EchoEntityState(
                    new EchoEntityId("client:mob_" + (++nextEntityNumber)),
                    definition,
                    new EchoEntityPositionComponent(new EchoWorldPosition(candidate.x(), y, candidate.z())),
                    new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                    new EchoEntityMovementComponent(definition.movementSpeed(), true),
                    new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
            ));
            spawned++;
            lastSummary = new EchoClientEntitySpawnSummary(
                    biome.id(),
                    definition.definitionId(),
                    "spawned",
                    store.living().size(),
                    livingHostiles(store),
                    attempts,
                    spawned
            );
            return lastSummary;
        }

        return summarize(store, world, player, "no_surface");
    }

    EchoClientEntitySpawnSummary lastSummary() {
        return lastSummary;
    }

    private EchoClientEntitySpawnSummary summarize(
            EchoEntityStore store,
            EchoVoxelWorld world,
            EchoVoxelPlayerState player,
            String reason
    ) {
        String biomeId = world.biomeAt(player.x(), player.z()).id();
        lastSummary = new EchoClientEntitySpawnSummary(
                biomeId,
                entityCatalog.definitionForBiome(world.biomeAt(player.x(), player.z())).definitionId(),
                reason,
                store.living().size(),
                livingHostiles(store),
                attempts,
                spawned
        );
        return lastSummary;
    }

    private static int maxHostiles(EchoVoxelWorld world) {
        return Math.max(2, Math.min(10, world.loadedChunkCount() / 8));
    }

    private static int livingHostiles(EchoEntityStore store) {
        return (int) store.hostile().stream()
                .filter(EchoEntityState::alive)
                .count();
    }

    private static boolean loaded(EchoVoxelWorld world, int x, int z) {
        return world.hasChunk(EchoVoxelChunkId.fromBlock(x, 0, z, world.chunkSize()));
    }

    private static boolean tooClose(EchoVoxelPlayerState player, int x, int z) {
        double dx = x + 0.5D - player.x();
        double dz = z + 0.5D - player.z();
        return dx * dx + dz * dz < MIN_SPAWN_DISTANCE * MIN_SPAWN_DISTANCE;
    }

    private static boolean occupiedNear(EchoEntityStore store, int x, int y, int z) {
        for (EchoEntityState entity : store.living()) {
            EchoWorldPosition position = entity.worldPosition();
            int dx = position.x() - x;
            int dy = position.y() - y;
            int dz = position.z() - z;
            if (dx * dx + dy * dy + dz * dz <= 4) {
                return true;
            }
        }
        return false;
    }

    private static int surfaceSpawnY(EchoVoxelWorld world, int x, int z) {
        for (int y = world.chunkSize() - 2; y >= 0; y--) {
            if (!world.blockStateAt(x, y, z).air()
                    && world.blockStateAt(x, y + 1, z).air()
                    && world.blockStateAt(x, y + 2, z).air()) {
                return y + 1;
            }
        }
        return -1;
    }

    private static SpawnCandidate candidate(EchoVoxelWorld world, EchoVoxelPlayerState player, long attempt) {
        long mixed = mix(world.seed() ^ attempt);
        double angle = unit(mixed) * Math.PI * 2.0D;
        double distance = MIN_SPAWN_DISTANCE
                + unit(mix(mixed ^ 0x534F554E44434FL)) * (MAX_SPAWN_DISTANCE - MIN_SPAWN_DISTANCE);
        int x = (int) Math.floor(player.x() + Math.cos(angle) * distance);
        int z = (int) Math.floor(player.z() + Math.sin(angle) * distance);
        return new SpawnCandidate(x, z);
    }

    private static long mix(long value) {
        long mixed = value;
        mixed ^= mixed >>> 33;
        mixed *= 0xff51afd7ed558ccdL;
        mixed ^= mixed >>> 33;
        mixed *= 0xc4ceb9fe1a85ec53L;
        mixed ^= mixed >>> 33;
        return mixed;
    }

    private static double unit(long value) {
        return ((value >>> 11) & ((1L << 53) - 1)) * 0x1.0p-53;
    }

    private record SpawnCandidate(int x, int z) {
    }
}
