package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;

public final class EchoClientDroppedItemPhysicsSmokeHarness {
    private static final double EXPECTED_GROUND_CLEARANCE = 0.08D;

    private EchoClientDroppedItemPhysicsSmokeHarness() {
    }

    public static void main(String[] args) {
        requireDroppedItemSettlesAndRemainsPickable();
        requireDroppedItemPhysicsSkipsSubFrameProbes();
        requireFallingItemReusesChunkIndexBetweenPhysicsSteps();
        requireFallingPileBudgetsPhysicsWork();
        requireLongFrameCapsDroppedItemCatchupWork();
        requireSettledItemSkipsChunkIndexingBetweenGroundProbes();
        requireSettledPileStaggersGroundRechecks();
        System.out.println("client dropped item physics smoke PASS settle=ground pickup=bucket budget=fixed-step falling=index-reuse falling-pile=budgeted catchup=bounded settled=index-skip pile=staggered");
    }

    private static void requireDroppedItemSettlesAndRemainsPickable() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-physics").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        int z = (int) Math.floor(player.z());
        int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
        require(surfaceY >= 0, "Dropped item physics smoke should find a solid floor near player spawn");

        EchoVoxelBlock block = session.bridge().runtimeMarkerBlock();
        int beforeCount = countItem(session, block.id());
        EchoClientDroppedItem drop = session.dropBlockItem(
                block,
                player.x(),
                surfaceY + 4.0D,
                player.z()
        );
        require(drop != null, "Dropped item physics smoke should create a high in-world drop");
        require(drop.y() > surfaceY + 3.0D,
                "Dropped item physics smoke should start the item above the target floor");

        for (int i = 0; i < 100; i++) {
            session.tickEntities(0.05D);
        }

        require(session.droppedItemCount() == 1,
                "Dropped item physics should not duplicate or despawn a fresh falling item");
        EchoClientDroppedItem settled = session.droppedItems().getFirst();
        double expectedY = surfaceY + EXPECTED_GROUND_CLEARANCE;
        require(settled.y() < drop.y() - 2.0D,
                "Dropped item physics should lower a high spawned item toward the floor");
        require(Math.abs(settled.y() - expectedY) < 0.05D,
                "Dropped item physics should settle on top of the nearest solid floor");
        require(settled.ageSeconds() >= 4.5D,
                "Dropped item physics should continue aging the live dropped item");

