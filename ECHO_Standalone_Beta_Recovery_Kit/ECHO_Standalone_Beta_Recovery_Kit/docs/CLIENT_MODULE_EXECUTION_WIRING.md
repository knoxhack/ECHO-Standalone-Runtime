# Client Module Execution Wiring

This is the hard blocker that must be implemented after the low-risk patches.

## Required boot sequence

1. Parse launch options before any service is constructed:
   - `--pack-root <path>`
   - `--modules-root <path>`
   - `--profile <id>`
   - `--safe-mode`
2. Resolve the installed pack manifest and exact required module list.
3. Mount every `mods/*-standalone.jar` as:
   - a descriptor/module source,
   - an asset source,
   - a data source,
   - a content-graph source.
4. Create one shared runtime service registry.
5. Register diagnostics, AdapterCore host services, save services, and mutation services.
6. Run `EchoRuntimeModuleManager.executableAbiV1()` before constructing the world template or screen catalog.
7. Reject launch when a required module is:
   - missing,
   - checksum-mismatched,
   - graph-failed,
   - trust-blocked,
   - runtime-disabled,
   - lifecycle-failed.
8. Convert actual module content registrations into `EchoClientRuntimeServices` rows.
9. Build the session factory, item catalog, recipes, loot, entities, screens, hazards, structures, and worldgen from those real rows.
10. Retain the module runtime result and unload it during client shutdown.

## Ownership change

The hard-coded `EchoAdapterCoreStandaloneContentBridge.ashfallLive(...)` may remain as a compatibility fallback, but it must not silently substitute for installed module activation in strict pack mode.

Recommended modes:

- `strict-pack`: installed pack and module graph are authoritative; missing activation is fatal.
- `safe-mode`: only trusted core modules activate; UI explains disabled content.
- `development`: source roots are permitted, but the same executable boot path is used.
- `legacy-compat`: hard-coded fallback allowed, explicitly labeled and never release-ready.

## Minimum implementation shape

Add an `EchoClientModuleBootstrap` that returns an immutable result containing:

- service registry,
- module runtime result,
- resolved pack identity,
- module fingerprints,
- AdapterCore runtime content rows,
- diagnostics,
- close/unload hook.

Inject that result into `EchoClientRuntimeAssembly.create(...)` and `EchoClientRuntimeServices`.

Do not let `EchoClientModScanService` become a second authority. It may present the bootstrap result, but it should not independently rescan and classify the running graph.

## Acceptance tests

- A test addon registers one block, one item, one recipe, one loot table, one entity, and one ScreenCore route from a real JAR.
- Removing the JAR blocks strict launch with a human-readable dependency error.
- Corrupting the JAR blocks launch before entrypoint execution.
- Disabling the addon removes all six runtime surfaces and preserves unrelated save data.
- Reloading data changes recipes/assets without duplicating registrations.
- Unload revokes services, content registrations, callbacks, and transient state.
- Save/reload preserves module-owned data.
- A saved world with a missing required module opens recovery UI instead of silently deleting content.
