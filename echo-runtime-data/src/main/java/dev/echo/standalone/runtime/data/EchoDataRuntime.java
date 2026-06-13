package dev.echo.standalone.runtime.data;

import dev.echo.standalone.runtime.assets.EchoAssetEntry;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoDataRuntime {
    private static final List<String> WORLDGEN_STRUCTURE_RUNTIME_HINT_KEYS = List.of(
            "structureBlockId",
            "placementBlockId",
            "blockId",
            "liveVoxelId",
            "x",
            "originX",
            "y",
            "startY",
            "startHeight",
            "z",
            "originZ",
            "width",
            "height",
            "depth",
            "shape"
    );
    private static final List<String> WORLDGEN_FEATURE_RUNTIME_HINT_KEYS = List.of(
            "featureBlockId",
            "generationBlockId",
            "placementBlockId",
            "blockId",
            "liveVoxelId",
            "x",
            "centerX",
            "y",
            "fixedY",
            "z",
            "centerZ",
            "width",
            "height",
            "depth",
            "radius",
            "surfaceYOffset",
            "count",
            "chance",
            "density",
            "shape"
    );
    private static final List<String> WORLDGEN_BIOME_RUNTIME_HINT_KEYS = List.of(
            "biomeId",
            "displayName",
            "centerX",
            "centerZ",
            "x",
            "z",
            "radius",
            "temperature",
            "downfall",
            "humidity",
            "fogColor",
            "fog_color",
            "grassColor",
            "grass_color",
            "ambientParticle"
    );
    private static final List<String> WORLDCORE_REGION_RUNTIME_HINT_KEYS = List.of(
            "surfaceBlockId",
            "surfaceBlock",
            "regionBlockId",
            "generationBlockId",
            "blockId",
            "liveVoxelId",
            "centerX",
            "centerZ",
            "x",
            "z",
            "radius",
            "fixedY",
            "y",
            "surfaceYOffset"
    );
    private static final List<String> WORLDCORE_HAZARD_RUNTIME_HINT_KEYS = List.of(
            "hazardId",
            "hazardBlockId",
            "blockId",
            "liveVoxelId",
            "originX",
            "originY",
            "originZ",
            "x",
            "y",
            "z",
            "radius",
            "damagePerTick",
            "statusEffectId",
            "defaultSeverity",
            "exposurePerSecond",
            "exposure",
            "damage",
            "biomeTags",
            "hazardBiomeTags",
            "matchTags"
    );

    private final EchoDataFreezePolicy freezePolicy;

    public EchoDataRuntime() {
        this(EchoDataFreezePolicy.FREEZE_AFTER_LOAD);
    }

    public EchoDataRuntime(EchoDataFreezePolicy freezePolicy) {
        this.freezePolicy = Objects.requireNonNull(freezePolicy, "freezePolicy");
    }

    public EchoDataRuntimeResult load(
            EchoRuntimeServiceRegistry services,
            EchoAssetRuntimeResult assets
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(assets, "assets");

        List<EchoDataDocument> documents = loadDocuments(assets);
        EchoDataSchemaRegistry schemas = new EchoDataSchemaRegistry();
        EchoDataRegistryStore registries = new EchoDataRegistryStore();
        EchoDataTagRegistry tags = new EchoDataTagRegistry();
        EchoRecipeRegistry recipes = new EchoRecipeRegistry();
        EchoLootRegistry loot = new EchoLootRegistry();
        EchoMissionRegistry missions = new EchoMissionRegistry();
        EchoWorldgenStructureRegistry worldgenStructures = new EchoWorldgenStructureRegistry();
        EchoWorldgenBiomeRegistry worldgenBiomes = new EchoWorldgenBiomeRegistry();
        EchoWorldgenFeatureRegistry worldgenFeatures = new EchoWorldgenFeatureRegistry();
        EchoWorldCoreRegionRegistry worldCoreRegions = new EchoWorldCoreRegionRegistry();
        EchoWorldCoreHazardRegistry worldCoreHazards = new EchoWorldCoreHazardRegistry();
        EchoSoundRegistry sounds = new EchoSoundRegistry();

        for (EchoDataDocument document : documents) {
            switch (document.category()) {
                case "schemas" -> schemas.register(toSchema(document));
                case "registries" -> registries.register(toDefinition(document));
                case "tags" -> tags.register(toTag(document));
                case "recipes", "recipe" -> recipes.register(toRecipe(document));
                case "loot_tables", "loot_table", "loot_modifiers" -> loot.register(toLoot(document));
                case "missioncore" -> {
                    if (document.relativePath().startsWith("missioncore/missions/")) {
                        missions.register(toMission(document));
                    }
                }
                case "worldgen" -> {
                    if (document.relativePath().startsWith("worldgen/structure/")) {
                        worldgenStructures.register(toWorldgenStructure(document));
                    } else if (document.relativePath().startsWith("worldgen/biome/")) {
                        worldgenBiomes.register(toWorldgenBiome(document));
                    } else if (document.relativePath().startsWith("worldgen/configured_feature/")
                            || document.relativePath().startsWith("worldgen/placed_feature/")) {
                        worldgenFeatures.register(toWorldgenFeature(document));
                    }
                }
                case "echoworldcore" -> {
                    if (document.relativePath().startsWith("echoworldcore/world_regions/")) {
                        worldCoreRegions.register(toWorldCoreRegion(document));
                    } else if (document.relativePath().startsWith("echoworldcore/world_hazards/")) {
                        worldCoreHazards.register(toWorldCoreHazard(document));
                    }
                }
                case "sounds.json" -> toSounds(document).forEach(sounds::register);
                default -> {
                    // Unknown data categories stay in documents for later phases.
                }
            }
        }

        EchoDataValidationReport validation = validate(registries, schemas);
        EchoDataFreezeReport freeze = applyFreezePolicy(
                schemas,
                registries,
                tags,
                recipes,
                loot,
                missions,
                worldgenStructures,
                worldgenBiomes,
                worldgenFeatures,
                worldCoreRegions,
                worldCoreHazards,
                sounds
        );
        EchoDataRuntimeResult result = new EchoDataRuntimeResult(
                documents,
                schemas,
                registries,
                tags,
                recipes,
                loot,
                missions,
                worldgenStructures,
                worldgenBiomes,
                worldgenFeatures,
                worldCoreRegions,
                worldCoreHazards,
                sounds,
                validation,
                freeze
        );
        services.register(EchoDataRuntimeResult.class, result);
        services.register(EchoDataSchemaRegistry.class, schemas);
        services.register(EchoDataRegistryStore.class, registries);
        services.register(EchoDataTagRegistry.class, tags);
        services.register(EchoRecipeRegistry.class, recipes);
        services.register(EchoLootRegistry.class, loot);
        services.register(EchoMissionRegistry.class, missions);
        services.register(EchoWorldgenStructureRegistry.class, worldgenStructures);
        services.register(EchoWorldgenBiomeRegistry.class, worldgenBiomes);
        services.register(EchoWorldgenFeatureRegistry.class, worldgenFeatures);
        services.register(EchoWorldCoreRegionRegistry.class, worldCoreRegions);
        services.register(EchoWorldCoreHazardRegistry.class, worldCoreHazards);
        services.register(EchoSoundRegistry.class, sounds);
        services.register(EchoDataValidationReport.class, validation);
        return result;
    }

    private static List<EchoDataDocument> loadDocuments(EchoAssetRuntimeResult assets) throws IOException {
        LinkedHashSet<String> logicalIds = new LinkedHashSet<>();
        for (EchoAssetEntry entry : assets.index().entries()) {
            if ("data".equals(entry.sourceKind()) && entry.logicalId().endsWith(".json")
                    || "assets".equals(entry.sourceKind()) && entry.relativePath().equals("sounds.json")) {
                logicalIds.add(entry.logicalId());
            }
        }
        ArrayList<EchoDataDocument> documents = new ArrayList<>();
        for (String logicalId : logicalIds.stream().sorted().toList()) {
            EchoAssetEntry entry = assets.index().resolve(logicalId).orElseThrow();
            boolean supportedCategory = List.of("schemas", "tags", "recipes", "recipe", "loot_tables",
                            "loot_table", "loot_modifiers", "missioncore")
                    .contains(entry.category())
                    || "registries".equals(entry.category())
                    && supportedRegistryPath(entry.relativePath())
                    || "worldgen".equals(entry.category())
                    && supportedWorldgenPath(entry.relativePath())
                    || "echoworldcore".equals(entry.category())
                    && supportedWorldCorePath(entry.relativePath())
                    || "assets".equals(entry.sourceKind()) && entry.relativePath().equals("sounds.json");
            if (!supportedCategory) {
                continue;
            }
            String text = assets.resolver().loadText(logicalId).orElseThrow();
            documents.add(new EchoDataDocument(
                    logicalId,
                    entry.namespace().id(),
                    entry.category(),
                    entry.relativePath(),
                    entry.mount().source(),
                    EchoDataObjects.object(logicalId, text)
            ));
        }
        return documents.stream()
                .sorted(Comparator.comparing(EchoDataDocument::logicalId))
                .toList();
    }

    private static EchoDataSchema toSchema(EchoDataDocument document) {
        String schemaId = document.namespace() + ":" + EchoDataPaths.stripJsonSuffix(afterPrefix(document.relativePath(), "schemas/"));
        String registry = EchoDataObjects.string(
                document.object(),
                "registry",
                EchoDataPaths.stripJsonSuffix(afterPrefix(document.relativePath(), "schemas/"))
        );
        return new EchoDataSchema(
                schemaId,
                registry,
                EchoDataObjects.stringList(document.object(), "requiredFields"),
                document.logicalId()
        );
    }

    private static List<EchoSoundDefinition> toSounds(EchoDataDocument document) {
        ArrayList<EchoSoundDefinition> sounds = new ArrayList<>();
        for (Map.Entry<String, Object> entry : document.object().entrySet()) {
            if (entry.getValue() instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> definition = (Map<String, Object>) map;
                sounds.add(new EchoSoundDefinition(
                        document.namespace() + ":" + entry.getKey(),
                        EchoDataObjects.string(definition, "subtitle", ""),
                        soundAssets(definition),
                        document.logicalId()
                ));
            }
        }
        return sounds.stream()
                .sorted(Comparator.comparing(EchoSoundDefinition::id))
                .toList();
    }

    private static EchoWorldgenStructureDefinition toWorldgenStructure(EchoDataDocument document) {
        return new EchoWorldgenStructureDefinition(
                EchoDataPaths.logicalId(document.namespace(),
                        afterPrefix(document.relativePath(), "worldgen/structure/")),
                EchoDataObjects.string(document.object(), "type", "unknown"),
                worldgenStructureReferences(document.object()),
                worldgenStructureRuntimeHints(document.object()),
                document.logicalId()
        );
    }

    private static EchoWorldgenBiomeDefinition toWorldgenBiome(EchoDataDocument document) {
        String id = EchoDataPaths.logicalId(
                document.namespace(),
                afterPrefix(document.relativePath(), "worldgen/biome/")
        );
        Map<String, Object> object = document.object();
        int fogColor = colorValue(
                firstObject(
                        object.get("fogColor"),
                        object.get("fog_color"),
                        nestedValue(object, "effects", "fog_color")
                ),
                0x5F7741
        );
        int grassColor = colorValue(
                firstObject(
                        object.get("grassColor"),
                        object.get("grass_color"),
                        nestedValue(object, "effects", "grass_color"),
                        nestedValue(object, "effects", "foliage_color")
                ),
                fogColor
        );
        return new EchoWorldgenBiomeDefinition(
                id,
                firstTextValue(object.get("displayName"), object.get("display_name"), object.get("name"), idPath(id)),
                doubleValue(firstObject(object.get("temperature")), 0.8D),
                Math.max(0.0D, doubleValue(firstObject(object.get("downfall"), object.get("humidity")), 0.1D)),
                fogColor,
                grassColor,
                firstTextValue(
                        object.get("ambientParticle"),
                        object.get("ambient_particle"),
                        nestedValue(object, "effects", "ambient_particle", "options", "type"),
                        "minecraft:ash"
                ),
                worldgenBiomeTags(id, object),
                worldgenBiomeReferences(object),
                worldgenBiomeRuntimeHints(object),
                document.logicalId()
        );
    }

    private static EchoWorldgenFeatureDefinition toWorldgenFeature(EchoDataDocument document) {
        String featureKind = document.relativePath().startsWith("worldgen/configured_feature/")
                ? "configured_feature"
                : "placed_feature";
        String pathPrefix = "worldgen/" + featureKind + "/";
        String configuredFeature = configuredFeatureReference(featureKind, document.object());
        return new EchoWorldgenFeatureDefinition(
                EchoDataPaths.logicalId(document.namespace(), afterPrefix(document.relativePath(), pathPrefix)),
                featureKind,
                worldgenFeatureType(featureKind, document.object()),
                configuredFeature,
                placementModifiers(document.object()),
                worldgenFeatureReferences(configuredFeature, document.object()),
                worldgenFeatureRuntimeHints(document.object()),
                document.logicalId()
        );
    }

    private static EchoWorldCoreRegionDefinition toWorldCoreRegion(EchoDataDocument document) {
        Map<String, Object> object = document.object();
        String id = firstTextValue(
                object.get("id"),
                EchoDataPaths.logicalId(document.namespace(),
                        afterPrefix(document.relativePath(), "echoworldcore/world_regions/"))
        );
        List<String> biomeIds = EchoDataObjects.stringList(object, "biomeIds");
        List<String> biomeTags = EchoDataObjects.stringList(object, "biomeTags");
        List<String> structureIds = EchoDataObjects.stringList(object, "structureIds");
        List<String> hazardIds = worldCoreRegionHazardIds(id, object);
        return new EchoWorldCoreRegionDefinition(
                id,
                firstTextValue(object.get("type"), object.get("regionType")),
                firstTextValue(object.get("displayName"), object.get("display_name"), object.get("name"), idPath(id)),
                firstTextValue(object.get("summary"), object.get("description")),
                biomeIds,
                biomeTags,
                structureIds,
                hazardIds,
                firstTextValue(object.get("discoveryId"), object.get("discovery")),
                Math.max(0, positiveInt(object.get("radius"), 0)),
                firstTextValue(object.get("renderProfileId"), object.get("renderProfile")),
                firstTextValue(object.get("audioProfileId"), object.get("audioProfile")),
                intValue(object.get("sortOrder"), intValue(object.get("priority"), 0)),
                worldCoreRegionReferences(biomeIds, biomeTags, structureIds, hazardIds, object),
                worldCoreRegionRuntimeHints(object),
                document.logicalId()
        );
    }

    private static EchoWorldCoreHazardDefinition toWorldCoreHazard(EchoDataDocument document) {
        Map<String, Object> object = document.object();
        String id = firstTextValue(
                object.get("id"),
                EchoDataPaths.logicalId(document.namespace(),
                        afterPrefix(document.relativePath(), "echoworldcore/world_hazards/"))
        );
        return new EchoWorldCoreHazardDefinition(
                id,
                firstTextValue(object.get("type"), object.get("hazardType")),
                firstTextValue(object.get("displayName"), object.get("display_name"), object.get("name"), idPath(id)),
                firstTextValue(object.get("summary"), object.get("description")),
                intValue(object.get("defaultSeverity"), intValue(object.get("severity"), 0)),
                booleanValue(object.get("ticking"), false),
                worldCoreHazardReferences(object),
                worldCoreHazardRuntimeHints(object),
                document.logicalId()
        );
    }

    private static EchoDataDefinition toDefinition(EchoDataDocument document) {
        String tail = afterPrefix(document.relativePath(), "registries/");
        String registry = firstSegment(tail);
        String idPath = afterPrefix(tail, registry + "/");
        return new EchoDataDefinition(
                EchoDataPaths.logicalId(document.namespace(), idPath),
                registry,
                document.logicalId(),
                document.object()
        );
    }

    private static EchoDataTag toTag(EchoDataDocument document) {
        String tail = afterPrefix(document.relativePath(), "tags/");
        String registry = firstSegment(tail);
        String tagPath = afterPrefix(tail, registry + "/");
        return new EchoDataTag(
                EchoDataPaths.logicalId(document.namespace(), tagPath),
                registry,
                EchoDataObjects.stringList(document.object(), "values"),
                document.logicalId()
        );
    }

    private static EchoRecipeDefinition toRecipe(EchoDataDocument document) {
        String idPath = afterPrefix(document.relativePath(), recipePrefix(document.relativePath()));
        Map<String, Integer> ingredientCounts = recipeIngredientCounts(document.object());
        List<String> ingredients = ingredientCounts.isEmpty()
                ? recipeIngredients(document.object())
                : ingredientCounts.keySet().stream().sorted().toList();
        RecipeResult result = recipeResult(document.object());
        return new EchoRecipeDefinition(
                EchoDataPaths.logicalId(document.namespace(), idPath),
                EchoDataObjects.string(document.object(), "type", "unknown"),
                ingredients,
                ingredientCounts,
                result.id(),
                result.count(),
                recipePattern(document.object()),
                EchoDataObjects.string(document.object(), "group", ""),
                EchoDataObjects.string(document.object(), "category", ""),
                document.logicalId()
        );
    }

    private static EchoLootDefinition toLoot(EchoDataDocument document) {
        String idPath = afterPrefix(document.relativePath(), lootPrefix(document.relativePath()));
        return new EchoLootDefinition(
                EchoDataPaths.logicalId(document.namespace(), idPath),
                lootEntries(document.object()),
                document.logicalId()
        );
    }

    private static EchoMissionDefinition toMission(EchoDataDocument document) {
        return new EchoMissionDefinition(
                EchoDataObjects.string(document.object(), "id",
                        EchoDataPaths.logicalId(document.namespace(),
                                afterPrefix(document.relativePath(), "missioncore/missions/"))),
                EchoDataObjects.string(document.object(), "chapterId", "missing:chapter"),
                EchoDataObjects.string(document.object(), "title", "Untitled Mission"),
                missionObjectives(document.object()),
                missionReferences(document.object()),
                document.logicalId()
        );
    }

    private static String recipePrefix(String relativePath) {
        return relativePath.startsWith("recipes/") ? "recipes/" : "recipe/";
    }

    private static String lootPrefix(String relativePath) {
        if (relativePath.startsWith("loot_tables/")) {
            return "loot_tables/";
        }
        if (relativePath.startsWith("loot_modifiers/")) {
            return "loot_modifiers/";
        }
        return "loot_table/";
    }

    private static List<String> missionObjectives(Map<String, Object> object) {
        LinkedHashSet<String> objectives = new LinkedHashSet<>();
        addMissionObjectiveValue(objectives, object.get("objectives"));
        return objectives.stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addMissionObjectiveValue(LinkedHashSet<String> objectives, Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addMissionObjectiveValue(objectives, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id != null) {
                objectives.add(String.valueOf(id));
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                    addMissionObjectiveValue(objectives, nested);
                }
            }
        }
    }

    private static List<String> missionReferences(Map<String, Object> object) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static List<String> worldgenStructureReferences(Map<String, Object> object) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static Map<String, String> worldgenStructureRuntimeHints(Map<String, Object> object) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        for (String key : WORLDGEN_STRUCTURE_RUNTIME_HINT_KEYS) {
            addRuntimeHint(hints, key, object.get(key));
        }
        addRuntimeHint(hints, "startY", nestedValue(object, "start_height", "absolute"));
        return Map.copyOf(hints);
    }

    private static List<String> worldgenBiomeReferences(Map<String, Object> object) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static List<String> worldgenBiomeTags(String biomeId, Map<String, Object> object) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.addAll(EchoDataObjects.stringList(object, "tags"));
        tags.addAll(EchoDataObjects.stringList(object, "biomeTags"));
        String path = idPath(biomeId).toLowerCase(java.util.Locale.ROOT);
        for (String token : List.of("toxic", "radiation", "industrial", "city", "cold", "nexus", "anomaly")) {
            if (path.contains(token)) {
                tags.add(token);
            }
        }
        return tags.stream().sorted().toList();
    }

    private static Map<String, String> worldgenBiomeRuntimeHints(Map<String, Object> object) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        for (String key : WORLDGEN_BIOME_RUNTIME_HINT_KEYS) {
            addRuntimeHint(hints, key, object.get(key));
        }
        addRuntimeHint(hints, "fogColor", firstObject(
                object.get("fogColor"),
                object.get("fog_color"),
                nestedValue(object, "effects", "fog_color")
        ));
        addRuntimeHint(hints, "grassColor", firstObject(
                object.get("grassColor"),
                object.get("grass_color"),
                nestedValue(object, "effects", "grass_color"),
                nestedValue(object, "effects", "foliage_color")
        ));
        addRuntimeHint(hints, "ambientParticle", firstObject(
                object.get("ambientParticle"),
                object.get("ambient_particle"),
                nestedValue(object, "effects", "ambient_particle", "options", "type")
        ));
        return Map.copyOf(hints);
    }

    private static List<String> worldgenFeatureReferences(String configuredFeature, Map<String, Object> object) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        if (configuredFeature != null && !configuredFeature.isBlank()) {
            references.add(configuredFeature);
        }
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static Map<String, String> worldgenFeatureRuntimeHints(Map<String, Object> object) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        for (String key : WORLDGEN_FEATURE_RUNTIME_HINT_KEYS) {
            addRuntimeHint(hints, key, object.get(key));
        }
        addPlacementRuntimeHints(hints, object.get("placement"));
        addPlacementRuntimeHints(hints, object.get("placements"));
        return Map.copyOf(hints);
    }

    private static List<String> worldCoreRegionHazardIds(String regionId, Map<String, Object> object) {
        LinkedHashSet<String> hazardIds = new LinkedHashSet<>();
        hazardIds.addAll(EchoDataObjects.stringList(object, "hazardIds"));
        hazardIds.addAll(EchoDataObjects.stringList(object, "hazards"));
        hazardIds.addAll(EchoDataObjects.stringList(object, "worldHazards"));
        addWorldCoreHazardReference(hazardIds, object.get("hazard"));
        String path = idPath(regionId).toLowerCase(java.util.Locale.ROOT);
        if (hazardIds.isEmpty() && path.contains("toxic")) {
            hazardIds.add("echoworldcore:hazard/toxic_air");
        }
        return hazardIds.stream().sorted().toList();
    }

    private static List<String> worldCoreRegionReferences(
            List<String> biomeIds,
            List<String> biomeTags,
            List<String> structureIds,
            List<String> hazardIds,
            Map<String, Object> object
    ) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        references.addAll(biomeIds);
        references.addAll(biomeTags);
        references.addAll(structureIds);
        references.addAll(hazardIds);
        addReferenceIfPresent(references, object.get("discoveryId"));
        addReferenceIfPresent(references, object.get("renderProfileId"));
        addReferenceIfPresent(references, object.get("audioProfileId"));
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static List<String> worldCoreHazardReferences(Map<String, Object> object) {
        LinkedHashSet<String> references = new LinkedHashSet<>();
        addReferenceIfPresent(references, object.get("statusEffectId"));
        addReferenceIfPresent(references, object.get("effectId"));
        addReferenceIfPresent(references, object.get("damageSourceId"));
        addMissionReferenceValue(references, object);
        return references.stream().sorted().toList();
    }

    private static Map<String, String> worldCoreRegionRuntimeHints(Map<String, Object> object) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        for (String key : WORLDCORE_REGION_RUNTIME_HINT_KEYS) {
            addRuntimeHint(hints, key, object.get(key));
        }
        addRuntimeHintIfAbsent(hints, "surfaceBlockId", firstObject(
                object.get("surfaceBlock"),
                nestedValue(object, "runtime", "surfaceBlockId"),
                nestedValue(object, "runtime", "surfaceBlock"),
                nestedValue(object, "standaloneRuntime", "surfaceBlockId")
        ));
        addRuntimeHintIfAbsent(hints, "centerX", firstObject(
                nestedValue(object, "center", "x"),
                nestedValue(object, "origin", "x"),
                nestedValue(object, "runtime", "centerX")
        ));
        addRuntimeHintIfAbsent(hints, "centerZ", firstObject(
                nestedValue(object, "center", "z"),
                nestedValue(object, "origin", "z"),
                nestedValue(object, "runtime", "centerZ")
        ));
        addRuntimeHintIfAbsent(hints, "fixedY", firstObject(
                nestedValue(object, "center", "y"),
                nestedValue(object, "origin", "y"),
                nestedValue(object, "runtime", "fixedY")
        ));
        return Map.copyOf(hints);
    }

    private static Map<String, String> worldCoreHazardRuntimeHints(Map<String, Object> object) {
        LinkedHashMap<String, String> hints = new LinkedHashMap<>();
        for (String key : WORLDCORE_HAZARD_RUNTIME_HINT_KEYS) {
            addRuntimeHint(hints, key, object.get(key));
        }
        addRuntimeHintIfAbsent(hints, "statusEffectId", firstObject(
                object.get("statusEffectId"),
                object.get("effectId"),
                nestedValue(object, "statusEffect", "id")
        ));
        addRuntimeHintIfAbsent(hints, "originX", firstObject(
                nestedValue(object, "origin", "x"),
                nestedValue(object, "center", "x")
        ));
        addRuntimeHintIfAbsent(hints, "originY", firstObject(
                nestedValue(object, "origin", "y"),
                nestedValue(object, "center", "y")
        ));
        addRuntimeHintIfAbsent(hints, "originZ", firstObject(
                nestedValue(object, "origin", "z"),
                nestedValue(object, "center", "z")
        ));
        return Map.copyOf(hints);
    }

    @SuppressWarnings("unchecked")
    private static void addWorldCoreHazardReference(LinkedHashSet<String> references, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            if (!text.isBlank()) {
                references.add(text);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addWorldCoreHazardReference(references, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("hazardId");
            }
            if (id != null && !String.valueOf(id).isBlank()) {
                references.add(String.valueOf(id));
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                    addWorldCoreHazardReference(references, nested);
                }
            }
        }
    }

    private static void addReferenceIfPresent(LinkedHashSet<String> references, Object value) {
        String text = runtimeHintText(value);
        if (!text.isBlank() && text.contains(":")) {
            references.add(text);
        }
    }

    private static String worldgenFeatureType(String featureKind, Map<String, Object> object) {
        return EchoDataObjects.string(
                object,
                "type",
                "placed_feature".equals(featureKind) ? "minecraft:placed_feature" : "unknown"
        );
    }

    private static String configuredFeatureReference(String featureKind, Map<String, Object> object) {
        if (!"placed_feature".equals(featureKind)) {
            return "";
        }
        Object feature = object.get("feature");
        if (feature instanceof String text) {
            return text.trim();
        }
        if (feature instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("feature");
            }
            if (id != null) {
                return String.valueOf(id).trim();
            }
        }
        return "";
    }

    private static List<String> placementModifiers(Map<String, Object> object) {
        LinkedHashSet<String> modifiers = new LinkedHashSet<>();
        addPlacementModifierValue(modifiers, object.get("placement"));
        addPlacementModifierValue(modifiers, object.get("placements"));
        return modifiers.stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addPlacementModifierValue(LinkedHashSet<String> modifiers, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            if (!text.isBlank()) {
                modifiers.add(text);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addPlacementModifierValue(modifiers, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object type = map.get("type");
            if (type != null && !String.valueOf(type).isBlank()) {
                modifiers.add(String.valueOf(type));
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                    addPlacementModifierValue(modifiers, nested);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addPlacementRuntimeHints(LinkedHashMap<String, String> hints, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addPlacementRuntimeHints(hints, item);
            }
            return;
        }
        if (!(value instanceof Map<?, ?> map)) {
            return;
        }
        for (String key : WORLDGEN_FEATURE_RUNTIME_HINT_KEYS) {
            hints.putIfAbsent(key, runtimeHintText(map.get(key)));
            if (hints.get(key) != null && hints.get(key).isBlank()) {
                hints.remove(key);
            }
        }
        String type = runtimeHintText(map.get("type")).toLowerCase(java.util.Locale.ROOT);
        if (type.endsWith(":count") || type.equals("count")) {
            addRuntimeHintIfAbsent(hints, "count", map.get("count"));
        } else if (type.endsWith(":rarity_filter") || type.equals("rarity_filter")) {
            double chance = positiveDouble(map.get("chance"), 0.0D);
            if (chance > 0.0D) {
                addRuntimeHintIfAbsent(hints, "chance", Double.toString(1.0D / chance));
            }
        }
        for (Object nested : ((Map<String, Object>) map).values()) {
            if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                addPlacementRuntimeHints(hints, nested);
            }
        }
    }

    private static void addRuntimeHint(LinkedHashMap<String, String> hints, String key, Object value) {
        String text = runtimeHintText(value);
        if (!text.isBlank()) {
            hints.put(key, text);
        }
    }

    private static void addRuntimeHintIfAbsent(LinkedHashMap<String, String> hints, String key, Object value) {
        if (hints.containsKey(key)) {
            return;
        }
        String text = runtimeHintText(value);
        if (!text.isBlank()) {
            hints.put(key, text);
        }
    }

    private static String runtimeHintText(Object value) {
        if (value instanceof String text) {
            return text.trim();
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value).trim();
        }
        return "";
    }

    private static Object firstObject(Object... values) {
        if (values == null) {
            return null;
        }
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = runtimeHintText(value);
            if (!text.isBlank() || value instanceof Map<?, ?> || value instanceof List<?>) {
                return value;
            }
        }
        return null;
    }

    private static String firstTextValue(Object... values) {
        String text = runtimeHintText(firstObject(values));
        return text == null ? "" : text;
    }

    private static double doubleValue(Object value, double fallback) {
        if (value instanceof Number number) {
            double parsed = number.doubleValue();
            return Double.isFinite(parsed) ? parsed : fallback;
        }
        String text = runtimeHintText(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            double parsed = Double.parseDouble(text);
            return Double.isFinite(parsed) ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int intValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = runtimeHintText(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = runtimeHintText(value);
        if (text.isBlank()) {
            return fallback;
        }
        return Boolean.parseBoolean(text);
    }

    private static int colorValue(Object value, int fallback) {
        if (value instanceof Number number) {
            return number.intValue() & 0xFFFFFF;
        }
        String text = runtimeHintText(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            if (text.startsWith("#")) {
                return Integer.parseUnsignedInt(text.substring(1), 16) & 0xFFFFFF;
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return Integer.parseUnsignedInt(text.substring(2), 16) & 0xFFFFFF;
            }
            return Integer.parseInt(text) & 0xFFFFFF;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    @SuppressWarnings("unchecked")
    private static Object nestedValue(Map<String, Object> root, String... keys) {
        if (root == null || keys == null || keys.length == 0) {
            return null;
        }
        Object value = root;
        for (String key : keys) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = ((Map<String, Object>) map).get(key);
        }
        return value;
    }

    private static String idPath(String id) {
        String text = runtimeHintText(id);
        int separator = text.indexOf(':');
        return separator >= 0 ? text.substring(separator + 1) : text;
    }

    private static double positiveDouble(Object value, double fallback) {
        String text = runtimeHintText(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            double parsed = Double.parseDouble(text);
            return parsed > 0.0D && Double.isFinite(parsed) ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static List<String> soundAssets(Map<String, Object> object) {
        LinkedHashSet<String> assets = new LinkedHashSet<>();
        addSoundAssetValue(assets, object.get("sounds"));
        return assets.stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addSoundAssetValue(LinkedHashSet<String> assets, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            assets.add(text);
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addSoundAssetValue(assets, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object name = map.get("name");
            if (name != null) {
                assets.add(String.valueOf(name));
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                    addSoundAssetValue(assets, nested);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void addMissionReferenceValue(LinkedHashSet<String> references, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            if (text.contains(":")) {
                references.add(text);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addMissionReferenceValue(references, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            for (Object nested : ((Map<String, Object>) map).values()) {
                addMissionReferenceValue(references, nested);
            }
        }
    }

    private static List<String> lootEntries(Map<String, Object> object) {
        LinkedHashSet<String> entries = new LinkedHashSet<>(EchoDataObjects.stringList(object, "entries"));
        addLootValue(entries, object);
        return entries.stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addLootValue(LinkedHashSet<String> entries, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            if (text.contains(":")) {
                entries.add(text);
            }
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addLootValue(entries, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            addLootValue(entries, map.get("name"));
            addLootValue(entries, map.get("item"));
            addLootValue(entries, map.get("id"));
            addLootValue(entries, map.get("loot_table_id"));
            addLootValue(entries, map.get("guideBookId"));
            for (Object nested : ((Map<String, Object>) map).values()) {
                if (nested instanceof Map<?, ?> || nested instanceof List<?>) {
                    addLootValue(entries, nested);
                }
            }
        }
    }

    private static List<String> recipeIngredients(Map<String, Object> object) {
        LinkedHashSet<String> ingredients = new LinkedHashSet<>();
        addIngredientValue(ingredients, object.get("ingredient"));
        addIngredientValue(ingredients, object.get("ingredients"));
        addIngredientValue(ingredients, object.get("key"));
        return ingredients.stream().sorted().toList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Integer> recipeIngredientCounts(Map<String, Object> object) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        Object pattern = object.get("pattern");
        Object key = object.get("key");
        if (pattern instanceof List<?> rows && key instanceof Map<?, ?> keyMap) {
            Map<String, Object> keys = (Map<String, Object>) keyMap;
            for (Object rowValue : rows) {
                String row = String.valueOf(rowValue);
                for (int i = 0; i < row.length(); i++) {
                    char symbol = row.charAt(i);
                    if (!Character.isWhitespace(symbol)) {
                        addIngredientCount(counts, keys.get(String.valueOf(symbol)), 1);
                    }
                }
            }
        }
        if (counts.isEmpty()) {
            addIngredientListCounts(counts, object.get("ingredients"));
            addIngredientCount(counts, object.get("ingredient"), 1);
        }
        if (counts.isEmpty()) {
            for (String ingredient : recipeIngredients(object)) {
                counts.merge(ingredient, 1, Integer::sum);
            }
        }
        return counts;
    }

    private static void addIngredientListCounts(LinkedHashMap<String, Integer> counts, Object value) {
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addIngredientCount(counts, item, 1);
            }
            return;
        }
        addIngredientCount(counts, value, 1);
    }

    private static void addIngredientCount(LinkedHashMap<String, Integer> counts, Object value, int amount) {
        for (String ingredient : ingredientIds(value)) {
            counts.merge(ingredient, amount, Integer::sum);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> ingredientIds(Object value) {
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        if (value == null) {
            return List.of();
        }
        if (value instanceof String text) {
            if (!text.isBlank()) {
                ids.add(text);
            }
            return ids.stream().toList();
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                ids.addAll(ingredientIds(item));
            }
            return ids.stream().toList();
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("item");
            }
            if (id == null) {
                id = tagId(map.get("tag"));
            }
            if (id != null) {
                ids.add(String.valueOf(id));
                return ids.stream().toList();
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                ids.addAll(ingredientIds(nested));
            }
        }
        return ids.stream().toList();
    }

    @SuppressWarnings("unchecked")
    private static void addIngredientValue(LinkedHashSet<String> ingredients, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String text) {
            ingredients.add(text);
            return;
        }
        if (value instanceof List<?> list) {
            for (Object item : list) {
                addIngredientValue(ingredients, item);
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("item");
            }
            if (id == null) {
                id = tagId(map.get("tag"));
            }
            if (id != null) {
                ingredients.add(String.valueOf(id));
            }
            for (Object nested : ((Map<String, Object>) map).values()) {
                addIngredientValue(ingredients, nested);
            }
        }
    }

    private static RecipeResult recipeResult(Map<String, Object> object) {
        Object result = object.get("result");
        if (result instanceof String text) {
            return new RecipeResult(text, 1);
        }
        if (result instanceof Map<?, ?> map) {
            Object id = map.get("id");
            if (id == null) {
                id = map.get("item");
            }
            if (id != null) {
                return new RecipeResult(String.valueOf(id), positiveInt(map.get("count"), 1));
            }
        }
        return new RecipeResult("missing:result", 1);
    }

    private static String tagId(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return text.startsWith("#") ? text : "#" + text;
    }

    private static int positiveInt(Object value, int fallback) {
        if (value instanceof Number number) {
            int result = number.intValue();
            return result > 0 ? result : fallback;
        }
        if (value instanceof String text) {
            try {
                int result = Integer.parseInt(text);
                return result > 0 ? result : fallback;
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
        return fallback;
    }

    private static List<String> recipePattern(Map<String, Object> object) {
        Object pattern = object.get("pattern");
        if (!(pattern instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<String> rows = new ArrayList<>();
        for (Object row : list) {
            rows.add(String.valueOf(row));
        }
        return rows;
    }

    private static EchoDataValidationReport validate(EchoDataRegistryStore registries, EchoDataSchemaRegistry schemas) {
        ArrayList<EchoDataValidationIssue> issues = new ArrayList<>();
        for (EchoDataRegistry registry : registries.registries()) {
            schemas.schemaFor(registry.registryId()).ifPresent(schema -> {
                for (EchoDataDefinition definition : registry.entries()) {
                    for (String requiredField : schema.requiredFields()) {
                        if (!definition.fields().containsKey(requiredField)) {
                            issues.add(new EchoDataValidationIssue(
                                    EchoDataValidationSeverity.ERROR,
                                    "MISSING_REQUIRED_FIELD",
                                    definition.id(),
                                    "Missing required field " + requiredField + " for registry " + registry.registryId()
                            ));
                        }
                    }
                }
            });
        }
        return new EchoDataValidationReport(issues);
    }

    private EchoDataFreezeReport applyFreezePolicy(
            EchoDataSchemaRegistry schemas,
            EchoDataRegistryStore registries,
            EchoDataTagRegistry tags,
            EchoRecipeRegistry recipes,
            EchoLootRegistry loot,
            EchoMissionRegistry missions,
            EchoWorldgenStructureRegistry worldgenStructures,
            EchoWorldgenBiomeRegistry worldgenBiomes,
            EchoWorldgenFeatureRegistry worldgenFeatures,
            EchoWorldCoreRegionRegistry worldCoreRegions,
            EchoWorldCoreHazardRegistry worldCoreHazards,
            EchoSoundRegistry sounds
    ) {
        if (freezePolicy == EchoDataFreezePolicy.FREEZE_AFTER_LOAD) {
            schemas.freeze();
            registries.freezeAll();
            tags.freeze();
            recipes.freeze();
            loot.freeze();
            missions.freeze();
            worldgenStructures.freeze();
            worldgenBiomes.freeze();
            worldgenFeatures.freeze();
            worldCoreRegions.freeze();
            worldCoreHazards.freeze();
            sounds.freeze();
            return new EchoDataFreezeReport(freezePolicy, true, registries.registries().size(), 11);
        }
        return new EchoDataFreezeReport(freezePolicy, false, 0, 0);
    }

    private static boolean supportedWorldgenPath(String relativePath) {
        return relativePath.startsWith("worldgen/structure/")
                || relativePath.startsWith("worldgen/biome/")
                || relativePath.startsWith("worldgen/configured_feature/")
                || relativePath.startsWith("worldgen/placed_feature/");
    }

    private static boolean supportedRegistryPath(String relativePath) {
        if (!relativePath.startsWith("registries/")) {
            return false;
        }
        String tail = relativePath.substring("registries/".length());
        int slash = tail.indexOf('/');
        return slash > 0 && slash + 1 < tail.length();
    }

    private static boolean supportedWorldCorePath(String relativePath) {
        return relativePath.startsWith("echoworldcore/world_regions/")
                || relativePath.startsWith("echoworldcore/world_hazards/");
    }

    private static String firstSegment(String path) {
        String normalized = EchoDataPaths.requireText(path, "path");
        int slash = normalized.indexOf('/');
        if (slash < 0) {
            return normalized;
        }
        return normalized.substring(0, slash);
    }

    private static String afterPrefix(String value, String prefix) {
        String normalized = EchoDataPaths.requireText(value, "value");
        if (!normalized.startsWith(prefix)) {
            throw new IllegalArgumentException("Expected path prefix " + prefix + " in " + value);
        }
        return normalized.substring(prefix.length());
    }

    private record RecipeResult(String id, int count) {
    }
}
