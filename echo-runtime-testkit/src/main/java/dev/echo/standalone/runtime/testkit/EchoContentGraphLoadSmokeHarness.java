package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoContentGraphLoader;
import dev.echo.standalone.runtime.data.EchoDataJson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Smoke harness for .ECHO Content Graph loading in the standalone runtime.
 *
 * <p>Expects a single command-line argument: the path to the ECHO-Modules repository root.
 * Loads generated content graphs from module release outputs and asserts that the expected
 * nodes, edges, features, and Hytale export plans are present.
 */
public final class EchoContentGraphLoadSmokeHarness {

    private EchoContentGraphLoadSmokeHarness() {
    }

    private static Path findFirstVersionDir(Path releaseDir) throws IOException {
        if (!Files.isDirectory(releaseDir)) return null;
        try (var stream = Files.list(releaseDir)) {
            return stream
                    .filter(Files::isDirectory)
                    .filter(p -> Files.isDirectory(p.resolve(".echo/content-graph")))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static List<Path> discoverAllModuleGraphRoots(Path modulesRoot) throws IOException {
        List<Path> roots = new ArrayList<>();
        Path releaseRoot = modulesRoot.resolve("dist/echo-module-release");
        if (!Files.isDirectory(releaseRoot)) return roots;
        try (var stream = Files.list(releaseRoot)) {
            stream.filter(Files::isDirectory).forEach(moduleDir -> {
                try {
                    Path versionDir = findFirstVersionDir(moduleDir);
                    if (versionDir != null) {
                        roots.add(versionDir);
                    }
                } catch (IOException e) {
                    // ignore
                }
            });
        }
        return roots;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected ECHO-Modules repository root as first argument");
        }
        Path modulesRoot = Path.of(args[0]).toAbsolutePath().normalize();
        Path reportRoot = Path.of(".").toAbsolutePath().normalize().resolve("reports/echo/standalone");
        Files.createDirectories(reportRoot);

        List<Path> moduleRoots = discoverAllModuleGraphRoots(modulesRoot);

        EchoContentGraphLoader loader = new EchoContentGraphLoader(moduleRoots);
        EchoContentGraphLoader.EchoContentGraphLoadResult result = loader.load();
        Path canonicalEvidencePath = modulesRoot.resolve("dist/echo-module-release/content-graph-evidence.json");
        Map<String, Object> canonicalEvidence = readCanonicalEvidence(canonicalEvidencePath);

        int checked = 0;
        int failures = 0;
        StringBuilder failureDetails = new StringBuilder();

        checked++;
        if (result.graphCount() < 3) {
            failures++;
            failureDetails.append("graphs=").append(result.graphCount()).append(" expected>=3; ");
        }

        checked++;
        if (result.nodes().isEmpty()) {
            failures++;
            failureDetails.append("no nodes loaded; ");
        }

        checked++;
        if (result.edges().isEmpty()) {
            failures++;
            failureDetails.append("no edges loaded; ");
        }

        checked++;
        if (result.findNode("echoopenlandsprotocol:meadow_grass_block").isEmpty()) {
            failures++;
            failureDetails.append("missing meadow_grass_block node; ");
        }

        checked++;
        if (result.findNode("echoopenlandsprotocol:pine_log").isEmpty()) {
            failures++;
            failureDetails.append("missing pine_log node; ");
        }

        checked++;
        if (result.countByKind("echo:block") < 10) {
            failures++;
            failureDetails.append("block nodes=").append(result.countByKind("echo:block")).append(" expected>=10; ");
        }

        checked++;
        if (result.countByKind("echo:recipe") < 1) {
            failures++;
            failureDetails.append("recipe nodes=").append(result.countByKind("echo:recipe")).append(" expected>=1; ");
        }

        checked++;
        if (result.countByKind("echo:ui_intent") < 1) {
            failures++;
            failureDetails.append("ui_intent nodes=").append(result.countByKind("echo:ui_intent")).append(" expected>=1; ");
        }

        checked++;
        if (result.features().isEmpty()) {
            failures++;
            failureDetails.append("no features loaded; ");
        }

        checked++;
        if (result.hytalePlans().isEmpty()) {
            failures++;
            failureDetails.append("no Hytale export plans loaded; ");
        }

        checked++;
        int hytaleBlockedNodes = hytaleStatusCount(result.hytalePlans(), "blocked");
        if (hytaleBlockedNodes < 1) {
            failures++;
            failureDetails.append("hytale blocked nodes=").append(hytaleBlockedNodes).append(" expected>=1; ");
        }

        checked++;
        int hytaleAdapterRequiredNodes = hytaleStatusCount(result.hytalePlans(), "adapter_required");
        if (hytaleAdapterRequiredNodes < 1) {
            failures++;
            failureDetails.append("hytale adapter_required nodes=").append(hytaleAdapterRequiredNodes).append(" expected>=1; ");
        }

        checked++;
        if (openlandsBlockedHytaleNodes(result.hytalePlans()) != 9) {
            failures++;
            failureDetails.append("echoopenlandsprotocol Hytale blocked nodes expected=9; ");
        }

        if (!canonicalEvidence.isEmpty()) {
            checked++;
            if (!"echo.content_graph.evidence.v1".equals(stringValue(canonicalEvidence.get("schemaVersion")))) {
                failures++;
                failureDetails.append("canonical evidence schema mismatch; ");
            }
            checked++;
            if (number(canonicalEvidence, "moduleCount") != result.graphCount()) {
                failures++;
                failureDetails.append("canonical moduleCount=").append(number(canonicalEvidence, "moduleCount"))
                        .append(" loaded=").append(result.graphCount()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "graphCount") != result.graphCount()) {
                failures++;
                failureDetails.append("canonical graphCount=").append(number(canonicalEvidence, "graphCount"))
                        .append(" loaded=").append(result.graphCount()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "nodeCount") != result.nodes().size()) {
                failures++;
                failureDetails.append("canonical nodeCount=").append(number(canonicalEvidence, "nodeCount"))
                        .append(" loaded=").append(result.nodes().size()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "edgeCount") != result.edges().size()) {
                failures++;
                failureDetails.append("canonical edgeCount=").append(number(canonicalEvidence, "edgeCount"))
                        .append(" loaded=").append(result.edges().size()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "featureCount") != result.features().size()) {
                failures++;
                failureDetails.append("canonical featureCount=").append(number(canonicalEvidence, "featureCount"))
                        .append(" loaded=").append(result.features().size()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "exportPlanCount") != result.exportPlanCount()) {
                failures++;
                failureDetails.append("canonical exportPlanCount=").append(number(canonicalEvidence, "exportPlanCount"))
                        .append(" loaded=").append(result.exportPlanCount()).append("; ");
            }
            checked++;
            if (number(canonicalEvidence, "hytaleBlockerCount") != hytaleBlockedNodes) {
                failures++;
                failureDetails.append("canonical hytaleBlockerCount=").append(number(canonicalEvidence, "hytaleBlockerCount"))
                        .append(" loaded=").append(hytaleBlockedNodes).append("; ");
            }
            checked++;
            if (canonicalOpenlandsBlockedHytaleNodes(canonicalEvidence) != 9) {
                failures++;
                failureDetails.append("canonical echoopenlandsprotocol Hytale blocked nodes expected=9; ");
            }
        }

        checked++;
        long errorDiagnostics = result.diagnostics().stream()
                .filter(d -> d.severity() == dev.echo.standalone.runtime.compat.EchoCompatDiagnosticSeverity.ERROR)
                .count();
        if (errorDiagnostics > 0) {
            failures++;
            failureDetails.append("error diagnostics=").append(errorDiagnostics).append("; ");
        }

        String status = failures == 0 ? "PASS" : "FAIL";
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schema", "echo.standalone.content_graph_load_smoke.v1");
        report.put("evidenceSchemaVersion", "echo.content_graph.evidence.v1");
        report.put("status", status);
        report.put("graphs", result.graphCount());
        report.put("moduleCount", result.graphCount());
        report.put("nodes", result.nodes().size());
        report.put("edges", result.edges().size());
        report.put("features", result.features().size());
        report.put("exportPlans", result.exportPlanCount());
        report.put("hytalePlans", result.hytalePlans().size());
        report.put("hytaleBlockedNodes", hytaleBlockedNodes);
        report.put("hytaleAdapterRequiredNodes", hytaleAdapterRequiredNodes);
        report.put("canonicalEvidence", !canonicalEvidence.isEmpty());
        report.put("canonicalEvidencePath", canonicalEvidence.isEmpty() ? null : canonicalEvidencePath.toString());
        report.put("diagnostics", result.diagnostics().size());
        report.put("checked", checked);
        report.put("failures", failures);
        if (failures > 0) {
            report.put("failureDetails", failureDetails.toString().trim());
        }

        Path reportPath = reportRoot.resolve("content-graph-load.json");
        Files.writeString(reportPath, toJson(report) + System.lineSeparator(), StandardCharsets.UTF_8);

        System.out.println("content graph load smoke " + status
                + " graphs=" + result.graphCount()
                + " nodes=" + result.nodes().size()
                + " edges=" + result.edges().size()
                + " features=" + result.features().size()
                + " exportPlans=" + result.exportPlanCount()
                + " hytalePlans=" + result.hytalePlans().size()
                + " hytaleBlockedNodes=" + hytaleBlockedNodes
                + " checked=" + checked
                + " failures=" + failures);

        if (failures > 0) {
            System.exit(1);
        }
    }

    private static int hytaleStatusCount(List<Map<String, Object>> hytalePlans, String status) {
        int count = 0;
        for (Map<String, Object> plan : hytalePlans) {
            Object nodes = plan.get("nodes");
            if (nodes instanceof List<?> list) {
                for (Object node : list) {
                    if (node instanceof Map<?, ?> map && status.equals(map.get("status"))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static int openlandsBlockedHytaleNodes(List<Map<String, Object>> hytalePlans) {
        int count = 0;
        for (Map<String, Object> plan : hytalePlans) {
            Object sourceGraphId = plan.get("sourceGraphId");
            if (!(sourceGraphId instanceof String graphId) || !graphId.startsWith("echoopenlandsprotocol:")) {
                continue;
            }
            Object nodes = plan.get("nodes");
            if (nodes instanceof List<?> list) {
                for (Object node : list) {
                    if (node instanceof Map<?, ?> map && "blocked".equals(map.get("status"))) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static Map<String, Object> readCanonicalEvidence(Path path) throws IOException {
        if (!Files.isRegularFile(path)) {
            return Map.of();
        }
        return asObject(EchoDataJson.parse(Files.readString(path, StandardCharsets.UTF_8)));
    }

    private static int canonicalOpenlandsBlockedHytaleNodes(Map<String, Object> canonicalEvidence) {
        Object modules = canonicalEvidence.get("modules");
        if (!(modules instanceof List<?> list)) {
            return 0;
        }
        for (Object item : list) {
            Map<String, Object> module = asObject(item);
            if ("echoopenlandsprotocol".equals(module.get("moduleId"))) {
                return number(module, "hytaleBlockerCount");
            }
        }
        return 0;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static int number(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static String stringValue(Object value) {
        return value instanceof String string ? string : "";
    }

    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"').append(escape(entry.getKey())).append('"').append(':');
            sb.append(valueToJson(entry.getValue()));
        }
        sb.append('}');
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static String valueToJson(Object value) {
        if (value == null) return "null";
        if (value instanceof String s) return '"' + escape(s) + '"';
        if (value instanceof Number || value instanceof Boolean) return value.toString();
        if (value instanceof List<?> list) {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) sb.append(',');
                first = false;
                sb.append(valueToJson(item));
            }
            sb.append(']');
            return sb.toString();
        }
        if (value instanceof Map<?, ?> m) return toJson((Map<String, Object>) m);
        return '"' + escape(value.toString()) + '"';
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
