package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.item.EchoInventoryContainer;
import dev.echo.standalone.runtime.item.EchoInventoryId;
import dev.echo.standalone.runtime.item.EchoInventoryOperationResult;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemLootResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldPoi;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public final class EchoInteractionSystem {
    private static final String HYDRATE_OBJECTIVE = "ashfall:hydrate_survivor";
    private static final String TERMINAL_OBJECTIVE = "ashfall:activate_terminal";
    private static final String CACHE_OBJECTIVE = "ashfall:salvage_cache";
    private static final EchoItemId WATER_RATION = new EchoItemId(EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID);

    private final EchoWorldRuntimeResult world;
    private final EchoEntityRuntimeResult entities;
    private final EchoItemRuntimeResult items;
    private final EchoGameplayMissionState mission;
    private final EchoSurvivalState survival;
    private final EchoProgressionState progression;
    private final EchoFactionRuntime factions;
    private final EchoNotificationLog notifications;
    private boolean missionCompleteNotified;

    public EchoInteractionSystem(
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayMissionState mission,
            EchoSurvivalState survival,
            EchoProgressionState progression,
            EchoFactionRuntime factions,
            EchoNotificationLog notifications
    ) {
        this.world = Objects.requireNonNull(world, "world");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.items = Objects.requireNonNull(items, "items");
        this.mission = Objects.requireNonNull(mission, "mission");
        this.survival = Objects.requireNonNull(survival, "survival");
        this.progression = Objects.requireNonNull(progression, "progression");
        this.factions = Objects.requireNonNull(factions, "factions");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
    }

    public EchoGameplayInteractionResult drinkWater(EchoEntityId playerId) {
        Objects.requireNonNull(playerId, "playerId");
        EchoInventoryContainer pack = playerPack();
        EchoInventoryOperationResult consumed = items.operations().consume(pack, WATER_RATION, 1);
        if (!consumed.success()) {
            return new EchoGameplayInteractionResult("ashfall:drink_water", false, false, 0, consumed.reason());
        }
        survival.addHydration(15.0D);
        boolean completed = mission.completeObjective(HYDRATE_OBJECTIVE);
        award(completed, 15, "hydration_stabilized");
        notifications.add(
                EchoGameplayNotificationSeverity.INFO,
                "Clean water bottle consumed.",
                world.world().tick()
        );
        notifyMissionComplete();
        return new EchoGameplayInteractionResult("ashfall:drink_water", true, completed, completed ? 15 : 0, "hydrated");
    }

    public EchoGameplayInteractionResult activateTerminal(EchoEntityId playerId) {
        Objects.requireNonNull(playerId, "playerId");
        EchoEntityState player = entities.store().require(playerId);
        boolean alreadyCompleted = mission.objective(TERMINAL_OBJECTIVE)
                .orElseThrow(() -> new IllegalStateException("Missing terminal objective"))
                .completed();
        if (alreadyCompleted) {
            notifications.add(
                    EchoGameplayNotificationSeverity.INFO,
                    "Emergency terminal is online.",
                    world.world().tick()
            );
            notifyMissionComplete();
            return new EchoGameplayInteractionResult(
                    "ashfall:activate_terminal",
                    true,
                    false,
                    0,
                    "terminal_online"
            );
        }
        EchoWorldPoi terminal = world.query().poi("ashfall:terminal_pod")
                .orElseThrow(() -> new IllegalStateException("Missing terminal POI"));
        if (!player.worldPosition().equals(terminal.position())) {
            return new EchoGameplayInteractionResult(
                    "ashfall:activate_terminal",
                    false,
                    false,
                    0,
                    "too_far"
            );
        }
        boolean completed = mission.completeObjective(TERMINAL_OBJECTIVE);
        award(completed, 25, "terminal_online");
        notifications.add(
                EchoGameplayNotificationSeverity.INFO,
                "Emergency terminal is online.",
                world.world().tick()
        );
        notifyMissionComplete();
        return new EchoGameplayInteractionResult(
                "ashfall:activate_terminal",
                true,
                completed,
                completed ? 25 : 0,
                "terminal_online"
        );
    }

    public EchoGameplayInteractionResult salvageCrashCache(EchoEntityId playerId) {
        Objects.requireNonNull(playerId, "playerId");
        EchoEntityState player = entities.store().require(playerId);
        EchoWorldPoi cache = world.query().poi("ashfall:crash_cache")
                .orElseThrow(() -> new IllegalStateException("Missing crash cache POI"));
        if (!player.worldPosition().equals(cache.position())) {
            return new EchoGameplayInteractionResult(
                    "ashfall:salvage_cache",
                    false,
                    false,
                    0,
                    "too_far"
            );
        }
        EchoItemLootResult loot = items.lootRuntime().grant(items.debugLootTable(), playerPack());
        if (!loot.granted()) {
            return new EchoGameplayInteractionResult(
                    "ashfall:salvage_cache",
                    false,
                    false,
                    0,
                    loot.reason()
            );
        }
        boolean completed = mission.completeObjective(CACHE_OBJECTIVE);
        if (completed) {
            progression.awardExperience(30);
            progression.addMilestone("crash_cache_salvaged");
            factions.adjustReputation("ashfall:crash_survivors", 5);
            factions.adjustReputation("ashfall:wasteland_scavengers", -5);
        }
        notifications.add(
                EchoGameplayNotificationSeverity.INFO,
                "Crash cache salvaged.",
                world.world().tick()
        );
        notifyMissionComplete();
        return new EchoGameplayInteractionResult(
                "ashfall:salvage_cache",
                true,
                completed,
                completed ? 30 : 0,
                "cache_salvaged"
        );
    }

    private void award(boolean completed, int experience, String milestone) {
        if (!completed) {
            return;
        }
        progression.awardExperience(experience);
        progression.addMilestone(milestone);
    }

    private void notifyMissionComplete() {
        if (mission.status() == EchoGameplayMissionStatus.COMPLETED && !missionCompleteNotified) {
            progression.addMilestone("mission_secure_crash_site");
            notifications.add(
                    EchoGameplayNotificationSeverity.INFO,
                    "Crash site secured.",
                    world.world().tick()
            );
            missionCompleteNotified = true;
        }
    }

    private EchoInventoryContainer playerPack() {
        return items.inventoryStore().require(new EchoInventoryId("inventory:player-001"));
    }
}
