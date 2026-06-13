package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.save.EchoSaveCommitResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public record EchoOpenlandsFirstHourResult(
        List<String> routeStepsCompleted,
        Map<String, Integer> inventory,
        List<String> hotbar,
        List<String> placedBlocks,
        Map<String, List<String>> chestContents,
        boolean bedrollSpawnSet,
        boolean campfireLitState,
        int shelterScore,
        String waystoneState,
        String restoredWaystoneState,
        boolean holomapRegionDiscovered,
        List<String> discoveredLandmarks,
        List<String> tutorialPromptsShown,
        Set<String> saveFieldsPersisted,
        EchoSaveCommitResult saveCommit,
        Map<String, String> restoredState,
        boolean runtimePlaytestPass,
        boolean saveReloadPass,
        boolean waystoneSaveReloadPass,
        boolean firstHourComplete
) {
    public EchoOpenlandsFirstHourResult {
        Objects.requireNonNull(routeStepsCompleted, "routeStepsCompleted");
        Objects.requireNonNull(inventory, "inventory");
        Objects.requireNonNull(hotbar, "hotbar");
        Objects.requireNonNull(placedBlocks, "placedBlocks");
        Objects.requireNonNull(chestContents, "chestContents");
        waystoneState = requireText(waystoneState, "waystoneState");
        restoredWaystoneState = requireText(restoredWaystoneState, "restoredWaystoneState");
        Objects.requireNonNull(discoveredLandmarks, "discoveredLandmarks");
        Objects.requireNonNull(tutorialPromptsShown, "tutorialPromptsShown");
        Objects.requireNonNull(saveFieldsPersisted, "saveFieldsPersisted");
        Objects.requireNonNull(saveCommit, "saveCommit");
        Objects.requireNonNull(restoredState, "restoredState");
        routeStepsCompleted = List.copyOf(routeStepsCompleted);
        inventory = Map.copyOf(new TreeMap<>(inventory));
        hotbar = List.copyOf(hotbar);
        placedBlocks = List.copyOf(placedBlocks);
        chestContents = copyChestContents(chestContents);
        discoveredLandmarks = List.copyOf(discoveredLandmarks);
        tutorialPromptsShown = List.copyOf(tutorialPromptsShown);
        saveFieldsPersisted = Set.copyOf(new TreeSet<>(saveFieldsPersisted));
        restoredState = Map.copyOf(new TreeMap<>(restoredState));
    }

    public String summary() {
        return "steps=" + routeStepsCompleted.size()
                + " inventory=" + inventory.size()
                + " placed=" + placedBlocks.size()
                + " shelter=" + shelterScore
                + " waystone=" + waystoneState
                + " restored=" + restoredWaystoneState
                + " saveFiles=" + saveCommit.filesWritten()
                + " playtest=" + runtimePlaytestPass
                + " saveReload=" + saveReloadPass;
    }

    private static Map<String, List<String>> copyChestContents(Map<String, List<String>> value) {
        TreeMap<String, List<String>> copy = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : value.entrySet()) {
            copy.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(copy);
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value;
    }
}
