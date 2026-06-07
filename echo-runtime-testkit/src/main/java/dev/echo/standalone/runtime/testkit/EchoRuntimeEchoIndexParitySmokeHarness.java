package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoIndexStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimeEchoIndexParitySmokeHarness {
    private EchoRuntimeEchoIndexParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeQuery = executeNativeReferenceQuery(EchoIndexStandaloneAdapter.REFERENCE_QUERY);
        EchoIndexStandaloneAdapter standaloneAdapter = new EchoIndexStandaloneAdapter();
        Map<String, Object> standaloneQuery = standaloneAdapter.executeQuery(EchoIndexStandaloneAdapter.REFERENCE_QUERY);
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();
        Map<String, Object> nativeOverlay = nativeInventoryOverlay(nativeQuery);
        Map<String, Object> standaloneOverlay = object(standaloneActivation.get("inventoryOverlay"));

        require(nativeReferenceQueryPassed(nativeQuery),
                "native Index reference query should resolve power cell recipe");
        require(standaloneAdapter.referenceQueryPassed(standaloneQuery),
                "standalone Index query should resolve power cell recipe");
        require(standaloneActivation.get("queryServiceExecuted").equals(Boolean.TRUE),
                "standalone activation should execute query service");
        require(Boolean.TRUE.equals(standaloneActivation.get("inventoryOverlayReady")),
                "standalone activation should expose the inventory overlay");
        require(nativeQuery.get("adapterCoreContract").equals(standaloneQuery.get("adapterCoreContract")),
                "native and standalone query contracts should match");
        require(nativeQuery.get("selectedRecipeId").equals(standaloneQuery.get("selectedRecipeId")),
                "native and standalone selected recipe should match");
        require(nativeQuery.get("resultIds").equals(standaloneQuery.get("resultIds")),
                "native and standalone result ids should match");
        require(nativeQuery.get("inputIds").equals(standaloneQuery.get("inputIds")),
                "native and standalone inputs should match");
        require(nativeQuery.get("outputIds").equals(standaloneQuery.get("outputIds")),
                "native and standalone outputs should match");
        require(nativeOverlay.get("surfaceId").equals(standaloneOverlay.get("surfaceId")),
                "native and standalone inventory overlay surfaces should match");
        require(nativeOverlay.get("focusedControl").equals(standaloneOverlay.get("focusedControl")),
                "native and standalone inventory overlay focus controls should match");
        require(nativeOverlay.get("actions").equals(standaloneOverlay.get("actions")),
                "native and standalone inventory overlay actions should match");
        require(nativeOverlay.get("recipeRows").equals(standaloneOverlay.get("recipeRows")),
                "native and standalone inventory overlay recipe rows should match");
        require(Boolean.TRUE.equals(nativeOverlay.get("visible")) && Boolean.TRUE.equals(standaloneOverlay.get("visible")),
                "native and standalone inventory overlays should be visible");

        System.out.println("echoindex parity smoke PASS contract="
                + nativeQuery.get("adapterCoreContract")
                + " overlay=" + nativeOverlay.get("surfaceId")
                + " selected="
                + nativeQuery.get("selectedRecipeId")
                + " results="
                + ((List<?>) nativeQuery.get("resultIds")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceQuery(String query) {
        String normalizedQuery = normalize(query);
        List<Map<String, Object>> recipes = nativeReferenceRecipes();
        List<Map<String, Object>> matches = recipes.stream()
                .filter(recipe -> searchableText(recipe).contains(normalizedQuery))
                .toList();
        Map<String, Object> selected = matches.isEmpty() ? Map.of() : matches.get(0);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("adapterCoreContract", EchoIndexStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        report.put("service", "echoindex:index_service");
        report.put("query", normalizedQuery);
        report.put("queryExecuted", true);
        report.put("matchedCount", matches.size());
        report.put("resultIds", matches.stream().map(recipe -> String.valueOf(recipe.get("id"))).toList());
        report.put("selectedRecipeId", String.valueOf(selected.getOrDefault("id", "")));
        report.put("selectedTitle", String.valueOf(selected.getOrDefault("title", "")));
        report.put("inputIds", list(selected.get("inputs")));
        report.put("outputIds", list(selected.get("outputs")));
        report.put("referenceBehavior", "recipe_query_resolves_power_cell");
        return Map.copyOf(report);
    }

    private static boolean nativeReferenceQueryPassed(Map<String, Object> report) {
        return Boolean.TRUE.equals(report.get("queryExecuted"))
                && EchoIndexStandaloneAdapter.REFERENCE_RECIPE_ID.equals(report.get("selectedRecipeId"))
                && list(report.get("inputIds")).contains("echoashfallprotocol:energy_cell")
                && list(report.get("outputIds")).contains("echoashfallprotocol:power_cell");
    }

    private static Map<String, Object> nativeInventoryOverlay(Map<String, Object> queryService) {
        Map<String, Object> overlay = new LinkedHashMap<>();
        overlay.put("surfaceId", EchoIndexStandaloneAdapter.INVENTORY_OVERLAY_SURFACE_ID);
        overlay.put("parentSurfaceId", "minecraft:container_screen");
        overlay.put("adapterCoreContract", EchoIndexStandaloneAdapter.INVENTORY_OVERLAY_CONTRACT_ID);
        overlay.put("service", "echoindex:inventory_overlay");
        overlay.put("focusedControl", "index:overlay_search");
        overlay.put("actions", List.of(
                "index.inventory_overlay_render",
                "index.inventory_overlay_input",
                "index.open_recipes_for_item",
                "index.open_usages_for_item",
                "index.toggle_favorite"
        ));
        overlay.put("recipeRows", list(queryService.get("resultIds")));
        overlay.put("visible", true);
        overlay.put("referenceBehavior", "inventory_screen_renders_index_side_drawer");
        return Map.copyOf(overlay);
    }

    private static List<Map<String, Object>> nativeReferenceRecipes() {
        return List.of(
                recipe(
                        "echoashfallprotocol:power_cell",
                        "Power Cell",
                        List.of("echoashfallprotocol:energy_cell", "minecraft:copper_ingot", "minecraft:redstone"),
                        List.of("echoashfallprotocol:power_cell")
                ),
                recipe(
                        "echoashfallprotocol:clean_water",
                        "Clean Water",
                        List.of("echoashfallprotocol:dirty_water", "minecraft:charcoal"),
                        List.of("echoashfallprotocol:clean_water")
                ),
                recipe(
                        "echoashfallprotocol:gas_mask",
                        "Gas Mask",
                        List.of("minecraft:leather", "minecraft:glass", "echoashfallprotocol:filter"),
                        List.of("echoashfallprotocol:gas_mask")
                )
        );
    }

    private static Map<String, Object> recipe(
            String id,
            String title,
            List<String> inputs,
            List<String> outputs
    ) {
        Map<String, Object> recipe = new LinkedHashMap<>();
        recipe.put("id", id);
        recipe.put("title", title);
        recipe.put("inputs", List.copyOf(inputs));
        recipe.put("outputs", List.copyOf(outputs));
        return Map.copyOf(recipe);
    }

    private static String searchableText(Map<String, Object> recipe) {
        return normalize(recipe.get("id") + " "
                + recipe.get("title") + " "
                + String.join(" ", list(recipe.get("inputs"))) + " "
                + String.join(" ", list(recipe.get("outputs"))));
    }

    private static String normalize(Object value) {
        return String.valueOf(value).toLowerCase(Locale.ROOT).replace('_', ' ').trim();
    }

    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }
}
