package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;
import dev.echo.standalone.runtime.modules.EchoRuntimeSystemModuleStatusReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class EchoRuntimeFullCatalogModuleStatusSmokeHarness {
    private static final int MIN_REAL_MODULE_DESCRIPTORS = 90;
    private static final List<String> REQUIRED_SYSTEM_MODULES = List.of(
            "echomodpackcommandcenter",
            "signalos",
            "signalosexample",
            "echobridgecore",
            "echoagentcore",
            "echoreportcore",
            "echometadatacore",
            "echomodulegraph"
    );

    private EchoRuntimeFullCatalogModuleStatusSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path standaloneRoot = args.length > 0
                ? Path.of(args[0]).toAbsolutePath().normalize()
                : Path.of(".").toAbsolutePath().normalize();
        Path repoRoot = standaloneRoot.getFileName() != null
                && standaloneRoot.getFileName().toString().equals("echo-standalone-runtime")
                ? standaloneRoot.getParent()
                : standaloneRoot;
        require(repoRoot != null && Files.isDirectory(repoRoot.resolve("addons")),
                "full catalog status smoke requires the ECHO repo root with addons/");

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeModuleRuntimeResult result = EchoRuntimeModuleManager.descriptorOnly()
                .run(moduleRoots(repoRoot), services);
        EchoRuntimeModuleRegistry registry = result.registry();
        List<EchoRuntimeModuleDescriptor> descriptors = registry.descriptors();
        require(descriptors.size() >= MIN_REAL_MODULE_DESCRIPTORS,
                "real module catalog should include the expanded Echo module set");
        Set<String> descriptorIds = new TreeSet<>();
        for (EchoRuntimeModuleDescriptor descriptor : descriptors) {
            descriptorIds.add(descriptor.id());
        }
        for (String systemModule : REQUIRED_SYSTEM_MODULES) {
            require(descriptorIds.contains(systemModule),
                    "required system module should have a real runtime descriptor: " + systemModule);
        }

        Map<EchoRuntimeModuleStatus, Integer> counts = new EnumMap<>(EchoRuntimeModuleStatus.class);
        for (EchoRuntimeModuleStatus status : EchoRuntimeModuleStatus.values()) {
            counts.put(status, 0);
        }
        Map<String, String> moduleStatuses = new TreeMap<>();
        Map<String, String> moduleReasons = new TreeMap<>();
        for (EchoRuntimeModuleDescriptor descriptor : descriptors) {
            EchoRuntimeModuleStatus status = registry.runtimeStatus(descriptor.id());
            require(status != null, "module should always have a formal runtime status: " + descriptor.id());
            require(!registry.notes(descriptor.id()).isEmpty(),
                    "module status should include a reason note: " + descriptor.id());
            counts.compute(status, (ignored, current) -> current == null ? 1 : current + 1);
            moduleStatuses.put(descriptor.id(), status.id());
            moduleReasons.put(descriptor.id(), String.join("; ", registry.notes(descriptor.id())));
        }
        int classified = counts.values().stream().mapToInt(Integer::intValue).sum();
        require(classified == descriptors.size(), "every descriptor must be classified exactly once");
        require(counts.get(EchoRuntimeModuleStatus.RUNTIME_ACTIVE) > 0,
                "real catalog should have runtime-active modules");
        require(counts.get(EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY) > 0,
                "real catalog should have runtime-tooling-only modules");
        require(counts.get(EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY) > 0,
                "real catalog should have runtime-dev-only modules");

        EchoRuntimeSystemModuleStatusReport systemStatus = EchoRuntimeSystemModuleStatusReport.forRequiredModules(
                registry,
                REQUIRED_SYSTEM_MODULES
        );
        require(systemStatus.entries().size() == REQUIRED_SYSTEM_MODULES.size(),
                "every required system module should have a formal runtime status entry");
        require(systemStatus.require("signalos").status() == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "signalos should load as runtime-active");
        require(systemStatus.require("signalosexample").status() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "signalosexample should load as runtime-dev-only");
        require(systemStatus.require("echobridgecore").status() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "echobridgecore should load as runtime-dev-only");
        require(systemStatus.require("echoagentcore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echoagentcore should load as runtime-tooling-only");
        require(systemStatus.require("echoreportcore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echoreportcore should load as runtime-tooling-only");
        require(systemStatus.require("echometadatacore").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echometadatacore should load as runtime-tooling-only");
        require(systemStatus.require("echomodulegraph").status() == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echomodulegraph should load as runtime-tooling-only");
        require(systemStatus.require("echomodpackcommandcenter").status()
                        == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "echomodpackcommandcenter should load as runtime-tooling-only");

        writeReport(standaloneRoot, descriptors.size(), counts, moduleStatuses, moduleReasons, systemStatus);
        System.out.println("agent3 full catalog runtime status smoke PASS total="
                + descriptors.size()
                + " active=" + counts.get(EchoRuntimeModuleStatus.RUNTIME_ACTIVE)
                + " tooling=" + counts.get(EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY)
                + " dev=" + counts.get(EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY)
                + " disabled=" + counts.get(EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON)
                + " system=" + systemStatus.entries().size()
                + " diagnostics=" + diagnostics.diagnostics().size());
    }

    private static void writeReport(
            Path standaloneRoot,
            int descriptorCount,
            Map<EchoRuntimeModuleStatus, Integer> counts,
            Map<String, String> moduleStatuses,
            Map<String, String> moduleReasons,
            EchoRuntimeSystemModuleStatusReport systemStatus
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/runtime-full-catalog-module-status.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.runtime_full_catalog_module_status.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"PASS\",\n");
        json.append("  \"descriptorCount\": ").append(descriptorCount).append(",\n");
        json.append("  \"statusCounts\": {\n");
        int statusIndex = 0;
        for (EchoRuntimeModuleStatus status : EchoRuntimeModuleStatus.values()) {
            json.append("    \"").append(status.id()).append("\": ").append(counts.get(status));
            json.append(++statusIndex == EchoRuntimeModuleStatus.values().length ? "\n" : ",\n");
        }
        json.append("  },\n");
        json.append("  \"systemModules\": {\n");
        for (int i = 0; i < systemStatus.entries().size(); i++) {
            var entry = systemStatus.entries().get(i);
            json.append("    \"").append(escape(entry.moduleId())).append("\": {")
                    .append("\"status\": \"").append(entry.status().id()).append("\", ")
                    .append("\"reason\": \"").append(escape(entry.reason())).append("\"}");
            json.append(i + 1 == systemStatus.entries().size() ? "\n" : ",\n");
        }
        json.append("  },\n");
        json.append("  \"modules\": {\n");
        int index = 0;
        for (Map.Entry<String, String> entry : moduleStatuses.entrySet()) {
            json.append("    \"").append(escape(entry.getKey())).append("\": {")
                    .append("\"status\": \"").append(entry.getValue()).append("\", ")
                    .append("\"reason\": \"").append(escape(moduleReasons.get(entry.getKey()))).append("\"}");
            json.append(++index == moduleStatuses.size() ? "\n" : ",\n");
        }
        json.append("  }\n");
        json.append("}\n");
        Files.writeString(report, json.toString());
    }

    private static List<Path> moduleRoots(Path repoRoot) {
        ArrayList<Path> roots = new ArrayList<>();
        addIfDirectory(roots, repoRoot.resolve("core"));
        addIfDirectory(roots, repoRoot.resolve("addons"));
        addIfDirectory(roots, repoRoot.resolve("src/main/resources"));
        return List.copyOf(roots);
    }

    private static void addIfDirectory(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
