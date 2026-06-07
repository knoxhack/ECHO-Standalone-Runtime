# ECHO Standalone Windowed Runtime

Phase 15.2 moves the standalone runtime past headless-only smoke execution by adding a real window lifecycle contract. The default backend is still deterministic and display-safe, but it now models the operations a desktop backend must support before the renderer is replaced: create, resize, fullscreen, restore windowed mode, request close, close, and crash-safe shutdown.

## Runtime Pieces

- `EchoRenderWindowMode` now includes `FULLSCREEN`.
- `EchoRenderWindowState` tracks `open` and `closeRequested`.
- `EchoRenderBackend` exposes window lifecycle operations for resize, mode switch, close request, final close, crash-safe close, current state, and lifecycle events.
- `EchoRenderWindowEvent` and `EchoRenderWindowEventType` record deterministic window lifecycle history.
- `EchoRenderWindowLifecycleController` coordinates lifecycle operations through the backend.
- `EchoWindowedRuntimeApplication` and `EchoWindowedRuntime` boot the app runtime in `windowed-dev` mode and drive the window lifecycle through the app crash boundary.
- `EchoRuntimeBootContext.windowed(...)` and `EchoRuntimeBootContext.windowedCrashSmoke(...)` provide normal and fault-injected boot paths.
- `EchoRuntimeMain --windowed [workspaceRoot]` remains the Phase 15.2 deterministic window-lifecycle smoke path. The current player-facing OpenGL client launches with `..\gradlew.bat -p . :echo-runtime-client:run --console=plain --no-problems-report`.

## Window Flow

The Phase 15.2 smoke path proves this sequence:

```text
create windowed 1280x720
resize to 1600x900
enter fullscreen
restore windowed 1280x720
request close
close
```

The app lifecycle trace includes:

```text
CREATED
BOOTSTRAPPING
LOADING_CONFIG
INITIALIZING_SERVICES
STARTING_RENDERER
RUNNING
STOPPING
STOPPED
```

## Crash-Safe Shutdown

The fault-injected smoke path opens a window, resizes it, throws a simulated lifecycle failure, closes the window through `closeAfterCrash`, emits `ECHO-STANDALONE-WINDOW-CRASH-SAFE-SHUTDOWN`, and then lets the app crash boundary return `CRASHED` instead of escaping the runtime.

## Boundary

This phase establishes the window lifecycle and app boot path. It does not create a GPU context, bind LWJGL, render textured assets to the screen, or claim the Phase 15.5 renderer upgrade.
