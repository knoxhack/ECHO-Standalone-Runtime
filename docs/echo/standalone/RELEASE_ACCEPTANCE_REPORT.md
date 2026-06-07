# ECHO Standalone Release Acceptance Report

Status: beta accepted with warnings.

Decision date: 2026-05-31.

## Acceptance

Agent 5 accepts the current standalone runtime for beta tester handoff. There are 0 active beta blockers. The remaining warnings are accepted for beta and held for public release.

## Evidence

- Full standalone check: PASS, `..\gradlew.bat -p . check --console=plain`.
- Full workspace build smoke: PASS, `buildEchoWorkspace -PechoAddonSet=all`.
- Beta readiness gate: `READY_WITH_WARNINGS`, 91 checks, 0 blockers, 3 accepted warnings.
- AdapterCore parity: PASS, 373 parity rows, save contract backed and versioned.
- AdapterCore module coverage: PASS, 95/95 active modules, 0 gaps.
- Playable route: PASS, 12/12 route, 63/63 mission objectives.
- Save/load: PASS, render checksum `2118313923059fef`, including save-backed dropped item, named-slot autosave/manual backups, corruption restore, incompatible-mod Continue blocking, and coordinate-backed editable machine block entity power state with placement metadata, break/reconnect lifecycle, and chunk unload/reload cache persistence.
- ScreenCore shell: PASS, Mods screen shows native runtime content imports by domain and row through `runStandaloneClientModsRuntimeContentSmoke`; settings screens support pointer-driven sliders through `runStandaloneClientSettingsSmoke`; machine and terminal ScreenCore surfaces expose restored machine state, power graph, terminal command payloads, machine lifecycle evidence, multi-network machine diagnostics, player-inventory-backed per-instance machine input/output controls, per-instance machine recipe selection, container-backed machine slot persistence, and are covered by catalog, runtime-controller, command-controller, world-interaction, and save/continue smokes.
- Block model chunk rendering: PASS, mounted Minecraft blockstate/model JSON drives OpenGL chunk-render model selection, face textures, atlas keys, and JSON element bounds through deterministic client smokes.
- Voxel biome chunk rendering: PASS, generated chunk cells expose biome ids, block states carry biome metadata, OpenGL render materials carry biome id/tint color, and client fog/ambience changes are covered by deterministic client smokes.
- Dropped item loop: PASS, live block drops become item entities, mounted datapack block loot can replace self-drops with concrete item drops, nearby pickup transfers them into inventory, and disk restore preserves outstanding drops.
- Stability soak: PASS, 60-minute equivalent, 720 steps, 12 save/restore cycles.
- Packaged OpenGL EXE evidence: PASS, refreshed `echo-runtime-client` jpackage image, strict two-refresh portable package SHA-256 `c08f6efc4675dc440225781e0e9119e5e14aaca6aa25221c42e31e792797bb27`, 15/15 smoke alive samples, and 20/20 strict rehearsal process samples with a visible `ECHO Ashfall - Standalone Client` window.
- Support bundle: PASS, tester zip archive produced with standalone OpenGL renderer metadata, generated module diagnostics, runtime registry fingerprints, bounded save metadata, incompatible-mod save recovery evidence, ScreenCore route, machine/terminal state-payload evidence, blockstate/model chunk-render evidence, voxel biome id/tint rendering evidence, machine placement/break lifecycle evidence, chunk cache persistence evidence, ScreenCore multi-network machine diagnostics, per-instance machine input/output evidence, machine recipe/container slot evidence, distribution install/uninstall evidence status, AdapterCore target diff artifacts, and descriptor-only module lifecycle traces.

## Beta Warnings

- Distribution signing/setup audit is pending: runtime, launcher, and setup EXEs are currently unsigned, launcher signing config is disabled, and `distribution-install-uninstall-evidence.json` is still `PENDING_MANUAL_OR_VM_RUN`.
- Human 30-minute packaged EXE playtest evidence is pending.
- Human 60-minute packaged EXE playtest evidence is pending.

## Public Release Decision

Public release is not accepted. Enable release signing, sign runtime/launcher/setup EXEs, complete the generated clean install/uninstall evidence template in a disposable Windows profile or VM, and record the 30-minute and 60-minute human wall-clock packaged EXE sessions before changing public release status.
