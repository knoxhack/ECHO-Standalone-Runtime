# ECHO Standalone Data/Registry Runtime

Phase 14.8 adds the standalone data and registry runtime. It loads JSON definitions from the resource runtime, builds typed registries, validates entries against small schema definitions, loads tags, recipes, and loot tables, then freezes loaded data so later gameplay phases can rely on stable state.

The runtime is intentionally compact and platform-neutral. It does not use Minecraft registries, datapack reload listeners, or NeoForge events.

## Runtime Pieces

- `EchoDataRuntime` loads data documents from `EchoAssetRuntimeResult`.
- `EchoDataDocument` records the winning namespaced data file and parsed JSON object.
- `EchoDataSchemaRegistry` stores schema documents by target registry.
- `EchoDataRegistryStore` owns standalone named registries such as `items`.
- `EchoDataRegistry` stores `EchoDataDefinition` entries by id.
- `EchoDataTagRegistry` stores tag values per registry.
- `EchoRecipeRegistry` stores standalone recipe definitions.
- `EchoLootRegistry` stores standalone loot table definitions.
- `EchoDataValidationReport` records schema validation issues.
- `EchoDataFreezeReport` records whether the loaded data was frozen.

## Data Layout

Definitions use the Phase 14.5 asset logical id convention:

```text
data/<namespace>/schemas/<registry>.json
data/<namespace>/registries/<registry>/<id>.json
data/<namespace>/tags/<registry>/<tag>.json
data/<namespace>/recipes/<id>.json
data/<namespace>/loot_tables/<id>.json
```

Examples:

```text
data/ashfall/schemas/items.json -> ashfall:schemas/items.json
data/ashfall/registries/items/ash_steel.json -> ashfall:ash_steel in registry items
data/ashfall/tags/items/scrap.json -> ashfall:scrap tag for registry items
```

## Schema Model

The first schema model is field-presence validation:

```json
{
  "registry": "items",
  "requiredFields": ["displayName", "stackSize"]
}
```

Later phases can replace this with richer schemas, type checks, references, and authoring diagnostics.

## Freeze Policy

The default policy is `FREEZE_AFTER_LOAD`. Runtime registries, schemas, tags, recipes, and loot tables reject mutation after the load completes. This keeps later world, entity, item, and gameplay phases from seeing registry state change under them.

## Smoke Harness Coverage

The Phase 14.8 smoke harness proves:

- data documents load from mounted resource data.
- an `items` schema validates loaded item definitions.
- standalone item registry entries are readable by id.
- item tags load and resolve values.
- recipe and loot table definitions load.
- the runtime binds registries and validation into services.
- freeze policy rejects late registry mutation.
- the registry-backed asset coverage audit cross-checks live AdapterCore block/item ids against mounted blockstates, models, textures, and language keys before the renderer/gameplay stack relies on them.

The smoke writes concrete non-placeholder evidence to `runtime-data.json`, `data-registries.json`, `data-schemas.json`, `data-tags.json`, `data-recipes.json`, `data-loot.json`, and `data-freeze-policy.json`. `verifyStandaloneDataRuntime` regenerates that evidence, rejects bootstrap schemas, and requires concrete `PASS` fields for service binding, schema validation, item registries, tags, Minecraft/NeoForge-style recipes, loot tables and modifiers, MissionCore data, WorldCore regions/hazards, worldgen structures/biomes/features, sounds, and freeze-after-load enforcement.

## Out Of Scope

Phase 14.8 does not:

- implement full schema language validation
- execute recipes
- roll loot
- resolve cross-registry references
- hot reload frozen registries
- import Minecraft registries
- launch Minecraft or NeoForge

The next phase is Phase 14.9, the ECHO World Runtime Prototype.
