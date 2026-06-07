# ECHO Standalone Command Runtime

The standalone command runtime promotes command dispatch out of Terminal-only UI code and into the platform-neutral runtime contracts package.

The contract surface is:

- `EchoRuntimeCommandRegistry`
- `EchoRuntimeCommand`
- `EchoRuntimeCommandContext`
- `EchoRuntimeCommandResult`
- `EchoRuntimeCommandHandler`

`EchoRuntimeCommandRegistry` supports deterministic registration, lookup, execution, default commands, handled/unhandled results, output lines, clear requests, and close requests. It uses only Java standard library types and has no dependency on Minecraft, NeoForge, Brigadier, UI screens, scripting engines, or launcher code.

## Terminal Bridge

`EchoTerminalCommandRegistry` now owns a runtime command registry bridge. Terminal-specific commands remain available for theme-aware output, while shared runtime commands can be registered once and executed from the Terminal shell. This makes command behavior reusable by gameplay, diagnostics, scripting, native modules, and UI surfaces instead of being locked inside Terminal screen code.

## Smoke Coverage

`EchoRuntimeCommandSmokeHarness` proves:

- default runtime commands execute deterministically
- custom Ashfall command registration executes through the runtime registry
- unknown commands return unhandled diagnostic output
- Terminal shell execution can route to shared runtime commands
- Terminal command registry exposes runtime command definitions

The current smoke result is:

```text
runtime command smoke PASS commands=6 terminalBridge=true handled=true unknownHandled=false
```
