package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeContentRegistry;
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
import dev.echo.standalone.runtime.gameplay.EchoGameplayNotification;
import dev.echo.standalone.runtime.gameplay.EchoGameplayNotificationSeverity;
import dev.echo.standalone.runtime.gameplay.EchoNotificationLog;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoInventoryOperations;
import dev.echo.standalone.runtime.item.EchoInventorySlot;
import dev.echo.standalone.runtime.item.EchoInventoryStore;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarMutation;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerController;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerHotbar;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.render.EchoVoxelFramebuffer;
import dev.echo.standalone.runtime.render.EchoVoxelSoftwareRenderer;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoNotification;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelWorld;
import dev.echo.standalone.runtime.world.EchoVoxelWorldRuntimeProfile;
import dev.echo.standalone.runtime.world.EchoWorldPosition;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class StandaloneRuntimeHostContext {
    public static final String DEFAULT_RUNTIME_HOST_ID = "echo_runtime_standalone:ashfall_live_host";
    public static final String DEFAULT_PLAYER_ID = "standalone-player";
    public static final String DEFAULT_DIMENSION_ID = "echoashfallprotocol:standalone_crash_site";
    public static final String DEFAULT_SLOT_ID = "standalone-runtime-host-live";
    private static final long ASHFALL_SEED = 42L;

    private final String runtimeHostId;
    private final String playerId;
    private final String dimensionId;
    private final String slotId;
    private final EchoAdapterCoreStandaloneContentBridge bridge;
    private final EchoInventoryStore inventoryStore;
    private final EchoInventoryContainer playerInventory;
    private final EchoInventoryOperations inventoryOperations;
    private final EchoEntityStore entityStore;
    private final EchoNotificationLog notificationLog;
    private final ArrayList<EchoNotification> hudNotifications;
    private final ArrayList<EchoStandalonePlayableVoxelEdit> edits;
    private final LinkedHashMap<String, EchoItemDefinition> itemDefinitions;
    private final LinkedHashMap<String, Map<String, Object>> worldStateRows;
    private final LinkedHashMap<String, Map<String, Object>> blockEntityRows;
    private final LinkedHashMap<String, Map<String, Object>> saveDataRows;
    private final EchoAdapterCoreRuntimeContentRegistry contentRegistrations;
    private final LinkedHashMap<String, LinkedHashMap<String, Integer>> capabilityItemStores;
    private final LinkedHashMap<String, Integer> capabilityEnergyStores;
    private final ArrayList<Map<String, Object>> eventLog;
    private final ArrayList<Map<String, Object>> packetLog;
    private final EchoSaveRuntimeResult saveRuntime;
    private EchoVoxelWorld world;
    private EchoVoxelPlayerController playerController;
    private EchoVoxelPlayerHotbar hotbar;
    private EchoAshfallLiveMissionState mission;
    private String lastFeedback;
    private long gameTime;
    private int transactionSequence;

    private StandaloneRuntimeHostContext(
            String runtimeHostId,
            String playerId,
            String dimensionId,
            String slotId,
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoVoxelWorld world,
            EchoVoxelPlayerController playerController,
            EchoVoxelPlayerHotbar hotbar,
            EchoAshfallLiveMissionState mission,
            EchoInventoryStore inventoryStore,
            EchoInventoryContainer playerInventory,
            EchoInventoryOperations inventoryOperations,
            EchoEntityStore entityStore,
            EchoNotificationLog notificationLog,
            EchoSaveRuntimeResult saveRuntime
    ) {
        this.runtimeHostId = requireText(runtimeHostId, "runtimeHostId");
        this.playerId = requireText(playerId, "playerId");
        this.dimensionId = requireText(dimensionId, "dimensionId");
        this.slotId = requireText(slotId, "slotId");
        this.bridge = Objects.requireNonNull(bridge, "bridge");
        this.world = Objects.requireNonNull(world, "world");
        this.playerController = Objects.requireNonNull(playerController, "playerController");
        this.hotbar = Objects.requireNonNull(hotbar, "hotbar");
        this.mission = Objects.requireNonNull(mission, "mission");
        this.inventoryStore = Objects.requireNonNull(inventoryStore, "inventoryStore");
        this.playerInventory = Objects.requireNonNull(playerInventory, "playerInventory");
        this.inventoryOperations = Objects.requireNonNull(inventoryOperations, "inventoryOperations");
        this.entityStore = Objects.requireNonNull(entityStore, "entityStore");
        this.notificationLog = Objects.requireNonNull(notificationLog, "notificationLog");
        this.saveRuntime = Objects.requireNonNull(saveRuntime, "saveRuntime");
        this.hudNotifications = new ArrayList<>();
        this.edits = new ArrayList<>();
        this.itemDefinitions = new LinkedHashMap<>();
        this.worldStateRows = new LinkedHashMap<>();
        this.blockEntityRows = new LinkedHashMap<>();
        this.saveDataRows = new LinkedHashMap<>();
        this.contentRegistrations = new EchoAdapterCoreRuntimeContentRegistry();
        this.capabilityItemStores = new LinkedHashMap<>();
        this.capabilityEnergyStores = new LinkedHashMap<>();
        this.eventLog = new ArrayList<>();
        this.packetLog = new ArrayList<>();
        this.lastFeedback = mission.lastMessage();
    }

    public static StandaloneRuntimeHostContext ashfall(
            EchoAdapterCoreStandaloneContentBridge bridge,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(bridge, "bridge");
        Objects.requireNonNull(saveRoot, "saveRoot");
        dev.echo.standalone.runtime.player.EchoVoxelSessionRuntimeProfile sessionProfile =
                dev.echo.standalone.runtime.player.EchoVoxelSessionProfiles.ashfallCrashSite(
                        bridge.registry()::requireLiveVoxelBlock,
                        bridge.runtimeMarkerBlock(),
                        1
                );
        EchoVoxelWorld world = sessionProfile.generate(ASHFALL_SEED, 0);
        world = sessionProfile.streamer().streamAround(world, world.spawnX(), world.spawnZ());
        EchoVoxelPlayerController playerController = EchoVoxelPlayerController.spawnAt(
                world,
                world.spawnX(),
                world.spawnZ(),
                world.spawnYawDegrees(),
                -32.0D
        );
        EchoVoxelPlayerHotbar hotbar = new EchoVoxelPlayerHotbar(List.of(), 0);
        EchoInventoryStore inventoryStore = new EchoInventoryStore();
        EchoInventoryContainer playerInventory = new EchoInventoryContainer(
                new EchoInventoryId("standalone-runtime-host/player"),
                Optional.of(new EchoEntityId(DEFAULT_PLAYER_ID)),
                "Standalone Player Inventory",
                EchoVoxelPlayerHotbar.SLOT_COUNT
        );
        inventoryStore.register(playerInventory);
        EchoEntityStore entityStore = new EchoEntityStore();
        EchoSaveRuntimeResult saveRuntime = EchoStandaloneLiveSessionSaveRuntime.openSave(saveRoot);
        StandaloneRuntimeHostContext context = new StandaloneRuntimeHostContext(
                DEFAULT_RUNTIME_HOST_ID,
                DEFAULT_PLAYER_ID,
                DEFAULT_DIMENSION_ID,
                DEFAULT_SLOT_ID,
                bridge,
                world,
                playerController,
                hotbar,
                new EchoAshfallLiveMissionState(),
                inventoryStore,
                playerInventory,
                new EchoInventoryOperations(),
                entityStore,
                new EchoNotificationLog(),
                saveRuntime
        );
        context.registerPlayerEntity();
        context.grantItem(bridge.runtimeMarkerBlock().id(), 12);
        context.grantItem(bridge.shelterAnchorBlock().id(), 2);
        context.grantItem(bridge.waterRationItem().id(), 2);
        context.grantItem(bridge.fieldRationItem().id(), 2);
        context.grantItem(bridge.emergencyScannerItem().id(), 1);
        context.grantItem(bridge.rainCollectorBlock().id(), 1);
        context.grantItem(bridge.waterPurifierBlock().id(), 1);
        context.grantItem(bridge.handRecyclerBlock().id(), 1);
        return context;
    }

    public String runtimeHostId() {
        return runtimeHostId;
    }

    public String playerId() {
        return playerId;
    }

    public String dimensionId() {
        return dimensionId;
    }

    public EchoAdapterCoreStandaloneContentBridge bridge() {
        return bridge;
    }

    public EchoVoxelWorld world() {
        return world;
    }

    public EchoVoxelPlayerState player() {
        return playerController.state();
    }

    public EchoVoxelPlayerHotbar hotbar() {
        return hotbar;
    }

    public EchoAshfallLiveMissionState mission() {
        return mission;
    }

    public EchoInventoryContainer playerInventory() {
        return playerInventory;
    }

    public EchoInventoryOperations inventoryOperations() {
        return inventoryOperations;
    }

    public EchoEntityStore entityStore() {
        return entityStore;
    }

    public long gameTime() {
        return gameTime;
    }

    public List<EchoStandalonePlayableVoxelEdit> edits() {
        return List.copyOf(edits);
    }

    public List<EchoNotification> hudNotifications() {
        return List.copyOf(hudNotifications);
    }

    public List<Map<String, Object>> eventLog() {
        return List.copyOf(eventLog);
    }

    public List<Map<String, Object>> packetLog() {
        return List.copyOf(packetLog);
    }

    public EchoVoxelBlock requireVoxel(String canonicalId) {
        String id = requireText(canonicalId, "canonicalId");
        return id.equals(EchoVoxelBlock.AIR.id()) ? EchoVoxelBlock.AIR : bridge.registry().requireLiveVoxelBlock(id);
    }

    public EchoItemDefinition itemDefinition(String canonicalId) {
        String id = requireText(canonicalId, "canonicalId");
        return itemDefinitions.computeIfAbsent(id, key -> new EchoItemDefinition(
                new EchoItemId(key),
                displayName(key),
                itemCategory(key),
                EchoVoxelPlayerHotbar.MAX_STACK,
                1.0D,
                List.of("adaptercore", "standalone-runtime", key.substring(0, key.indexOf(':'))),
                List.of("Canonical AdapterCore id: " + key)
        ));
    }

    public EchoInventoryOperationResult grantItem(String canonicalId, int amount) {
        requirePositive(amount, "amount");
        EchoItemDefinition definition = itemDefinition(canonicalId);
        int remaining = amount;
        EchoInventoryOperationResult result = new EchoInventoryOperationResult("add", true, 0, "added");
        while (remaining > 0) {
            int stackSize = Math.min(remaining, definition.maxStackSize());
            result = inventoryOperations.add(playerInventory, new EchoItemStack(definition, stackSize));
            remaining -= result.quantity();
            if (!result.success()) {
                break;
            }
        }
        hotbar.add(requireVoxel(canonicalId), amount);
        return result;
    }

    public boolean removeItem(String canonicalId, int amount) {
        requirePositive(amount, "amount");
        EchoItemId itemId = new EchoItemId(requireText(canonicalId, "canonicalId"));
        EchoInventoryOperationResult inventoryResult = inventoryOperations.consume(playerInventory, itemId, amount);
        int removedFromHotbar = 0;
        for (EchoVoxelHotbarSlot slot : hotbar.slots()) {
            if (removedFromHotbar >= amount) {
                break;
            }
            if (!slot.empty() && slot.block().id().equals(canonicalId)) {
                if (slot.index() < EchoVoxelPlayerHotbar.HOTBAR_COUNT) {
                    hotbar.select(slot.index());
                    playerController.selectSlot(slot.index());
                }
                EchoVoxelHotbarMutation mutation = hotbar.consumeSelected();
                if (mutation.changed()) {
                    removedFromHotbar++;
                }
            }
        }
        return inventoryResult.success() || removedFromHotbar >= amount;
    }

    public boolean selectHotbarItem(String canonicalId) {
        for (EchoVoxelHotbarSlot slot : hotbar.hotbarSlots()) {
            if (!slot.empty() && slot.block().id().equals(canonicalId)) {
                hotbar.select(slot.index());
                playerController.selectSlot(slot.index());
                mirrorPlayerEntity();
                return true;
            }
        }
        return false;
    }

    public void setPlayerState(EchoVoxelPlayerState state) {
        playerController = new EchoVoxelPlayerController(Objects.requireNonNull(state, "state"));
        mirrorPlayerEntity();
    }

    public void advanceGameTime(long ticks) {
        if (ticks < 0) {
            throw new IllegalArgumentException("ticks must not be negative");
        }
        gameTime += ticks;
    }

    public void recordWorldEdit(int x, int y, int z, EchoVoxelBlock before, EchoVoxelBlock after) {
        edits.add(new EchoStandalonePlayableVoxelEdit(
                x,
                y,
                z,
                Objects.requireNonNullElse(before, EchoVoxelBlock.AIR).id(),
                Objects.requireNonNullElse(after, EchoVoxelBlock.AIR).id()
        ));
    }

    public void putWorldState(String key, Map<String, Object> values) {
        worldStateRows.put(requireText(key, "key"), sanitized(values));
    }

    public void putBlockEntityState(String key, Map<String, Object> values) {
        blockEntityRows.put(requireText(key, "key"), sanitized(values));
    }

    public Map<String, Object> blockEntityState(String key) {
        return blockEntityRows.getOrDefault(requireText(key, "key"), Map.of());
    }

    public void putSaveData(String key, Map<String, Object> values) {
        saveDataRows.put(requireText(key, "key"), sanitized(values));
    }

    public Map<String, Object> readSaveData(String key) {
        return saveDataRows.getOrDefault(requireText(key, "key"), Map.of());
    }

    public boolean deleteSaveData(String key) {
        return saveDataRows.remove(requireText(key, "key")) != null;
    }

    public boolean putContentRegistration(String contentId, Map<String, Object> values) {
        return contentRegistrations.register(requireText(contentId, "contentId"), sanitized(values));
    }

    public List<Map<String, Object>> contentRegistrations(String domain) {
        return contentRegistrations.registrations(domain);
    }

    public Map<String, Integer> capabilityInventory(String key) {
        return Map.copyOf(capabilityItemStores.computeIfAbsent(requireText(key, "key"), ignored -> new LinkedHashMap<>()));
    }

    public int insertCapabilityItem(String capabilityKey, String canonicalId, int amount) {
        requirePositive(amount, "amount");
        LinkedHashMap<String, Integer> store =
                capabilityItemStores.computeIfAbsent(requireText(capabilityKey, "capabilityKey"), ignored -> new LinkedHashMap<>());
        store.merge(requireText(canonicalId, "canonicalId"), amount, Integer::sum);
        return amount;
    }

    public int extractCapabilityItem(String capabilityKey, String canonicalId, int amount) {
        requirePositive(amount, "amount");
        LinkedHashMap<String, Integer> store =
                capabilityItemStores.computeIfAbsent(requireText(capabilityKey, "capabilityKey"), ignored -> new LinkedHashMap<>());
        int current = store.getOrDefault(requireText(canonicalId, "canonicalId"), 0);
        int extracted = Math.min(current, amount);
        if (extracted == current) {
            store.remove(canonicalId);
        } else {
            store.put(canonicalId, current - extracted);
        }
        return extracted;
    }

    public int receiveEnergy(String capabilityKey, int amount) {
        requirePositive(amount, "amount");
        String key = requireText(capabilityKey, "capabilityKey");
        capabilityEnergyStores.merge(key, amount, Integer::sum);
        return amount;
    }

    public int extractEnergy(String capabilityKey, int amount) {
        requirePositive(amount, "amount");
        String key = requireText(capabilityKey, "capabilityKey");
        int current = capabilityEnergyStores.getOrDefault(key, 0);
        int extracted = Math.min(current, amount);
        capabilityEnergyStores.put(key, current - extracted);
        return extracted;
    }

    public int energyStored(String capabilityKey) {
        return capabilityEnergyStores.getOrDefault(requireText(capabilityKey, "capabilityKey"), 0);
    }

    public void appendEvent(String eventName, String canonicalId, Map<String, Object> payload) {
        eventLog.add(Map.of(
                "tick", gameTime,
                "eventName", requireText(eventName, "eventName"),
                "canonicalId", requireText(canonicalId, "canonicalId"),
                "payload", sanitized(payload)
        ));
    }

    public void appendPacket(String packetName, String playerId, Map<String, Object> payload) {
        packetLog.add(Map.of(
                "tick", gameTime,
                "packetName", requireText(packetName, "packetName"),
                "playerId", requireText(playerId, "playerId"),
                "payload", sanitized(payload)
        ));
    }

    public EchoNotification publishFeedback(EchoGameplayNotificationSeverity severity, String message) {
        EchoGameplayNotification notification = notificationLog.add(
                Objects.requireNonNull(severity, "severity"),
                requireText(message, "message"),
                gameTime
        );
        EchoNotification hudNotification = new EchoNotification(
                notification.notificationId(),
                severity.name().toLowerCase(Locale.ROOT),
                notification.message(),
                "mission_hud",
                true
        );
        hudNotifications.add(hudNotification);
        lastFeedback = notification.message();
        return hudNotification;
    }

    public EchoSaveCommitResult commitLiveSnapshot(
            String actionId,
            String eventName,
            String canonicalId
    ) throws IOException {
        transactionSequence++;
        EchoVoxelFramebuffer frame = new EchoVoxelSoftwareRenderer().render(world, player().camera(), 96, 54);
        return EchoStandalonePlayableVoxelSaveCodec.writeLiveSnapshot(
                saveRuntime,
                slotId,
                String.format(Locale.ROOT, "standalone-host-%05d", transactionSequence),
                player(),
                hotbar,
                mission,
                edits,
                frame,
                Map.of(
                        "runtimeHostId", runtimeHostId,
                        "adapterCoreActionId", requireText(actionId, "actionId"),
                        "adapterCoreEventName", requireText(eventName, "eventName"),
                        "canonicalId", requireText(canonicalId, "canonicalId"),
                        "path", "player_action_to_adaptercore_to_standalone_host"
                )
        );
    }

    public Map<String, Object> inventorySnapshot() {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();
        for (EchoInventorySlot slot : playerInventory.slots()) {
            slot.stack().ifPresent(stack -> snapshot.merge(
                    stack.itemId().value(),
                    stack.quantity(),
                    (left, right) -> (Integer) left + (Integer) right
            ));
        }
        return Map.copyOf(snapshot);
    }

    public Map<String, Object> summary() {
        LinkedHashMap<String, Object> summary = new LinkedHashMap<>();
        summary.put("runtimeHostId", runtimeHostId);
        summary.put("playerId", playerId);
        summary.put("dimensionId", dimensionId);
        summary.put("gameTime", gameTime);
        summary.put("worldId", world.worldId());
        summary.put("loadedChunks", world.loadedChunkCount());
        summary.put("edits", edits.size());
        summary.put("playerBlock", player().blockPosition());
        summary.put("selectedSlot", player().selectedSlot());
        summary.put("inventory", inventorySnapshot());
        summary.put("waterUsed", mission.waterUsed());
        summary.put("foodUsed", mission.foodUsed());
        summary.put("scannerUsed", mission.scannerUsed());
        summary.put("shelterBuilt", mission.shelterBuilt());
        summary.put("status", mission.status());
        summary.put("completedObjectives", mission.completedObjectives());
        summary.put("missionMessage", mission.lastMessage());
        summary.put("hudObjectiveState", mission.hudObjectiveState());
        summary.put("notifications", hudNotifications.size());
        summary.put("lastFeedback", lastFeedback == null ? "" : lastFeedback);
        summary.put("worldStateRows", worldStateRows.size());
        summary.put("blockEntityRows", blockEntityRows.size());
        summary.put("saveDataRows", saveDataRows.size());
        summary.put("contentRegistrations", contentRegistrations.size());
        summary.put("events", eventLog.size());
        summary.put("packets", packetLog.size());
        return Map.copyOf(summary);
    }

    private void registerPlayerEntity() {
        EchoEntityId id = new EchoEntityId(playerId);
        EchoEntityDefinition definition = new EchoEntityDefinition(
                "echoashfallprotocol:entity/standalone_player",
                "Standalone Player",
                EchoEntityKind.PLAYER,
                100,
                0,
                "player"
        );
        entityStore.register(new EchoEntityState(
                id,
                definition,
                new EchoEntityPositionComponent(playerWorldPosition()),
                new EchoEntityHealthComponent(100, 100),
                new EchoEntityMovementComponent(0, false),
                new EchoEntityAiComponent("player", EchoEntityAiState.IDLE)
        ));
    }

    private void mirrorPlayerEntity() {
        EchoEntityId id = new EchoEntityId(playerId);
        EchoEntityState previous = entityStore.find(id).orElse(null);
        if (previous == null) {
            registerPlayerEntity();
            return;
        }
        entityStore.update(previous.withPosition(playerWorldPosition()));
    }

    private EchoWorldPosition playerWorldPosition() {
        EchoVoxelPlayerState player = player();
        return new EchoWorldPosition(
                (int) Math.floor(player.x()),
                (int) Math.floor(player.y()),
                (int) Math.floor(player.z())
        );
    }

    private static Map<String, Object> sanitized(Map<String, Object> values) {
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        if (values != null) {
            values.forEach((key, value) -> copy.put(requireText(key, "key"), value == null ? "" : value));
        }
        return Map.copyOf(copy);
    }

    private static EchoItemCategory itemCategory(String canonicalId) {
        if (canonicalId.contains("ration")
                || canonicalId.contains("water")
                || canonicalId.contains("rad_away")
                || canonicalId.contains("stim")
                || canonicalId.contains("warmer")) {
            return EchoItemCategory.CONSUMABLE;
        }
        if (canonicalId.contains("knife")
                || canonicalId.contains("scanner")
                || canonicalId.contains("filter")
                || canonicalId.contains("liner")
                || canonicalId.contains("keystone")
                || canonicalId.contains("beacon")) {
            return EchoItemCategory.TOOL;
        }
        return EchoItemCategory.MATERIAL;
    }

    private static String displayName(String canonicalId) {
        String local = canonicalId.substring(canonicalId.indexOf(':') + 1)
                .replace('_', ' ')
                .replace('/', ' ');
        StringBuilder builder = new StringBuilder();
        for (String word : local.split(" ")) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.isEmpty() ? canonicalId : builder.toString();
    }

    private static void requirePositive(int value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + " must be positive");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
