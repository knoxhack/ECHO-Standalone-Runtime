# ECHO Standalone Phase 14 Plan

Phase 14 turns ECHO toward a standalone game runtime foundation. The work stays layered so Ashfall can move gradually without breaking Minecraft-compatible Ashfall or the Native Loader path.

## Subphases

1. `14.1 Runtime Architecture Lock` defines the standalone workspace, contracts, lifecycle, modes, boundaries, docs, and deterministic reports.
2. `14.2 ECHO App Runtime` creates the bootable app shell, tick loop, lifecycle manager, crash boundary, logging bridge, and shutdown path.
3. `14.3 ECHO Module Runtime` discovers ECHO runtime module descriptors, validates dependencies, binds services, reports the module graph, and fails bad modules safely without executing module code.
4. `14.4 ECHO PackOS Runtime` loads pack profiles, creates pack sessions, reads lockfiles, verifies modules/features, refuses incompatible pack state, creates planning-only repair advice, and plans asset/data mounts.
5. `14.5 ECHO Resource + Asset Runtime` indexes namespaced assets and data definitions from mounted roots, resolves overrides, detects conflicts, reports missing assets, and supports deterministic dev hot reload.
6. `14.6 ECHO UI Runtime` builds a platform-independent screen stack, modal stack, input router, theme runtime, and Terminal shell prototype with deterministic smoke coverage.
7. `14.7 ECHO Save Runtime` defines save profiles, manifests, transactional writes, backups, migration plans, corruption checks, and recovery journals with deterministic smoke coverage.
8. `14.8 ECHO Data + Registry Runtime` creates standalone registries, schemas, tags, recipes, loot definitions, and freeze policies with deterministic smoke coverage.
9. `14.9 ECHO World Runtime Prototype` creates a small debug world model with regions, chunks, cells, hazards, weather fields, POIs, and save hooks with deterministic smoke coverage.
10. `14.10 ECHO Entity Runtime Prototype` defines entity ids, definitions, components, movement, health, simple AI shell, hostile scavenger prototype, and entity saves.
11. `14.11 ECHO Item + Inventory Runtime` defines item ids, stacks, inventories, tooltips, tags, recipes, loot, and inventory saves.
12. `14.12 ECHO Gameplay Systems Runtime` adds missions, objectives, progression, hazards, weather, factions, survival state, interactions, and notifications.
13. `14.13 ECHO Renderer Runtime Prototype` introduces an abstract renderer backend, window, scene, camera, layers, debug world render, entities, particles, and UI bridge.
14. `14.14 ECHO Audio Runtime` introduces audio backend contracts, buses, ambience, music, UI sounds, mission stingers, and volume profiles.
15. `14.15 ECHO Networking Runtime` adds protocol contracts, packet registry, local client/server handshake, simple entity sync, inventory sync, and diagnostics.
16. `14.16 ECHO Scripting / Rules Runtime` adds sandboxed declarative rules with triggers, conditions, actions, validation, and execution diagnostics.
17. `14.17 Compatibility + Migration Layer` maps ECHO-owned Minecraft/NeoForge content into standalone definitions and produces manual migration plans.
18. `14.18 Ashfall Standalone Vertical Slice` boots Ashfall as a tiny standalone prototype with test world, debug player, hostile entity, Terminal, inventory, hazard meter, objective, save/load, and clean exit.
19. `14.19 Standalone Launcher Integration` lets the launcher detect, verify, repair-plan, support-bundle, and launch the OpenGL standalone runtime without breaking external platform handoff.
20. `14.20 Standalone Alpha Readiness Gate` blocks unsafe alpha builds until runtime, modules, PackOS, assets, UI, saves, registries, world, entities, items, gameplay, renderer, audio, network, vertical slice, launcher, and support bundle checks are ready.

## Phase 15 Continuation

1. `15.1 Desktop Launcher` connects the real launcher shell to standalone OpenGL and external platform handoff modes.
2. `15.2 Windowed Runtime` adds window creation, resize handling, fullscreen/windowed mode switching, close behavior, and crash-safe shutdown around the existing renderer contract.
3. `15.3 Input Runtime` adds keyboard, mouse, and gamepad abstraction, rebindable actions, input contexts, Terminal focus handling, and deterministic UI/gameplay routing.
4. `15.4 Playable Player Controller` adds deterministic movement, collision, camera follow, interaction targeting, inventory shortcuts, hazard feedback, and debug traversal through the Ashfall test space.
5. `15.5 Renderer Upgrade` replaces the default recording renderer with a headless-safe software backend adapter that rasterizes tiles, sprites, UI, lighting, particles, and debug overlays into deterministic pixels.
6. `15.6 Audio Device Runtime` adds a Java Sound device backend, synthesized cue output, runtime volume/mute controls, mission/ambience/music/UI routing, and deterministic recording fallback.
7. `15.7 Ashfall Gameplay Slice Expansion` turns the tiny Ashfall slice into a short deterministic mission with intro, objectives, terminal flow, inventory use, hazard traversal, scavenger encounter, rewards, and fail/retry coverage.
8. `15.8 Save Profiles And Continue Flow` adds user-facing save slots, new game, continue, autosave, manual save, corruption warnings, backup restore UI, and compatibility-safe migration prompts.
9. `15.9 Installer And Distribution` verifies launcher/runtime app images, icons, version metadata, bundled runtime files, first-run checks, uninstall behavior, and release-time signing/installer warnings.
10. `15.10 Standalone Beta Readiness` creates a beta gate for launcher, window, input, renderer/audio fallback, save/restore, playable mission QA, support bundles, distribution warnings, and fail-closed CI release policy.

## First Playable Target

The first real playable target is a tiny Ashfall standalone vertical slice:

```text
boot runtime
load Ashfall pack profile
load theme/assets
show boot screen
open Terminal shell
create small test wasteland world
spawn debug player
spawn one hostile entity
move around simple space
show inventory
show hazard meter
save/load
exit cleanly
```

## Deferred Until After The Vertical Slice

The architecture intentionally defers advanced terrain generation, infinite world streaming, complex lighting, advanced physics, complex mob AI, full multiplayer, shader graph, marketplace, arbitrary scripting, automatic save migration, full Minecraft save import, full Ashfall campaign, vehicle runtime, machine automation, and faction war simulation.
