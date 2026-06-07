# ECHO Runtime Boundaries

The standalone runtime has to stay clean enough that Ashfall can run through more than one backend. This document locks the first ownership boundaries.

## Ownership Map

| Layer | Owns | Does Not Own |
| --- | --- | --- |
| `echo-runtime-contracts` | Runtime interfaces, modes, lifecycle, diagnostics, service registry contracts | Minecraft classes, NeoForge APIs, launcher code, concrete renderer/audio/network backends |
| `echo-runtime-core` | Runtime orchestration, lifecycle coordination, service composition, diagnostics fan-out | Module discovery details, PackOS policy, world simulation, platform adapters |
| `echo-runtime-app` | Boot shell, main thread, shutdown, crash boundary wiring | Gameplay, rendering implementation, asset formats |
| `echo-runtime-modules` | Runtime module descriptors, dependency graph, lifecycle binding | NeoForge mod loading, unsafe classpath mutation |
| `echo-runtime-packos` | Pack profile, session, lockfile, mount plan, compatibility checks | Repair execution without confirmation, save migration execution |
| `echo-runtime-assets` | Asset/data pack mounts, namespaces, conflict and missing asset reports | Minecraft resource manager coupling |
| `echo-runtime-data` | Registries, schemas, tags, recipes, loot, data validation | Minecraft registry dependency |
| `echo-runtime-ui` | Screen stack, modal stack, input routing, themes, layout | Minecraft `Screen` dependency |
| `echo-runtime-save` | Manifests, save profiles, transactions, backup, migration plans | Automatic Minecraft save migration |
| `echo-runtime-world` | Minimal ECHO world model, regions, chunks, cells, hazards | Full terrain engine in Phase 14.1 |
| `echo-runtime-entity` | Standalone entity definitions and state contracts | Full mob AI in Phase 14.1 |
| `echo-runtime-item` | Standalone item, stack, inventory, recipe surface | Minecraft `ItemStack` dependency |
| `echo-runtime-gameplay` | Missions, objectives, hazards, weather, factions, survival state | Full Ashfall campaign |
| `echo-runtime-render` | Abstract render backend and scene contracts | Direct LWJGL exposure to gameplay systems |
| `echo-runtime-audio` | Abstract audio backend, buses, music, ambience | Minecraft sound engine dependency |
| `echo-runtime-network` | Packets, protocol, local client/server sync contracts | Full multiplayer in Phase 14.1 |
| `echo-runtime-scripting` | Declarative rule contracts and sandbox policy | Arbitrary unsafe scripting |
| `echo-runtime-compat` | Adapters and migration bridges | Runtime contracts, direct save mutation |
| `echo-runtime-testkit` | Deterministic fixtures and validation harnesses | Production runtime ownership |
| `echo-runtime-devtools` | Developer-only inspection and report tools | Required runtime path |

## Dependency Direction

Allowed dependency direction:

```text
feature module -> echo-runtime-contracts
runtime implementation -> echo-runtime-contracts
compat adapter -> echo-runtime-contracts
testkit/devtools -> runtime implementation modules
```

Disallowed dependency direction:

```text
echo-runtime-contracts -> Minecraft
echo-runtime-contracts -> NeoForge
echo-runtime-contracts -> LWJGL
echo-runtime-contracts -> compat adapters
core runtime -> concrete Minecraft/NeoForge adapter
gameplay systems -> renderer backend implementation
```

## Adapter Separation

Minecraft and NeoForge compatibility must live behind adapter modules or migration tooling. The standalone contracts should describe ECHO concepts such as items, worlds, UI, saves, diagnostics, and registries without naming Minecraft concepts as required runtime types.

Adapters may map Minecraft or NeoForge content into ECHO definitions later, but those adapters are not allowed to leak platform-specific types into contract signatures.

## Save and Migration Safety

Migration remains planning-only until explicitly approved by a later phase. Save tools must:

- report what they would migrate
- identify ownership and schema versions
- create backups before migrations
- avoid automatic migration of Minecraft saves
- preserve manual review points

## Current Preflight Note

The existing Native Loader Phase 14 readiness report is `PASS_WITH_WARNINGS` and marks Phase 14 blocked until tester feedback or no-crash evidence is collected. Phase 14.1 therefore remains an architecture lock only: no game process, classloader, renderer, adapter execution, or save mutation is started by this workspace.
