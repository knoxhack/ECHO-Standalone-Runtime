# Standalone Addon Compatibility

## Supported APIs
- dev.echo.api.addon
- dev.echo.api.lifecycle
- dev.echo.api.event
- dev.echo.api.registry (read-only)
- dev.echo.api.context

## Unsupported APIs
- NeoForge registries (direct)
- Minecraft Block/Item constructors
- Server networking
- Dimension/worldgen logic

## Bridge APIs
Use `dev.echo.standalone.bridge.*` for shared Native/Standalone code.
