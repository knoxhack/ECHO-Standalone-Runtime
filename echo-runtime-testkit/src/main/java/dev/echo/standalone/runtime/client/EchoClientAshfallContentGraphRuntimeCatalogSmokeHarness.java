package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.compat.EchoCompatDiagnostic;
import dev.echo.standalone.runtime.compat.EchoCompatDiagnosticSeverity;
import dev.echo.standalone.runtime.compat.EchoContentGraphLoader;
import dev.echo.standalone.runtime.data.EchoDataJson;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoClientAshfallContentGraphRuntimeCatalogSmokeHarness {
    private static final Path DEFAULT_PACK_ROOT = Path.of(
            "..",
            "ECHO-Ashfall-Standalone-Edition",
            "tmp",
            "rebuild-official-modpack-assets",
            "ashfall-standalone-edition"
    ).toAbsolutePath().normalize();
    private static final Path REPORT_PATH = Path.of(
            "reports",
            "echo",
            "standalone",
            "ashfall-content-graph-runtime-catalog.json"
    );

    private EchoClientAshfallContentGraphRuntimeCatalogSmokeHarness() {
    }

    public static void main(String[] args) throws Exception {
        Path packRoot = args.length >= 1 && !args[0].isBlank()
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : DEFAULT_PACK_ROOT;
        Path modulesRoot = args.length >= 2 && !args[1].isBlank()
                ? Path.of(args[1]).toAbsolutePath().normalize()
                : packRoot.resolve("mods").toAbsolutePath().normalize();
        Path manifestPath = packRoot.resolve(".echo").resolve("pack-manifest.json");
        ArrayList<String> failures = new ArrayList<>();
        LinkedHashMap<String, Object> report = new LinkedHashMap<>();
        report.put("schema", "echo.standalone.ashfall_content_graph_runtime_catalog.v1");
        report.put("generatedAt", Instant.now().toString());
        report.put("generator", EchoClientAshfallContentGraphRuntimeCatalogSmokeHarness.class.getName());
        report.put("packRoot", packRoot.toString());
        report.put("modulesRoot", modulesRoot.toString());
        report.put("packManifest", manifestPath.toString());

        int requiredArtifactCount = 0;
        List<String> requiredModuleIds = List.of();
        if (!Files.isDirectory(packRoot)) {
            failures.add("Ashfall installed pack root is missing: " + packRoot);
        }
        if (!Files.isDirectory(modulesRoot)) {
            failures.add("Ashfall installed modules root is missing: " + modulesRoot);
        }
        if (!Files.isRegularFile(manifestPath)) {
            failures.add("Ashfall pack manifest is missing: " + manifestPath);
        } else {
            Map<String, Object> manifest = object(EchoDataJson.parse(Files.readString(manifestPath, StandardCharsets.UTF_8)));
            requiredArtifactCount = requiredStandaloneArtifactCount(manifest);
            requiredModuleIds = requiredStandaloneModuleIds(manifest);
        }
        report.put("requiredStandaloneArtifacts", requiredArtifactCount);
        report.put("requiredModuleIds", requiredModuleIds);

        if (Files.isDirectory(modulesRoot)) {
            List<Path> archives = discoverStandaloneArchives(modulesRoot);
            report.put("discoveredStandaloneArchives", archives.size());
            try {
                EchoContentGraphLoader.EchoContentGraphLoadResult graphResult =
                        new EchoContentGraphLoader(archives).load();
                List<Map<String, Object>> graphRows = EchoClientContentGraphRuntimeCatalog.rows(graphResult);
                Map<String, Object> graphReport =
                        EchoClientContentGraphRuntimeCatalog.consumptionReport(graphResult, graphRows);
                List<String> graphReadinessFailures =
                        EchoClientContentGraphRuntimeCatalog.readinessValidationFailures(graphResult, graphRows);
                int graphErrors = diagnosticCount(graphResult.diagnostics(), EchoCompatDiagnosticSeverity.ERROR);
                report.put("preflightContentGraphConsumption", graphReport);
                report.put("preflightContentGraphDiagnostics", diagnosticRows(graphResult.diagnostics()));
                report.put("preflightContentGraphReadinessFailures", graphReadinessFailures);
                if (graphResult.graphCount() < requiredArtifactCount) {
                    failures.add("preflight loaded " + graphResult.graphCount()
                            + " graph(s), expected at least " + requiredArtifactCount);
                }
                if (graphResult.standalonePlans().size() < requiredArtifactCount) {
                    failures.add("preflight loaded " + graphResult.standalonePlans().size()
                            + " standalone export plan(s), expected at least " + requiredArtifactCount);
                }
                if (graphErrors > 0) {
                    failures.add("preflight content graph loader reported " + graphErrors + " error diagnostic(s)");
                }
                failures.addAll(graphReadinessFailures);
            } catch (Exception exception) {
                failures.add("preflight content graph load failed: " + exception.getMessage());
                report.put("preflightContentGraphException", exception.getClass().getName());
            }
        }

        EchoClientModuleBootstrapResult bootstrap = null;
        try {
            if (!Files.isDirectory(packRoot) || !Files.isDirectory(modulesRoot) || !Files.isRegularFile(manifestPath)) {
                // The preflight section already recorded concrete path failures.
            } else {
                EchoClientLaunchContext context = EchoClientLaunchContext.parse(new String[]{
                        "--pack-root",
                        packRoot.toString(),
                        "--modules-root",
                        modulesRoot.toString()
                });
                bootstrap = EchoClientModuleBootstrap.boot(context);
                EchoClientRuntimeServices services = EchoClientRuntimeServices.forTemplate(
                        EchoClientWorldTemplates.ashfallCrashSite(),
                        Files.createTempDirectory("echo-ashfall-content-graph-runtime-catalog"),
                        bootstrap
                );
                List<Map<String, Object>> rows = bootstrap.adapterCoreContentRows();
                Map<String, Object> graphReport = bootstrap.contentGraphConsumptionReport();
                Map<String, Integer> domainCounts = domainCounts(rows);
                List<String> readinessFailures =
                        EchoClientContentGraphRuntimeCatalog.readinessValidationFailures(
                                bootstrap.contentGraphResult(),
                                rows
                        );

                require(bootstrap.active(), failures, "strict Ashfall installed-pack bootstrap did not activate");
                require(bootstrap.strictPackMode(), failures, "Ashfall bootstrap did not retain strict-pack mode");
                require(!bootstrap.safeMode(), failures, "Ashfall smoke must not run through safe mode");
                require(bootstrap.moduleRoots().size() >= requiredArtifactCount, failures,
                        "discovered " + bootstrap.moduleRoots().size()
                                + " module root(s), expected at least " + requiredArtifactCount);
                require(bootstrap.contentGraphLoaded(), failures, "Ashfall bootstrap did not load Content Graph data");
                require(bootstrap.contentGraphResult().graphCount() >= requiredArtifactCount, failures,
                        "loaded " + bootstrap.contentGraphResult().graphCount()
                                + " graph(s), expected at least " + requiredArtifactCount);
                require(bootstrap.contentGraphResult().standalonePlans().size() >= requiredArtifactCount, failures,
                        "loaded " + bootstrap.contentGraphResult().standalonePlans().size()
                                + " standalone export plan(s), expected at least " + requiredArtifactCount);
                require(!rows.isEmpty(), failures, "runtime catalog has no graph/native rows");
                require(domainCounts.getOrDefault("blocks", 0) > 0, failures,
                        "runtime catalog has no block rows");
                require(domainCounts.getOrDefault("items", 0) > 0, failures,
                        "runtime catalog has no item rows");
                require(domainCounts.getOrDefault("inventory", 0) > 0, failures,
                        "runtime catalog has no creative tab/inventory rows");
                require(domainCounts.getOrDefault("recipes", 0) > 0, failures,
                        "runtime catalog has no recipe rows");
                require(domainCounts.getOrDefault("terminal", 0) > 0, failures,
                        "runtime catalog has no Terminal rows");
                require(domainCounts.getOrDefault("lens", 0) > 0, failures,
                        "runtime catalog has no Lens rows");
                require(domainCounts.getOrDefault("index", 0) > 0, failures,
                        "runtime catalog has no Index rows");
                failures.addAll(readinessFailures);

                report.put("bootstrapActive", bootstrap.active());
                report.put("strictPackMode", bootstrap.strictPackMode());
                report.put("safeMode", bootstrap.safeMode());
                report.put("discoveredModuleRoots", bootstrap.moduleRoots().size());
                report.put("loadedGraphs", bootstrap.contentGraphResult().graphCount());
                report.put("loadedStandaloneExportPlans", bootstrap.contentGraphResult().standalonePlans().size());
                report.put("runtimeCatalogRows", rows.size());
                report.put("runtimeContentSummaryRows", services.runtimeContentSummary().rowCount());
                report.put("runtimeContentDomains", services.runtimeContentSummary().domainCounts());
                report.put("runtimeCatalogDomains", domainCounts);
                report.put("screenCatalogRoutes", services.screenCatalog().screenCount());
                report.put("adapterCoreScreenRoutes", services.screenCatalog().adapterCoreScreenCount());
                report.put("contentGraphConsumption", graphReport);
                report.put("readinessFailures", List.copyOf(readinessFailures));
            }
        } catch (Exception exception) {
            failures.add("strict Ashfall installed-pack bootstrap failed: " + exception.getMessage());
            report.put("exception", exception.getClass().getName());
        } finally {
            if (bootstrap != null) {
                bootstrap.close();
            }
        }

        String status = failures.isEmpty() ? "PASS" : "BLOCKED";
        report.put("status", status);
        report.put("failures", List.copyOf(failures));
        Files.createDirectories(REPORT_PATH.toAbsolutePath().normalize().getParent());
        Files.writeString(REPORT_PATH, toJson(report) + System.lineSeparator(), StandardCharsets.UTF_8);
        System.out.println("ashfall content graph runtime catalog smoke " + status
                + " requiredArtifacts=" + requiredArtifactCount
                + " failures=" + failures.size()
                + " report=" + REPORT_PATH.toAbsolutePath().normalize());
        if (!failures.isEmpty()) {
            System.exit(1);
        }
    }

    private static int requiredStandaloneArtifactCount(Map<String, Object> manifest) {
        int count = 0;
        for (Map<String, Object> file : manifestFiles(manifest)) {
            if (requiredStandaloneArtifact(file)) {
                count++;
            }
        }
        return count;
    }

    private static List<String> requiredStandaloneModuleIds(Map<String, Object> manifest) {
        ArrayList<String> moduleIds = new ArrayList<>();
        for (Map<String, Object> file : manifestFiles(manifest)) {
            if (!requiredStandaloneArtifact(file)) {
                continue;
            }
            String moduleId = text(file.get("moduleId"));
            if (!moduleId.isBlank()) {
                moduleIds.add(moduleId);
            }
        }
        return List.copyOf(moduleIds);
    }

    private static boolean requiredStandaloneArtifact(Map<String, Object> file) {
        if (!Boolean.TRUE.equals(file.get("required"))) {
            return false;
        }
        String artifactFamily = text(file.get("artifactFamily"));
        String path = text(file.get("path"));
        String assetName = text(file.get("assetName"));
        return artifactFamily.equals("standalone")
                || path.endsWith("-standalone.jar")
                || assetName.endsWith("-standalone.jar");
    }

    private static List<Map<String, Object>> manifestFiles(Map<String, Object> manifest) {
        Object files = manifest.get("files");
        if (!(files instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> file = object(item);
            if (!file.isEmpty()) {
                result.add(file);
            }
        }
        return List.copyOf(result);
    }

    private static List<Path> discoverStandaloneArchives(Path modulesRoot) throws Exception {
        try (var stream = Files.list(modulesRoot)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("-standalone.jar"))
                    .sorted(Comparator.comparing(Path::toString))
                    .map(path -> path.toAbsolutePath().normalize())
                    .toList();
        }
    }

    private static int diagnosticCount(
            List<EchoCompatDiagnostic> diagnostics,
            EchoCompatDiagnosticSeverity severity
    ) {
        int count = 0;
        for (EchoCompatDiagnostic diagnostic : diagnostics) {
            if (diagnostic.severity() == severity) {
                count++;
            }
        }
        return count;
    }

    private static List<Map<String, Object>> diagnosticRows(List<EchoCompatDiagnostic> diagnostics) {
        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (EchoCompatDiagnostic diagnostic : diagnostics) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("severity", diagnostic.severity().name());
            row.put("subject", diagnostic.subject());
            row.put("message", diagnostic.message());
            rows.add(Map.copyOf(row));
        }
        return List.copyOf(rows);
    }

    private static Map<String, Integer> domainCounts(List<Map<String, Object>> rows) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String domain = text(row.get("domain"));
            if (!domain.isBlank()) {
                counts.merge(domain, 1, Integer::sum);
            }
        }
        return Map.copyOf(counts);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static void require(boolean condition, List<String> failures, String message) {
        if (!condition) {
            failures.add(message);
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String toJson(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder out = new StringBuilder();
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                out.append(toJson(String.valueOf(entry.getKey()))).append(':').append(toJson(entry.getValue()));
            }
            out.append('}');
            return out.toString();
        }
        if (value instanceof Iterable<?> iterable) {
            StringBuilder out = new StringBuilder();
            out.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    out.append(',');
                }
                first = false;
                out.append(toJson(item));
            }
            out.append(']');
            return out.toString();
        }
        return toJson(String.valueOf(value));
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
