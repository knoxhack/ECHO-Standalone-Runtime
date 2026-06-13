# ECHO Standalone Minecraft Mod Emulator Plan

This plan expands the standalone runtime from a rendered voxel shell into a Minecraft-like client that can run ECHO Native Loader modules and AdapterCore-backed Minecraft mod content without launching Minecraft.

The target is not a blind clone of Minecraft internals. The target is a contract-driven runtime:

```text
ECHO Native Loader module -> AdapterCore contract -> standalone implementation
Minecraft data/assets -> normalized runtime registries -> standalone implementation
ScreenCore surface -> renderer-neutral frame -> LWJGL client draw pass
```

## Current Foundation

- The standalone runtime has a separate Gradle workspace and module boundaries.
- The client can open a native window, render chunks, draw a HUD and hotbar, and load runtime textures.
- The gameplay shell can create a session, stream chunks around the player, pause, save an in-memory snapshot, and continue.
- The UI shell now routes main menu, pause, options, controls, video, audio, mods, resource packs, loading, saving, fatal runtime errors, and HUD screens through ScreenCore-backed runtime primitives.
- Machine and terminal ScreenCore routes are live client surfaces with AdapterCore catalog counts, runtime content counts, machine state payloads, power graph telemetry, multi-network machine diagnostics, inventory-backed per-instance machine input/output controls, per-instance recipe selection, container-backed machine inventory slots, terminal command payloads, registered terminal routing, and deterministic back-stack behavior.
- Client machine and power graph state now persists through coordinate-backed `client/block_entities.tsv` rows; machine ticks reconcile from placed machine blocks, hotbar placement states carry block-entity identity immediately, item-pipe break/replacement disconnects and reconnects ticks, chunk unload/reload preserves cached machine block entities through save, multiple machine networks tick with independent coordinate-stable power/logistics state, ScreenCore can feed a selected machine instance from player inventory, extract compressed scrap back into player inventory, switch the selected recipe for a specific scrap press, and persist explicit machine slot item/count state through disk Continue; an edited item-pipe/ore-grinder layout round-trips through disk Continue, and legacy `client/machines.tsv` saves remain loadable.
- The debug overlay exposes active loaded chunk count, cached chunk count, frame-pacing counters, and machine graph/block-entity diagnostics for player-facing QA; the ScreenCore Diagnostics route now also surfaces active world slot/name, chunks, biome/hazard, vitals, entities, dropped items, frame pacing, audio backend/device/fallback state, audio mix/subtitle state, and machine block-entity counts without requiring the overlay. Runtime update/render failures now enter a ScreenCore `RUNTIME ERROR` route with support bundle export, Diagnostics, quit-to-title, and quit-client actions.
- The OpenGL Controls route now renders keybinding rows as selectable ScreenCore actions: selecting a row enters a pending key-capture state, pressing a supported key updates the active client settings, and reset-to-defaults remains available.
- The Mods ScreenCore route shows source-backed module scans, AdapterCore UI targets, and live native runtime content imports by domain and row.
- NeoForge `META-INF/neoforge.mods.toml` metadata is now discovered by the compatibility layer as diagnostics-only candidate input. Source/template catalog evidence currently reports 93 candidates with zero parse errors in `reports/echo/standalone/neoforge-compat-candidates.json`, while the module runtime smoke proves those TOML files are not activated as standalone descriptors.
- The launcher support bundle now includes generated module diagnostics, runtime registry fingerprints, bounded client save metadata, incompatible-mod save recovery evidence, ScreenCore route, renderer target, AdapterCore target diffs, voxel biome id/tint rendering evidence, and Native Loader ABI lifecycle/crash traces. The OpenGL client support bundle also includes the current ScreenCore snapshot, option tooltip detail, frame-pacing counters, and structured audio backend/device/fallback diagnostics so fatal-error screen exports preserve the player-visible failure summary and sound-device state.
- The client workbench resolves datapack item tags from mounted resource packs into visible, craftable item-runtime recipes.
- The client has a save-backed dropped item runtime for block drops, nearby pickup, debug overlay counts, and `client/dropped_items.tsv` restore.
- Mounted datapack and AdapterCore loot definitions bridge into the client item registry so block loot tables can create live dropped items, including tag-resolved item drops.
- Mounted Minecraft blockstate/model JSON now reaches OpenGL chunk-render planning for blockstate variant selection, multipart applies, parent-template face textures, atlas keys, JSON element bounds, cullface metadata, multi-element mesh quads, vanilla slab/stair/wall/fence/pane/trapdoor/door/cross/crop/bush/leaves/cactus/template-campfire/button/pressure-plate/weighted-pressure-plate/carpet/moss-carpet template geometry, abstract `block/block` and `thin_block` element-parent models, asymmetric face texture/UV/tint/cullface remapping for axis-aligned blockstate and multipart rotations, arbitrary element rotation `rescale` across Y 45-degree, X 22.5-degree, and signed Z -22.5-degree fixtures, `uvlock` texture-orientation parity for rotated blockstate variants, and blockstate-rotated arbitrary element cullface/tint behavior.
- Voxel fluid mechanics now have standalone runtime coverage for source placement, downward flow, horizontal spread, loaded chunk-boundary flow, solid blocking, water/lava hardening, bucket collection/placement, waterlogging host preservation, data-driven scheduled fluid updates, render surface heights from `fluidLevel`, player swim drag/buoyancy, jump-to-swim, crouch sinking, reduced movement speed in fluid, and real client save/load restore in `reports/echo/standalone/world-fluids.json`, `reports/echo/standalone/world-fluid-save-load.json`, `reports/echo/standalone/world-fluid-buckets.json`, `reports/echo/standalone/player-fluid-movement.json`, `reports/echo/standalone/world-fluid-waterlogging.json`, `reports/echo/standalone/world-fluid-scheduled-updates.json`, and `reports/echo/standalone/world-fluid-surface-height.json`.
- The OpenGL client atlas now reuses resolved block texture plans and decoded 64x64 resource-pack tiles across atlas rebuilds, reducing repeated PNG decode/log churn as chunk streaming introduces new block materials.
- The render material path now strips runtime-only machine metadata such as block entity ids and recipe progress from render identity while preserving model-relevant properties, preventing gameplay ticks from invalidating atlas signatures when visuals do not change.
- Fully model-resolved blocks no longer keep a duplicate base material atlas tile when every rendered face already maps to model texture atlas entries, reducing atlas layout/upload pressure for multipart/model-backed blocks.
- The OpenGL renderer now caches CPU chunk meshes by chunk id, chunk version, neighbor versions, seed, and biome source so render-region changes reuse unchanged meshes instead of rebuilding every visible chunk before the GPU upload cap can help.
- The idle main menu no longer rescans runtime surfaces every client tick; at the passive refresh boundary it records a lightweight title heartbeat instead of rereading save-slot, module, resource-pack, or workbench surfaces, stable title snapshots are reused between input/state changes, and unchanged settings are skipped before they reach input, audio, language, render, or window host hooks. The title panorama now uses a 19-line lightweight primitive budget on the idle route. The title menu stays on player-facing primary actions while Mods and Resource Packs remain available through Options.
- The OpenGL client now consumes native GLFW focus-loss events before gameplay input runs: active gameplay is routed to the ScreenCore Pause menu, the cursor is unlocked, stale one-shot input is cleared, the active world remains attached, Resume returns to gameplay, and `reports/echo/standalone/client-session-resilience.json` proves 8 pause/resume cycles plus 4 save/quit/continue cycles on the same slot.
- The debug overlay now exposes live frame-pacing and renderer/atlas pressure counters (`FRAME`, `RENDER`, and `ATLAS` lines), including fixed updates, sleep budget, slow-frame streaks, chunk upload backlog, CPU mesh cache hits/builds/evictions, atlas rebuild/reuse counts, decoded tile counts, and duplicate base tile removals.
- Resource-pack discovery now accepts Minecraft-style resource-pack folders, `.zip` archives, and `.jar` archives from standalone `resourcepacks/` and `packs/` roots; archive pack bytes load through the same namespaced resolver as directory mounts.
- A registry-backed asset coverage audit now walks 192 live AdapterCore block/item rows through the mounted resource-pack resolver and is wired into `check` as non-placeholder evidence. Current evidence shows 192 complete rows, with 122 of 122 blockstates, 122 of 122 block models, 70 of 70 item models, 300 of 300 declared textures, and 192 of 192 language keys resolved in `reports/echo/standalone/registry-asset-coverage.json`.

