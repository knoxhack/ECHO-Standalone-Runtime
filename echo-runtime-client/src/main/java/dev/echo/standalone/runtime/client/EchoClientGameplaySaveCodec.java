package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.entity.EchoEntityAiState;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockState;
import dev.echo.standalone.runtime.world.EchoVoxelChunk;
import dev.echo.standalone.runtime.world.EchoVoxelChunkId;
import dev.echo.standalone.runtime.world.EchoVoxelBiomeSources;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class EchoClientGameplaySaveCodec {
    static final String WORLD_PATH = "client/world.properties";
    static final String PLAYER_PATH = "client/player.properties";
    static final String HOTBAR_PATH = "client/hotbar.tsv";
    static final String INVENTORY_PATH = "client/inventory.tsv";
    static final String CONTAINER_PATH = "client/container.tsv";
    static final String CHUNKS_PATH = "client/chunks.tsv";
    static final String VITALS_PATH = "client/vitals.properties";
    static final String COMBAT_PATH = "client/combat.properties";
    static final String EQUIPMENT_PATH = "client/equipment.tsv";
    static final String OFFHAND_PATH = "client/offhand.tsv";
    static final String PROGRESSION_PATH = "client/progression.properties";
    static final String HAZARDS_PATH = "client/hazards.properties";
    static final String TOOLS_PATH = "client/tools.tsv";
    static final String ENTITIES_PATH = "client/entities.tsv";
    static final String DROPPED_ITEMS_PATH = "client/dropped_items.tsv";
    static final String MACHINES_PATH = "client/machines.tsv";
    static final String BLOCK_ENTITIES_PATH = "client/block_entities.tsv";
    static final String SESSION_PATH = "client/session.properties";
    static final String RUNTIME_CONTENT_PATH = "client/runtime_content.json";

    private EchoClientGameplaySaveCodec() {
    }

    static void writeSession(
            EchoSaveRuntimeResult saves,
            EchoClientWorldSession worldSession,
            String transactionId,
            String reason
    ) throws IOException {
        writeSession(saves, worldSession, transactionId, reason, List.of());
    }

    static void writeSession(
            EchoSaveRuntimeResult saves,
            EchoClientWorldSession worldSession,
            String transactionId,
            String reason,
            List<Map<String, Object>> runtimeContentRows
    ) throws IOException {
        writeSession(saves, worldSession, transactionId, reason, runtimeContentRows, Map.of());
    }

    static void writeSession(
            EchoSaveRuntimeResult saves,
            EchoClientWorldSession worldSession,
            String transactionId,
            String reason,
            List<Map<String, Object>> runtimeContentRows,
            Map<String, String> environmentMetadata
    ) throws IOException {
        EchoClientGameSession session = worldSession.gameSession();
        session.reconcileMachineBlockEntitiesFromWorld();
        session.materializeMachineBlockEntities();
        EchoVoxelWorld saveWorld = session.persistedWorld();
        List<Map<String, Object>> runtimeRows = safeRuntimeRows(runtimeContentRows);
        String runtimeFingerprint = EchoClientRuntimeContentFingerprint.fingerprint(runtimeRows);
        EchoSaveTransaction transaction = saves.beginTransaction(worldSession.slotId(), transactionId);
        transaction.writeText(WORLD_PATH, worldText(saveWorld));
        transaction.writeText(PLAYER_PATH, playerText(session.player().state()));
        transaction.writeText(HOTBAR_PATH, hotbarText(session.hotbar()));
        transaction.writeText(INVENTORY_PATH, inventoryText(session.inventorySnapshots()));
        transaction.writeText(CONTAINER_PATH, inventoryText(session.containerSnapshots()));
        transaction.writeText(CHUNKS_PATH, chunksText(saveWorld));
        transaction.writeText(VITALS_PATH, vitalsText(session.playerVitals()));
        transaction.writeText(COMBAT_PATH, combatText(session.playerCombatState()));
        transaction.writeText(EQUIPMENT_PATH, equipmentText(session.playerCombatState().equipment()));
        transaction.writeText(OFFHAND_PATH, offhandText(session.playerCombatState().equipment()));
        transaction.writeText(PROGRESSION_PATH, progressionText(session.progressionState()));
        transaction.writeText(HAZARDS_PATH, hazardText(session.hazardState()));
        transaction.writeText(TOOLS_PATH, toolsText(session.toolState()));
        transaction.writeText(ENTITIES_PATH, entitiesText(session.entitySnapshots()));
        transaction.writeText(DROPPED_ITEMS_PATH, droppedItemsText(session.droppedItemSnapshots()));
        transaction.writeText(BLOCK_ENTITIES_PATH, blockEntitiesText(session.machineStateSnapshot()));
        transaction.writeText(SESSION_PATH, sessionText(worldSession, session, saveWorld, reason));
        transaction.writeText(RUNTIME_CONTENT_PATH, EchoClientRuntimeContentSaveCodec.writeRows(runtimeRows));
        EchoClientSaveSlotThumbnailGenerator.Snapshot thumbnail =
                EchoClientSaveSlotThumbnailGenerator.writeThumbnail(
                        transaction,
                        saveWorld,
                        session.player().state()
                );
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("displayName", worldSession.displayName());
        metadata.put("lastClientAction", reason);
        metadata.put("adapterCorePack", saves.profile().packId());
        metadata.put("screen", "echoscreencore:world_select");
        metadata.put("clientSaveCodec", "echo.client.gameplay.v1");
        metadata.put("clientInventoryCodec", "echo.client.inventory.v1");
        metadata.put("clientContainerCodec", "echo.client.container.v1");
        metadata.put("clientVitalsCodec", "echo.client.vitals.v2");
        metadata.put("clientCombatCodec", "echo.client.combat.v1");
        metadata.put("clientOffhandCodec", "echo.client.offhand.v1");
        metadata.put("clientProgressionCodec", "echo.client.progression.v1");
        metadata.put("clientHazardsCodec", "echo.client.hazards.v1");
        metadata.put("clientToolsCodec", "echo.client.tools.v1");
        metadata.put("clientEntitiesCodec", "echo.client.entities.v1");
        metadata.put("clientDroppedItemsCodec", "echo.client.dropped_items.v1");
        metadata.put("clientBlockEntitiesCodec", "echo.client.block_entities.v1");
        metadata.put("clientRuntimeContentCodec", "echo.client.runtime_content.v1");
        metadata.put("clientThumbnailCodec", EchoClientSaveSlotThumbnailGenerator.THUMBNAIL_CODEC);
        metadata.put("clientThumbnailPath", thumbnail.relativePath());
        metadata.put("clientThumbnailSource", thumbnail.source());
        metadata.put("clientThumbnailWidth", Integer.toString(thumbnail.width()));
        metadata.put("clientThumbnailHeight", Integer.toString(thumbnail.height()));
        metadata.put("clientThumbnailBiomeId", thumbnail.biomeId());
        metadata.put("clientThumbnailCameraX", Double.toString(thumbnail.cameraX()));
        metadata.put("clientThumbnailCameraY", Double.toString(thumbnail.cameraY()));
        metadata.put("clientThumbnailCameraZ", Double.toString(thumbnail.cameraZ()));
        metadata.put("clientThumbnailCameraYaw", Double.toString(thumbnail.cameraYawDegrees()));
        metadata.put("clientThumbnailCameraPitch", Double.toString(thumbnail.cameraPitchDegrees()));
        metadata.put("clientThumbnailSkyArgb", argbText(thumbnail.skyArgb()));
        metadata.put("clientThumbnailTerrainArgb", argbText(thumbnail.terrainArgb()));
        metadata.put("clientThumbnailAccentArgb", argbText(thumbnail.accentArgb()));
        metadata.put("clientThumbnailShadowArgb", argbText(thumbnail.shadowArgb()));
        metadata.put(
                EchoClientRuntimeContentFingerprint.ALGORITHM_METADATA_KEY,
                EchoClientRuntimeContentFingerprint.ALGORITHM
        );
        metadata.put(EchoClientRuntimeContentFingerprint.FINGERPRINT_METADATA_KEY, runtimeFingerprint);
        metadata.put("runtimeContentRows", Integer.toString(runtimeRows.size()));
        metadata.put("progressionLevel", Integer.toString(session.progressionState().level()));
        metadata.put("hazardExposure", Integer.toString(session.hazardState().exposurePercent()));
        metadata.putAll(safeStringMap(environmentMetadata));
        transaction.commit(metadata);
    }

    static boolean canRestore(EchoSaveManifest manifest) {
        return manifest.file(WORLD_PATH).isPresent()
                && manifest.file(PLAYER_PATH).isPresent()
                && manifest.file(HOTBAR_PATH).isPresent()
                && manifest.file(CHUNKS_PATH).isPresent();
    }

    static EchoClientSavedSessionSnapshot restoreSessionSnapshot(
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoSaveRuntimeResult saves,
            EchoSaveManifest manifest
    ) throws IOException {
        Path dataRoot = saves.profile().slot(manifest.slotId()).dataRoot();
        Map<String, String> worldValues = properties(Files.readString(dataRoot.resolve(WORLD_PATH)));
        EchoVoxelPlayerState player = player(properties(Files.readString(dataRoot.resolve(PLAYER_PATH))));
        EchoVoxelPlayerHotbar hotbar = hotbar(bridge, player.selectedSlot(), Files.readAllLines(dataRoot.resolve(HOTBAR_PATH)));
        List<EchoClientInventorySlotSnapshot> inventorySlots = manifest.file(INVENTORY_PATH).isPresent()
                ? inventory(Files.readAllLines(dataRoot.resolve(INVENTORY_PATH)))
                : List.of();
        List<EchoClientInventorySlotSnapshot> containerSlots = manifest.file(CONTAINER_PATH).isPresent()
                ? inventory(Files.readAllLines(dataRoot.resolve(CONTAINER_PATH)))
                : List.of();
        EchoClientPlayerVitals vitals = manifest.file(VITALS_PATH).isPresent()
                ? vitals(properties(Files.readString(dataRoot.resolve(VITALS_PATH))))
                : EchoClientPlayerVitals.full();
        EchoClientEquipmentState equipment = manifest.file(EQUIPMENT_PATH).isPresent()
                ? equipment(Files.readAllLines(dataRoot.resolve(EQUIPMENT_PATH)))
                : EchoClientEquipmentState.empty();
        if (manifest.file(OFFHAND_PATH).isPresent()) {
            EchoItemStack offhand = offhand(Files.readAllLines(dataRoot.resolve(OFFHAND_PATH)));
            if (offhand != null) {
                equipment = equipment.withOffhand(offhand);
            }
        }
        EchoClientPlayerCombatState combat = manifest.file(COMBAT_PATH).isPresent()
                ? combat(properties(Files.readString(dataRoot.resolve(COMBAT_PATH))), equipment)
                : new EchoClientPlayerCombatState(EchoClientGameMode.SURVIVAL, equipment, EchoClientDamageSource.none());
        EchoClientProgressionState progression = manifest.file(PROGRESSION_PATH).isPresent()
                ? progression(properties(Files.readString(dataRoot.resolve(PROGRESSION_PATH))))
                : EchoClientProgressionState.empty();
        EchoClientHazardState hazards = manifest.file(HAZARDS_PATH).isPresent()
                ? hazards(properties(Files.readString(dataRoot.resolve(HAZARDS_PATH))))
                : EchoClientHazardState.empty();
        EchoClientToolState tools = manifest.file(TOOLS_PATH).isPresent()
                ? tools(Files.readAllLines(dataRoot.resolve(TOOLS_PATH)))
                : EchoClientToolState.empty();
        List<EchoClientEntitySnapshot> entities = manifest.file(ENTITIES_PATH).isPresent()
                ? entities(Files.readAllLines(dataRoot.resolve(ENTITIES_PATH)))
                : List.of();
        List<EchoClientDroppedItemSnapshot> droppedItems = manifest.file(DROPPED_ITEMS_PATH).isPresent()
                ? droppedItems(Files.readAllLines(dataRoot.resolve(DROPPED_ITEMS_PATH)))
                : List.of();
        EchoClientMachineStateSnapshot machineState = manifest.file(BLOCK_ENTITIES_PATH).isPresent()
                ? blockEntities(Files.readAllLines(dataRoot.resolve(BLOCK_ENTITIES_PATH))).withStateReloaded(true)
                : manifest.file(MACHINES_PATH).isPresent()
                ? machines(Files.readAllLines(dataRoot.resolve(MACHINES_PATH))).withStateReloaded(true)
                : EchoClientMachineStateSnapshot.reference().withStateReloaded(true);
        EchoVoxelWorld world = world(bridge, worldValues, Files.readAllLines(dataRoot.resolve(CHUNKS_PATH)));
        return new EchoClientSavedSessionSnapshot(
                new EchoClientGameplay.GameplaySnapshot(world, player, hotbar),
                inventorySlots,
                containerSlots,
                vitals,
                combat,
                progression,
                hazards,
                tools,
                entities,
                droppedItems,
                machineState
        );
    }

    static List<Map<String, Object>> restoreRuntimeContentRegistrations(
            EchoSaveRuntimeResult saves,
            EchoSaveManifest manifest
    ) throws IOException {
        if (!manifest.file(RUNTIME_CONTENT_PATH).isPresent()) {
            return List.of();
        }
        Path dataRoot = saves.profile().slot(manifest.slotId()).dataRoot();
        return EchoClientRuntimeContentSaveCodec.readRows(Files.readString(dataRoot.resolve(RUNTIME_CONTENT_PATH)));
    }

    private static List<Map<String, Object>> safeRuntimeRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (row != null && !row.isEmpty()) {
                result.add(Map.copyOf(row));
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, String> safeStringMap(Map<String, String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static String argbText(int argb) {
        return "0x" + String.format(java.util.Locale.ROOT, "%08X", argb);
    }

    private static String worldText(EchoVoxelWorld world) {
        return "worldId=" + world.worldId() + "\n"
                + "seed=" + world.seed() + "\n"
                + "biomeSourceId=" + world.biomeSource().id() + "\n"
                + "chunkSize=" + world.chunkSize() + "\n"
                + "spawnX=" + world.spawnX() + "\n"
                + "spawnY=" + world.spawnY() + "\n"
                + "spawnZ=" + world.spawnZ() + "\n"
                + "spawnYawDegrees=" + world.spawnYawDegrees() + "\n";
    }

    private static String playerText(EchoVoxelPlayerState player) {
        return "x=" + player.x() + "\n"
                + "y=" + player.y() + "\n"
                + "z=" + player.z() + "\n"
                + "velocityY=" + player.velocityY() + "\n"
                + "yawDegrees=" + player.yawDegrees() + "\n"
                + "pitchDegrees=" + player.pitchDegrees() + "\n"
                + "grounded=" + player.grounded() + "\n"
                + "crouching=" + player.crouching() + "\n"
                + "sprinting=" + player.sprinting() + "\n"
                + "selectedSlot=" + player.selectedSlot() + "\n"
                + "reach=" + player.reach() + "\n";
    }

    private static String hotbarText(EchoVoxelPlayerHotbar hotbar) {
        StringBuilder builder = new StringBuilder("slot\tblock\tcount\n");
        for (EchoVoxelHotbarSlot slot : hotbar.slots()) {
            builder.append(slot.index()).append('\t')
                    .append(slot.empty() ? EchoVoxelBlock.AIR.id() : slot.block().id()).append('\t')
                    .append(slot.count()).append('\n');
        }
        return builder.toString();
    }

    private static String inventoryText(List<EchoClientInventorySlotSnapshot> slots) {
        StringBuilder builder = new StringBuilder("slot\titem\tlabel\tcategory\tmaxStack\tcount\n");
        for (EchoClientInventorySlotSnapshot slot : slots) {
            builder.append(slot.index()).append('\t')
                    .append(slot.itemId()).append('\t')
                    .append(escapeTab(slot.displayName())).append('\t')
                    .append(slot.category().name()).append('\t')
                    .append(slot.maxStackSize()).append('\t')
                    .append(slot.count()).append('\n');
        }
        return builder.toString();
    }

    private static String vitalsText(EchoClientPlayerVitals vitals) {
        return "currentHealth=" + vitals.currentHealth() + "\n"
                + "maxHealth=" + vitals.maxHealth() + "\n"
                + "lastDamage=" + vitals.lastDamage() + "\n"
                + "foodLevel=" + vitals.foodLevel() + "\n"
                + "saturation=" + vitals.saturation() + "\n"
                + "exhaustion=" + vitals.exhaustion() + "\n"
                + "survivalTickSeconds=" + vitals.survivalTickSeconds() + "\n";
    }

    private static String combatText(EchoClientPlayerCombatState combat) {
        EchoClientPlayerCombatState safeCombat =
                combat == null ? EchoClientPlayerCombatState.defaults() : combat;
        EchoClientDamageSource source = safeCombat.lastDamageSource();
        return "gameMode=" + safeCombat.gameMode().name() + "\n"
                + "armorPoints=" + safeCombat.equipment().armorPoints() + "\n"
                + "armorSlots=" + safeCombat.equipment().armorSlotsFilled() + "\n"
                + "lastDamageSourceId=" + source.id() + "\n"
                + "lastDamageSourceLabel=" + escapeProperty(source.label()) + "\n"
                + "lastDamageSourceBypassesArmor=" + source.bypassesArmor() + "\n"
                + "lastDamageSourceBypassesGameMode=" + source.bypassesGameMode() + "\n";
    }

    private static String equipmentText(EchoClientEquipmentState equipment) {
        EchoClientEquipmentState safeEquipment =
                equipment == null ? EchoClientEquipmentState.empty() : equipment;
        StringBuilder builder = new StringBuilder("slot\titem\tlabel\tarmor\tdurability\tmaxDurability\n");
        for (EchoClientArmorSlot slot : EchoClientArmorSlot.values()) {
            safeEquipment.piece(slot).ifPresent(piece -> builder.append(piece.slot().id()).append('\t')
                    .append(piece.itemId()).append('\t')
                    .append(escapeTab(piece.displayName())).append('\t')
                    .append(piece.armorPoints()).append('\t')
                    .append(piece.durability()).append('\t')
                    .append(piece.maxDurability()).append('\n'));
        }
        return builder.toString();
    }

    private static String offhandText(EchoClientEquipmentState equipment) {
        EchoClientEquipmentState safeEquipment =
                equipment == null ? EchoClientEquipmentState.empty() : equipment;
        StringBuilder builder = new StringBuilder("item\tlabel\tcategory\tmaxStack\tcount\n");
        safeEquipment.offhand().ifPresent(stack -> {
            EchoItemDefinition definition = stack.definition();
            builder.append(definition.id().value()).append('\t')
                    .append(escapeTab(definition.displayName())).append('\t')
                    .append(definition.category().name()).append('\t')
                    .append(definition.maxStackSize()).append('\t')
                    .append(stack.quantity()).append('\n');
        });
        return builder.toString();
    }

    private static String progressionText(EchoClientProgressionState progression) {
        EchoClientProgressionState safeProgression =
                progression == null ? EchoClientProgressionState.empty() : progression;
        return "experience=" + safeProgression.experience() + "\n"
                + "lastAward=" + safeProgression.lastAward() + "\n"
                + "milestones=" + safeProgression.milestones().stream()
                        .map(EchoClientGameplaySaveCodec::escapeProgressionToken)
                        .reduce((left, right) -> left + "|" + right)
                        .orElse("") + "\n";
    }

    private static String hazardText(EchoClientHazardState hazardState) {
        EchoClientHazardState safeHazard = hazardState == null ? EchoClientHazardState.empty() : hazardState;
        return "hazardId=" + safeHazard.hazardId() + "\n"
                + "label=" + escapeProperty(safeHazard.label()) + "\n"
                + "exposure=" + safeHazard.exposure() + "\n"
                + "tickSeconds=" + safeHazard.tickSeconds() + "\n"
                + "lastDamage=" + safeHazard.lastDamage() + "\n";
    }

    private static String toolsText(EchoClientToolState toolState) {
        EchoClientToolState safeTools = toolState == null ? EchoClientToolState.empty() : toolState;
        StringBuilder builder = new StringBuilder("item\tdurability\n");
        safeTools.durabilityByItemId().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> builder.append(escapeTab(entry.getKey())).append('\t')
                        .append(entry.getValue()).append('\n'));
        return builder.toString();
    }

    private static String entitiesText(List<EchoClientEntitySnapshot> entities) {
        StringBuilder builder = new StringBuilder(
                "entity\tdefinition\tlabel\tkind\tmaxHealth\tmovementSpeed\taiProfile\tx\ty\tz\tcurrentHealth\thealthMax\tblockedByWorld\taiState\n"
        );
        for (EchoClientEntitySnapshot entity : entities == null ? List.<EchoClientEntitySnapshot>of() : entities) {
            builder.append(escapeTab(entity.entityId())).append('\t')
                    .append(escapeTab(entity.definitionId())).append('\t')
                    .append(escapeTab(entity.displayName())).append('\t')
                    .append(entity.kind().name()).append('\t')
                    .append(entity.maxHealth()).append('\t')
                    .append(entity.movementSpeed()).append('\t')
                    .append(escapeTab(entity.aiProfile())).append('\t')
                    .append(entity.x()).append('\t')
                    .append(entity.y()).append('\t')
                    .append(entity.z()).append('\t')
                    .append(entity.currentHealth()).append('\t')
                    .append(entity.healthMax()).append('\t')
                    .append(entity.blockedByWorld()).append('\t')
                    .append(entity.aiState().name()).append('\n');
        }
        return builder.toString();
    }

    private static String droppedItemsText(List<EchoClientDroppedItemSnapshot> drops) {
        StringBuilder builder = new StringBuilder(
                "drop\titem\tlabel\tcategory\tmaxStack\tcount\tx\ty\tz\tageSeconds\n"
        );
        for (EchoClientDroppedItemSnapshot drop : drops == null ? List.<EchoClientDroppedItemSnapshot>of() : drops) {
            builder.append(escapeTab(drop.dropId())).append('\t')
                    .append(drop.itemId()).append('\t')
                    .append(escapeTab(drop.displayName())).append('\t')
                    .append(drop.category().name()).append('\t')
                    .append(drop.maxStackSize()).append('\t')
                    .append(drop.quantity()).append('\t')
                    .append(drop.x()).append('\t')
                    .append(drop.y()).append('\t')
                    .append(drop.z()).append('\t')
                    .append(drop.ageSeconds()).append('\n');
        }
        return builder.toString();
    }

    private static String machinesText(EchoClientMachineStateSnapshot snapshot) {
        EchoClientMachineStateSnapshot safeSnapshot =
                snapshot == null ? EchoClientMachineStateSnapshot.reference() : snapshot;
        StringBuilder builder = new StringBuilder(
                "rowType\tid\tvalue\tkind\tenergy\tcapacity\ttransferPerTick\tgenerationPerTick\tneighbors\n"
        );
        appendMachineState(builder, "graphConnected", Boolean.toString(safeSnapshot.graphConnected()));
        appendMachineState(builder, "machineUiOpened", Boolean.toString(safeSnapshot.machineUiOpened()));
        appendMachineState(builder, "scrapPressInputCount", Integer.toString(safeSnapshot.scrapPressInputCount()));
        appendMachineState(builder, "scrapPressOutputCount", Integer.toString(safeSnapshot.scrapPressOutputCount()));
        appendMachineState(builder, "oreGrinderInputCount", Integer.toString(safeSnapshot.oreGrinderInputCount()));
        appendMachineState(builder, "scrapPressProgressTicks", Integer.toString(safeSnapshot.scrapPressProgressTicks()));
        appendMachineState(builder, "recipeProgressTicks", Integer.toString(safeSnapshot.recipeProgressTicks()));
        appendMachineState(builder, "powerConsumed", Integer.toString(safeSnapshot.powerConsumed()));
        appendMachineState(builder, "outputAppeared", Boolean.toString(safeSnapshot.outputAppeared()));
        appendMachineState(builder, "outputCountBeforeLogistics", Integer.toString(safeSnapshot.outputCountBeforeLogistics()));
        appendMachineState(builder, "stateReloaded", Boolean.toString(safeSnapshot.stateReloaded()));
        appendMachineState(builder, "missionDependency", Boolean.toString(safeSnapshot.missionDependency()));
        for (EchoClientMachineStateSnapshot.PowerNode node : safeSnapshot.powerGraph()) {
            builder.append("node").append('\t')
                    .append(escapeTab(node.id())).append('\t')
                    .append('\t')
                    .append(escapeTab(node.kind())).append('\t')
                    .append(node.energy()).append('\t')
                    .append(node.capacity()).append('\t')
                    .append(node.transferPerTick()).append('\t')
                    .append(node.generationPerTick()).append('\t')
                    .append(pipeText(node.neighbors())).append('\n');
        }
        for (EchoClientMachineStateSnapshot.InventoryPort port : safeSnapshot.inventoryPorts()) {
            builder.append("port").append('\t')
                    .append(escapeTab(port.machineId() + "/" + port.port())).append('\t')
                    .append(pipeText(port.accepts()))
                    .append("\t\t\t\t\t\t")
                    .append('\n');
        }
        return builder.toString();
    }

    private static String blockEntitiesText(EchoClientMachineStateSnapshot snapshot) {
        EchoClientMachineStateSnapshot safeSnapshot =
                snapshot == null ? EchoClientMachineStateSnapshot.reference() : snapshot;
        List<EchoClientMachineStateSnapshot.BlockEntity> blockEntities = safeSnapshot.blockEntities().isEmpty()
                ? EchoClientMachineRuntime.restore(safeSnapshot).snapshot().blockEntities()
                : safeSnapshot.blockEntities();
        StringBuilder builder = new StringBuilder(
                "chunkX\tchunkY\tchunkZ\tlocalX\tlocalY\tlocalZ\tx\ty\tz\tentityId\tblock\tkind\tstate\n"
        );
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : blockEntities) {
            builder.append(blockEntity.chunkX()).append('\t')
                    .append(blockEntity.chunkY()).append('\t')
                    .append(blockEntity.chunkZ()).append('\t')
                    .append(blockEntity.localX()).append('\t')
                    .append(blockEntity.localY()).append('\t')
                    .append(blockEntity.localZ()).append('\t')
                    .append(blockEntity.x()).append('\t')
                    .append(blockEntity.y()).append('\t')
                    .append(blockEntity.z()).append('\t')
                    .append(escapeTab(blockEntity.entityId())).append('\t')
                    .append(escapeTab(blockEntity.blockId())).append('\t')
                    .append(escapeTab(blockEntity.kind())).append('\t')
                    .append(statePropertiesText(blockEntity.state()))
                    .append('\n');
        }
        return builder.toString();
    }

    private static void appendMachineState(StringBuilder builder, String key, String value) {
        builder.append("state").append('\t')
                .append(key).append('\t')
                .append(escapeTab(value))
                .append("\t\t\t\t\t\t")
                .append('\n');
    }

    private static String chunksText(EchoVoxelWorld world) {
        StringBuilder builder = new StringBuilder(
                "chunkX\tchunkY\tchunkZ\tlocalX\tlocalY\tlocalZ\tblock\tproperties\ttickVersion\n"
        );
        for (EchoVoxelChunk chunk : world.chunks()) {
            EchoVoxelChunkId id = chunk.id();
            for (int y = 0; y < chunk.size(); y++) {
                for (int z = 0; z < chunk.size(); z++) {
                    for (int x = 0; x < chunk.size(); x++) {
                        EchoVoxelBlockState state = chunk.stateAtLocal(x, y, z);
                        builder.append(id.x()).append('\t')
                                .append(id.y()).append('\t')
                                .append(id.z()).append('\t')
                                .append(x).append('\t')
                                .append(y).append('\t')
                                .append(z).append('\t')
                                .append(state.block().id()).append('\t')
                                .append(statePropertiesText(state.properties())).append('\t')
                                .append(state.tickVersion()).append('\n');
                    }
                }
            }
        }
        return builder.toString();
    }

    private static String sessionText(
            EchoClientWorldSession worldSession,
            EchoClientGameSession session,
            EchoVoxelWorld saveWorld,
            String reason
    ) {
        return "slotId=" + worldSession.slotId() + "\n"
                + "displayName=" + worldSession.displayName() + "\n"
                + "reason=" + reason + "\n"
                + "world.loadedChunks=" + session.world().loadedChunkCount() + "\n"
                + "world.cachedChunks=" + session.cachedChunkCount() + "\n"
                + "world.savedChunks=" + saveWorld.loadedChunkCount() + "\n"
                + "world.playerBiome=" + session.world().biomeAt(
                        session.player().state().x(),
                        session.player().state().z()
                ).id() + "\n"
                + "player.health=" + session.playerVitals().currentHealth() + "\n"
                + "player.maxHealth=" + session.playerVitals().maxHealth() + "\n"
                + "player.food=" + session.playerVitals().foodLevel() + "\n"
                + "player.saturation=" + session.playerVitals().saturation() + "\n"
                + "player.gameMode=" + session.gameMode().name() + "\n"
                + "player.armorPoints=" + session.playerCombatState().equipment().armorPoints() + "\n"
                + "player.lastDamageSource=" + session.playerCombatState().lastDamageSource().id() + "\n"
                + "player.level=" + session.progressionState().level() + "\n"
                + "player.experience=" + session.progressionState().experience() + "\n"
                + "player.hazard=" + session.hazardState().hazardId() + "\n"
                + "player.hazardExposure=" + session.hazardState().exposurePercent() + "\n"
                + "player.tool=" + session.selectedToolStatus(EchoVoxelBlock.AIR).itemId() + "\n"
                + "world.entities=" + session.livingEntityCount() + "\n"
                + "world.hostileEntities=" + session.hostileEntityCount() + "\n"
                + "world.droppedItems=" + session.droppedItemCount() + "\n"
                + "world.droppedItemQuantity=" + session.droppedItemQuantity() + "\n"
                + "player.x=" + session.player().state().x() + "\n"
                + "player.y=" + session.player().state().y() + "\n"
                + "player.z=" + session.player().state().z() + "\n";
    }

    private static EchoVoxelWorld world(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Map<String, String> values,
            List<String> chunkLines
    ) {
        int chunkSize = intValue(values, "chunkSize");
        TreeMap<EchoVoxelChunkId, EchoVoxelChunk> chunks = new TreeMap<>(
                java.util.Comparator.comparingInt(EchoVoxelChunkId::x)
                        .thenComparingInt(EchoVoxelChunkId::y)
                        .thenComparingInt(EchoVoxelChunkId::z)
        );
        for (String line : chunkLines) {
            if (line.isBlank() || line.startsWith("chunkX\t")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            if (parts.length < 7) {
                throw new IllegalArgumentException("Invalid client chunk save line: " + line);
            }
            EchoVoxelChunkId id = new EchoVoxelChunkId(
                    Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2])
            );
            EchoVoxelChunk chunk = chunks.computeIfAbsent(id, key -> new EchoVoxelChunk(key, chunkSize));
            EchoVoxelBlock block = block(bridge, parts[6]);
            EchoVoxelBlockState state = block.air()
                    ? EchoVoxelBlockState.AIR
                    : new EchoVoxelBlockState(
                    block,
                    parts.length >= 8 ? stateProperties(parts[7]) : Map.of(),
                    parts.length >= 9 && !parts[8].isBlank() ? Long.parseLong(parts[8]) : 0L
            );
            chunk.setStateLocal(
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    state
            );
        }
        return new EchoVoxelWorld(
                textValue(values, "worldId"),
                longValue(values, "seed"),
                chunkSize,
                new ArrayList<>(chunks.values()),
                doubleValue(values, "spawnX"),
                doubleValue(values, "spawnY"),
                doubleValue(values, "spawnZ"),
                doubleValue(values, "spawnYawDegrees"),
                EchoVoxelBiomeSources.byId(optionalTextValue(
                        values,
                        "biomeSourceId",
                        EchoVoxelBiomeSources.byWorldId(textValue(values, "worldId")).id()
                ))
        );
    }

    private static EchoVoxelPlayerState player(Map<String, String> values) {
        return new EchoVoxelPlayerState(
                doubleValue(values, "x"),
                doubleValue(values, "y"),
                doubleValue(values, "z"),
                doubleValue(values, "velocityY"),
                doubleValue(values, "yawDegrees"),
                doubleValue(values, "pitchDegrees"),
                booleanValue(values, "grounded"),
                booleanValue(values, "crouching"),
                booleanValue(values, "sprinting"),
                intValue(values, "selectedSlot"),
                doubleValue(values, "reach")
        );
    }

    private static EchoVoxelPlayerHotbar hotbar(
            EchoAdapterCoreStandaloneContentBridge bridge,
            int selectedSlot,
            List<String> lines
    ) {
        ArrayList<EchoVoxelHotbarSlot> slots = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("slot\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid client hotbar save line: " + line);
            }
            slots.add(new EchoVoxelHotbarSlot(
                    Integer.parseInt(parts[0]),
                    parts[1].equals(EchoVoxelBlock.AIR.id()) ? EchoVoxelBlock.AIR : bridge.registry().requireLiveVoxelBlock(parts[1]),
                    Integer.parseInt(parts[2])
            ));
        }
        return new EchoVoxelPlayerHotbar(slots, selectedSlot);
    }

    private static EchoClientPlayerVitals vitals(Map<String, String> values) {
        return new EchoClientPlayerVitals(
                intValue(values, "currentHealth"),
                intValue(values, "maxHealth"),
                intValue(values, "lastDamage"),
                optionalIntValue(values, "foodLevel", EchoClientPlayerVitals.DEFAULT_MAX_FOOD),
                optionalDoubleValue(values, "saturation", 5.0D),
                optionalDoubleValue(values, "exhaustion", 0.0D),
                optionalDoubleValue(values, "survivalTickSeconds", 0.0D)
        );
    }

    private static EchoClientPlayerCombatState combat(
            Map<String, String> values,
            EchoClientEquipmentState equipment
    ) {
        EchoClientDamageSource source = EchoClientDamageSource.parse(
                optionalTextValue(values, "lastDamageSourceId", EchoClientDamageSource.none().id()),
                optionalTextValue(values, "lastDamageSourceLabel", EchoClientDamageSource.none().label()),
                optionalBooleanValue(values, "lastDamageSourceBypassesArmor", true),
                optionalBooleanValue(values, "lastDamageSourceBypassesGameMode", false)
        );
        return new EchoClientPlayerCombatState(
                EchoClientGameMode.parse(values.get("gameMode")),
                equipment,
                source
        );
    }

    private static EchoClientEquipmentState equipment(List<String> lines) {
        ArrayList<EchoClientArmorPiece> pieces = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("slot\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid client equipment save line: " + line);
            }
            pieces.add(new EchoClientArmorPiece(
                    EchoClientArmorSlot.parse(parts[0]),
                    parts[1],
                    parts[2],
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5])
            ));
        }
        return new EchoClientEquipmentState(pieces);
    }

    private static EchoItemStack offhand(List<String> lines) {
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("item\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 5) {
                throw new IllegalArgumentException("Invalid client offhand save line: " + line);
            }
            EchoItemDefinition definition = new EchoItemDefinition(
                    new EchoItemId(parts[0]),
                    parts[1],
                    itemCategory(parts[2]),
                    Integer.parseInt(parts[3]),
                    1.0D,
                    List.of("offhand", "client-save"),
                    List.of("Restored from client offhand save")
            );
            return new EchoItemStack(definition, Integer.parseInt(parts[4]));
        }
        return null;
    }

    private static EchoClientProgressionState progression(Map<String, String> values) {
        ArrayList<String> milestones = new ArrayList<>();
        String milestoneText = values.getOrDefault("milestones", "");
        if (!milestoneText.isBlank()) {
            for (String milestone : milestoneText.split("\\|")) {
                if (!milestone.isBlank()) {
                    milestones.add(milestone);
                }
            }
        }
        return new EchoClientProgressionState(
                optionalIntValue(values, "experience", 0),
                optionalIntValue(values, "lastAward", 0),
                milestones
        );
    }

    private static EchoClientHazardState hazards(Map<String, String> values) {
        return new EchoClientHazardState(
                optionalTextValue(values, "hazardId", EchoClientHazardState.empty().hazardId()),
                optionalTextValue(values, "label", EchoClientHazardState.empty().label()),
                optionalDoubleValue(values, "exposure", 0.0D),
                optionalDoubleValue(values, "tickSeconds", 0.0D),
                optionalIntValue(values, "lastDamage", 0)
        );
    }

    private static EchoClientToolState tools(List<String> lines) {
        LinkedHashMap<String, Integer> tools = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("item\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid client tools save line: " + line);
            }
            tools.put(parts[0], Integer.parseInt(parts[1]));
        }
        return new EchoClientToolState(tools);
    }

    private static List<EchoClientEntitySnapshot> entities(List<String> lines) {
        ArrayList<EchoClientEntitySnapshot> entities = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("entity\t")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            if (parts.length != 14) {
                throw new IllegalArgumentException("Invalid client entity save line: " + line);
            }
            entities.add(new EchoClientEntitySnapshot(
                    parts[0],
                    parts[1],
                    parts[2],
                    entityKind(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    parts[6],
                    Integer.parseInt(parts[7]),
                    Integer.parseInt(parts[8]),
                    Integer.parseInt(parts[9]),
                    Integer.parseInt(parts[10]),
                    Integer.parseInt(parts[11]),
                    Boolean.parseBoolean(parts[12]),
                    entityAiState(parts[13])
            ));
        }
        return List.copyOf(entities);
    }

    private static List<EchoClientDroppedItemSnapshot> droppedItems(List<String> lines) {
        ArrayList<EchoClientDroppedItemSnapshot> drops = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("drop\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 10) {
                throw new IllegalArgumentException("Invalid client dropped item save line: " + line);
            }
            drops.add(new EchoClientDroppedItemSnapshot(
                    parts[0],
                    parts[1],
                    parts[2],
                    itemCategory(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Double.parseDouble(parts[6]),
                    Double.parseDouble(parts[7]),
                    Double.parseDouble(parts[8]),
                    Double.parseDouble(parts[9])
            ));
        }
        return List.copyOf(drops);
    }

    private static EchoClientMachineStateSnapshot blockEntities(List<String> lines) {
        ArrayList<EchoClientMachineStateSnapshot.BlockEntity> blockEntities = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("chunkX\t")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            if (parts.length != 13) {
                throw new IllegalArgumentException("Invalid client block entity save line: " + line);
            }
            blockEntities.add(new EchoClientMachineStateSnapshot.BlockEntity(
                    parts[9],
                    parts[10],
                    parts[11],
                    Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7]),
                    Integer.parseInt(parts[8]),
                    stateProperties(parts[12])
            ));
        }
        return machineStateFromBlockEntities(blockEntities);
    }

    private static EchoClientMachineStateSnapshot machineStateFromBlockEntities(
            List<EchoClientMachineStateSnapshot.BlockEntity> blockEntities
    ) {
        EchoClientMachineStateSnapshot reference = EchoClientMachineStateSnapshot.reference();
        LinkedHashMap<String, String> state = new LinkedHashMap<>();
        ArrayList<EchoClientMachineStateSnapshot.PowerNode> nodes = new ArrayList<>();
        ArrayList<EchoClientMachineStateSnapshot.InventoryPort> ports = new ArrayList<>();
        for (EchoClientMachineStateSnapshot.BlockEntity blockEntity : blockEntities) {
            Map<String, String> entityState = blockEntity.state();
            String canonicalId = canonicalMachineEntityId(blockEntity);
            copyMachineState(state, entityState, "graphConnected");
            copyMachineState(state, entityState, "machineUiOpened");
            copyMachineState(state, entityState, "stateReloaded");
            if ("scrap_press".equals(canonicalId)) {
                copyMachineState(state, entityState, "inputCount", "scrapPressInputCount");
                copyMachineState(state, entityState, "outputCount", "scrapPressOutputCount");
                copyMachineState(state, entityState, "scrapPressProgressTicks");
                copyMachineState(state, entityState, "recipeProgressTicks");
                copyMachineState(state, entityState, "powerConsumed");
                copyMachineState(state, entityState, "outputAppeared");
                copyMachineState(state, entityState, "outputCountBeforeLogistics");
                copyMachineState(state, entityState, "missionDependency");
            }
            if ("ore_grinder".equals(canonicalId)) {
                copyMachineState(state, entityState, "inputCount", "oreGrinderInputCount");
            }
            if (isPowerGraphBlockEntity(canonicalId)) {
                nodes.add(new EchoClientMachineStateSnapshot.PowerNode(
                        blockEntity.entityId(),
                        blockEntity.kind(),
                        optionalIntValue(entityState, "energy", 0),
                        optionalIntValue(entityState, "capacity", 0),
                        optionalIntValue(entityState, "transferPerTick", 0),
                        optionalIntValue(entityState, "generationPerTick", 0),
                        pipeValues(entityState.getOrDefault("neighbors", ""))
                ));
            }
            for (Map.Entry<String, String> entry : entityState.entrySet()) {
                if (entry.getKey().startsWith("port.") && !entry.getValue().isBlank()) {
                    ports.add(new EchoClientMachineStateSnapshot.InventoryPort(
                            blockEntity.entityId(),
                            entry.getKey().substring("port.".length()),
                            pipeValues(entry.getValue())
                    ));
                }
            }
        }
        return new EchoClientMachineStateSnapshot(
                optionalBooleanValue(state, "graphConnected", reference.graphConnected()),
                optionalBooleanValue(state, "machineUiOpened", reference.machineUiOpened()),
                optionalIntValue(state, "scrapPressInputCount", reference.scrapPressInputCount()),
                optionalIntValue(state, "scrapPressOutputCount", reference.scrapPressOutputCount()),
                optionalIntValue(state, "oreGrinderInputCount", reference.oreGrinderInputCount()),
                optionalIntValue(state, "scrapPressProgressTicks", reference.scrapPressProgressTicks()),
                optionalIntValue(state, "recipeProgressTicks", reference.recipeProgressTicks()),
                optionalIntValue(state, "powerConsumed", reference.powerConsumed()),
                optionalBooleanValue(state, "outputAppeared", reference.outputAppeared()),
                optionalIntValue(state, "outputCountBeforeLogistics", reference.outputCountBeforeLogistics()),
                optionalBooleanValue(state, "stateReloaded", reference.stateReloaded()),
                optionalBooleanValue(state, "missionDependency", reference.missionDependency()),
                nodes.isEmpty() ? reference.powerGraph() : nodes,
                ports.isEmpty() ? reference.inventoryPorts() : ports,
                blockEntities.isEmpty() ? reference.blockEntities() : blockEntities
        );
    }

    private static boolean isPowerGraphBlockEntity(String entityId) {
        return switch (entityId) {
            case "micro_generator", "power_cable", "load_distributor", "battery_bank", "scrap_press" -> true;
            default -> false;
        };
    }

    private static String canonicalMachineEntityId(EchoClientMachineStateSnapshot.BlockEntity blockEntity) {
        String fromState = blockEntity.state().get("canonicalId");
        if (isKnownMachineEntityId(fromState)) {
            return fromState;
        }
        String entityId = blockEntity.entityId();
        if (entityId != null && entityId.contains("@")) {
            String prefix = entityId.substring(0, entityId.indexOf('@'));
            if (isKnownMachineEntityId(prefix)) {
                return prefix;
            }
        }
        if (isKnownMachineEntityId(entityId)) {
            return entityId;
        }
        return entityIdForMachineBlockId(blockEntity.blockId());
    }

    private static boolean isKnownMachineEntityId(String entityId) {
        return switch (entityId == null ? "" : entityId) {
            case "micro_generator", "power_cable", "load_distributor", "battery_bank",
                 "scrap_press", "item_pipe", "ore_grinder" -> true;
            default -> false;
        };
    }

    private static String entityIdForMachineBlockId(String blockId) {
        return switch (blockId) {
            case EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID -> "micro_generator";
            case EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID,
                 EchoAdapterCoreStandaloneContentBridge.REINFORCED_POWER_CABLE_BLOCK_ID,
                 EchoAdapterCoreStandaloneContentBridge.HIGH_VOLTAGE_POWER_CABLE_BLOCK_ID -> "power_cable";
            case EchoAdapterCoreStandaloneContentBridge.LOAD_DISTRIBUTOR_BLOCK_ID -> "load_distributor";
            case EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID -> "battery_bank";
            case EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID -> "scrap_press";
            case EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID -> "item_pipe";
            case EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID -> "ore_grinder";
            default -> "";
        };
    }

    private static void copyMachineState(
            LinkedHashMap<String, String> target,
            Map<String, String> source,
            String key
    ) {
        copyMachineState(target, source, key, key);
    }

    private static void copyMachineState(
            LinkedHashMap<String, String> target,
            Map<String, String> source,
            String sourceKey,
            String targetKey
    ) {
        String value = source.get(sourceKey);
        if (value != null && !value.isBlank()) {
            if (!target.containsKey(targetKey) || "true".equalsIgnoreCase(value)) {
                target.put(targetKey, value);
            }
        }
    }

    private static EchoClientMachineStateSnapshot machines(List<String> lines) {
        LinkedHashMap<String, String> state = new LinkedHashMap<>();
        ArrayList<EchoClientMachineStateSnapshot.PowerNode> nodes = new ArrayList<>();
        ArrayList<EchoClientMachineStateSnapshot.InventoryPort> ports = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("rowType\t")) {
                continue;
            }
            String[] parts = line.split("\\t", -1);
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid client machine save line: " + line);
            }
            switch (parts[0]) {
                case "state" -> state.put(parts[1], parts[2]);
                case "node" -> {
                    if (parts.length != 9) {
                        throw new IllegalArgumentException("Invalid client machine node save line: " + line);
                    }
                    nodes.add(new EchoClientMachineStateSnapshot.PowerNode(
                            parts[1],
                            parts[3],
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5]),
                            Integer.parseInt(parts[6]),
                            Integer.parseInt(parts[7]),
                            pipeValues(parts[8])
                    ));
                }
                case "port" -> {
                    String[] id = parts[1].split("/", 2);
                    if (id.length == 2) {
                        ports.add(new EchoClientMachineStateSnapshot.InventoryPort(
                                id[0],
                                id[1],
                                pipeValues(parts[2])
                        ));
                    }
                }
                default -> throw new IllegalArgumentException("Unknown client machine save row: " + line);
            }
        }
        EchoClientMachineStateSnapshot reference = EchoClientMachineStateSnapshot.reference();
        return new EchoClientMachineStateSnapshot(
                optionalBooleanValue(state, "graphConnected", reference.graphConnected()),
                optionalBooleanValue(state, "machineUiOpened", reference.machineUiOpened()),
                optionalIntValue(state, "scrapPressInputCount", reference.scrapPressInputCount()),
                optionalIntValue(state, "scrapPressOutputCount", reference.scrapPressOutputCount()),
                optionalIntValue(state, "oreGrinderInputCount", reference.oreGrinderInputCount()),
                optionalIntValue(state, "scrapPressProgressTicks", reference.scrapPressProgressTicks()),
                optionalIntValue(state, "recipeProgressTicks", reference.recipeProgressTicks()),
                optionalIntValue(state, "powerConsumed", reference.powerConsumed()),
                optionalBooleanValue(state, "outputAppeared", reference.outputAppeared()),
                optionalIntValue(state, "outputCountBeforeLogistics", reference.outputCountBeforeLogistics()),
                optionalBooleanValue(state, "stateReloaded", reference.stateReloaded()),
                optionalBooleanValue(state, "missionDependency", reference.missionDependency()),
                nodes.isEmpty() ? reference.powerGraph() : nodes,
                ports.isEmpty() ? reference.inventoryPorts() : ports
        );
    }

    private static String pipeText(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(EchoClientGameplaySaveCodec::escapePipe)
                .reduce((left, right) -> left + "|" + right)
                .orElse("");
    }

    private static List<String> pipeValues(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (String value : text.split("\\|")) {
            if (!value.isBlank()) {
                result.add(value.replace("%7C", "|").replace("%09", "\t"));
            }
        }
        return List.copyOf(result);
    }

    private static List<EchoClientInventorySlotSnapshot> inventory(List<String> lines) {
        ArrayList<EchoClientInventorySlotSnapshot> slots = new ArrayList<>();
        for (String line : lines) {
            if (line.isBlank() || line.startsWith("slot\t")) {
                continue;
            }
            String[] parts = line.split("\\t");
            if (parts.length != 6) {
                throw new IllegalArgumentException("Invalid client inventory save line: " + line);
            }
            slots.add(new EchoClientInventorySlotSnapshot(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    parts[2],
                    itemCategory(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5])
            ));
        }
        return slots;
    }

    private static EchoItemCategory itemCategory(String value) {
        try {
            return EchoItemCategory.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return EchoItemCategory.MATERIAL;
        }
    }

    private static EchoEntityKind entityKind(String value) {
        try {
            return EchoEntityKind.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return EchoEntityKind.HOSTILE;
        }
    }

    private static EchoEntityAiState entityAiState(String value) {
        try {
            return EchoEntityAiState.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return EchoEntityAiState.IDLE;
        }
    }

    private static EchoVoxelBlock block(EchoAdapterCoreStandaloneContentBridge bridge, String id) {
        if (id.equals(EchoVoxelBlock.AIR.id())) {
            return EchoVoxelBlock.AIR;
        }
        return bridge.registry().requireLiveVoxelBlock(id);
    }

    private static String statePropertiesText(Map<String, String> properties) {
        if (properties == null || properties.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (builder.length() > 0) {
                builder.append(';');
            }
            builder.append(escapeStateToken(entry.getKey()))
                    .append('=')
                    .append(escapeStateToken(entry.getValue()));
        }
        return builder.toString();
    }

    private static Map<String, String> stateProperties(String text) {
        if (text == null || text.isBlank()) {
            return Map.of();
        }
        LinkedHashMap<String, String> properties = new LinkedHashMap<>();
        for (String pair : text.split(";")) {
            if (pair.isBlank()) {
                continue;
            }
            int separator = pair.indexOf('=');
            if (separator <= 0) {
                continue;
            }
            String key = unescapeStateToken(pair.substring(0, separator));
            String value = unescapeStateToken(pair.substring(separator + 1));
            if (!key.isBlank() && !value.isBlank()) {
                properties.put(key, value);
            }
        }
        return properties.isEmpty() ? Map.of() : Map.copyOf(properties);
    }

    private static Map<String, String> properties(String text) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (String line : text.split("\\R")) {
            if (line.isBlank() || !line.contains("=")) {
                continue;
            }
            int separator = line.indexOf('=');
            values.put(line.substring(0, separator), line.substring(separator + 1));
        }
        return Map.copyOf(values);
    }

    private static String textValue(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing client save value: " + key);
        }
        return value;
    }

    private static int intValue(Map<String, String> values, String key) {
        return Integer.parseInt(textValue(values, key));
    }

    private static int optionalIntValue(Map<String, String> values, String key, int fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Integer.parseInt(value);
    }

    private static long longValue(Map<String, String> values, String key) {
        return Long.parseLong(textValue(values, key));
    }

    private static double doubleValue(Map<String, String> values, String key) {
        return Double.parseDouble(textValue(values, key));
    }

    private static double optionalDoubleValue(Map<String, String> values, String key, double fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Double.parseDouble(value);
    }

    private static String optionalTextValue(Map<String, String> values, String key, String fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private static boolean optionalBooleanValue(Map<String, String> values, String key, boolean fallback) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(value);
    }

    private static boolean booleanValue(Map<String, String> values, String key) {
        return Boolean.parseBoolean(textValue(values, key));
    }

    private static String escapeTab(String value) {
        return value == null ? "" : value.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ');
    }

    private static String escapePipe(String value) {
        return escapeTab(value).replace("|", "%7C");
    }

    private static String escapeStateToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim()
                .replace("%", "%25")
                .replace("=", "%3D")
                .replace(";", "%3B")
                .replace("\t", "%09")
                .replace("\n", "%0A")
                .replace("\r", "%0D");
    }

    private static String unescapeStateToken(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '%' && index + 2 < value.length()) {
                String hex = value.substring(index + 1, index + 3);
                try {
                    builder.append((char) Integer.parseInt(hex, 16));
                    index += 2;
                    continue;
                } catch (NumberFormatException ignored) {
                    // Keep malformed escape sequences literal so old hand-edited saves still load.
                }
            }
            builder.append(current);
        }
        return builder.toString().trim();
    }

    private static String escapeProperty(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static String escapeProgressionToken(String value) {
        return escapeProperty(value).replace('|', ' ');
    }
}
