# AdapterCore Parity Matrix

AdapterCore is the only bridge for Ashfall parity across NeoForge, ECHO Native Loader, and ECHO Runtime Standalone. Standalone gameplay may implement these targets, but it must bind through AdapterCore contracts instead of creating a standalone-only duplicate gameplay system.

| NeoForge feature | AdapterCore binding | Standalone behavior |
| --- | --- | --- |
| DeferredRegister<Block> and blockstate/model assets | `registry.blocks.toxic_ash_block` | Voxel registry exposes real Ashfall block IDs such as `echoashfallprotocol:fallout_dust` through AdapterCore. |
| DeferredRegister<Item>, creative tabs, and item components | `registry.items.water_ration` | Hotbar and inventory items resolve real Ashfall item IDs such as `echoashfallprotocol:clean_water_bottle` through AdapterCore. |
| EntityType registration, AI, spawn, and sync hooks | `registry.entities.scavenger_bandit` | Standalone entity runtime uses AdapterCore entity contracts instead of NeoForge classes. |
| RecipeSerializer, RecipeType, and datapack recipe JSON | `data.recipes.field_filter_patch` | Crafting recipes resolve as data contracts for standalone item crafting, currently backed by `echoashfallprotocol:filter_cartridge_basic`. |
| Loot tables and NeoForge global loot modifiers | `data.loot.crash_cache` | Cache and reward loot resolve as AdapterCore data, not NeoForge loot APIs. |
| Structure templates, pools, and feature placement | `world.structures.crash_site_outpost` | Crash-site and POI layout contracts feed the voxel world generator. |
| MenuType, Screen registration, HUD, and terminal screens | `ui.screens.field_terminal` | Terminal, inventory, mission log, and HUD screens render from standalone UI targets. |
| SoundEvent registration and sound JSON | `registry.sounds.radio_static` | Audio cues bind to standalone buses and generated audio events. |
| MissionCore services, objectives, and progression hooks | `gameplay.missions.secure_crash_site` | Mission state is shared by playable voxel, UI, saves, and beta gate runtime. |
| SavedData, attachments, and profile migration state | `save.records.live_mission_state` | Save profile writes world, player, hotbar, mission, and render snapshots. |
| Biome, region, hazard, POI, and worldgen datapacks | `world.regions.crash_site` | Ashfall regions, hazards, and materials drive standalone voxel chunks. |
| Custom payload channels and packet handlers | `network.hooks.live_state_sync` | Local sync contracts cover entity and inventory packet behavior. |
| Brigadier command registration | `commands.ashfall_status` | Runtime command hooks expose debug and terminal command targets. |

## Contract Lock

- Required beta domains: blocks, items, entities, recipes, loot, structures, UI screens, sounds, missions, saves, worldgen, networking, commands.
- Required runtime targets: NeoForge, ECHO Native Loader, ECHO Runtime Standalone.
- Beta readiness blocks when module coverage has adapter gaps, when any standalone module omits a runtime target, when a required AdapterCore domain is not represented, or when the Ashfall standalone bridge lacks a parity matrix entry.