## Current Remaining Work

- Keep the player-facing standalone path on the OpenGL `echo-runtime-client`. The legacy app/headless runtime remains useful for deterministic smokes, but docs and generated evidence that present it as the current player runtime should be cleaned up or clearly labeled.
- Blockstate/model rendering now covers variants, multipart, parent texture chains, JSON elements, cullface metadata, multi-element mesh quads, rotated asymmetric face metadata, vanilla slab/stair/wall/fence/pane/trapdoor/door/cross/crop/bush/leaves/cactus/template-campfire/button/pressure-plate/weighted-pressure-plate/carpet/moss-carpet templates, abstract `block/block` and `thin_block` element-parent models, arbitrary element rotation `rescale` for the main vanilla axes and signed decimal angles, `uvlock` texture-orientation parity, and blockstate-rotated arbitrary element cullface/tint behavior. Remaining bounded rendering gaps are broader vanilla model template coverage beyond this fixture set and exotic model/rendering edge cases.
- Fluids now cover deterministic source/flow/hardening behavior at the voxel-kernel level, bucket collection/placement through gameplay item use, waterlogging host preservation, data-driven scheduled fluid updates through live client gameplay ticks, render surface heights from `fluidLevel` through CPU mesh faces/OpenGL upload vertices/software renderer bounds, player swim drag/buoyancy, jump-to-swim, crouch sinking, reduced movement speed in fluid, and source/flowing/hardened cell persistence through the real client save/load codec. Remaining fluid work is broader vanilla edge-case parity rather than the previously bounded surface-height gap.
- NeoForge metadata is now discovered and diagnosed, but direct NeoForge mod execution remains intentionally out of scope unless a module also declares an ECHO standalone descriptor or AdapterCore compatibility path.
- Public release evidence still has accepted warnings around signing/setup, disposable install/uninstall, and human packaged-EXE wall-clock sessions. If tester-visible lag persists, capture the debug overlay `FRAME`, `RENDER`, `ATLAS`, `ENTITIES`, and `DROP R` lines and plumb those live counters into the wall-clock reports; dropped-item saves, repeated texture decodes, runtime-only metadata churn, duplicate model/base atlas tiles, and full CPU remeshes on unchanged render-region chunks are no longer the obvious source.

