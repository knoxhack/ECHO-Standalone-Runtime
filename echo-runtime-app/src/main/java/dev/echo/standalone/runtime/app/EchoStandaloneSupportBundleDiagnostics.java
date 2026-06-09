package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentBinding;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageAuditor;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageReport;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreModuleCoverageStatus;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoStandaloneSupportBundleDiagnostics {
    private static final String FIXED_TIME = "1970-01-01T00:00:00Z";
    private static final String MOD_DIAGNOSTICS_PATH = "build/support/standalone-mod-diagnostics.json";
    private static final String REGISTRY_FINGERPRINT_PATH =
            "build/support/standalone-runtime-registry-fingerprint.json";
    private static final String TARGET_DIFF_PATH = "build/support/standalone-adaptercore-target-diff.json";
    private static final String MODULE_LIFECYCLE_TRACE_PATH =
            "build/support/standalone-module-lifecycle-traces.json";
    private static final String REGISTRY_FINGERPRINT_ALGORITHM =
            "sha256:echo.standalone.support.runtime_registry.canonical.v1";
    private static final String MODULE_GRAPH_FINGERPRINT_ALGORITHM =
            "sha256:echo.standalone.support.module_graph.canonical.v1";

    private EchoStandaloneSupportBundleDiagnostics() {
    }

    static Generated generate(
            Path workspaceRoot,
            Path supportRoot,
            EchoStandaloneLauncherVerification verification,
            EchoStandaloneLauncherRepairPlan repairPlan,
            EchoStandaloneLauncherHandoffPlan handoffPlan
    ) throws IOException {
        Path root = workspaceRoot.toAbsolutePath().normalize();
        Files.createDirectories(supportRoot);
        Snapshot snapshot = snapshot(root);
        LifecycleTraceSummary lifecycleTrace = lifecycleTraceSummary(snapshot);
        String registryFingerprint = runtimeRegistryFingerprint(snapshot.bridge());
        String moduleGraphFingerprint = moduleGraphFingerprint(snapshot.modules());
        String targetDiffStatus = snapshot.coverage().contractLockedForBeta()
                && standaloneRuntimeReady(snapshot)
                ? "PASS"
                : "WARN";

        write(root.resolve(MOD_DIAGNOSTICS_PATH),
                modDiagnosticsJson(root, snapshot, verification, repairPlan, handoffPlan, lifecycleTrace));
        write(root.resolve(REGISTRY_FINGERPRINT_PATH),
                registryFingerprintJson(snapshot, registryFingerprint, moduleGraphFingerprint));
        write(root.resolve(TARGET_DIFF_PATH),
                targetDiffJson(snapshot, targetDiffStatus));
        write(root.resolve(MODULE_LIFECYCLE_TRACE_PATH),
                moduleLifecycleTracesJson(lifecycleTrace));

        return new Generated(
                List.of(
                        MOD_DIAGNOSTICS_PATH,
                        REGISTRY_FINGERPRINT_PATH,
                        TARGET_DIFF_PATH,
                        MODULE_LIFECYCLE_TRACE_PATH
                ),
                List.of(
                        "modDiagnostics=" + MOD_DIAGNOSTICS_PATH,
                        "moduleDescriptors=" + snapshot.modules().registry().descriptors().size(),
                        "moduleGraphFingerprint=" + moduleGraphFingerprint,
                        "runtimeRegistryFingerprint=" + registryFingerprint,
                        "adapterCoreTargetDiffStatus=" + targetDiffStatus,
                        "adapterCoreBindingCoverage=" + snapshot.bridge().bindingCoverageSummary(),
                        "adapterCoreStandaloneRuntimeReady=" + standaloneRuntimeReady(snapshot),
                        "moduleLifecycleTraces=" + MODULE_LIFECYCLE_TRACE_PATH,
                        "moduleLifecycleStatus=" + lifecycleTrace.status(),
                        "moduleLifecycleDescriptorCount=" + lifecycleTrace.descriptorCount(),
                        "moduleLifecycleFailedModuleCount=" + lifecycleTrace.failedModuleCount(),
                        "moduleLifecycleGraphIssueCount=" + lifecycleTrace.graphIssueCount(),
                        "screenCoreRoute=echoscreencore:mods",
                        "saveManifestCount=" + saveManifestPaths(root).size()
                )
        );
    }

    private static Snapshot snapshot(Path workspaceRoot) {
        Path repoRoot = repoRoot(workspaceRoot);
        List<Path> moduleRoots = moduleRoots(repoRoot);
        EchoRuntimeModuleRuntimeResult modules = EchoRuntimeModuleManager.descriptorOnly()
                .run(moduleRoots, new EchoDefaultRuntimeServiceRegistry());
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        EchoAdapterCoreModuleCoverageReport coverage = new EchoAdapterCoreModuleCoverageAuditor()
                .audit(modules, bridge);
        return new Snapshot(repoRoot, moduleRoots, modules, bridge, coverage);
    }

    private static String modDiagnosticsJson(
            Path workspaceRoot,
            Snapshot snapshot,
            EchoStandaloneLauncherVerification verification,
            EchoStandaloneLauncherRepairPlan repairPlan,
            EchoStandaloneLauncherHandoffPlan handoffPlan,
            LifecycleTraceSummary lifecycleTrace
    ) {
        StringBuilder json = new StringBuilder();
        List<Path> saveManifests = saveManifestPaths(workspaceRoot);
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.support.mod_diagnostics.v1\",\n");
        json.append("  \"generatedAt\": \"").append(FIXED_TIME).append("\",\n");
        json.append("  \"summary\": \"Support bundle diagnostics for module graph, AdapterCore target coverage, save metadata, ScreenCore route, and OpenGL renderer handoff.\",\n");
        json.append("  \"launcher\": {\n");
        json.append("    \"verificationReady\": ").append(verification.ready()).append(",\n");
        json.append("    \"repairActions\": ").append(repairPlan.actionCount()).append(",\n");
        json.append("    \"handoffMode\": \"").append(escape(handoffPlan.modeId())).append("\",\n");
        json.append("    \"runtimeFamily\": \"").append(escape(handoffPlan.runtimeFamily())).append("\",\n");
        json.append("    \"rendererTarget\": \"").append(escape(handoffPlan.rendererTarget())).append("\",\n");
        json.append("    \"standaloneLaunchTask\": \"").append(escape(handoffPlan.standaloneLaunchTask())).append("\",\n");
        json.append("    \"screenCoreRoute\": \"echoscreencore:mods\"\n");
        json.append("  },\n");
        json.append("  \"moduleScan\": {\n");
        json.append("    \"repoRoot\": \"").append(escape(relativeOrAbsolute(workspaceRoot, snapshot.repoRoot()))).append("\",\n");
        json.append("    \"roots\": ").append(pathArray(workspaceRoot, snapshot.moduleRoots())).append(",\n");
        json.append("    \"descriptorCount\": ").append(snapshot.modules().registry().descriptors().size()).append(",\n");
        json.append("    \"graphIssueCount\": ").append(snapshot.modules().moduleGraph().issues().size()).append(",\n");
        json.append("    \"failedModuleIds\": ").append(stringArray(snapshot.modules().moduleGraph().failedModuleIds())).append(",\n");
        json.append("    \"statusCounts\": ").append(statusCountsJson(snapshot.modules())).append("\n");
        json.append("  },\n");
        json.append("  \"moduleLifecycle\": {\n");
        json.append("    \"lifecycleTraceArtifact\": \"").append(MODULE_LIFECYCLE_TRACE_PATH).append("\",\n");
        json.append("    \"status\": \"").append(lifecycleTrace.status()).append("\",\n");
        json.append("    \"descriptorCount\": ").append(lifecycleTrace.descriptorCount()).append(",\n");
        json.append("    \"activeRuntimeCount\": ").append(lifecycleTrace.activeRuntimeCount()).append(",\n");
        json.append("    \"failedModuleCount\": ").append(lifecycleTrace.failedModuleCount()).append(",\n");
        json.append("    \"graphIssueCount\": ").append(lifecycleTrace.graphIssueCount()).append("\n");
        json.append("  },\n");
        json.append("  \"adapterCore\": {\n");
        json.append("    \"bindingCoverage\": \"").append(escape(snapshot.bridge().bindingCoverageSummary())).append("\",\n");
        json.append("    \"standaloneRuntimeReady\": ").append(standaloneRuntimeReady(snapshot)).append(",\n");
        json.append("    \"coverageSummary\": \"").append(escape(snapshot.coverage().contractSummary())).append("\",\n");
        json.append("    \"contractLockedForBeta\": ").append(snapshot.coverage().contractLockedForBeta()).append(",\n");
        json.append("    \"requiredDomainsMissing\": ")
                .append(stringArray(snapshot.coverage().missingRequiredBetaDomains().stream()
                        .map(EchoAdapterCoreDomain::id)
                        .toList()))
                .append("\n");
        json.append("  },\n");
        json.append("  \"saveMetadata\": {\n");
        json.append("    \"manifestCount\": ").append(saveManifests.size()).append(",\n");
        json.append("    \"manifests\": ").append(pathArray(workspaceRoot, saveManifests.stream().limit(12).toList())).append("\n");
        json.append("  },\n");
        json.append("  \"modules\": [\n");
        List<EchoRuntimeModuleDescriptor> descriptors = snapshot.modules().registry().descriptors();
        for (int i = 0; i < descriptors.size(); i++) {
            EchoRuntimeModuleDescriptor descriptor = descriptors.get(i);
            json.append("    {\n");
            json.append("      \"moduleId\": \"").append(escape(descriptor.id())).append("\",\n");
            json.append("      \"name\": \"").append(escape(descriptor.name())).append("\",\n");
            json.append("      \"version\": \"").append(escape(descriptor.version())).append("\",\n");
            json.append("      \"status\": \"")
                    .append(escape(snapshot.modules().registry().runtimeStatus(descriptor.id()).id()))
                    .append("\",\n");
            json.append("      \"lifecycle\": \"")
                    .append(snapshot.modules().registry().lifecycle(descriptor.id()).name().toLowerCase())
                    .append("\",\n");
            json.append("      \"nativeEntrypoint\": \"").append(escape(descriptor.executableEntrypoint())).append("\",\n");
            json.append("      \"permissions\": ").append(stringArray(descriptor.permissions())).append(",\n");
            json.append("      \"provides\": ").append(stringArray(descriptor.provides())).append(",\n");
            json.append("      \"consumes\": ").append(stringArray(descriptor.consumes())).append(",\n");
            json.append("      \"notes\": ")
                    .append(stringArray(snapshot.modules().registry().notes(descriptor.id())))
                    .append(",\n");
            json.append("      \"descriptorPath\": \"")
                    .append(escape(relativeOrAbsolute(workspaceRoot, descriptor.descriptorPath())))
                    .append("\"\n");
            json.append("    }").append(i + 1 == descriptors.size() ? "\n" : ",\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private static String moduleLifecycleTracesJson(LifecycleTraceSummary trace) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.support.module_lifecycle_traces.v1\",\n");
        json.append("  \"generatedAt\": \"").append(FIXED_TIME).append("\",\n");
        json.append("  \"status\": \"").append(trace.status()).append("\",\n");
        json.append("  \"summary\": \"Support bundle trace summary for descriptor-only standalone module discovery, graph status, and OpenGL launcher diagnostics.\",\n");
        json.append("  \"moduleGraph\": {\n");
        json.append("    \"descriptorCount\": ").append(trace.descriptorCount()).append(",\n");
        json.append("    \"activeRuntimeCount\": ").append(trace.activeRuntimeCount()).append(",\n");
        json.append("    \"failedModuleCount\": ").append(trace.failedModuleCount()).append(",\n");
        json.append("    \"graphIssueCount\": ").append(trace.graphIssueCount()).append("\n");
        json.append("  },\n");
        json.append("  \"runtimeState\": {\n");
        json.append("    \"activeModules\": ").append(trace.activeModulesJson()).append(",\n");
        json.append("    \"failedModules\": ").append(trace.failedModulesJson()).append("\n");
        json.append("  }\n");
        json.append("}\n");
        return json.toString();
    }

    private static LifecycleTraceSummary lifecycleTraceSummary(Snapshot snapshot) {
        EchoRuntimeModuleRuntimeResult modules = snapshot.modules();
        List<String> activeModules = modules.registry().descriptors().stream()
                .filter(descriptor -> modules.registry().runtimeStatus(descriptor.id()) == EchoRuntimeModuleStatus.RUNTIME_ACTIVE)
                .map(EchoRuntimeModuleDescriptor::id)
                .sorted()
                .toList();
        List<String> failedModules = modules.moduleGraph().failedModuleIds().stream()
                .sorted()
                .toList();
        return new LifecycleTraceSummary(
                modules.registry().descriptors().size(),
                activeModules.size(),
                failedModules.size(),
                modules.moduleGraph().issues().size(),
                stringArray(activeModules),
                stringArray(failedModules)
        );
    }

    private static String registryFingerprintJson(
            Snapshot snapshot,
            String registryFingerprint,
            String moduleGraphFingerprint
    ) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.support.runtime_registry_fingerprint.v1\",\n");
        json.append("  \"generatedAt\": \"").append(FIXED_TIME).append("\",\n");
        json.append("  \"registryFingerprintAlgorithm\": \"").append(REGISTRY_FINGERPRINT_ALGORITHM).append("\",\n");
        json.append("  \"runtimeRegistryFingerprint\": \"").append(registryFingerprint).append("\",\n");
        json.append("  \"moduleGraphFingerprintAlgorithm\": \"").append(MODULE_GRAPH_FINGERPRINT_ALGORITHM).append("\",\n");
        json.append("  \"moduleGraphFingerprint\": \"").append(moduleGraphFingerprint).append("\",\n");
        json.append("  \"bindingCount\": ").append(snapshot.bridge().bindingCount()).append(",\n");
        json.append("  \"readyBindingCount\": ").append(snapshot.bridge().readyBindingCount()).append(",\n");
        json.append("  \"standaloneRuntimeReady\": ").append(standaloneRuntimeReady(snapshot)).append(",\n");
        json.append("  \"domainCounts\": ").append(domainCountsJson(snapshot.bridge())).append(",\n");
        json.append("  \"runtimeKinds\": ")
                .append(stringArray(List.of(EchoAdapterCoreRuntimeKind.ECHO_RUNTIME_STANDALONE.adapterId())))
                .append(",\n");
        json.append("  \"sampleContentIds\": ")
                .append(stringArray(snapshot.bridge().registry().entries().stream()
                        .map(entry -> entry.contentId())
                        .sorted()
                        .limit(20)
                        .toList()))
                .append("\n");
        json.append("}\n");
        return json.toString();
    }

    private static String targetDiffJson(Snapshot snapshot, String targetDiffStatus) {
        List<EchoAdapterCoreModuleCoverageEntry> nonActive = snapshot.coverage().entries().stream()
                .filter(entry -> entry.status() != EchoAdapterCoreModuleCoverageStatus.ACTIVE)
                .toList();
        List<EchoAdapterCoreContentBinding> nonReadyBindings = snapshot.bridge().bindings().stream()
                .filter(binding -> !standaloneRuntimeReady(binding))
                .toList();
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"schema\": \"echo.standalone.support.adaptercore_target_diff.v1\",\n");
        json.append("  \"generatedAt\": \"").append(FIXED_TIME).append("\",\n");
        json.append("  \"status\": \"").append(targetDiffStatus).append("\",\n");
        json.append("  \"summary\": \"AdapterCore target diff between declared module metadata and live standalone runtime bindings.\",\n");
        json.append("  \"counts\": {\n");
        json.append("    \"modules\": ").append(snapshot.coverage().totalCount()).append(",\n");
        json.append("    \"activeModules\": ").append(snapshot.coverage().activeCount()).append(",\n");
        json.append("    \"adapterGaps\": ").append(snapshot.coverage().adapterGapCount()).append(",\n");
        json.append("    \"unsupportedModules\": ").append(snapshot.coverage().unsupportedCount()).append(",\n");
        json.append("    \"runtimeTargetGaps\": ")
                .append(snapshot.coverage().standaloneModulesMissingRuntimeTargets().size()).append(",\n");
        json.append("    \"nonReadyBindings\": ").append(nonReadyBindings.size()).append("\n");
        json.append("  },\n");
        json.append("  \"requiredDomainsMissing\": ")
                .append(stringArray(snapshot.coverage().missingRequiredBetaDomains().stream()
                        .map(EchoAdapterCoreDomain::id)
                        .toList()))
                .append(",\n");
        json.append("  \"graphIssues\": ").append(stringArray(snapshot.coverage().graphIssues())).append(",\n");
        json.append("  \"nonActiveModules\": [\n");
        for (int i = 0; i < nonActive.size(); i++) {
            EchoAdapterCoreModuleCoverageEntry entry = nonActive.get(i);
            json.append("    {\"moduleId\": \"").append(escape(entry.moduleId())).append("\", ")
                    .append("\"status\": \"").append(entry.status().name().toLowerCase()).append("\", ")
                    .append("\"gaps\": ").append(stringArray(entry.gaps())).append("}")
                    .append(i + 1 == nonActive.size() ? "\n" : ",\n");
        }
        json.append("  ],\n");
        json.append("  \"nonReadyBindings\": [\n");
        for (int i = 0; i < nonReadyBindings.size(); i++) {
            EchoAdapterCoreContentBinding binding = nonReadyBindings.get(i);
            json.append("    {\"contentId\": \"").append(escape(binding.contentId())).append("\", ")
                    .append("\"moduleId\": \"").append(escape(binding.moduleId())).append("\", ")
                    .append("\"contentKind\": \"").append(binding.contentKind().name().toLowerCase()).append("\", ")
                    .append("\"standaloneRuntimeReady\": ").append(standaloneRuntimeReady(binding)).append("}")
                    .append(i + 1 == nonReadyBindings.size() ? "\n" : ",\n");
        }
        json.append("  ]\n");
        json.append("}\n");
        return json.toString();
    }

    private static List<Path> saveManifestPaths(Path workspaceRoot) {
        ArrayList<Path> manifests = new ArrayList<>();
        addSaveManifests(manifests, workspaceRoot.resolve("saves"));
        addSaveManifests(manifests, workspaceRoot.resolve("echo-runtime-client/saves/client"));
        return manifests.stream()
                .map(path -> path.toAbsolutePath().normalize())
                .distinct()
                .sorted()
                .toList();
    }

    private static void addSaveManifests(List<Path> manifests, Path root) {
        if (!Files.isDirectory(root)) {
            return;
        }
        try (var stream = Files.walk(root, 8)) {
            manifests.addAll(stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("manifest.json"))
                    .toList());
        } catch (IOException ignored) {
            // Support bundle generation should keep going even if an old save directory cannot be scanned.
        }
    }

    private static Path repoRoot(Path workspaceRoot) {
        return EchoStandaloneModuleRoots.modulesRepoRoot(workspaceRoot);
    }

    private static List<Path> moduleRoots(Path repoRoot) {
        return EchoStandaloneModuleRoots.resolve(repoRoot);
    }

    private static void addIfDirectory(List<Path> roots, Path path) {
        if (Files.isDirectory(path)) {
            roots.add(path.toAbsolutePath().normalize());
        }
    }

    private static String runtimeRegistryFingerprint(EchoAdapterCoreStandaloneContentBridge bridge) {
        ArrayList<String> rows = new ArrayList<>();
        rows.add(REGISTRY_FINGERPRINT_ALGORITHM);
        for (EchoAdapterCoreContentBinding binding : bridge.bindings().stream()
                .sorted(java.util.Comparator.comparing(EchoAdapterCoreContentBinding::contentId))
                .toList()) {
            rows.add(String.join("|",
                    binding.moduleId(),
                    binding.contentId(),
                    binding.contentKind().name(),
                    binding.adapterKey(),
                    binding.neoForgeId(),
                    binding.standaloneRuntimeId(),
                    binding.liveVoxelId(),
                    Boolean.toString(binding.standaloneReady())
            ));
        }
        return sha256(String.join("\n", rows));
    }

    private static boolean standaloneRuntimeReady(Snapshot snapshot) {
        return snapshot.bridge().bindings().stream()
                .allMatch(EchoStandaloneSupportBundleDiagnostics::standaloneRuntimeReady);
    }

    private static boolean standaloneRuntimeReady(EchoAdapterCoreContentBinding binding) {
        return binding != null
                && binding.standaloneReady()
                && !binding.standaloneRuntimeId().isBlank();
    }

    private static String moduleGraphFingerprint(EchoRuntimeModuleRuntimeResult modules) {
        ArrayList<String> rows = new ArrayList<>();
        rows.add(MODULE_GRAPH_FINGERPRINT_ALGORITHM);
        for (EchoRuntimeModuleDescriptor descriptor : modules.registry().descriptors()) {
            rows.add("module|" + descriptor.id()
                    + "|" + descriptor.version()
                    + "|" + modules.registry().runtimeStatus(descriptor.id()).id()
                    + "|" + modules.registry().lifecycle(descriptor.id()).name()
                    + "|" + String.join(",", descriptor.requires())
                    + "|" + String.join(",", descriptor.optional())
                    + "|" + String.join(",", descriptor.permissions()));
        }
        EchoRuntimeModuleGraph graph = modules.moduleGraph();
        for (EchoRuntimeModuleGraph.Edge edge : graph.dependencyEdges()) {
            rows.add("edge|" + edge.fromModuleId() + "|" + edge.toModuleId() + "|" + edge.kind());
        }
        for (String failed : graph.failedModuleIds()) {
            rows.add("failed|" + failed);
        }
        rows.sort(String::compareTo);
        return sha256(String.join("\n", rows));
    }

    private static String statusCountsJson(EchoRuntimeModuleRuntimeResult modules) {
        EnumMap<EchoRuntimeModuleStatus, Integer> counts = new EnumMap<>(EchoRuntimeModuleStatus.class);
        for (EchoRuntimeModuleDescriptor descriptor : modules.registry().descriptors()) {
            counts.merge(modules.registry().runtimeStatus(descriptor.id()), 1, Integer::sum);
        }
        LinkedHashMap<String, Integer> ordered = new LinkedHashMap<>();
        for (EchoRuntimeModuleStatus status : EchoRuntimeModuleStatus.values()) {
            ordered.put(status.id(), counts.getOrDefault(status, 0));
        }
        return intMapJson(ordered);
    }

    private static String domainCountsJson(EchoAdapterCoreStandaloneContentBridge bridge) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (EchoAdapterCoreDomain domain : EchoAdapterCoreDomain.values()) {
            int count = bridge.registry().count(domain);
            if (count > 0) {
                counts.put(domain.id(), count);
            }
        }
        return intMapJson(counts);
    }

    private static String intMapJson(Map<String, Integer> values) {
        StringBuilder json = new StringBuilder("{");
        int index = 0;
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            if (index++ > 0) {
                json.append(", ");
            }
            json.append("\"").append(escape(entry.getKey())).append("\": ").append(entry.getValue());
        }
        json.append("}");
        return json.toString();
    }

    private static String pathArray(Path base, List<Path> paths) {
        return stringArray(paths.stream()
                .map(path -> relativeOrAbsolute(base, path))
                .toList());
    }

    private static String relativeOrAbsolute(Path base, Path path) {
        Path normalizedBase = base.toAbsolutePath().normalize();
        Path normalized = path.toAbsolutePath().normalize();
        try {
            return normalizedBase.relativize(normalized).toString().replace('\\', '/');
        } catch (IllegalArgumentException ignored) {
            return normalized.toString().replace('\\', '/');
        }
    }

    private static String stringArray(List<String> values) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                json.append(", ");
            }
            json.append("\"").append(escape(values.get(i))).append("\"");
        }
        json.append("]");
        return json.toString();
    }

    private static boolean booleanField(String json, String field) {
        return json.contains("\"" + field + "\": true");
    }

    private static long longField(String json, String field) {
        String value = jsonValueField(json, field, "0").trim();
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static String stringField(String json, String field, String fallback) {
        String value = jsonValueField(json, field, "");
        if (value.length() < 2 || value.charAt(0) != '"' || value.charAt(value.length() - 1) != '"') {
            return fallback;
        }
        return unescapeJsonString(value.substring(1, value.length() - 1));
    }

    private static String jsonValueField(String json, String field, String fallback) {
        String key = "\"" + field + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex < 0) {
            return fallback;
        }
        int colon = json.indexOf(':', keyIndex + key.length());
        if (colon < 0) {
            return fallback;
        }
        int valueStart = colon + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        if (valueStart >= json.length()) {
            return fallback;
        }
        char first = json.charAt(valueStart);
        if (first == '[') {
            return jsonContainer(json, valueStart, '[', ']', fallback);
        }
        if (first == '{') {
            return jsonContainer(json, valueStart, '{', '}', fallback);
        }
        if (first == '"') {
            return jsonStringLiteral(json, valueStart, fallback);
        }
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char ch = json.charAt(valueEnd);
            if (ch == ',' || ch == '\n' || ch == '\r' || ch == '}') {
                break;
            }
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd).trim();
    }

    private static String jsonContainer(
            String json,
            int valueStart,
            char open,
            char close,
            String fallback
    ) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int index = valueStart; index < json.length(); index++) {
            char ch = json.charAt(index);
            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }
            if (ch == '"') {
                inString = true;
            } else if (ch == open) {
                depth++;
            } else if (ch == close) {
                depth--;
                if (depth == 0) {
                    return json.substring(valueStart, index + 1);
                }
            }
        }
        return fallback;
    }

    private static String jsonStringLiteral(String json, int valueStart, String fallback) {
        boolean escaped = false;
        for (int index = valueStart + 1; index < json.length(); index++) {
            char ch = json.charAt(index);
            if (escaped) {
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '"') {
                return json.substring(valueStart, index + 1);
            }
        }
        return fallback;
    }

    private static String unescapeJsonString(String value) {
        StringBuilder builder = new StringBuilder();
        boolean escaped = false;
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (!escaped) {
                if (ch == '\\') {
                    escaped = true;
                } else {
                    builder.append(ch);
                }
                continue;
            }
            switch (ch) {
                case '"' -> builder.append('"');
                case '\\' -> builder.append('\\');
                case 'n' -> builder.append('\n');
                case 'r' -> builder.append('\r');
                case 't' -> builder.append('\t');
                default -> builder.append(ch);
            }
            escaped = false;
        }
        if (escaped) {
            builder.append('\\');
        }
        return builder.toString();
    }

    private static void write(Path path, String value) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, value, StandardCharsets.UTF_8);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hashed.length * 2);
            for (byte b : hashed) {
                builder.append(Character.forDigit((b >>> 4) & 0x0F, 16));
                builder.append(Character.forDigit(b & 0x0F, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 digest is unavailable", exception);
        }
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < safe.length(); index++) {
            char ch = safe.charAt(index);
            switch (ch) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> builder.append(ch);
            }
        }
        return builder.toString();
    }

    private record LifecycleTraceSummary(
            int descriptorCount,
            int activeRuntimeCount,
            int failedModuleCount,
            int graphIssueCount,
            String activeModulesJson,
            String failedModulesJson
    ) {
        private String status() {
            return descriptorCount > 0
                    && activeRuntimeCount > 0
                    && failedModuleCount == 0
                    && graphIssueCount == 0
                    ? "PASS"
                    : "WARN";
        }
    }

    record Generated(List<String> entries, List<String> diagnostics) {
        Generated {
            entries = List.copyOf(entries);
            diagnostics = List.copyOf(diagnostics);
        }
    }

    private record Snapshot(
            Path repoRoot,
            List<Path> moduleRoots,
            EchoRuntimeModuleRuntimeResult modules,
            EchoAdapterCoreStandaloneContentBridge bridge,
            EchoAdapterCoreModuleCoverageReport coverage
    ) {
        Snapshot {
            moduleRoots = List.copyOf(moduleRoots);
        }
    }
}
