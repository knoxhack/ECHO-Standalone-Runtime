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
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoClientPlayerProjectileCombatSmokeHarness {
    private static final String TEST_ENTITY_ID = "test:projectile_target";
    private static final String TEST_DEFINITION_ID = "echoashfallprotocol:rad_zombie";
    private static final String PROJECTILE_ID = "echo:test_broadhead_arrow";
    private static final String SCRAP_METAL_ID = "echoashfallprotocol:scrap_metal";

    private EchoClientPlayerProjectileCombatSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("player-projectile-combat").gameSession();
        EchoWorldPosition targetPosition = projectileTargetPosition(session);
        orientForTarget(session, targetPosition);
        session.entityStore().register(hostile(TEST_ENTITY_ID, targetPosition));
        installOffhandProjectiles(session);

        int beforeXp = session.progressionState().experience();
        int beforeDrops = session.droppedItemQuantity();
        EchoClientEntityAttackResult melee = session.attackLookedAtEntity(null);
        require(!melee.hit() && melee.reason().equals("no_target"),
                "Projectile smoke target should sit beyond melee reach before projectile fire");

        EchoClientProjectileResult projectile = session.fireOffhandProjectileAtLookedAtEntity(null);

        require(projectile.fired() && projectile.hit() && projectile.killed(),
                "Offhand projectile should fire, hit, and kill the looked-at hostile");
        require(projectile.consumedProjectile()
                        && projectile.projectileCountBefore() == 2
                        && projectile.projectileCountAfter() == 1,
                "Projectile fire should consume exactly one offhand ammo item");
        require(projectile.damageSourceId().equals("echo:projectile/" + PROJECTILE_ID + "/player"),
                "Projectile fire should record a typed projectile damage source");
        require(projectile.attack().damage() == 7
                        && projectile.attack().healthBefore() == 7
                        && projectile.attack().healthAfter() == 0,
                "Tagged combat projectile should apply lethal projectile damage");
        require(session.entityStore().find(new EchoEntityId(TEST_ENTITY_ID)).isEmpty(),
                "Killed projectile target should be removed from the live entity store");
        require(session.droppedItemQuantity() == beforeDrops + 2
                        && session.droppedItemSnapshots().stream()
                                .anyMatch(drop -> drop.itemId().equals(SCRAP_METAL_ID) && drop.quantity() == 2),
                "Projectile kill should create the same save-backed hostile death loot as melee combat");
        require(session.progressionState().experience() == beforeXp + 10
                        && session.progressionState().milestones().contains("kill:" + TEST_DEFINITION_ID),
                "Projectile kill should award hostile XP and kill milestone progress");

        writeReport(projectile);
        System.out.println("client player projectile combat smoke PASS projectile=hit ammo=1 xp=awarded");
    }

    private static void installOffhandProjectiles(EchoClientGameSession session) {
        session.playerInventory().slot(1).clear();
        session.playerInventory().slot(1).setStack(new EchoItemStack(projectileDefinition(), 2));
        session.hotbar().select(1);
        session.player().selectSlot(1);
        require(session.swapSelectedWithOffhand(),
                "Projectile smoke should move tagged projectile ammo into the offhand slot");
        require(session.playerCombatState().equipment().offhand().orElseThrow().itemId().value().equals(PROJECTILE_ID),
                "Offhand slot should expose the tagged projectile ammo");
    }

    private static EchoItemDefinition projectileDefinition() {
        return new EchoItemDefinition(
                new EchoItemId(PROJECTILE_ID),
                "Broadhead Arrow",
                EchoItemCategory.MATERIAL,
                16,
                1.0D,
                List.of("projectile", "ammo", "arrow", "combat"),
                List.of("Deterministic projectile smoke ammo")
        );
    }

    private static void orientForTarget(EchoClientGameSession session, EchoWorldPosition target) {
        EchoVoxelPlayerState state = session.player().state();
        double dx = target.x() + 0.5D - state.x();
        double dy = target.y() + 0.86D - state.eyeY();
        double dz = target.z() + 0.5D - state.z();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double targetYaw = Math.toDegrees(Math.atan2(dx, dz));
        double targetPitch = Math.toDegrees(Math.asin(dy / distance));
        session.player().tick(
                session.world(),
                EchoVoxelPlayerInput.look(targetYaw - state.yawDegrees(), targetPitch - state.pitchDegrees()),
                0.0D
        );
    }

    private static EchoWorldPosition projectileTargetPosition(EchoClientGameSession session) {
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        for (int distance = 6; distance <= 8; distance++) {
            int z = (int) Math.floor(player.z() + distance);
            int y = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
            if (y >= 0 && session.world().blockStateAt(x, y, z).air()) {
                return new EchoWorldPosition(x, y, z);
            }
        }
        throw new AssertionError("Could not find deterministic projectile target position");
    }

    private static EchoEntityState hostile(String entityId, EchoWorldPosition position) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                TEST_DEFINITION_ID,
                "Rad Zombie",
                EchoEntityKind.HOSTILE,
                7,
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

    private static void writeReport(EchoClientProjectileResult projectile) throws IOException {
        Path report = Path.of("reports", "echo", "standalone", "client-player-projectile-combat.json");
        Files.createDirectories(report.getParent());
        String json = "{\n"
                + "  \"schema\": \"echo.standalone.client_player_projectile_combat.v1\",\n"
                + "  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n"
                + "  \"generator\": \"EchoClientPlayerProjectileCombatSmokeHarness\",\n"
                + "  \"status\": \"PASS\",\n"
                + "  \"projectileItemId\": \"" + PROJECTILE_ID + "\",\n"
                + "  \"damageSourceId\": \"" + projectile.damageSourceId() + "\",\n"
                + "  \"fired\": " + projectile.fired() + ",\n"
                + "  \"hit\": " + projectile.hit() + ",\n"
                + "  \"killed\": " + projectile.killed() + ",\n"
                + "  \"projectileCountBefore\": " + projectile.projectileCountBefore() + ",\n"
                + "  \"projectileCountAfter\": " + projectile.projectileCountAfter() + ",\n"
                + "  \"damage\": " + projectile.attack().damage() + ",\n"
                + "  \"healthBefore\": " + projectile.attack().healthBefore() + ",\n"
                + "  \"healthAfter\": " + projectile.attack().healthAfter() + ",\n"
                + "  \"lootItemId\": \"" + SCRAP_METAL_ID + "\",\n"
                + "  \"lootQuantity\": 2,\n"
                + "  \"experienceAwarded\": 10,\n"
                + "  \"beyondMeleeReach\": true,\n"
                + "  \"offhandAmmoConsumed\": " + projectile.consumedProjectile() + "\n"
                + "}\n";
        Files.writeString(report, json, StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
