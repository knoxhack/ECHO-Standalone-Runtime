package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.gameplay.EchoGameplayNotificationSeverity;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.player.EchoVoxelPlayerState;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelBlockBreakResult;
import dev.echo.standalone.runtime.world.EchoVoxelHit;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class StandaloneEchoRuntimeHost {
    public static final String API_VERSION = "1.0.0-rc1";

    private final StandaloneRuntimeHostContext context;
    private final StandaloneRuntimeMutationLedgerSink ledger;
    private final PlayerInventory playerInventory;
    private final PlayerState playerState;
    private final WorldBlocks worldBlocks;
    private final WorldState worldState;
    private final Structures structures;
    private final BlockEntities blockEntities;
    private final Capabilities capabilities;
    private final Events events;
    private final Packets packets;
    private final Hud hud;
    private final SaveData saveData;
    private final ContentRegistries contentRegistries;

    public StandaloneEchoRuntimeHost(
            StandaloneRuntimeHostContext context,
            StandaloneRuntimeMutationLedgerSink ledger
    ) {
        this.context = Objects.requireNonNull(context, "context");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.playerInventory = new PlayerInventory();
        this.playerState = new PlayerState();
        this.worldBlocks = new WorldBlocks();
        this.worldState = new WorldState();
        this.structures = new Structures();
        this.blockEntities = new BlockEntities();
        this.capabilities = new Capabilities();
        this.events = new Events();
        this.packets = new Packets();
        this.hud = new Hud();
        this.saveData = new SaveData();
        this.contentRegistries = new ContentRegistries();
    }

    public StandaloneRuntimeHostContext context() {
        return context;
    }

    public StandaloneRuntimeMutationLedgerSink ledger() {
        return ledger;
    }

    public PlayerInventory playerInventory() {
        return playerInventory;
    }

    public PlayerState playerState() {
        return playerState;
    }

    public WorldBlocks worldBlocks() {
        return worldBlocks;
    }

    public WorldState worldState() {
        return worldState;
    }

    public Structures structures() {
        return structures;
    }

    public BlockEntities blockEntities() {
        return blockEntities;
    }

    public Capabilities capabilities() {
        return capabilities;
    }

    public Events events() {
        return events;
    }

    public Packets packets() {
        return packets;
    }

    public Hud hud() {
        return hud;
    }

    public SaveData saveData() {
        return saveData;
    }

    public ContentRegistries contentRegistries() {
        return contentRegistries;
    }

    public StandaloneRuntimeHostResult applyHostCall(Map<String, Object> hostCall) {
        Map<String, Object> call = sanitized(hostCall);
        Map<String, Object> payload = mapValue(call.get("payload"));
        String eventName = textValue(call, "eventName", textValue(call, "event", textValue(payload, "eventName", "resource_reload")));
        String actionId = textValue(call, "actionId", textValue(call, "action", "adaptercore.host_call." + eventName));
        if (!call.containsKey("payload")) {
            payload = call;
        }
        return applyAdapterCoreAction(actionId, eventName, payload);
    }

    public StandaloneRuntimeHostResult applyAdapterCoreAction(
            String actionId,
            String eventName,
            Map<String, Object> payload
    ) {
        String normalizedEvent = requireText(eventName, "eventName");
        Map<String, Object> safePayload = sanitized(payload);
        return switch (normalizedEvent) {
            case "player_join" -> playerState.writePersistentState(
                    actionId,
                    normalizedEvent,
                    "joinState",
                    context.playerId(),
                    safePayload
            );
            case "client_tick", "world_tick" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> tick(normalizedEvent, safePayload)
            );
            case "item_use" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> itemUse(safePayload)
            );
            case "block_place" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> blockPlace(safePayload)
            );
            case "block_break" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> blockBreak(safePayload)
            );
            case "entity_interact", "screen_open" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> interact(safePayload)
            );
            case "command_execution" -> execute(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    () -> command(safePayload)
            );
            case "save_load" -> saveData.write(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    Map.of("operation", textValue(safePayload, "operation", "load"), "payload", safePayload)
            );
            case "resource_reload" -> events.publish(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload
            );
            default -> failed(
                    actionId,
                    normalizedEvent,
                    canonicalId(safePayload, normalizedEvent),
                    safePayload,
                    "Unsupported AdapterCore event: " + normalizedEvent
            );
        };
    }

    private StandaloneRuntimeHostResult execute(
            String actionId,
            String eventName,
            String canonicalId,
            Map<String, Object> payload,
            HostMutation mutation
    ) {
        String safeActionId = requireText(actionId, "actionId");
        String safeEventName = requireText(eventName, "eventName");
        String safeCanonicalId = requireText(canonicalId, "canonicalId");
        Map<String, Object> safePayload = sanitized(payload);
        Map<String, Object> before = context.summary();
        boolean saveTouched = false;
        boolean feedbackEmitted = false;
        String status = "NOOP";
        String failureReason = "";
        Map<String, Object> details = Map.of();
        try {
            MutationOutcome outcome = mutation.apply();
            status = outcome.status();
            details = outcome.snapshot();
            if (outcome.mutated()) {
                context.appendEvent(safeEventName, safeCanonicalId, safePayload);
                if (outcome.feedbackEmitted()) {
                    context.publishFeedback(EchoGameplayNotificationSeverity.INFO, outcome.message());
                    feedbackEmitted = true;
                }
                EchoSaveCommitResult commit = context.commitLiveSnapshot(
                        safeActionId,
                        safeEventName,
                        safeCanonicalId
                );
                saveTouched = commit.filesWritten() > 0;
                if (!saveTouched) {
                    status = "FAILED";
                    failureReason = "Mutation did not write standalone save data";
                }
            }
        } catch (Exception exception) {
            status = "FAILED";
            failureReason = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();
            details = Map.of("exception", exception.getClass().getName());
        }
        Map<String, Object> after = context.summary();
        StandaloneRuntimeMutationLedgerSink.StandaloneRuntimeMutationLedgerEntry entry = ledger.append(
                safeActionId,
                safeEventName,
                safeCanonicalId,
                context.runtimeHostId(),
                safePayload,
                before,
                after,
                status,
                failureReason,
                saveTouched,
                feedbackEmitted
        );
        return new StandaloneRuntimeHostResult(
                status,
                safeActionId,
                safeEventName,
                safeCanonicalId,
                saveTouched,
                feedbackEmitted,
                entry.sequence(),
                failureReason,
                details,
                after
        );
    }

    private StandaloneRuntimeHostResult failed(
            String actionId,
            String eventName,
            String canonicalId,
            Map<String, Object> payload,
            String reason
    ) {
        Map<String, Object> snapshot = context.summary();
        StandaloneRuntimeMutationLedgerSink.StandaloneRuntimeMutationLedgerEntry entry = ledger.append(
                requireText(actionId, "actionId"),
                requireText(eventName, "eventName"),
                requireText(canonicalId, "canonicalId"),
                context.runtimeHostId(),
                sanitized(payload),
                snapshot,
                snapshot,
                "FAILED",
                requireText(reason, "reason"),
                false,
                false
        );
        return new StandaloneRuntimeHostResult(
                "FAILED",
                actionId,
                eventName,
                canonicalId,
                false,
                false,
                entry.sequence(),
                reason,
                Map.of(),
                snapshot
        );
    }

    private MutationOutcome tick(String eventName, Map<String, Object> payload) {
        long ticks = Math.max(1L, longValue(payload, "ticks", 1L));
        double seconds = doubleValue(payload, "seconds", ticks / 20.0D);
        boolean moved = booleanValue(payload, "moved", "client_tick".equals(eventName));
        context.advanceGameTime(ticks);
        context.world().tickLoadedBlocks(context.gameTime());
        context.mission().tick(
                context.world(),
                context.player(),
                moved,
                seconds,
                context.bridge().hazardTable(),
                context.bridge().shelterProfile(),
                context.bridge().survivalProfile()
        );
        context.putWorldState("tick/" + context.gameTime(), Map.of(
                "eventName", eventName,
                "ticks", ticks,
                "seconds", seconds,
                "moved", moved,
                "missionStatus", context.mission().status()
        ));
        return MutationOutcome.mutated(
                "Standalone runtime tick consumed: " + eventName,
                Map.of("gameTime", context.gameTime(), "mission", context.mission().hudObjectiveState())
        );
    }

    private MutationOutcome itemUse(Map<String, Object> payload) {
        String itemId = canonicalId(payload, "item_use");
        context.selectHotbarItem(itemId);
        EchoVoxelBlock item = context.requireVoxel(itemId);
        EchoAdapterCoreStandaloneContentBridge bridge = context.bridge();
        boolean consumed = false;
        boolean changed = false;
        switch (itemId) {
            case EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID -> {
                changed = context.mission().useWaterRation(bridge.survivalProfile());
                consumed = context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.FIELD_RATION_ITEM_ID -> {
                changed = context.mission().useFoodRation(bridge.survivalProfile());
                consumed = context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID ->
                    changed = context.mission().useEmergencyScanner(context.world(), context.player());
            case EchoAdapterCoreStandaloneContentBridge.ENERGY_CELL_ITEM_ID -> {
                changed = context.mission().chargeBasicBattery(item, bridge.machinePowerProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.GAS_MASK_ITEM_ID ->
                    changed = context.mission().equipGasMask(item, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.BASIC_FILTER_ITEM_ID -> {
                changed = context.mission().fixMaskFilter(item, bridge.expeditionSafetyProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.ADVANCED_FILTER_ITEM_ID -> {
                changed = context.mission().craftAdvancedFilter(item, bridge.expeditionSafetyProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.RAD_AWAY_ITEM_ID -> {
                changed = context.mission().useRadAway(item, bridge.fieldRecoveryProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.STIM_PACK_ITEM_ID -> {
                changed = context.mission().useStimPack(item, bridge.fieldRecoveryProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.HAND_WARMER_ITEM_ID -> {
                changed = context.mission().useHandWarmer(item, bridge.fieldRecoveryProfile());
                consumed = changed && context.removeItem(itemId, 1);
            }
            case EchoAdapterCoreStandaloneContentBridge.THERMAL_LINER_ITEM_ID ->
                    changed = context.mission().installThermalLiner(item, bridge.fieldRecoveryProfile());
            case EchoAdapterCoreStandaloneContentBridge.RETURN_BEACON_ITEM_ID ->
                    changed = context.mission().placeReturnBeacon(item, bridge.fieldRecoveryProfile());
            case EchoAdapterCoreStandaloneContentBridge.RETURN_KEYSTONE_ITEM_ID ->
                    changed = context.mission().bindReturnKeystone(item, bridge.fieldRecoveryProfile());
            default -> changed = context.mission().interact(target(payload), context.player());
        }
        if (!changed) {
            return MutationOutcome.noop("Item use did not match a live AdapterCore item: " + itemId);
        }
        return MutationOutcome.mutated(
                context.mission().lastMessage(),
                Map.of(
                        "itemId", itemId,
                        "consumed", consumed,
                        "inventory", context.inventorySnapshot(),
                        "mission", context.mission().hudObjectiveState()
                )
        );
    }

    private MutationOutcome blockPlace(Map<String, Object> payload) {
        String blockId = canonicalId(payload, "block_place");
        int x = intValue(payload, "x", 2);
        int y = intValue(payload, "y", 5);
        int z = intValue(payload, "z", 2);
        EchoVoxelBlock block = context.requireVoxel(blockId);
        EchoVoxelBlock before = context.world().blockAt(x, y, z);
        boolean placed = context.world().setBlockAt(x, y, z, block);
        if (!placed) {
            return MutationOutcome.noop("Block placement outside loaded standalone world: " + blockId);
        }
        context.recordWorldEdit(x, y, z, before, block);
        context.removeItem(blockId, 1);
        boolean missionTouched = applyPlacementMissionMutation(block, x, y, z);
        if (isMachineBlock(blockId)) {
            String key = positionKey(x, y, z);
            context.receiveEnergy(key, 5);
            context.putBlockEntityState(key, Map.of(
                    "blockId", blockId,
                    "simulation", "standalone_machine",
                    "energyStored", context.energyStored(key),
                    "lastTick", context.gameTime()
            ));
        }
        return MutationOutcome.mutated(
                missionTouched ? context.mission().lastMessage() : "Block placed: " + block.displayName(),
                Map.of(
                        "blockId", blockId,
                        "x", x,
                        "y", y,
                        "z", z,
                        "before", before.id(),
                        "after", block.id(),
                        "missionTouched", missionTouched
                )
        );
    }

    private MutationOutcome blockBreak(Map<String, Object> payload) {
        int x = intValue(payload, "x", (int) Math.floor(context.player().x()));
        int y = intValue(payload, "y", Math.max(0, (int) Math.floor(context.player().y()) - 1));
        int z = intValue(payload, "z", (int) Math.floor(context.player().z()));
        EchoVoxelBlock before = context.world().blockAt(x, y, z);
        if (before.air()) {
            return MutationOutcome.noop("Block break targeted air");
        }
        double speed = context.bridge().toolProfile().speedFor(before, booleanValue(payload, "toolAssisted", false));
        EchoVoxelBlockBreakResult result = context.world().attemptBreakBlock(
                x,
                y,
                z,
                doubleValue(payload, "accumulatedSeconds", 999.0D),
                speed
        );
        if (!result.broken()) {
            return MutationOutcome.noop("Block break not complete: " + result.summary());
        }
        context.recordWorldEdit(x, y, z, before, EchoVoxelBlock.AIR);
        context.grantItem(before.id(), 1);
        context.mission().markHazardCleared(before);
        context.mission().scavenge(before, context.bridge().scavengeTable(), positionKey(x, y, z));
        context.mission().recoverScrapMetal(before, context.bridge().toolProfile());
        return MutationOutcome.mutated(
                context.mission().lastMessage(),
                Map.of(
                        "blockId", before.id(),
                        "x", x,
                        "y", y,
                        "z", z,
                        "breakSummary", result.summary()
                )
        );
    }

    private MutationOutcome interact(Map<String, Object> payload) {
        EchoVoxelHit hit = target(payload);
        boolean changed = context.mission().interact(hit, context.player());
        if (!changed) {
            return MutationOutcome.noop("Interaction target was not handled");
        }
        return MutationOutcome.mutated(
                context.mission().lastMessage(),
                Map.of("target", hit == null ? "raycast" : hit.block().id(), "mission", context.mission().hudObjectiveState())
        );
    }

    private MutationOutcome command(Map<String, Object> payload) {
        String command = textValue(payload, "command", "");
        if (command.equals("status") || command.equals("mission") || command.equals("mission status")) {
            context.mission().tick(
                    context.world(),
                    context.player(),
                    false,
                    0.0D,
                    context.bridge().hazardTable(),
                    context.bridge().shelterProfile(),
                    context.bridge().survivalProfile()
            );
        } else if (command.equals("ashfall:extract")) {
            context.mission().attemptExtraction(context.player());
        } else if (command.equals("ashfall:set_power_priority")) {
            context.mission().setPowerPriority(context.bridge().midgameProgressionProfile());
        } else if (command.equals("ashfall:overclock_machine")) {
            context.mission().overclockMachine(context.bridge().midgameProgressionProfile());
        } else {
            context.putWorldState("command/" + context.gameTime(), Map.of("command", command));
        }
        return MutationOutcome.mutated(
                context.mission().lastMessage(),
                Map.of("command", command, "mission", context.mission().hudObjectiveState())
        );
    }

    private MutationOutcome missionObjectiveCompleted(String eventName, Map<String, Object> payload) {
        String target = canonicalId(payload, eventName);
        boolean changed;
        if (target.contains("field_manual")
                || "native_ui_mission_log".equals(textValue(payload, "source", ""))) {
            changed = context.mission().readFieldManual(context.bridge().fieldManualItem());
        } else {
            context.mission().tick(
                    context.world(),
                    context.player(),
                    booleanValue(payload, "moved", false),
                    doubleValue(payload, "seconds", 0.0D),
                    context.bridge().hazardTable(),
                    context.bridge().shelterProfile(),
                    context.bridge().survivalProfile()
            );
            changed = true;
        }
        return changed
                ? MutationOutcome.mutated(context.mission().lastMessage(), Map.of(
                        "target", target,
                        "mission", context.mission().hudObjectiveState(),
                        "source", textValue(payload, "source", "")))
                : MutationOutcome.noop("Mission event was not handled: " + target);
    }

    private boolean applyPlacementMissionMutation(EchoVoxelBlock block, int x, int y, int z) {
        EchoAdapterCoreStandaloneContentBridge bridge = context.bridge();
        String id = block.id();
        return switch (id) {
            case EchoAdapterCoreStandaloneContentBridge.SHELTER_ANCHOR_BLOCK_ID ->
                    context.mission().markShelterBuilt(block, x, y, z, context.player());
            case EchoAdapterCoreStandaloneContentBridge.RAIN_COLLECTOR_BLOCK_ID ->
                    context.mission().markRainCollectorBuilt(block, bridge.waterLoopProfile());
            case EchoAdapterCoreStandaloneContentBridge.WATER_PURIFIER_BLOCK_ID ->
                    context.mission().markWaterPurifierBuilt(block, bridge.waterLoopProfile());
            case EchoAdapterCoreStandaloneContentBridge.HAND_RECYCLER_BLOCK_ID ->
                    context.mission().markHandRecyclerBuilt(block, bridge.fieldWorkshopProfile());
            case EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID ->
                    context.mission().markMicroGeneratorBuilt(block, bridge.fieldPowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID ->
                    context.mission().routePowerCable(block, bridge.fieldPowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.ENERGY_METER_BLOCK_ID ->
                    context.mission().installEnergyMeter(block, bridge.fieldPowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.SCRAP_DYNAMO_BLOCK_ID ->
                    context.mission().markScrapDynamoBuilt(block, bridge.machinePowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID ->
                    context.mission().markBatteryBankBuilt(block, bridge.machinePowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.THERMAL_BURNER_BLOCK_ID ->
                    context.mission().markThermalBurnerBuilt(block, bridge.machinePowerProfile());
            case EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID ->
                    context.mission().markScrapPressBuilt(block, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID ->
                    context.mission().installItemPipe(block, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.FACTORY_CONTROLLER_BLOCK_ID ->
                    context.mission().markFactoryControllerBuilt(block, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.RESEARCH_LAB_BLOCK_ID ->
                    context.mission().markResearchLabBuilt(block, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.REINFORCED_POWER_CABLE_BLOCK_ID ->
                    context.mission().upgradePowerCable(block, bridge.midgameProgressionProfile());
            case EchoAdapterCoreStandaloneContentBridge.THERMAL_ARRAY_BLOCK_ID ->
                    context.mission().markThermalArrayBuilt(block, bridge.expeditionSafetyProfile());
            case EchoAdapterCoreStandaloneContentBridge.ATMOSPHERIC_SCRUBBER_BLOCK_ID ->
                    context.mission().markAtmosphericScrubberBuilt(block, bridge.expeditionSafetyProfile());
            case EchoAdapterCoreStandaloneContentBridge.RADIATION_CLEANSER_BLOCK_ID ->
                    context.mission().markRadiationCleanserBuilt(block, bridge.expeditionSafetyProfile());
            case EchoAdapterCoreStandaloneContentBridge.FIELD_MED_BAY_BLOCK_ID ->
                    context.mission().markFieldMedBayBuilt(block, bridge.expeditionSafetyProfile());
            case EchoAdapterCoreStandaloneContentBridge.FILTER_WORKBENCH_BLOCK_ID ->
                    context.mission().markFilterWorkbenchBuilt(block, bridge.advancedExpeditionProfile());
            case EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID ->
                    context.mission().markOreGrinderBuilt(block, bridge.advancedExpeditionProfile());
            case EchoAdapterCoreStandaloneContentBridge.ISOTOPE_REFINER_BLOCK_ID ->
                    context.mission().markIsotopeRefinerBuilt(block, bridge.advancedExpeditionProfile());
            case EchoAdapterCoreStandaloneContentBridge.RELAY_STATION_BLOCK_ID ->
                    context.mission().activateRelayStation(
                            block,
                            bridge.relayScannerLensItem(),
                            bridge.advancedExpeditionProfile()
                    );
            default -> false;
        };
    }

    private EchoVoxelHit target(Map<String, Object> payload) {
        if (payload.containsKey("x") && payload.containsKey("y") && payload.containsKey("z")) {
            int x = intValue(payload, "x", 0);
            int y = intValue(payload, "y", 0);
            int z = intValue(payload, "z", 0);
            EchoVoxelBlock block = payload.containsKey("blockId") || payload.containsKey("canonicalId")
                    ? context.requireVoxel(textValue(payload, "blockId", canonicalId(payload, "target")))
                    : context.world().blockAt(x, y, z);
            return new EchoVoxelHit(x, y, z, 0, 1, 0, block, 0.0D);
        }
        return context.world().raycast(
                context.player().x(),
                context.player().eyeY(),
                context.player().z(),
                context.player().yawDegrees(),
                context.player().pitchDegrees(),
                context.player().reach()
        ).orElse(null);
    }

    private boolean isMachineBlock(String blockId) {
        return blockId.contains("generator")
                || blockId.contains("dynamo")
                || blockId.contains("battery")
                || blockId.contains("burner")
                || blockId.contains("recycler")
                || blockId.contains("purifier")
                || blockId.contains("press")
                || blockId.contains("pipe")
                || blockId.contains("controller")
                || blockId.contains("lab")
                || blockId.contains("scrubber")
                || blockId.contains("cleanser")
                || blockId.contains("med_bay")
                || blockId.contains("grinder")
                || blockId.contains("refiner");
    }

    public final class PlayerInventory {
        public StandaloneRuntimeHostResult grant(String actionId, String eventName, String canonicalId, int amount) {
            return execute(actionId, eventName, canonicalId, Map.of("amount", amount), () -> {
                context.grantItem(canonicalId, amount);
                return MutationOutcome.mutated("Inventory granted: " + canonicalId, Map.of(
                        "inventory", context.inventorySnapshot(),
                        "mission", context.mission().hudObjectiveState()
                ));
            });
        }

        public StandaloneRuntimeHostResult remove(String actionId, String eventName, String canonicalId, int amount) {
            return execute(actionId, eventName, canonicalId, Map.of("amount", amount), () -> {
                boolean removed = context.removeItem(canonicalId, amount);
                return removed
                        ? MutationOutcome.mutated("Inventory removed: " + canonicalId, context.inventorySnapshot())
                        : MutationOutcome.noop("Inventory item missing: " + canonicalId);
            });
        }

        public Map<String, Object> snapshot() {
            return context.inventorySnapshot();
        }
    }

    public final class PlayerState {
        public StandaloneRuntimeHostResult teleport(
                String actionId,
                String eventName,
                double x,
                double y,
                double z
        ) {
            return execute(actionId, eventName, context.playerId(), Map.of("x", x, "y", y, "z", z), () -> {
                EchoVoxelPlayerState previous = context.player();
                context.setPlayerState(new EchoVoxelPlayerState(
                        x,
                        y,
                        z,
                        previous.velocityY(),
                        previous.yawDegrees(),
                        previous.pitchDegrees(),
                        previous.grounded(),
                        previous.crouching(),
                        previous.sprinting(),
                        previous.selectedSlot(),
                        previous.reach()
                ));
                return MutationOutcome.mutated("Player moved to " + context.player().blockPosition(), Map.of(
                        "playerBlock", context.player().blockPosition()
                ));
            });
        }

        public StandaloneRuntimeHostResult bindRespawn(String actionId, String eventName, int x, int y, int z) {
            return writePersistentState(actionId, eventName, "respawn", positionKey(x, y, z), Map.of("x", x, "y", y, "z", z));
        }

        public StandaloneRuntimeHostResult grantAdvancement(String actionId, String eventName, String advancementId) {
            return writePersistentState(actionId, eventName, "advancement", advancementId, Map.of("granted", true));
        }

        public StandaloneRuntimeHostResult writePersistentState(
                String actionId,
                String eventName,
                String key,
                Object value,
                Map<String, Object> payload
        ) {
            return execute(actionId, eventName, context.playerId(), payload, () -> {
                context.putSaveData("player/" + key, Map.of("value", Objects.toString(value, "")));
                return MutationOutcome.mutated("Player state stored: " + key, Map.of("key", key));
            });
        }
    }

    public final class WorldBlocks {
        public StandaloneRuntimeHostResult setBlock(
                String actionId,
                String eventName,
                String blockId,
                int x,
                int y,
                int z
        ) {
            return execute(actionId, eventName, blockId, Map.of("x", x, "y", y, "z", z), () -> {
                EchoVoxelBlock block = context.requireVoxel(blockId);
                EchoVoxelBlock before = context.world().blockAt(x, y, z);
                boolean changed = context.world().setBlockAt(x, y, z, block);
                if (!changed) {
                    return MutationOutcome.noop("World block outside loaded chunk: " + positionKey(x, y, z));
                }
                context.recordWorldEdit(x, y, z, before, block);
                return MutationOutcome.mutated("World block set: " + blockId, Map.of(
                        "before", before.id(),
                        "after", blockId,
                        "position", positionKey(x, y, z)
                ));
            });
        }

        public StandaloneRuntimeHostResult clearBlock(String actionId, String eventName, int x, int y, int z) {
            return setBlock(actionId, eventName, EchoVoxelBlock.AIR.id(), x, y, z);
        }

        public EchoVoxelBlock blockState(int x, int y, int z) {
            return context.world().blockAt(x, y, z);
        }

        public boolean isLoaded(int x, int y, int z) {
            return !context.world().blockAt(x, y, z).air() || y >= 0;
        }
    }

    public final class WorldState {
        public StandaloneRuntimeHostResult writeMarker(
                String actionId,
                String eventName,
                String markerId,
                Map<String, Object> values
        ) {
            return execute(actionId, eventName, markerId, values, () -> {
                context.putWorldState("marker/" + markerId, values);
                return MutationOutcome.mutated("World marker stored: " + markerId, Map.of("markerId", markerId));
            });
        }

        public StandaloneRuntimeHostResult writeWeatherState(String actionId, String eventName, Map<String, Object> values) {
            return writeMarker(actionId, eventName, "weather", values);
        }

        public StandaloneRuntimeHostResult writeRouteState(String actionId, String eventName, Map<String, Object> values) {
            return writeMarker(actionId, eventName, "route", values);
        }
    }

    public final class Structures {
        public StandaloneRuntimeHostResult placeStructure(
                String actionId,
                String eventName,
                String structureId,
                int x,
                int y,
                int z
        ) {
            return execute(actionId, eventName, structureId, Map.of("x", x, "y", y, "z", z), () -> {
                EchoVoxelBlock block = context.bridge().structureCacheBlock();
                EchoVoxelBlock before = context.world().blockAt(x, y, z);
                if (!context.world().setBlockAt(x, y, z, block)) {
                    return MutationOutcome.noop("Structure origin outside loaded world: " + structureId);
                }
                context.recordWorldEdit(x, y, z, before, block);
                context.putWorldState("poi/" + structureId, Map.of(
                        "x", x,
                        "y", y,
                        "z", z,
                        "blockId", block.id()
                ));
                return MutationOutcome.mutated("Structure POI placed: " + structureId, Map.of(
                        "structureId", structureId,
                        "position", positionKey(x, y, z)
                ));
            });
        }
    }

    public final class BlockEntities {
        public StandaloneRuntimeHostResult tick(String actionId, String eventName, String key) {
            return execute(actionId, eventName, key, Map.of("key", key), () -> {
                int energy = context.receiveEnergy(key, 1);
                context.putBlockEntityState(key, Map.of(
                        "simulation", "standalone_machine",
                        "energyDelta", energy,
                        "energyStored", context.energyStored(key),
                        "lastTick", context.gameTime()
                ));
                return MutationOutcome.mutated("Block entity ticked: " + key, context.blockEntityState(key));
            });
        }

        public Map<String, Object> snapshot(String key) {
            return context.blockEntityState(key);
        }

        public StandaloneRuntimeHostResult applySnapshot(
                String actionId,
                String eventName,
                String key,
                Map<String, Object> values
        ) {
            return execute(actionId, eventName, key, values, () -> {
                context.putBlockEntityState(key, values);
                return MutationOutcome.mutated("Block entity snapshot applied: " + key, context.blockEntityState(key));
            });
        }
    }

    public final class Capabilities {
        public StandaloneRuntimeHostResult insertItem(
                String actionId,
                String eventName,
                String capabilityKey,
                String canonicalId,
                int amount
        ) {
            return execute(actionId, eventName, canonicalId, Map.of("capability", capabilityKey, "amount", amount), () -> {
                int inserted = context.insertCapabilityItem(capabilityKey, canonicalId, amount);
                return MutationOutcome.mutated("Capability item inserted: " + canonicalId, Map.of("inserted", inserted));
            });
        }

        public StandaloneRuntimeHostResult extractItem(
                String actionId,
                String eventName,
                String capabilityKey,
                String canonicalId,
                int amount
        ) {
            return execute(actionId, eventName, canonicalId, Map.of("capability", capabilityKey, "amount", amount), () -> {
                int extracted = context.extractCapabilityItem(capabilityKey, canonicalId, amount);
                return extracted > 0
                        ? MutationOutcome.mutated("Capability item extracted: " + canonicalId, Map.of("extracted", extracted))
                        : MutationOutcome.noop("Capability item missing: " + canonicalId);
            });
        }

        public StandaloneRuntimeHostResult receiveEnergy(
                String actionId,
                String eventName,
                String capabilityKey,
                int amount
        ) {
            return execute(actionId, eventName, capabilityKey, Map.of("amount", amount), () -> {
                int received = context.receiveEnergy(capabilityKey, amount);
                return MutationOutcome.mutated("Capability energy received: " + amount, Map.of("received", received));
            });
        }

        public StandaloneRuntimeHostResult extractEnergy(
                String actionId,
                String eventName,
                String capabilityKey,
                int amount
        ) {
            return execute(actionId, eventName, capabilityKey, Map.of("amount", amount), () -> {
                int extracted = context.extractEnergy(capabilityKey, amount);
                return extracted > 0
                        ? MutationOutcome.mutated("Capability energy extracted: " + extracted, Map.of("extracted", extracted))
                        : MutationOutcome.noop("Capability energy empty: " + capabilityKey);
            });
        }

        public Map<String, Object> readCapability(String capabilityKey) {
            return Map.of(
                    "items", context.capabilityInventory(capabilityKey),
                    "energy", context.energyStored(capabilityKey)
            );
        }
    }

    public final class Events {
        public StandaloneRuntimeHostResult publish(
                String actionId,
                String eventName,
                String canonicalId,
                Map<String, Object> payload
        ) {
            return execute(actionId, eventName, canonicalId, payload, () -> {
                if ("client_tick".equals(eventName) || "world_tick".equals(eventName)) {
                    return tick(eventName, payload);
                }
                if ("mission.objective_completed".equals(eventName) || "mission.completed".equals(eventName)) {
                    return missionObjectiveCompleted(eventName, payload);
                }
                if ("command_execution".equals(eventName)) {
                    return command(payload);
                }
                if ("player.scanner_used".equals(eventName) || "native.ui.use_scanner".equals(eventName)) {
                    return scannerUsed(payload);
                }
                if ("player.terminal_opened".equals(eventName)) {
                    LinkedHashMap<String, Object> terminalPayload = new LinkedHashMap<>(sanitized(payload));
                    terminalPayload.putIfAbsent("blockId", EchoAdapterCoreStandaloneContentBridge.FIELD_TERMINAL_BLOCK_ID);
                    terminalPayload.putIfAbsent("x", 3);
                    terminalPayload.putIfAbsent("y", 4);
                    terminalPayload.putIfAbsent("z", 3);
                    MutationOutcome terminalInteraction = interact(Map.copyOf(terminalPayload));
                    if (terminalInteraction.mutated()) {
                        return terminalInteraction;
                    }
                }
                if (eventName.startsWith("native.ui.")) {
                    return nativeUiAction(eventName, payload);
                }
                context.putWorldState("event/" + eventName + "/" + context.gameTime(), payload);
                return MutationOutcome.mutated("AdapterCore event consumed: " + eventName, payload);
            });
        }
    }

    private MutationOutcome scannerUsed(Map<String, Object> payload) {
        LinkedHashMap<String, Object> scannerPayload = new LinkedHashMap<>(sanitized(payload));
        scannerPayload.putIfAbsent("canonicalId", EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID);
        scannerPayload.putIfAbsent("itemId", EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID);
        scannerPayload.putIfAbsent("target", "echoashfallprotocol:scan_first_poi");
        scannerPayload.putIfAbsent("source", "native_ui_scanner");
        MutationOutcome scannerUse = itemUse(Map.copyOf(scannerPayload));
        if (scannerUse.mutated()) {
            return scannerUse;
        }
        context.putWorldState("scanner/" + context.gameTime(), scannerPayload);
        return MutationOutcome.mutated("Standalone scanner event consumed: player.scanner_used", Map.of(
                "itemId", EchoAdapterCoreStandaloneContentBridge.EMERGENCY_SCANNER_ITEM_ID,
                "target", textValue(scannerPayload, "target", "echoashfallprotocol:scan_first_poi"),
                "mission", context.mission().hudObjectiveState(),
                "payload", scannerPayload
        ));
    }

    private MutationOutcome nativeUiAction(String eventName, Map<String, Object> payload) {
        Map<String, Object> safePayload = sanitized(payload);
        String target = canonicalId(safePayload, eventName);
        context.putWorldState("native-ui/" + eventName + "/" + context.gameTime(), safePayload);
        context.mission().tick(
                context.world(),
                context.player(),
                false,
                0.0D,
                context.bridge().hazardTable(),
                context.bridge().shelterProfile(),
                context.bridge().survivalProfile()
        );
        return MutationOutcome.mutated("Native UI action consumed: " + eventName, Map.of(
                "eventName", eventName,
                "target", target,
                "mission", context.mission().hudObjectiveState(),
                "payload", safePayload
        ));
    }

    public final class Packets {
        public StandaloneRuntimeHostResult sendToPlayer(
                String actionId,
                String eventName,
                String packetName,
                Map<String, Object> payload
        ) {
            return execute(actionId, eventName, packetName, payload, () -> {
                context.appendPacket(packetName, context.playerId(), payload);
                return MutationOutcome.mutated("Packet delivered to standalone UI: " + packetName, payload);
            });
        }

        public StandaloneRuntimeHostResult broadcast(
                String actionId,
                String eventName,
                String packetName,
                Map<String, Object> payload
        ) {
            return sendToPlayer(actionId, eventName, packetName, payload);
        }
    }

    public final class Hud {
        public StandaloneRuntimeHostResult publishNotification(
                String actionId,
                String eventName,
                String message,
                Map<String, Object> payload
        ) {
            return execute(actionId, eventName, "echo-runtime-ui:hud_notification", payload, () ->
                    MutationOutcome.mutated(requireText(message, "message"), payload));
        }
    }

    public final class SaveData {
        public StandaloneRuntimeHostResult write(
                String actionId,
                String eventName,
                String key,
                Map<String, Object> values
        ) {
            return execute(actionId, eventName, key, values, () -> {
                context.putSaveData(key, values);
                return MutationOutcome.mutated("Save data stored: " + key, Map.of("key", key));
            });
        }

        public StandaloneRuntimeHostResult delete(String actionId, String eventName, String key) {
            return execute(actionId, eventName, key, Map.of("delete", true), () -> {
                boolean deleted = context.deleteSaveData(key);
                return deleted
                        ? MutationOutcome.mutated("Save data deleted: " + key, Map.of("key", key))
                        : MutationOutcome.noop("Save data missing: " + key);
            });
        }

        public Map<String, Object> read(String key) {
            return context.readSaveData(key);
        }
    }

    public final class ContentRegistries {
        public StandaloneRuntimeHostResult register(
                String actionId,
                String eventName,
                String contentId,
                Map<String, Object> registration
        ) {
            Map<String, Object> safeRegistration = sanitized(registration);
            String safeContentId = requireText(contentId, "contentId");
            return execute(actionId, eventName, safeContentId, safeRegistration, () -> {
                boolean changed = context.putContentRegistration(safeContentId, safeRegistration);
                context.putSaveData("content-registries/" + safeContentId, safeRegistration);
                return changed
                        ? MutationOutcome.mutated(
                                "AdapterCore content registered: " + safeContentId,
                                Map.of("registration", safeRegistration)
                        )
                        : MutationOutcome.noop("AdapterCore content registration already current: " + safeContentId);
            });
        }

        public java.util.List<Map<String, Object>> registrations(String domain) {
            return context.contentRegistrations(domain);
        }
    }

    public record StandaloneRuntimeHostResult(
            String status,
            String actionId,
            String eventName,
            String canonicalId,
            boolean saveTouched,
            boolean feedbackEmitted,
            int ledgerSequence,
            String failureReason,
            Map<String, Object> details,
            Map<String, Object> hostSnapshot
    ) {
        public StandaloneRuntimeHostResult {
            status = requireText(status, "status");
            actionId = requireText(actionId, "actionId");
            eventName = requireText(eventName, "eventName");
            canonicalId = requireText(canonicalId, "canonicalId");
            if (ledgerSequence <= 0) {
                throw new IllegalArgumentException("ledgerSequence must be positive");
            }
            failureReason = failureReason == null ? "" : failureReason;
            details = sanitized(details);
            hostSnapshot = sanitized(hostSnapshot);
        }

        public boolean mutated() {
            return "MUTATED".equals(status);
        }
    }

    @FunctionalInterface
    private interface HostMutation {
        MutationOutcome apply() throws Exception;
    }

    private record MutationOutcome(
            boolean mutated,
            String status,
            String message,
            boolean feedbackEmitted,
            Map<String, Object> snapshot
    ) {
        private MutationOutcome {
            status = requireText(status, "status");
            message = requireText(message, "message");
            snapshot = sanitized(snapshot);
        }

        static MutationOutcome mutated(String message, Map<String, Object> snapshot) {
            return new MutationOutcome(true, "MUTATED", message, true, snapshot);
        }

        static MutationOutcome noop(String message) {
            return new MutationOutcome(false, "NOOP", message, false, Map.of());
        }
    }

    private static String canonicalId(Map<String, Object> payload, String fallback) {
        for (String key : new String[]{"canonicalId", "itemId", "blockId", "contentId", "targetId", "id"}) {
            Object value = payload.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text.trim();
            }
        }
        return requireText(fallback, "fallback");
    }

    private static int intValue(Map<String, Object> payload, String key, int fallback) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.parseInt(text.trim());
        }
        return fallback;
    }

    private static long longValue(Map<String, Object> payload, String key, long fallback) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text.trim());
        }
        return fallback;
    }

    private static double doubleValue(Map<String, Object> payload, String key, double fallback) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Double.parseDouble(text.trim());
        }
        return fallback;
    }

    private static boolean booleanValue(Map<String, Object> payload, String key, boolean fallback) {
        Object value = payload.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.parseBoolean(text.trim());
        }
        return fallback;
    }

    private static String textValue(Map<String, Object> payload, String key, String fallback) {
        Object value = payload.get(key);
        return value instanceof String text && !text.isBlank() ? text.trim() : fallback;
    }

    private static Map<String, Object> mapValue(Object value) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            map.forEach((key, mapValue) -> result.put(Objects.toString(key, ""), mapValue == null ? "" : mapValue));
        }
        return Map.copyOf(result);
    }

    private static Map<String, Object> sanitized(Map<String, Object> values) {
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        if (values != null) {
            values.forEach((key, value) -> copy.put(requireText(key, "key"), value == null ? "" : value));
        }
        return Map.copyOf(copy);
    }

    private static String positionKey(int x, int y, int z) {
        return String.format(Locale.ROOT, "%d,%d,%d", x, y, z);
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
