# ECHO Standalone Resource + Asset Runtime

Phase 14.5 adds the first standalone resource and asset runtime. It indexes mounted pack roots, resolves assets by namespace, detects conflicts, reports missing required assets, loads text resources, and performs a deterministic dev hot reload diff.

The runtime is directory/archive-backed and intentionally small. It supports Minecraft-style resource-pack layouts from folders, `.zip` archives, and `.jar` archives without using Minecraft or NeoForge resource manager APIs.

## Runtime Pieces

- `EchoAssetRuntime` orchestrates mount indexing, resolving, conflict detection, missing detection, and service binding.
- `EchoAssetMount` describes an ordered filesystem mount.
- `EchoAssetIndexBuilder` scans mounted directory roots and `.zip`/`.jar` archive roots for `assets/<namespace>/...` and `data/<namespace>/...`.
- `EchoAssetIndex` groups entries by logical id and namespace.
- `EchoAssetResolver` resolves the highest-order mounted asset for a logical id.
- `EchoAssetLoader` loads bytes or UTF-8 text.
- `EchoAssetConflictDetector` reports logical ids provided by more than one mount.
- `EchoAssetMissingDetector` reports required logical ids that cannot resolve.
- `EchoAssetHotReload` rebuilds the index and reports added, removed, and changed logical ids.
- `EchoLangRuntime`, `EchoThemeAssetRuntime`, and `EchoMaterialAssetRuntime` are small typed loaders over the shared resolver.
- `EchoDataRuntime` and `EchoDataPack` establish the data-pack boundary for Phase 14.8.

## Logical Ids

Mounted files use this convention:

```text
assets/<namespace>/<category>/<path>
data/<namespace>/<category>/<path>
```

Both produce logical ids in this shape:

```text
<namespace>:<category>/<path>
```

Examples:

```text
assets/ashfall/textures/gui/terminal.png -> ashfall:textures/gui/terminal.png
data/ashfall/world_regions/crash_site.json -> ashfall:world_regions/crash_site.json
data/ashfall/themes/cyberglass.json -> ashfall:themes/cyberglass.json
```

When more than one mount provides the same logical id, the highest mount order wins for resolution and the conflict detector records the override relationship.

## Smoke Harness Coverage

The Phase 14.5 smoke harness proves:

- `echo` and `ashfall` namespaces are indexed.
- assets and data definitions are indexed from mounted roots.
- a dev override wins over a base pack asset.
- conflicting logical ids are reported.
- required missing assets are reported.
- language, theme, and material loaders can load text JSON.
- zip and jar archive packs can be discovered, indexed, summarized, and loaded through the same resolver.
- Minecraft-style blockstates, models, textures, lang files, and pack metadata can be scanned from archive packs.
- hot reload reports a newly added asset.
- `runStandaloneRegistryAssetCoverageAudit` walks the live AdapterCore block/item registry against the mounted Minecraft resource-pack resolver and writes `reports/echo/standalone/registry-asset-coverage.json`.
- Current registry asset coverage proves 192 of 192 registered block/item rows complete, with 122 of 122 blockstates, 122 of 122 block models, 70 of 70 item models, 300 of 300 declared texture files, and 192 of 192 language keys present.
- The OpenGL client atlas caches decoded resource-pack tiles and block texture resolutions across atlas rebuilds; `runStandaloneClientBlockTextureResolverSmoke` covers repeated direct texture-id and model-resolved cache hits.
- The OpenGL client renderer caches CPU chunk meshes across render-region refreshes using chunk and neighbor versions, while the debug overlay reports mesh cache hits/builds/evictions and atlas cache pressure.

## Current Mount Model

Phase 14.5 supports:

```text
runtime defaults
pack base assets
pack data packs
dev override assets
external resource-pack folders
external resource-pack zip archives
external resource-pack jar archives
```

Later phases can expand this with platform assets, module assets, variant assets, channel overrides, user overrides, and live editor/dev mounts.

## Out Of Scope

Phase 14.5 does not:

- decode image formats
- upload or download assets
- generate missing art
- mount assets into a renderer
- validate schemas beyond file discovery
- parse recipes, loot, or registries
- launch Minecraft or NeoForge
- depend on Minecraft resource APIs

The next phase is Phase 14.6, the ECHO UI Runtime.
