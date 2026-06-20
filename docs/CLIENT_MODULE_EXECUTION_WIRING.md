# Client Module Execution Wiring

The LWJGL client now has an explicit installed-pack boot path. Strict pack launches parse
`--pack-root` and `--modules-root`, infer `.echo/pack-manifest.json`, validate required
`mods/*-standalone.jar` files, execute the ABI-v1 module graph, and import native registry
registrations into the client runtime content registry.

## Strict-Pack Boot Sequence

1. `EchoClientLaunchContext` parses launcher arguments and publishes `echo.pack.root`,
   `echo.modules.root`, and `echo.safe.mode` before runtime services are constructed.
2. `EchoClientModuleBootstrap` resolves the installed manifest from the explicit
   `--packManifest` argument or from `<pack-root>/.echo/pack-manifest.json`.
3. Required standalone module artifacts are checked for presence and SHA-256 before
   module entrypoints execute.
4. `EchoRuntimeModuleManager.executableAbiV1()` runs against the installed module jars.
5. Required modules must be discovered, runtime-active, and not failed or disabled.
6. Native registry-registration services are converted to AdapterCore runtime content rows.
7. `EchoClientRuntimeServices` imports those rows before screen catalogs, session factories,
   item definitions, recipes, loot, entities, hazards, structures, and worldgen are refreshed.
8. The retained bootstrap result owns Mods screen summaries and unloads the executable graph
   during client shutdown.

## Modes

- `strict-pack`: default when `--pack-root`, `--modules-root`, `--installPath`, or
  `--packManifest` is present. Missing, corrupt, disabled, or graph-failed required modules
  block launch.
- `safe-mode`: explicit `--safe-mode`; validation failures are reported in the bootstrap
  summary and only available installed modules are exposed.
- `development`: no installed-pack argument. Existing source-root scans remain available for
  local diagnostics, but they are not release evidence.

## Acceptance Checks

- `node scripts/verify-runtime-wiring.mjs <runtime-root> <ashfall-manifest>` must pass before
  the Ashfall standalone manifest is published.
- `node scripts/verify-standalone-evidence.mjs <runtime-root>` must pass for local evidence
  hygiene; `--release` remains intentionally red until real machine-run evidence replaces
  placeholders.
- Release readiness still requires a compiled Windows distribution and manual play evidence
  from the exact release bytes.
