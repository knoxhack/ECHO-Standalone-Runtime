package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemDefinitionInference;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;
import dev.echo.standalone.runtime.world.EchoVoxelMaterialPattern;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EchoAdapterCoreNativeContentRegistrations {
    private EchoAdapterCoreNativeContentRegistrations() {
    }

    public static List<EchoAdapterCoreRegistryEntry> entriesFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoAdapterCoreRegistryEntry> entries = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            entries.add(entryFromRow(row));
        }
        return List.copyOf(entries);
    }

    public static List<EchoItemDefinition> itemDefinitionsFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoItemDefinition> definitions = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (isItemRow(row)) {
                definitions.add(itemDefinitionFromRow(row));
            }
        }
        return List.copyOf(definitions);
    }

    public static EchoItemDefinition itemDefinitionFromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : Map.copyOf(row);
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = text(safeRow.get("contentId"));
        String standaloneRuntimeId = firstText(
                safeRow.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        String displayName = firstText(safeRow.get("displayName"), metadata.get("displayName"), displayName(contentId));
        EchoItemCategory category = itemCategory(firstText(safeRow.get("category"), metadata.get("category")), standaloneRuntimeId);
        int maxStackSize = Math.max(1, intValue(firstText(
                safeRow.get("maxStackSize"),
                metadata.get("maxStackSize"),
                metadata.get("stackSize")
        ), EchoItemDefinitionInference.DEFAULT_MAX_STACK_SIZE));
        double weight = doubleValue(firstText(safeRow.get("weight"), metadata.get("weight")), 1.0D);
        List<String> tags = textList(safeRow.get("tags"), metadata.get("tags"));
        if (tags.isEmpty()) {
            tags = List.of("adaptercore", moduleFromContentId(contentId), "native-content");
        }
        List<String> tooltip = textList(safeRow.get("tooltipLines"), metadata.get("tooltipLines"), metadata.get("tooltip"));
        if (tooltip.isEmpty()) {
            tooltip = List.of("Registered through AdapterCore native content");
        }
        return new EchoItemDefinition(
                new EchoItemId(standaloneRuntimeId),
                displayName,
                category,
                maxStackSize,
                weight,
                tags,
                tooltip
        );
    }

    public static List<EchoRecipeDefinition> recipeDefinitionsFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoRecipeDefinition> definitions = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (isRecipeRow(row)) {
                definitions.add(recipeDefinitionFromRow(row));
            }
        }
        return List.copyOf(definitions);
    }

    public static EchoRecipeDefinition recipeDefinitionFromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : Map.copyOf(row);
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = text(safeRow.get("contentId"));
        String recipeId = firstText(
                safeRow.get("recipeId"),
                metadata.get("recipeId"),
                safeRow.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        String type = firstText(
                safeRow.get("type"),
                metadata.get("type"),
                metadata.get("recipeType"),
                "minecraft:crafting_shapeless"
        );
        List<String> ingredients = textList(
                safeRow.get("ingredients"),
                metadata.get("ingredients"),
                metadata.get("ingredientIds")
        );
        Map<String, Integer> ingredientCounts = itemCountMap(
                safeRow.get("ingredientCounts"),
                metadata.get("ingredientCounts"),
                safeRow.get("counts"),
                metadata.get("counts")
        );
        if (ingredients.isEmpty() && !ingredientCounts.isEmpty()) {
            ingredients = List.copyOf(ingredientCounts.keySet());
        }
        if (ingredientCounts.isEmpty()) {
            ingredientCounts = countTextList(ingredients);
        }
        String result = firstText(
                safeRow.get("result"),
                metadata.get("result"),
                safeRow.get("resultItem"),
                metadata.get("resultItem"),
                metadata.get("output")
        );
        int resultCount = Math.max(1, intValue(firstText(
                safeRow.get("resultCount"),
                metadata.get("resultCount"),
                metadata.get("outputCount")
        ), 1));
        List<String> pattern = textList(safeRow.get("pattern"), metadata.get("pattern"));
        String group = firstText(safeRow.get("group"), metadata.get("group"));
        String category = firstText(safeRow.get("category"), metadata.get("category"));
        String sourceLogicalId = firstText(
                safeRow.get("sourceLogicalId"),
                metadata.get("sourceLogicalId"),
                safeRow.get("nativeLoaderId"),
                metadata.get("nativeLoaderId"),
                contentId
        );
        return new EchoRecipeDefinition(
                recipeId,
                type,
                ingredients,
                ingredientCounts,
                result,
                resultCount,
                pattern,
                group,
                category,
                sourceLogicalId
        );
    }

    public static List<EchoLootDefinition> lootDefinitionsFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoLootDefinition> definitions = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (isLootRow(row)) {
                definitions.add(lootDefinitionFromRow(row));
            }
        }
        return List.copyOf(definitions);
    }

    public static EchoLootDefinition lootDefinitionFromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : Map.copyOf(row);
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = text(safeRow.get("contentId"));
        String tableId = firstText(
                safeRow.get("lootTableId"),
                metadata.get("lootTableId"),
                safeRow.get("tableId"),
                metadata.get("tableId"),
                safeRow.get("lootId"),
                metadata.get("lootId"),
                safeRow.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        List<String> entries = textList(
                safeRow.get("entries"),
                metadata.get("entries"),
                safeRow.get("items"),
                metadata.get("items"),
                safeRow.get("lootEntries"),
                metadata.get("lootEntries")
        );
        Map<String, Integer> entryCounts = itemCountMap(
                safeRow.get("entryCounts"),
                metadata.get("entryCounts"),
                safeRow.get("itemCounts"),
                metadata.get("itemCounts"),
                safeRow.get("counts"),
                metadata.get("counts")
        );
        if (!entryCounts.isEmpty()) {
            entries = expandCounts(entryCounts);
        }
        String sourceLogicalId = firstText(
                safeRow.get("sourceLogicalId"),
                metadata.get("sourceLogicalId"),
                safeRow.get("nativeLoaderId"),
                metadata.get("nativeLoaderId"),
                contentId
        );
        return new EchoLootDefinition(tableId, entries, sourceLogicalId);
    }

    public static List<EchoEntityDefinition> entityDefinitionsFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        ArrayList<EchoEntityDefinition> definitions = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (isEntityRow(row)) {
                definitions.add(entityDefinitionFromRow(row));
            }
        }
        return List.copyOf(definitions);
    }

    public static EchoEntityDefinition entityDefinitionFromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : Map.copyOf(row);
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = text(safeRow.get("contentId"));
        String definitionId = firstText(
                safeRow.get("definitionId"),
                metadata.get("definitionId"),
                safeRow.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        String displayName = firstText(safeRow.get("displayName"), metadata.get("displayName"), displayName(contentId));
        EchoEntityKind kind = entityKind(firstText(safeRow.get("kind"), metadata.get("kind")), definitionId);
        int maxHealth = Math.max(1, intValue(firstText(safeRow.get("maxHealth"), metadata.get("maxHealth")), 20));
        int movementSpeed = Math.max(0, intValue(firstText(
                safeRow.get("movementSpeed"),
                metadata.get("movementSpeed"),
                metadata.get("speed")
        ), 1));
        String aiProfile = firstText(safeRow.get("aiProfile"), metadata.get("aiProfile"), defaultAiProfile(kind));
        return new EchoEntityDefinition(
                definitionId,
                displayName,
                kind,
                maxHealth,
                movementSpeed,
                aiProfile
        );
    }

    public static EchoAdapterCoreRegistryEntry entryFromRow(Map<String, Object> row) {
        Map<String, Object> safeRow = row == null ? Map.of() : Map.copyOf(row);
        Map<String, Object> metadata = map(safeRow.get("metadata"));
        String contentId = text(safeRow.get("contentId"));
        EchoAdapterCoreContentKind contentKind = contentKind(text(safeRow.get("contentKind")),
                text(safeRow.get("domain")));
        EchoAdapterCoreDomain domain = domain(text(safeRow.get("domain")), contentKind);
        String moduleId = firstText(safeRow.get("moduleId"), metadata.get("moduleId"), moduleFromContentId(contentId));
        String displayName = firstText(safeRow.get("displayName"), metadata.get("displayName"), displayName(contentId));
        String adapterKey = firstText(safeRow.get("adapterKey"), metadata.get("adapterKey"), metadata.get("route"), contentId);
        String neoForgeId = firstText(safeRow.get("neoForgeId"), metadata.get("neoForgeId"), contentId);
        String nativeLoaderId = firstText(safeRow.get("nativeLoaderId"), metadata.get("nativeLoaderId"), contentId);
        String standaloneRuntimeId = firstText(
                safeRow.get("standaloneRuntimeId"),
                metadata.get("standaloneRuntimeId"),
                contentId
        );
        String liveVoxelId = firstText(safeRow.get("liveVoxelId"), metadata.get("liveVoxelId"));
        if (liveVoxelId.isBlank()) {
            liveVoxelId = contentKind == EchoAdapterCoreContentKind.BLOCK
                    ? standaloneRuntimeId
                    : "adaptercore:non_voxel/" + compact(contentId);
        }
        boolean standaloneReady = booleanValue(safeRow.get("standaloneReady"),
                booleanValue(metadata.get("standaloneReady"), true));
        EchoVoxelBlock liveVoxelBlock = contentKind == EchoAdapterCoreContentKind.BLOCK
                ? blockFromRegistration(safeRow, metadata, liveVoxelId, displayName)
                : null;
        EchoAdapterCoreAssetReferences assetReferences =
                assetReferences(safeRow, metadata, moduleId, contentKind);
        EchoAdapterCoreRegistryMetadata registryMetadata =
                registryMetadata(safeRow, metadata, moduleId, domain, contentKind);
        return new EchoAdapterCoreRegistryEntry(
                new EchoAdapterCoreContentBinding(
                        moduleId,
                        contentId,
                        contentKind,
                        adapterKey,
                        neoForgeId,
                        nativeLoaderId,
                        standaloneRuntimeId,
                        liveVoxelId,
                        standaloneReady
                ),
                domain,
                displayName,
                liveVoxelBlock,
                assetReferences,
                registryMetadata
        );
    }

    private static boolean isItemRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        EchoAdapterCoreContentKind kind = contentKind(text(row.get("contentKind")), text(row.get("domain")));
        return kind == EchoAdapterCoreContentKind.ITEM;
    }

    private static boolean isRecipeRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        EchoAdapterCoreContentKind kind = contentKind(text(row.get("contentKind")), text(row.get("domain")));
        return kind == EchoAdapterCoreContentKind.RECIPE;
    }

    private static boolean isLootRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        EchoAdapterCoreContentKind kind = contentKind(text(row.get("contentKind")), text(row.get("domain")));
        return kind == EchoAdapterCoreContentKind.LOOT_TABLE;
    }

    private static boolean isEntityRow(Map<String, Object> row) {
        if (row == null) {
            return false;
        }
        EchoAdapterCoreContentKind kind = contentKind(text(row.get("contentKind")), text(row.get("domain")));
        return kind == EchoAdapterCoreContentKind.ENTITY;
    }

    private static EchoVoxelBlock blockFromRegistration(
            Map<String, Object> row,
            Map<String, Object> metadata,
            String liveVoxelId,
            String displayName
    ) {
        int argb = intValue(firstText(
                row.get("argb"),
                row.get("color"),
                metadata.get("argb"),
                metadata.get("color"),
                metadata.get("renderArgb")
        ), defaultBlockColor(liveVoxelId));
        int detailArgb = intValue(firstText(
                row.get("detailArgb"),
                metadata.get("detailArgb"),
                metadata.get("detailColor")
        ), detailColor(argb));
        String atlasKey = atlasKey(
                row.get("atlasKey"),
                metadata.get("atlasKey"),
                metadata.get("texture"),
                metadata.get("textureId"),
                metadata.get("texturePath"),
                liveVoxelId.replace(':', '/')
        );
        EchoVoxelMaterialPattern materialPattern = materialPattern(firstText(
                row.get("materialPattern"),
                metadata.get("materialPattern"),
                metadata.get("pattern")
        ), liveVoxelId);
        boolean solid = booleanValue(row.get("solid"), booleanValue(metadata.get("solid"), true));
        boolean opaque = booleanValue(row.get("opaque"), booleanValue(metadata.get("opaque"), solid));
        double hardness = doubleValue(firstText(row.get("hardness"), metadata.get("hardness")), 1.0D);
        return new EchoVoxelBlock(
                liveVoxelId,
                displayName,
                argb,
                detailArgb,
                atlasKey,
                materialPattern,
                solid,
                opaque,
                hardness
        );
    }

    private static EchoAdapterCoreAssetReferences assetReferences(
            Map<String, Object> row,
            Map<String, Object> metadata,
            String defaultNamespace,
            EchoAdapterCoreContentKind contentKind
    ) {
        String namespace = firstText(defaultNamespace, moduleFromContentId(text(row.get("contentId"))));
        String blockstateId = resourceId(
                firstText(
                        row.get("blockstate"),
                        metadata.get("blockstate"),
                        metadata.get("blockstateId"),
                        metadata.get("blockstateAsset")
                ),
                namespace,
                "blockstates",
                ".json"
        );
        String modelKind = contentKind == EchoAdapterCoreContentKind.ITEM ? "models/item" : "models";
        String modelId = resourceId(
                firstText(
                        row.get("model"),
                        metadata.get("model"),
                        metadata.get("modelId"),
                        metadata.get("modelAsset")
                ),
                namespace,
                modelKind,
                ".json"
        );
        String textureId = resourceId(
                firstText(
                        row.get("texture"),
                        row.get("textureId"),
                        row.get("texturePath"),
                        metadata.get("texture"),
                        metadata.get("textureId"),
                        metadata.get("texturePath"),
                        metadata.get("textureAsset")
                ),
                namespace,
                "textures",
                ".png"
        );
        String sourceLogicalId = firstText(
                row.get("sourceLogicalId"),
                metadata.get("sourceLogicalId"),
                row.get("nativeLoaderId"),
                metadata.get("nativeLoaderId")
        );
        return new EchoAdapterCoreAssetReferences(
                blockstateId,
                modelId,
                textureId,
                firstText(row.get("langKey"), metadata.get("langKey")),
                firstText(row.get("langValue"), metadata.get("langValue")),
                sourceLogicalId
        );
    }

    private static EchoAdapterCoreRegistryMetadata registryMetadata(
            Map<String, Object> row,
            Map<String, Object> metadata,
            String moduleId,
            EchoAdapterCoreDomain domain,
            EchoAdapterCoreContentKind contentKind
    ) {
        List<String> tags = textList(row.get("tags"), metadata.get("tags"));
        if (tags.isEmpty()) {
            tags = List.of("adaptercore", moduleId, domain.id(), contentKind.name().toLowerCase(Locale.ROOT));
        }
        return new EchoAdapterCoreRegistryMetadata(
                tags,
                stringMap(
                        row.get("defaultState"),
                        row.get("defaultStateProperties"),
                        metadata.get("defaultState"),
                        metadata.get("defaultStateProperties"),
                        metadata.get("stateProperties")
                ),
                textList(
                        row.get("behaviorHooks"),
                        row.get("runtimeBehaviorHooks"),
                        row.get("hooks"),
                        metadata.get("behaviorHooks"),
                        metadata.get("runtimeBehaviorHooks"),
                        metadata.get("hooks"),
                        metadata.get("interactionHooks")
                ),
                firstText(
                        row.get("saveCodecVersion"),
                        row.get("codecVersion"),
                        metadata.get("saveCodecVersion"),
                        metadata.get("codecVersion"),
                        metadata.get("saveCodec")
                ),
                stringMap(
                        row.get("compatibilityMetadata"),
                        row.get("compatibility"),
                        metadata.get("compatibilityMetadata"),
                        metadata.get("compatibility")
                )
        );
    }

    private static EchoAdapterCoreContentKind contentKind(String value, String domain) {
        String normalized = normalizedEnumToken(value);
        if (!normalized.isBlank()) {
            try {
                return EchoAdapterCoreContentKind.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                throw new IllegalArgumentException("Unknown AdapterCore content kind: " + value);
            }
        }
        if (normalizedEnumToken(domain).equals("FEATURES")) {
            return EchoAdapterCoreContentKind.FEATURE;
        }
        return switch (domain(domain, EchoAdapterCoreContentKind.DIAGNOSTIC)) {
            case BLOCKS -> EchoAdapterCoreContentKind.BLOCK;
            case ITEMS, INVENTORY -> EchoAdapterCoreContentKind.ITEM;
            case ENTITIES -> EchoAdapterCoreContentKind.ENTITY;
            case RECIPES -> EchoAdapterCoreContentKind.RECIPE;
            case LOOT -> EchoAdapterCoreContentKind.LOOT_TABLE;
            case STRUCTURES -> EchoAdapterCoreContentKind.STRUCTURE;
            case UI_SCREENS, UI_OVERLAYS -> EchoAdapterCoreContentKind.UI_SCREEN;
            case SOUNDS -> EchoAdapterCoreContentKind.SOUND_EVENT;
            case MISSIONS, STORY -> EchoAdapterCoreContentKind.MISSION;
            case SAVES -> EchoAdapterCoreContentKind.SAVE_RECORD;
            case WEATHER, HAZARDS -> EchoAdapterCoreContentKind.WORLD_HAZARD;
            case WORLDGEN, MAPS -> EchoAdapterCoreContentKind.WORLDGEN_DEFINITION;
            case NETWORKING -> EchoAdapterCoreContentKind.NETWORK_HOOK;
            case COMMANDS -> EchoAdapterCoreContentKind.COMMAND;
            default -> EchoAdapterCoreContentKind.DIAGNOSTIC;
        };
    }

    private static EchoAdapterCoreDomain domain(String value, EchoAdapterCoreContentKind contentKind) {
        return EchoAdapterCoreDomain.fromId(value).orElseGet(() -> domainForKind(contentKind));
    }

    private static EchoAdapterCoreDomain domainForKind(EchoAdapterCoreContentKind contentKind) {
        return switch (Objects.requireNonNull(contentKind, "contentKind")) {
            case BLOCK -> EchoAdapterCoreDomain.BLOCKS;
            case ITEM -> EchoAdapterCoreDomain.ITEMS;
            case ENTITY -> EchoAdapterCoreDomain.ENTITIES;
            case RECIPE -> EchoAdapterCoreDomain.RECIPES;
            case LOOT_TABLE -> EchoAdapterCoreDomain.LOOT;
            case STRUCTURE -> EchoAdapterCoreDomain.STRUCTURES;
            case FEATURE -> EchoAdapterCoreDomain.WORLDGEN;
            case UI_SCREEN -> EchoAdapterCoreDomain.UI_SCREENS;
            case SOUND_EVENT -> EchoAdapterCoreDomain.SOUNDS;
            case MISSION -> EchoAdapterCoreDomain.MISSIONS;
            case SAVE_RECORD -> EchoAdapterCoreDomain.SAVES;
            case WORLD_REGION -> EchoAdapterCoreDomain.WORLDGEN;
            case WORLD_HAZARD -> EchoAdapterCoreDomain.HAZARDS;
            case WORLDGEN_DEFINITION -> EchoAdapterCoreDomain.WORLDGEN;
            case NETWORK_HOOK -> EchoAdapterCoreDomain.NETWORKING;
            case COMMAND -> EchoAdapterCoreDomain.COMMANDS;
            case DIAGNOSTIC, DATA_COMPONENT, STATUS_EFFECT -> EchoAdapterCoreDomain.DIAGNOSTICS;
        };
    }

    private static String moduleFromContentId(String contentId) {
        int separator = contentId.indexOf(':');
        return separator > 0 ? contentId.substring(0, separator) : "adaptercore";
    }

    private static String displayName(String contentId) {
        String token = contentId;
        int separator = token.indexOf(':');
        if (separator >= 0 && separator + 1 < token.length()) {
            token = token.substring(separator + 1);
        }
        token = token.replace('/', ' ').replace('_', ' ').replace('-', ' ').trim();
        if (token.isBlank()) {
            return "AdapterCore Content";
        }
        StringBuilder result = new StringBuilder(token.length());
        boolean nextUpper = true;
        for (int index = 0; index < token.length(); index++) {
            char ch = token.charAt(index);
            if (Character.isWhitespace(ch)) {
                result.append(ch);
                nextUpper = true;
            } else if (nextUpper) {
                result.append(Character.toUpperCase(ch));
                nextUpper = false;
            } else {
                result.append(ch);
            }
        }
        return result.toString();
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static String atlasKey(Object... values) {
        String value = firstText(values).replace('\\', '/');
        if (value.isBlank()) {
            return "echo/block/missing";
        }
        if (value.startsWith("assets/")) {
            String path = value.substring("assets/".length());
            int separator = path.indexOf('/');
            if (separator > 0) {
                String namespace = path.substring(0, separator);
                String assetPath = stripTexturePath(path.substring(separator + 1));
                return namespace + "/" + assetPath;
            }
        }
        int namespaceSeparator = value.indexOf(':');
        if (namespaceSeparator > 0 && namespaceSeparator < value.length() - 1) {
            String namespace = value.substring(0, namespaceSeparator);
            String path = stripTexturePath(value.substring(namespaceSeparator + 1));
            return namespace + "/" + path;
        }
        return stripTexturePath(value);
    }

    private static String stripTexturePath(String path) {
        String normalized = text(path).replace('\\', '/');
        if (normalized.startsWith("textures/")) {
            normalized = normalized.substring("textures/".length());
        }
        if (normalized.endsWith(".png")) {
            normalized = normalized.substring(0, normalized.length() - ".png".length());
        }
        return normalized.isBlank() ? "block/missing" : normalized;
    }

    private static String resourceId(String value, String defaultNamespace, String kindPrefix, String suffix) {
        String normalized = text(value).replace('\\', '/');
        if (normalized.isBlank()) {
            return "";
        }
        String namespace = firstText(defaultNamespace, "adaptercore");
        String path = normalized;
        if (path.startsWith("assets/")) {
            path = path.substring("assets/".length());
            int separator = path.indexOf('/');
            if (separator > 0 && separator < path.length() - 1) {
                namespace = path.substring(0, separator);
                path = path.substring(separator + 1);
            }
        } else {
            int separator = path.indexOf(':');
            if (separator > 0 && separator < path.length() - 1) {
                namespace = path.substring(0, separator);
                path = path.substring(separator + 1);
            }
        }
        if (suffix != null && !suffix.isBlank() && path.endsWith(suffix)) {
            path = path.substring(0, path.length() - suffix.length());
        }
        String requiredPrefix = text(kindPrefix).replace('\\', '/');
        if ("models/item".equals(requiredPrefix) && path.startsWith("item/")) {
            path = "models/" + path;
        } else if ("models/item".equals(requiredPrefix) && path.startsWith("block/")) {
            path = "models/" + path;
        } else if ("models".equals(requiredPrefix) && (path.startsWith("item/") || path.startsWith("block/"))) {
            path = "models/" + path;
        }
        if (requiredPrefix.startsWith("models") && path.startsWith("models/")) {
            return namespace + ":" + path;
        }
        if (!requiredPrefix.isBlank() && !path.startsWith(requiredPrefix + "/")) {
            path = requiredPrefix + "/" + path;
        }
        return namespace + ":" + path;
    }

    private static String normalizedEnumToken(String value) {
        return text(value)
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace('.', '_')
                .replace(' ', '_');
    }

    private static String compact(String value) {
        String normalized = text(value).toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(normalized.length());
        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            result.append(Character.isLetterOrDigit(ch) ? ch : '_');
        }
        return result.isEmpty() ? "content" : result.toString();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static boolean booleanValue(Object value, boolean fallback) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        String text = text(value);
        return text.isBlank() ? fallback : Boolean.parseBoolean(text);
    }

    private static int intValue(String value, int fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            if (text.startsWith("#")) {
                String hex = text.substring(1);
                if (hex.length() == 6) {
                    hex = "FF" + hex;
                }
                return (int) Long.parseLong(hex, 16);
            }
            if (text.startsWith("0x") || text.startsWith("0X")) {
                return (int) Long.parseLong(text.substring(2), 16);
            }
            return Integer.parseInt(text);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static double doubleValue(String value, double fallback) {
        String text = text(value);
        if (text.isBlank()) {
            return fallback;
        }
        try {
            return Math.max(0.0D, Double.parseDouble(text));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static EchoVoxelMaterialPattern materialPattern(String value, String liveVoxelId) {
        String normalized = normalizedEnumToken(value);
        if (!normalized.isBlank()) {
            try {
                return EchoVoxelMaterialPattern.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return EchoVoxelMaterialPattern.infer(liveVoxelId);
            }
        }
        return EchoVoxelMaterialPattern.infer(liveVoxelId);
    }

    private static EchoItemCategory itemCategory(String value, String itemId) {
        String normalized = normalizedEnumToken(value);
        if (!normalized.isBlank()) {
            try {
                return EchoItemCategory.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return EchoItemDefinitionInference.inferCategory(itemId);
            }
        }
        return EchoItemDefinitionInference.inferCategory(itemId);
    }

    private static EchoEntityKind entityKind(String value, String definitionId) {
        String normalized = normalizedEnumToken(value);
        if (!normalized.isBlank()) {
            try {
                return EchoEntityKind.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return inferEntityKind(definitionId);
            }
        }
        return inferEntityKind(definitionId);
    }

    private static EchoEntityKind inferEntityKind(String definitionId) {
        String id = text(definitionId).toLowerCase(Locale.ROOT);
        if (id.contains("player")) {
            return EchoEntityKind.PLAYER;
        }
        if (id.contains("familiar") || id.contains("pet")) {
            return EchoEntityKind.FAMILIAR;
        }
        if (id.contains("npc") || id.contains("villager")) {
            return EchoEntityKind.NPC;
        }
        if (id.contains("prop") || id.contains("deco")) {
            return EchoEntityKind.PROP;
        }
        return EchoEntityKind.HOSTILE;
    }

    private static String defaultAiProfile(EchoEntityKind kind) {
        return kind == EchoEntityKind.HOSTILE ? "hostile" : "idle";
    }

    private static int defaultBlockColor(String liveVoxelId) {
        int hash = text(liveVoxelId).hashCode();
        int red = 96 + Math.floorMod(hash >>> 16, 96);
        int green = 96 + Math.floorMod(hash >>> 8, 96);
        int blue = 96 + Math.floorMod(hash, 96);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static int detailColor(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = clamp((int) Math.round(((argb >>> 16) & 0xFF) * 1.18D), 0, 255);
        int green = clamp((int) Math.round(((argb >>> 8) & 0xFF) * 1.10D), 0, 255);
        int blue = clamp((int) Math.round((argb & 0xFF) * 0.92D), 0, 255);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static List<String> textList(Object... values) {
        if (values == null) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    String text = text(item);
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            } else if (value instanceof String text) {
                for (String item : text.split("[,;]")) {
                    String normalized = item.trim();
                    if (!normalized.isBlank()) {
                        result.add(normalized);
                    }
                }
            }
            if (!result.isEmpty()) {
                return List.copyOf(result);
            }
        }
        return List.of();
    }

    private static Map<String, Integer> itemCountMap(Object... values) {
        if (values == null) {
            return Map.of();
        }
        for (Object value : values) {
            if (value instanceof Map<?, ?> raw) {
                LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    String itemId = text(entry.getKey());
                    int count = intValue(text(entry.getValue()), 0);
                    if (!itemId.isBlank() && count > 0) {
                        result.put(itemId, count);
                    }
                }
                if (!result.isEmpty()) {
                    return Map.copyOf(result);
                }
            }
        }
        return Map.of();
    }

    private static Map<String, Integer> countTextList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (String value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                result.merge(text, 1, Integer::sum);
            }
        }
        return result.isEmpty() ? Map.of() : Map.copyOf(result);
    }

    private static List<String> expandCounts(Map<String, Integer> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            String text = text(entry.getKey());
            int count = Math.max(0, entry.getValue() == null ? 0 : entry.getValue());
            for (int index = 0; !text.isBlank() && index < count; index++) {
                result.add(text);
            }
        }
        return result.isEmpty() ? List.of() : List.copyOf(result);
    }

    private static Map<String, String> stringMap(Object... values) {
        if (values == null) {
            return Map.of();
        }
        for (Object value : values) {
            if (value instanceof Map<?, ?> raw) {
                LinkedHashMap<String, String> result = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    String key = text(entry.getKey());
                    String entryValue = text(entry.getValue());
                    if (!key.isBlank() && !entryValue.isBlank()) {
                        result.put(key, entryValue);
                    }
                }
                if (!result.isEmpty()) {
                    return Map.copyOf(result);
                }
            }
        }
        return Map.of();
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> mapped = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                mapped.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(mapped);
    }
}
