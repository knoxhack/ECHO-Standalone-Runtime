package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoContentGraphLoader;

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
        report.put("status", status);
        report.put("graphs", result.graphCount());
        report.put("nodes", result.nodes().size());
        report.put("edges", result.edges().size());
        report.put("features", result.features().size());
        report.put("hytalePlans", result.hytalePlans().size());
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
                + " hytalePlans=" + result.hytalePlans().size()
                + " checked=" + checked
                + " failures=" + failures);

        if (failures > 0) {
            System.exit(1);
        }
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
