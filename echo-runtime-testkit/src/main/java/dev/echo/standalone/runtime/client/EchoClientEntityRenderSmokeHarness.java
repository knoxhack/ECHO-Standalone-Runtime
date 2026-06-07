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
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.List;

public final class EchoClientEntityRenderSmokeHarness {
    private EchoClientEntityRenderSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientGameSession session = EchoClientWorldSessionFactory.defaultFactory().newWorld("42").gameSession();
        session.tickEntities(1.1D);
        EchoClientEntityCatalog entityCatalog = session.entityCatalog();

        EchoClientEntityRenderer.MeshData humanoid =
                EchoClientEntityRenderer.meshData(session.entityStore().living(), entityCatalog);
        require(humanoid.vertexCount() == EchoClientEntityRenderer.HUMANOID_VERTEX_COUNT,
                "Crash-zone spawned humanoid should produce body and head cuboids");
        require(humanoid.indexCount() == EchoClientEntityRenderer.HUMANOID_INDEX_COUNT,
                "Crash-zone spawned humanoid should produce expected triangle indices");
        require(humanoid.vertices().length
                        == humanoid.vertexCount() * EchoClientEntityRenderer.VERTEX_STRIDE_FLOATS,
                "Entity vertex buffer should use the documented stride");

        EchoClientEntityRenderer.MeshData mixed = EchoClientEntityRenderer.meshData(List.of(
                entity("echoashfallprotocol:toxic_slime", 1, 3, 1),
                entity("echoashfallprotocol:echo_drone", 3, 4, 2)
        ), entityCatalog);
        require(mixed.vertexCount() == 96,
                "Slime plus drone should generate four visible cuboids");
        require(mixed.indexCount() == 144,
                "Slime plus drone should generate four cuboids worth of triangle indices");
        require(EchoClientEntityRenderer.argbForDefinition(
                "echoashfallprotocol:toxic_slime",
                entityCatalog
        ) == 0xFF5CD66E,
                "Toxic slime should have a readable green render tint");
        require(EchoClientEntityRenderer.argbForDefinition(
                "echoashfallprotocol:echo_drone",
                entityCatalog
        ) == 0xFF8F63E6,
                "ECHO drone should have a distinct anomaly render tint");
        require(EchoClientEntityRenderer.meshData(List.of(), entityCatalog).indexCount() == 0,
                "Empty entity lists should not emit render geometry");
        requireEntityRenderSelection(entityCatalog);

        System.out.println("client entity render smoke PASS humanoidVertices="
                + humanoid.vertexCount()
                + " mixedVertices=" + mixed.vertexCount());
    }

    private static void requireEntityRenderSelection(EchoClientEntityCatalog entityCatalog) {
        EchoClientEntityRuntime runtime = new EchoClientEntityRuntime(entityCatalog);
        EchoVoxelCamera camera = new EchoVoxelCamera(0.5D, 2.8D, 0.5D, 0.0D, 0.0D, 70.0D);
        int nearbyCount = EchoClientEntityRuntime.MAX_RENDERED_ENTITIES + 40;
        for (int index = 0; index < nearbyCount; index++) {
            runtime.store().register(entity(
                    "test:render_" + index,
                    "echoashfallprotocol:rad_zombie",
                    index % 16 - 8,
                    2,
                    index / 16 - 4
            ));
        }
        runtime.store().register(entity(
                "test:far_entity",
                "echoashfallprotocol:rad_zombie",
                128,
                2,
                128
        ));

        List<EchoEntityState> selected = runtime.renderEntitiesNear(
                camera,
                24.0D,
                EchoClientEntityRuntime.MAX_RENDERED_ENTITIES
        );
        require(!runtime.lastRenderCacheHit(),
                "First entity render selection should build the visible candidate list");
        require(runtime.lastRenderCandidateCount() == nearbyCount,
                "Entity render selection should count only nearby candidates");
        require(runtime.lastRenderReturnedCount() == EchoClientEntityRuntime.MAX_RENDERED_ENTITIES,
                "Entity render selection should cap rendered entities");
        require(selected.stream().noneMatch(entity -> entity.id().value().equals("test:far_entity")),
                "Entity render selection should exclude far entities");
        require(selected.size() == EchoClientEntityRuntime.MAX_RENDERED_ENTITIES,
                "Entity render selection should return the capped visible list");

        List<EchoEntityState> cached = runtime.renderEntitiesNear(
                camera,
                24.0D,
                EchoClientEntityRuntime.MAX_RENDERED_ENTITIES
        );
        require(runtime.lastRenderCacheHit() && cached == selected,
                "Repeated entity render selection should reuse the cached visible list");

        EchoVoxelCamera nudgedCamera = new EchoVoxelCamera(0.6D, 2.8D, 0.5D, 0.0D, 0.0D, 70.0D);
        List<EchoEntityState> nudged = runtime.renderEntitiesNear(
                nudgedCamera,
                24.0D,
                EchoClientEntityRuntime.MAX_RENDERED_ENTITIES
        );
        require(runtime.lastRenderCacheHit() && nudged == selected,
                "Tiny camera movement should not churn the entity render selection cache");

        runtime.store().register(entity(
                "test:late_nearby",
                "echoashfallprotocol:rad_zombie",
                0,
                2,
                0
        ));
        runtime.renderEntitiesNear(camera, 24.0D, EchoClientEntityRuntime.MAX_RENDERED_ENTITIES);
        require(!runtime.lastRenderCacheHit() && runtime.lastRenderCandidateCount() == nearbyCount + 1,
                "Entity store changes should invalidate render selection cache");

        EchoClientEntityRuntime tieRuntime = new EchoClientEntityRuntime(entityCatalog);
        tieRuntime.store().register(entity("test:z_tie", "echoashfallprotocol:rad_zombie", 1, 1, 0));
        tieRuntime.store().register(entity("test:a_tie", "echoashfallprotocol:rad_zombie", -1, 1, 0));
        List<EchoEntityState> ties = tieRuntime.renderEntitiesNear(
                new EchoVoxelCamera(0.5D, 1.86D, 0.5D, 0.0D, 0.0D, 70.0D),
                4.0D,
                2
        );
        require(ties.size() == 2
                        && ties.get(0).id().value().equals("test:a_tie")
                        && ties.get(1).id().value().equals("test:z_tie"),
                "Equal-distance entity render selection should expose a stable id order");
    }

    private static EchoEntityState entity(String definitionId, int x, int y, int z) {
        return entity(
                "test:" + definitionId.substring(definitionId.indexOf(':') + 1),
                definitionId,
                x,
                y,
                z
        );
    }

    private static EchoEntityState entity(String entityId, String definitionId, int x, int y, int z) {
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
                new EchoEntityPositionComponent(new EchoWorldPosition(x, y, z)),
                new EchoEntityHealthComponent(definition.maxHealth(), definition.maxHealth()),
                new EchoEntityMovementComponent(definition.movementSpeed(), true),
                new EchoEntityAiComponent(definition.aiProfile(), EchoEntityAiState.IDLE)
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
