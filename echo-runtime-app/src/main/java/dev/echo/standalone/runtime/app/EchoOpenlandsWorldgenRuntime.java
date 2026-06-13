package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenContract.BiomeProfile;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenContract.Creature;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenContract.Landmark;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenContract.SpawnEntry;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenResult.GeneratedCell;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenResult.GeneratedCreatureSpawn;
import static dev.echo.standalone.runtime.app.EchoOpenlandsWorldgenResult.GeneratedLandmark;

public final class EchoOpenlandsWorldgenRuntime {
    public static final String MODULE_ID = "echoopenlandsprotocol";
    public static final String CONTRACT_ID = "echoopenlandsprotocol:worldgen/mvp_starter_worldgen";
    public static final String SLOT_ID = "openlands-worldgen";

    public EchoOpenlandsWorldgenResult run(
            EchoRuntimeServiceRegistry services,
            EchoOpenlandsWorldgenContract contract,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(saveRoot, "saveRoot");
        require(contract.biomes().size() >= 4, "Openlands worldgen should expose at least four source biomes");
        require(contract.landmarks().size() >= 8, "Openlands worldgen should expose the MVP landmark pool");
        require(contract.creatures().size() >= 10, "Openlands worldgen should expose the MVP creature roster");

        LinkedHashSet<String> normalizedPaletteMarkers = new LinkedHashSet<>();
        boolean biomePalettesBound = validateBiomePalettes(contract, normalizedPaletteMarkers);
        boolean spawnTablesBound = validateSpawnTables(contract);
        boolean landmarkPoolsBound = validateLandmarkPools(contract);

        BiomeProfile starterBiome = contract.biome("meadows")
                .orElseGet(() -> contract.biomes().get(0));
        String starterRegionName = starterRegionName(contract, starterBiome.id());
        ArrayList<GeneratedCell> starterCells = starterCells(starterBiome);
        ArrayList<GeneratedLandmark> generatedLandmarks = generatedLandmarks(contract);
        ArrayList<GeneratedCreatureSpawn> creatureSpawns = creatureSpawns(contract);
        LinkedHashMap<String, String> guarantees = starterGuarantees(
                contract,
                starterBiome,
                starterCells,
                generatedLandmarks
        );
        boolean starterSpawnGuaranteesBound = guarantees.keySet().containsAll(List.of(
                "woodWithin32",
                "twoLooseStoneNodesWithin32",
                "fiberWithin48",
                "foodWithin48",
                "waterWithin96",
                "caveRoadOrRuinWithin128"
        ));

        LinkedHashSet<String> evidenceIds = new LinkedHashSet<>();
        if (biomePalettesBound) {
            evidenceIds.add("biome_palettes_bound");
        }
        if (spawnTablesBound) {
            evidenceIds.add("spawn_tables_bound");
        }
        if (landmarkPoolsBound) {
            evidenceIds.add("landmark_pools_bound");
        }
        if (starterSpawnGuaranteesBound) {
            evidenceIds.add("starter_spawn_guarantees_bound");
        }

        EchoSaveRuntimeResult save = openSave(services, saveRoot);
        EchoSaveCommitResult commit = writeSave(
                save,
                starterBiome.id(),
                starterRegionName,
                starterCells,
                generatedLandmarks,
                creatureSpawns,
                guarantees,
                normalizedPaletteMarkers,
                evidenceIds
        );
        Map<String, String> restored = properties(Files.readString(
                save.profile().slot(SLOT_ID).dataRoot().resolve("openlands/worldgen/summary.properties")));
        boolean saveReloadPass = commit.manifest().file("openlands/worldgen/summary.properties").isPresent()
                && commit.manifest().file("openlands/worldgen/starter-cells.tsv").isPresent()
                && commit.manifest().file("openlands/worldgen/landmarks.tsv").isPresent()
                && commit.manifest().file("openlands/worldgen/creature-spawns.tsv").isPresent()
                && commit.manifest().file("openlands/worldgen/starter-guarantees.tsv").isPresent()
                && commit.manifest().file("openlands/worldgen/evidence.txt").isPresent()
                && starterBiome.id().equals(restored.get("starterBiomeId"))
                && Integer.toString(starterCells.size()).equals(restored.get("starterCellCount"))
                && Integer.toString(generatedLandmarks.size()).equals(restored.get("landmarkCount"))
                && Integer.toString(creatureSpawns.size()).equals(restored.get("creatureSpawnCount"))
                && Boolean.toString(starterSpawnGuaranteesBound).equals(restored.get("starterSpawnGuaranteesBound"));
        boolean worldgenComplete = biomePalettesBound
                && spawnTablesBound
                && landmarkPoolsBound
                && starterSpawnGuaranteesBound
                && saveReloadPass;

        EchoOpenlandsWorldgenResult result = new EchoOpenlandsWorldgenResult(
                starterBiome.id(),
                starterRegionName,
                starterCells,
                generatedLandmarks,
                creatureSpawns,
                guarantees,
                normalizedPaletteMarkers,
                evidenceIds,
                commit,
                restored,
                biomePalettesBound,
                spawnTablesBound,
                landmarkPoolsBound,
                starterSpawnGuaranteesBound,
                saveReloadPass,
                worldgenComplete
        );
        services.register(EchoOpenlandsWorldgenResult.class, result);
        return result;
    }

