package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.data.EchoDataTag;
import dev.echo.standalone.runtime.data.EchoRecipeDefinition;

import java.util.List;

final class EchoClientWorldSessionFactory {
    private static final EchoClientWorldSessionFactory DEFAULT =
            new EchoClientWorldSessionFactory(EchoClientWorldTemplates.defaultTemplate());
    private final EchoClientWorldTemplate template;

    private EchoClientWorldSessionFactory(EchoClientWorldTemplate template) {
        this.template = template == null ? EchoClientWorldTemplates.defaultTemplate() : template;
    }

    static EchoClientWorldSessionFactory defaultFactory() {
        return DEFAULT;
    }

    static EchoClientWorldSessionFactory forTemplate(EchoClientWorldTemplate template) {
        return new EchoClientWorldSessionFactory(template);
    }

    EchoClientWorldTemplate template() {
        return template;
    }

    EchoClientWorldSession newWorld(String seedText) {
        return newWorld(seedText, List.of());
    }

    EchoClientWorldSession newWorld(String seedText, List<EchoRecipeDefinition> recipes) {
        return newWorld(seedText, recipes, List.of());
    }

    EchoClientWorldSession newWorld(
            String seedText,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        String normalizedSeed = normalizeSeed(seedText);
        return new EchoClientWorldSession(
                template.slotIdPrefix() + "-" + slotSeedSuffix(normalizedSeed),
                template.displayName() + " - Seed " + normalizedSeed,
                template.newSession(normalizedSeed, safeRecipes(recipes), safeTags(tags))
        );
    }

    EchoClientWorldSession newWorld(String seedText, String worldName) {
        return newWorld(seedText, worldName, List.of());
    }

    EchoClientWorldSession newWorld(String seedText, String worldName, List<EchoRecipeDefinition> recipes) {
        return newWorld(seedText, worldName, recipes, List.of());
    }

    EchoClientWorldSession newWorld(
            String seedText,
            String worldName,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        String normalizedSeed = normalizeSeed(seedText);
        String normalizedName = normalizeWorldName(worldName);
        return new EchoClientWorldSession(
                template.slotIdPrefix()
                        + "-" + slotNameSuffix(normalizedName)
                        + "-" + slotSeedSuffix(normalizedSeed),
                normalizedName,
                template.newSession(normalizedSeed, safeRecipes(recipes), safeTags(tags))
        );
    }

    EchoClientWorldSession restoreSavedSession(EchoClientSavedSessionSnapshot snapshot) {
        return restoreSavedSession(snapshot, List.of());
    }

    EchoClientWorldSession restoreSavedSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return restoreSavedSession(snapshot, recipes, List.of());
    }

    EchoClientWorldSession restoreSavedSession(
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return new EchoClientWorldSession(
                template.slotIdPrefix(),
                template.displayName(),
                template.restoreSession(snapshot, safeRecipes(recipes), safeTags(tags))
        );
    }

    EchoClientWorldSession restoreSavedSession(
            String slotId,
            String displayName,
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return restoreSavedSession(slotId, displayName, snapshot, recipes, List.of());
    }

    EchoClientWorldSession restoreSavedSession(
            String slotId,
            String displayName,
            EchoClientSavedSessionSnapshot snapshot,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return new EchoClientWorldSession(
                slotId,
                displayName,
                template.restoreSession(snapshot, safeRecipes(recipes), safeTags(tags))
        );
    }

    EchoClientWorldSession restoreGameplaySnapshot(
            String slotId,
            String displayName,
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> recipes
    ) {
        return restoreGameplaySnapshot(slotId, displayName, snapshot, recipes, List.of());
    }

    EchoClientWorldSession restoreGameplaySnapshot(
            String slotId,
            String displayName,
            EchoClientGameplay.GameplaySnapshot snapshot,
            List<EchoRecipeDefinition> recipes,
            List<EchoDataTag> tags
    ) {
        return new EchoClientWorldSession(
                slotId,
                displayName,
                template.restoreGameplaySnapshot(snapshot, safeRecipes(recipes), safeTags(tags))
        );
    }

    private static List<EchoRecipeDefinition> safeRecipes(List<EchoRecipeDefinition> recipes) {
        return recipes == null ? List.of() : List.copyOf(recipes);
    }

    private static List<EchoDataTag> safeTags(List<EchoDataTag> tags) {
        return tags == null ? List.of() : List.copyOf(tags);
    }

    private String normalizeSeed(String seedText) {
        String normalized = seedText == null || seedText.isBlank()
                ? Long.toString(template.defaultSeed())
                : seedText.trim();
        return normalized.length() > 24 ? normalized.substring(0, 24) : normalized;
    }

    private String normalizeWorldName(String worldName) {
        String normalized = worldName == null || worldName.isBlank()
                ? template.displayName()
                : worldName.trim();
        return normalized.length() > 48 ? normalized.substring(0, 48).stripTrailing() : normalized;
    }

    private String slotSeedSuffix(String seedText) {
        String normalized = seedText.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return normalized.isBlank() ? Long.toString(template.defaultSeed()) : normalized;
    }

    private String slotNameSuffix(String worldName) {
        String normalized = worldName.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9_-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        return normalized.isBlank() ? slotSeedSuffix(worldName) : normalized;
    }
}
