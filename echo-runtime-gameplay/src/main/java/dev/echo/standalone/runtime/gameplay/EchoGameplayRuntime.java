package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public final class EchoGameplayRuntime {
    public EchoGameplayRuntimeResult createDebugGameplay(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");

        EchoEntityState player = entities.store().all().stream()
                .filter(entity -> entity.definition().kind() == EchoEntityKind.PLAYER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Gameplay debug runtime requires a player entity"));
        EchoGameplayMissionState mission = new EchoGameplayMissionState(
                "ashfall:secure_crash_site",
                "Secure The Crash Site",
                List.of(
                        new EchoGameplayMissionObjective(
                                "ashfall:hydrate_survivor",
                                "Hydrate the survivor",
                                EchoGameplayObjectiveStatus.ACTIVE,
                                0,
                                1
                        ),
                        new EchoGameplayMissionObjective(
                                "ashfall:activate_terminal",
                                "Bring the emergency terminal online",
                                EchoGameplayObjectiveStatus.ACTIVE,
                                0,
                                1
                        ),
                        new EchoGameplayMissionObjective(
                                "ashfall:salvage_cache",
                                "Salvage the crash cache",
                                EchoGameplayObjectiveStatus.ACTIVE,
                                0,
                                1
                        )
                )
        );
        EchoSurvivalState survival = new EchoSurvivalState(player.id(), 55.0D, 0.0D, 0.0D);
        EchoProgressionState progression = new EchoProgressionState();
        EchoFactionRuntime factions = new EchoFactionRuntime();
        factions.register(new EchoFactionStanding("ashfall:crash_survivors", "Crash Survivors", 10));
        factions.register(new EchoFactionStanding("ashfall:wasteland_scavengers", "Wasteland Scavengers", -25));
        EchoNotificationLog notifications = new EchoNotificationLog();
        notifications.add(EchoGameplayNotificationSeverity.INFO, "Crash-site mission started.", world.world().tick());

        EchoHazardGameplaySystem hazardSystem = new EchoHazardGameplaySystem(world, entities, survival, notifications);
        EchoWeatherGameplaySystem weatherSystem = new EchoWeatherGameplaySystem(world, survival, notifications);
        EchoInteractionSystem interactionSystem = new EchoInteractionSystem(
                world,
                entities,
                items,
                mission,
                survival,
                progression,
                factions,
                notifications
        );
        EchoGameplaySaveHook saveHook = new EchoGameplaySaveHook(
                mission,
                survival,
                progression,
                factions,
                notifications
        );
        EchoGameplayRuntimeResult result = new EchoGameplayRuntimeResult(
                mission,
                survival,
                progression,
                factions,
                notifications,
                hazardSystem,
                weatherSystem,
                interactionSystem,
                saveHook
        );
        services.register(EchoGameplayRuntimeResult.class, result);
        services.register(EchoGameplayMissionState.class, mission);
        services.register(EchoSurvivalState.class, survival);
        services.register(EchoProgressionState.class, progression);
        services.register(EchoFactionRuntime.class, factions);
        services.register(EchoNotificationLog.class, notifications);
        services.register(EchoHazardGameplaySystem.class, hazardSystem);
        services.register(EchoWeatherGameplaySystem.class, weatherSystem);
        services.register(EchoInteractionSystem.class, interactionSystem);
        services.register(EchoGameplaySaveHook.class, saveHook);
        return result;
    }

    public EchoAshfallStandaloneMissionRuntime createAshfallFirstPlayableLoop(EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(services, "services");
        EchoAshfallStandaloneMissionRuntime runtime = new EchoAshfallStandaloneMissionRuntime();
        runtime.apply(
                EchoAshfallStandaloneMissionRuntime.EchoObjectiveTrigger.PLAYER_SPAWNED,
                "echoashfallprotocol:drop_pod");
        services.register(EchoAshfallStandaloneMissionRuntime.class, runtime);
        services.register(EchoGameplayMissionState.class, runtime.mission());
        return runtime;
    }
}