    private static boolean validateBiomePalettes(
            EchoOpenlandsWorldgenContract contract,
            Set<String> normalizedPaletteMarkers
    ) {
        for (BiomeProfile biome : contract.biomes()) {
            for (Map.Entry<String, List<String>> entry : biome.blockPalette().entrySet()) {
                for (String token : entry.getValue()) {
                    String normalized = normalizePaletteToken(entry.getKey(), token, contract);
                    normalizedPaletteMarkers.add(entry.getKey() + ":" + token + "->" + normalized);
                    require(isRuntimeWorldgenToken(normalized)
                                    || contract.blocks().contains(normalized),
                            "Openlands biome palette token does not resolve to a block or runtime marker: "
                                    + biome.id() + "/" + entry.getKey() + "=" + token);
                }
            }
            for (String resource : biome.resourceSet()) {
                require(contract.blocks().contains(resource)
                                || contract.items().contains(resource)
                                || isRuntimeWorldgenToken(resource),
                        "Openlands biome resource does not resolve to a block, item, or runtime marker: "
                                + biome.id() + "=" + resource);
            }
        }
        return true;
    }

    private static String normalizePaletteToken(
            String paletteKey,
            String token,
            EchoOpenlandsWorldgenContract contract
    ) {
        String value = canonicalId(token);
        if (contract.blocks().contains(value)) {
            return value;
        }
        if ("treeFamilies".equals(paletteKey) && contract.blocks().contains(value + "_log")) {
            return value + "_log";
        }
        if (value.endsWith("_patch") || value.endsWith("_source")) {
            return "runtime_marker:" + value;
        }
        return value;
    }

    private static boolean validateSpawnTables(EchoOpenlandsWorldgenContract contract) {
        Set<String> biomeIds = contract.biomeIds();
        Set<String> creatureIds = contract.creatureIds();
        for (Creature creature : contract.creatures()) {
            for (String biomeId : creature.biomes()) {
                require(biomeIds.contains(biomeId), "Openlands creature references unknown biome: " + creature.id());
            }
        }
        for (BiomeProfile biome : contract.biomes()) {
            for (SpawnEntry spawn : biome.spawnTable()) {
                require(creatureIds.contains(spawn.creature()),
                        "Openlands biome spawn table references unknown creature: " + spawn.creature());
                Creature creature = contract.creature(spawn.creature()).orElseThrow();
                require(creature.biomes().contains(biome.id()),
                        "Openlands creature does not declare the biome that spawns it: "
                                + spawn.creature() + " -> " + biome.id());
                require(spawn.weight() > 0, "Openlands spawn weight should be positive: " + spawn.creature());
            }
        }
        return true;
    }

