# ECHO Standalone Renderer Runtime

Phase 14.13 introduced the renderer contract and deterministic command recording. Phase 15.5 upgrades the default runtime backend to a headless-safe software renderer that produces a real ARGB framebuffer while keeping the old recording backend available for command audits.

## Runtime Pieces

- `EchoRenderRuntime` creates and service-binds the debug renderer.
- `EchoRenderBackend` remains the backend contract used by recording, software, and future native/GPU adapters.
- `EchoSoftwareRenderBackend` is the default backend and rasterizes scene commands into `EchoSoftwareFramebuffer`.
- `EchoRecordingRenderBackend` remains available through `createRecordingDebugRenderer()` for deterministic command-only validation.
- `EchoSoftwareRenderStats` reports pass order, command counts, lighting coverage, non-background pixels, and framebuffer checksum.
- `EchoRenderWindowSettings` and `EchoRenderWindowState` describe window intent and runtime state.
- `EchoRenderSceneBuilder` builds the debug Ashfall scene from world, entity, gameplay, and UI state.
- `EchoRenderUiBridge` maps `EchoUiFrame` surfaces to UI render commands.

## Software Pipeline

The Phase 15.5 software backend runs this deterministic pass order:

```text
clear
tiles
sprites
UI
lighting
particles
debug overlays
```

The default debug scene remains:

```text
scene: ashfall-debug-scene
backend: echo:software_renderer
window: headless 1280x720
camera: ashfall-debug-camera
commands: 29
framebuffer: 1280x720 ARGB
```

The command breakdown is:

```text
background: 1
world tiles: 16
entities: 2
particles: 5
UI bridge: 4
diagnostic: 1
```

## Backend Safety

The software backend is still display-free. It opens a logical window, rasterizes renderer-neutral scene commands into memory, records frame metadata, and reports deterministic diagnostics. It does not import or start LWJGL, AWT, JavaFX, Minecraft, NeoForge, or a GPU context.

## Smoke Harness Coverage

`EchoRuntimeRenderSmokeHarness` proves:

- renderer runtime result, backend, window, scene, and frame are service-bound.
- the default backend is `echo:software_renderer`.
- the debug window is headless and uses a 1280x720 viewport.
- scene layer counts match expected world, entity, particle, UI, and diagnostic output.
- the software framebuffer is generated at viewport size.
- pass order is clear, tiles, sprites, UI, lighting, particles, debug overlays.
- lighting touches the full framebuffer.
- sampled player, blocked-tile, and UI pixels differ from the background sample.
- framebuffer checksum is deterministic and non-zero.
- the recording backend compatibility path still records one frame.

The harness also writes concrete non-placeholder evidence to the Phase 15.5 render reports under `reports/echo/standalone`. `verifyStandaloneRenderRuntime` regenerates those reports, rejects bootstrap schemas, and requires PASS evidence for service binding, the software backend, logical headless window state, scene command composition, camera samples, layer counts, world/entity/particle/UI rendering, framebuffer diagnostics, lighting coverage, checksum output, and display-free renderer boundaries.

## Out Of Scope

Phase 15.5 does not create an OS window or GPU context. Native device-backed rendering, texture atlases, shader pipelines, font shaping, animation blending, and presentation timing remain future renderer work.
