package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoContentGraphLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientContentGraphRuntimeCatalog {
    private EchoClientContentGraphRuntimeCatalog() {
    }

    static List<Map<String, Object>> rows(EchoContentGraphLoader.EchoContentGraphLoadResult result) {
        if (result == null || result.nodes().isEmpty()) {
            return List.of();
        }
        Map<String, String> standaloneStatuses = standaloneStatuses(result.standalonePlans());
        Map<String, List<String>> creativeTabsByItem = creativeTabsByItem(result.nodes(), result.edges());
        LinkedHashMap<String, Map<String, Object>> rows = new LinkedHashMap<>();
        for (Map<String, Object> node : result.nodes()) {
            Map<String, Object> row = row(node, standaloneStatuses, creativeTabsByItem);
            String contentId = text(row.get("contentId"));
            if (!contentId.isBlank()) {
                rows.put(contentId, row);
            }
        }
        return List.copyOf(rows.values());
    }

    static List<String> strictValidationFailures(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            int requiredModuleRootCount
    ) {
        ArrayList<String> failures = new ArrayList<>();
        if (result == null) {
            failures.add("content graph loader did not return a result");
            return List.copyOf(failures);
        }
        if (result.graphCount() < requiredModuleRootCount) {
            failures.add("loaded " + result.graphCount() + " content graph(s) for "
                    + requiredModuleRootCount + " installed module root(s)");
        }
        if (result.standalonePlans().size() < result.graphCount()) {
            failures.add("loaded " + result.standalonePlans().size()
                    + " echo_runtime_standalone export plan(s) for "
                    + result.graphCount() + " content graph(s)");
        }
        long errors = result.diagnostics().stream()
                .filter(diagnostic -> "ERROR".equals(diagnostic.severity().name()))
                .count();
        if (errors > 0) {
            failures.add("content graph loader reported " + errors + " error diagnostic(s)");
        }
        if (result.countByKind("echo:block") <= 0) {
            failures.add("content graph exposes no block nodes");
        }
        if (result.countByKind("echo:item") <= 0) {
            failures.add("content graph exposes no item nodes");
        }
        if (result.countByKind("echo:creative_tab") <= 0) {
            failures.add("content graph exposes no creative tab nodes");
        }
        return List.copyOf(failures);
    }

    static Map<String, Object> consumptionReport(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<Map<String, Object>> rows
    ) {
        LinkedHashMap<String, Object> report = new LinkedHashMap<>();
        if (result == null) {
            report.put("schema", "echo.standalone.content_graph_consumption.v1");
            report.put("status", "MISSING");
            report.put("graphs", 0);
            report.put("rows", 0);
            return Map.copyOf(report);
        }
        report.put("schema", "echo.standalone.content_graph_consumption.v1");
        report.put("graphs", result.graphCount());
        report.put("nodes", result.nodes().size());
        report.put("edges", result.edges().size());
        report.put("features", result.features().size());
        report.put("exportPlans", result.exportPlanCount());
        report.put("standaloneExportPlans", result.standalonePlans().size());
        report.put("unsupportedStandaloneNodes", result.unsupportedStandaloneNodeCount());
        report.put("rows", rows == null ? 0 : rows.size());
        report.put("domainCounts", domainCounts(rows));
        report.put("nodeKindCounts", nodeKindCounts(result.nodes()));
        report.put("requiredCoverage", requiredCoverage(result, rows));
        List<String> readinessFailures = readinessValidationFailures(result, rows);
        report.put("readinessFailures", readinessFailures);
        report.put("status", readinessFailures.isEmpty() ? "PASS" : "BLOCKED");
        return Map.copyOf(report);
    }

    static List<String> readinessValidationFailures(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<Map<String, Object>> rows
    ) {
        ArrayList<String> failures = new ArrayList<>(strictValidationFailures(
                result,
                result == null ? 0 : result.graphCount()
        ));
        if (result == null) {
            return List.copyOf(failures);
        }
        Map<String, Integer> domains = domainCounts(rows);
        requireKind(result, failures, "echo:recipe", "content graph exposes no recipe nodes");
        requireCoverage(result, domains, failures, "loot", List.of("echo:loot", "echo:loot_table"),
                "content graph exposes no loot table nodes");
        requireCoverage(result, domains, failures, "entities", List.of("echo:entity", "echo:npc"),
                "content graph exposes no entity nodes");
        requireCoverage(result, domains, failures, "worldgen", List.of("echo:biome", "echo:region"),
                "content graph exposes no biome or region identity nodes");
        if (countAnyKind(result, List.of("echo:structure"))
                + domains.getOrDefault("structures", 0)
                + countNodesMatchingAny(result, List.of("structure")) <= 0) {
            failures.add("content graph exposes no structure nodes");
        }
        if (result.features().isEmpty() && result.countByKind("echo:feature") <= 0) {
            failures.add("content graph exposes no world feature nodes");
        }
        requireCoverage(result, domains, failures, "entities", List.of("echo:spawn_rule"),
                "content graph exposes no spawn rule nodes");
        requireCoverage(result, domains, failures, "sounds", List.of("echo:sound", "echo:sound_event"),
                "content graph exposes no sound event nodes");
        requireCoverage(result, domains, failures, "missions", List.of("echo:mission", "echo:objective"),
                "content graph exposes no mission/objective nodes");
        requireDomain(domains, failures, "terminal", "runtime catalog exposes no Terminal registrations");
        requireAnyDomain(domains, failures, List.of("lens", "holomap"),
                "runtime catalog exposes no Lens registrations");
        requireDomain(domains, failures, "index", "runtime catalog exposes no Index registrations");
        requireAnyDomain(domains, failures, List.of("ui_overlays", "hud"),
                "runtime catalog exposes no HUD/UI overlay registrations");
        return List.copyOf(failures);
    }

    private static void requireCoverage(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            Map<String, Integer> domains,
            List<String> failures,
            String domain,
            List<String> kinds,
            String message
    ) {
        if (countAnyKind(result, kinds) + domains.getOrDefault(domain, 0) <= 0) {
            failures.add(message);
        }
    }

    private static Map<String, Object> row(
            Map<String, Object> node,
            Map<String, String> standaloneStatuses,
            Map<String, List<String>> creativeTabsByItem
    ) {
        String id = text(node.get("id"));
        String kind = text(node.get("kind"));
        String moduleId = firstText(node.get("moduleId"), moduleFromId(id));
        String displayName = firstText(node.get("displayName"), displayName(id));
        Map<String, Object> data = map(node.get("data"));
        String domain = domainForKind(kind, node, data);
        String contentKind = contentKindForKind(kind, domain, node, data);
        String runtimeId = firstText(
                valueAt(node, "runtimeHints", "echo_runtime_standalone", "id"),
                valueAt(data, "echo_runtime_standalone", "id"),
                id
        );

        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "content-graph");
        metadata.put("contentGraphKind", kind);
        metadata.put("contentGraphId", id);
        metadata.put("standaloneExportStatus", standaloneStatuses.getOrDefault(id, "missing"));
        metadata.put("moduleId", moduleId);
        metadata.put("displayName", displayName);
        metadata.put("data", data);
        putIfPresent(metadata, "sourcePath", valueAt(node, "source", "path"));
        putIfPresent(metadata, "runtimeHints", node.get("runtimeHints"));
        putIfPresent(metadata, "capabilities", node.get("capabilities"));
        copyIfPresent(metadata, node, "intent");
        copyIfPresent(metadata, node, "actions");
        copyIfPresent(metadata, node, "fallbacks");
        List<String> creativeTabs = creativeTabsByItem.getOrDefault(id, List.of());
        if (!creativeTabs.isEmpty()) {
            metadata.put("creativeTabs", creativeTabs);
            metadata.put("tags", List.copyOf(creativeTabs));
        }
        if ("echo:creative_tab".equals(kind)) {
            metadata.put("creativeTab", true);
            metadata.put("titleKey", firstText(data.get("titleKey"), displayName));
            putIfPresent(metadata, "iconItem", data.get("iconItem"));
            putIfPresent(metadata, "itemIds", data.get("itemIds"));
        }

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("contentId", id);
        row.put("domain", domain);
        row.put("contentKind", contentKind);
        row.put("moduleId", moduleId);
        row.put("displayName", displayName);
        row.put("adapterKey", "content_graph." + kind.replace(':', '.') + "." + id);
        row.put("nativeLoaderId", id);
        row.put("standaloneRuntimeId", runtimeId);
        row.put("standaloneReady", standaloneReady(standaloneStatuses.get(id)));
        row.put("metadata", immutableMap(metadata));
        copyCommonData(row, data);
        return immutableMap(row);
    }

    private static void copyCommonData(LinkedHashMap<String, Object> row, Map<String, Object> data) {
        if (data.isEmpty()) {
            return;
        }
        copyIfPresent(row, data, "maxStackSize");
        copyIfPresent(row, data, "stackSize");
        copyIfPresent(row, data, "hardness");
        copyIfPresent(row, data, "texture");
        copyIfPresent(row, data, "textureId");
        copyIfPresent(row, data, "model");
        copyIfPresent(row, data, "modelId");
        copyIfPresent(row, data, "spawnBiomeTags");
        copyIfPresent(row, data, "biomeTags");
        copyIfPresent(row, data, "maxHealth");
        copyIfPresent(row, data, "movementSpeed");
        copyIfPresent(row, data, "aiProfile");
        copyIfPresent(row, data, "renderShape");
        copyIfPresent(row, data, "renderArgb");
        copyIfPresent(row, data, "biomeId");
        copyIfPresent(row, data, "structureId");
        copyIfPresent(row, data, "featureId");
        copyIfPresent(row, data, "surfaceBlockId");
        copyIfPresent(row, data, "generationBlockId");
        copyIfPresent(row, data, "placementBlockId");
        copyIfPresent(row, data, "worldgenType");
        copyIfPresent(row, data, "lootTableId");
        copyIfPresent(row, data, "spawnRules");
        copyIfPresent(row, data, "texturePath");
        copyIfPresent(row, data, "modelPath");
        copyIfPresent(row, data, "animation");
        copyIfPresent(row, data, "animationId");
    }

    private static void copyIfPresent(LinkedHashMap<String, Object> row, Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value != null) {
            row.putIfAbsent(key, value);
        }
    }

    private static void putIfPresent(LinkedHashMap<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    private static Map<String, String> standaloneStatuses(List<Map<String, Object>> plans) {
        LinkedHashMap<String, String> statuses = new LinkedHashMap<>();
        if (plans == null) {
            return Map.of();
        }
        for (Map<String, Object> plan : plans) {
            Object nodes = plan.get("nodes");
            if (!(nodes instanceof List<?> list)) {
                continue;
            }
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> node)) {
                    continue;
                }
                String nodeId = text(node.get("nodeId"));
                String status = text(node.get("status"));
                if (!nodeId.isBlank() && !status.isBlank()) {
                    statuses.put(nodeId, status);
                }
            }
        }
        return Map.copyOf(statuses);
    }

    private static Map<String, List<String>> creativeTabsByItem(
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> edges
    ) {
        LinkedHashMap<String, LinkedHashSet<String>> byItem = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            if (!"echo:creative_tab".equals(text(node.get("kind")))) {
                continue;
            }
            String tabId = text(node.get("id"));
            Object itemIds = valueAt(node, "data", "itemIds");
            if (itemIds instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    addCreativeTab(byItem, text(item), tabId);
                }
            }
        }
        for (Map<String, Object> edge : edges) {
            if (!"creative_tab_contains_item".equals(text(edge.get("kind")))) {
                continue;
            }
            addCreativeTab(byItem, text(edge.get("to")), text(edge.get("from")));
        }
        LinkedHashMap<String, List<String>> result = new LinkedHashMap<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : byItem.entrySet()) {
            result.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(result);
    }

    private static void addCreativeTab(
            LinkedHashMap<String, LinkedHashSet<String>> byItem,
            String itemId,
            String tabId
    ) {
        if (itemId.isBlank() || tabId.isBlank()) {
            return;
        }
        byItem.computeIfAbsent(itemId, ignored -> new LinkedHashSet<>()).add(tabId);
    }

    private static Map<String, Integer> domainCounts(List<Map<String, Object>> rows) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                String domain = text(row.get("domain"));
                if (!domain.isBlank()) {
                    counts.merge(domain, 1, Integer::sum);
                }
            }
        }
        return Map.copyOf(counts);
    }

    private static Map<String, Integer> nodeKindCounts(List<Map<String, Object>> nodes) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        if (nodes != null) {
            for (Map<String, Object> node : nodes) {
                String kind = text(node.get("kind"));
                if (!kind.isBlank()) {
                    counts.merge(kind, 1, Integer::sum);
                }
            }
        }
        return Map.copyOf(counts);
    }

    private static Map<String, Object> requiredCoverage(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<Map<String, Object>> rows
    ) {
        LinkedHashMap<String, Object> coverage = new LinkedHashMap<>();
        Map<String, Integer> domains = domainCounts(rows);
        putCoverage(coverage, "blocks", result.countByKind("echo:block"));
        putCoverage(coverage, "items", result.countByKind("echo:item"));
        putCoverage(coverage, "creativeTabs", result.countByKind("echo:creative_tab"));
        putCoverage(coverage, "recipes", result.countByKind("echo:recipe"));
        putCoverage(coverage, "loot", countAnyKind(result, List.of("echo:loot", "echo:loot_table"))
                + countNodesWithAnyKey(result, List.of("echo:block", "echo:item"),
                List.of("lootTable", "lootTableId", "drops"))
                + domains.getOrDefault("loot", 0));
        putCoverage(coverage, "entities", countAnyKind(result, List.of("echo:entity", "echo:npc"))
                + domains.getOrDefault("entities", 0));
        putCoverage(coverage, "entityVisuals", countNodesWithAnyKey(
                result,
                List.of("echo:entity", "echo:npc"),
                List.of("model", "modelId", "modelPath", "texture", "textureId", "texturePath",
                        "animation", "animationId")
        ));
        putCoverage(coverage, "spawnRules", countAnyKind(result, List.of("echo:spawn_rule"))
                + countNodesWithAnyKey(result, List.of("echo:entity", "echo:npc"),
                List.of("spawnRules", "spawnBiomeTags"))
                + countNodesMatchingAny(result, List.of("spawn"))
                + domains.getOrDefault("entities", 0));
        putCoverage(coverage, "biomesOrRegions", countAnyKind(result, List.of("echo:biome", "echo:region"))
                + countNodesMatchingAny(result, List.of("biome", "region"))
                + domains.getOrDefault("worldgen", 0));
        putCoverage(coverage, "structures", result.countByKind("echo:structure")
                + countNodesMatchingAny(result, List.of("structure"))
                + domains.getOrDefault("structures", 0));
        putCoverage(coverage, "features", result.countByKind("echo:feature") + result.features().size());
        putCoverage(coverage, "sounds", countAnyKind(result, List.of("echo:sound", "echo:sound_event"))
                + domains.getOrDefault("sounds", 0));
        putCoverage(coverage, "missions", countAnyKind(result, List.of("echo:mission", "echo:objective"))
                + domains.getOrDefault("missions", 0));
        putCoverage(coverage, "terminal", domains.getOrDefault("terminal", 0));
        putCoverage(coverage, "lens", domains.getOrDefault("lens", 0)
                + domains.getOrDefault("holomap", 0));
        putCoverage(coverage, "index", domains.getOrDefault("index", 0));
        putCoverage(coverage, "hudOrOverlays", domains.getOrDefault("ui_overlays", 0)
                + domains.getOrDefault("hud", 0));
        return Map.copyOf(coverage);
    }

    private static void putCoverage(LinkedHashMap<String, Object> coverage, String key, long count) {
        LinkedHashMap<String, Object> value = new LinkedHashMap<>();
        value.put("status", count > 0 ? "LOADED" : "MISSING");
        value.put("count", count);
        coverage.put(key, Map.copyOf(value));
    }

    private static boolean standaloneReady(String status) {
        String normalized = text(status).toLowerCase(Locale.ROOT);
        return normalized.isBlank()
                || normalized.equals("direct")
                || normalized.equals("adapter_required")
                || normalized.equals("fallback");
    }

    private static String domainForKind(String kind, Map<String, Object> node, Map<String, Object> data) {
        return switch (text(kind)) {
            case "echo:block" -> "blocks";
            case "echo:item" -> "items";
            case "echo:creative_tab" -> "inventory";
            case "echo:recipe" -> "recipes";
            case "echo:loot", "echo:loot_table" -> "loot";
            case "echo:entity" -> "entities";
            case "echo:npc" -> "creatures";
            case "echo:biome" -> "biomes";
            case "echo:region", "echo:feature" -> "worldgen";
            case "echo:structure" -> "structures";
            case "echo:mission", "echo:objective" -> "missions";
            case "echo:ui_intent" -> uiDomain(node, data);
            case "echo:effect", "echo:hazard" -> "hazards";
            case "echo:sound", "echo:sound_event" -> "sounds";
            case "echo:spawn_rule" -> "entities";
            case "echo:asset" -> "assets";
            case "echo:system", "echo:setting" -> capabilityDomain(node, data);
            default -> "diagnostics";
        };
    }

    private static String capabilityDomain(Map<String, Object> node, Map<String, Object> data) {
        String haystack = searchable(node, data);
        if (haystack.contains("terminal")) {
            return "terminal";
        }
        if (haystack.contains("lens") || haystack.contains("scanner")) {
            return "lens";
        }
        if (haystack.contains("holomap") || haystack.contains("map")) {
            return "holomap";
        }
        if (haystack.contains("index") || haystack.contains("catalog")) {
            return "index";
        }
        if (haystack.contains("hud") || haystack.contains("overlay")) {
            return "ui_overlays";
        }
        if (haystack.contains("sound") || haystack.contains("audio") || haystack.contains("music")) {
            return "sounds";
        }
        if (haystack.contains("mission") || haystack.contains("objective")) {
            return "missions";
        }
        if (haystack.contains("spawn") || haystack.contains("creature") || haystack.contains("entity")) {
            return "entities";
        }
        if (haystack.contains("region") || haystack.contains("biome") || haystack.contains("world")) {
            return "worldgen";
        }
        if (haystack.contains("structure")) {
            return "structures";
        }
        if (haystack.contains("loot")) {
            return "loot";
        }
        return "diagnostics";
    }

    private static String uiDomain(Map<String, Object> node, Map<String, Object> data) {
        String haystack = searchable(node, data);
        if (haystack.contains("terminal")) {
            return "terminal";
        }
        if (haystack.contains("lens") || haystack.contains("observe")) {
            return "lens";
        }
        if (haystack.contains("index") || haystack.contains("catalog")) {
            return "index";
        }
        if (haystack.contains("hud") || haystack.contains("overlay")) {
            return "ui_overlays";
        }
        return "ui_screens";
    }

    private static String contentKindForKind(
            String kind,
            String domain,
            Map<String, Object> node,
            Map<String, Object> data
    ) {
        return switch (text(kind)) {
            case "echo:block" -> "BLOCK";
            case "echo:item" -> "ITEM";
            case "echo:recipe" -> "RECIPE";
            case "echo:loot", "echo:loot_table" -> "LOOT_TABLE";
            case "echo:entity", "echo:npc" -> "ENTITY";
            case "echo:structure" -> "STRUCTURE";
            case "echo:feature" -> "FEATURE";
            case "echo:biome" -> "WORLDGEN_DEFINITION";
            case "echo:region" -> "WORLD_REGION";
            case "echo:mission", "echo:objective" -> "MISSION";
            case "echo:ui_intent" -> "UI_SCREEN";
            case "echo:effect", "echo:hazard" -> "WORLD_HAZARD";
            case "echo:sound", "echo:sound_event" -> "SOUND_EVENT";
            default -> switch (text(domain)) {
                case "sounds" -> "SOUND_EVENT";
                case "missions" -> "MISSION";
                case "entities" -> "ENTITY";
                case "worldgen" -> "WORLDGEN_DEFINITION";
                case "structures" -> "STRUCTURE";
                case "assets" -> "DIAGNOSTIC";
                case "inventory" -> "DIAGNOSTIC";
                default -> "DIAGNOSTIC";
            };
        };
    }

    private static void requireKind(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<String> failures,
            String kind,
            String message
    ) {
        if (result.countByKind(kind) <= 0) {
            failures.add(message);
        }
    }

    private static void requireAnyKind(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<String> failures,
            List<String> kinds,
            String message
    ) {
        if (countAnyKind(result, kinds) <= 0) {
            failures.add(message);
        }
    }

    private static long countAnyKind(EchoContentGraphLoader.EchoContentGraphLoadResult result, List<String> kinds) {
        long count = 0;
        for (String kind : kinds) {
            count += result.countByKind(kind);
        }
        return count;
    }

    private static long countNodesMatchingAny(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<String> terms
    ) {
        long count = 0;
        for (Map<String, Object> node : result.nodes()) {
            if (containsAny(searchable(node, map(node.get("data"))), terms)) {
                count++;
            }
        }
        return count;
    }

    private static String searchable(Map<String, Object> node, Map<String, Object> data) {
        return String.join(" ",
                text(node.get("id")),
                text(node.get("moduleId")),
                text(node.get("displayName")),
                text(node.get("intent")),
                text(data.get("intent")),
                text(data.get("screenId")),
                text(data.get("route")),
                text(data.get("capability")),
                text(data.get("settingType")),
                text(data.get("assetKind"))
        ).toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String value, List<String> terms) {
        for (String term : terms) {
            if (value.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static long countNodesWithAnyKey(
            EchoContentGraphLoader.EchoContentGraphLoadResult result,
            List<String> kinds,
            List<String> keys
    ) {
        long count = 0;
        for (Map<String, Object> node : result.nodes()) {
            String kind = text(node.get("kind"));
            if (!kinds.contains(kind)) {
                continue;
            }
            Map<String, Object> data = map(node.get("data"));
            for (String key : keys) {
                if (node.get(key) != null || data.get(key) != null) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }

    private static void requireDomain(
            Map<String, Integer> domains,
            List<String> failures,
            String domain,
            String message
    ) {
        if (domains.getOrDefault(domain, 0) <= 0) {
            failures.add(message);
        }
    }

    private static void requireAnyDomain(
            Map<String, Integer> domains,
            List<String> failures,
            List<String> requestedDomains,
            String message
    ) {
        int count = 0;
        for (String domain : requestedDomains) {
            count += domains.getOrDefault(domain, 0);
        }
        if (count <= 0) {
            failures.add(message);
        }
    }

    private static String moduleFromId(String id) {
        int separator = text(id).indexOf(':');
        return separator > 0 ? id.substring(0, separator) : "";
    }

    private static String displayName(String id) {
        String value = text(id);
        int separator = value.indexOf(':');
        if (separator >= 0 && separator + 1 < value.length()) {
            value = value.substring(separator + 1);
        }
        value = value.replace('/', ' ').replace('_', ' ').replace('-', ' ').trim();
        if (value.isBlank()) {
            return "Runtime Content";
        }
        StringBuilder builder = new StringBuilder();
        for (String part : value.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1));
            }
        }
        return builder.isEmpty() ? "Runtime Content" : builder.toString();
    }

    private static String firstText(Object... values) {
        if (values == null) {
            return "";
        }
        for (Object value : values) {
            String text = text(value);
            if (!text.isBlank()) {
                return text;
            }
        }
        return "";
    }

    private static Object valueAt(Object root, String... keys) {
        Object value = root;
        for (String key : keys) {
            if (!(value instanceof Map<?, ?> map)) {
                return null;
            }
            value = map.get(key);
        }
        return value;
    }

    private static Map<String, Object> map(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Map<String, Object> immutableMap(LinkedHashMap<String, Object> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
