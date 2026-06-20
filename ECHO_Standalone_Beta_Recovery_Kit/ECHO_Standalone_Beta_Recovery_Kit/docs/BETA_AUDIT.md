# ECHO Standalone Runtime Beta Audit

## Status update

The findings below describe the pre-fix audit snapshot. The current runtime tree has since
implemented strict installed-pack discovery, executable ABI-v1 client module bootstrap,
fail-closed evidence placeholders, and VSync pacing protection. The beta readiness gate can
now report `READY_WITH_WARNINGS` with zero blockers when deterministic runtime checks and
packaged OpenGL probes pass. The remaining warning is real human playtest evidence from the
exact release bytes; public-release evidence such as signing, clean install/uninstall, hardware
audio, and 30/60 minute wall-clock sessions remains external/manual.

## Verdict

The current codebase is a substantial engine prototype with many real systems, but it is not yet a defensible beta release for module-driven Ashfall. The largest blocker is not a missing menu or one broken recipe: the player-facing client and the executable module runtime are separate boot paths.

## Release-blocking findings

### P0 — The Ashfall Standalone manifest launches NeoForge

`release-manifest.template.json` declares `loader: echo-standalone-runtime`, but its launch class is `net.neoforged.fml.startup.Client` with FML arguments. A Standalone install can therefore be routed into the wrong runtime contract.

Required result:

```json
{
  "mainClass": "dev.echo.standalone.runtime.client.EchoClientMain",
  "gameArgs": ["--pack-root", "${game_directory}", "--modules-root", "${game_directory}/mods"]
}
```

The release ZIP, hashes, pack audit, and Release Index must then be regenerated from the corrected manifest.

### P0 — The game client does not execute the installed module graph

The current LWJGL entry point starts `EchoClientEngine` directly. `EchoClientRuntimeAssembly` constructs hard-coded template services. `EchoClientModScanService` calls `EchoRuntimeModuleManager.descriptorOnly()`. By contrast, the executable `EchoRuntimeModuleManager.executableAbiV1()` path is used in the runtime-app/system-module boot and test harnesses.

Consequences:

- Installed module descriptors can appear in UI without their entrypoints powering gameplay.
- Synthetic AdapterCore rows can make client UI smoke pass without proving real installed JAR activation.
- Ashfall gameplay can appear rich because the client contains a large hard-coded Ashfall compatibility bridge, while third-party addons remain outside the real game boot path.
- A pack can be complete on disk but not be the authority for the running session.

This must be fixed before calling ECHO Standalone a module emulator or addon-capable beta.

### P0 — Installed-root discovery is source-tree biased

Runtime root discovery currently recognizes a source checkout by `settings.gradle` plus `echo-runtime-client`. Launcher-installed pack roots do not necessarily have that shape. Resource discovery favors source resources, `resourcepacks`, and `packs`; installed `mods/*.jar` resources need an explicit archive-mount route.

### P0 — Readiness evidence can fail open

The evidence bootstrap creates missing reports, and unspecified reports previously defaulted to `status: PASS`. The Java alpha gate checks file existence. The beta task also classifies a Gradle task as PASS whenever it has no failure, even when Gradle skipped it.

A release gate must reject:

- bootstrap schemas,
- missing reports,
- invalid JSON,
- skipped tasks,
- stale reports not produced by the current run,
- simulated-duration reports presented as wall-clock proof,
- manual/hardware/signing evidence that is not PASS.

### P1 — Duplicate frame limiting can cause visible stutter

The GLFW window defaults to VSync (`swap interval 1`). After the blocking buffer swap, the engine independently sleeps toward 60 Hz. The two limiters can fight one another, especially after a late frame, and can produce uneven pacing or effective half-rate behavior.

Manual sleeping should only run when VSync is disabled.

### P1 — Chunk generation and mesh preparation run synchronously

Player movement can synchronously generate chunks. Renderer cache misses synchronously build CPU meshes. GPU upload also converts mesh data on the render thread. The existing upload-count budget does not bound CPU generation or conversion time.

Beta architecture should use:

1. immutable chunk snapshots,
2. nearest-first generation queue,
3. worker CPU-mesh queue,
4. prepared upload buffers,
5. render-thread-only OpenGL upload,
6. per-frame time budgets and cancellation/version checks.

### P1 — “804 features” is an inventory, not 804 proven gameplay loops

The parity tooling has useful catalog and contract coverage, but its feature count is largely IDs/resources/classification plus selected contract probes. Promotion wording must distinguish inventory parity from visible, interactive, save-stable behavior.

### P1 — The soak evidence is simulated time

A deterministic simulation that advances 60 minutes of game time is valuable, but it is not equivalent to a real 60-minute wall-clock session with rendering, input, audio, saving, focus changes, and GPU/driver behavior.

## Beta acceptance criteria

The beta gate should remain red until all of the following are true:

- Correct Standalone launch class and arguments.
- Exact pack root and module root passed by Launcher.
- Required module JARs loaded through executable ABI v1.
- Required module graph has zero errors and zero disabled required modules.
- Module content reaches blocks/items/recipes/loot/entities/screens/hazards/worldgen.
- Runtime unload/reload behavior is deterministic.
- Save fingerprint records exact module IDs, versions, and SHA-256 values.
- Missing/changed required modules fail safely with recovery UI.
- VSync and uncapped/manual pacing both behave correctly.
- Chunk generation/meshing no longer stalls the render loop.
- Clean packaged install/uninstall passes.
- Signed release bytes pass checksum and provenance checks.
- Real visible 30-minute and 60-minute sessions pass.
- Hardware audio passes.
- Ashfall progression, machines, hazards, UI, entities, and save/reload pass from release bytes.
