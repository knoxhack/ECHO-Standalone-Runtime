package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public record EchoOpenlandsFirstHourContract(
        List<String> routeSteps,
        Set<String> blocks,
        Set<String> items,
        Set<String> recipes,
        Set<String> biomes,
        Set<String> landmarks,
        Set<String> creatures,
        Set<String> tutorialPrompts,
        List<String> waystoneStates,
        List<String> saveFields,
        Set<String> playtestScenarios,
        Set<String> saveLoadCheckpoints
) {
    public EchoOpenlandsFirstHourContract {
        Objects.requireNonNull(routeSteps, "routeSteps");
        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(recipes, "recipes");
        Objects.requireNonNull(biomes, "biomes");
        Objects.requireNonNull(landmarks, "landmarks");
        Objects.requireNonNull(creatures, "creatures");
        Objects.requireNonNull(tutorialPrompts, "tutorialPrompts");
        Objects.requireNonNull(waystoneStates, "waystoneStates");
        Objects.requireNonNull(saveFields, "saveFields");
        Objects.requireNonNull(playtestScenarios, "playtestScenarios");
        Objects.requireNonNull(saveLoadCheckpoints, "saveLoadCheckpoints");
        routeSteps = List.copyOf(routeSteps);
        blocks = Set.copyOf(new TreeSet<>(blocks));
        items = Set.copyOf(new TreeSet<>(items));
        recipes = Set.copyOf(new TreeSet<>(recipes));
        biomes = Set.copyOf(new TreeSet<>(biomes));
        landmarks = Set.copyOf(new TreeSet<>(landmarks));
        creatures = Set.copyOf(new TreeSet<>(creatures));
        tutorialPrompts = Set.copyOf(new TreeSet<>(tutorialPrompts));
        waystoneStates = List.copyOf(waystoneStates);
        saveFields = List.copyOf(saveFields);
        playtestScenarios = Set.copyOf(new TreeSet<>(playtestScenarios));
        saveLoadCheckpoints = Set.copyOf(new TreeSet<>(saveLoadCheckpoints));
    }
}
