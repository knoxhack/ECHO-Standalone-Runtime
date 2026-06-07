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

## Boundary

This phase does not open real devices, poll OS input queues, implement analog stick dead-zone calibration beyond normalized axis events, or claim the Phase 15.4 playable controller.
