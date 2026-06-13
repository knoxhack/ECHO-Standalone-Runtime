package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.data.EchoDataDefinition;
import dev.echo.standalone.runtime.data.EchoDataRegistry;
import dev.echo.standalone.runtime.data.EchoDataRuntime;
import dev.echo.standalone.runtime.data.EchoDataRuntimeResult;
import dev.echo.standalone.runtime.data.EchoDataValidationReport;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreHazardDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreHazardRegistry;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionDefinition;
import dev.echo.standalone.runtime.data.EchoWorldCoreRegionRegistry;
import dev.echo.standalone.runtime.data.EchoWorldgenBiomeDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenBiomeRegistry;
import dev.echo.standalone.runtime.data.EchoWorldgenFeatureDefinition;
import dev.echo.standalone.runtime.data.EchoWorldgenFeatureRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;

public final class EchoRuntimeDataSmokeHarness {
    private EchoRuntimeDataSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-data-smoke");
        Path dataRoot = fixtureRoot.resolve("ashfall-data-pack");

        write(dataRoot.resolve("data/ashfall/schemas/items.json"), """
                {
                  "registry": "items",
                  "requiredFields": ["displayName", "stackSize"]
                }
                """);
        write(dataRoot.resolve("data/ashfall/registries/items/ash_steel.json"), """
                {
                  "displayName": "Ash Steel",
                  "stackSize": 64,
                  "rarity": "uncommon"
                }
                """);
        write(dataRoot.resolve("data/ashfall/registries/items/toxic_filter.json"), """
                {
                  "displayName": "Toxic Filter",
                  "stackSize": 16,
                  "rarity": "rare"
                }
                """);
        write(dataRoot.resolve("data/ashfall/tags/items/scrap.json"), """
                {
                  "values": ["ashfall:ash_steel"]
                }
                """);
        write(dataRoot.resolve("data/ashfall/recipes/ash_steel_plate.json"), """
                {
                  "type": "crafting_shaped",
                  "ingredients": ["ashfall:ash_steel"],
                  "result": "ashfall:ash_steel_plate"
                }
                """);
        write(dataRoot.resolve("data/ashfall/recipe/power_cell.json"), """
                {
                  "type": "minecraft:crafting_shaped",
                  "group": "ashfall_power",
                  "category": "equipment",
                  "pattern": [" W ", "CEC", " M "],
                  "key": {
                    "W": "ashfall:scrap_wire",
                    "C": {"item": "ashfall:circuit_board"},
                    "E": {"id": "ashfall:energy_cell"},
                    "M": "#ashfall:metal_scrap"
                  },
                  "result": {
                    "id": "ashfall:power_cell",
                    "count": 1
                  }
                }
                """);
        write(dataRoot.resolve("data/ashfall/recipes/clean_glass_smelting.json"), """
                {
                  "type": "minecraft:smelting",
                  "category": "misc",
                  "ingredient": {
                    "item": "ashfall:dirty_glass"
                  },
                  "result": {
                    "item": "ashfall:clean_glass",
                    "count": 2
                  },
                  "experience": 0.2,
                  "cookingtime": 120
                }
                """);
        write(dataRoot.resolve("data/ashfall/loot_tables/crash_cache.json"), """
                {
                  "entries": ["ashfall:ash_steel", "ashfall:toxic_filter"]
                }
                """);
        write(dataRoot.resolve("data/ashfall/loot_table/blocks/ash_campfire.json"), """
                {
                  "type": "minecraft:block",
                  "pools": [
                    {
                      "rolls": 1,
                      "entries": [
                        {
                          "type": "minecraft:item",
                          "name": "echoashfallprotocol:ash_campfire"
                        }
                      ]
                    }
                  ]
                }
                """);
        write(dataRoot.resolve("data/ashfall/loot_modifiers/wiki_manual_radio_tower_cache.json"), """
                {
                  "type": "ashfall:wiki_manual",
                  "conditions": [
                    {
                      "condition": "neoforge:loot_table_id",
                      "loot_table_id": "ashfall:chests/radio_tower_cache"
                    }
                  ],
                  "guideBookId": "echoashfallprotocol:field_manual",
                  "chance": 0.12
                }
                """);
        write(dataRoot.resolve("data/ashfall/missioncore/missions/build_battery_bank.json"), """
                {
                  "id": "ashfall:build_battery_bank",
                  "chapterId": "ashfall:ashfall_crash_landing",
                  "title": "Build a Battery Bank",
                  "prerequisites": ["ashfall:stockpile_clean_water"],
                  "objectives": [
                    {
                      "id": "ashfall:build_battery_bank/place_battery_bank",
                      "type": "place_block",
                      "target": "ashfall:battery_bank",
                      "criteria": {
                        "block": "ashfall:battery_bank",
                        "hazard": "ashfall:hazard/salvage_debris"
                      }
                    }
                  ],
                  "requirements": [
                    {"item": "ashfall:energy_cell", "count": 2},
                    {"block": "ashfall:battery_bank", "count": 1}
                  ],
                  "rewards": [
                    {"item": "ashfall:scrap_wire", "count": 6}
                  ]
                }
                """);
        write(dataRoot.resolve("data/ashfall/echoworldcore/world_regions/toxic_data_basin.json"), """
                {
                  "id": "ashfall:toxic_data_basin",
                  "type": "toxic_swamp",
                  "displayName": "Toxic Data Basin",
                  "summary": "A smoke-test WorldCore region with an explicit standalone surface marker.",
                  "biomeIds": ["ashfall:toxic_swamp"],
                  "biomeTags": ["ashfall:hazardous_wasteland_biomes"],
                  "structureIds": ["ashfall:bio_lab"],
                  "hazardIds": ["ashfall:hazard/toxic_spores"],
                  "discoveryId": "ashfall:toxic_data_basin",
                  "renderProfileId": "echoworldcore:region/toxic_swamp",
                  "audioProfileId": "echoworldcore:ambience/toxic_swamp",
                  "radius": 11,
                  "sortOrder": 12,
                  "surfaceBlockId": "ashfall:toxic_scar_marker",
                  "centerX": 24,
                  "centerZ": 32,
                  "fixedY": 8
                }
                """);
        write(dataRoot.resolve("data/ashfall/echoworldcore/world_hazards/hazard/toxic_spores.json"), """
                {
                  "id": "ashfall:hazard/toxic_spores",
                  "type": "toxic_air",
                  "displayName": "Toxic Spores",
                  "summary": "Airborne spores that raise exposure in basin routes.",
                  "defaultSeverity": 66,
                  "ticking": true,
                  "statusEffectId": "echostatuscore:status/toxic_spores",
                  "radius": 11
                }
                """);
        write(dataRoot.resolve("data/ashfall/worldgen/structure/bio_lab.json"), """
                {
                  "type": "minecraft:jigsaw",
                  "biomes": "#ashfall:has_structure/bio_lab",
                  "step": "surface_structures",
                  "start_pool": "ashfall:bio_lab",
                  "size": 1,
                  "structureBlockId": "ashfall:bio_lab_marker",
                  "x": 24,
                  "y": 9,
                  "z": 32,
                  "width": 3,
                  "height": 2,
                  "depth": 2,
                  "shape": "WALL"
                }
                """);
        write(dataRoot.resolve("data/ashfall/worldgen/biome/toxic_swamp.json"), """
                {
                  "temperature": 0.9,
                  "downfall": 0.9,
                  "centerX": 24,
                  "centerZ": 32,
                  "radius": 8,
                  "tags": ["toxic", "swamp"],
                  "effects": {
                    "fog_color": 5270058,
                    "grass_color": 7907379,
                    "ambient_particle": {
                      "options": {
                        "type": "minecraft:spore_blossom_air"
                      }
                    },
                    "ambient_loop_sound": "minecraft:ambient.swamp.loop"
                  },
                  "features": [
                    [
                      "ashfall:scar_grass_patch"
                    ]
                  ],
                  "spawners": {
                    "monster": [
                      {
                        "type": "ashfall:rad_zombie",
                        "weight": 25
                      }
                    ]
                  }
                }
                """);
        write(dataRoot.resolve("data/ashfall/worldgen/configured_feature/scar_grass_patch.json"), """
                {
                  "type": "minecraft:random_patch",
                  "config": {
                    "tries": 12,
                    "feature": {
                      "feature": {
                        "type": "minecraft:simple_block",
                        "config": {
                          "to_place": {
                            "type": "minecraft:simple_state_provider",
                            "state": {
                              "Name": "ashfall:scar_grass"
                            }
                          }
                        }
                      }
                    }
                  }
                }
                """);
        write(dataRoot.resolve("data/ashfall/worldgen/placed_feature/scar_grass_patch.json"), """
                {
                  "feature": "ashfall:scar_grass_patch",
                  "placement": [
                    {
                      "type": "minecraft:count",
                      "count": 3
                    },
                    {
                      "type": "minecraft:in_square"
                    },
                    {
                      "type": "minecraft:biome"
                    }
                  ]
                }
                """);
        write(dataRoot.resolve("assets/ashfall/sounds.json"), """
                {
                  "ui.echo_message": {
                    "subtitle": "subtitles.ashfall.echo.message",
                    "sounds": [
                      "ashfall:ui/echo_message"
                    ]
                  },
                  "event.ash_storm": {
                    "subtitle": "subtitles.ashfall.event.ash_storm",
                    "sounds": []
                  }
                }
                """);

