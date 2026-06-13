# ECHO Standalone Input Runtime

Phase 15.3 adds a device-neutral input runtime in front of UI and gameplay. It does not bind to OS device APIs yet; it creates the abstraction and deterministic routing contract that real keyboard, mouse, and gamepad adapters will feed.

## Runtime Pieces

- `EchoInputControl` describes keyboard, mouse, and gamepad controls with stable ids.
- `EchoInputEvent` describes pressed, released, axis, and text input.
- `EchoInputBindingMap` owns default Ashfall bindings and rebinds controls per context/action/device.
- `EchoInputFocusState` tracks active context and Terminal focus path.
- `EchoInputRouter` resolves raw input through bindings, focus, UI, entity movement, and gameplay interaction systems.
- `EchoInputRuntime` service-binds the binding map, focus state, router, and result.

## Default Bindings

```text
keyboard: W/A/S/D -> movement
keyboard: E -> interact
keyboard: Digit1 -> quick slot 1
keyboard: Backquote -> focus Terminal
keyboard: Escape -> leave Terminal
mouse: Primary -> pointer/interact
gamepad: D-pad -> movement
gamepad: South -> interact
gamepad: North -> focus Terminal
```

## Routing Rules

Input is routed in this order:

1. Resolve the current focus context.
2. Match a binding for the active context.
3. When Terminal is focused, text goes to UI and gameplay movement/interaction is blocked.
4. Gameplay movement routes to `EchoEntityMovementSystem`.
5. Interact and quick-slot actions route to `EchoInteractionSystem`.
6. Terminal focus uses the active UI frame focus path.

## Smoke Harness Coverage

`EchoRuntimeInputSmokeHarness` proves:

- keyboard, mouse, and gamepad default bindings exist.
- keyboard movement can be rebound and the old key stops working.
- quick-slot input consumes a water ration through gameplay.
- keyboard and gamepad movement move the player deterministically.
- mouse primary interaction salvages the crash cache.
- Terminal focus captures text and routes it to the Terminal shell.
- Terminal focus blocks gameplay movement until Escape blurs focus.

It also writes concrete non-placeholder evidence to `runtime-input.json`, `input-devices.json`, `input-bindings.json`, `input-rebinding.json`, `input-focus.json`, and `input-routing.json`. `verifyStandaloneInputRuntime` regenerates that evidence, rejects bootstrap schemas, and requires concrete `PASS` fields for service binding, keyboard/mouse/gamepad/text device coverage, gameplay/UI/terminal bindings, stale-key removal after rebinding, quick-slot routing, UI inventory feedback routing, gamepad movement, mouse interaction, Terminal focus/text handling, focus-blocked movement, and blur back to gameplay.

The player-facing OpenGL client is covered separately by `runStandaloneClientKeyBindingsSmoke`: the ScreenCore Controls route renders selectable keybinding rows, enters a pending "press a key" state, writes the selected key into `EchoClientSettings`, marks settings dirty for persistence, and keeps reset-to-defaults available.

Native window focus is also covered on the OpenGL client path by `runStandaloneClientSessionResilienceSmoke`: GLFW focus loss is consumed before gameplay input runs, active gameplay moves to the ScreenCore Pause menu, cursor lock is released, stale one-shot input is cleared, Resume returns to the same active world, and repeated pause/resume plus save/quit/continue cycles remain stable in `reports/echo/standalone/client-session-resilience.json`.

## Boundary

This phase does not open real devices, poll OS input queues, implement analog stick dead-zone calibration beyond normalized axis events, or claim the Phase 15.4 playable controller.
