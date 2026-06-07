# ECHO Standalone Item And Inventory Runtime

Phase 14.11 adds the first standalone item and inventory runtime. It defines item ids, item definitions, stacks, inventory containers, inventory operations, consumption, simple crafting, deterministic loot grants, tooltip rendering, and an item save hook that writes through the Phase 14.7 save runtime.

The runtime stays data-shaped and platform-neutral. It does not import Minecraft item stacks, item registries, NBT, capabilities, menus, or NeoForge inventory APIs.

## Runtime Pieces

- `EchoItemRuntime` creates and service-binds the debug item runtime.
- `EchoItemRegistry` stores deterministic item definitions and tag lookup.
- `EchoItemDefinition` stores id, display name, category, max stack size, weight, tags, and tooltip lines.
- `EchoItemStack` stores an item definition and bounded stack quantity.
- `EchoInventoryContainer` stores owner metadata, label, capacity, and slots.
- `EchoInventoryStore` stores deterministic inventory containers.
- `EchoInventoryOperations` handles add, consume, count, space, and transfer operations.
- `EchoItemCraftingSystem` validates ingredient counts, reserves output space, consumes ingredients, and grants output.
- `EchoItemLootRuntime` grants deterministic loot table entries into inventories.
- `EchoItemTooltipRenderer` renders simple text tooltips from item definitions and stacks.
- `EchoItemSaveHook` writes inventory summaries through `EchoSaveRuntimeResult`.

## Debug Items

The Phase 14.11 debug item registry contains:

```text
ashfall:salvaged_metal
ashfall:water_ration
ashfall:filter_canister
ashfall:patched_filter
ashfall:scavenger_blade
```

The player inventory starts with salvaged metal, water, and a filter canister. The crash cache starts with salvaged metal and a scavenger blade.

## Inventory Operations

Inventory operations are deterministic and stack-aware:

- add first merges into compatible stacks, then fills empty slots.
- transfer moves from a source slot into the target inventory.
- consume checks total quantity before mutating slots.
- crafting checks ingredient quantities and output space before consuming inputs.
- loot grants entries in table order and reports partial grant reasons.

## Save Hook

The item save hook writes:

```text
items/summary.json
items/inventories/inventory_player-001.json
items/inventories/container_crash-cache.json
```

These files are committed through the save runtime transaction path, included in the save manifest, and validated by the corruption checker.

## Smoke Harness Coverage

The Phase 14.11 smoke harness proves:

- item runtime result, registry, and inventory store are service-bound.
- the debug item registry has five definitions.
- crafting tags resolve deterministic definitions.
- player pack and crash cache are created.
- stack merge increases water count.
- blade transfer moves from cache to player pack.
- water consumption reduces the player count.
- patched filter crafting consumes metal and canister inputs.
- deterministic crash-cache loot grants three items.
- tooltip rendering includes display name and tags.
- item save hook writes summary and inventory files.
- the resulting save manifest tracks item files.
- the save corruption checker reports the item save as healthy.
- `runStandaloneRegistryAssetCoverageAudit` now verifies mounted item models/textures for the live AdapterCore item registry; current coverage has 70 of 70 item models and 70 item rows with at least one texture present.

## Out Of Scope

Phase 14.11 does not:

- render item icons
- open inventory UI screens
- attach equipment stats to combat
- run durability or repair systems
- import Minecraft item registries
- serialize NBT
- expose networked inventory sync

The next phase is Phase 14.12, the ECHO Gameplay Systems Runtime.
