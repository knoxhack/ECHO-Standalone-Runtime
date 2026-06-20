package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.entity.EchoEntityDefinition;
import dev.echo.standalone.runtime.entity.EchoEntityKind;
import dev.echo.standalone.runtime.world.EchoVoxelBiome;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

record EchoClientEntityCatalog(
        EchoEntityDefinition fallbackHostile,
        List<SpawnRule> spawnRules,
        Map<String, RenderProfile> renderProfiles,
        Map<String, EchoEntityDefinition> definitions
) {
    EchoClientEntityCatalog {
        fallbackHostile = fallbackHostile == null
                ? hostile("echo:hostile", "Hostile", 20, "hostile")
                : fallbackHostile;
        spawnRules = List.copyOf(spawnRules == null ? List.of() : spawnRules);
        renderProfiles = Map.copyOf(renderProfiles == null ? Map.of() : renderProfiles);
        LinkedHashMap<String, EchoEntityDefinition> cleanDefinitions = new LinkedHashMap<>();
        if (definitions != null) {
            for (EchoEntityDefinition definition : definitions.values()) {
                if (definition != null && !definition.definitionId().isBlank()) {
                    cleanDefinitions.put(definition.definitionId(), definition);
                }
            }
        }
        cleanDefinitions.put(fallbackHostile.definitionId(), fallbackHostile);
        for (SpawnRule rule : spawnRules) {
            cleanDefinitions.put(rule.definition().definitionId(), rule.definition());
        }
        definitions = Map.copyOf(cleanDefinitions);
    }

    static EchoClientEntityCatalog empty() {
        return new EchoClientEntityCatalog(
                hostile("echo:hostile", "Hostile", 20, "hostile"),
                List.of(),
                Map.of(),
                Map.of()
        );
    }

    EchoEntityDefinition definitionForBiome(EchoVoxelBiome biome) {
        if (biome == null) {
            return fallbackHostile;
        }
        for (SpawnRule rule : spawnRules) {
            if (rule.matches(biome)) {
                return rule.definition();
            }
        }
        return fallbackHostile;
    }

    Optional<EchoEntityDefinition> definition(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(definitions.get(definitionId.trim()));
    }

    RenderProfile renderProfile(String definitionId) {
        if (definitionId == null || definitionId.isBlank()) {
            return RenderProfile.DEFAULT;
        }
        return renderProfiles.getOrDefault(definitionId, RenderProfile.DEFAULT);
    }

    Optional<EntityVisualProfile> firstGraphBackedSpawnProfile() {
        for (SpawnRule rule : spawnRules) {
            EchoEntityDefinition definition = rule.definition();
            RenderProfile profile = renderProfile(definition.definitionId());
            if (profile.graphBackedVisual() && !rule.biomeTags().isEmpty()) {
                return Optional.of(new EntityVisualProfile(definition, profile, rule.biomeTags()));
            }
        }
        return graphBackedVisualProfiles().stream().findFirst();
    }

    List<EntityVisualProfile> graphBackedVisualProfiles() {
        ArrayList<EntityVisualProfile> profiles = new ArrayList<>();
        for (Map.Entry<String, RenderProfile> entry : renderProfiles.entrySet()) {
            RenderProfile profile = entry.getValue();
            if (profile == null || !profile.graphBackedVisual()) {
                continue;
            }
            EchoEntityDefinition definition = definitions.get(entry.getKey());
            if (definition != null) {
                profiles.add(new EntityVisualProfile(definition, profile, profile.spawnBiomeTags()));
            }
        }
        return List.copyOf(profiles);
    }

    long graphBackedVisualProfileCount() {
        return renderProfiles.values().stream()
                .filter(RenderProfile::graphBackedVisual)
                .count();
    }

    long graphBackedThreatProfileCount() {
        return renderProfiles.values().stream()
                .filter(RenderProfile::threatMetadataPresent)
                .count();
    }

    long graphBackedSpawnRuleProfileCount() {
        return renderProfiles.values().stream()
                .filter(RenderProfile::spawnRuleMetadataPresent)
                .count();
    }

    static EchoEntityDefinition hostile(String id, String name, int maxHealth, String aiProfile) {
        return new EchoEntityDefinition(id, name, EchoEntityKind.HOSTILE, maxHealth, 1, aiProfile);
    }

    static SpawnRule spawnWhenAnyTag(EchoEntityDefinition definition, String... tags) {
        return new SpawnRule(List.of(tags), definition);
    }

    static RenderProfile renderProfile(int argb, RenderShape shape) {
        return new RenderProfile(argb, shape);
    }

    record SpawnRule(
            List<String> biomeTags,
            EchoEntityDefinition definition
    ) {
        SpawnRule {
            biomeTags = List.copyOf(biomeTags == null ? List.of() : biomeTags);
            if (definition == null) {
                throw new IllegalArgumentException("definition must not be null");
            }
        }

        boolean matches(EchoVoxelBiome biome) {
            for (String tag : biomeTags) {
                if (tag != null && !tag.isBlank() && biome.hasTag(tag)) {
                    return true;
                }
            }
            return false;
        }
    }

    record EntityVisualProfile(
            EchoEntityDefinition definition,
            RenderProfile renderProfile,
            List<String> spawnBiomeTags
    ) {
        EntityVisualProfile {
            if (definition == null) {
                throw new IllegalArgumentException("definition must not be null");
            }
            renderProfile = renderProfile == null ? RenderProfile.DEFAULT : renderProfile;
            spawnBiomeTags = List.copyOf(spawnBiomeTags == null ? List.of() : spawnBiomeTags);
        }

        boolean graphBackedVisual() {
            return renderProfile.graphBackedVisual();
        }

        boolean threatMetadataPresent() {
            return renderProfile.threatMetadataPresent();
        }

        boolean spawnRuleMetadataPresent() {
            return !spawnBiomeTags.isEmpty() || renderProfile.spawnRuleMetadataPresent();
        }
    }

    record RenderProfile(
            int argb,
            RenderShape shape,
            String modelId,
            String textureId,
            String animationId,
            String renderProfileId,
            boolean graphBackedVisual,
            String threatProfile,
            int threatLevel,
            List<String> spawnBiomeTags
    ) {
        static final RenderProfile DEFAULT = new RenderProfile(0xFF7FA35D, RenderShape.HUMANOID);

        RenderProfile(int argb, RenderShape shape) {
            this(argb, shape, "", "", "", "", false, "", 0, List.of());
        }

        RenderProfile {
            shape = shape == null ? RenderShape.HUMANOID : shape;
            modelId = clean(modelId);
            textureId = clean(textureId);
            animationId = clean(animationId);
            renderProfileId = clean(renderProfileId);
            graphBackedVisual = graphBackedVisual
                    && (!modelId.isBlank() || !textureId.isBlank() || !animationId.isBlank());
            threatProfile = clean(threatProfile);
            threatLevel = Math.max(0, threatLevel);
            spawnBiomeTags = List.copyOf(spawnBiomeTags == null ? List.of() : spawnBiomeTags.stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(String::trim)
                    .toList());
        }

        boolean threatMetadataPresent() {
            return !threatProfile.isBlank() || threatLevel > 0;
        }

        boolean spawnRuleMetadataPresent() {
            return !spawnBiomeTags.isEmpty();
        }

        private static String clean(String value) {
            return value == null ? "" : value.trim();
        }
    }

    enum RenderShape {
        HUMANOID,
        SLIME,
        DRONE
    }

    static Map<String, RenderProfile> renderProfiles(RenderProfileEntry... entries) {
        LinkedHashMap<String, RenderProfile> result = new LinkedHashMap<>();
        for (RenderProfileEntry entry : entries == null ? List.<RenderProfileEntry>of() : List.of(entries)) {
            if (entry != null) {
                result.put(entry.definitionId(), entry.profile());
            }
        }
        return result;
    }

    static RenderProfileEntry renderProfileEntry(String definitionId, int argb, RenderShape shape) {
        return new RenderProfileEntry(definitionId, renderProfile(argb, shape));
    }

    record RenderProfileEntry(String definitionId, RenderProfile profile) {
        RenderProfileEntry {
            if (definitionId == null || definitionId.isBlank()) {
                throw new IllegalArgumentException("definitionId must not be blank");
            }
            definitionId = definitionId.trim();
            profile = profile == null ? RenderProfile.DEFAULT : profile;
        }
    }
}
