package dev.echo.standalone.runtime.client;

import dev.echo.nativeplatform.contracts.EchoNativeRegisteredService;
import dev.echo.nativeplatform.contracts.EchoNativeServiceRegistry;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoRuntimeDiagnosticCollector;
import dev.echo.standalone.runtime.data.EchoDataJson;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleIssue;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleLifecycle;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class EchoClientModuleBootstrap {
    private EchoClientModuleBootstrap() {
    }

    static EchoClientModuleBootstrapResult boot(EchoClientLaunchContext launchContext) {
        EchoClientLaunchContext context = launchContext == null
                ? EchoClientLaunchContext.empty()
                : launchContext;
        if (!context.strictPackMode()) {
            return EchoClientModuleBootstrapResult.inactive();
        }
        try {
            PackManifest manifest = PackManifest.load(context);
            List<String> manifestFailures = manifest.validateInstalledFiles(context.safeMode());
            if (!manifestFailures.isEmpty() && !context.safeMode()) {
                throw new IllegalStateException("Installed pack validation failed: "
                        + String.join("; ", manifestFailures));
            }

            List<Path> roots = moduleRoots(context, manifest);
            if (roots.isEmpty()) {
                String message = "No installed standalone module jars found under " + context.modulesRoot();
                if (!context.safeMode()) {
                    throw new IllegalStateException(message);
                }
                return EchoClientModuleBootstrapResult.inactive(message, true, true);
            }

            EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
            EchoRuntimeDiagnosticCollector diagnostics = new EchoRuntimeDiagnosticCollector();
            services.register(EchoRuntimeDiagnosticSink.class, diagnostics);
            services.register(EchoRuntimeDiagnosticCollector.class, diagnostics);

            EchoRuntimeModuleManager manager = EchoRuntimeModuleManager.executableAbiV1();
            EchoRuntimeModuleRuntimeResult moduleRuntimeResult = manager.run(roots, services);
            List<String> runtimeFailures = validateRuntimeResult(moduleRuntimeResult, manifest);
            if (!runtimeFailures.isEmpty() && !context.safeMode()) {
                manager.unload(moduleRuntimeResult, services);
                throw new IllegalStateException("Installed module graph failed: "
                        + String.join("; ", runtimeFailures));
            }

            List<Map<String, Object>> rows = adapterCoreRows(services);
            EchoClientModScanSummary summary = summary(moduleRuntimeResult, roots, concat(manifestFailures, runtimeFailures));
            return EchoClientModuleBootstrapResult.active(
                    true,
                    context.safeMode(),
                    manager,
                    services,
                    moduleRuntimeResult,
                    diagnostics,
                    summary,
                    roots,
                    rows
            );
        } catch (IOException | RuntimeException exception) {
            if (context.safeMode()) {
                return EchoClientModuleBootstrapResult.inactive(exception.getMessage(), true, true);
            }
            throw new IllegalStateException("Strict installed-pack module bootstrap failed: "
                    + exception.getMessage(), exception);
        }
    }

    private static List<Path> moduleRoots(EchoClientLaunchContext context, PackManifest manifest) throws IOException {
        LinkedHashSet<Path> roots = new LinkedHashSet<>();
        Path modulesRoot = context.modulesRoot();
        if (modulesRoot != null && Files.isDirectory(modulesRoot)) {
            try (var stream = Files.list(modulesRoot)) {
                for (Path child : stream
                        .filter(Files::isRegularFile)
                        .filter(EchoClientModuleBootstrap::standaloneArchive)
                        .sorted(Comparator.comparing(Path::toString))
                        .toList()) {
                    roots.add(child.toAbsolutePath().normalize());
                }
            }
        }
        for (ModuleFile file : manifest.files()) {
            if (file.required() && Files.isRegularFile(file.absolutePath()) && standaloneArchive(file.absolutePath())) {
                roots.add(file.absolutePath());
            }
        }
        return List.copyOf(roots);
    }

    private static List<String> validateRuntimeResult(
            EchoRuntimeModuleRuntimeResult result,
            PackManifest manifest
    ) {
        ArrayList<String> failures = new ArrayList<>();
        EchoRuntimeModuleRegistry registry = result.registry();
        if (result.moduleGraph().hasBlockingIssues()) {
            for (EchoRuntimeModuleIssue issue : result.moduleGraph().issues()) {
                if (issue.severity() == EchoRuntimeModuleIssue.Severity.ERROR) {
                    failures.add(issue.code() + ": " + issue.summary());
                }
            }
        }
        for (String moduleId : manifest.requiredModuleIds()) {
            if (registry.find(moduleId).isEmpty()) {
                failures.add(moduleId + " missing from executable module registry");
                continue;
            }
            EchoRuntimeModuleStatus status = registry.runtimeStatus(moduleId);
            EchoRuntimeModuleLifecycle lifecycle = registry.lifecycle(moduleId);
            if (status != EchoRuntimeModuleStatus.RUNTIME_ACTIVE) {
                failures.add(moduleId + " is " + status.id());
            }
            if (lifecycle == EchoRuntimeModuleLifecycle.FAILED || lifecycle == EchoRuntimeModuleLifecycle.DISABLED) {
                failures.add(moduleId + " lifecycle is " + lifecycle.name());
            }
        }
        return List.copyOf(failures);
    }

    private static EchoClientModScanSummary summary(
            EchoRuntimeModuleRuntimeResult result,
            List<Path> roots,
            List<String> validationFailures
    ) {
        EchoRuntimeModuleRegistry registry = result.registry();
        ArrayList<EchoClientModSummary> modules = new ArrayList<>();
        for (EchoRuntimeModuleDescriptor descriptor : registry.descriptors()) {
            EchoRuntimeModuleStatus status = registry.runtimeStatus(descriptor.id());
            modules.add(toSummary(descriptor, status, registry.notes(descriptor.id())));
        }
        modules.sort(Comparator.comparing(EchoClientModSummary::id));
        List<EchoRuntimeModuleIssue> issues = result.moduleGraph().issues();
        int runtimeErrors = issueCount(issues, EchoRuntimeModuleIssue.Severity.ERROR);
        int runtimeWarnings = issueCount(issues, EchoRuntimeModuleIssue.Severity.WARNING);
        int validationErrors = validationFailures == null ? 0 : validationFailures.size();
        String lastError = validationErrors == 0 ? "" : String.join("; ", validationFailures);
        return new EchoClientModScanSummary(
                modules,
                roots.stream().map(Path::toString).toList(),
                statusCounts(modules),
                issues.size() + validationErrors,
                runtimeErrors + validationErrors,
                runtimeWarnings,
                lastError
        );
    }

    private static EchoClientModSummary toSummary(
            EchoRuntimeModuleDescriptor descriptor,
            EchoRuntimeModuleStatus status,
            List<String> notes
    ) {
        Map<String, Object> adapterCore = object(descriptor.access().get("adapterCore"));
        List<String> domains = stringList(adapterCore.get("domains"));
        List<String> runtimes = stringList(adapterCore.get("runtimes"));
        String nativeEntrypoint = descriptor.nativeEntrypoint();
        return new EchoClientModSummary(
                descriptor.id(),
                descriptor.name(),
                descriptor.version(),
                descriptor.kind(),
                descriptor.side().id(),
                descriptor.standalone(),
                descriptor.official(),
                !nativeEntrypoint.isBlank(),
                !domains.isEmpty() || !runtimes.isEmpty(),
                status.id(),
                descriptor.descriptorPath().toString(),
                descriptor.requires().size(),
                descriptor.optional().size(),
                domains,
                runtimes,
                notes.isEmpty() ? status.id() : String.join("; ", notes)
        );
    }

    private static List<Map<String, Object>> adapterCoreRows(EchoDefaultRuntimeServiceRegistry services) {
        EchoNativeServiceRegistry nativeServices = services.find(EchoNativeServiceRegistry.class).orElse(null);
        if (nativeServices == null) {
            return List.of();
        }
        LinkedHashMap<String, Map<String, Object>> rows = new LinkedHashMap<>();
        for (EchoNativeRegisteredService descriptor : nativeServices.registeredServices()) {
            Object service = nativeServices.service(descriptor.moduleId(), descriptor.serviceId()).orElse(null);
            Map<String, Object> serviceMap = object(service);
            if (serviceMap.isEmpty()) {
                continue;
            }
            if ("registry_registration".equals(text(serviceMap.get("kind")))) {
                Map<String, Object> evidence = object(serviceMap.get("evidence"));
                addRow(rows, rowFromRegistration(descriptor, evidence));
            }
            for (Map<String, Object> registration : registryRegistrations(serviceMap)) {
                addRow(rows, rowFromRegistration(descriptor, registration));
            }
        }
        return List.copyOf(rows.values());
    }

    private static void addRow(LinkedHashMap<String, Map<String, Object>> rows, Map<String, Object> row) {
        String contentId = text(row.get("contentId"));
        if (!contentId.isBlank()) {
            rows.put(contentId, row);
        }
    }

    private static Map<String, Object> rowFromRegistration(
            EchoNativeRegisteredService service,
            Map<String, Object> registration
    ) {
        String registry = firstText(registration.get("registry"), registration.get("domain"), "diagnostics");
        String id = firstText(registration.get("id"), registration.get("contentId"), service.serviceId());
        String moduleId = firstText(registration.get("moduleId"), service.moduleId());
        String contentId = contentId(moduleId, id);
        String domain = domainForRegistry(registry);
        String contentKind = kindForRegistry(registry);
        LinkedHashMap<String, Object> metadata = new LinkedHashMap<>(registration);
        metadata.put("source", "native-activation-surface");
        metadata.put("serviceId", service.serviceId());
        metadata.put("serviceImplementation", service.implementationClass());
        metadata.put("surfaces", service.surfaces());
        metadata.putIfAbsent("moduleId", moduleId);
        metadata.putIfAbsent("registry", registry);

        LinkedHashMap<String, Object> row = new LinkedHashMap<>();
        row.put("contentId", contentId);
        row.put("domain", domain);
        row.put("contentKind", contentKind);
        row.put("moduleId", moduleId);
        row.put("displayName", firstText(
                registration.get("displayName"),
                registration.get("name"),
                registration.get("summary"),
                displayName(id)
        ));
        row.put("adapterKey", firstText(registration.get("adapterKey"), registry + "." + id));
        row.put("nativeLoaderId", firstText(registration.get("nativeLoaderId"), contentId));
        row.put("standaloneRuntimeId", firstText(registration.get("standaloneRuntimeId"), contentId));
        row.put("metadata", Map.copyOf(metadata));
        return Map.copyOf(row);
    }

    private static List<Map<String, Object>> registryRegistrations(Map<String, Object> serviceMap) {
        Map<String, Object> registryBridge = object(serviceMap.get("registryBridge"));
        if (registryBridge.isEmpty()) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        result.addAll(objectList(registryBridge.get("registrations")));
        result.addAll(objectList(registryBridge.get("entries")));
        return List.copyOf(result);
    }

    private static String contentId(String moduleId, String id) {
        String cleanId = text(id).replace('\\', '/');
        if (cleanId.contains(":")) {
            return cleanId;
        }
        String cleanModule = text(moduleId);
        return cleanModule.isBlank() ? cleanId : cleanModule + ":" + cleanId;
    }

    private static String domainForRegistry(String registry) {
        String value = text(registry).toLowerCase(Locale.ROOT);
        if (value.contains("block")) return "blocks";
        if (value.contains("item") || value.contains("inventory")) return "items";
        if (value.contains("entity") || value.contains("creature")) return "entities";
        if (value.contains("recipe")) return "recipes";
        if (value.contains("loot")) return "loot";
        if (value.contains("structure")) return "structures";
        if (value.contains("biome")) return "biomes";
        if (value.contains("worldgen") || value.contains("feature")) return "worldgen";
        if (value.contains("menu") || value.contains("screen") || value.contains("ui")) return "ui_screens";
        if (value.contains("sound") || value.contains("audio")) return "sounds";
        if (value.contains("hazard") || value.contains("weather")) return "hazards";
        if (value.contains("mission") || value.contains("quest") || value.contains("progression")) return "missions";
        if (value.contains("command")) return "commands";
        if (value.contains("save")) return "saves";
        return "diagnostics";
    }

    private static String kindForRegistry(String registry) {
        return switch (domainForRegistry(registry)) {
            case "blocks" -> "BLOCK";
            case "items" -> "ITEM";
            case "entities" -> "ENTITY";
            case "recipes" -> "RECIPE";
            case "loot" -> "LOOT_TABLE";
            case "structures" -> "STRUCTURE";
            case "biomes", "worldgen" -> "WORLDGEN_DEFINITION";
            case "ui_screens" -> "UI_SCREEN";
            case "sounds" -> "SOUND_EVENT";
            case "hazards" -> "WORLD_HAZARD";
            case "missions" -> "MISSION";
            case "commands" -> "COMMAND";
            case "saves" -> "SAVE_RECORD";
            default -> "DIAGNOSTIC";
        };
    }

    private static boolean standaloneArchive(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith("-standalone.jar") || name.endsWith(".echo-addon");
    }

    private static LinkedHashMap<String, Integer> statusCounts(List<EchoClientModSummary> modules) {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        for (EchoClientModSummary module : modules) {
            counts.merge(module.runtimeStatus(), 1, Integer::sum);
        }
        return counts;
    }

    private static int issueCount(List<EchoRuntimeModuleIssue> issues, EchoRuntimeModuleIssue.Severity severity) {
        return (int) issues.stream().filter(issue -> issue.severity() == severity).count();
    }

    private static List<String> concat(List<String> left, List<String> right) {
        ArrayList<String> result = new ArrayList<>();
        if (left != null) {
            result.addAll(left);
        }
        if (right != null) {
            result.addAll(right);
        }
        return List.copyOf(result);
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(EchoClientModuleBootstrap::text)
                .filter(item -> !item.isBlank())
                .sorted()
                .toList();
    }

    private static List<Map<String, Object>> objectList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> object = object(item);
            if (!object.isEmpty()) {
                result.add(object);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> raw)) {
            return Map.of();
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : raw.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static String displayName(String value) {
        String text = text(value);
        int slash = text.lastIndexOf('/');
        int colon = text.lastIndexOf(':');
        int start = Math.max(slash, colon) + 1;
        if (start > 0 && start < text.length()) {
            text = text.substring(start);
        }
        text = text.replace('_', ' ').replace('-', ' ');
        if (text.isBlank()) {
            return "Runtime Content";
        }
        StringBuilder result = new StringBuilder();
        for (String part : text.split("\\s+")) {
            if (part.isBlank()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return result.isEmpty() ? "Runtime Content" : result.toString();
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

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record PackManifest(Path manifestPath, Path packRoot, List<ModuleFile> files) {
        static PackManifest load(EchoClientLaunchContext context) throws IOException {
            Path manifestPath = resolveManifestPath(context);
            if (manifestPath == null || !Files.isRegularFile(manifestPath)) {
                throw new IllegalStateException("Missing installed pack manifest under " + context.packRoot());
            }
            Map<String, Object> manifest = object(EchoDataJson.parse(Files.readString(manifestPath)));
            if (manifest.isEmpty()) {
                throw new IllegalStateException("Pack manifest is empty: " + manifestPath);
            }
            Path packRoot = context.packRoot() == null
                    ? manifestPath.toAbsolutePath().normalize().getParent()
                    : context.packRoot();
            ArrayList<ModuleFile> files = new ArrayList<>();
            for (Map<String, Object> row : objectList(manifest.get("files"))) {
                String artifactFamily = text(row.get("artifactFamily"));
                if (!artifactFamily.isBlank() && !"standalone".equalsIgnoreCase(artifactFamily)) {
                    continue;
                }
                String moduleId = firstText(row.get("moduleId"), row.get("id"));
                if (moduleId.isBlank()) {
                    continue;
                }
                String relativePath = firstText(
                        row.get("path"),
                        "mods/" + firstText(row.get("assetName"), row.get("artifactName"))
                );
                Path absolutePath = absolute(packRoot, relativePath);
                files.add(new ModuleFile(
                        moduleId,
                        absolutePath,
                        text(row.get("sha256")),
                        booleanValue(row.get("required"), true)
                ));
            }
            return new PackManifest(manifestPath, packRoot, List.copyOf(files));
        }

        List<String> requiredModuleIds() {
            return files.stream()
                    .filter(ModuleFile::required)
                    .map(ModuleFile::moduleId)
                    .distinct()
                    .sorted()
                    .toList();
        }

        List<String> validateInstalledFiles(boolean safeMode) {
            ArrayList<String> failures = new ArrayList<>();
            for (ModuleFile file : files) {
                if (!file.required()) {
                    continue;
                }
                if (!Files.isRegularFile(file.absolutePath())) {
                    failures.add(file.moduleId() + " missing artifact " + file.absolutePath());
                    continue;
                }
                if (!file.sha256().isBlank() && file.sha256().matches("(?i)[a-f0-9]{64}")) {
                    String actual = sha256(file.absolutePath());
                    if (!file.sha256().equalsIgnoreCase(actual)) {
                        failures.add(file.moduleId() + " checksum mismatch expected "
                                + file.sha256() + " actual " + actual);
                    }
                }
            }
            if (!safeMode && files.stream().noneMatch(ModuleFile::required)) {
                failures.add("pack manifest declares no required standalone module files");
            }
            return List.copyOf(failures);
        }

        private static Path resolveManifestPath(EchoClientLaunchContext context) {
            if (context.packManifest() != null && Files.isRegularFile(context.packManifest())) {
                return context.packManifest();
            }
            Path packRoot = context.packRoot();
            if (packRoot == null) {
                return null;
            }
            List<Path> candidates = List.of(
                    packRoot.resolve(".echo").resolve("pack-manifest.json"),
                    packRoot.resolve("pack-manifest.json"),
                    packRoot.resolve("release-manifest.template.json")
            );
            return candidates.stream()
                    .map(path -> path.toAbsolutePath().normalize())
                    .filter(Files::isRegularFile)
                    .findFirst()
                    .orElse(null);
        }

        private static Path absolute(Path packRoot, String relativePath) {
            Path path = Path.of(relativePath);
            if (path.isAbsolute()) {
                return path.toAbsolutePath().normalize();
            }
            return packRoot.resolve(path).toAbsolutePath().normalize();
        }

        private static boolean booleanValue(Object value, boolean fallback) {
            if (value instanceof Boolean bool) {
                return bool;
            }
            String text = text(value);
            return text.isBlank() ? fallback : Boolean.parseBoolean(text);
        }

        private static String sha256(Path file) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                try (InputStream input = Files.newInputStream(file)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = input.read(buffer)) >= 0) {
                        if (read > 0) {
                            digest.update(buffer, 0, read);
                        }
                    }
                }
                return HexFormat.of().formatHex(digest.digest());
            } catch (Exception exception) {
                throw new IllegalStateException("Unable to hash " + file + ": " + exception.getMessage(), exception);
            }
        }
    }

    private record ModuleFile(String moduleId, Path absolutePath, String sha256, boolean required) {
        private ModuleFile {
            moduleId = text(moduleId);
            sha256 = text(sha256);
            absolutePath = absolutePath.toAbsolutePath().normalize();
        }
    }
}
