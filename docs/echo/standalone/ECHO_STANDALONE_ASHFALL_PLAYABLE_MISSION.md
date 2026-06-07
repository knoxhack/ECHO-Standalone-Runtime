# ECHO Standalone Ashfall Playable Mission

Phase 15.7 expands the tiny Ashfall slice into a deterministic short mission. It is still a runtime mission slice, not a complete campaign: it proves the playable loop and branch coverage while remaining safe for headless smoke tests.

## Mission Flow

`EchoAshfallPlayableMissionRuntime` runs these beats:

```text
intro beacon
terminal uplink
water ration
toxic ash traversal
crash cache salvage
scavenger encounter
patched filter reward
failure and retry branch
```

The mission uses the existing world, entity, item, gameplay, UI, input, player controller, renderer, and audio runtimes. Player actions route through `EchoPlayerController` and `EchoInputRuntime`, so the mission is not just direct system mutation.

## Coverage

The Phase 15.7 smoke proves:

- intro notification and mission start are represented.
- Terminal focus accepts the `uplink cache` command and exposes the cache route.
- water ration use consumes inventory and completes hydration.
- toxic ash traversal applies hazard feedback and ash exposure.
- crash cache salvage completes the core mission.
- scavenger AI attacks once and the player survives.
- encounter reward adds XP, milestones, faction changes, and crafts a patched filter.
- failure simulation downs the player in toxic ash.
- retry restores the main route and completes the mission.
- render and audio backends close cleanly after the mission.

## Boundary

Phase 15.7 does not add save-slot UX, full combat, authored cutscenes, asset-backed dialogue, long-form encounter AI, or campaign progression beyond this short deterministic mission.
