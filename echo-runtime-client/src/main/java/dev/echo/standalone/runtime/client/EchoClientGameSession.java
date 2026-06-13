package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.entity.EchoEntityStore;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoInventoryOperations;
import dev.echo.standalone.runtime.item.EchoInventoryTransferResult;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemCraftResult;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRecipe;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.item.EchoLootTable;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerInput;
import dev.echo.standalone.runtime.render.EchoVoxelCamera;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime;
import dev.echo.standalone.runtime.world.EchoVoxelFluidRuntime.EchoVoxelFluidType;
import dev.echo.standalone.runtime.world.EchoVoxelHit;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldStreamer;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

final class EchoClientGameSession {
    private static final int HOSTILE_ATTACK_DAMAGE = 2;
    private static final String EMPTY_BUCKET_ITEM_ID = "minecraft:bucket";
    private static final String WATER_BUCKET_ITEM_ID = "minecraft:water_bucket";
    private static final String LAVA_BUCKET_ITEM_ID = "minecraft:lava_bucket";
    private static final String SCRAP_METAL_ITEM_ID = "echoashfallprotocol:scrap_metal";
    private static final String COMPRESSED_SCRAP_ITEM_ID = "echoashfallprotocol:compressed_scrap";

    private EchoAdapterCoreStandaloneContentBridge bridge;
    private final EchoClientWorldRuntime worldRuntime;
    private final EchoClientWorldPresentation presentation;
    private EchoClientWorldInteractionCatalog interactionCatalog;
    private EchoClientEntityCatalog entityCatalog;
    private EchoVoxelPlayerController player;
    private EchoVoxelPlayerHotbar hotbar;
    private final EchoClientInventoryRuntime inventory;
    private final EchoClientEntityRuntime entities;
    private final EchoClientDroppedItemRuntime droppedItems = new EchoClientDroppedItemRuntime();
    private EchoClientMachineRuntime machines = EchoClientMachineRuntime.reference();
    private EchoClientPlayerRuntime playerRuntime = new EchoClientPlayerRuntime();

    EchoClientGameSession(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelWorld world,
            EchoVoxelPlayerController player,
            EchoVoxelPlayerHotbar hotbar,
            EchoInventoryOperations inventoryOperations,
            EchoInventoryContainer playerInventory,
            EchoInventoryContainer openContainer,
            EchoItemRegistry itemRegistry,
            List<EchoItemRecipe> workbenchRecipes,
            List<EchoLootTable> lootTables,
            EchoVoxelWorldStreamer streamer,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog,
            EchoClientWorldPresentation presentation
    ) {
        this.bridge = bridge;
        this.presentation = presentation == null ? EchoClientWorldPresentation.generic() : presentation;
        this.interactionCatalog = interactionCatalog == null
                ? EchoClientWorldInteractionCatalog.empty()
                : interactionCatalog;
        this.entityCatalog = entityCatalog == null ? EchoClientEntityCatalog.empty() : entityCatalog;
        this.worldRuntime = new EchoClientWorldRuntime(world, streamer, hazardCatalog);
        this.player = player;
        this.hotbar = hotbar;
        this.entities = new EchoClientEntityRuntime(this.entityCatalog);
        this.inventory = new EchoClientInventoryRuntime(
                bridge,
                inventoryOperations,
                playerInventory,
                openContainer,
                itemRegistry,
                workbenchRecipes,
                lootTables
        );
        inventory.syncInventoryFromHotbar(hotbar);
    }

    EchoAdapterCoreStandaloneContentBridge bridge() {
        return bridge;
    }

    EchoVoxelWorld world() {
        return worldRuntime.world();
    }

    EchoVoxelPlayerController player() {
        return player;
    }

    EchoVoxelPlayerHotbar hotbar() {
        return hotbar;
    }

    EchoEntityStore entityStore() {
        return entities.store();
    }

    EchoClientEntityCatalog entityCatalog() {
        return entityCatalog;
    }

    void updateRuntimeContent(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoItemRegistry itemRegistry,
            List<EchoItemRecipe> workbenchRecipes,
            List<EchoLootTable> lootTables,
            EchoClientEntityCatalog entityCatalog,
            EchoClientHazardCatalog hazardCatalog,
            EchoClientWorldInteractionCatalog interactionCatalog,
            EchoVoxelWorldStreamer streamer
    ) {
        if (bridge != null) {
            this.bridge = bridge;
        }
        inventory.updateRuntimeContent(this.bridge, itemRegistry, workbenchRecipes, lootTables);
        if (entityCatalog != null) {
            this.entityCatalog = entityCatalog;
            entities.updateCatalog(entityCatalog);
        }
        worldRuntime.updateHazardCatalog(hazardCatalog);
        worldRuntime.updateStreamer(streamer);
        if (interactionCatalog != null) {
            this.interactionCatalog = interactionCatalog;
        }
    }

    int livingEntityCount() {
        return entities.livingCount();
    }

    int hostileEntityCount() {
        return entities.hostileCount();
    }

    int renderedEntityCount() {
        return entities.lastRenderReturnedCount();
    }

    int entityRenderCandidateCount() {
        return entities.lastRenderCandidateCount();
    }

    int droppedItemCount() {
        return droppedItems.count();
    }

    int droppedItemQuantity() {
        return droppedItems.totalQuantity();
    }

    int droppedItemRenderCandidateCount() {
        return droppedItems.lastRenderCandidateCount();
    }

    int droppedItemRenderCount() {
        return droppedItems.lastRenderReturnedCount();
    }

    int droppedItemPhysicsStepCount() {
        return droppedItems.lastPhysicsStepCount();
    }

    int droppedItemPhysicsBlockLookupCount() {
        return droppedItems.lastPhysicsBlockLookupCount();
    }

    int droppedItemPhysicsChunkIndexBuildCount() {
        return droppedItems.lastPhysicsChunkIndexBuildCount();
    }

    int droppedItemPhysicsDropWorkCount() {
        return droppedItems.lastPhysicsDropWorkCount();
    }

    List<EchoClientDroppedItemSnapshot> droppedItemSnapshots() {
        return droppedItems.snapshots();
    }

