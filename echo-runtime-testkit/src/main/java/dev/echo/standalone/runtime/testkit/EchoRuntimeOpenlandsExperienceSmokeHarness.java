package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageAuditor;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageStatus;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanResult;
import dev.echo.standalone.runtime.compat.EchoNeoForgeMetadataScanner;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public final class EchoRuntimeOpenlandsExperienceSmokeHarness {
    private static final String MODULE_ID = "echoopenlandsprotocol";
    private static final List<String> RUNTIME_TARGETS = List.of(
            "echo_native",
            "echo_runtime_standalone",
            "neoforge"
    );
    private static final List<EchoAdapterCoreDomain> REQUIRED_DOMAINS = List.of(
            EchoAdapterCoreDomain.BLOCKS,
            EchoAdapterCoreDomain.BIOMES,
            EchoAdapterCoreDomain.CREATURES,
            EchoAdapterCoreDomain.DATA,
            EchoAdapterCoreDomain.ITEMS,
            EchoAdapterCoreDomain.LOOT,
            EchoAdapterCoreDomain.PROGRESSION,
            EchoAdapterCoreDomain.PLAYTESTS,
            EchoAdapterCoreDomain.RECIPES,
            EchoAdapterCoreDomain.SAVES,
            EchoAdapterCoreDomain.SOUNDS,
            EchoAdapterCoreDomain.STRUCTURES,
            EchoAdapterCoreDomain.TUTORIALS,
            EchoAdapterCoreDomain.WAYSTONES,
            EchoAdapterCoreDomain.WORLDGEN
    );
    private static final List<String> REQUIRED_SAVE_FIELDS = List.of(
            "inventory",
            "hotbar",
            "placedBlocks",
            "chestContents",
            "bedrollSpawn",
            "campfireLitState",
            "shelterScore",
            "waystoneState",
            "holomapRegionDiscovery"
    );
    private static final List<String> HARDCORE_FLAGS = List.of(
            "stamina",
            "hydration",
            "foodSpoilage",
            "temperatureDamage"
    );
    private static final List<String> EXPECTED_WAYSTONE_STATES = List.of(
            "undiscovered",
            "discovered",
            "debris_cleared",
            "stone_repaired",
            "fitted",
            "charged",
            "bound",
            "active"
    );

    private EchoRuntimeOpenlandsExperienceSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path modulesRoot = args.length > 1
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : echoModulesRoot(standaloneRoot);
        Path openlandsRoot = modulesRoot.resolve(MODULE_ID).toAbsolutePath().normalize();
        Path resourcesRoot = openlandsRoot.resolve("src/main/resources").toAbsolutePath().normalize();
        Path dataRoot = resourcesRoot.resolve("data/" + MODULE_ID + "/openlands").toAbsolutePath().normalize();
        Path assetsRoot = resourcesRoot.resolve("assets/" + MODULE_ID).toAbsolutePath().normalize();

        require(Files.isDirectory(openlandsRoot), "Openlands module root should exist: " + openlandsRoot);
        require(Files.isDirectory(dataRoot), "Openlands data root should exist: " + dataRoot);
        require(Files.isDirectory(assetsRoot), "Openlands asset root should exist: " + assetsRoot);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        List<Path> roots = moduleRoots(standaloneRoot, modulesRoot);
        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(roots, services);
        EchoAdapterCoreModuleCoverageReport coverage = new EchoAdapterCoreModuleCoverageAuditor()
                .audit(modules, EchoAdapterCoreStandaloneContentBridge.ashfallLive());
        EchoNeoForgeMetadataScanResult neoForgeMetadata = new EchoNeoForgeMetadataScanner().scan(roots);

        EchoRuntimeModuleDescriptor descriptor = modules.registry().find(MODULE_ID)
                .orElseThrow(() -> new AssertionError("Openlands descriptor should be discovered"));
        EchoAdapterCoreModuleCoverageEntry openlands = coverage.require(MODULE_ID);

        require(descriptor.standalone(), "Openlands should declare standalone support");
        require(descriptor.official(), "Openlands should be classified as an official ECHO pack");
        require(descriptor.role().equals("official_pack"), "Openlands should declare role=official_pack");
        require(descriptor.kind().equals("pack_root"), "Openlands should declare kind=pack_root");
        require(descriptor.executableEntrypoint().equals("com.knoxhack.echoopenlandsprotocol.EchoOpenlandsNativeModule"),
                "Openlands native entrypoint should be stable");
        require(modules.registry().runtimeStatus(MODULE_ID) == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "Openlands should be runtime-active in descriptor coverage");
        require(!modules.moduleGraph().failedModuleIds().contains(MODULE_ID),
                "Openlands should have no dependency graph failure");
        require(openlands.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE,
                "Openlands should be active in AdapterCore coverage");
        require(coverage.contractLockedForBeta(),
                "Openlands experience report cannot pass until the AdapterCore beta contract is locked");
        for (EchoAdapterCoreDomain domain : REQUIRED_DOMAINS) {
            require(openlands.adapterDomains().contains(domain),
                    "Openlands should declare AdapterCore domain " + domain.id());
        }
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            require(openlands.adapterRuntimes().contains(runtimeKind),
                    "Openlands should declare runtime " + runtimeKind.adapterId());
        }
        require(neoForgeMetadata.find(MODULE_ID).isPresent(),
                "Openlands NeoForge metadata should be discoverable as compatibility input");
        require(neoForgeMetadata.errorCount() == 0,
                "NeoForge metadata scan should not contain parse errors");

        OpenlandsData data = loadData(dataRoot, assetsRoot);
        verifyDataContracts(data, assetsRoot);
        List<String> warnings = warnings(data, openlandsRoot);
        writeReport(standaloneRoot, openlandsRoot, assetsRoot, coverage, openlands, data, warnings);

        System.out.println("openlands experience smoke PASS domains="
                + openlands.adapterDomains().size()
                + " blocks=" + data.blocks.size()
                + " items=" + data.items.size()
                + " recipes=" + data.recipes.size()
                + " biomes=" + data.biomes.size()
                + " creatures=" + data.creatures.size()
                + " waystoneStates=" + data.waystoneStates.size()
                + " firstHour=" + data.firstHourSteps.size()
                + " warnings=" + warnings.size());
    }

    private static OpenlandsData loadData(Path dataRoot, Path assetsRoot) throws IOException {
        Map<String, Object> conformance = readJson(dataRoot.resolve("conformance/openlands_mvp_registry.json"));
        Map<String, Object> blocks = readJson(dataRoot.resolve("blocks/mvp_blocks.json"));
        Map<String, Object> items = readJson(dataRoot.resolve("items/mvp_items.json"));
        Map<String, Object> recipes = readJson(dataRoot.resolve("recipes/mvp_recipes.json"));
        Map<String, Object> loot = readJson(dataRoot.resolve("loot/mvp_loot.json"));
        Map<String, Object> tags = readJson(dataRoot.resolve("tags/mvp_tags.json"));
        Map<String, Object> biomes = readJson(dataRoot.resolve("biomes/mvp_biomes.json"));
        Map<String, Object> structures = readJson(dataRoot.resolve("structures/mvp_landmarks.json"));
        Map<String, Object> creatures = readJson(dataRoot.resolve("creatures/mvp_creatures.json"));
        Map<String, Object> waystones = readJson(dataRoot.resolve("waystones/waystone_contract.json"));
        Map<String, Object> progression = readJson(dataRoot.resolve("progression/first_hour_route.json"));
        Map<String, Object> tutorials = readJson(dataRoot.resolve("tutorials/first_hour_prompts.json"));
        Map<String, Object> playtests = readJson(dataRoot.resolve("playtests/mvp_first_hour_acceptance.json"));
        Map<String, Object> holomap = readJson(dataRoot.resolve("holomap/mvp_regions.json"));
        Map<String, Object> sounds = readJson(dataRoot.resolve("sounds/mvp_sound_contract.json"));
        Map<String, Object> modes = readJson(dataRoot.resolve("config/game_modes.json"));
        Map<String, Object> assetManifest = readJson(assetsRoot.resolve("asset_manifest.json"));

        return new OpenlandsData(
                conformance,
                blocks,
                items,
                recipes,
                loot,
                tags,
                biomes,
                structures,
                creatures,
                waystones,
                progression,
                tutorials,
                playtests,
                holomap,
                sounds,
                modes,
                assetManifest,
                ids(objectList(blocks.get("blocks"))),
                ids(objectList(items.get("items"))),
                ids(objectList(recipes.get("recipes"))),
                ids(objectList(biomes.get("biomes"))),
                ids(objectList(structures.get("landmarks"))),
                ids(objectList(creatures.get("creatures"))),
                ids(objectList(tutorials.get("prompts"))),
                stringList(playtests.get("requiredRouteSteps")),
                ids(objectList(playtests.get("acceptanceScenarios"))),
                ids(objectList(playtests.get("saveLoadCheckpoints"))),
                objectList(progression.get("firstHour")).stream()
                        .map(item -> text(item.get("id")))
                        .filter(value -> !value.isBlank())
                        .toList(),
                objectList(waystones.get("stateMachine")).stream()
                        .map(item -> text(item.get("state")))
                        .filter(value -> !value.isBlank())
                        .toList(),
                stringList(progression.get("saveLoadAcceptance")),
                stringList(conformance.get("runtimeTargets")),
                stringList(conformance.get("requiredContentRoots")),
                stringList(conformance.get("requiredAssetRoots"))
        );
    }

    private static void verifyDataContracts(OpenlandsData data, Path assetsRoot) throws IOException {
        require(data.runtimeTargets.containsAll(RUNTIME_TARGETS),
                "Openlands conformance should declare all runtime targets");
        for (Map<String, Object> payload : data.parityPayloads()) {
            require(stringList(payload.get("runtimeParity")).containsAll(RUNTIME_TARGETS),
                    "Openlands payload should declare runtime parity for schema " + text(payload.get("schema")));
        }

        require(data.blocks.size() >= stringList(data.conformance.get("blockRegistry")).size(),
                "block payload should satisfy conformance block registry");
        require(data.items.size() >= stringList(data.conformance.get("itemRegistry")).size(),
                "item payload should satisfy conformance item registry");
        require(data.recipes.size() >= stringList(data.conformance.get("recipeRegistry")).size(),
                "recipe payload should satisfy conformance recipe registry");
        require(data.biomes.size() >= stringList(data.conformance.get("biomeRegistry")).size(),
                "biome payload should satisfy conformance biome registry");
        require(data.creatures.size() >= stringList(data.conformance.get("creatureRegistry")).size(),
                "creature payload should satisfy conformance creature registry");
        require(data.blocks.containsAll(stringList(data.conformance.get("blockRegistry"))),
                "all conformance block ids should resolve in block payload");
        require(data.items.containsAll(stringList(data.conformance.get("itemRegistry"))),
                "all conformance item ids should resolve in item payload");
        require(data.recipes.containsAll(stringList(data.conformance.get("recipeRegistry"))),
                "all conformance recipe ids should resolve in recipe payload");
        require(data.biomes.containsAll(stringList(data.conformance.get("biomeRegistry"))),
                "all conformance biome ids should resolve in biome payload");
        require(data.creatures.containsAll(stringList(data.conformance.get("creatureRegistry"))),
                "all conformance creature ids should resolve in creature payload");

        verifyAssetManifest(data, assetsRoot);

        List<String> expectedFirstHourSteps = List.of(
                "safe_spawn",
                "first_gathering",
                "first_tools",
                "first_shelter",
                "sleep_and_recover",
                "first_exploration_hook",
                "first_waystone");
        TreeSet<String> ownedAndMovedFirstHourSteps = new TreeSet<>(data.firstHourSteps);
        ownedAndMovedFirstHourSteps.addAll(stringList(data.progressionPayload.get("foundationMovedSteps")));
        require(ownedAndMovedFirstHourSteps.containsAll(expectedFirstHourSteps)
                        && data.firstHourSteps.contains("first_waystone"),
                "Openlands first-hour route should cover foundation spawn through its first waystone milestone");
        require(data.saveLoadAcceptance.containsAll(REQUIRED_SAVE_FIELDS),
                "Openlands first-hour route should declare save/load fields");
        require(data.waystoneStates.equals(EXPECTED_WAYSTONE_STATES),
                "Openlands waystone state machine should stay stable");
        verifyPlaytests(data);

        verifyGameModes(data.gameModes);
        verifyCrossReferences(data);
    }

    private static void verifyPlaytests(OpenlandsData data) {
        require(stringList(data.playtestsPayload.get("runtimeParity")).containsAll(RUNTIME_TARGETS),
                "Openlands playtest payload should declare all runtime parity targets");
        require(text(data.playtestsPayload.get("defaultMode")).equals("openlands_standard"),
                "Openlands playtest payload should default to Openlands Standard");
        Map<String, Object> sourceContracts = object(data.playtestsPayload.get("sourceContracts"));
        require(text(sourceContracts.get("route")).equals("progression/first_hour_route.json"),
                "Openlands playtest payload should bind to the first-hour route");
        require(text(sourceContracts.get("tutorials")).equals("tutorials/first_hour_prompts.json"),
                "Openlands playtest payload should bind to first-hour tutorials");
        require(text(sourceContracts.get("waystones")).equals("waystones/waystone_contract.json"),
                "Openlands playtest payload should bind to waystone state");
        require(text(sourceContracts.get("holomap")).equals("holomap/mvp_regions.json"),
                "Openlands playtest payload should bind to HoloMap regions");
        Set<String> combinedFirstHourSteps = combinedFirstHourSteps(data);
        require(new TreeSet<>(data.requiredPlaytestRouteSteps).equals(combinedFirstHourSteps),
                "Openlands playtest route steps should mirror the first-hour route");
        require(data.playtestScenarios.containsAll(combinedFirstHourSteps),
                "Openlands playtest scenarios should cover every first-hour step");
        require(data.playtestSaveLoadCheckpoints.size() >= 2,
                "Openlands playtest should include first-hour save/load checkpoints");
        for (Map<String, Object> scenario : objectList(data.playtestsPayload.get("acceptanceScenarios"))) {
            String routeStep = text(scenario.get("routeStep"));
            require(combinedFirstHourSteps.contains(routeStep),
                    "Openlands playtest scenario references unknown route step " + routeStep);
            Map<String, Object> requires = object(scenario.get("requires"));
            for (String block : stringList(requires.get("blocks"))) {
                require(resolvesReference(data.blocks, block),
                        "Openlands playtest scenario references unknown block " + block);
            }
            for (String item : stringList(requires.get("items"))) {
                require(resolvesReference(data.items, item),
                        "Openlands playtest scenario references unknown item " + item);
            }
            for (String recipe : stringList(requires.get("recipes"))) {
                require(resolvesReference(data.recipes, recipe),
                        "Openlands playtest scenario references unknown recipe " + recipe);
            }
            for (String biome : stringList(requires.get("biomes"))) {
                require(resolvesReference(data.biomes, biome),
                        "Openlands playtest scenario references unknown biome " + biome);
            }
            for (String landmark : stringList(requires.get("landmarks"))) {
                require(resolvesReference(data.structures, landmark),
                        "Openlands playtest scenario references unknown landmark " + landmark);
            }
            for (String creature : stringList(requires.get("creaturesAllowed"))) {
                require(resolvesReference(data.creatures, creature),
                        "Openlands playtest scenario references unknown creature " + creature);
            }
            for (String prompt : stringList(requires.get("tutorialPrompts"))) {
                require(resolvesReference(data.tutorials, prompt),
                        "Openlands playtest scenario references unknown tutorial prompt " + prompt);
            }
            for (String state : stringList(requires.get("waystoneStates"))) {
                require(data.waystoneStates.contains(state),
                        "Openlands playtest scenario references unknown waystone state " + state);
            }
            require(!objectList(scenario.get("runtimeActions")).isEmpty(),
                    "Openlands playtest scenario should declare runtime actions: " + text(scenario.get("id")));
            require(!stringList(scenario.get("successEvidence")).isEmpty(),
                    "Openlands playtest scenario should declare success evidence: " + text(scenario.get("id")));
        }
        for (Map<String, Object> checkpoint : objectList(data.playtestsPayload.get("saveLoadCheckpoints"))) {
            require(data.playtestScenarios.contains(text(checkpoint.get("afterScenario"))),
                    "Openlands save/load checkpoint references unknown scenario " + text(checkpoint.get("afterScenario")));
            for (String item : stringList(checkpoint.get("sampleInventoryItems"))) {
                require(resolvesReference(data.items, item),
                        "Openlands save/load checkpoint references unknown item " + item);
            }
            for (String block : stringList(checkpoint.get("samplePlacedBlocks"))) {
                require(resolvesReference(data.blocks, block),
                        "Openlands save/load checkpoint references unknown placed block " + block);
            }
            require(!stringList(checkpoint.get("requiredAssertions")).isEmpty(),
                    "Openlands save/load checkpoint should declare assertions: " + text(checkpoint.get("id")));
        }
        Map<String, Object> waystoneAlpha = object(data.playtestsPayload.get("waystonePublicAlphaScenario"));
        require(stringList(waystoneAlpha.get("requiresStates")).containsAll(EXPECTED_WAYSTONE_STATES),
                "Openlands public waystone playtest should cover every waystone state");
        require(resolvesAllReferences(data.items, stringList(waystoneAlpha.get("requiresItems"))),
                "Openlands public waystone playtest should resolve required items");
        require(resolvesAllReferences(data.recipes, stringList(waystoneAlpha.get("requiresRecipes"))),
                "Openlands public waystone playtest should resolve required recipes");
        require(resolvesAllReferences(data.blocks, stringList(waystoneAlpha.get("requiresBlocks"))),
                "Openlands public waystone playtest should resolve required blocks");
        require(stringList(waystoneAlpha.get("mustPersist")).contains("state"),
                "Openlands public waystone playtest should persist state");
        Map<String, Object> holomapAcceptance = object(data.playtestsPayload.get("holomapAcceptance"));
        require(Boolean.TRUE.equals(holomapAcceptance.get("fallbackRequired")),
                "Openlands playtest should require HoloMap fallback persistence");
        require(stringList(holomapAcceptance.get("requiredLayers")).containsAll(List.of("region_names", "old_roads", "waystones")),
                "Openlands playtest should cover core HoloMap layers");
        require(stringList(data.playtestsPayload.get("releaseEvidence")).contains("first_hour_runtime_playtest_pass"),
                "Openlands playtest should declare first-hour runtime playtest release evidence");
    }

    private static void verifyAssetManifest(OpenlandsData data, Path assetsRoot) throws IOException {
        require(List.of("owned_placeholder_coverage", "owned_openlands_coverage_after_foundation_split")
                        .contains(text(data.assetManifest.get("status"))),
                "Openlands asset manifest should declare owned Openlands placeholder coverage");
        require(Boolean.FALSE.equals(data.assetManifest.get("publicReleaseAllowedWithPlaceholders")),
                "Openlands placeholder assets must not be public-release approved");
        Map<String, Object> placeholderPolicy = object(data.assetManifest.get("placeholderPolicy"));
        require(Boolean.TRUE.equals(placeholderPolicy.get("mustBeOriginal")),
                "Openlands placeholder policy must require original Echo assets");
        require(text(placeholderPolicy.get("replacementGate")).equals("public_alpha_art_review"),
                "Openlands placeholder policy should require public alpha art review");
        Map<String, Object> pathTemplates = object(data.assetManifest.get("pathTemplates"));
        require(text(pathTemplates.get("blockstate")).equals("blockstates/{id}.json"),
                "Openlands blockstate asset template mismatch");
        require(text(pathTemplates.get("blockModel")).equals("models/block/{id}.json"),
                "Openlands block model asset template mismatch");
        require(text(pathTemplates.get("blockTexture")).equals("textures/block/{texture}.png"),
                "Openlands block texture asset template mismatch");
        require(text(pathTemplates.get("itemModel")).equals("models/item/{id}.json"),
                "Openlands item model asset template mismatch");
        require(text(pathTemplates.get("itemTexture")).equals("textures/item/{texture}.png"),
                "Openlands item texture asset template mismatch");
        Map<String, Object> coverage = object(data.assetManifest.get("mvpCoverage"));
        require(new TreeSet<>(stringList(coverage.get("blockIds"))).containsAll(data.blocks),
                "Openlands asset manifest block coverage should include current MVP block ids");
        require(new TreeSet<>(stringList(coverage.get("itemIds"))).containsAll(data.items),
                "Openlands asset manifest item coverage should include current MVP item ids");
        for (Map<String, Object> block : objectList(data.blocksPayload.get("blocks"))) {
            String id = canonicalId(text(block.get("id")));
            String texture = localTextureKey(text(block.get("texture")), "block");
            Path blockstate = assetsRoot.resolve("blockstates/" + id + ".json");
            Path modelPath = assetsRoot.resolve("models/block/" + id + ".json");
            Path texturePath = assetsRoot.resolve("textures/block/" + texture + ".png");
            Map<String, Object> blockstateJson = readJson(blockstate);
            Map<String, Object> variants = object(blockstateJson.get("variants"));
            require(text(object(variants.get("")).get("model")).equals(MODULE_ID + ":block/" + id),
                    "Openlands blockstate should point to generated model for " + id);
            Map<String, Object> model = readJson(modelPath);
            require(text(object(model.get("textures")).get("all")).equals(MODULE_ID + ":block/" + texture),
                    "Openlands block model should point to declared texture for " + id);
            require(!objectList(model.get("elements")).isEmpty(),
                    "Openlands block model should define placeholder geometry for " + id);
            require(isPng(texturePath), "Openlands block texture should be a valid PNG for " + id);
        }
        for (Map<String, Object> item : objectList(data.itemsPayload.get("items"))) {
            String id = canonicalId(text(item.get("id")));
            String texture = localTextureKey(text(item.get("texture")), "item");
            Path modelPath = assetsRoot.resolve("models/item/" + id + ".json");
            Path texturePath = assetsRoot.resolve("textures/item/" + texture + ".png");
            Map<String, Object> model = readJson(modelPath);
            require(text(object(model.get("textures")).get("layer0")).equals(MODULE_ID + ":item/" + texture),
                    "Openlands item model should point to declared texture for " + id);
            require(!objectList(model.get("elements")).isEmpty(),
                    "Openlands item model should define placeholder geometry for " + id);
            require(isPng(texturePath), "Openlands item texture should be a valid PNG for " + id);
        }
    }

    private static void verifyGameModes(Map<String, Object> modesPayload) {
        require(text(modesPayload.get("defaultMode")).equals("openlands_standard"),
                "Openlands default mode should be openlands_standard");
        List<Map<String, Object>> modes = objectList(modesPayload.get("modes"));
        require(modes.size() >= 6, "Openlands should expose the current mode set");
        Map<String, Object> standard = modes.stream()
                .filter(mode -> text(mode.get("id")).equals("openlands_standard"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Openlands Standard mode should exist"));
        Map<String, Object> hardlands = modes.stream()
                .filter(mode -> text(mode.get("id")).equals("openlands_hardlands"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Openlands Hardlands mode should exist"));
        Map<String, Object> standardRules = object(standard.get("rules"));
        require(text(standardRules.get("hunger")).equals("gentle"),
                "Openlands Standard should keep gentle hunger");
        require(text(standardRules.get("deathPack")).equals("recoverable"),
                "Openlands Standard should keep recoverable death");
        for (String flag : HARDCORE_FLAGS) {
            require(Boolean.FALSE.equals(standardRules.get(flag)),
                    "Openlands Standard should disable " + flag);
        }
        Map<String, Object> hardlandsRules = object(hardlands.get("rules"));
        require(Boolean.TRUE.equals(hardlandsRules.get("stamina")),
                "Hardlands should be the opt-in stamina mode");
    }

    private static void verifyCrossReferences(OpenlandsData data) {
        for (Map<String, Object> biome : objectList(data.biomesPayload.get("biomes"))) {
            for (Map<String, Object> spawn : objectList(biome.get("spawnTable"))) {
                require(resolvesReference(data.creatures, text(spawn.get("creature"))),
                        "biome " + text(biome.get("id")) + " references unknown creature "
                                + text(spawn.get("creature")));
            }
        }
        for (Map<String, Object> creature : objectList(data.creaturesPayload.get("creatures"))) {
            for (String biome : stringList(creature.get("biomes"))) {
                require(resolvesReference(data.biomes, biome),
                        "creature " + text(creature.get("id")) + " references unknown biome " + biome);
            }
        }
        for (Map<String, Object> landmark : objectList(data.structuresPayload.get("landmarks"))) {
            for (String biome : stringList(landmark.get("preferredBiomes"))) {
                require(resolvesReference(data.biomes, biome),
                        "landmark " + text(landmark.get("id")) + " references unknown biome " + biome);
            }
        }
        Set<String> lootCreatureIds = fieldValues(objectList(data.lootPayload.get("creatureDrops")), "creature");
        require(lootCreatureIds.containsAll(data.creatures),
                "creature loot should cover every Openlands creature");
        for (Map<String, Object> drop : objectList(data.lootPayload.get("blockDrops"))) {
            require(resolvesReference(data.blocks, text(drop.get("block"))),
                    "block loot references unknown block " + text(drop.get("block")));
        }
        for (String block : stringList(data.waystonesPayload.get("blocks"))) {
            require(resolvesReference(data.blocks, block),
                    "waystone contract references unknown block " + block);
        }
    }

    private static List<String> warnings(OpenlandsData data, Path openlandsRoot) {
        ArrayList<String> warnings = new ArrayList<>();
        if (text(data.assetManifest.get("status")).equals("placeholder_manifest_only")) {
            warnings.add("Openlands asset_manifest.json is still placeholder_manifest_only; final art/audio roots are not complete.");
        }
        Path runtimeAdapterPlan = openlandsRoot.resolve(
                "src/main/resources/data/" + MODULE_ID + "/openlands/systems/runtime_adapter_load_plan.json");
        if (!Files.isRegularFile(runtimeAdapterPlan)) {
            warnings.add("Openlands runtime_adapter_load_plan.json is not present yet; standalone smoke used Java contract/data files instead.");
        }
        for (String assetRoot : data.requiredAssetRoots) {
            Path path = openlandsRoot.resolve("src/main/resources/assets/" + MODULE_ID).resolve(assetRoot);
            if (!Files.exists(path)) {
                warnings.add("Openlands required asset root is missing: " + assetRoot);
            }
        }
        return List.copyOf(warnings);
    }

    private static void writeReport(
            Path standaloneRoot,
            Path openlandsRoot,
            Path assetsRoot,
            EchoAdapterCoreModuleCoverageReport coverage,
            EchoAdapterCoreModuleCoverageEntry openlands,
            OpenlandsData data,
            List<String> warnings
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/openlands-experience-contract.json");
        Files.createDirectories(report.getParent());
        boolean adapterCoreReady = openlands.status() == EchoAdapterCoreModuleCoverageStatus.ACTIVE
                && coverage.contractLockedForBeta()
                && openlands.nativeEntrypointDeclared();
        Map<String, Object> placeholderPolicy = object(data.assetManifest.get("placeholderPolicy"));
        String assetManifestStatus = text(data.assetManifest.get("status"));
        String replacementGate = text(placeholderPolicy.get("replacementGate"));
        String publicReleaseArtReviewStatus = text(data.assetManifest.get("publicReleaseArtReviewStatus"));
        if (publicReleaseArtReviewStatus.isBlank()) {
            publicReleaseArtReviewStatus = "PENDING_PUBLIC_ALPHA_ART_REVIEW";
        }
        boolean publicReleaseAllowedWithPlaceholders = Boolean.TRUE.equals(
                data.assetManifest.get("publicReleaseAllowedWithPlaceholders"));
        boolean publicReleaseAssetPolishReady = !publicReleaseAllowedWithPlaceholders
                && publicReleaseArtReviewStatus.equals("PASS")
                && List.of("final_public_release_art", "public_release_art_complete").contains(assetManifestStatus);
        boolean placeholderAssetPolicyAcceptedForBetaOnly = Boolean.TRUE.equals(
                placeholderPolicy.get("allowedBeforePublicAlpha"))
                && !publicReleaseAllowedWithPlaceholders
                && !publicReleaseAssetPolishReady;
        ArrayList<String> publicReleasePolishBlockers = new ArrayList<>();
        if (placeholderAssetPolicyAcceptedForBetaOnly) {
            publicReleasePolishBlockers.add(
                    "Openlands asset coverage is accepted only as beta placeholder coverage until the public alpha art review passes.");
        }
        if (!publicReleaseAssetPolishReady) {
            publicReleasePolishBlockers.add(
                    "Openlands final public art/content review is not recorded as PASS in asset_manifest.json.");
        }
        if (publicReleaseAllowedWithPlaceholders) {
            publicReleasePolishBlockers.add("Openlands placeholder assets must not be allowed for public release.");
        }
        String summary = warnings.isEmpty()
                ? (publicReleaseAssetPolishReady
                ? "Standalone accepts Openlands as a source-backed ECHO experience contract with public-release art/content polish evidence."
                : "Standalone accepts Openlands as a beta source-backed ECHO experience contract; public art/content polish remains blocked by beta-only placeholder coverage.")
                : "Standalone accepts Openlands as a source-backed ECHO experience contract; pack-side warnings remain.";
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.openlands_experience_contract.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"").append(adapterCoreReady ? "PASS" : "FAILED").append("\",\n");
        json.append("  \"summary\": \"").append(escape(summary)).append("\",\n");
        json.append("  \"betaExperienceContractReady\": ").append(adapterCoreReady).append(",\n");
        json.append("  \"publicReleaseContentPolishReady\": ").append(publicReleaseAssetPolishReady).append(",\n");
        json.append("  \"placeholderAssetPolicyAcceptedForBetaOnly\": ").append(placeholderAssetPolicyAcceptedForBetaOnly).append(",\n");
        json.append("  \"publicReleasePolishBlockers\": ").append(stringArray(publicReleasePolishBlockers)).append(",\n");
        json.append("  \"moduleId\": \"").append(MODULE_ID).append("\",\n");
        json.append("  \"moduleRoot\": \"").append(escape(standaloneRoot.relativize(openlandsRoot).toString().replace('\\', '/'))).append("\",\n");
        json.append("  \"adapterCore\": {\n");
        json.append("    \"coverageStatus\": \"").append(openlands.status().name().toLowerCase()).append("\",\n");
        json.append("    \"contractLockedForBeta\": ").append(coverage.contractLockedForBeta()).append(",\n");
        json.append("    \"contractRequiredForPass\": true,\n");
        json.append("    \"domains\": ").append(stringArray(openlands.adapterDomains().stream().map(EchoAdapterCoreDomain::id).toList())).append(",\n");
        json.append("    \"runtimes\": ").append(stringArray(openlands.adapterRuntimes().stream().map(EchoAdapterCoreRuntimeKind::adapterId).toList())).append(",\n");
        json.append("    \"nativeEntrypointDeclared\": ").append(openlands.nativeEntrypointDeclared()).append("\n");
        json.append("  },\n");
        json.append("  \"contentCounts\": {\n");
        json.append("    \"blocks\": ").append(data.blocks.size()).append(",\n");
        json.append("    \"items\": ").append(data.items.size()).append(",\n");
        json.append("    \"recipes\": ").append(data.recipes.size()).append(",\n");
        json.append("    \"biomes\": ").append(data.biomes.size()).append(",\n");
        json.append("    \"structures\": ").append(data.structures.size()).append(",\n");
        json.append("    \"creatures\": ").append(data.creatures.size()).append(",\n");
        json.append("    \"tutorialPrompts\": ").append(data.tutorials.size()).append(",\n");
        json.append("    \"playtestAcceptanceScenarios\": ").append(data.playtestScenarios.size()).append(",\n");
        json.append("    \"playtestSaveLoadCheckpoints\": ").append(data.playtestSaveLoadCheckpoints.size()).append(",\n");
        json.append("    \"firstHourSteps\": ").append(data.firstHourSteps.size()).append(",\n");
        json.append("    \"waystoneStates\": ").append(data.waystoneStates.size()).append("\n");
        json.append("  },\n");
        json.append("  \"runtimeTargets\": ").append(stringArray(data.runtimeTargets)).append(",\n");
        json.append("  \"firstHourSteps\": ").append(stringArray(data.firstHourSteps)).append(",\n");
        json.append("  \"playtestRequiredRouteSteps\": ").append(stringArray(data.requiredPlaytestRouteSteps)).append(",\n");
        json.append("  \"saveLoadAcceptance\": ").append(stringArray(data.saveLoadAcceptance)).append(",\n");
        json.append("  \"waystoneStates\": ").append(stringArray(data.waystoneStates)).append(",\n");
        Map<String, Object> assetCoverage = object(data.assetManifest.get("mvpCoverage"));
        json.append("  \"assetCoverage\": {\n");
        json.append("    \"status\": \"").append(escape(assetManifestStatus)).append("\",\n");
        json.append("    \"publicReleaseAllowedWithPlaceholders\": ").append(publicReleaseAllowedWithPlaceholders).append(",\n");
        json.append("    \"placeholderCoverageAcceptedForBetaOnly\": ").append(placeholderAssetPolicyAcceptedForBetaOnly).append(",\n");
        json.append("    \"publicReleaseArtReviewRequired\": ").append(!publicReleaseAssetPolishReady).append(",\n");
        json.append("    \"publicReleaseArtReviewStatus\": \"").append(escape(publicReleaseArtReviewStatus)).append("\",\n");
        json.append("    \"publicReleaseAssetPolishReady\": ").append(publicReleaseAssetPolishReady).append(",\n");
        json.append("    \"publicReleaseReady\": ").append(publicReleaseAssetPolishReady).append(",\n");
        json.append("    \"replacementGate\": \"").append(escape(replacementGate)).append("\",\n");
        json.append("    \"blocks\": ").append(stringList(assetCoverage.get("blockIds")).size()).append(",\n");
        json.append("    \"items\": ").append(stringList(assetCoverage.get("itemIds")).size()).append(",\n");
        json.append("    \"physicalBlockstates\": ").append(countFiles(assetsRoot.resolve("blockstates"), ".json")).append(",\n");
        json.append("    \"physicalBlockModels\": ").append(countFiles(assetsRoot.resolve("models/block"), ".json")).append(",\n");
        json.append("    \"physicalBlockTextures\": ").append(countFiles(assetsRoot.resolve("textures/block"), ".png")).append(",\n");
        json.append("    \"physicalItemModels\": ").append(countFiles(assetsRoot.resolve("models/item"), ".json")).append(",\n");
        json.append("    \"physicalItemTextures\": ").append(countFiles(assetsRoot.resolve("textures/item"), ".png")).append("\n");
        json.append("  },\n");
        json.append("  \"warnings\": ").append(stringArray(warnings)).append(",\n");
        json.append("  \"warningCount\": ").append(warnings.size()).append(",\n");
        json.append("  \"evidence\": {\n");
        json.append("    \"descriptorDiscovered\": true,\n");
        json.append("    \"adapterCoreActive\": ").append(adapterCoreReady).append(",\n");
        json.append("    \"neoforgeMetadataDiscovered\": true,\n");
        json.append("    \"runtimeParityDeclared\": true,\n");
        json.append("    \"conformanceIdsResolved\": true,\n");
        json.append("    \"firstHourRouteVisible\": true,\n");
        json.append("    \"playtestAcceptanceVisible\": true,\n");
        json.append("    \"waystoneStateMachineVisible\": true,\n");
        json.append("    \"publicReleasePlaceholdersRejected\": ").append(!publicReleaseAllowedWithPlaceholders).append(",\n");
        json.append("    \"publicReleaseArtReviewRequired\": ").append(!publicReleaseAssetPolishReady).append(",\n");
        json.append("    \"standardModeRelaxed\": true\n");
        json.append("  }\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
    }

    private static Map<String, Object> readJson(Path path) throws IOException {
        require(Files.isRegularFile(path), "Missing Openlands contract file: " + path);
        Object parsed = new Json(Files.readString(path)).parse();
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("JSON root must be an object: " + path);
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private static boolean isPng(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return false;
        }
        byte[] signature = Files.readAllBytes(path);
        return signature.length >= 8
                && (signature[0] & 0xff) == 137
                && signature[1] == 80
                && signature[2] == 78
                && signature[3] == 71
                && signature[4] == 13
                && signature[5] == 10
                && signature[6] == 26
                && signature[7] == 10;
    }

    private static long countFiles(Path directory, String suffix) throws IOException {
        if (!Files.isDirectory(directory)) {
            return 0;
        }
        try (var stream = Files.list(directory)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(suffix))
                    .count();
        }
    }

    private static Path echoModulesRoot(Path standaloneRoot) {
        String configured = System.getProperty("echo.modules.root");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("ECHO_MODULES_ROOT");
        }
        if (configured != null && !configured.isBlank()) {
            return Path.of(configured).toAbsolutePath().normalize();
        }
        Path workspaceRoot = standaloneRoot.getParent();
        if (workspaceRoot != null) {
            Path workspaceModules = workspaceRoot.resolve("ECHO-Modules/addons");
            if (Files.isDirectory(workspaceModules)) {
                return workspaceModules.toAbsolutePath().normalize();
            }
            Path legacyAddons = workspaceRoot.resolve("addons");
            if (Files.isDirectory(legacyAddons)) {
                return legacyAddons.toAbsolutePath().normalize();
            }
            return workspaceModules.toAbsolutePath().normalize();
        }
        return standaloneRoot.resolve("../ECHO-Modules/addons").toAbsolutePath().normalize();
    }

    private static List<Path> moduleRoots(Path standaloneRoot, Path modulesRoot) {
        ArrayList<Path> roots = new ArrayList<>();
        addIfDirectory(roots, modulesRoot);
        addIfDirectory(roots, standaloneRoot.resolve("src/main/resources"));
        return List.copyOf(roots);
    }

    private static void addIfDirectory(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path);
        }
    }

    private static Set<String> ids(List<Map<String, Object>> rows) {
        return fieldValues(rows, "id");
    }

    private static Set<String> combinedFirstHourSteps(OpenlandsData data) {
        TreeSet<String> steps = new TreeSet<>(data.firstHourSteps);
        steps.addAll(stringList(data.progressionPayload.get("foundationMovedSteps")));
        return Set.copyOf(steps);
    }

    private static boolean resolvesAllReferences(Set<String> localIds, List<String> references) {
        return references.stream().allMatch(reference -> resolvesReference(localIds, reference));
    }

    private static boolean resolvesReference(Set<String> localIds, String reference) {
        String text = text(reference);
        if (text.isBlank()) {
            return false;
        }
        if (isExternalEchoModuleReference(text)) {
            return true;
        }
        return localIds.contains(canonicalId(text));
    }

    private static boolean isExternalEchoModuleReference(String reference) {
        int separator = reference.indexOf(':');
        if (separator <= 0) {
            return false;
        }
        String namespace = reference.substring(0, separator);
        return !namespace.equals(MODULE_ID) && namespace.startsWith("echo");
    }

    private static Set<String> fieldValues(List<Map<String, Object>> rows, String fieldName) {
        TreeSet<String> ids = new TreeSet<>();
        for (Map<String, Object> row : rows) {
            String id = canonicalId(text(row.get(fieldName)));
            if (!id.isBlank()) {
                ids.add(id);
            }
        }
        return Set.copyOf(ids);
    }

    private static String canonicalId(String value) {
        String text = value == null ? "" : value.trim();
        int separator = text.indexOf(':');
        if (separator >= 0 && separator + 1 < text.length()) {
            return text.substring(separator + 1);
        }
        return text;
    }

    private static String localTextureKey(String value, String prefix) {
        String text = value == null ? "" : value.trim();
        String fullPrefix = prefix + "/";
        return text.startsWith(fullPrefix) ? text.substring(fullPrefix.length()) : text;
    }

    private static List<Map<String, Object>> objectList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> object = object(item);
            if (!object.isEmpty()) {
                result.add(object);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return java.util.Collections.unmodifiableMap(result);
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = text(item);
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return List.copyOf(result);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String stringArray(Set<String> values) {
        return stringArray(values.stream().sorted().toList());
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> escaped.append("\\\\");
                case '"' -> escaped.append("\\\"");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }

    private record OpenlandsData(
            Map<String, Object> conformance,
            Map<String, Object> blocksPayload,
            Map<String, Object> itemsPayload,
            Map<String, Object> recipesPayload,
            Map<String, Object> lootPayload,
            Map<String, Object> tagsPayload,
            Map<String, Object> biomesPayload,
            Map<String, Object> structuresPayload,
            Map<String, Object> creaturesPayload,
            Map<String, Object> waystonesPayload,
            Map<String, Object> progressionPayload,
            Map<String, Object> tutorialsPayload,
            Map<String, Object> playtestsPayload,
            Map<String, Object> holomapPayload,
            Map<String, Object> soundsPayload,
            Map<String, Object> gameModes,
            Map<String, Object> assetManifest,
            Set<String> blocks,
            Set<String> items,
            Set<String> recipes,
            Set<String> biomes,
            Set<String> structures,
            Set<String> creatures,
            Set<String> tutorials,
            List<String> requiredPlaytestRouteSteps,
            Set<String> playtestScenarios,
            Set<String> playtestSaveLoadCheckpoints,
            List<String> firstHourSteps,
            List<String> waystoneStates,
            List<String> saveLoadAcceptance,
            List<String> runtimeTargets,
            List<String> requiredContentRoots,
            List<String> requiredAssetRoots
    ) {
        List<Map<String, Object>> parityPayloads() {
            return List.of(
                    blocksPayload,
                    itemsPayload,
                    recipesPayload,
                    lootPayload,
                    biomesPayload,
                    structuresPayload,
                    creaturesPayload,
                    waystonesPayload,
                    progressionPayload,
                    tutorialsPayload,
                    playtestsPayload,
                    holomapPayload,
                    soundsPayload
            );
        }
    }

    private static final class Json {
        private static final Pattern CONTROL = Pattern.compile("[\\x00-\\x1F]");
        private final String text;
        private int index;

        private Json(String text) {
            this.text = stripBom(Objects.requireNonNull(text, "text"));
        }

        Object parse() {
            Object value = readValue();
            skipWhitespace();
            if (!end()) {
                throw error("Unexpected trailing JSON content");
            }
            return value;
        }

        private static String stripBom(String value) {
            return !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
        }

        private Object readValue() {
            skipWhitespace();
            if (end()) {
                throw error("Unexpected end of JSON");
            }
            char c = peek();
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) {
                        yield readNumber();
                    }
                    throw error("Unexpected JSON token: " + c);
                }
            };
        }

        private Map<String, Object> readObject() {
            expect('{');
            LinkedHashMap<String, Object> object = new LinkedHashMap<>();
            skipWhitespace();
            if (tryConsume('}')) {
                return object;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                object.put(key, readValue());
                skipWhitespace();
                if (tryConsume('}')) {
                    return object;
                }
                expect(',');
            }
        }

        private List<Object> readArray() {
            expect('[');
            ArrayList<Object> array = new ArrayList<>();
            skipWhitespace();
            if (tryConsume(']')) {
                return array;
            }
            while (true) {
                array.add(readValue());
                skipWhitespace();
                if (tryConsume(']')) {
                    return array;
                }
                expect(',');
            }
        }

        private String readString() {
            expect('"');
            StringBuilder builder = new StringBuilder();
            while (!end()) {
                char c = next();
                if (c == '"') {
                    String value = builder.toString();
                    if (CONTROL.matcher(value).find()) {
                        throw error("JSON string contains an unescaped control character");
                    }
                    return value;
                }
                if (c == '\\') {
                    if (end()) {
                        throw error("Unterminated escape sequence");
                    }
                    char escape = next();
                    switch (escape) {
                        case '"' -> builder.append('"');
                        case '\\' -> builder.append('\\');
                        case '/' -> builder.append('/');
                        case 'b' -> builder.append('\b');
                        case 'f' -> builder.append('\f');
                        case 'n' -> builder.append('\n');
                        case 'r' -> builder.append('\r');
                        case 't' -> builder.append('\t');
                        case 'u' -> builder.append(readUnicodeEscape());
                        default -> throw error("Unsupported escape sequence: \\" + escape);
                    }
                } else {
                    builder.append(c);
                }
            }
            throw error("Unterminated string");
        }

        private char readUnicodeEscape() {
            if (index + 4 > text.length()) {
                throw error("Incomplete unicode escape");
            }
            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object readNumber() {
            int start = index;
            if (peek() == '-') {
                index++;
            }
            while (!end() && Character.isDigit(peek())) {
                index++;
            }
            boolean decimal = false;
            if (!end() && peek() == '.') {
                decimal = true;
                index++;
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            if (!end() && (peek() == 'e' || peek() == 'E')) {
                decimal = true;
                index++;
                if (!end() && (peek() == '+' || peek() == '-')) {
                    index++;
                }
                while (!end() && Character.isDigit(peek())) {
                    index++;
                }
            }
            String value = text.substring(start, index);
            return decimal ? Double.parseDouble(value) : Long.parseLong(value);
        }

        private Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw error("Expected literal " + literal);
            }
            index += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (!end() && Character.isWhitespace(peek())) {
                index++;
            }
        }

        private boolean tryConsume(char expected) {
            if (!end() && peek() == expected) {
                index++;
                return true;
            }
            return false;
        }

        private void expect(char expected) {
            if (end() || next() != expected) {
                throw error("Expected '" + expected + "'");
            }
        }

        private char peek() {
            return text.charAt(index);
        }

        private char next() {
            return text.charAt(index++);
        }

        private boolean end() {
            return index >= text.length();
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " at index " + index);
        }
    }
}
