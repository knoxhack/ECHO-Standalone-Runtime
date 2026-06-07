# ECHO Standalone Entity Runtime Prototype

Phase 14.10 adds the first standalone entity runtime prototype. It defines entity ids, definitions, components, movement, health, a small AI shell, an Ashfall hostile scavenger prototype, and an entity save hook that writes through the Phase 14.7 save runtime.

The runtime remains model-first. It does not render entities, run full combat, pathfind across streamed worlds, or import Minecraft entity data.

## Runtime Pieces

- `EchoEntityRuntime` creates and service-binds deterministic debug entities.
- `EchoEntityId` provides stable entity identity and file-safe save keys.
- `EchoEntityDefinition` stores definition id, display name, kind, max health, movement speed, and AI profile.
- `EchoEntityState` combines position, health, movement, and AI components.
- `EchoEntityStore` keeps deterministic entity ordering and update boundaries.
- `EchoEntityMovementSystem` moves entities across `EchoWorldQuery` cells and respects blocked world cells.
- `EchoEntityAiSystem` runs a small hostile-scavenger pursuit shell.
- `EchoEntitySaveHook` writes entity summaries and entity state files through `EchoSaveRuntimeResult`.

## Debug Entities

The Phase 14.10 debug entity set is:

```text
player-001: echo:debug_player at 0,0,0
scavenger-001: ashfall:hostile_scavenger at 3,0,1
```

The player is a manual actor with 100 health. The scavenger is a hostile AI actor with 35 health and the `hostile_scavenger` profile.

## Movement And AI

Movement is world-aware and deterministic:

- entities move on the same Y plane.
- movement speed caps Chebyshev distance per move.
- missing cells reject movement as outside world.
- blocked cells reject movement when the entity is blocked by world geometry.

The initial AI shell finds the first living player and moves hostile scavengers one cell toward that player. If the hostile starts adjacent to the player, it applies a small deterministic damage packet instead.

## Save Hook

The entity save hook writes:

```text
entities/summary.json
entities/player-001.json
entities/scavenger-001.json
```

These files are committed through the save runtime transaction path, included in the save manifest, and validated by the corruption checker.

## Smoke Harness Coverage

The Phase 14.10 smoke harness proves:

- entity runtime result and store are service-bound.
- the debug entity set contains one player and one hostile.
- player movement succeeds into an open cell.
- blocked world cells prevent movement.
- hostile scavenger AI pursues the player.
- health damage updates entity state deterministically.
- entity save hook writes summary and entity files.
- the resulting save manifest tracks entity files.
- the save corruption checker reports the entity save as healthy.

## Out Of Scope

Phase 14.10 does not:

- render entities
- run animation controllers
- pathfind through streamed chunks
- execute full combat or status effects
- simulate inventory
- run behavior trees
- import Minecraft or NeoForge entity data

The next phase is Phase 14.11, the ECHO Item and Inventory Runtime.
