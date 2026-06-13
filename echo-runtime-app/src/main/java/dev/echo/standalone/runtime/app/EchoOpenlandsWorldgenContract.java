package dev.echo.standalone.runtime.app;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record EchoOpenlandsWorldgenContract(
        Set<String> blocks,
        Set<String> items,
        List<String> starterGuarantees,
        List<BiomeProfile> biomes,
        List<Landmark> landmarks,
        List<Creature> creatures,
        Map<String, List<String>> regionNamePools,
        Set<String> hintTypes
) {
    public EchoOpenlandsWorldgenContract {
        Objects.requireNonNull(blocks, "blocks");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(starterGuarantees, "starterGuarantees");
        Objects.requireNonNull(biomes, "biomes");
        Objects.requireNonNull(landmarks, "landmarks");
        Objects.requireNonNull(creatures, "creatures");
        Objects.requireNonNull(regionNamePools, "regionNamePools");
        Objects.requireNonNull(hintTypes, "hintTypes");
        blocks = Set.copyOf(new TreeSet<>(blocks));
        items = Set.copyOf(new TreeSet<>(items));
        starterGuarantees = List.copyOf(starterGuarantees);
        biomes = List.copyOf(biomes);
        landmarks = List.copyOf(landmarks);
        creatures = List.copyOf(creatures);
        regionNamePools = copyListMap(regionNamePools);
        hintTypes = Set.copyOf(new TreeSet<>(hintTypes));
    }

    public Optional<BiomeProfile> biome(String id) {
        return biomes.stream().filter(profile -> profile.id().equals(id)).findFirst();
    }

    public Optional<Landmark> landmark(String id) {
        return landmarks.stream().filter(landmark -> landmark.id().equals(id)).findFirst();
    }

    public Optional<Creature> creature(String id) {
        return creatures.stream().filter(creature -> creature.id().equals(id)).findFirst();
    }

    public Set<String> biomeIds() {
        TreeSet<String> ids = new TreeSet<>();
        for (BiomeProfile biome : biomes) {
            ids.add(biome.id());
        }
        return Set.copyOf(ids);
    }

    public Set<String> landmarkIds() {
        TreeSet<String> ids = new TreeSet<>();
        for (Landmark landmark : landmarks) {
            ids.add(landmark.id());
        }
        return Set.copyOf(ids);
    }

    public Set<String> creatureIds() {
        TreeSet<String> ids = new TreeSet<>();
        for (Creature creature : creatures) {
            ids.add(creature.id());
        }
        return Set.copyOf(ids);
    }

    private static Map<String, List<String>> copyListMap(Map<String, List<String>> source) {
        TreeMap<String, List<String>> copy = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copy.put(requireText(entry.getKey(), "map key"), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }

    public record BiomeProfile(
            String id,
            Map<String, List<String>> blockPalette,
            List<String> resourceSet,
            List<SpawnEntry> spawnTable,
            Map<String, String> landmarkFrequency
    ) {
        public BiomeProfile {
            id = requireText(id, "biome id");
            Objects.requireNonNull(blockPalette, "blockPalette");
            Objects.requireNonNull(resourceSet, "resourceSet");
            Objects.requireNonNull(spawnTable, "spawnTable");
            Objects.requireNonNull(landmarkFrequency, "landmarkFrequency");
            blockPalette = copyListMap(blockPalette);
            resourceSet = List.copyOf(resourceSet);
            spawnTable = List.copyOf(spawnTable);
            landmarkFrequency = Map.copyOf(new TreeMap<>(landmarkFrequency));
        }

        public Set<String> paletteTokens() {
            TreeSet<String> tokens = new TreeSet<>();
            for (List<String> values : blockPalette.values()) {
                tokens.addAll(values);
            }
            return Set.copyOf(tokens);
        }
    }

    public record SpawnEntry(String creature, int weight, String group, List<String> conditions) {
        public SpawnEntry {
            creature = requireText(creature, "spawn creature");
            group = group == null ? "" : group;
            conditions = conditions == null ? List.of() : List.copyOf(conditions);
        }
    }

    public record Landmark(
            String id,
            List<String> preferredBiomes,
            List<String> blocks,
            String lootTable,
            String holoMapHint,
            String tutorialHook
    ) {
        public Landmark {
            id = requireText(id, "landmark id");
            Objects.requireNonNull(preferredBiomes, "preferredBiomes");
            Objects.requireNonNull(blocks, "blocks");
            preferredBiomes = List.copyOf(preferredBiomes);
            blocks = List.copyOf(blocks);
            lootTable = lootTable == null ? "" : lootTable;
            holoMapHint = requireText(holoMapHint, "holoMapHint");
            tutorialHook = requireText(tutorialHook, "tutorialHook");
        }
    }

    public record Creature(String id, List<String> biomes, String category, int health, int damage) {
        public Creature {
            id = requireText(id, "creature id");
            Objects.requireNonNull(biomes, "biomes");
            biomes = List.copyOf(biomes);
            category = category == null ? "" : category;
        }
    }

    public static Map<String, List<String>> palette(Map<String, List<String>> source) {
        LinkedHashMap<String, List<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : source.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
