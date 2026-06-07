package dev.echo.standalone.runtime.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoRecipeRegistry {
    private final LinkedHashMap<String, EchoRecipeDefinition> recipes = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoRecipeDefinition recipe) {
        ensureMutable();
        Objects.requireNonNull(recipe, "recipe");
        recipes.put(recipe.id(), recipe);
    }

    public Optional<EchoRecipeDefinition> find(String id) {
        return Optional.ofNullable(recipes.get(id));
    }

    public List<EchoRecipeDefinition> recipes() {
        return recipes.values().stream()
                .sorted(java.util.Comparator.comparing(EchoRecipeDefinition::id))
                .toList();
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Recipe registry is frozen");
        }
    }
}