## Non-Negotiable Rules

- ScreenCore owns every user-facing screen, modal, menu, HUD overlay, inventory, machine UI, error page, loading step, and mod diagnostics page.
- AdapterCore is the bridge for Minecraft mod parity. Standalone gameplay should bind to AdapterCore targets instead of duplicating NeoForge-only logic.
- ECHO Native Loader modules must declare capabilities, lifecycle hooks, dependencies, data roots, asset roots, and entrypoints before execution.
- The standalone runtime must not depend on Minecraft or NeoForge classes in core modules.
- Registries must be data-driven, frozen after loading, and report conflicts with exact namespace ids.
- Saves must be transactional, migration-aware, and able to reject incompatible mod sets before a world opens.

## Phase 1: Screen Shell And Flow

Implement the full Minecraft-like client shell with ScreenCore.

- Main menu: continue, new game, load game, options, quit, with Mods and Resource Packs reachable from Options instead of the idle title path.
- Loading screen: module scan, dependency resolve, asset atlas build, data registry freeze, world open, chunk warmup.
- Pause menu: resume, advancements/objectives, statistics, options, save, quit to title.
- Options: controls, video, audio, accessibility, resource packs, language.
- Error screens: fatal runtime crash recovery is ScreenCore-routed; remaining module-specific pages include mod load failure, missing dependency, resource pack conflict, and save migration required.
- World screens: world select, create world, seed/options, delete confirmation, backup warning.
- In-game screens: inventory, crafting, container, machine menus, terminal, chat, death/recovery.

Acceptance:

- Every screen has a stable `echoscreencore:*` id.
- Every screen emits an `EchoUiFrame`.
- Mouse, keyboard, escape/back behavior, and selected option state are deterministic.
- No gameplay input leaks through blocking screens.

## Phase 2: Resource Pack And Texture Runtime

Load Minecraft-style and ECHO-style assets into one resolver.

- Mount folders, jars, zips, generated resources, and runtime packs.
- Resolve namespaces with override priority and conflict diagnostics.
- Load `assets/<namespace>/textures`, `models`, `blockstates`, `lang`, `sounds`, and `atlases`.
- Generate missing texture fallbacks with clear diagnostics.
- Support hot reload from the Resource Packs screen.
- Build chunk, item, entity, GUI, font, particle, and block entity atlases.

Acceptance:

- A mod jar can provide textures and JSON assets without Minecraft.
- Directory, zip, and jar packs are discoverable from the Resource Packs screen.
- Atlas rebuilds do not require restarting the runtime.
- Missing textures are visible and reported by namespace/id.

## Phase 3: Registries And Data Loader

Create standalone equivalents for the runtime identities Minecraft mods expect.

