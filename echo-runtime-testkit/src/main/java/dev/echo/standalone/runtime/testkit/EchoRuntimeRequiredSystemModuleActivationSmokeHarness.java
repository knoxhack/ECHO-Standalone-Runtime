package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoRuntimeLogBridge;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoRequiredSystemModuleActivation;
import dev.echo.standalone.runtime.compat.EchoRequiredSystemModuleRuntimeResult;
import dev.echo.standalone.runtime.compat.EchoRequiredSystemModuleStandaloneRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class EchoRuntimeRequiredSystemModuleActivationSmokeHarness {
    private EchoRuntimeRequiredSystemModuleActivationSmokeHarness() {
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
                "system module activation smoke requires the ECHO repo root with addons/");

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoRuntimeLogBridge diagnostics = new EchoRuntimeLogBridge();
        services.register(EchoRuntimeDiagnosticSink.class, diagnostics);

        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(moduleRoots(repoRoot), services);
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoRequiredSystemModuleRuntimeResult result =
                new EchoRequiredSystemModuleStandaloneRuntime().activate(services, modules, bridge);

        writeReport(standaloneRoot, result);
        require(result.activationCount() == EchoRequiredSystemModuleStandaloneRuntime.REQUIRED_SYSTEM_MODULES.size(),
                "every required system module should produce an activation result");
        require(result.allExecutable(),
                "every required system module should execute through AdapterCore bindings");
        require(services.find(EchoRequiredSystemModuleRuntimeResult.class).isPresent(),
                "system module runtime result should be registered as a runtime service");
        require(result.require("signalos").runtimeStatus() == EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                "signalos should activate as runtime-active");
        require(result.require("signalosexample").runtimeStatus() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "signalosexample should activate as runtime-dev-only");
        require(result.require("echobridgecore").runtimeStatus() == EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                "echobridgecore should activate as runtime-dev-only");
        require(result.require("echomodpackcommandcenter").runtimeStatus()
                        == EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                "Command Center should activate as runtime-tooling-only");
        require(result.require("echoagentcore").runtimeDomains().contains("commands"),
                "AgentCore activation should expose command contracts");
        require(result.require("echometadatacore").runtimeDomains().contains("data"),
                "MetadataCore activation should expose data contracts");
        require(result.require("echoreportcore").runtimeDomains().contains("diagnostics"),
                "ReportCore activation should expose diagnostics contracts");
        require(result.require("echomodulegraph").contractIds().contains("echomodulegraph:data/module_graph"),
                "ModuleGraph activation should expose module graph contract");

        System.out.println("agent3 required system module activation smoke PASS modules="
                + result.activationCount()
                + " executable=" + result.executableCount()
                + " services=" + services.snapshot().size()
                + " diagnostics=" + diagnostics.diagnostics().size());
    }

    private static void writeReport(
            Path standaloneRoot,
            EchoRequiredSystemModuleRuntimeResult result
    ) throws IOException {
        Path report = standaloneRoot.resolve("reports/echo/standalone/runtime-required-system-module-activation.json");
        Files.createDirectories(report.getParent());
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.required_system_module_activation.v1\",\n");
        json.append("  \"generatedAt\": \"1970-01-01T00:00:00Z\",\n");
        json.append("  \"status\": \"").append(result.allExecutable() ? "PASS" : "FAIL").append("\",\n");
        json.append("  \"activationCount\": ").append(result.activationCount()).append(",\n");
        json.append("  \"executableCount\": ").append(result.executableCount()).append(",\n");
        json.append("  \"allExecutable\": ").append(result.allExecutable()).append(",\n");
        json.append("  \"modules\": [\n");
        for (int i = 0; i < result.activations().size(); i++) {
            EchoRequiredSystemModuleActivation activation = result.activations().get(i);
            json.append("    {\n");
            json.append("      \"moduleId\": \"").append(escape(activation.moduleId())).append("\",\n");
            json.append("      \"runtimeStatus\": \"").append(activation.runtimeStatus().id()).append("\",\n");
            json.append("      \"descriptorLoaded\": ").append(activation.descriptorLoaded()).append(",\n");
            json.append("      \"adapterCoreUsed\": ").append(activation.adapterCoreUsed()).append(",\n");
            json.append("      \"standaloneRuntimeCodeExecuted\": ")
                    .append(activation.standaloneRuntimeCodeExecuted()).append(",\n");
            json.append("      \"allRuntimeAliasesResolved\": ")
                    .append(activation.allRuntimeAliasesResolved()).append(",\n");
            json.append("      \"contractIds\": ").append(stringArray(activation.contractIds())).append(",\n");
            json.append("      \"runtimeDomains\": ").append(stringArray(activation.runtimeDomains())).append("\n");
            json.append("    }").append(i + 1 == result.activations().size() ? "\n" : ",\n");
        }
        json.append("  ]\n");
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

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            json.append("\"").append(escape(values.get(i))).append("\"");
            if (i + 1 < values.size()) {
                json.append(", ");
            }
        }
        json.append("]");
        return json.toString();
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