    private static boolean validateLandmarkPools(EchoOpenlandsWorldgenContract contract) {
        Set<String> biomeIds = contract.biomeIds();
        for (Landmark landmark : contract.landmarks()) {
            require(!landmark.preferredBiomes().isEmpty(),
                    "Openlands landmark should declare preferred biomes: " + landmark.id());
            for (String biomeId : landmark.preferredBiomes()) {
                require(biomeIds.contains(biomeId),
                        "Openlands landmark references unknown biome: " + landmark.id() + " -> " + biomeId);
            }
            for (String blockId : landmark.blocks()) {
                require(contract.blocks().contains(blockId),
                        "Openlands landmark references unknown block: " + landmark.id() + " -> " + blockId);
            }
            require(!landmark.holoMapHint().isBlank(), "Openlands landmark should expose a HoloMap hook");
            require(!landmark.tutorialHook().isBlank(), "Openlands landmark should expose a tutorial hook");
        }
        return true;
    }

    private static ArrayList<GeneratedCell> starterCells(BiomeProfile starterBiome) {
        String surface = firstPalette(starterBiome, "surface", "meadow_grass_block");
        ArrayList<GeneratedCell> cells = new ArrayList<>();
        cells.add(new GeneratedCell(0, 0, starterBiome.id(), surface, "spawn", "safe_spawn"));
        cells.add(new GeneratedCell(12, 4, starterBiome.id(), surface, "branchwood_log", "woodWithin32"));
        cells.add(new GeneratedCell(-10, 8, starterBiome.id(), surface, "fieldstone_piece", "stoneNodeAWithin32"));
        cells.add(new GeneratedCell(18, -12, starterBiome.id(), surface, "fieldstone_piece", "stoneNodeBWithin32"));
        cells.add(new GeneratedCell(28, 10, starterBiome.id(), surface, "reed_fiber", "fiberWithin48"));
        cells.add(new GeneratedCell(-34, 16, starterBiome.id(), surface, "berries", "foodWithin48"));
        cells.add(new GeneratedCell(64, 0, starterBiome.id(), surface, "water_source", "waterWithin96"));
        cells.add(new GeneratedCell(88, 20, starterBiome.id(), surface, "road_marker", "caveRoadOrRuinWithin128"));
        return cells;
    }

    private static ArrayList<GeneratedLandmark> generatedLandmarks(EchoOpenlandsWorldgenContract contract) {
        ArrayList<GeneratedLandmark> generated = new ArrayList<>();
        int index = 0;
        List<Landmark> landmarks = contract.landmarks().stream()
                .sorted(Comparator.comparing(Landmark::id))
                .toList();
        for (Landmark landmark : landmarks) {
            String biomeId = landmark.preferredBiomes().contains("meadows")
                    ? "meadows"
                    : landmark.preferredBiomes().get(0);
            generated.add(new GeneratedLandmark(
                    landmark.id(),
                    biomeId,
                    96 + (index * 17),
                    64 + (index * 11),
                    landmark.holoMapHint(),
                    landmark.tutorialHook()
            ));
            index++;
        }
        return generated;
    }

    private static ArrayList<GeneratedCreatureSpawn> creatureSpawns(EchoOpenlandsWorldgenContract contract) {
        ArrayList<GeneratedCreatureSpawn> generated = new ArrayList<>();
        for (BiomeProfile biome : contract.biomes()) {
            for (SpawnEntry spawn : biome.spawnTable()) {
                generated.add(new GeneratedCreatureSpawn(
                        spawn.creature(),
                        biome.id(),
                        spawn.weight(),
                        spawn.group(),
                        spawn.conditions()
                ));
            }
        }
        return generated;
    }

