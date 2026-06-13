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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

public final class EchoOpenlandsFirstHourRuntime {
    public static final String MODULE_ID = "echoopenlandsprotocol";
    public static final String CONTRACT_ID = "echoopenlandsprotocol:playtests/mvp_first_hour_acceptance";
    public static final String SLOT_ID = "openlands-first-hour";
    private static final List<String> EXPECTED_ROUTE = List.of(
            "safe_spawn",
            "first_gathering",
            "first_tools",
            "first_shelter",
            "sleep_and_recover",
            "first_exploration_hook",
            "first_waystone"
    );

    public EchoOpenlandsFirstHourResult run(
            EchoRuntimeServiceRegistry services,
            EchoOpenlandsFirstHourContract contract,
            Path saveRoot
    ) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(contract, "contract");
        Objects.requireNonNull(saveRoot, "saveRoot");
        require(contract.routeSteps().equals(EXPECTED_ROUTE), "Openlands route order should match the first-hour MVP contract");

        LinkedHashMap<String, Integer> inventory = new LinkedHashMap<>();
        ArrayList<String> hotbar = new ArrayList<>();
        ArrayList<String> placedBlocks = new ArrayList<>();
        LinkedHashMap<String, List<String>> chestContents = new LinkedHashMap<>();
        ArrayList<String> discoveredLandmarks = new ArrayList<>();
        ArrayList<String> tutorialPrompts = new ArrayList<>();
        ArrayList<String> completed = new ArrayList<>();
        LinkedHashSet<String> saveFields = new LinkedHashSet<>();

        require(contract.biomes().containsAll(List.of("meadows", "woodlands")),
                "Openlands spawn should be backed by starter biomes");
        require(contract.creatures().containsAll(List.of("hare", "deer")),
                "Openlands starter spawn should be backed by passive creature data");
        addAll(discoveredLandmarks, "road_marker", "ruined_well", "tiny_camp", "broken_waystone_site");
        add(inventory, "branchwood_stick", 4);
        add(inventory, "fieldstone_piece", 5);
        add(inventory, "reed_fiber", 6);
        add(inventory, "berries", 3);
        addAll(hotbar, "branchwood_stick", "fieldstone_piece", "reed_fiber", "berries");
        completed.add("safe_spawn");

        add(inventory, "flint_shard", 2);
        addAll(tutorialPrompts, "first_stick", "first_stone", "first_fiber");
        completed.add("first_gathering");

        craft(contract, inventory, "fiber_binding", 2);
        craft(contract, inventory, "crude_axe", 1);
        craft(contract, inventory, "crude_pick", 1);
        craft(contract, inventory, "crude_spade", 1);
        craft(contract, inventory, "flint_knife", 1);
        craft(contract, inventory, "torch_bundle", 1);
        add(inventory, "pitch", 1);
        addAll(hotbar, "crude_axe", "crude_pick", "flint_knife", "torch_bundle");
        addAll(tutorialPrompts, "first_tool_craft");
        completed.add("first_tools");

        addAll(placedBlocks,
                "branchwood_planks",
                "wooden_door",
                "bedroll_block",
                "campfire",
                "torch",
                "chest",
                "thatch_roof");
        add(inventory, "bedroll", 1);
        add(inventory, "hide", 1);
        chestContents.put("starter_chest", List.of("berries", "torch_bundle", "fieldstone_piece"));
        boolean bedrollSpawnSet = true;
        boolean campfireLitState = true;
        int shelterScore = 65;
        completed.add("first_shelter");

        add(inventory, "small_pack", 1);
        completed.add("sleep_and_recover");

        addAll(discoveredLandmarks, "old_mine", "cellar_entrance");
        boolean holomapRegionDiscovered = true;
        completed.add("first_exploration_hook");

        add(inventory, "repair_kit", 1);
        craft(contract, inventory, "region_rubbing", 1);
        addAll(placedBlocks, "broken_waystone", "waystone_plinth", "old_road_marker");
        String waystoneState = "stone_repaired";
        completed.add("first_waystone");

        saveFields.addAll(contract.saveFields());
        EchoSaveRuntimeResult save = openSave(services, saveRoot);
        EchoSaveCommitResult commit = writeSave(
                save,
                completed,
                inventory,
                hotbar,
                placedBlocks,
                chestContents,
                bedrollSpawnSet,
                campfireLitState,
                shelterScore,
                waystoneState,
                holomapRegionDiscovered,
                discoveredLandmarks,
                tutorialPrompts,
                saveFields
        );
        Map<String, String> restored = properties(Files.readString(
                save.profile().slot(SLOT_ID).dataRoot().resolve("openlands/first-hour.properties")));
        String restoredWaystoneState = restored.getOrDefault("waystoneState", "missing");

        boolean runtimePlaytestPass = completed.equals(contract.routeSteps())
                && contract.playtestScenarios().containsAll(contract.routeSteps())
                && contract.items().containsAll(inventory.keySet())
                && contract.blocks().containsAll(placedBlocks)
                && contract.landmarks().containsAll(discoveredLandmarks)
                && contract.tutorialPrompts().containsAll(tutorialPrompts)
                && contract.waystoneStates().contains(waystoneState)
                && shelterScore >= 55;
        boolean saveReloadPass = commit.manifest().file("openlands/first-hour.properties").isPresent()
                && commit.manifest().file("openlands/inventory.tsv").isPresent()
                && commit.manifest().file("openlands/placed-blocks.txt").isPresent()
                && commit.manifest().file("openlands/chest.tsv").isPresent()
                && commit.manifest().file("openlands/holomap.properties").isPresent()
                && commit.manifest().file("openlands/waystone.properties").isPresent()
                && saveFields.containsAll(contract.saveFields())
                && Boolean.toString(bedrollSpawnSet).equals(restored.get("bedrollSpawn"))
                && Boolean.toString(campfireLitState).equals(restored.get("campfireLitState"))
                && Integer.toString(shelterScore).equals(restored.get("shelterScore"))
                && Boolean.toString(holomapRegionDiscovered).equals(restored.get("holomapRegionDiscovery"));
        boolean waystoneSaveReloadPass = waystoneState.equals(restoredWaystoneState);
        boolean firstHourComplete = runtimePlaytestPass
                && saveReloadPass
                && waystoneSaveReloadPass
                && completed.size() == EXPECTED_ROUTE.size();

