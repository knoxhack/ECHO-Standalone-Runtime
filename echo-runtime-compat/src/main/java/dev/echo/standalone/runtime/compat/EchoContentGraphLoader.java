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
        List<EchoCompatDiagnostic> diagnostics = new ArrayList<>();
        int exportPlanCount = 0;
        int graphCount = 0;

        for (Path root : moduleRoots) {
            Path graphPath = root.resolve(".echo/content-graph/content-graph.json");
            if (!Files.isRegularFile(graphPath)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.INFO,
                        root.toString(),
                        "content_graph_missing: No content graph found at " + graphPath));
                continue;
            }
            String text = Files.readString(graphPath, StandardCharsets.UTF_8);
            Map<String, Object> graph;
            try {
                graph = parseJsonObject(text);
            } catch (Exception e) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.ERROR,
                        graphPath.toString(),
                        "content_graph_parse_failed: " + e.getMessage()));
                continue;
            }

            String schemaVersion = stringValue(graph.get("schemaVersion"));
            if (!"echo.content_graph.v1".equals(schemaVersion)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.WARNING,
                        graphPath.toString(),
                        "content_graph_schema_unsupported: Expected echo.content_graph.v1, found " + schemaVersion));
            }

            collectList(graph, "nodes", nodes);
            collectList(graph, "edges", edges);
            graphCount++;

            Path featuresPath = root.resolve(".echo/content-graph/features.json");
            if (Files.isRegularFile(featuresPath)) {
                Map<String, Object> featureList = parseJsonObject(Files.readString(featuresPath, StandardCharsets.UTF_8));
                collectList(featureList, "features", features);
            }

            Path hytalePath = root.resolve(".echo/content-graph/export-plans/hytale.json");
            exportPlanCount += countExportPlanFiles(root.resolve(".echo/content-graph/export-plans"));
            if (Files.isRegularFile(hytalePath)) {
                hytalePlans.add(parseJsonObject(Files.readString(hytalePath, StandardCharsets.UTF_8)));
            }
        }

        validateNodeReferences(nodes, edges, diagnostics);

        return new EchoContentGraphLoadResult(
                Collections.unmodifiableList(nodes),
                Collections.unmodifiableList(edges),
                Collections.unmodifiableList(features),
                Collections.unmodifiableList(hytalePlans),
                exportPlanCount,
                graphCount,
                Collections.unmodifiableList(diagnostics));
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
            if (from != null && !nodeById.containsKey(from)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.ERROR,
                        stringValue(edge.get("id")),
                        "content_graph_unresolved_edge_source: Edge references missing source node " + from));
            }
            if (to != null && !nodeById.containsKey(to)) {
                diagnostics.add(new EchoCompatDiagnostic(
                        EchoCompatDiagnosticSeverity.ERROR,
                        stringValue(edge.get("id")),
                        "content_graph_unresolved_edge_target: Edge references missing target node " + to));
            }
        }
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
    }
}