        EchoClientDroppedItemRuntime.PickupResult pickup = session.pickupNearbyDroppedItems();
        require(pickup.pickedQuantity() == 1 && pickup.remainingDrops() == 0,
                "Settled dropped item should remain pickable after crossing spatial buckets");
        require(countItem(session, block.id()) == beforeCount + 1,
                "Picking up the settled dropped item should add it to the player inventory");
    }

    private static void requireDroppedItemPhysicsSkipsSubFrameProbes() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-physics-budget").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        int z = (int) Math.floor(player.z());
        int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
        require(surfaceY >= 0, "Dropped item physics budget smoke should find a solid floor near player spawn");

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        runtime.drop(
                new EchoItemStack(droppedItemDefinition(), 1),
                player.x(),
                surfaceY + 4.0D,
                player.z()
        );

        runtime.tick(0.016D, session.world());
        require(runtime.lastPhysicsStepCount() == 0,
                "Sub-frame dropped item ticks should accumulate instead of probing the world every render frame");
        require(runtime.lastPhysicsBlockLookupCount() == 0,
                "Sub-frame dropped item ticks should not spend block lookups before a fixed physics step");

        double yBeforePhysicsStep = runtime.drops().getFirst().y();
        runtime.tick(0.035D, session.world());
        require(runtime.lastPhysicsStepCount() == 1,
                "Accumulated dropped item time should execute one fixed physics step");
        require(runtime.lastPhysicsChunkIndexBuildCount() == 1,
                "A fixed dropped item physics step should index chunks once when a falling item needs a floor probe");
        require(runtime.lastPhysicsBlockLookupCount() > 0,
                "A fixed dropped item physics step should still probe the floor");
        require(runtime.drops().getFirst().y() < yBeforePhysicsStep,
                "A fixed dropped item physics step should move a falling item downward");
    }

    private static void requireSettledItemSkipsChunkIndexingBetweenGroundProbes() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-settled-budget").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        int z = (int) Math.floor(player.z());
        int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
        require(surfaceY >= 0, "Dropped item settled budget smoke should find a solid floor near player spawn");

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        runtime.drop(
                new EchoItemStack(droppedItemDefinition(), 1),
                player.x(),
                surfaceY + 4.0D,
                player.z()
        );
        for (int i = 0; i < 100; i++) {
            runtime.tick(0.05D, session.world());
        }
        double settledY = runtime.drops().getFirst().y();
        double expectedY = surfaceY + EXPECTED_GROUND_CLEARANCE;
        require(Math.abs(settledY - expectedY) < 0.05D,
                "Dropped item settled budget smoke should start from a settled item");

        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsStepCount() == 1,
                "A settled dropped item should still consume fixed physics time");
        require(runtime.lastPhysicsChunkIndexBuildCount() == 0,
                "Settled dropped items should not rebuild the chunk index between scheduled ground probes");
        require(runtime.lastPhysicsBlockLookupCount() == 0,
                "Settled dropped items should not probe blocks between scheduled ground probes");
        require(Math.abs(runtime.drops().getFirst().y() - settledY) < 0.001D,
                "Skipping settled dropped item chunk indexing should leave the item at rest");
    }

    private static void requireSettledPileStaggersGroundRechecks() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-pile-budget").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int baseX = (int) Math.floor(player.x()) - 10;
        int baseZ = (int) Math.floor(player.z()) - 8;
        int targetDrops = 80;

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        int placed = 0;
        for (int gridX = 0; gridX < 10 && placed < targetDrops; gridX++) {
            for (int gridZ = 0; gridZ < 8 && placed < targetDrops; gridZ++) {
                int blockX = baseX + gridX * 2;
                int blockZ = baseZ + gridZ * 2;
                int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), blockX, blockZ);
                require(surfaceY >= 0,
                        "Dropped item pile budget smoke should find loaded floor columns near spawn");
                runtime.drop(
                        new EchoItemStack(definition, 1),
                        blockX + 0.5D,
                        surfaceY + EXPECTED_GROUND_CLEARANCE,
                        blockZ + 0.5D
                );
                placed++;
            }
        }
        require(runtime.count() == targetDrops,
                "Dropped item pile budget smoke should create a dense but active settled pile");

        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsSettledProbeCount() == 0,
                "Newly settled dropped items should not count as scheduled settled rechecks");
        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsSettledProbeCount() == 0,
                "Settled dropped items should not immediately resynchronize ground probes");

        runtime.tick(0.69D, session.world());
        runtime.tick(0.20D, session.world());
        int settledProbes = runtime.lastPhysicsSettledProbeCount();
        require(settledProbes > 0,
                "A settled dropped item pile should eventually recheck ground stability");
        require(settledProbes <= 32 && settledProbes < targetDrops,
                "Settled dropped item pile ground probes should be staggered and budgeted per frame");
    }

    private static void requireFallingItemReusesChunkIndexBetweenPhysicsSteps() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-falling-budget").gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int x = (int) Math.floor(player.x());
        int z = (int) Math.floor(player.z());
        int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), x, z);
        require(surfaceY >= 0, "Dropped item falling budget smoke should find a solid floor near player spawn");

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        runtime.drop(
                new EchoItemStack(droppedItemDefinition(), 1),
                player.x(),
                surfaceY + 8.0D,
                player.z()
        );

        runtime.tick(0.05D, session.world());
        double yAfterFirstStep = runtime.drops().getFirst().y();
        require(runtime.lastPhysicsStepCount() == 1,
                "A falling dropped item should execute one fixed physics step");
        require(runtime.lastPhysicsChunkIndexBuildCount() == 1,
                "The first falling dropped item physics step should build one chunk index");
        require(runtime.lastPhysicsBlockLookupCount() > 0,
                "The first falling dropped item physics step should probe blocks through the chunk index");

        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsStepCount() == 1,
                "A second falling dropped item tick should still execute one fixed physics step");
        require(runtime.lastPhysicsChunkIndexBuildCount() == 0,
                "Falling dropped item physics should reuse the chunk index between unchanged world snapshots");
        require(runtime.lastPhysicsBlockLookupCount() == 0,
                "Falling dropped item physics should reuse the cached ground column after the first probe");
        require(runtime.drops().getFirst().y() < yAfterFirstStep,
                "The falling dropped item should keep moving while reusing the chunk index");
    }

    private static void requireFallingPileBudgetsPhysicsWork() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-falling-pile-budget")
                        .gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int baseX = (int) Math.floor(player.x()) - 10;
        int baseZ = (int) Math.floor(player.z()) - 8;
        int targetDrops = EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP + 16;

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        int placed = 0;
        for (int gridX = 0; gridX < 10 && placed < targetDrops; gridX++) {
            for (int gridZ = 0; gridZ < 8 && placed < targetDrops; gridZ++) {
                int blockX = baseX + gridX * 2;
                int blockZ = baseZ + gridZ * 2;
                int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), blockX, blockZ);
                require(surfaceY >= 0,
                        "Dropped item falling pile budget smoke should find loaded floor columns near spawn");
                runtime.drop(
                        new EchoItemStack(definition, 1),
                        blockX + 0.5D,
                        surfaceY + 6.0D,
                        blockZ + 0.5D
                );
                placed++;
            }
        }
        require(runtime.count() == targetDrops,
                "Dropped item falling pile budget smoke should create more drops than one physics step can process");

        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsDropWorkCount() == EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP,
                "A falling dropped item pile should be capped by the per-step physics work budget");
        require(runtime.lastPhysicsBlockLookupCount()
                        <= EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP * 9,
                "A falling dropped item pile should not probe every active drop in one physics step");

        runtime.tick(0.05D, session.world());
        require(runtime.lastPhysicsDropWorkCount() <= EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP,
                "Rotating dropped item physics work should stay capped on later fixed steps");
    }

    private static void requireLongFrameCapsDroppedItemCatchupWork() {
        EchoClientGameSession session =
                EchoClientWorldSessionFactory.defaultFactory().newWorld("dropped-item-long-frame-budget")
                        .gameSession();
        EchoVoxelPlayerState player = session.player().state();
        int baseX = (int) Math.floor(player.x()) - 10;
        int baseZ = (int) Math.floor(player.z()) - 8;
        int targetDrops = EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP
                * EchoClientDroppedItemRuntime.MAX_PHYSICS_STEPS_PER_TICK
                + 16;

        EchoClientDroppedItemRuntime runtime = new EchoClientDroppedItemRuntime();
        EchoItemDefinition definition = droppedItemDefinition();
        int placed = 0;
        for (int gridX = 0; gridX < 16 && placed < targetDrops; gridX++) {
            for (int gridZ = 0; gridZ < 10 && placed < targetDrops; gridZ++) {
                int blockX = baseX + gridX * 2;
                int blockZ = baseZ + gridZ * 2;
                int surfaceY = EchoClientEntityAi.surfaceSpawnY(session.world(), blockX, blockZ);
                require(surfaceY >= 0,
                        "Dropped item long-frame budget smoke should find loaded floor columns near spawn");
                runtime.drop(
                        new EchoItemStack(definition, 1),
                        blockX + 0.5D,
                        surfaceY + 6.0D,
                        blockZ + 0.5D
                );
                placed++;
            }
        }
        require(runtime.count() == targetDrops,
                "Dropped item long-frame budget smoke should create more drops than one capped tick can process");

        runtime.tick(0.25D, session.world());
        require(runtime.lastPhysicsStepCount() == EchoClientDroppedItemRuntime.MAX_PHYSICS_STEPS_PER_TICK,
                "Dropped item physics should cap fixed-step catch-up work after a long frame");
        require(runtime.lastPhysicsDropWorkCount()
                        <= EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP
                        * EchoClientDroppedItemRuntime.MAX_PHYSICS_STEPS_PER_TICK,
                "Dropped item long-frame catch-up should stay inside the per-tick work ceiling");
        require(runtime.lastPhysicsBlockLookupCount()
                        <= EchoClientDroppedItemRuntime.MAX_PHYSICS_DROPS_PER_STEP
                        * EchoClientDroppedItemRuntime.MAX_PHYSICS_STEPS_PER_TICK
                        * 9,
                "Dropped item long-frame catch-up should not probe every active drop repeatedly");
    }

    private static int countItem(EchoClientGameSession session, String itemId) {
        return session.inventoryScreenModel().slots().stream()
                .filter(slot -> slot.runtimeId().equals(itemId))
                .mapToInt(EchoClientSlotStack::count)
                .sum();
    }

    private static EchoItemDefinition droppedItemDefinition() {
        return new EchoItemDefinition(
                new EchoItemId("echoashfallprotocol:scrap_metal"),
                "Scrap Metal",
                EchoItemCategory.MATERIAL,
                64,
                1.0D,
                List.of("salvage", "dropped_item_physics_budget"),
                List.of("Recovered material")
        );
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
