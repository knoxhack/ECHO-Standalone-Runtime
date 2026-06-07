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
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class EchoClientEntitySaveSmokeHarness {
    private EchoClientEntitySaveSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        requireEntityDiskRestore();
        System.out.println("client entity save smoke PASS restored=1");
    }

    private static void requireEntityDiskRestore() throws IOException {
        Path fixtureRoot = Path.of("build", "tmp", "client-entity-save-smoke").toAbsolutePath();
        deleteRecursively(fixtureRoot);
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.client_entity_profile.v1",
                "client-entity-smoke",
                "Client Entity Smoke",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/client-entity"),
                Map.of("surface", "echoscreencore:hud")
        );
        EchoSaveRuntimeResult saves = new EchoSaveRuntime().open(new EchoDefaultRuntimeServiceRegistry(), profile);
        EchoClientWorldSession worldSession =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("entity-save-smoke");
        EchoClientGameSession session = worldSession.gameSession();
        EchoEntityState savedEntity = entity(
                "test:persistent_entity",
                "echoruntimehost:persistent_watcher",
                "Persistent Watcher",
                new EchoWorldPosition(7, 6, -5)
        );
        session.entityStore().register(savedEntity);
        require(session.savedSessionSnapshot().entities().size() == 1,
                "In-memory session snapshot should include live entity state");

        EchoClientGameplaySaveCodec.writeSession(saves, worldSession, "tx-entity-save", "entity-save-smoke");
        EchoSaveManifest manifest = saves.readManifest(worldSession.slotId());
        require(manifest.file(EchoClientGameplaySaveCodec.ENTITIES_PATH).isPresent(),
                "Client save manifest should include entity state");
        require(manifest.metadata().getOrDefault("clientEntitiesCodec", "").equals("echo.client.entities.v1"),
                "Client save manifest should advertise the entity codec");

        Path entitiesPath = fixtureRoot.resolve("profiles/client-entity/slots")
                .resolve(worldSession.slotId())
                .resolve("data")
                .resolve(EchoClientGameplaySaveCodec.ENTITIES_PATH);
        String entityText = Files.readString(entitiesPath);
        require(entityText.contains("test:persistent_entity")
                        && entityText.contains("echoruntimehost:persistent_watcher")
                        && entityText.contains("PURSUING"),
                "Entity sidecar should include entity id definition id and AI state");

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
        EchoEntityState restoredEntity =
                restored.gameSession().entityStore().require(new EchoEntityId("test:persistent_entity"));
        require(restored.gameSession().entityStore().count() == 1,
                "Disk restore should preserve one active client entity");
        require(restoredEntity.definition().definitionId().equals("echoruntimehost:persistent_watcher"),
                "Disk restore should preserve entity definition id");
        require(restoredEntity.definition().displayName().equals("Persistent Watcher"),
                "Disk restore should preserve entity display name");
        require(restoredEntity.worldPosition().equals(new EchoWorldPosition(7, 6, -5)),
                "Disk restore should preserve entity world position");
        require(restoredEntity.health().currentHealth() == 7 && restoredEntity.health().maxHealth() == 20,
                "Disk restore should preserve entity health component");
        require(restoredEntity.movement().movementSpeed() == 2 && restoredEntity.movement().blockedByWorld(),
                "Disk restore should preserve entity movement component");
        require(restoredEntity.ai().profile().equals("hostile_scavenger")
                        && restoredEntity.ai().state() == EchoEntityAiState.PURSUING,
                "Disk restore should preserve entity AI component");
    }

    private static EchoEntityState entity(
            String entityId,
            String definitionId,
            String displayName,
            EchoWorldPosition position
    ) {
        EchoEntityDefinition definition = new EchoEntityDefinition(
                definitionId,
                displayName,
                EchoEntityKind.HOSTILE,
                20,
                2,
                "hostile_scavenger"
        );
        return new EchoEntityState(
                new EchoEntityId(entityId),
                definition,
                new EchoEntityPositionComponent(position),
                new EchoEntityHealthComponent(7, definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.PURSUING)
        );
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