- Block registry, item registry, entity registry, biome registry, sound registry, menu/screen registry.
- Tags for blocks, items, biomes, entities, fluids, damage types, and tool classes.
- Recipes, loot tables, advancements/objectives, structures, features, configured features, placed features.
- Status effects, attributes, particles, damage sources, stats, game rules, dimensions.
- Registry freeze and save compatibility fingerprints.

Acceptance:

- AdapterCore can enumerate all loaded runtime ids.
- Duplicate ids, missing parents, and invalid tags fail before gameplay starts.
- Registry fingerprints are stored in save metadata.

## Phase 4: Blocks And Voxel Behavior

Turn loaded block ids into playable voxel behavior.

- Block states with properties, default state, and model selection.
- Collision boxes, selection boxes, occlusion, light emission, hardness, resistance, drops.
- Placement, breaking, tools, block sounds, particles, and item form.
- Block entities for machines, containers, terminals, power nodes, cables, and modded storage.
- Tick hooks for scheduled block updates and random ticks.

Acceptance:

- Blocks placed by AdapterCore ids persist through save/load.
- The renderer chooses the correct model/texture for the active block state.
- Block entity state survives chunk unload/reload.

## Phase 5: Items, Inventory, Crafting, And Containers

Build the player and container item loop.

- ItemStack with id, count, damage, components/NBT-style payload, tags, tooltip data.
- Player inventory, hotbar, armor/offhand slots, cursor stack, drag/drop, shift-click, number key swaps.
- Creative/search tabs for dev and testing.
- Crafting grid, shaped/shapeless recipes, smelting/processing hooks.
- Container menus for chests, machines, terminals, power grids, and modded UIs through ScreenCore.

Acceptance:

- Inventory is fully usable from mouse and keyboard.
- Item and container screens are ScreenCore surfaces, not direct renderer hacks.
- Recipes and loot resolve from loaded data.

## Phase 6: World, Biomes, Dimensions, And Structures

Move from debug chunks to a real world runtime.

- Seeded chunk generation with biome maps, heightmaps, caves, ores, surface rules, structures, POIs.
- Dimension registry with world type, spawn rules, sky/fog/light settings, and travel restrictions.
- Biome runtime with temperature, downfall, fog/sky colors, ambient sounds, mob spawns, features.
- ECHO WorldCore regions and hazards from `data/<namespace>/echoworldcore/world_regions` and `world_hazards`.
- Chunk persistence, region files, chunk tickets, streaming priorities, and unload safety.

Acceptance:

- The same seed and mod set generate deterministic terrain.
- Biomes affect rendering, spawning, ambient audio, and gameplay hazards.
- WorldCore JSON overrides can change regions/hazards without recompiling.

## Phase 7: Player, Entities, AI, And Combat

Make the world alive enough for survival and mods.

- Player movement, collision, swimming/climbing/crouching/sprinting, camera bob, reach.
- Health, hunger, hydration, radiation/toxicity, armor, status effects, death/recovery.
- Entity components, pathing, simple goals, targeting, damage, drops, sounds, particles.
- Projectiles, boss bars, NPCs, drones, hostile mobs, spawn rules, despawn rules.
- Local entity sync model that can later feed multiplayer.

Acceptance:

- AdapterCore entity ids spawn real standalone entities.
- Combat, death, drops, and status effects are save-backed.
- Entity behavior remains deterministic in test mode.

## Phase 8: Gameplay Systems And Ashfall Parity

Bind Ashfall and other ECHO modules into the standalone game loop.

- Missions, objectives, rewards, tutorials, notifications, guidebook/wiki entries.
- Survival systems: hunger, hydration, temperature, radiation, oxygen/toxicity, fatigue.
- Factions, reputation, scripted encounters, POI discovery, scanner/lens/holomap hooks.
- Machines, power, logistics, storage, processing, generators, cables, pipes, multiblocks.
- Weather, hazards, biome events, rift events, curses, rituals, spells, recovery, relic tech.

Acceptance:

- Features listed in the AdapterCore parity matrix have standalone runtime targets.
- Mission/save fields are shared with AdapterCore contracts.
- Gameplay screens are ScreenCore-backed and not NeoForge menu dependencies.

## Phase 9: ECHO Native Loader And AdapterCore Mod Compatibility

Define exactly how mods run without Minecraft.

