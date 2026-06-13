# ECHO Standalone App Runtime

Phase 14.2 adds the first executable standalone runtime shell. It is intentionally headless: it can boot, transition lifecycle states, run a fixed tick loop, catch a simulated crash, and shut down cleanly without starting a renderer, loading Minecraft, launching NeoForge, or touching saves.

This shell is not the player-facing runtime authority. Player launch, packaged runtime image, and launcher handoff must target the LWJGL/OpenGL client entrypoint `dev.echo.standalone.runtime.client.EchoClientMain`; `EchoRuntimeMain` remains a headless evidence and lifecycle smoke entrypoint. `reports/echo/standalone/player-runtime-authority.json` is the machine-readable guard for that split.

## Implemented Runtime Pieces

- `EchoRuntimeLauncher` creates the app runtime context and runs the boot sequence.
- `EchoRuntimeMain` provides a minimal command-line entry point.
- `EchoRuntimeBootContext` captures runtime id, mode, workspace paths, deterministic clock settings, max tick count, capabilities, and the selected app.
- `EchoRuntimeBootResult` captures exit code, final lifecycle, lifecycle trace, diagnostics, ticks run, crash handling status, and shutdown hook.
- `EchoRuntimeLifecycleManager` records deterministic lifecycle transitions.
- `EchoHeadlessRuntimeTickLoop` implements `EchoRuntimeTickLoop` for fixed-step headless execution.
- `EchoFixedStepRuntimeClock` provides deterministic tick timestamps.
- `EchoRuntimeMainThread` captures and validates main-thread ownership.
- `EchoRuntimeWorkerPool` provides daemon worker threads for later phases.
- `EchoRuntimeShutdownController` records requested shutdown hooks.
- `EchoRuntimeCrashHandler` catches guarded fatal failures and emits diagnostics.
- `EchoRuntimeDiagnosticCollector` is the shared service-bound diagnostics collector for runtime layers and support/report consumers.
- `EchoRuntimeLogBridge` preserves the app-runtime bridge name while delegating to the shared diagnostics collector.
- `EchoRuntimeExitCode` defines app shell exit outcomes.

## Tick Layers

The app runtime locks these tick layers through `EchoRuntimeTickLayer`:

```text
pre_tick
input
network
world
entity
player
gameplay
ui
audio
render
save
post_tick
```

The current headless loop calls one tick handler per fixed tick and exposes the full layer list to the tick context. Later phases can attach concrete systems to those layer names without changing the boot contract.

## Headless Boot Flow

The deterministic smoke path uses this lifecycle trace:

```text
CREATED
BOOTSTRAPPING
LOADING_CONFIG
INITIALIZING_SERVICES
STARTING_GAME_LOOP
RUNNING
STOPPING
STOPPED
```

The default boot context uses:

```text
mode: headless-test
bootInstant: 1970-01-01T00:00:00Z
tickBudget: 50ms
maxTicks: 3
```

## Crash Boundary Flow

The smoke harness also launches a deliberately crashing app. The crash is caught by `EchoRuntimeCrashHandler`, a fatal diagnostic is emitted into the shared collector, and the boot result exits with `CRASHED` instead of allowing the failure to escape the runtime boundary.

## Out Of Scope

Phase 14.2 still does not:

- open a render window
- run world simulation
- load modules
- load PackOS profiles
- mount assets
- load saves
- execute adapters
- start networking
- start audio
- run Minecraft or NeoForge
- migrate saves or execute repairs

The next phase is Phase 14.3, the ECHO Module Runtime.