        EchoAssetRuntime assetRuntime = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "data", dataRoot, "ashfall-data-pack")
        ));
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntimeResult assets = assetRuntime.load(services, List.of(
                "ashfall:schemas/items.json",
                "ashfall:registries/items/ash_steel.json"
        ));
        EchoDataRuntimeResult data = new EchoDataRuntime().load(services, assets);

        require(services.require(EchoDataRuntimeResult.class) == data, "data runtime result should be service-bound");
        require(services.require(EchoWorldgenBiomeRegistry.class) == data.worldgenBiomes(),
                "worldgen biome registry should be service-bound");
        require(services.require(EchoWorldgenFeatureRegistry.class) == data.worldgenFeatures(),
                "worldgen feature registry should be service-bound");
        require(services.require(EchoWorldCoreRegionRegistry.class) == data.worldCoreRegions(),
                "WorldCore region registry should be service-bound");
        require(services.require(EchoWorldCoreHazardRegistry.class) == data.worldCoreHazards(),
                "WorldCore hazard registry should be service-bound");
        require(services.require(EchoDataValidationReport.class).ok(), "data validation should pass");
        require(data.documents().size() == 18, "eighteen data documents should load");
        require(data.schemas().schemas().size() == 1, "one schema should load");

        EchoDataRegistry items = data.registries().registry("items").orElseThrow();
        require(items.size() == 2, "items registry should contain two entries");
        require(items.find("ashfall:ash_steel").orElseThrow().fields().get("displayName").equals("Ash Steel"),
                "ash steel definition should be readable");
        require(data.tags().find("ashfall:scrap").orElseThrow().values().equals(List.of("ashfall:ash_steel")),
                "scrap tag should reference ash steel");
        require(data.recipes().find("ashfall:ash_steel_plate").orElseThrow().result().equals("ashfall:ash_steel_plate"),
                "recipe result should load");
        EchoRecipeDefinition powerCell = data.recipes().find("ashfall:power_cell").orElseThrow();
        require(powerCell.result().equals("ashfall:power_cell"),
                "NeoForge singular recipe result object should load");
        require(powerCell.resultCount() == 1, "NeoForge shaped recipe result count should load");
        require(powerCell.group().equals("ashfall_power"), "NeoForge shaped recipe group should load");
        require(powerCell.category().equals("equipment"), "NeoForge shaped recipe category should load");
        require(powerCell.pattern().equals(List.of(" W ", "CEC", " M ")),
                "NeoForge shaped recipe pattern should preserve row order");
        require(powerCell.ingredients().contains("ashfall:energy_cell"),
                "NeoForge shaped recipe key should load as shared data ingredients");
        require(powerCell.ingredientCounts().get("ashfall:circuit_board") == 2,
                "NeoForge shaped recipe should count repeated pattern ingredients");
        EchoRecipeDefinition cleanGlass = data.recipes().find("ashfall:clean_glass_smelting").orElseThrow();
        require(cleanGlass.result().equals("ashfall:clean_glass"),
                "Minecraft smelting recipe object result item should load");
        require(cleanGlass.resultCount() == 2, "Minecraft smelting recipe object count should load");
        require(cleanGlass.ingredientCounts().get("ashfall:dirty_glass") == 1,
                "Minecraft smelting recipe ingredient should load with count");
        require(cleanGlass.category().equals("misc"), "Minecraft smelting recipe category should load");
        require(data.loot().find("ashfall:crash_cache").orElseThrow().entries().contains("ashfall:toxic_filter"),
                "loot table should include toxic filter");
        require(data.loot().find("ashfall:blocks/ash_campfire").orElseThrow().entries()
                        .contains("echoashfallprotocol:ash_campfire"),
                "NeoForge singular loot table should expose pooled item entries");
        require(data.loot().find("ashfall:wiki_manual_radio_tower_cache").orElseThrow().entries()
                        .contains("ashfall:chests/radio_tower_cache"),
                "NeoForge loot modifier should expose target loot table id");
        require(data.missions().find("ashfall:build_battery_bank").orElseThrow()
                        .objectives().contains("ashfall:build_battery_bank/place_battery_bank"),
                "MissionCore mission objectives should load as shared data");
        require(data.missions().find("ashfall:build_battery_bank").orElseThrow()
                        .references().contains("ashfall:battery_bank"),
                "MissionCore mission references should expose block/item targets as shared data");
        EchoWorldCoreRegionDefinition toxicBasin =
                data.worldCoreRegions().find("ashfall:toxic_data_basin").orElseThrow();
        require(toxicBasin.hazardIds().equals(List.of("ashfall:hazard/toxic_spores")),
                "WorldCore regions should preserve linked hazard ids");
        require(toxicBasin.references().containsAll(List.of(
                        "ashfall:toxic_swamp",
                        "ashfall:bio_lab",
                        "echoworldcore:region/toxic_swamp",
                        "echoworldcore:ambience/toxic_swamp"
                )),
                "WorldCore regions should expose biome, structure, render, and ambience references");
        require(toxicBasin.runtimeHints().get("surfaceBlockId").equals("ashfall:toxic_scar_marker")
                        && toxicBasin.runtimeHints().get("fixedY").equals("8"),
                "WorldCore regions should preserve standalone surface-rule runtime hints");
        EchoWorldCoreHazardDefinition toxicSpores =
                data.worldCoreHazards().find("ashfall:hazard/toxic_spores").orElseThrow();
        require(toxicSpores.defaultSeverity() == 66 && toxicSpores.ticking(),
                "WorldCore hazards should preserve severity and ticking fields");
        require(toxicSpores.references().contains("echostatuscore:status/toxic_spores"),
                "WorldCore hazards should expose status-effect references");
        require(toxicSpores.runtimeHints().get("radius").equals("11"),
                "WorldCore hazards should preserve standalone hazard runtime hints");
        require(data.worldgenStructures().find("ashfall:bio_lab").orElseThrow()
                        .references().contains("#ashfall:has_structure/bio_lab"),
                "Worldgen structures should expose biome tag references as shared data");
        require(data.worldgenStructures().find("ashfall:bio_lab").orElseThrow()
                        .references().contains("ashfall:bio_lab"),
                "Worldgen structures should expose template pool references as shared data");
        require(data.worldgenStructures().find("ashfall:bio_lab").orElseThrow()
                        .runtimeHints().get("structureBlockId").equals("ashfall:bio_lab_marker"),
                "Worldgen structures should preserve standalone runtime block hints");
        require(data.worldgenStructures().find("ashfall:bio_lab").orElseThrow()
                        .runtimeHints().get("shape").equals("WALL"),
                "Worldgen structures should preserve standalone runtime placement hints");
        EchoWorldgenBiomeDefinition toxicSwamp =
                data.worldgenBiomes().find("ashfall:toxic_swamp").orElseThrow();
        require(toxicSwamp.temperature() == 0.9D && toxicSwamp.downfall() == 0.9D,
                "Worldgen biomes should preserve climate fields");
        require(toxicSwamp.fogColor() == 5270058 && toxicSwamp.grassColor() == 7907379,
                "Worldgen biomes should preserve Minecraft effects colors");
        require(toxicSwamp.ambientParticle().equals("minecraft:spore_blossom_air"),
                "Worldgen biomes should preserve ambient particle effects");
        require(toxicSwamp.tags().containsAll(List.of("swamp", "toxic")),
                "Worldgen biomes should expose data and inferred biome tags");
        require(toxicSwamp.references().contains("ashfall:scar_grass_patch")
                        && toxicSwamp.references().contains("ashfall:rad_zombie"),
                "Worldgen biomes should expose feature and spawn references as shared data");
        require(toxicSwamp.runtimeHints().get("radius").equals("8"),
                "Worldgen biomes should preserve standalone runtime overlay hints");
        EchoWorldgenFeatureDefinition configuredFeature =
                data.worldgenFeatures().findConfiguredFeature("ashfall:scar_grass_patch").orElseThrow();
        require(configuredFeature.featureKind().equals("configured_feature"),
                "Configured worldgen feature should preserve its feature kind");
        require(configuredFeature.type().equals("minecraft:random_patch"),
                "Configured worldgen feature should preserve its configured feature type");
        require(configuredFeature.references().contains("ashfall:scar_grass"),
                "Configured worldgen feature should expose nested block references as shared data");
        EchoWorldgenFeatureDefinition placedFeature =
                data.worldgenFeatures().findPlacedFeature("ashfall:scar_grass_patch").orElseThrow();
        require(placedFeature.references().contains("ashfall:scar_grass_patch"),
                "Placed worldgen feature should expose configured feature references as shared data");
        require(placedFeature.placementModifiers()
                        .containsAll(List.of("minecraft:biome", "minecraft:count", "minecraft:in_square")),
                "Placed worldgen feature should expose placement modifier types as shared data");
        require(data.sounds().find("ashfall:ui.echo_message").orElseThrow()
                        .subtitle().equals("subtitles.ashfall.echo.message"),
                "Asset sound catalog should expose sound event subtitles as shared data");
        require(data.sounds().find("ashfall:ui.echo_message").orElseThrow()
                        .soundAssets().contains("ashfall:ui/echo_message"),
                "Asset sound catalog should expose referenced sound assets as shared data");
        require(data.freezeReport().frozen(), "data runtime should freeze after load");

        boolean freezeRejected = false;
        try {
            items.register(new EchoDataDefinition(
                    "ashfall:late_item",
                    "items",
                    "smoke:late",
                    Map.of("displayName", "Late", "stackSize", 1)
            ));
        } catch (IllegalStateException expected) {
            freezeRejected = true;
        }
        require(freezeRejected, "frozen registry should reject late writes");
        writeReports(Path.of(".").toAbsolutePath().normalize(), data, services, freezeRejected);

        System.out.println("phase14.8 data runtime smoke PASS registries="
                + data.registries().registries().size()
                + " entries="
                + data.registries().totalEntries()
                + " schemas="
                + data.schemas().schemas().size()
                + " tags="
                + data.tags().tags().size()
                + " recipes="
                + data.recipes().recipes().size()
                + " loot="
                + data.loot().lootTables().size()
                + " missions="
                + data.missions().missions().size()
                + " structures="
                + data.worldgenStructures().structures().size()
                + " biomes="
                + data.worldgenBiomes().biomes().size()
                + " features="
                + data.worldgenFeatures().features().size()
                + " worldCoreRegions="
                + data.worldCoreRegions().regions().size()
                + " worldCoreHazards="
                + data.worldCoreHazards().hazards().size()
                + " sounds="
                + data.sounds().sounds().size()
                + " frozen="
                + data.freezeReport().frozen());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoDataRuntimeResult data,
            EchoDefaultRuntimeServiceRegistry services,
            boolean freezeRejected
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoDataRegistry items = data.registries().registry("items").orElseThrow();
        EchoRecipeDefinition powerCell = data.recipes().find("ashfall:power_cell").orElseThrow();
        EchoRecipeDefinition cleanGlass = data.recipes().find("ashfall:clean_glass_smelting").orElseThrow();

        write(root.resolve("runtime-data.json"), """
                {
                  "schema": "echo.standalone.runtime_data.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "documents": %d,
                  "schemas": %d,
                  "registries": %d,
                  "registryEntries": %d,
                  "tags": %d,
                  "recipes": %d,
                  "lootTables": %d,
                  "missions": %d,
                  "worldgenStructures": %d,
                  "worldgenBiomes": %d,
                  "worldgenFeatures": %d,
                  "worldCoreRegions": %d,
                  "worldCoreHazards": %d,
                  "sounds": %d,
                  "validationOk": %s,
                  "freezePolicy": "%s",
                  "frozen": %s,
                  "serviceBound": %s,
                  "worldgenBiomeRegistryBound": %s,
                  "worldCoreRegionRegistryBound": %s,
                  "soundRegistryBound": %s
                }
                """.formatted(
                data.documents().size(),
                data.schemas().schemas().size(),
                data.registries().registries().size(),
                data.registries().totalEntries(),
                data.tags().tags().size(),
                data.recipes().recipes().size(),
                data.loot().lootTables().size(),
                data.missions().missions().size(),
                data.worldgenStructures().structures().size(),
                data.worldgenBiomes().biomes().size(),
                data.worldgenFeatures().features().size(),
                data.worldCoreRegions().regions().size(),
                data.worldCoreHazards().hazards().size(),
                data.sounds().sounds().size(),
                data.validationReport().ok(),
                data.freezeReport().policy().name(),
                data.freezeReport().frozen(),
                services.find(EchoDataRuntimeResult.class).isPresent(),
                services.find(EchoWorldgenBiomeRegistry.class).isPresent(),
                services.find(EchoWorldCoreRegionRegistry.class).isPresent(),
                services.find(dev.echo.standalone.runtime.data.EchoSoundRegistry.class).isPresent()
        ));

        write(root.resolve("data-registries.json"), """
                {
                  "schema": "echo.standalone.data_registries.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "registryCount": %d,
                  "totalEntries": %d,
                  "registries": %s,
                  "itemsFrozen": %s,
                  "ashSteelDisplayName": "%s",
                  "toxicFilterStackSize": "%s"
                }
                """.formatted(
                data.registries().registries().size(),
                data.registries().totalEntries(),
                jsonRegistries(data.registries().registries()),
                items.frozen(),
                escape(String.valueOf(items.find("ashfall:ash_steel").orElseThrow().fields().get("displayName"))),
                escape(String.valueOf(items.find("ashfall:toxic_filter").orElseThrow().fields().get("stackSize")))
        ));

        write(root.resolve("data-schemas.json"), """
                {
                  "schema": "echo.standalone.data_schemas.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "schemaCount": %d,
                  "schemas": %s,
                  "validationOk": %s,
                  "validationIssueCount": %d,
                  "itemsSchemaRequiredFields": %s,
                  "frozen": %s
                }
                """.formatted(
                data.schemas().schemas().size(),
                jsonSchemas(data.schemas().schemas()),
                data.validationReport().ok(),
                data.validationReport().issues().size(),
                jsonArray(data.schemas().schemaFor("items").orElseThrow().requiredFields()),
                data.schemas().frozen()
        ));

        write(root.resolve("data-tags.json"), """
                {
                  "schema": "echo.standalone.data_tags.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "tagCount": %d,
                  "tags": %s,
                  "scrapValues": %s,
                  "frozen": %s
                }
                """.formatted(
                data.tags().tags().size(),
                jsonTags(data.tags().tags()),
                jsonArray(data.tags().find("ashfall:scrap").orElseThrow().values()),
                data.tags().frozen()
        ));

        write(root.resolve("data-recipes.json"), """
                {
                  "schema": "echo.standalone.data_recipes.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "recipeCount": %d,
                  "recipeIds": %s,
                  "shapedPowerCellResult": "%s",
                  "shapedPowerCellResultCount": %d,
                  "shapedPowerCellGroup": "%s",
                  "shapedPowerCellCategory": "%s",
                  "shapedPowerCellPattern": %s,
                  "shapedPowerCellIngredientCounts": %s,
                  "smeltingCleanGlassResult": "%s",
                  "smeltingCleanGlassResultCount": %d,
                  "smeltingCleanGlassCategory": "%s",
                  "smeltingCleanGlassIngredientCounts": %s,
                  "frozen": %s
                }
                """.formatted(
                data.recipes().recipes().size(),
                jsonArray(data.recipes().recipes().stream().map(EchoRecipeDefinition::id).toList()),
                escape(powerCell.result()),
                powerCell.resultCount(),
                escape(powerCell.group()),
                escape(powerCell.category()),
                jsonArray(powerCell.pattern()),
                jsonIntegerMap(powerCell.ingredientCounts()),
                escape(cleanGlass.result()),
                cleanGlass.resultCount(),
                escape(cleanGlass.category()),
                jsonIntegerMap(cleanGlass.ingredientCounts()),
                data.recipes().frozen()
        ));

        write(root.resolve("data-loot.json"), """
                {
                  "schema": "echo.standalone.data_loot.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "lootTableCount": %d,
                  "lootIds": %s,
                  "crashCacheEntries": %s,
                  "ashCampfireEntries": %s,
                  "wikiManualTargets": %s,
                  "frozen": %s
                }
                """.formatted(
                data.loot().lootTables().size(),
                jsonArray(data.loot().lootTables().stream().map(dev.echo.standalone.runtime.data.EchoLootDefinition::id).toList()),
                jsonArray(data.loot().find("ashfall:crash_cache").orElseThrow().entries()),
                jsonArray(data.loot().find("ashfall:blocks/ash_campfire").orElseThrow().entries()),
                jsonArray(data.loot().find("ashfall:wiki_manual_radio_tower_cache").orElseThrow().entries()),
                data.loot().frozen()
        ));

        write(root.resolve("data-freeze-policy.json"), """
                {
                  "schema": "echo.standalone.data_freeze_policy.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "policy": "%s",
                  "frozen": %s,
                  "frozenRegistries": %d,
                  "frozenSupportingRegistries": %d,
                  "lateRegistryWriteRejected": %s,
                  "registriesFrozen": %s,
                  "schemasFrozen": %s,
                  "tagsFrozen": %s,
                  "recipesFrozen": %s,
                  "lootFrozen": %s,
                  "worldgenFrozen": %s
                }
                """.formatted(
                data.freezeReport().policy().name(),
                data.freezeReport().frozen(),
                data.freezeReport().frozenRegistries(),
                data.freezeReport().frozenSupportingRegistries(),
                freezeRejected,
                data.registries().frozen(),
                data.schemas().frozen(),
                data.tags().frozen(),
                data.recipes().frozen(),
                data.loot().frozen(),
                data.worldgenStructures().frozen()
                        && data.worldgenBiomes().frozen()
                        && data.worldgenFeatures().frozen()
        ));
    }

    private static String jsonRegistries(List<EchoDataRegistry> registries) {
        return registries.stream()
                .map(registry -> "{\"registryId\": \"" + escape(registry.registryId())
                        + "\", \"size\": " + registry.size()
                        + ", \"entryIds\": " + jsonArray(registry.ids()) + "}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonSchemas(List<dev.echo.standalone.runtime.data.EchoDataSchema> schemas) {
        return schemas.stream()
                .map(schema -> "{\"schemaId\": \"" + escape(schema.schemaId())
                        + "\", \"registryId\": \"" + escape(schema.registryId())
                        + "\", \"requiredFields\": " + jsonArray(schema.requiredFields())
                        + ", \"sourceLogicalId\": \"" + escape(schema.sourceLogicalId()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonTags(List<dev.echo.standalone.runtime.data.EchoDataTag> tags) {
        return tags.stream()
                .map(tag -> "{\"id\": \"" + escape(tag.id())
                        + "\", \"registryId\": \"" + escape(tag.registryId())
                        + "\", \"values\": " + jsonArray(tag.values())
                        + ", \"sourceLogicalId\": \"" + escape(tag.sourceLogicalId()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonIntegerMap(Map<String, Integer> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": " + entry.getValue())
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
