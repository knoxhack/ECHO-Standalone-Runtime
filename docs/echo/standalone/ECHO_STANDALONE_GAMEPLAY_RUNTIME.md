# ECHO Standalone Gameplay Systems Runtime

Phase 14.12 adds the first standalone gameplay systems runtime. It connects the world, entity, and item runtimes into a deterministic Ashfall gameplay loop with missions, objectives, progression, hazards, weather, survival state, factions, interactions, notifications, and gameplay save output.

The runtime remains platform-neutral. It does not import Minecraft advancement data, effects, attributes, capabilities, commands, network sync, or NeoForge event buses.

## Runtime Pieces

- `EchoGameplayRuntime` creates and service-binds the debug gameplay state and systems.
- `EchoGameplayMissionState` tracks the active mission and deterministic objective progress.
- `EchoProgressionState` tracks experience, level, and milestones.
- `EchoSurvivalState` tracks hydration, ash exposure, and heat stress.
- `EchoFactionRuntime` tracks faction standings and hostility.
- `EchoHazardGameplaySystem` applies world hazard pressure to survival state and entity health.
- `EchoWeatherGameplaySystem` applies current weather pressure to survival state.
- `EchoInteractionSystem` handles water, terminal, and crash-cache interactions.
- `EchoNotificationLog` records deterministic gameplay notifications.
- `EchoGameplaySaveHook` writes mission, survival, progression, faction, notification, and summary data through `EchoSaveRuntimeResult`.

## Debug Loop

The Phase 14.12 debug mission is:

```text
mission: ashfall:secure_crash_site
objectives:
  ashfall:hydrate_survivor
  ashfall:activate_terminal
  ashfall:salvage_cache
```

The smoke loop applies toxic ash exposure, applies ash-storm weather pressure, consumes a water ration, activates the emergency terminal, moves the player to the crash cache, salvages the cache, completes all objectives, advances progression to level 2, adjusts faction reputation, and records notifications.

## Gameplay Save Hook

The gameplay save hook writes:

```text
gameplay/summary.json
gameplay/mission.json
gameplay/survival.json
gameplay/progression.json
gameplay/factions.json
gameplay/notifications.json
```

These files are committed through the save runtime transaction path, included in the save manifest, and validated by the corruption checker.

## Smoke Harness Coverage

The Phase 14.12 smoke harness proves:

- gameplay runtime result and key systems are service-bound.
- the debug mission contains three objectives.
- survival state receives hazard exposure and weather heat stress.
- hazard gameplay damages player health through the entity store.
- water interaction consumes a ration and completes the hydration objective.
- terminal interaction completes the terminal objective.
- cache interaction grants loot, completes the salvage objective, and adjusts faction reputation.
- progression reaches level 2 with deterministic milestones.
- all mission objectives complete and mission progress reaches 100 percent.
- notifications are recorded in deterministic order.
- gameplay save hook writes six gameplay files.
- the resulting save manifest tracks gameplay files.
- the save corruption checker reports the gameplay save as healthy.

It also writes concrete non-placeholder evidence to `runtime-gameplay.json`, `gameplay-missions.json`, `gameplay-objectives.json`, `gameplay-progression.json`, `gameplay-hazards.json`, `gameplay-weather.json`, `gameplay-survival.json`, `gameplay-factions.json`, `gameplay-interactions.json`, `gameplay-notifications.json`, and `gameplay-save-hooks.json`. `verifyStandaloneGameplayRuntime` regenerates that evidence, rejects bootstrap schemas, and requires concrete `PASS` fields for service binding, mission completion, objective completion, deterministic XP/level/milestone progression, toxic ash damage/exposure, ash-storm heat pressure, hydration and water consumption, faction reputation shifts, water/terminal/cache interactions, deterministic notifications, and gameplay save manifest/corruption coverage.

## Out Of Scope

Phase 14.12 does not:

- run a full campaign
- integrate renderer UI feedback
- sync gameplay state over a network
- import Minecraft advancements or effects
- load external scripting rules
- execute behavior-tree AI
- attach equipment stats to combat

The next phase is Phase 14.13, the ECHO Renderer Runtime Prototype.
