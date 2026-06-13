# ECHO Standalone Compatibility + Migration Layer

Phase 14.17 adds the standalone compatibility and migration layer. It maps ECHO-owned platform content into standalone definitions, validates targets against the current standalone runtime, and produces manual migration plans.

This phase deliberately does not mutate real saves or execute migration repairs. It creates deterministic dry-run plans so migration can be reviewed, backed up, and performed by explicit tooling later.

## Runtime Pieces

- `EchoCompatRuntime` creates and service-binds the debug compatibility runtime.
- `EchoRuntimeCompatibilityAdapterBoundary` keeps adapter separation rules visible.
- `EchoCompatMappingRegistry` stores source-to-standalone mappings.
- `EchoCompatTargetValidator` validates mapping targets against standalone item, world, entity, and gameplay runtime state.
- `EchoCompatMigrationPolicy` locks this phase to manual plan-only behavior.
- `EchoCompatMigrationPlanner` creates backup-required dry-run migration steps.
- `EchoCompatMigrationPlan` records manual migration steps and blocked state.
- `EchoCompatDiagnostics` records initialization, registry, manual review, and planning diagnostics.
- `EchoNeoForgeMetadataScanner` discovers source/template `META-INF/neoforge.mods.toml` files as compatibility candidates.
- `EchoNeoForgeMetadataParser` extracts mod ids, display names, versions, dependency rows, dependency reasons, ordering, and side metadata without importing platform APIs.

## NeoForge Metadata Candidates

NeoForge metadata is compatibility input only. It is not added to `EchoRuntimeModuleDescriptorScanner`, and `neoforge.mods.toml` files are not activated as standalone modules.

The current catalog evidence is `reports/echo/standalone/neoforge-compat-candidates.json`:

```text
status: PASS
candidates: 132
warnings: 0
errors: 0
runtimeStatus: runtime-disabled-with-reason
```

The metadata report preserves required and optional dependency rows, including `reason`, `ordering`, `side`, and version range fields. Its safety block records that no module activation, classloader creation, module code execution, or native-loader handoff occurred.

## Debug Mapping Set

The Phase 14.17 debug mapping set contains seven ECHO-owned source records:

```text
echoashfallprotocol:item/clean_water_bottle -> echoashfallprotocol:clean_water_bottle
echoashfallprotocol:item/scrap_metal -> echoashfallprotocol:scrap_metal
echoashfallprotocol:world_region/crash_site -> ashfall:crash_site
echoashfallprotocol:world_hazard/toxic_ash -> ashfall:toxic_ash
echoashfallprotocol:entity/hostile_scavenger -> ashfall:hostile_scavenger
echoashfallprotocol:mission/secure_crash_site -> ashfall:secure_crash_site
echoashfallprotocol:save/player_progress_v1 -> echo:manual_save_review/player_progress
```

Six mappings are directly supported. The player progress save payload requires manual review.

## Target Validation

The target validator checks supported mappings against runtime state:

- item targets must exist in the standalone item registry.
- region targets must exist in the standalone world regions.
- hazard targets must exist in standalone world hazards.
- entity targets must exist as standalone entity definitions.
- mission targets must match the standalone mission state.
- save record targets are manual-review only in this phase.

The debug validation passes with one warning for the manual save review record and no errors.

## Manual Migration Policy

The Phase 14.17 policy is:

```text
policy: echo:manual_migration_plan_only
manualOnly: true
executeAutomatically: false
mutateSourceAllowed: false
backupRequired: true
```

The generated plan contains eight steps: one backup step plus seven mapping/review steps. None of the steps mutates source data.

## Runtime Safety

The compatibility layer may name source platforms as data, but it does not import platform APIs or mutate save data. It does not depend on Minecraft classes, NeoForge classes, launcher state, socket transport, native renderer APIs, native audio APIs, or script engines.

## Smoke Harness Coverage

The Phase 14.17 smoke harness proves:

- compatibility runtime result, adapter boundary, mapping registry, migration policy, target validator, validation result, migration planner, migration plan, and diagnostics are service-bound.
- adapter boundary rules still require planning before mutation.
- seven source mappings are registered.
- six mappings are directly supported.
- one mapping requires manual review.
- target validation passes with one warning and no errors.
- migration policy is manual-only, does not execute automatically, does not mutate sources, and requires backup.
- migration plan is not blocked.
- migration plan contains backup plus seven mapping/review steps.
- no migration step mutates source data.
- diagnostics are deterministic and contain no errors.
- NeoForge metadata fixture parsing preserves platform dependencies, required ECHO dependency reasons/orderings, and optional dependency reasons.
- module runtime smoke proves a NeoForge-only `META-INF/neoforge.mods.toml` fixture remains outside standalone module activation.
- AdapterCore module coverage smoke writes the source/template NeoForge metadata candidate report, `runtime-adaptercore-module-blockers.json`, and `runtime-adaptercore-permission-catalog.json`; current evidence verifies Ashfall, Core, resource-backed metadata discovery, 0 module blockers, and 0 unknown descriptor permissions.

`verifyStandaloneCompatRuntime` now depends on `runStandaloneCompatRuntimeSmoke`, rejects `echo.standalone.evidence.bootstrap.v1` placeholders, and validates the concrete `echo.standalone.runtime_compatibility.v2`, boundary, mapping, source-record, validation, migration-policy, migration-plan, manual-review, diagnostics, and NeoForge-candidate reports before Phase 14.17 can pass.

## Out Of Scope

Phase 14.17 does not:

- execute save migration
- mutate source saves
- import full platform saves
- repair pack state
- run a launcher migration flow
- depend on platform runtime APIs
- activate `neoforge.mods.toml` files as standalone module descriptors
- convert arbitrary third-party content

The next phase is Phase 14.18, the Ashfall Standalone Vertical Slice.
