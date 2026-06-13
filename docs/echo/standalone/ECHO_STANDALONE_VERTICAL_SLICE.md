# ECHO Standalone Vertical Slice

Phase 14.18 boots Ashfall as a tiny standalone prototype inside the native runtime boundary. The slice is intentionally deterministic: it creates the debug world, player, hostile scavenger, inventory, gameplay mission, terminal UI, software renderer, Java Sound audio backend with recording fallback, in-memory local network, declarative rules runtime, compatibility planner, save/load round trip, and clean shutdown path without importing platform, native renderer, socket, or script-engine APIs.

## Runtime Entry

`EchoAshfallVerticalSliceRuntime` lives in `echo-runtime-app` because the app layer is the first standalone layer allowed to orchestrate every runtime subsystem together. It does not own subsystem behavior. World, entity, item, gameplay, UI, render, audio, network, scripting, compatibility, and save modules keep their own services and service registrations.

The boot sequence is:

1. create the Ashfall debug world.
2. spawn the debug survivor and hostile scavenger.
3. create the survivor pack and crash cache inventories.
4. start the crash-site mission and survival state.
5. run declarative debug rules.
6. apply storm and toxic-ash hazard pressure.
7. drink water, activate the terminal, move to the cache, salvage loot, and tick hostile AI.
8. render the terminal screen through the software renderer.
9. plan audio through the device-capable audio backend.
10. sync entities and inventories over local in-memory transport.
11. create the compatibility migration dry-run plan.
12. write, read, and check the vertical-slice save files.
13. close render/audio backends and report clean exit.

## Gameplay State

The slice completes `ashfall:secure_crash_site` with all three objectives done:

- `ashfall:hydrate_survivor`
- `ashfall:activate_terminal`
- `ashfall:salvage_cache`

The terminal objective is completed by the declarative rules runtime before the direct terminal interaction. The direct terminal action still succeeds, but it does not award duplicate objective progress. Final deterministic state:

- mission status: `COMPLETED`
- completed objectives: `3/3`
- player health: `91`
- hazard meter: `0.72`
- hydration: `70.00`
- ash exposure: `7.20`
- heat stress: `2.86`
- progression XP: `55`
- progression level: `2`
- occupied inventory slots: `5`
- notifications: `9`

## Terminal, Render, Audio

The active UI screen is `ashfall-vertical-slice-terminal`. It shows mission progress, terminal state, hazard and hydration meters, inventory slots, and the armed clean-shutdown path.

The renderer uses `EchoSoftwareRenderBackend`, opens a headless debug window, rasterizes one framebuffer, and submits `35` commands. The audio runtime uses `EchoJavaSoundAudioBackend`, attempts short PCM cue output when a device is available, falls back to `EchoRecordingAudioBackend` when needed, and records `3` initial playback events. Since the mission is complete before audio planning, the active-mission stinger is not queued.

## Network, Compatibility, Save

The local network runtime records four packets: handshake request, handshake acknowledgement, entity sync, and inventory sync. Entity sync sends two entity snapshots. Inventory sync sends two inventory snapshots and five occupied item stacks.

The compatibility layer remains available inside the slice and produces the Phase 14.17 dry-run migration plan: eight steps, one manual-review item, and no source mutations.

The save/load round trip writes three files under a temporary smoke-test profile:

- `vertical-slice/summary.json`
- `vertical-slice/player.json`
- `vertical-slice/inventory.json`

The manifest reloads with three files and the corruption check is healthy.

## Smoke Harness Coverage

`EchoRuntimeVerticalSliceSmokeHarness` proves:

- all core subsystem runtime results are service-bound.
- the vertical-slice result, summary, and save round trip are service-bound.
- the mission completes all objectives.
- player health, survival meters, XP, notifications, and inventory slots are deterministic.
- the terminal UI is active.
- render/audio/network counts match the deterministic slice.
- scripting and compatibility results remain available during the slice.
- save/load round trip is healthy.
- render and audio backends close cleanly.
- non-placeholder evidence is written to `runtime-vertical-slice.json` plus the `vertical-slice-*` boot, world, player, terminal, inventory, hazard, objective, render/audio, network, save/load, and clean-exit reports; `verifyStandaloneVerticalSliceRuntime` rejects bootstrap schemas for these reports.

## Out Of Scope

Phase 14.18 does not:

- launch a desktop window.
- require a real audio device to be available.
- open sockets.
- execute external scripts.
- import platform runtime APIs.
- run arbitrary user saves.
- implement full game input or free movement.

The next phase is Phase 14.19, the Standalone Launcher MVP.
