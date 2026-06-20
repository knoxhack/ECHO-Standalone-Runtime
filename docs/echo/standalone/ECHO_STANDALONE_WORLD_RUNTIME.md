# ECHO Standalone World Runtime Prototype

Phase 14.9 adds the first standalone world runtime prototype. It creates a small deterministic Ashfall debug world with a standalone dimension, region, chunk, cell grid, hazard field, weather field, points of interest, query helpers, and a save hook that writes world data through the Phase 14.7 save runtime.

The runtime is intentionally tiny and model-first. It does not generate terrain streams, render geometry, run physics, or import Minecraft world data.

## Runtime Pieces

- `EchoWorldRuntime` creates and service-binds a debug world.
- `EchoWorldDebugGenerator` builds the deterministic 4x4 Ashfall crash-site chunk.
- `EchoWorldState` stores world id, seed, tick, dimensions, regions, and chunks.
- `EchoWorldDimension` stores standalone dimension id, display name, environment, gravity, and owned region ids without depending on Minecraft dimension classes.
- `EchoWorldRegion` stores region id, display name, danger level, hazards, and weather profile.
- `EchoWorldChunk` stores cells, hazards, weather, and points of interest.
- `EchoWorldCell` stores position, terrain, region id, hazard ids, and blocked state.
- `EchoWorldHazard` stores type, intensity, origin, and radius.
- `EchoWorldWeatherField` stores temperature, wind, ash density, and visibility.
- `EchoWorldPoi` stores queryable world points such as Terminal and loot cache locations.
- `EchoWorldQuery` supports dimension, cell, hazard, and POI lookup.
- `EchoWorldSaveHook` writes world summaries through `EchoSaveRuntimeResult`.

## Debug World

The Phase 14.9 debug world is:

```text
worldId: ashfall-debug-world
seed: 1409
dimension: ashfall:surface
region: ashfall:crash_site
chunk: 0,0
cells: 4x4
hazard: ashfall:toxic_ash
weather: ashfall:ash_storm
POIs: echoashfallprotocol:poi/drop_pod, ashfall:crash_cache
```

The model is deliberately small enough for deterministic smoke validation and later vertical-slice bootstrapping.

## Save Hook

The world save hook writes:

```text
world/summary.json
world/chunks/0_0.json
```

These files are committed through the save runtime transaction path, included in the save manifest, and validated by the corruption checker.

## Smoke Harness Coverage

The Phase 14.9 smoke harness proves:

- world runtime result, state, query, and save hook are service-bound.
- the debug world has one dimension, one region, one chunk, sixteen cells, one hazard, and two POIs.
- the Ashfall surface dimension is queryable and owns the crash-site region.
- origin cell terrain is queryable.
- toxic ash hazard intensity is queryable.
- Terminal POI is queryable.
- world save hook writes summary and chunk data.
- the resulting save manifest tracks world files.
- the save corruption checker reports the world save as healthy.

The smoke writes concrete non-placeholder evidence to `runtime-world.json`, `world-dimensions.json`, `world-regions.json`, `world-chunks.json`, `world-hazards.json`, `world-weather.json`, `world-pois.json`, and `world-save-hooks.json`. `verifyStandaloneWorldRuntime` regenerates that evidence, rejects bootstrap schemas, and requires concrete `PASS` fields for service binding, debug-world shape, dimension ownership, region hazard/weather data, chunk/cell layout, toxic ash hazard queries, ash storm weather, POI queries, and save-hook manifest/corruption-check coverage.

## Out Of Scope

Phase 14.9 does not:

- generate infinite terrain
- stream chunks
- simulate entities
- render the world
- run physics or pathfinding
- execute weather gameplay effects
- import Minecraft world saves
- launch Minecraft or NeoForge

The next phase is Phase 14.10, the ECHO Entity Runtime Prototype.
