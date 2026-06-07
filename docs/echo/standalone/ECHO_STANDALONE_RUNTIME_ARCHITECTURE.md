# ECHO Standalone Runtime Architecture

Phase 14.1 locks the first standalone runtime shape without starting a full game engine. The standalone runtime lives in its own Gradle workspace under `echo-standalone-runtime` so it can compile, report, and evolve without depending on the Minecraft or NeoForge mod workspace.

The rule for every runtime layer is:

```text
ECHO contract first
Platform implementation second
Minecraft/NeoForge adapter third
Standalone runtime implementation fourth
```

This means Ashfall content should move toward ECHO contracts before it moves toward any particular backend. The contracts module is the clean boundary; compatibility adapters are separate and may bridge to Minecraft, NeoForge, or the Native Loader later.

## Workspace Modules

The architecture lock declares these modules:

```text
echo-runtime-contracts
echo-runtime-core
echo-runtime-app
echo-runtime-modules
echo-runtime-packos
echo-runtime-assets
echo-runtime-data
echo-runtime-ui
echo-runtime-save
echo-runtime-world
echo-runtime-entity
echo-runtime-item
echo-runtime-gameplay
echo-runtime-render
echo-runtime-audio
echo-runtime-network
echo-runtime-scripting
echo-runtime-compat
echo-runtime-testkit
echo-runtime-devtools
```

Only `echo-runtime-contracts`, `echo-runtime-core`, and `echo-runtime-compat` contain concrete Phase 14.1 boundary code. The other modules are intentionally thin package boundaries until their subphase begins.

## Core Contracts

The initial contract surface is:

- `EchoRuntime`
- `EchoRuntimeContext`
- `EchoRuntimeApplication`
- `EchoRuntimeEnvironment`
- `EchoRuntimeMode`
- `EchoRuntimeLifecycle`
- `EchoRuntimeClock`
- `EchoRuntimeTickLoop`
- `EchoRuntimeServiceRegistry`
- `EchoRuntimeCommandRegistry`
- `EchoRuntimeCommand`
- `EchoRuntimeCommandContext`
- `EchoRuntimeCommandResult`
- `EchoRuntimeDiagnosticSink`
- `EchoRuntimeCrashBoundary`
- `EchoRuntimeShutdownHook`
- `EchoRuntimeConfiguration`
- `EchoRuntimeCapabilities`
- `EchoRuntimePlatform`

Supporting diagnostic records and enums live in the same contract package. They use Java standard library types only. The concrete `EchoRuntimeDiagnosticCollector` lives in `echo-runtime-core` and is service-bound through the contract sink.

## Runtime Modes

The locked runtime modes are:

```text
headless-test
windowed-dev
playable-beta
packaged-tester
```

Mode ids are explicit lowercase strings so reports, configs, lockfiles, and future launchers do not depend on enum naming details.

`packaged-tester` is the legacy app-runtime smoke mode. Current player-facing packaged evidence uses the OpenGL `echo-runtime-client` jpackage image and `dev.echo.standalone.runtime.client.EchoClientMain`, not the app-runtime `EchoRuntimeMain` path.

## Runtime Lifecycle

The locked lifecycle states are:

```text
CREATED
BOOTSTRAPPING
LOADING_PACKOS
LOADING_MODULES
RESOLVING_DEPENDENCIES
LOADING_ASSETS
LOADING_CONFIG
LOADING_SAVE
INITIALIZING_SERVICES
STARTING_RENDERER
STARTING_AUDIO
STARTING_NETWORK
STARTING_GAME_LOOP
RUNNING
PAUSED
STOPPING
STOPPED
FAILED
CRASHED
RECOVERING
```

The lifecycle deliberately includes renderer, audio, network, and game loop states before those systems exist so later subphases can attach to stable names instead of inventing new boot semantics.

## Architecture Decisions

- The standalone runtime is a separate Java workspace, not another NeoForge addon.
- Java 25 is used to align with the current ECHO mod stack toolchain.
- The contracts module has no dependency on Minecraft, NeoForge, LWJGL, audio libraries, scripting engines, or launcher code.
- The compatibility module is present now to make adapter separation visible, but it does not implement a Minecraft or NeoForge bridge in Phase 14.1.
- Reports are checked in as deterministic architecture artifacts with `generatedAt` set to `1970-01-01T00:00:00Z`.

## Phase 14.1 Scope

This phase defines the runtime shape. It does not:

- launch a game process
- create a renderer window
- run a Minecraft or NeoForge adapter
- load Ashfall gameplay content
- execute scripting
- mutate saves
- run repair actions
- import Minecraft or NeoForge classes into standalone contracts

The next implementation phase is Phase 14.2, the ECHO App Runtime.