- Native Loader module descriptors with id, version, dependencies, entrypoints, capabilities, assets, data, configs.
- Classloader isolation, lifecycle events, reload events, crash containment, unload/reload state cleanup.
- AdapterCore entrypoint ABI for registry targets, gameplay hooks, UI targets, data hooks, save hooks, and service exports.
- Minecraft compatibility shims only where the contract is clear: metadata, data packs, resource packs, registries, tags, recipes, loot, configs, events, networking intents, menu intents.
- Explicit unsupported diagnostics for mods that require direct Minecraft client/server internals without an AdapterCore target.

Acceptance:

- Mods either load through a declared compatibility path or fail with a readable reason.
- Module capability permissions are enforced.
- The Mods screen shows loaded modules, disabled modules, dependencies, errors, exported AdapterCore targets, and live native runtime content imports.

## Phase 10: ScreenCore UI System

Make the client feel like a complete game, not a debug window.

- ScreenCore IDs for title, pause, options, controls, video, audio, accessibility, language, mods, resource packs, world select, create world, loading, saving, inventory, crafting, containers, terminals, death, chat, notifications.
- Theme bridge for Minecraft-like controls plus ECHO Ashfall styling.
- Reusable primitives: buttons, icon buttons, lists, sliders, toggles, tabs, scroll panes, slot grids, tooltips, mod badges, warning panels.
- Controller routing: keyboard, mouse, gamepad later, focus restoration, back stack, modal stack, input capture.
- Rendering bridge from `EchoUiFrame` to LWJGL draw commands.

Acceptance:

- A new screen can be added by publishing a ScreenCore surface and renderer model.
- Slot grids and menu buttons use consistent layout and input behavior.
- ScreenCore smokes cover routing, disabled actions, modal blocking, back navigation, and frame output.

## Phase 11: Save, Load, Profiles, And Migration

Make worlds durable.

- Save slots with OpenGL framebuffer thumbnails, generated fallback icons, last played metadata, mod list, resource pack list, registry fingerprint, runtime version.
- Transactional chunk, player, inventory, entity, block entity, mission, settings, and module state saves.
- Autosave, manual save, backup restore, corruption detection, migration planning, and incompatible mod warnings.
- Continue flow from main menu and pause menu.

Acceptance:

- Pulling power during save cannot leave the active save half-written.
- A changed mod set blocks unsafe world load until migration or backup confirmation.
- Save/load round trips preserve player, chunks, items, blocks, entities, missions, and settings.

## Phase 12: Input, Audio, Networking, Devtools, And QA

Finish the platform layer around the game loop.

- Input: rebindable controls through the OpenGL Controls route, categories, sensitivity, raw mouse, controller later.
- Audio: buses, subtitles, UI sounds, music, ambience, block/entity/item sounds, volume sliders.
- Networking: local client/server split, packets, entity/chunk/inventory sync, diagnostics.
- Devtools: registry browser, asset browser, chunk inspector, ScreenCore inspector, mod diagnostics, crash bundles.
- QA: headless smokes, screenshot checks, deterministic save/load tests, mod fixture tests, long-run stability tests.

Acceptance:

- Every release candidate has automated evidence for boot, menus, pack load, world load, inventory, save/load, mod diagnostics, and graceful crash handling.
- The support bundle contains logs, mod list, registry fingerprints, save metadata, incompatible-mod save recovery evidence, ScreenCore route, fatal-error screen snapshot, Native Loader ABI lifecycle/crash traces, voxel biome id/tint rendering evidence, renderer/device info, and OpenGL-client audio backend/device/fallback diagnostics.

## Immediate Next Slices

1. Expand the fixture-proven Native Loader ABI into real mounted-module launch flows with permission UX, module-specific crash containment, and reload/unload cleanup around user-selected module sets.
2. Continue replacing debug-only HUD/diagnostic affordances with player-facing ScreenCore surfaces.
3. Tighten block model rendering parity around broader vanilla model templates beyond the currently fixture-proven slab/stair/wall/fence/pane/trapdoor/door/cross/crop/bush/leaves/cactus/template-campfire/button/pressure-plate/weighted-pressure-plate/carpet/moss-carpet families and abstract `block/block`/`thin_block` element-parent models, plus remaining exotic model/rendering edge cases now that multipart/multi-element quads, rotated asymmetric face metadata, `rescale` across the main axes and signed decimal angles, `uvlock` orientation, and blockstate-rotated arbitrary element cullface behavior are fixture-proven.
4. Load data-driven biome, feature, and structure definitions into the live voxel generator beyond the current Ashfall deterministic biome source.
5. Carry captured save-slot thumbnail evidence and the automated focus-loss/session-resilience smoke into packaged long-session playtest reports so public-release testers can confirm World Select previews across real save/load/alt-tab sessions.