        EchoOpenlandsFirstHourResult result = new EchoOpenlandsFirstHourResult(
                completed,
                inventory,
                hotbar,
                placedBlocks,
                chestContents,
                bedrollSpawnSet,
                campfireLitState,
                shelterScore,
                waystoneState,
                restoredWaystoneState,
                holomapRegionDiscovered,
                discoveredLandmarks,
                tutorialPrompts,
                saveFields,
                commit,
                restored,
                runtimePlaytestPass,
                saveReloadPass,
                waystoneSaveReloadPass,
                firstHourComplete
        );
        services.register(EchoOpenlandsFirstHourResult.class, result);
        return result;
    }

    private static EchoSaveRuntimeResult openSave(EchoRuntimeServiceRegistry services, Path saveRoot) throws IOException {
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "openlands-first-hour-profile",
                "Openlands First Hour",
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
            List<String> completed,
            Map<String, Integer> inventory,
            List<String> hotbar,
            List<String> placedBlocks,
            Map<String, List<String>> chestContents,
            boolean bedrollSpawnSet,
            boolean campfireLitState,
            int shelterScore,
            String waystoneState,
            boolean holomapRegionDiscovered,
            List<String> discoveredLandmarks,
            List<String> tutorialPrompts,
            Set<String> saveFields
    ) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(SLOT_ID, "openlands-first-hour-runtime");
        transaction.writeText("openlands/first-hour.properties", properties(Map.of(
                "contractId", CONTRACT_ID,
                "routeStepsCompleted", Integer.toString(completed.size()),
                "firstHourComplete", Boolean.toString(completed.equals(EXPECTED_ROUTE)),
                "bedrollSpawn", Boolean.toString(bedrollSpawnSet),
                "campfireLitState", Boolean.toString(campfireLitState),
                "shelterScore", Integer.toString(shelterScore),
                "waystoneState", waystoneState,
                "holomapRegionDiscovery", Boolean.toString(holomapRegionDiscovered),
                "tutorialPromptCount", Integer.toString(tutorialPrompts.size())
        )));
        transaction.writeText("openlands/inventory.tsv", inventoryTsv(inventory));
        transaction.writeText("openlands/hotbar.txt", String.join("\n", hotbar) + "\n");
        transaction.writeText("openlands/placed-blocks.txt", String.join("\n", placedBlocks) + "\n");
        transaction.writeText("openlands/chest.tsv", chestTsv(chestContents));
        transaction.writeText("openlands/holomap.properties", properties(Map.of(
                "regionId", "starter_meadows",
                "displayName", "Starter Meadows",
                "oldRoadSegments", "1",
                "restoredWaystones", waystoneState.equals("stone_repaired") ? "1" : "0",
                "nearbyHints", Integer.toString(discoveredLandmarks.size())
        )));
        transaction.writeText("openlands/waystone.properties", properties(Map.of(
                "waystoneId", "starter_waystone_001",
                "regionId", "starter_meadows",
                "state", waystoneState,
                "repairContributorIds", "player-001",
                "linkedRouteIds", "old_road_starter"
        )));
        transaction.writeText("openlands/save-fields.txt", String.join("\n", saveFields) + "\n");
        return transaction.commit(Map.of(
                "contractId", CONTRACT_ID,
                "routeStepsCompleted", Integer.toString(completed.size()),
                "waystoneState", waystoneState,
                "saveFieldCount", Integer.toString(saveFields.size())
        ));
    }

    private static void craft(
            EchoOpenlandsFirstHourContract contract,
            Map<String, Integer> inventory,
            String recipeId,
            int quantity
    ) {
        require(contract.recipes().contains(recipeId), "Openlands recipe missing from source contract: " + recipeId);
        require(contract.items().contains(recipeId), "Openlands recipe output missing from item contract: " + recipeId);
        add(inventory, recipeId, quantity);
    }

    private static void add(Map<String, Integer> inventory, String id, int quantity) {
        inventory.merge(id, quantity, Integer::sum);
    }

    private static void addAll(List<String> target, String... values) {
        for (String value : values) {
            if (!target.contains(value)) {
                target.add(value);
            }
        }
    }

    private static String inventoryTsv(Map<String, Integer> inventory) {
        StringBuilder text = new StringBuilder("item\tquantity\n");
        for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
            text.append(entry.getKey()).append('\t').append(entry.getValue()).append('\n');
        }
        return text.toString();
    }

    private static String chestTsv(Map<String, List<String>> chestContents) {
        StringBuilder text = new StringBuilder("chest\titem\n");
        for (Map.Entry<String, List<String>> entry : chestContents.entrySet()) {
            for (String item : entry.getValue()) {
                text.append(entry.getKey()).append('\t').append(item).append('\n');
            }
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

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
