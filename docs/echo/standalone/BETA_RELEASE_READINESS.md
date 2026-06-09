# ECHO Standalone Beta Release Readiness

Status: BETA READY WITH ACCEPTED WARNINGS.

Decision date: 2026-05-31.

## Release Gate

The current standalone build is acceptable for beta tester handoff. The beta gate reports `READY_WITH_WARNINGS` with 91 checks, 0 blockers, and 3 warnings from the live beta readiness smoke.

Evidence:

- `.\gradlew.bat check --console=plain`: PASS, 190 tasks, 142 executed, 48 up-to-date.
- `.\gradlew.bat runStandaloneFullWorkspaceBuildSmoke --console=plain`: PASS, parent `buildEchoWorkspace -PechoAddonSet=all`, 473 parent tasks, 20 executed, 453 up-to-date.
- `reports/echo/standalone/beta-readiness-gate.json`: `READY_WITH_WARNINGS`, 0 blockers.
- `reports/echo/standalone/packaged-exe-wallclock-smoke.json`: `PASS`, refreshed packaged OpenGL client image, `EchoClientMain` launch target, and 15/15 alive packaged-client probe samples.
- `reports/echo/standalone/packaged-exe-wallclock-strict-rehearsal.json`: `PASS`, current OpenGL packaged EXE, 20/20 process samples, visible `ECHO Ashfall - Standalone Client` window, and no native mod-loader handoff.
- `reports/echo/standalone/distribution-package-reproducibility-strict.json`: `PASS`, strict two-refresh portable package reproducibility with stable SHA-256 `c08f6efc4675dc440225781e0e9119e5e14aaca6aa25221c42e31e792797bb27`.
- `reports/echo/standalone/packaged-opengl-client-image.json`: `PASS`, jpackage app image config targets `dev.echo.standalone.runtime.client.EchoClientMain` and excludes the superseded `EchoRuntimeMain`.
- `reports/echo/standalone/full-workspace-build-smoke.json`: `PASS`, exit code 0.

## Required Integration Checks

- Beta readiness: PASS with accepted beta warnings.
- AdapterCore parity: PASS, 373 parity rows, save contract `echoashfallprotocol:save/live_mission_state`.
- AdapterCore module coverage: PASS, 95/95 active modules, 0 gaps.
- Full playable route: PASS, 63/63 objectives, route 12/12.
- Save/load: PASS, world edits/player/hotbar/dropped items/coordinate-backed editable machine block entity power state with placement metadata, break/reconnect lifecycle, chunk unload/reload cache persistence/mission/render checksum restored; named save slots cover autosave/manual backups, corruption restore, manual-only migration prompt, and incompatible-mod Continue blocking.
- ScreenCore shell polish: PASS, title panorama, stage-keyed loading tips, pointer-driven settings sliders, accessibility settings, language settings, Mods runtime content inventory, and coordinate-backed editable machine/terminal ScreenCore surfaces with state payload rows, machine lifecycle evidence, multi-network machine diagnostics, player-inventory-backed per-instance machine input/output controls, per-instance recipe selection, and container-backed machine slot persistence are covered by deterministic client smokes.
- Block model chunk rendering: PASS, mounted Minecraft blockstate/model JSON reaches the OpenGL chunk-render planning path for blockstate variant selection, parent-template face textures, atlas keys, and JSON element bounds.
- Voxel biome chunk rendering: PASS, generated chunks carry biome ids through block state and chunk cell metadata, while OpenGL chunk materials carry biome id/tint color and client fog/ambience state switches by biome.
- Visible HUD feedback: PASS, held item preview, block-break feedback, and action particles are covered by deterministic framebuffer pixel evidence.
- Save recovery: PASS through save profile flow warning, backup restore path, and incompatible-mod blocked-slot recovery prompt.
- Inventory UX: PASS, drag, split, hotbar assignment, tooltip, data-tag-aware workbench recipes, data-driven block loot drops, disabled states, keyboard/mouse flow.
- Dropped item loop: PASS, `runStandaloneClientDroppedItemSmoke` covers live dropped block items, nearby pickup, debug overlay counts, and save/restore through `client/dropped_items.tsv`; `runStandaloneClientDroppedItemPhysicsSmoke` covers fixed-step dropped-item physics budget and settled-item chunk-index skipping; `runStandaloneClientBlockLootDataSmoke` covers mounted datapack block loot and item-tag resolution into live dropped items.
- Terminal branching: PASS, terminal framebuffer states, diagnostics, extraction authorization, registered terminal route bridging, terminal command payloads, and terminal surface back-stack behavior.
- Support bundle diagnostics: PASS, launcher export targets standalone OpenGL and includes generated module diagnostics, runtime registry fingerprints, bounded save metadata, incompatible-mod save recovery evidence, ScreenCore route, machine/terminal state-payload evidence, blockstate/model chunk-render evidence, voxel biome id/tint rendering evidence, machine placement/break lifecycle evidence, chunk cache persistence evidence, ScreenCore multi-network machine diagnostics, per-instance machine input/output evidence, machine recipe/container slot evidence, distribution install/uninstall evidence status, AdapterCore target diff artifacts, and descriptor-only module lifecycle traces.
- Stability soak: PASS, 60-minute equivalent, 720 steps, 60 frames, 12 save/restore cycles.
- Full workspace build smoke: PASS, parent full addon set build.
- Packaged runtime refresh: PASS, `refreshStandalonePackagedRuntimeImage` rebuilds the `echo-runtime-client` jpackage app image and portable zip before packaged EXE smoke.

## Accepted Beta Warnings

- Distribution signing/setup remains pending: `distribution-signing-setup.json` records unsigned runtime, launcher, and setup EXEs plus disabled launcher signing config; `distribution-install-uninstall-evidence.json` records that clean install/uninstall evidence is still pending with a generated VM/profile capture template. This is accepted for beta tester handoff and blocks public release only.
- Human 30-minute packaged EXE wall-clock playtest evidence remains pending. This is accepted for beta tester handoff and blocks public release only.
- Human 60-minute packaged EXE wall-clock playtest evidence remains pending. This is accepted for beta tester handoff and blocks public release only.

## Signoff

Agent 5 release decision: this is beta-ready with evidence. It is not public-release-ready until the accepted warnings above are resolved.
