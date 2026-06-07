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
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.Arrays;
import java.util.List;

public final class EchoClientDroppedItemRenderSmokeHarness {
    private EchoClientDroppedItemRenderSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDroppedItemMesh();
        requireMixedEntityAndDropMesh();
        requireRenderViewCullsFarPiles();
        requireWalkingCameraKeepsStableMeshSignatureForSameVisibleSet();
        requireFallingDropReusesRenderSelectionCache();
        requireRenderSelectionTieBreaks();
        System.out.println("client dropped item render smoke PASS droppedCube=visible mixed=combined cull=nearest walking=stable fallingCache=rehydrated tie=id");
    }

    private static void requireDroppedItemMesh() {
        EchoClientDroppedItem drop = droppedItem("drop-render-a", 3, 2.5D, 4.0D, 6.5D, 0.25D);
        EchoClientEntityRenderer.MeshData mesh = EchoClientEntityRenderer.meshData(
                List.of(),
                EchoClientEntityCatalog.empty(),
                List.of(drop)
        );

        require(mesh.vertexCount() == EchoClientEntityRenderer.DROPPED_ITEM_VERTEX_COUNT,
                "One dropped item should emit one visible cuboid worth of vertices");
        require(mesh.indexCount() == EchoClientEntityRenderer.DROPPED_ITEM_INDEX_COUNT,
                "One dropped item should emit one visible cuboid worth of indices");
        require(mesh.vertices().length
                        == EchoClientEntityRenderer.DROPPED_ITEM_VERTEX_COUNT
                        * EchoClientEntityRenderer.VERTEX_STRIDE_FLOATS,
                "Dropped item vertices should use the shared entity vertex stride");
        int color = EchoClientEntityRenderer.argbForDroppedItem(drop);
        require((color >>> 24) == 0xFF && (color & 0x00FFFFFF) != 0,
                "Dropped item render tint should be opaque and non-black");

        EchoClientEntityCatalog catalog = EchoClientEntityCatalog.empty();
        int firstSignature = EchoClientEntityRenderer.meshSignature(
                List.of(),
                catalog,
                List.of(droppedItem("drop-render-a", 3, 2.5D, 4.0D, 6.5D, 0.05D))
        );
        int laterSignature = EchoClientEntityRenderer.meshSignature(
                List.of(),
                catalog,
                List.of(droppedItem("drop-render-a", 3, 2.5D, 4.0D, 6.5D, 0.45D))
        );
        require(firstSignature == laterSignature,
                "Dropped item render signature should stay stable when only age changes");
        EchoClientEntityRenderer.MeshData firstMesh = EchoClientEntityRenderer.meshData(
                List.of(),
                EchoClientEntityCatalog.empty(),
                List.of(droppedItem("drop-render-a", 3, 2.5D, 4.0D, 6.5D, 0.05D))
        );
        EchoClientEntityRenderer.MeshData laterMesh = EchoClientEntityRenderer.meshData(
                List.of(),
                EchoClientEntityCatalog.empty(),
                List.of(droppedItem("drop-render-a", 3, 2.5D, 4.0D, 6.5D, 0.45D))
        );
        require(Arrays.equals(firstMesh.vertices(), laterMesh.vertices()),
                "Dropped item render geometry should not churn when only age changes");
    }

    private static void requireMixedEntityAndDropMesh() {
        EchoEntityState hostile = hostile("test:render_hostile", new EchoWorldPosition(1, 3, 1));
        EchoClientDroppedItem first = droppedItem("drop-render-b", 1, 2.0D, 3.0D, 2.0D, 0.0D);
        EchoClientDroppedItem second = droppedItem("drop-render-c", 12, 3.0D, 3.0D, 2.0D, 1.0D);
        EchoClientEntityRenderer.MeshData mesh = EchoClientEntityRenderer.meshData(
                List.of(hostile),
                EchoClientEntityCatalog.empty(),
                List.of(first, second)
        );

        require(mesh.vertexCount() == EchoClientEntityRenderer.HUMANOID_VERTEX_COUNT
                        + EchoClientEntityRenderer.DROPPED_ITEM_VERTEX_COUNT * 2,
                "Hostile plus two dropped items should share one combined render mesh");
        require(mesh.indexCount() == EchoClientEntityRenderer.HUMANOID_INDEX_COUNT
                        + EchoClientEntityRenderer.DROPPED_ITEM_INDEX_COUNT * 2,
                "Combined entity/drop mesh should preserve expected triangle indices");
    }

    private static void requireRenderViewCullsFarPiles() {
        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        for (int i = 0; i < 120; i++) {
            runtime.drop(new EchoItemStack(definition, 1), i * 2.0D, 1.08D, 0.0D);
        }

        List<EchoClientDroppedItem> visible = runtime.renderDropsNear(
                0.0D,
                1.60D,
                0.0D,
                96.0D,
                24
        );
        require(!runtime.lastRenderCacheHit(),
                "First dropped item render query should build a fresh culled view");
        require(runtime.count() == 120,
                "Render culling should not remove simulated dropped items");
        require(visible.size() == 24,
                "Render culling should cap a large item pile to the requested visible limit");
        require(runtime.lastRenderCandidateCount() == 48 && runtime.lastRenderReturnedCount() == visible.size(),
                "Render culling should expose nearby candidate and returned diagnostics");
        require(runtime.lastNearbyQueryDropIdCount() < runtime.count(),
                "Render culling should query spatial bucket drop IDs instead of scanning every active drop");
        require(visible.getFirst().dropId().equals("drop-1"),
                "Render culling should keep nearest drops first for stable mesh signatures");
        double farthestVisibleX = visible.stream().mapToDouble(EchoClientDroppedItem::x).max().orElse(-1.0D);
        require(farthestVisibleX <= 46.0D,
                "Render culling should keep the nearest drops instead of distant pile entries");
        List<EchoClientDroppedItem> cachedVisible = runtime.renderDropsNear(
                0.0D,
                1.60D,
                0.0D,
                96.0D,
                24
        );
        require(runtime.lastRenderCacheHit() && cachedVisible == visible,
                "Repeated dropped item render queries should reuse the cached culled view");
        List<EchoClientDroppedItem> nudgedVisible = runtime.renderDropsNear(
                0.12D,
                1.60D,
                0.0D,
                96.0D,
                24
        );
        require(runtime.lastRenderCacheHit() && nudgedVisible == visible,
                "Tiny camera movement should reuse the cached dropped item render view");
        List<EchoClientDroppedItem> movedVisible = runtime.renderDropsNear(
                0.30D,
                1.60D,
                0.0D,
                96.0D,
                24
        );
        require(!runtime.lastRenderCacheHit() && movedVisible.size() == visible.size(),
                "Larger camera movement should refresh dropped item render selection");
        runtime.drop(new EchoItemStack(definition, 1), 0.25D, 1.08D, 0.0D);
        List<EchoClientDroppedItem> refreshedVisible = runtime.renderDropsNear(
                0.0D,
                1.60D,
                0.0D,
                96.0D,
                24
        );
        require(!runtime.lastRenderCacheHit(),
                "Dropped item merges should invalidate the cached render view");
        require(refreshedVisible.getFirst().quantity() == 2,
                "Refreshed render view should expose the merged nearest dropped item quantity");

        EchoClientEntityRenderer.MeshData mesh = EchoClientEntityRenderer.meshData(
                List.of(),
                EchoClientEntityCatalog.empty(),
                refreshedVisible
        );
        require(mesh.indexCount() == EchoClientEntityRenderer.DROPPED_ITEM_INDEX_COUNT * refreshedVisible.size(),
                "Culled dropped item render view should drive mesh size, not full simulation count");
    }

    private static void requireRenderSelectionTieBreaks() {
        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        runtime.applySnapshots(List.of(
                snapshot("drop-z", definition, 1.0D, 1.0D, 0.0D),
                snapshot("drop-a", definition, -1.0D, 1.0D, 0.0D)
        ));

        List<EchoClientDroppedItem> visible = runtime.renderDropsNear(
                0.0D,
                1.0D,
                0.0D,
                4.0D,
                1
        );
        require(visible.size() == 1 && visible.getFirst().dropId().equals("drop-a"),
                "Culled dropped item render selection should keep drop-id tie ordering without a full sort");
        require(runtime.lastRenderCandidateCount() == 2 && runtime.lastRenderReturnedCount() == 1,
                "Dropped item render selection should still report full candidate count when capped");
    }

    private static void requireWalkingCameraKeepsStableMeshSignatureForSameVisibleSet() {
        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        runtime.applySnapshots(List.of(
                snapshot("drop-b", definition, -1.0D, 1.0D, 0.0D),
                snapshot("drop-a", definition, 1.0D, 1.0D, 0.0D)
        ));

        List<EchoClientDroppedItem> leftView = runtime.renderDropsNear(
                -0.6D,
                1.0D,
                0.0D,
                4.0D,
                2
        );
        int leftSignature = EchoClientEntityRenderer.droppedItemMeshSignature(leftView);
        require(!runtime.lastRenderCacheHit()
                        && leftView.size() == 2
                        && leftView.get(0).dropId().equals("drop-a")
                        && leftView.get(1).dropId().equals("drop-b"),
                "Dropped item walking smoke should start with a stable drop-id render order");

        List<EchoClientDroppedItem> rightView = runtime.renderDropsNear(
                0.6D,
                1.0D,
                0.0D,
                4.0D,
                2
        );
        int rightSignature = EchoClientEntityRenderer.droppedItemMeshSignature(rightView);
        require(!runtime.lastRenderCacheHit(),
                "Walking beyond the tiny render-cache tolerance should rebuild the culled drop view");
        require(rightView.size() == leftView.size()
                        && rightView.get(0).dropId().equals("drop-a")
                        && rightView.get(1).dropId().equals("drop-b"),
                "Walking around the same visible drop set should keep render order stable");
        require(leftSignature == rightSignature,
                "Walking around the same visible drop set should not churn the dropped-item mesh signature");
    }

    private static void requireFallingDropReusesRenderSelectionCache() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-render-cache").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int blockX = (int) Math.floor(player.x());
        int blockZ = (int) Math.floor(player.z());
        int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), blockX, blockZ);
        require(surfaceY >= 0,
                "Falling dropped item render cache smoke should find a solid floor near player spawn");

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        double dropX = player.x();
        double dropY = surfaceY + 2.35D;
        double dropZ = player.z();
        runtime.drop(new EchoItemStack(definition, 1), dropX, dropY, dropZ);

        List<EchoClientDroppedItem> initial = runtime.renderDropsNear(dropX, dropY, dropZ, 8.0D, 8);
        require(!runtime.lastRenderCacheHit() && initial.size() == 1,
                "First falling dropped item render query should select a fresh visible set");
        double initialY = initial.getFirst().y();

        runtime.tick(0.05D, session.world());
        List<EchoClientDroppedItem> refreshed = runtime.renderDropsNear(dropX, dropY, dropZ, 8.0D, 8);
        require(runtime.lastRenderCacheHit(),
                "Falling dropped item movement inside one spatial bucket should reuse selected drop ids");
        require(refreshed.size() == 1 && refreshed.getFirst().dropId().equals(initial.getFirst().dropId()),
                "Rehydrated falling dropped item render cache should preserve the selected drop id");
        require(refreshed.getFirst().y() < initialY,
                "Rehydrated falling dropped item render cache should expose the current falling y position");
        require(runtime.lastRenderCandidateCount() == 1 && runtime.lastRenderReturnedCount() == 1,
                "Rehydrated falling dropped item render cache should preserve selection diagnostics");
    }

    private static EchoClientDroppedItemSnapshot snapshot(
            String dropId,
            EchoItemDefinition definition,
            double x,
            double y,
            double z
    ) {
        return new EchoClientDroppedItemSnapshot(
                dropId,
                definition.id().value(),
                definition.displayName(),
                definition.category(),
                definition.maxStackSize(),
                1,
                x,
                y,
                z,
                0.0D
        );
    }

    private static EchoClientDroppedItem droppedItem(
            String dropId,
            int quantity,
            double x,
            double y,
            double z,
            double ageSeconds
    ) {
        return new EchoClientDroppedItem(
                dropId,
                droppedItemDefinition(),
                quantity,
                x,
                y,
                z,
                ageSeconds
        );
    }

    private static EchoItemDefinition droppedItemDefinition() {
        return new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:scrap_metal"),
                "Scrap Metal",
                EchoItemCategory.MATERIAL,
                64,
                1.0D,
                List.of("salvage", "dropped_item_render_smoke"),
                List.of("Recovered material")
        );
    }

    private static EchoEntityState hostile(String entityId, EchoWorldPosition position) {
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
