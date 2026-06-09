package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleDescriptor;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleIssue;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleManager;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EchoClientModScanService {
    private EchoClientModScanSummary summary = EchoClientModScanSummary.empty();

    EchoClientModScanService() {
        refresh();
    }

    void refresh() {
        try {
            List<Path> roots = candidateRoots();
            EchoRuntimeModuleRuntimeResult result = EchoRuntimeModuleManager.descriptorOnly()
                    .run(roots, new EchoDefaultRuntimeServiceRegistry());
            EchoRuntimeModuleRegistry registry = result.registry();
            ArrayList<EchoClientModSummary> modules = new ArrayList<>();
            for (EchoRuntimeModuleDescriptor descriptor : registry.descriptors()) {
                EchoRuntimeModuleStatus status = registry.runtimeStatus(descriptor.id());
                modules.add(toSummary(descriptor, status, registry.notes(descriptor.id())));
            }
            modules.sort(Comparator.comparing(EchoClientModSummary::id));
            List<EchoRuntimeModuleIssue> issues = result.moduleGraph().issues();
            summary = new EchoClientModScanSummary(
                    modules,
                    roots.stream().map(Path::toString).toList(),
                    statusCounts(modules),
                    issues.size(),
                    issueCount(issues, EchoRuntimeModuleIssue.Severity.ERROR),
                    issueCount(issues, EchoRuntimeModuleIssue.Severity.WARNING),
                    ""
            );
        } catch (RuntimeException | IOException exception) {
            summary = new EchoClientModScanSummary(
                    List.of(),
                    List.of(),
                    Map.of(),
                    0,
                    1,
                    0,
                    exception.getMessage()
            );
        }
    }

    EchoClientModScanSummary summary() {
        return summary;
    }

    private static EchoClientModSummary toSummary(
            EchoRuntimeModuleDescriptor descriptor,
            EchoRuntimeModuleStatus status,
            List<String> notes
    ) {
        Map<String, Object> adapterCore = object(descriptor.access().get("adapterCore"));
        List<String> domains = stringList(adapterCore.get("domains"));
        List<String> runtimes = stringList(adapterCore.get("runtimes"));
        String nativeEntrypoint = descriptor.adapterCoreEntrypoint();
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
                relativePath(descriptor.descriptorPath()),
                descriptor.requires().size(),
                descriptor.optional().size(),
                domains,
                runtimes,
                notes.isEmpty() ? status.id() : String.join("; ", notes)
        );
    }

    private static List<Path> candidateRoots() throws IOException {
        ArrayList<Path> roots = new ArrayList<>();
        for (Path runtimeRoot : EchoClientWorkspaceRoots.standaloneRuntimeRoots()) {
            addIfDirectory(roots, runtimeRoot.resolve("mods"));
            addIfDirectory(roots, runtimeRoot.resolve("modules"));
        }
        for (Path echoRoot : EchoClientWorkspaceRoots.echoWorkspaceRoots()) {
            addIfDirectory(roots, echoRoot.resolve("src/main/resources"));
            addIfDirectory(roots, echoRoot.resolve("addons"));
            addIfDirectory(roots, echoRoot.resolve("core"));
            addIfDirectory(roots, echoRoot.resolve("ECHO-Modules/addons"));
            addIfDirectory(roots, echoRoot.resolve("ECHO-Modules/core"));
        }
        for (Path addonsRoot : EchoClientWorkspaceRoots.echoModuleAddonRoots()) {
            addIfDirectory(roots, addonsRoot);
        }
        return List.copyOf(roots);
    }

    private static void addIfDirectory(ArrayList<Path> roots, Path path) throws IOException {
        Path normalized = path.toAbsolutePath().normalize();
        if (Files.isDirectory(normalized) && !roots.contains(normalized)) {
            roots.add(normalized);
        }
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

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream()
                .map(item -> item == null ? "" : String.valueOf(item).trim())
                .filter(item -> !item.isBlank())
                .sorted()
                .toList();
    }

    private static String relativePath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        for (Path echoRoot : EchoClientWorkspaceRoots.echoWorkspaceRoots()) {
            if (normalized.startsWith(echoRoot)) {
                return echoRoot.relativize(normalized).toString().replace('\\', '/');
            }
        }
        for (Path addonsRoot : EchoClientWorkspaceRoots.echoModuleAddonRoots()) {
            if (normalized.startsWith(addonsRoot)) {
                return addonsRoot.getFileName() + "/" + addonsRoot.relativize(normalized).toString().replace('\\', '/');
            }
        }
        for (Path runtimeRoot : EchoClientWorkspaceRoots.standaloneRuntimeRoots()) {
            if (normalized.startsWith(runtimeRoot)) {
                return runtimeRoot.relativize(normalized).toString().replace('\\', '/');
            }
        }
        Path echoRoot = Path.of(".").toAbsolutePath().normalize().getParent();
        if (echoRoot != null && normalized.startsWith(echoRoot)) {
            return echoRoot.relativize(normalized).toString().replace('\\', '/');
        }
        return normalized.toString().replace('\\', '/');
    }
}