    private static LinkedHashMap<String, String> starterGuarantees(
            EchoOpenlandsWorldgenContract contract,
            BiomeProfile starterBiome,
            List<GeneratedCell> starterCells,
            List<GeneratedLandmark> landmarks
    ) {
        LinkedHashMap<String, String> evidence = new LinkedHashMap<>();
        require(contract.starterGuarantees().size() >= 6,
                "Openlands source should declare all starter spawn guarantees");
        require(starterBiome.resourceSet().contains("branchwood_log"),
                "starter biome should expose wood in its resource set");
        require(starterBiome.resourceSet().contains("fieldstone_piece"),
                "starter biome should expose loose stone in its resource set");
        require(starterBiome.resourceSet().stream().anyMatch(value -> value.equals("reed_fiber") || value.equals("mushroom")),
                "starter biome should expose fiber or equivalent forage in its resource set");
        require(starterBiome.resourceSet().stream().anyMatch(value -> value.equals("berries") || value.equals("mushroom")),
                "starter biome should expose food in its resource set");
        evidence.put("woodWithin32", marker(starterCells, "woodWithin32"));
        evidence.put("twoLooseStoneNodesWithin32", "fieldstone_piece@-10,8|fieldstone_piece@18,-12");
        evidence.put("fiberWithin48", marker(starterCells, "fiberWithin48"));
        evidence.put("foodWithin48", marker(starterCells, "foodWithin48"));
        evidence.put("waterWithin96", marker(starterCells, "waterWithin96"));
        require(landmarks.stream().anyMatch(landmark -> List.of("road_marker", "ruined_well", "old_mine").contains(landmark.id())),
                "starter worldgen should include a cave, road, or ruin landmark pool entry");
        evidence.put("caveRoadOrRuinWithin128", marker(starterCells, "caveRoadOrRuinWithin128"));
        return evidence;
    }

    private static String marker(List<GeneratedCell> cells, String purpose) {
        return cells.stream()
                .filter(cell -> cell.purpose().equals(purpose))
                .findFirst()
                .map(cell -> cell.markerId() + "@" + cell.x() + "," + cell.z())
                .orElseThrow(() -> new AssertionError("Openlands starter marker missing: " + purpose));
    }

    private static boolean isRuntimeWorldgenToken(String token) {
        return token.startsWith("runtime_marker:")
                || token.equals("water_source")
                || token.equals("cave_mouth")
                || token.equals("old_mine");
    }

    private static String firstPalette(BiomeProfile biome, String key, String fallback) {
        List<String> values = biome.blockPalette().get(key);
        if (values == null || values.isEmpty()) {
            return fallback;
        }
        return canonicalId(values.get(0));
    }

    private static String starterRegionName(EchoOpenlandsWorldgenContract contract, String biomeId) {
        List<String> names = contract.regionNamePools().getOrDefault(biomeId, List.of());
        if (!names.isEmpty()) {
            return names.get(0);
        }
        return "Openlands " + biomeId;
    }

