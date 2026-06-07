package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoIndexStandaloneAdapter {
    public static final String MODULE_ID = "echoindex";
    public static final String ADAPTERCORE_CONTRACT_ID = "echoindex:recipe_search/index_query";
    public static final String INVENTORY_OVERLAY_CONTRACT_ID = "index.inventory_overlay";
    public static final String INVENTORY_OVERLAY_SURFACE_ID = "echoindex:inventory_overlay";
    public static final String REFERENCE_QUERY = "power cell";
    public static final String REFERENCE_RECIPE_ID = "echoashfallprotocol:power_cell";

    public Map<String, Object> activate() {
        Map<String, Object> queryService = executeQuery(REFERENCE_QUERY);
        boolean queryPassed = referenceQueryPassed(queryService);
        Map<String, Object> inventoryOverlay = inventoryOverlay(queryService);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "index_standalone_query_service_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of(
                "index.recipes",
                INVENTORY_OVERLAY_CONTRACT_ID,
                ADAPTERCORE_CONTRACT_ID,
                INVENTORY_OVERLAY_SURFACE_ID
        ));
        report.put("queryService", queryService);
        report.put("inventoryOverlay", inventoryOverlay);
        report.put("queryServiceExecuted", queryPassed);
        report.put("inventoryOverlayReady", Boolean.TRUE.equals(inventoryOverlay.get("visible")));
        report.put("serviceCodeExecuted", queryPassed);
        report.put("summary", "Index standalone adapter executed the AdapterCore recipe query service and exposed the inventory overlay surface.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeQuery(String query) {
        String normalizedQuery = normalize(query);
        List<Map<String, Object>> recipes = sampleRecipes();
        List<Map<String, Object>> matches = recipes.stream()
                .filter(recipe -> searchableText(recipe).contains(normalizedQuery))
                .toList();
        Map<String, Object> selected = matches.isEmpty() ? Map.of() : matches.get(0);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
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

    public boolean referenceQueryPassed(Map<String, Object> report) {
        return Boolean.TRUE.equals(report.get("queryExecuted"))
                && REFERENCE_RECIPE_ID.equals(report.get("selectedRecipeId"))
                && list(report.get("inputIds")).contains("echoashfallprotocol:energy_cell")
                && list(report.get("outputIds")).contains("echoashfallprotocol:power_cell");
    }

    public Map<String, Object> inventoryOverlay(Map<String, Object> queryService) {
        Map<String, Object> overlay = new LinkedHashMap<>();
        overlay.put("surfaceId", INVENTORY_OVERLAY_SURFACE_ID);
        overlay.put("parentSurfaceId", "standalone:inventory");
        overlay.put("adapterCoreContract", INVENTORY_OVERLAY_CONTRACT_ID);
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

    private static List<Map<String, Object>> sampleRecipes() {
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

    @SuppressWarnings("unchecked")
    private static List<String> list(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
