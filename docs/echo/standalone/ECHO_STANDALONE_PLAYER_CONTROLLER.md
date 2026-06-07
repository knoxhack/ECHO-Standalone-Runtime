# ECHO Standalone Player Controller

Phase 15.4 adds a deterministic player-controller layer on top of the Phase 15.3 input runtime. It is still headless-safe: no OS input loop, native window polling, renderer device, or desktop game campaign is required.

## Runtime Pieces

- `EchoPlayerController` resolves input actions into movement, interactions, quick-slot usage, hazard feedback, and focus delegation.
- `EchoPlayerControllerRuntime` service-binds the controller, camera rig, targeter, shortcuts, and runtime result.
- `EchoPlayerInteractionTargeter` selects nearby Ashfall POIs by exact position, facing cell, distance, then stable id.
- `EchoPlayerCameraRig` follows the active player with a stable debug camera.
- `EchoPlayerInventoryShortcuts` maps quick slot 1 to water ration consumption through gameplay.
- `EchoPlayerHazardFeedback` converts hazard application into player-facing feedback.

## Ashfall Debug Traversal

The deterministic smoke path starts at the emergency terminal, activates it, consumes a water ration, walks east/east/south to the crash cache, salvages the cache, then walks to the blocked southeast cell edge. The final south input collides with the blocked world cell at `3,0,3`, preserving the player at `3,0,2`.

## Smoke Harness Coverage

`EchoRuntimePlayerControllerSmokeHarness` proves:

- controller services are registered.
- initial camera and exact terminal target are stable.
- interact activates the terminal target.
- quick slot 1 consumes a water ration.
- movement updates position, facing, camera, and hazard feedback.
- targeting shifts from terminal to nearby crash cache, then exact crash cache.
- cache salvage completes the Ashfall debug mission.
- blocked-cell collision reports `blocked_cell` and keeps camera/player state stable.
- Terminal focus delegation blocks gameplay movement until Escape blurs focus.

## Boundary

This phase means runtime foundation ready, not full game ready. It does not add a renderer device backend, audio device output, campaign content, save-slot UI, installer flow, or native desktop input polling.
