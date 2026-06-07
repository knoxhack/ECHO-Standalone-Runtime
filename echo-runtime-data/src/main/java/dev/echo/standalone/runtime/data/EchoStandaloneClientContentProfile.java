package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EchoStandaloneClientContentProfile(
        String id,
        WorldTemplate worldTemplate,
        EntityCatalog entityCatalog,
        HazardCatalog hazardCatalog,
        StarterLoadout starterLoadout,
        InteractionCatalog interactionCatalog
) {
    public EchoStandaloneClientContentProfile {
        id = EchoDataPaths.requireText(id, "id");
        worldTemplate = Objects.requireNonNull(worldTemplate, "worldTemplate");
        entityCatalog = Objects.requireNonNull(entityCatalog, "entityCatalog");
        hazardCatalog = Objects.requireNonNull(hazardCatalog, "hazardCatalog");
        starterLoadout = Objects.requireNonNull(starterLoadout, "starterLoadout");
        interactionCatalog = Objects.requireNonNull(interactionCatalog, "interactionCatalog");
    }

    public record WorldTemplate(
            String slotIdPrefix,
            String displayName,
            SaveProfile saveProfile,
            Presentation presentation,
            AudioProfile audioProfile
    ) {
        public WorldTemplate {
            slotIdPrefix = EchoDataPaths.requireText(slotIdPrefix, "slotIdPrefix");
            displayName = EchoDataPaths.requireText(displayName, "displayName");
            saveProfile = Objects.requireNonNull(saveProfile, "saveProfile");
            presentation = Objects.requireNonNull(presentation, "presentation");
            audioProfile = Objects.requireNonNull(audioProfile, "audioProfile");
        }
    }

    public record SaveProfile(
            String schema,
            String profileId,
            String displayName,
            String packId,
            int formatVersion,
            Map<String, String> metadata
    ) {
        public SaveProfile {
            schema = EchoDataPaths.requireText(schema, "schema");
            profileId = EchoDataPaths.requireText(profileId, "profileId");
            displayName = EchoDataPaths.requireText(displayName, "displayName");
            packId = EchoDataPaths.requireText(packId, "packId");
            if (formatVersion <= 0) {
                throw new IllegalArgumentException("formatVersion must be positive");
            }
            metadata = Map.copyOf(metadata == null ? Map.of() : metadata);
        }
    }

    public record Presentation(
            String windowTitle,
            String settingsFileComment,
            String createWorldActionLabel,
            String worldTypeLabel,
            String packLabel,
            String newWorldModalMessage,
            String loadingInitialDetail,
            String newWorldGenerationLabel,
            String newWorldDetailSuffix,
            String newWorldLoadingFooter,
            String savedWorldLoadingFooter,
            Map<String, String> moduleSourceLabels,
            String hostileDamageSourceId
    ) {
        public Presentation {
            windowTitle = EchoDataPaths.requireText(windowTitle, "windowTitle");
            settingsFileComment = EchoDataPaths.requireText(settingsFileComment, "settingsFileComment");
            createWorldActionLabel = EchoDataPaths.requireText(createWorldActionLabel, "createWorldActionLabel");
            worldTypeLabel = EchoDataPaths.requireText(worldTypeLabel, "worldTypeLabel");
            packLabel = EchoDataPaths.requireText(packLabel, "packLabel");
            newWorldModalMessage = EchoDataPaths.requireText(newWorldModalMessage, "newWorldModalMessage");
            loadingInitialDetail = EchoDataPaths.requireText(loadingInitialDetail, "loadingInitialDetail");
            newWorldGenerationLabel = EchoDataPaths.requireText(newWorldGenerationLabel, "newWorldGenerationLabel");
            newWorldDetailSuffix = EchoDataPaths.requireText(newWorldDetailSuffix, "newWorldDetailSuffix");
            newWorldLoadingFooter = EchoDataPaths.requireText(newWorldLoadingFooter, "newWorldLoadingFooter");
            savedWorldLoadingFooter = EchoDataPaths.requireText(savedWorldLoadingFooter, "savedWorldLoadingFooter");
            moduleSourceLabels = Map.copyOf(moduleSourceLabels == null ? Map.of() : moduleSourceLabels);
            hostileDamageSourceId = EchoDataPaths.requireText(hostileDamageSourceId, "hostileDamageSourceId");
        }
    }

    public record AudioProfile(
            String deviceProfileId,
            String volumeProfileId
    ) {
        public AudioProfile {
            deviceProfileId = EchoDataPaths.requireText(deviceProfileId, "deviceProfileId");
            volumeProfileId = EchoDataPaths.requireText(volumeProfileId, "volumeProfileId");
        }
    }

    public record EntityCatalog(
            EntityDefinition fallbackHostile,
            List<SpawnRule> spawnRules,
            List<RenderProfile> renderProfiles
    ) {
        public EntityCatalog {
            fallbackHostile = Objects.requireNonNull(fallbackHostile, "fallbackHostile");
            spawnRules = List.copyOf(spawnRules == null ? List.of() : spawnRules);
            renderProfiles = List.copyOf(renderProfiles == null ? List.of() : renderProfiles);
        }
    }

    public record EntityDefinition(
            String id,
            String displayName,
            int maxHealth,
            String aiProfile
    ) {
        public EntityDefinition {
            id = EchoDataPaths.requireText(id, "entity id");
            displayName = EchoDataPaths.requireText(displayName, "entity displayName");
            if (maxHealth <= 0) {
                throw new IllegalArgumentException("entity maxHealth must be positive");
            }
            aiProfile = EchoDataPaths.requireText(aiProfile, "entity aiProfile");
        }
    }

    public record SpawnRule(
            List<String> biomeTags,
            EntityDefinition definition
    ) {
        public SpawnRule {
            biomeTags = normalizedTags(biomeTags);
            definition = Objects.requireNonNull(definition, "definition");
        }
    }

    public record RenderProfile(
            String definitionId,
            int argb,
            String shape
    ) {
        public RenderProfile {
            definitionId = EchoDataPaths.requireText(definitionId, "render profile definitionId");
            shape = EchoDataPaths.requireText(shape, "render profile shape").toUpperCase(java.util.Locale.ROOT);
        }
    }

    public record HazardCatalog(
            List<HazardRule> rules
    ) {
        public HazardCatalog {
            rules = List.copyOf(rules == null ? List.of() : rules);
        }
    }

    public record HazardRule(
            List<String> biomeTags,
            HazardProfile profile
    ) {
        public HazardRule {
            biomeTags = normalizedTags(biomeTags);
            profile = Objects.requireNonNull(profile, "profile");
        }
    }

    public record HazardProfile(
            String id,
            String label,
            double exposurePerSecond,
            int damage
    ) {
        public HazardProfile {
            id = EchoDataPaths.requireText(id, "hazard id");
            label = EchoDataPaths.requireText(label, "hazard label");
            if (exposurePerSecond < 0.0D) {
                throw new IllegalArgumentException("hazard exposurePerSecond must not be negative");
            }
            if (damage < 0) {
                throw new IllegalArgumentException("hazard damage must not be negative");
            }
        }
    }

    public record StarterLoadout(
            String playerInventoryId,
            String playerInventoryLabel,
            String openContainerId,
            String openContainerLabel,
            List<StarterItem> items,
            List<StarterStack> openContainerStacks,
            List<StarterRecipe> workbenchRecipes
    ) {
        public StarterLoadout {
            playerInventoryId = EchoDataPaths.requireText(playerInventoryId, "playerInventoryId");
            playerInventoryLabel = EchoDataPaths.requireText(playerInventoryLabel, "playerInventoryLabel");
            openContainerId = EchoDataPaths.requireText(openContainerId, "openContainerId");
            openContainerLabel = EchoDataPaths.requireText(openContainerLabel, "openContainerLabel");
            items = List.copyOf(items == null ? List.of() : items);
            openContainerStacks = List.copyOf(openContainerStacks == null ? List.of() : openContainerStacks);
            workbenchRecipes = List.copyOf(workbenchRecipes == null ? List.of() : workbenchRecipes);
        }
    }

    public record StarterItem(
            String id,
            String displayName,
            String category,
            int maxStackSize,
            List<String> tags,
            List<String> tooltipLines
    ) {
        public StarterItem {
            id = EchoDataPaths.requireText(id, "starter item id");
            displayName = EchoDataPaths.requireText(displayName, "starter item displayName");
            category = EchoDataPaths.requireText(category, "starter item category").toUpperCase(java.util.Locale.ROOT);
            if (maxStackSize <= 0) {
                throw new IllegalArgumentException("starter item maxStackSize must be positive");
            }
            tags = normalizedTags(tags);
            tooltipLines = List.copyOf(tooltipLines == null ? List.of() : tooltipLines);
        }
    }

    public record StarterStack(
            int slotIndex,
            String itemId,
            int quantity
    ) {
        public StarterStack {
            itemId = EchoDataPaths.requireText(itemId, "starter stack itemId");
            if (quantity <= 0) {
                throw new IllegalArgumentException("starter stack quantity must be positive");
            }
        }
    }

    public record StarterRecipe(
            String recipeId,
            Map<String, Integer> ingredients,
            String outputItemId,
            int outputQuantity
    ) {
        public StarterRecipe {
            recipeId = EchoDataPaths.requireText(recipeId, "starter recipe recipeId");
            outputItemId = EchoDataPaths.requireText(outputItemId, "starter recipe outputItemId");
            ingredients = normalizedIngredientCounts(ingredients);
            if (ingredients.isEmpty()) {
                throw new IllegalArgumentException("starter recipe ingredients must not be empty");
            }
            if (outputQuantity <= 0) {
                throw new IllegalArgumentException("starter recipe outputQuantity must be positive");
            }
        }
    }

    public record InteractionCatalog(
            List<InteractionRule> rules
    ) {
        public InteractionCatalog {
            rules = List.copyOf(rules == null ? List.of() : rules);
        }
    }

    public record InteractionRule(
            List<String> matchTokens,
            String command,
            String targetId
    ) {
        public InteractionRule {
            matchTokens = normalizedTags(matchTokens);
            if (matchTokens.isEmpty()) {
                throw new IllegalArgumentException("interaction rule matchTokens must not be empty");
            }
            command = EchoDataPaths.requireText(command, "interaction rule command").toUpperCase(java.util.Locale.ROOT);
            targetId = targetId == null ? "" : targetId.trim();
        }
    }

    private static List<String> normalizedTags(List<String> values) {
        return (values == null ? List.<String>of() : values).stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private static Map<String, Integer> normalizedIngredientCounts(Map<String, Integer> values) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : (values == null ? Map.<String, Integer>of() : values).entrySet()) {
            String itemId = EchoDataPaths.requireText(entry.getKey(), "starter recipe ingredient");
            Integer quantity = entry.getValue();
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("starter recipe ingredient quantity must be positive");
            }
            result.put(itemId, quantity);
        }
        return Map.copyOf(result);
    }
}
