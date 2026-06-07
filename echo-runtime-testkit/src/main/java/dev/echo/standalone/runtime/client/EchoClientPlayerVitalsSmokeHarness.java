package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
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
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public final class EchoClientPlayerVitalsSmokeHarness {
    private EchoClientPlayerVitalsSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireVitalsModelAndHud();
        requireHostileAttackDamageAndDebug();
        requireSnapshotAndDiskRestore();
        System.out.println("client player vitals smoke PASS health=restored hearts=10");
    }

    private static void requireVitalsModelAndHud() {
        EchoClientPlayerVitals vitals = EchoClientPlayerVitals.full().damage(5);
        require(vitals.currentHealth() == 15 && vitals.maxHealth() == 20 && vitals.lastDamage() == 5,
                "Player vitals should clamp and remember the last damage amount");
        require(vitals.heal(3).currentHealth() == 18,
                "Player vitals should heal up to max health");
        int[] hearts = EchoClientHud.heartFillStates(vitals);
        require(hearts.length == 10,
                "Default player vitals should render ten heart slots");
        require(hearts[0] == 2 && hearts[6] == 2 && hearts[7] == 1 && hearts[8] == 0 && hearts[9] == 0,
                "Heart fill states should represent full half and empty hearts");
    }

    private static void requireHostileAttackDamageAndDebug() {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        EchoClientGameplay gameplay = new EchoClientGameplay();
        gameplay.init(session.world(), session.player(), session.hotbar());
        session.entityStore().register(entity("test:attacker", attackPosition(session.world(), session.player().state())));

        session.tickEntities(0.5D);

        require(session.playerVitals().currentHealth() == 18,
                "Adjacent hostile attack should damage player vitals");
        require(session.playerVitals().lastDamage() == 2,
                "Hostile attack damage should be exposed in player vitals");
        String debug = EchoClientDebugOverlay.text(
                60,
                EchoClientGameState.IN_GAME,
                EchoClientScreenKind.MAIN_MENU,
                session,
                gameplay
        );
        require(debug.contains("HEALTH 18 OF 20 DMG 2"),
                "Debug overlay should show current player health and damage");
        require(!debug.contains(","),
                "Health debug line should preserve the HUD font punctuation contract");
    }

    private static void requireSnapshotAndDiskRestore() throws IOException {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        session.damagePlayer(7);
        EchoClientWorldSession memoryRestored = EchoClientWorldSessionFactory.defaultFactory()
                .restoreSavedSession(session.savedSessionSnapshot());
        require(memoryRestored.gameSession().playerVitals().currentHealth() == 13,
                "In-memory saved session snapshots should preserve player vitals");

        Path fixtureRoot = Path.of("build", "tmp", "client-vitals-save-smoke").toAbsolutePath();
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_vitals_profile.v1",
                "client-vitals-smoke",
                "Client Vitals Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-vitals"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession = EchoClientWorldSessionFactory.defaultFactory().newWorld("vitals-save-smoke");
        worldSession.gameSession().damagePlayer(6);

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-vitals-save", "vitals-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.VITALS_PATH).isPresent(),
                "Client save manifest should include player vitals file");
        require(manifest.metadata().getOrDefault("clientVitalsCodec", "").equals("echo.client.vitals.v2"),
                "Client save manifest should advertise the player vitals codec");

        EchoClientSavedSessionSnapshot restoredSnapshot = EchoClientGameplaySaveCodec.restoreSessionSnapshot(
                EchoAdapterCoreStandaloneContentBridge.ashfallLive(),
                saves,
                manifest
        );
        EchoClientWorldSession restored = EchoClientWorldSession.fromSavedSession(
                manifest.slotId(),
                manifest.metadata().getOrDefault("displayName", manifest.slotId()),
                restoredSnapshot
        );
        require(restored.gameSession().playerVitals().currentHealth() == 14,
                "Disk save restore should preserve player vitals");
        require(restored.gameSession().playerVitals().lastDamage() == 6,
                "Disk save restore should preserve last damage amount");
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

    private static EchoEntityState entity(String entityId, EchoWorldPosition position) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                "echoashfallprotocol:rad_zombie",
                "Rad Zombie",
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