    List<EchoClientDroppedItem> droppedItems() {
        return droppedItems.drops();
    }

    List<EchoClientDroppedItem> renderDroppedItems(EchoVoxelCamera camera, int chunkViewDistance) {
        if (camera == null) {
            return List.of();
        }
        double radius = EchoClientSettings.visibleDistanceBlocks(
                chunkViewDistance,
                world().chunkSize()
        ) + 8.0D;
        radius = Math.min(radius, EchoClientDroppedItemRuntime.MAX_RENDER_RADIUS_BLOCKS);
        return droppedItems.renderDropsNear(
                camera.x(),
                camera.y(),
                camera.z(),
                radius,
                EchoClientDroppedItemRuntime.MAX_RENDERED_DROPS
        );
    }

    List<EchoEntityState> renderEntities(EchoVoxelCamera camera, int chunkViewDistance) {
        if (camera == null) {
            return List.of();
        }
        double radius = EchoClientSettings.visibleDistanceBlocks(
                chunkViewDistance,
                world().chunkSize()
        ) + 8.0D;
        radius = Math.min(radius, EchoClientEntityRuntime.MAX_RENDER_RADIUS_BLOCKS);
        return entities.renderEntitiesNear(
                camera,
                radius,
                EchoClientEntityRuntime.MAX_RENDERED_ENTITIES
        );
    }

    List<EchoClientEntitySnapshot> entitySnapshots() {
        return entities.snapshots();
    }

    EchoClientMachineStateSnapshot machineStateSnapshot() {
        return machines.snapshot();
    }

    int reconcileMachineBlockEntitiesFromWorld() {
        return machines.reconcileFromWorld(persistedWorld());
    }

