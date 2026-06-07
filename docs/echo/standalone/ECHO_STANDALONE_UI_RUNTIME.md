# ECHO Standalone UI Runtime

Phase 14.6 adds the first platform-independent UI runtime. It owns screen stack state, modal stack state, input routing, active theme state, and an early Terminal shell screen without depending on Minecraft, NeoForge, a renderer, or a window toolkit.

The runtime is intentionally model-first. Later renderer phases can draw `EchoUiFrame` surfaces, but this phase only produces deterministic UI state and command output.

## Runtime Pieces

- `EchoUiRuntime` boots the UI model and binds services.
- `EchoUiRuntimeResult` exposes dispatch and frame rendering over the active stacks.
- `EchoUiScreenStack` tracks navigable screens.
- `EchoUiModalStack` tracks blocking modal surfaces.
- `EchoUiInputRouter` routes input to the top modal first, then the active screen.
- `EchoUiThemeRuntime` owns the active theme and loads theme JSON through the asset resolver.
- `EchoUiFrame` and `EchoUiSurface` are renderer-neutral frame descriptions.
- `EchoMenuDefinition`, `EchoMenuOption`, `EchoMenuRegistry`, and `EchoMenuScreen` provide reusable menu definitions, lookup, rendering, enabled/disabled option state, and action routing.
- `EchoTerminalScreen` and `EchoTerminalShell` provide the first usable Terminal prototype.
- `EchoTerminalCommandRegistry` provides default `help`, `echo`, `clear`, `status`, and `theme` commands.

## Theme Files

UI themes are loaded from the resource runtime using the same logical id convention established in Phase 14.5:

```text
data/<namespace>/themes/<theme>.json -> <namespace>:themes/<theme>.json
```

The current theme schema is a small flat object:

```json
{
  "id": "ashfall-terminal",
  "displayName": "Ashfall Terminal",
  "accentColor": "#67e8f9",
  "backgroundColor": "#061014",
  "foregroundColor": "#d8fbff",
  "warningColor": "#facc15",
  "fontFamily": "ECHO Mono",
  "density": "compact",
  "tokens": {
    "terminal.prompt": "ASH>"
  }
}
```

Phase 14.6 parses this into `EchoUiTheme`. Schema validation and a richer design-token registry remain for later data and UI authoring phases.

## Input Routing

Input is routed in this order:

1. If a modal is open, the top modal receives input.
2. A blocking modal consumes input until it receives the `dismiss` command.
3. If no modal is open, the active screen receives input.
4. Terminal command input is submitted to the shell and recorded in deterministic output history.

This gives the standalone runtime a stable UI behavior model before any platform backend exists.

## Smoke Harness Coverage

The Phase 14.6 smoke harness proves:

- a theme is loaded from mounted pack data through the asset runtime.
- the UI runtime binds screen stack, modal stack, router, theme runtime, and result services.
- Terminal command input is routed and produces deterministic output.
- menu definitions are registered, rendered, selected by action id, and disabled options are blocked.
- pushed screens become active and pop back to Terminal.
- blocking modals consume input before the screen.
- `dismiss` closes the top modal.
- generated frames expose active screen, modal, and theme state.

## Out Of Scope

Phase 14.6 does not:

- open a native window
- draw pixels
- depend on a renderer backend
- process real keyboard scancodes
- decode fonts
- implement layout constraints
- run Minecraft or NeoForge UI APIs
- launch or save a game session

The next phase is Phase 14.7, the ECHO Save Runtime.
