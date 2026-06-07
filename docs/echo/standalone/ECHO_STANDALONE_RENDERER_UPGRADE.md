# ECHO Standalone Renderer Upgrade

Phase 15.5 replaces the default recording renderer with a real software backend adapter. The upgrade keeps `EchoRenderBackend` stable, so later native/GPU work can swap in behind the same scene, window, and frame contract.

## Implemented

- `EchoSoftwareRenderBackend` opens logical headless/windowed/fullscreen window states and supports resize, mode switching, close, and crash-safe close.
- `EchoSoftwareFramebuffer` stores deterministic ARGB pixels.
- `EchoSoftwareRenderStats` records pass order, command counts, lit pixels, non-background pixels, and checksum.
- `EchoRenderRuntime.createDebugRenderer()` now uses the software backend by default.
- `EchoRenderRuntime.createRecordingDebugRenderer()` preserves the recorder for command audits.
- `EchoWindowedRuntimeApplication` now uses the software backend for lifecycle smoke.

## Pipeline

```text
CLEAR -> TILES -> SPRITES -> UI -> LIGHTING -> PARTICLES -> DEBUG_OVERLAY
```

Tiles and entity sprites are projected through the scene camera. UI commands are rasterized in screen space. Lighting is applied after tiles/sprites/UI. Particles and diagnostic overlays are drawn last so they remain readable.

## Boundary

This is a real backend adapter because it produces pixels and lifecycle state, but it is intentionally not a native display backend yet. That keeps Phase 15.5 deterministic and safe inside headless CI while moving the runtime away from command recording as the default renderer.
