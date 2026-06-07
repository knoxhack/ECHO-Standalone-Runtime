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
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

public final class EchoClientEntityAiSmokeHarness {
    private EchoClientEntityAiSmokeHarness() {
    }

    public static void main(String[] args) {
        requirePursuitMovement();
        requireAttackStateAndDebugLine();
        System.out.println("client entity ai smoke PASS pursuit=1 attack=1");
    }

    private static void requirePursuitMovement() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoWorldPosition start = chasePosition(session.world(), session.player().state());
        session.entityStore().register(entity("test:chaser", "echoashfallprotocol:rad_zombie", start));
        double before = distanceSquared(start, session.player().state());

        session.tickEntities(0.5D);

        EchoEntityState moved = session.entityStore().require(new EchoEntityId("test:chaser"));
        double after = distanceSquared(moved.worldPosition(), session.player().state());
        EchoClientEntityAiSummary ai = session.entityAiSummary();
        require(after < before, "Hostile AI should move closer to the player");
        require(ai.movements() > 0 && ai.pursuing() > 0,
                "Hostile AI summary should report pursuit movement");
        require(moved.ai().state() == EchoEntityAiState.PURSUING,
                "Moved hostile should enter PURSUING state");
        require(session.world().blockStateAt(
                moved.worldPosition().x(),
                moved.worldPosition().y(),
                moved.worldPosition().z()
        ).air(), "AI movement should never place an entity inside a solid block");
        require(!session.world().blockStateAt(
                moved.worldPosition().x(),
                moved.worldPosition().y() - 1,
                moved.worldPosition().z()
        ).air(), "AI movement should keep the entity standing on terrain");
    }

    private static void requireAttackStateAndDebugLine() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        EchoWorldPosition attack = attackPosition(session.world(), session.player().state());
        session.entityStore().register(entity("test:attacker", "echoashfallprotocol:rad_zombie", attack));

        session.tickEntities(0.5D);

        EchoEntityState attacker = session.entityStore().require(new EchoEntityId("test:attacker"));
        EchoClientEntityAiSummary ai = session.entityAiSummary();
        require(attacker.ai().state() == EchoEntityAiState.ATTACKING,
                "Adjacent hostile should enter ATTACKING state");
        require(ai.attacks() > 0 && ai.attacking() > 0 && ai.totalAttacks() > 0,
                "Hostile AI summary should report attack ticks");

        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("AI I "),
                "Debug overlay should include live hostile AI state counts");
        require(debug.contains(" HIT " + ai.totalAttacks()),
                "Debug overlay should include total hostile attack ticks");
        require(!debug.contains(","),
                "AI debug line should preserve the HUD font punctuation contract");
    }

    private static EchoWorldPosition chasePosition(EchoVoxelWorld world, EchoVoxelPlayerState player) {
        int playerX = (int) Math.floor(player.x());
        int playerZ = (int) Math.floor(player.z());
        for (int distance = 5; distance <= 10; distance++) {
            for (int dz = -2; dz <= 2; dz++) {
                int x = playerX + distance;
                int z = playerZ + dz;
                int y = EchoClientEntityAi.surfaceSpawnY(world, x, z);
                int nextY = EchoClientEntityAi.surfaceSpawnY(world, x - 1, z);
                if (y >= 0 && nextY >= 0 && Math.abs(nextY - y) <= 1) {
                    return new EchoWorldPosition(x, y, z);
                }
            }
        }
        throw new AssertionError("Could not find a valid deterministic chase position");
    }

    private static EchoWorldPosition attackPosition(EchoVoxelWorld world, EchoVoxelPlayerState player) {
        int playerX = (int) Math.floor(player.x());
        int playerZ = (int) Math.floor(player.z());
        int[][] offsets = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] offset : offsets) {
            int x = playerX + offset[0];
            int z = playerZ + offset[1];
            int y = EchoClientEntityAi.surfaceSpawnY(world, x, z);
            if (y >= 0) {
                EchoWorldPosition position = new EchoWorldPosition(x, y, z);
                if (distanceSquared(position, player) <= 1.35D * 1.35D) {
                    return position;
                }
            }
        }
        throw new AssertionError("Could not find a valid deterministic attack position");
    }

    private static EchoEntityState entity(String entityId, String definitionId, EchoWorldPosition position) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                definitionId,
                definitionId.substring(definitionId.indexOf(':') + 1),
                EchoEntityKind.HOSTILE,
                20,
                1,
                "hostile_scavenger"
        );
        return new EchoEntityState(
                new EchoEntityId(entityId),
                definition,
                new EchoEntityPositionComponent(position),
                new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
        );
    }

    private static double distanceSquared(EchoWorldPosition position, EchoVoxelPlayerState player) {
        double dx = position.x() + 0.5D - player.x();
        double dz = position.z() + 0.5D - player.z();
        return dx * dx + dz * dz;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