    int materializeMachineBlockEntities() {
        int placed = 0;
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : machineStateSnapshot().blockEntities()) {
            try {
                EchoVoxelBlock block = bridge.registry().requireLiveVoxelBlock(blockEntity.blockId());
                EchoVoxelBlockState state = defaultBlockStateFor(block)
                        .withProperty("source", "machine_block_entity")
                        .withProperty("blockEntityId", blockEntity.entityId())
                        .withProperty("canonicalId", blockEntity.state().getOrDefault("canonicalId", blockEntity.entityId()))
                        .withProperty("machineKind", blockEntity.kind());
                String recipeProgress = blockEntity.state().get("recipeProgressTicks");
                if (recipeProgress != null && !recipeProgress.isBlank()) {
                    state = state.withProperty("recipeProgressTicks", recipeProgress);
                }
                if (worldRuntime.setBlockStateAt(blockEntity.x(), blockEntity.y(), blockEntity.z(), state)) {
                    placed++;
                }
            } catch (RuntimeException ignored) {
                // Missing runtime machine blocks should not prevent older saves from loading.
            }
        }
        return placed;
    }

    void recordBlockPlaced(EchoVoxelBlockState state) {
        if (state == null || state.air()) {
            return;
        }
        if (EchoClientMachineRuntime.machineBlockDefinition(state.block()).isPresent()) {
            reconcileMachineBlockEntitiesFromWorld();
            materializeMachineBlockEntities();
        }
    }

    EchoClientTechSurfaceModel techSurfaceModel() {
        reconcileMachineBlockEntitiesFromWorld();
        return machines.techSurfaceModel(bridge);
    }

    int tickMachines(int ticks) {
        reconcileMachineBlockEntitiesFromWorld();
        int processed = machines.tick(ticks);
        materializeMachineBlockEntities();
        return processed;
    }

    EchoClientMachineInputResult insertScrapIntoMachine(String machineId) {
        reconcileMachineBlockEntitiesFromWorld();
        String targetMachineId = machineId == null ? "" : machineId.trim();
        if (!machines.acceptsScrapInput(targetMachineId)) {
            return new EchoClientMachineInputResult(
                    false,
                    targetMachineId,
                    SCRAP_METAL_ITEM_ID,
                    0,
                    inventory.itemCount(SCRAP_METAL_ITEM_ID),
                    0,
                    "unknown_or_unsupported_machine"
            );
        }
        EchoInventoryOperationResult consumed = inventory.consumeItem(SCRAP_METAL_ITEM_ID, 1, hotbar);
        if (!consumed.success()) {
            return new EchoClientMachineInputResult(
                    false,
                    targetMachineId,
                    SCRAP_METAL_ITEM_ID,
                    0,
                    inventory.itemCount(SCRAP_METAL_ITEM_ID),
                    0,
                    consumed.reason()
            );
        }
        EchoClientMachineRuntime.MachineInputResult inserted = machines.insertScrapInput(targetMachineId, 1);
        materializeMachineBlockEntities();
        return new EchoClientMachineInputResult(
                inserted.success(),
                inserted.machineId(),
                SCRAP_METAL_ITEM_ID,
                inserted.insertedQuantity(),
                inventory.itemCount(SCRAP_METAL_ITEM_ID),
                inserted.inputCount(),
                inserted.reason()
        );
    }

    EchoClientMachineOutputResult extractCompressedScrapFromMachine(String machineId) {
        reconcileMachineBlockEntitiesFromWorld();
        String targetMachineId = machineId == null ? "" : machineId.trim();
        int available = machines.compressedScrapAvailable(targetMachineId);
        EchoItemDefinition outputDefinition = compressedScrapDefinition();
        if (available <= 0) {
            return new EchoClientMachineOutputResult(
                    false,
                    targetMachineId,
                    COMPRESSED_SCRAP_ITEM_ID,
                    0,
                    inventory.itemCount(COMPRESSED_SCRAP_ITEM_ID),
                    0,
                    "no_compressed_scrap"
            );
        }
        if (inventory.availableSpace(outputDefinition) <= 0) {
            return new EchoClientMachineOutputResult(
                    false,
                    targetMachineId,
                    COMPRESSED_SCRAP_ITEM_ID,
                    0,
                    inventory.itemCount(COMPRESSED_SCRAP_ITEM_ID),
                    available,
                    "inventory_full"
            );
        }
        EchoClientMachineRuntime.MachineOutputResult extracted =
                machines.extractCompressedScrap(targetMachineId, 1);
        if (!extracted.success()) {
            return new EchoClientMachineOutputResult(
                    false,
                    extracted.machineId(),
                    COMPRESSED_SCRAP_ITEM_ID,
                    0,
                    inventory.itemCount(COMPRESSED_SCRAP_ITEM_ID),
                    extracted.outputCount(),
                    extracted.reason()
            );
        }
        EchoInventoryOperationResult collected = inventory.collectItemStack(
                new EchoItemStack(outputDefinition, extracted.extractedQuantity()),
                hotbar
        );
        materializeMachineBlockEntities();
        return new EchoClientMachineOutputResult(
                collected.success() || collected.quantity() > 0,
                extracted.machineId(),
                COMPRESSED_SCRAP_ITEM_ID,
                collected.quantity(),
                inventory.itemCount(COMPRESSED_SCRAP_ITEM_ID),
                extracted.outputCount(),
                collected.success() ? extracted.reason() : collected.reason()
        );
    }

    EchoClientMachineRecipeSelectionResult selectMachineRecipe(String machineRecipeTargetId) {
        reconcileMachineBlockEntitiesFromWorld();
        EchoClientMachineRuntime.MachineRecipeSelectionResult selected =
                machines.selectRecipe(machineRecipeTargetId);
        materializeMachineBlockEntities();
        return new EchoClientMachineRecipeSelectionResult(
                selected.success(),
                selected.machineId(),
                selected.recipeId(),
                selected.selectedRecipeId(),
                selected.changed(),
                selected.reason()
        );
    }

    EchoClientEntitySpawnSummary entitySpawnSummary() {
        return entities.spawnSummary();
    }

    EchoClientEntityAiSummary entityAiSummary() {
        return entities.aiSummary();
    }

    int cachedChunkCount() {
        return worldRuntime.cachedChunkCount();
    }

    int cachedProtectedChunkCount() {
        return worldRuntime.cachedProtectedChunkCount();
    }

    EchoVoxelWorld persistedWorld() {
        return worldRuntime.persistedWorld();
    }

    EchoClientPlayerVitals playerVitals() {
        return playerRuntime.vitals();
    }

    EchoClientPlayerCombatState playerCombatState() {
        return playerRuntime.combatState();
    }

    EchoClientProgressionState progressionState() {
        return playerRuntime.progressionState();
    }

    EchoClientHazardState hazardState() {
        return playerRuntime.hazardState();
    }

    EchoClientHazardCatalog hazardCatalog() {
        return worldRuntime.hazardCatalog();
    }

    EchoClientToolState toolState() {
        return playerRuntime.toolState();
    }

    void applyInventorySnapshot(List<EchoClientInventorySlotSnapshot> inventorySlots) {
        inventory.applyInventorySnapshot(inventorySlots, hotbar);
    }

    void applyContainerSnapshot(List<EchoClientInventorySlotSnapshot> containerSlots) {
        inventory.applyContainerSnapshot(containerSlots);
    }

    void applyDroppedItemSnapshots(List<EchoClientDroppedItemSnapshot> snapshots) {
        droppedItems.applySnapshots(snapshots);
    }

    void applyEntitySnapshots(List<EchoClientEntitySnapshot> snapshots) {
        entities.applySnapshots(snapshots);
    }

    void applyMachineStateSnapshot(EchoClientMachineStateSnapshot snapshot) {
        machines = EchoClientMachineRuntime.restore(
                snapshot == null ? EchoClientMachineStateSnapshot.reference() : snapshot
        );
    }

    void restorePlayerRuntime(
            EchoClientPlayerVitals playerVitals,
            EchoClientPlayerCombatState playerCombatState,
            EchoClientProgressionState progressionState,
            EchoClientHazardState hazardState,
            EchoClientToolState toolState
    ) {
        playerRuntime = new EchoClientPlayerRuntime(
                playerVitals,
                playerCombatState,
                progressionState,
                hazardState,
                toolState
        );
    }

    EchoClientToolStatus selectedToolStatus(EchoVoxelBlock targetBlock) {
        return inventory.selectedItemDefinition(hotbar)
                .map(definition -> playerRuntime.selectedToolStatus(definition, targetBlock))
                .orElse(EchoClientToolStatus.hand());
    }

    double selectedMiningSpeed(EchoVoxelBlock targetBlock) {
        return selectedToolStatus(targetBlock).miningSpeed();
    }

    EchoClientGameMode gameMode() {
        return playerRuntime.gameMode();
    }

    void setGameMode(EchoClientGameMode gameMode) {
        playerRuntime.setGameMode(gameMode);
    }

    EchoClientPlayerVitals damagePlayer(int damage) {
        return playerRuntime.damagePlayer(damage);
    }

    EchoClientPlayerVitals damagePlayer(EchoClientDamageSource source, int damage) {
        return playerRuntime.damagePlayer(source, damage);
    }

    EchoClientPlayerVitals healPlayer(int amount) {
        return playerRuntime.healPlayer(amount);
    }

    EchoClientProgressionState awardExperience(int amount, String milestone) {
        return playerRuntime.awardExperience(amount, milestone);
    }

    EchoClientProgressionState awardBlockBreakExperience(EchoVoxelBlock block) {
        return playerRuntime.awardBlockBreakExperience(block);
    }

    void recordBlockBroken(EchoVoxelBlock block) {
        EchoVoxelBlock safeBlock = block == null ? EchoVoxelBlock.AIR : block;
        if (safeBlock.air()) {
            return;
        }
        if (playerRuntime.gameMode().consumesPlacedItems()) {
            collectBlockDropsOrSpawnOverflow(safeBlock);
        }
        if (playerRuntime.gameMode().ticksSurvival()) {
            awardBlockBreakExperience(safeBlock);
            damageSelectedTool(safeBlock);
        }
        inventory.syncHotbarFromInventory(hotbar);
        if (EchoClientMachineRuntime.machineBlockDefinition(safeBlock).isPresent()) {
            reconcileMachineBlockEntitiesFromWorld();
            materializeMachineBlockEntities();
        }
    }

    EchoClientEntityAttackResult attackLookedAtEntity(EchoVoxelHit blockingTarget) {
        if (!playerRuntime.gameMode().ticksSurvival()) {
            return EchoClientEntityAttackResult.miss("game_mode");
        }
        double reach = player.state().reach();
        if (blockingTarget != null && !blockingTarget.block().air()) {
            reach = Math.min(reach, blockingTarget.distance() + 0.05D);
        }
        EchoItemDefinition selectedDefinition = inventory.selectedItemDefinition(hotbar).orElse(null);
        EchoClientToolStatus toolStatus = selectedDefinition == null
                ? EchoClientToolStatus.hand()
                : playerRuntime.selectedToolStatus(selectedDefinition, EchoVoxelBlock.AIR);
        int damage = EchoClientGameSimulationRules.entityAttackDamage(selectedDefinition, toolStatus);
        EchoClientEntityAttackResult result = entities.attackNearest(player.state(), reach, damage);
        if (!result.hit()) {
            return result;
        }

        damageSelectedTool(1);
        applyEntityKillRewards(result);
        inventory.syncHotbarFromInventory(hotbar);
        return result;
    }

    EchoClientProjectileResult fireOffhandProjectileAtLookedAtEntity(EchoVoxelHit blockingTarget) {
        if (!playerRuntime.gameMode().ticksSurvival()) {
            return EchoClientProjectileResult.miss("game_mode");
        }
        EchoItemStack projectile = playerRuntime.combatState().equipment().offhand().orElse(null);
        if (projectile == null || !EchoClientGameSimulationRules.isProjectileAmmo(projectile.definition())) {
            return EchoClientProjectileResult.miss("no_projectile");
        }

        double reach = 18.0D;
        if (blockingTarget != null && !blockingTarget.block().air()) {
            reach = Math.min(reach, blockingTarget.distance() + 0.05D);
        }
        int damage = EchoClientGameSimulationRules.projectileDamage(projectile.definition());
        EchoClientDamageSource source =
                EchoClientDamageSource.projectile(projectile.itemId().value(), "player");
        EchoClientEntityAttackResult attack = entities.attackNearestProjectile(player.state(), reach, damage);
        int beforeCount = projectile.quantity();
        consumeOffhandStack(1);
        int afterCount = playerRuntime.combatState().equipment().offhand()
                .map(EchoItemStack::quantity)
                .orElse(0);
        if (attack.killed()) {
            applyEntityKillRewards(attack);
        }
        inventory.syncHotbarFromInventory(hotbar);
        return new EchoClientProjectileResult(
                true,
                beforeCount != afterCount,
                projectile.itemId().value(),
                beforeCount,
                afterCount,
                source.id(),
                attack,
                attack.reason()
        );
    }

    EchoClientEntityInteractionResult interactLookedAtEntity(EchoVoxelHit blockingTarget) {
        double reach = player.state().reach();
        if (blockingTarget != null && !blockingTarget.block().air()) {
            reach = Math.min(reach, blockingTarget.distance() + 0.05D);
        }
        return entities.interactNearest(player.state(), reach);
    }

    private void applyEntityKillRewards(EchoClientEntityAttackResult result) {
        if (result == null || !result.killed()) {
            return;
        }
        int lootQuantity = EchoClientGameSimulationRules.entityDeathLootQuantity(result);
        if (lootQuantity > 0) {
            droppedItems.drop(
                    new EchoItemStack(scrapMetalDefinition(), lootQuantity),
                    result.position().x() + 0.5D,
                    result.position().y() + 0.25D,
                    result.position().z() + 0.5D
            );
        }
        int experience = EchoClientGameSimulationRules.entityKillExperience(result);
        if (experience > 0) {
            awardExperience(experience, "kill:" + result.definitionId());
        }
    }

    EchoClientHazardState tickBiomeHazards(double deltaSeconds) {
        if (!playerRuntime.gameMode().ticksSurvival()) {
            return playerRuntime.hazardState();
        }
        EchoClientWorldRuntime.EchoClientBiomeHazardResult tick =
                worldRuntime.tickBiomeHazards(playerRuntime.hazardState(), player.state(), deltaSeconds);
        return playerRuntime.applyHazardTick(tick);
    }

    EchoClientPlayerVitals tickPlayerSurvival(double deltaSeconds, EchoVoxelPlayerInput input) {
        return playerRuntime.tickSurvival(deltaSeconds, input);
    }

    boolean consumeSelectedConsumable() {
        EchoClientInventoryRuntime.ConsumableUseResult result =
                inventory.consumeSelectedConsumable(hotbar, playerRuntime.vitals());
        playerRuntime.applyConsumableUse(result);
        return result.consumed();
    }

    boolean equipSelectedArmor() {
        EchoClientInventoryRuntime.ArmorEquipResult result =
                inventory.equipSelectedArmor(hotbar, playerRuntime.combatState().equipment());
        playerRuntime.applyArmorEquip(result);
        return result.equipped();
    }

    boolean spawnSelectedEntity(EchoVoxelHit target) {
        if (!playerRuntime.gameMode().allowsBlockPlacing()) {
            return false;
        }
        EchoItemDefinition selectedDefinition = inventory.selectedItemDefinition(hotbar).orElse(null);
        String entityId = EchoClientGameSimulationRules.spawnEggEntityId(selectedDefinition).orElse("");
        if (entityId.isBlank()) {
            return false;
        }
        EchoEntityDefinition entityDefinition = entityCatalog.definition(entityId).orElse(null);
        if (entityDefinition == null) {
            return false;
        }
        EchoWorldPosition position = spawnEggPosition(target);
        if (position == null || entities.spawn(entityDefinition, position) == null) {
            return false;
        }
        if (playerRuntime.gameMode().consumesPlacedItems()) {
            inventory.removeSelectedItemStack(hotbar, 1);
        } else {
            inventory.syncHotbarFromInventory(hotbar);
        }
        return true;
    }

    EchoClientFluidBucketUse useSelectedFluidBucket(EchoVoxelHit target) {
        if (target == null || !playerRuntime.gameMode().allowsBlockPlacing()) {
            return EchoClientFluidBucketUse.none("missing_target");
        }
        EchoItemDefinition selectedDefinition = inventory.selectedItemDefinition(hotbar).orElse(null);
        if (selectedDefinition == null) {
            return EchoClientFluidBucketUse.none("empty_selected_slot");
        }
        if (selectedDefinition.id().value().equals(EMPTY_BUCKET_ITEM_ID)) {
            return collectFluidWithBucket(target);
        }
        Optional<EchoVoxelFluidType> bucketFluid = bucketFluid(selectedDefinition);
        if (bucketFluid.isPresent()) {
            return placeFluidFromBucket(target, selectedDefinition, bucketFluid.orElseThrow());
        }
        return EchoClientFluidBucketUse.none("not_a_fluid_bucket");
    }

    void respawnPlayer() {
        player = EchoVoxelPlayerController.spawnAt(
                world(),
                world().spawnX(),
                world().spawnZ(),
                world().spawnYawDegrees(),
                -32.0D
        );
        playerRuntime.resetForRespawn();
        entities.clear();
    }

    EchoInventoryContainer playerInventory() {
        return inventory.playerInventory();
    }

    EchoInventoryContainer openContainer() {
        return inventory.openContainer();
    }

    List<EchoClientInventorySlotSnapshot> inventorySnapshots() {
        return inventory.inventorySnapshots();
    }

    List<EchoClientInventorySlotSnapshot> containerSnapshots() {
        return inventory.containerSnapshots();
    }

    EchoClientSavedSessionSnapshot savedSessionSnapshot() {
        return new EchoClientSavedSessionSnapshot(
                new EchoClientGameplay.GameplaySnapshot(persistedWorld(), player.state(), hotbar),
                inventorySnapshots(),
                containerSnapshots(),
                playerRuntime.vitals(),
                playerRuntime.combatState(),
                playerRuntime.progressionState(),
                playerRuntime.hazardState(),
                playerRuntime.toolState(),
                entitySnapshots(),
                droppedItemSnapshots(),
                machineStateSnapshot()
        );
    }

    EchoClientInventoryScreenModel inventoryScreenModel() {
        return inventory.inventoryScreenModel(hotbar, playerRuntime.toolState());
    }

    EchoClientInventoryScreenModel containerScreenModel() {
        return inventory.containerScreenModel(playerRuntime.toolState());
    }

    EchoClientEquipmentScreenModel equipmentScreenModel() {
        return EchoClientEquipmentScreenModel.fromEquipment(playerRuntime.combatState().equipment());
    }

    List<EchoClientWorkbenchRecipeSummary> workbenchRecipeSummaries() {
        return inventory.workbenchRecipeSummaries();
    }

    EchoClientWorkbenchScreenModel workbenchScreenModel(String selectedRecipeId) {
        return inventory.workbenchScreenModel(selectedRecipeId);
    }

    EchoItemCraftResult craftWorkbenchRecipe(String recipeId) {
        EchoClientInventoryRuntime.WorkbenchCraftResult craft = inventory.craftWorkbenchRecipe(recipeId, hotbar);
        if (craft.result().crafted()) {
            awardExperience(craft.experience(), craft.milestone());
        }
        return craft.result();
    }

    EchoClientDroppedItem dropBlockItem(EchoVoxelBlock block) {
        List<EchoClientDroppedItem> drops = dropBlockItems(
                block,
                player.state().x(),
                player.state().y(),
                player.state().z()
        );
        return drops.isEmpty() ? null : drops.getFirst();
    }

    EchoClientDroppedItem dropBlockItem(EchoVoxelBlock block, double x, double y, double z) {
        List<EchoClientDroppedItem> drops = dropBlockItems(block, x, y, z);
        return drops.isEmpty() ? null : drops.getFirst();
    }

    EchoClientDroppedItem dropSelectedItem() {
        return dropSelectedItem(1);
    }

    EchoClientDroppedItem dropSelectedItem(int quantity) {
        EchoItemStack stack = inventory.removeSelectedItemStack(hotbar, quantity).orElse(null);
        return dropItemStackNearPlayer(stack);
    }

    EchoClientDroppedItem dropCursorStack(int quantity) {
        return dropItemStackNearPlayer(inventory.removeCursorStack(quantity).orElse(null));
    }

    EchoClientDroppedItem dropInventorySlotStack(int slotIndex, int quantity) {
        return dropItemStackNearPlayer(inventory.removeInventorySlotStack(slotIndex, quantity, hotbar).orElse(null));
    }

    EchoClientDroppedItem dropContainerSlotStack(int slotIndex, int quantity) {
        return dropItemStackNearPlayer(inventory.removeContainerSlotStack(slotIndex, quantity).orElse(null));
    }

    EchoClientDroppedItem dropEquipmentSlot(EchoClientArmorSlot armorSlot) {
        EchoClientEquipmentState equipment = playerRuntime.combatState().equipment();
        EchoClientArmorPiece piece = equipment.piece(armorSlot).orElse(null);
        if (piece == null) {
            return null;
        }
        playerRuntime.setEquipment(equipment.unequip(armorSlot));
        return dropItemStackNearPlayer(piece.toStack());
    }

    EchoClientDroppedItem dropOffhandStack(int quantity) {
        if (quantity <= 0) {
            return null;
        }
        EchoClientEquipmentState equipment = playerRuntime.combatState().equipment();
        EchoItemStack offhand = equipment.offhand().orElse(null);
        if (offhand == null) {
            return null;
        }
        int removed = Math.min(quantity, offhand.quantity());
        EchoItemStack removedStack = new EchoItemStack(offhand.definition(), removed);
        EchoClientEquipmentState nextEquipment = offhand.remove(removed)
                .map(equipment::withOffhand)
                .orElseGet(equipment::withoutOffhand);
        playerRuntime.setEquipment(nextEquipment);
        return dropItemStackNearPlayer(removedStack);
    }

    private EchoItemStack consumeOffhandStack(int quantity) {
        if (quantity <= 0) {
            return null;
        }
        EchoClientEquipmentState equipment = playerRuntime.combatState().equipment();
        EchoItemStack offhand = equipment.offhand().orElse(null);
        if (offhand == null) {
            return null;
        }
        int removed = Math.min(quantity, offhand.quantity());
        EchoItemStack removedStack = new EchoItemStack(offhand.definition(), removed);
        EchoClientEquipmentState nextEquipment = offhand.remove(removed)
                .map(equipment::withOffhand)
                .orElseGet(equipment::withoutOffhand);
        playerRuntime.setEquipment(nextEquipment);
        return removedStack;
    }

    List<EchoClientDroppedItem> dropBlockItems(EchoVoxelBlock block) {
        return dropBlockItems(
                block,
                player.state().x(),
                player.state().y(),
                player.state().z()
        );
    }

    List<EchoClientDroppedItem> dropBlockItems(EchoVoxelBlock block, double x, double y, double z) {
        ArrayList<EchoClientDroppedItem> drops = new ArrayList<>();
        for (EchoItemStack stack : inventory.blockDropStacks(block)) {
            EchoClientDroppedItem drop = droppedItems.drop(stack, x, y, z);
            if (drop != null) {
                drops.add(drop);
            }
        }
        return List.copyOf(drops);
    }

    private void collectBlockDropsOrSpawnOverflow(EchoVoxelBlock block) {
        for (EchoItemStack stack : inventory.blockDropStacks(block)) {
            EchoInventoryOperationResult collected = inventory.collectItemStack(stack);
            int remaining = stack.quantity() - collected.quantity();
            if (remaining > 0) {
                dropItemStackNearPlayer(stack.withQuantity(remaining));
            }
        }
    }

    private EchoClientDroppedItem dropItemStackNearPlayer(EchoItemStack stack) {
        if (stack == null) {
            return null;
        }
        var state = player.state();
        double yawRadians = Math.toRadians(state.yawDegrees());
        double pitchRadians = Math.toRadians(state.pitchDegrees());
        double forwardX = Math.sin(yawRadians) * Math.cos(pitchRadians);
        double forwardY = Math.sin(pitchRadians);
        double forwardZ = Math.cos(yawRadians) * Math.cos(pitchRadians);
        return droppedItems.drop(
                stack,
                state.x() + forwardX * 0.65D,
                state.eyeY() + forwardY * 0.35D - 0.25D,
                state.z() + forwardZ * 0.65D
        );
    }

    EchoClientDroppedItemRuntime.PickupResult pickupNearbyDroppedItems() {
        return pickupNearbyDroppedItems(0.0D);
    }

    EchoClientDroppedItemRuntime.PickupResult pickupNearbyDroppedItems(double minimumAgeSeconds) {
        EchoClientDroppedItemRuntime.PickupResult result = droppedItems.pickupNearby(
                this::collectDroppedItemStack,
                player.state().x(),
                player.state().y(),
                player.state().z(),
                1.75D,
                minimumAgeSeconds
        );
        if (result.pickedQuantity() > 0) {
            inventory.syncHotbarFromInventory(hotbar);
        }
        return result;
    }

    boolean inventorySlotEmpty(int slotIndex) {
        return inventory.inventorySlotEmpty(slotIndex);
    }

    boolean containerSlotEmpty(int slotIndex) {
        return inventory.containerSlotEmpty(slotIndex);
    }

    boolean cursorStackHeld() {
        return inventory.cursorStackHeld();
    }

    EchoClientSlotStack cursorSlotStack() {
        return inventory.cursorSlotStack(playerRuntime.toolState());
    }

    boolean primaryClickInventorySlot(int slotIndex) {
        return inventory.primaryClickInventorySlot(slotIndex, hotbar);
    }

    boolean secondaryClickInventorySlot(int slotIndex) {
        return inventory.secondaryClickInventorySlot(slotIndex, hotbar);
    }

    boolean primaryClickContainerSlot(int slotIndex) {
        return inventory.primaryClickContainerSlot(slotIndex);
    }

    boolean secondaryClickContainerSlot(int slotIndex) {
        return inventory.secondaryClickContainerSlot(slotIndex);
    }

    boolean returnCursorStackToInventory() {
        return inventory.returnCursorStackToInventory(hotbar);
    }

    boolean clickEquipmentSlot(EchoClientArmorSlot armorSlot) {
        EchoClientInventoryRuntime.EquipmentSlotClickResult result =
                inventory.clickEquipmentSlot(armorSlot, playerRuntime.combatState().equipment(), hotbar);
        if (result.changed()) {
            playerRuntime.setEquipment(result.equipment());
        }
        return result.changed();
    }

    boolean primaryClickOffhandSlot() {
        EchoClientInventoryRuntime.EquipmentSlotClickResult result =
                inventory.primaryClickOffhandSlot(playerRuntime.combatState().equipment(), hotbar);
        if (result.changed()) {
            playerRuntime.setEquipment(result.equipment());
        }
        return result.changed();
    }

    boolean secondaryClickOffhandSlot() {
        EchoClientInventoryRuntime.EquipmentSlotClickResult result =
                inventory.secondaryClickOffhandSlot(playerRuntime.combatState().equipment(), hotbar);
        if (result.changed()) {
            playerRuntime.setEquipment(result.equipment());
        }
        return result.changed();
    }

    boolean swapSelectedWithOffhand() {
        EchoClientInventoryRuntime.EquipmentSlotClickResult result =
                inventory.swapSelectedWithOffhand(hotbar, playerRuntime.combatState().equipment());
        if (result.changed()) {
            playerRuntime.setEquipment(result.equipment());
        }
        return result.changed();
    }

    EchoInventoryTransferResult moveOrMergeInventorySlot(int sourceSlot, int targetSlot) {
        return inventory.moveOrMergeInventorySlot(sourceSlot, targetSlot, hotbar);
    }

    EchoInventoryTransferResult splitInventorySlotTo(int sourceSlot, int targetSlot) {
        return inventory.splitInventorySlotTo(sourceSlot, targetSlot, hotbar);
    }

    EchoInventoryTransferResult moveOrMergeContainerSlot(int sourceSlot, int targetSlot) {
        return inventory.moveOrMergeContainerSlot(sourceSlot, targetSlot);
    }

    EchoInventoryTransferResult splitContainerSlotTo(int sourceSlot, int targetSlot) {
        return inventory.splitContainerSlotTo(sourceSlot, targetSlot);
    }

    EchoInventoryTransferResult quickMoveContainerSlotToPlayer(int sourceSlot) {
        return inventory.quickMoveContainerSlotToPlayer(sourceSlot, hotbar);
    }

    EchoInventoryTransferResult quickMoveInventorySlotToContainer(int sourceSlot) {
        return inventory.quickMoveInventorySlotToContainer(sourceSlot, hotbar);
    }

    EchoInventoryTransferResult swapContainerSlotWithHotbar(int sourceSlot, int hotbarSlot) {
        return inventory.swapContainerSlotWithHotbar(sourceSlot, hotbarSlot, hotbar);
    }

    EchoInventoryTransferResult quickMoveInventorySlot(int sourceSlot) {
        return inventory.quickMoveInventorySlot(sourceSlot, hotbar);
    }

    EchoInventoryTransferResult swapInventorySlots(int sourceSlot, int hotbarSlot) {
        return inventory.swapInventorySlots(sourceSlot, hotbarSlot, hotbar);
    }

    void updateFromGameplay(EchoClientGameplay gameplay) {
        worldRuntime.setWorld(gameplay.world());
        player = gameplay.player();
        hotbar = gameplay.hotbar();
        inventory.syncInventoryFromHotbar(hotbar);
    }

    EchoClientEntitySpawnSummary tickEntities(double deltaSeconds) {
        droppedItems.tick(deltaSeconds, world());
        EchoClientEntityRuntime.EntityTickResult result = entities.tick(world(), player.state(), deltaSeconds);
        if (result.hostileAttacks() > 0) {
            damagePlayer(EchoClientDamageSource.hostile(presentation.hostileDamageSourceId()),
                    result.hostileAttacks() * HOSTILE_ATTACK_DAMAGE);
        }
        return result.spawnSummary();
    }

    EchoClientWorldStreamResult streamAroundPlayer() {
        return streamAroundPlayer(EchoClientSettings.DEFAULT_CHUNK_VIEW_DISTANCE);
    }

    EchoClientWorldStreamResult streamAroundPlayer(int chunkViewDistance) {
        return worldRuntime.streamAroundPlayer(player.state(), chunkViewDistance);
    }

    EchoClientScreenRouteRequest worldInteractionRouteFor(EchoVoxelBlock block) {
        return interactionCatalog.routeFor(block);
    }

    EchoVoxelBlockState defaultBlockStateFor(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return EchoVoxelBlockState.AIR;
        }
        EchoVoxelBlockState state = EchoVoxelBlockState.of(block);
        for (Map.Entry<String, String> property : bridge.registry()
                .findLiveVoxelId(block.id())
                .map(entry -> entry.registryMetadata().defaultState())
                .orElse(Map.of())
                .entrySet()) {
            state = state.withProperty(property.getKey(), property.getValue());
        }
        java.util.Optional<EchoClientMachineRuntime.MachineBlockDefinition> machineBlock =
                EchoClientMachineRuntime.machineBlockDefinition(block);
        if (machineBlock.isPresent()) {
            EchoClientMachineRuntime.MachineBlockDefinition definition = machineBlock.orElseThrow();
            state = state.withProperty("source", "machine_block_entity")
                    .withProperty("blockEntityId", definition.entityId())
                    .withProperty("canonicalId", definition.entityId())
                    .withProperty("machineKind", definition.kind());
        }
        return state;
    }

    private EchoWorldPosition spawnEggPosition(EchoVoxelHit target) {
        EchoWorldPosition targeted = spawnEggTargetPosition(target);
        if (targeted != null) {
            return targeted;
        }
        return spawnEggForwardPosition();
    }

    private EchoClientFluidBucketUse collectFluidWithBucket(EchoVoxelHit target) {
        EchoVoxelBlockState targetState = world().blockStateAt(target.x(), target.y(), target.z());
        Optional<EchoVoxelFluidType> fluid = EchoVoxelFluidRuntime.fluidType(targetState);
        if (fluid.isEmpty() || EchoVoxelFluidRuntime.fluidLevel(targetState) != 0) {
            return EchoClientFluidBucketUse.none("not_a_source_fluid");
        }
        EchoItemDefinition fullBucket = fluidBucketDefinition(fluid.orElseThrow());
        boolean consumes = playerRuntime.gameMode().consumesPlacedItems();
        if (consumes && !inventory.canReplaceSelectedItem(hotbar, fullBucket)) {
            return EchoClientFluidBucketUse.none("inventory_full");
        }
        EchoVoxelBlockState drained = EchoVoxelFluidRuntime.drainedState(targetState);
        if (!world().setBlockStateAt(target.x(), target.y(), target.z(), drained)) {
            return EchoClientFluidBucketUse.none("outside_loaded_chunk");
        }
        if (consumes && !inventory.replaceSelectedItem(hotbar, fullBucket)) {
            world().setBlockStateAt(target.x(), target.y(), target.z(), targetState);
            return EchoClientFluidBucketUse.none("inventory_replace_failed");
        }
        return EchoClientFluidBucketUse.collected(
                fullBucket.displayName(),
                target.x(),
                target.y(),
                target.z(),
                targetState
        );
    }

    private EchoClientFluidBucketUse placeFluidFromBucket(
            EchoVoxelHit target,
            EchoItemDefinition selectedDefinition,
            EchoVoxelFluidType fluid
    ) {
        EchoVoxelBlockState targetState = world().blockStateAt(target.x(), target.y(), target.z());
        boolean waterlogTarget = EchoVoxelFluidRuntime.isWaterloggable(targetState)
                && !EchoVoxelFluidRuntime.isFluid(targetState);
        int x = waterlogTarget ? target.x() : target.x() + target.normalX();
        int y = waterlogTarget ? target.y() : target.y() + target.normalY();
        int z = waterlogTarget ? target.z() : target.z() + target.normalZ();
        EchoVoxelBlockState previous = world().blockStateAt(x, y, z);
        if (!previous.air() && !EchoVoxelFluidRuntime.isFluid(previous)
                && !EchoVoxelFluidRuntime.isWaterloggable(previous)) {
            return EchoClientFluidBucketUse.none("blocked_by_solid");
        }
        EchoItemDefinition emptyBucket = emptyBucketDefinition();
        boolean consumes = playerRuntime.gameMode().consumesPlacedItems();
        if (consumes && !inventory.canReplaceSelectedItem(hotbar, emptyBucket)) {
            return EchoClientFluidBucketUse.none("inventory_full");
        }
        EchoVoxelFluidRuntime.EchoVoxelFluidPlacement placement =
                new EchoVoxelFluidRuntime().placeSource(world(), fluid, x, y, z);
        if (!placement.placed()) {
            return EchoClientFluidBucketUse.none(placement.reason());
        }
        EchoVoxelBlockState placedState = world().blockStateAt(x, y, z);
        if (consumes && !inventory.replaceSelectedItem(hotbar, emptyBucket)) {
            world().setBlockStateAt(x, y, z, previous);
            return EchoClientFluidBucketUse.none("inventory_replace_failed");
        }
        return EchoClientFluidBucketUse.placed(selectedDefinition.displayName(), x, y, z, placedState);
    }

    private static boolean isBucketItem(EchoItemDefinition definition, String itemId) {
        return definition != null && definition.id().value().equals(itemId);
    }

    private static Optional<EchoVoxelFluidType> bucketFluid(EchoItemDefinition definition) {
        if (isBucketItem(definition, WATER_BUCKET_ITEM_ID)) {
            return Optional.of(EchoVoxelFluidType.WATER);
        }
        if (isBucketItem(definition, LAVA_BUCKET_ITEM_ID)) {
            return Optional.of(EchoVoxelFluidType.LAVA);
        }
        return Optional.empty();
    }

    static EchoItemDefinition emptyBucketDefinition() {
        return new EchoItemDefinition(
                new EchoItemId(EMPTY_BUCKET_ITEM_ID),
                "Bucket",
                EchoItemCategory.TOOL,
                16,
                1.0D,
                List.of("bucket", "fluid_container"),
                List.of("Collects source fluids")
        );
    }

    static EchoItemDefinition waterBucketDefinition() {
        return fluidBucketDefinition(EchoVoxelFluidType.WATER);
    }

    static EchoItemDefinition lavaBucketDefinition() {
        return fluidBucketDefinition(EchoVoxelFluidType.LAVA);
    }

    private static EchoItemDefinition fluidBucketDefinition(EchoVoxelFluidType fluid) {
        String id = fluid == EchoVoxelFluidType.LAVA ? LAVA_BUCKET_ITEM_ID : WATER_BUCKET_ITEM_ID;
        String label = fluid == EchoVoxelFluidType.LAVA ? "Lava Bucket" : "Water Bucket";
        return new EchoItemDefinition(
                new EchoItemId(id),
                label,
                EchoItemCategory.TOOL,
                1,
                1.0D,
                List.of("bucket", "fluid_bucket", fluid.id()),
                List.of("Places " + fluid.id() + " source blocks")
        );
    }

    private EchoWorldPosition spawnEggTargetPosition(EchoVoxelHit target) {
        if (target == null || target.block().air()) {
            return null;
        }
        int x = target.x() + target.normalX();
        int z = target.z() + target.normalZ();
        int y = target.normalY() > 0
                ? target.y() + 1
                : EchoClientEntityAi.surfaceSpawnY(world(), x, z);
        EchoWorldPosition position = new EchoWorldPosition(x, y, z);
        return validSpawnEggPosition(position) ? position : null;
    }

    private EchoWorldPosition spawnEggForwardPosition() {
        var state = player.state();
        double yawRadians = Math.toRadians(state.yawDegrees());
        double forwardX = Math.sin(yawRadians);
        double forwardZ = Math.cos(yawRadians);
        for (int distance = 2; distance <= 4; distance++) {
            int x = (int) Math.floor(state.x() + forwardX * distance);
            int z = (int) Math.floor(state.z() + forwardZ * distance);
            int y = EchoClientEntityAi.surfaceSpawnY(world(), x, z);
            EchoWorldPosition position = new EchoWorldPosition(x, y, z);
            if (validSpawnEggPosition(position)) {
                return position;
            }
        }
        return null;
    }

    private boolean validSpawnEggPosition(EchoWorldPosition position) {
        if (position == null || position.y() < 0 || position.y() + 1 >= world().chunkSize()) {
            return false;
        }
        return world().blockStateAt(position.x(), position.y(), position.z()).air()
                && world().blockStateAt(position.x(), position.y() + 1, position.z()).air();
    }

    private void damageSelectedTool(EchoVoxelBlock block) {
        playerRuntime.setToolState(inventory.damageSelectedTool(hotbar, playerRuntime.toolState(), block));
    }

    private void damageSelectedTool(int wear) {
        playerRuntime.setToolState(inventory.damageSelectedTool(hotbar, playerRuntime.toolState(), wear));
    }

    private EchoInventoryOperationResult collectDroppedItemStack(EchoItemStack stack) {
        return inventory.collectItemStack(stack);
    }

    private static EchoItemDefinition compressedScrapDefinition() {
        return new EchoItemDefinition(
                new EchoItemId(COMPRESSED_SCRAP_ITEM_ID),
                "Compressed Scrap",
                EchoItemCategory.MATERIAL,
                EchoVoxelPlayerHotbar.MAX_STACK,
                1.0D,
                List.of("crafting", "machine_output", "salvage"),
                List.of("Recovered from standalone machine output")
        );
    }

    private static EchoItemDefinition scrapMetalDefinition() {
        return new EchoItemDefinition(
                new EchoItemId(SCRAP_METAL_ITEM_ID),
                "Scrap Metal",
                EchoItemCategory.MATERIAL,
                EchoVoxelPlayerHotbar.MAX_STACK,
                1.0D,
                List.of("crafting", "salvage", "entity_drop"),
                List.of("Recovered from hostile scrap and Ashfall debris")
        );
    }
}