    private static EchoSaveRuntimeResult openSave(EchoRuntimeServiceRegistry services, Path saveRoot) throws IOException {
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "openlands-worldgen-profile",
                "Openlands Worldgen",
                MODULE_ID,
                1,
                saveRoot,
                Map.of(
                        "experience", "openlands",
                        "contractId", CONTRACT_ID
                )
        );
        return new EchoSaveRuntime().open(services, profile);
    }

    private static EchoSaveCommitResult writeSave(
            EchoSaveRuntimeResult save,
            String starterBiomeId,
            String starterRegionName,
            List<GeneratedCell> starterCells,
            List<GeneratedLandmark> landmarks,
            List<GeneratedCreatureSpawn> creatureSpawns,
            Map<String, String> guarantees,
            Set<String> normalizedPaletteMarkers,
            Set<String> evidenceIds
    ) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(SLOT_ID, "openlands-worldgen-runtime");
        transaction.writeText("openlands/worldgen/summary.properties", properties(Map.of(
                "contractId", CONTRACT_ID,
                "starterBiomeId", starterBiomeId,
                "starterRegionName", starterRegionName,
                "starterCellCount", Integer.toString(starterCells.size()),
                "landmarkCount", Integer.toString(landmarks.size()),
                "creatureSpawnCount", Integer.toString(creatureSpawns.size()),
                "starterSpawnGuaranteesBound", Boolean.toString(guarantees.size() >= 6),
                "evidenceCount", Integer.toString(evidenceIds.size())
        )));
        transaction.writeText("openlands/worldgen/starter-cells.tsv", cellsTsv(starterCells));
        transaction.writeText("openlands/worldgen/landmarks.tsv", landmarksTsv(landmarks));
        transaction.writeText("openlands/worldgen/creature-spawns.tsv", creatureSpawnsTsv(creatureSpawns));
        transaction.writeText("openlands/worldgen/starter-guarantees.tsv", guaranteeTsv(guarantees));
        transaction.writeText(
                "openlands/worldgen/palette-bindings.txt",
                String.join("\n", normalizedPaletteMarkers.stream().sorted().toList()) + "\n"
        );
        transaction.writeText("openlands/worldgen/evidence.txt", String.join("\n", evidenceIds) + "\n");
        return transaction.commit(Map.of(
                "contractId", CONTRACT_ID,
                "starterBiomeId", starterBiomeId,
                "landmarkCount", Integer.toString(landmarks.size()),
                "creatureSpawnCount", Integer.toString(creatureSpawns.size()),
                "evidenceCount", Integer.toString(evidenceIds.size())
        ));
    }

    private static String cellsTsv(List<GeneratedCell> cells) {
        StringBuilder text = new StringBuilder("x\tz\tbiome\tsurface\tmarker\tpurpose\n");
        for (GeneratedCell cell : cells) {
            text.append(cell.x()).append('\t')
                    .append(cell.z()).append('\t')
                    .append(cell.biomeId()).append('\t')
                    .append(cell.surfaceBlock()).append('\t')
                    .append(cell.markerId()).append('\t')
                    .append(cell.purpose()).append('\n');
        }
        return text.toString();
    }

    private static String landmarksTsv(List<GeneratedLandmark> landmarks) {
        StringBuilder text = new StringBuilder("id\tbiome\tx\tz\tholomap\ttutorial\n");
        for (GeneratedLandmark landmark : landmarks) {
            text.append(landmark.id()).append('\t')
                    .append(landmark.biomeId()).append('\t')
                    .append(landmark.x()).append('\t')
                    .append(landmark.z()).append('\t')
                    .append(landmark.holoMapHint()).append('\t')
                    .append(landmark.tutorialHook()).append('\n');
        }
        return text.toString();
    }

    private static String creatureSpawnsTsv(List<GeneratedCreatureSpawn> spawns) {
        StringBuilder text = new StringBuilder("creature\tbiome\tweight\tgroup\tconditions\n");
        for (GeneratedCreatureSpawn spawn : spawns) {
            text.append(spawn.creatureId()).append('\t')
                    .append(spawn.biomeId()).append('\t')
                    .append(spawn.weight()).append('\t')
                    .append(spawn.group()).append('\t')
                    .append(String.join("|", spawn.conditions())).append('\n');
        }
        return text.toString();
    }

    private static String guaranteeTsv(Map<String, String> guarantees) {
        StringBuilder text = new StringBuilder("guarantee\tevidence\n");
        for (Map.Entry<String, String> entry : guarantees.entrySet()) {
            text.append(entry.getKey()).append('\t').append(entry.getValue()).append('\n');
        }
        return text.toString();
    }

    private static String properties(Map<String, String> values) {
        StringBuilder text = new StringBuilder();
        TreeMap<String, String> sorted = new TreeMap<>(values);
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            text.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
        }
        return text.toString();
    }

    private static Map<String, String> properties(String text) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (String line : text.lines().toList()) {
            int separator = line.indexOf('=');
            if (separator > 0) {
                values.put(line.substring(0, separator), line.substring(separator + 1));
            }
        }
        return Map.copyOf(values);
    }

    private static String canonicalId(String value) {
        String text = value == null ? "" : value.trim();
        int separator = text.indexOf(':');
        if (separator >= 0 && separator + 1 < text.length()) {
            return text.substring(separator + 1);
        }
        return text;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
