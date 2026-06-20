package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoDataJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Loads a runtime-neutral .ECHO Content Graph from module release roots.
 *
 * <p>The loader expects each module root to contain:
 * <pre>.echo/content-graph/content-graph.json</pre>
 * and optionally:
 * <pre>.echo/content-graph/features.json
 * .echo/content-graph/export-plans/hytale.json</pre>
 *
 * <p>It exposes nodes, edges, features, and export plans so that the standalone
 * runtime can map content-graph definitions into existing runtime contracts.
 */
public final class EchoContentGraphLoader {

    private final List<Path> moduleRoots;

    public EchoContentGraphLoader(List<Path> moduleRoots) {
        this.moduleRoots = List.copyOf(moduleRoots);
    }

    public EchoContentGraphLoadResult load() throws IOException {
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        List<Map<String, Object>> features = new ArrayList<>();
        List<Map<String, Object>> hytalePlans = new ArrayList<>();
        List<Map<String, Object>> standalonePlans = new ArrayList<>();
        List<Map<String, Object>> exportPlans = new ArrayList<>();
        List<EchoCompatDiagnostic> diagnostics = new ArrayList<>();
        int exportPlanCount = 0;
        int graphCount = 0;

        for (Path root : moduleRoots) {
            LoadedContentGraph graphFiles = loadGraphFiles(root, diagnostics);
            if (graphFiles == null || graphFiles.contentGraphText() == null) {
                String expected = archive(root)
                        ? root + "!/.echo/content-graph/content-graph.json"
                        : root.resolve(".echo/content-graph/content-graph.json").toString();
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.INFO,
                        root.toString(),
                        "content_graph_missing: No content graph found at " + expected));
                continue;
            }
            Map<String, Object> graph;
            try {
                graph = parseJsonObject(graphFiles.contentGraphText());
            } catch (Exception e) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.ERROR,
                        graphFiles.contentGraphLocation(),
                        "content_graph_parse_failed: " + e.getMessage()));
                continue;
            }

            String schemaVersion = stringValue(graph.get("schemaVersion"));
            if (!"echo.content_graph.v1".equals(schemaVersion)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.WARNING,
                        graphFiles.contentGraphLocation(),
                        "content_graph_schema_unsupported: Expected echo.content_graph.v1, found " + schemaVersion));
            }

            collectList(graph, "nodes", nodes);
            collectList(graph, "edges", edges);
            graphCount++;

            if (graphFiles.featuresText() != null) {
                Map<String, Object> featureList = parseJsonObject(graphFiles.featuresText());
                collectList(featureList, "features", features);
            }

            exportPlanCount += graphFiles.exportPlanTexts().size();
            for (Map.Entry<String, String> plan : graphFiles.exportPlanTexts().entrySet()) {
                Map<String, Object> parsedPlan = parseJsonObject(plan.getValue());
                exportPlans.add(parsedPlan);
                if ("hytale".equals(plan.getKey())) {
                    hytalePlans.add(parsedPlan);
                }
                if ("echo_runtime_standalone".equals(plan.getKey())) {
                    standalonePlans.add(parsedPlan);
                }
            }
        }

        validateNodeReferences(nodes, edges, diagnostics);

        return new EchoContentGraphLoadResult(
                Collections.unmodifiableList(nodes),
                Collections.unmodifiableList(edges),
                Collections.unmodifiableList(features),
                Collections.unmodifiableList(hytalePlans),
                Collections.unmodifiableList(standalonePlans),
                Collections.unmodifiableList(exportPlans),
                exportPlanCount,
                graphCount,
                Collections.unmodifiableList(diagnostics));
    }

    private static LoadedContentGraph loadGraphFiles(
            Path root,
            List<EchoCompatDiagnostic> diagnostics
    ) throws IOException {
        if (archive(root)) {
            return loadArchiveGraphFiles(root, diagnostics);
        }
        return loadDirectoryGraphFiles(root);
    }

    private static LoadedContentGraph loadDirectoryGraphFiles(Path root) throws IOException {
        Path graphPath = root.resolve(".echo/content-graph/content-graph.json");
        String graphText = Files.isRegularFile(graphPath)
                ? Files.readString(graphPath, StandardCharsets.UTF_8)
                : null;
        Path featuresPath = root.resolve(".echo/content-graph/features.json");
        String featuresText = Files.isRegularFile(featuresPath)
                ? Files.readString(featuresPath, StandardCharsets.UTF_8)
                : null;
        TreeMap<String, String> exportPlans = new TreeMap<>();
        Path exportPlansDir = root.resolve(".echo/content-graph/export-plans");
        if (Files.isDirectory(exportPlansDir)) {
            try (var stream = Files.list(exportPlansDir)) {
                for (Path plan : stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".json"))
                        .sorted()
                        .toList()) {
                    exportPlans.put(planName(plan.getFileName().toString()),
                            Files.readString(plan, StandardCharsets.UTF_8));
                }
            }
        }
        return new LoadedContentGraph(
                graphText,
                graphPath.toString(),
                featuresText,
                Collections.unmodifiableMap(exportPlans)
        );
    }

    private static LoadedContentGraph loadArchiveGraphFiles(
            Path root,
            List<EchoCompatDiagnostic> diagnostics
    ) throws IOException {
        TreeMap<String, String> exportPlans = new TreeMap<>();
        String graphText = null;
        String featuresText = null;
        try (ZipFile zip = new ZipFile(root.toFile())) {
            ZipEntry graph = zip.getEntry(".echo/content-graph/content-graph.json");
            if (graph != null && !graph.isDirectory()) {
                graphText = new String(zip.getInputStream(graph).readAllBytes(), StandardCharsets.UTF_8);
            }
            ZipEntry features = zip.getEntry(".echo/content-graph/features.json");
            if (features != null && !features.isDirectory()) {
                featuresText = new String(zip.getInputStream(features).readAllBytes(), StandardCharsets.UTF_8);
            }
            var entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()
                        || !name.startsWith(".echo/content-graph/export-plans/")
                        || !name.endsWith(".json")) {
                    continue;
                }
                exportPlans.put(planName(name.substring(name.lastIndexOf('/') + 1)),
                        new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            diagnostics.add(new EchoCompatDiagnostic(
                    EchoCompatDiagnosticSeverity.ERROR,
                    root.toString(),
                    "content_graph_archive_read_failed: " + exception.getMessage()));
            throw exception;
        }
        return new LoadedContentGraph(
                graphText,
                root + "!/.echo/content-graph/content-graph.json",
                featuresText,
                Collections.unmodifiableMap(exportPlans)
        );
    }

    private static boolean archive(Path path) {
        if (path == null || !Files.isRegularFile(path)) {
            return false;
        }
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase();
        return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".echo-addon");
    }

    private static String planName(String fileName) {
        String name = fileName == null ? "" : fileName.trim();
        return name.endsWith(".json") ? name.substring(0, name.length() - ".json".length()) : name;
    }

    private static int countExportPlanFiles(Path exportPlansDir) throws IOException {
        if (!Files.isDirectory(exportPlansDir)) {
            return 0;
        }
        try (var stream = Files.list(exportPlansDir)) {
            return (int) stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .count();
        }
    }

    private void validateNodeReferences(
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> edges,
            List<EchoCompatDiagnostic> diagnostics) {
        Map<String, Map<String, Object>> nodeById = new LinkedHashMap<>();
        for (Map<String, Object> node : nodes) {
            String id = stringValue(node.get("id"));
            if (id != null) {
                nodeById.put(id, node);
            }
        }
        for (Map<String, Object> edge : edges) {
            String from = stringValue(edge.get("from"));
            String to = stringValue(edge.get("to"));
            EchoCompatDiagnosticSeverity unresolvedSeverity = moduleDependencyEdge(edge)
                    ? EchoCompatDiagnosticSeverity.WARNING
                    : EchoCompatDiagnosticSeverity.ERROR;
            if (from != null && !nodeById.containsKey(from)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        unresolvedSeverity,
                        stringValue(edge.get("id")),
                        "content_graph_unresolved_edge_source: Edge references missing source node " + from));
            }
            if (to != null && !nodeById.containsKey(to)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        unresolvedSeverity,
                        stringValue(edge.get("id")),
                        "content_graph_unresolved_edge_target: Edge references missing target node " + to));
            }
        }
    }

    private static boolean moduleDependencyEdge(Map<String, Object> edge) {
        String kind = stringValue(edge.get("kind"));
        String id = stringValue(edge.get("id"));
        return "module_requires_module".equals(kind)
                || (id != null && id.contains("module_requires_module"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseJsonObject(String text) {
        Object parsed = EchoDataJson.parse(text);
        if (parsed instanceof Map) {
            return (Map<String, Object>) parsed;
        }
        throw new IllegalArgumentException("Expected JSON object");
    }

    private static String stringValue(Object value) {
        return value instanceof String s ? s : null;
    }

    @SuppressWarnings("unchecked")
    private static void collectList(Map<String, Object> graph, String key, List<Map<String, Object>> target) {
        Object value = graph.get(key);
        if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    target.add((Map<String, Object>) map);
                }
            }
        }
    }

    public record EchoContentGraphLoadResult(
            List<Map<String, Object>> nodes,
            List<Map<String, Object>> edges,
            List<Map<String, Object>> features,
            List<Map<String, Object>> hytalePlans,
            List<Map<String, Object>> standalonePlans,
            List<Map<String, Object>> exportPlans,
            int exportPlanCount,
            int graphCount,
            List<EchoCompatDiagnostic> diagnostics
    ) {
        public Optional<Map<String, Object>> findNode(String id) {
            return nodes.stream()
                    .filter(n -> id.equals(stringValue(n.get("id"))))
                    .findFirst();
        }

        public long countByKind(String kind) {
            return nodes.stream()
                    .filter(n -> kind.equals(stringValue(n.get("kind"))))
                    .count();
        }

        public int unsupportedStandaloneNodeCount() {
            return standalonePlans.stream()
                    .mapToInt(EchoContentGraphLoader::unsupportedExportPlanNodeCount)
                    .sum();
        }
    }

    private static int unsupportedExportPlanNodeCount(Map<String, Object> plan) {
        Object nodes = plan == null ? null : plan.get("nodes");
        if (!(nodes instanceof List<?> list)) {
            return 0;
        }
        int count = 0;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> node)) {
                continue;
            }
            String status = stringValue(node.get("status"));
            if ("blocked".equals(status) || "unsupported".equals(status)) {
                count++;
            }
        }
        return count;
    }

    private record LoadedContentGraph(
            String contentGraphText,
            String contentGraphLocation,
            String featuresText,
            Map<String, String> exportPlanTexts
    ) {
    }
}
