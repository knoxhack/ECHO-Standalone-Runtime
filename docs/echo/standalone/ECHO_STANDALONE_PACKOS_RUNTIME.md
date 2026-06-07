# ECHO Standalone PackOS Runtime

Phase 14.4 moves PackOS into the standalone runtime as a planning and validation layer. It loads pack profiles, reads lockfiles, creates pack sessions, checks module and feature compatibility, prepares mount plans, and emits repair advice without executing repairs.

The Native Loader Phase 14 preflight still reports missing tester/no-crash evidence, so this phase remains headless, deterministic, and no-launch.

## Runtime Pieces

- `EchoRuntimePackOs` orchestrates profile loading, lockfile reading, integrity checks, compatibility checks, mount planning, repair advice, and service binding.
- `EchoRuntimePackProfileLoader` parses `echo.runtime.pack.v1` profiles.
- `EchoRuntimePackLockfileReader` parses `echo.runtime.pack_lock.v1` lockfiles.
- `EchoRuntimePackIntegrityChecker` compares profile and lockfile state.
- `EchoRuntimePackCompatibilityChecker` verifies enabled modules and features against the module and feature graphs.
- `EchoRuntimePackMountPlan` creates deterministic asset, data, and theme mount ordering.
- `EchoRuntimePackRepairAdvisor` produces planning-only repair advice.
- `EchoRuntimePackSession` exposes the selected pack, variant, channel, lockfile, mount plan, integrity report, compatibility report, and repair plan.

## Pack Session Fields

A runtime pack session contains:

- `packId`
- `packName`
- `variant`
- `channel`
- `runtimeVersion`
- `enabledModules`
- `enabledFeatures`
- `lockfile`
- `saveCompatibility`
- `assetPacks`
- `dataPacks`
- `theme`
- `launchMode`
- `mountPlan`
- `integrityReport`
- `compatibilityReport`
- `repairPlan`

## Ashfall Fixture

The smoke harness creates an Ashfall-style fixture:

```text
packId: ashfall
variant: dev_sandbox
channel: alpha
enabledModules: echo-core, echoashfallprotocol
enabledFeatures: ashfall:chapter, echo:services
theme: ashfall_cyberglass
launchMode: headless-test
```

It validates that the pack session is launch-allowed only when:

- the profile and lockfile pack ids match
- enabled modules are discovered
- enabled modules have not failed module validation
- enabled features have providers
- save migration policy remains `plan_only`

The same harness also creates an incompatible profile with a missing module, missing feature, and unsafe migration policy. That session is refused and produces repair advice, but no repair action is executed.

## Mount Order

Phase 14.4 creates a deterministic mount plan in this order:

```text
runtime defaults
pack asset packs
pack data packs
pack theme
```

Later asset and data runtimes will expand this with platform assets, module assets, variants, channel overrides, user overrides, and dev overrides.

## Out Of Scope

Phase 14.4 does not:

- execute repair actions
- mutate lockfiles
- download assets
- mount assets into a renderer
- load saves
- migrate saves
- launch Minecraft or NeoForge
- execute module code
- create a classloader

The next phase is Phase 14.5, the ECHO Resource + Asset Runtime.
