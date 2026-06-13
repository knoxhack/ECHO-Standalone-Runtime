package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class EchoRuntimeModuleManager {
    private final EchoRuntimeModuleDescriptorScanner scanner;
    private final EchoRuntimeModuleDependencyResolver dependencyResolver;
    private final EchoRuntimeModuleTrustPolicy trustPolicy;
    private final EchoRuntimeModuleLoader loader;
    private final EchoRuntimeModuleServiceBinder serviceBinder;

    public EchoRuntimeModuleManager(
            EchoRuntimeModuleDescriptorScanner scanner,
            EchoRuntimeModuleDependencyResolver dependencyResolver,
            EchoRuntimeModuleTrustPolicy trustPolicy,
            EchoRuntimeModuleLoader loader,
            EchoRuntimeModuleServiceBinder serviceBinder
    ) {
        this.scanner = Objects.requireNonNull(scanner, "scanner");
        this.dependencyResolver = Objects.requireNonNull(dependencyResolver, "dependencyResolver");
        this.trustPolicy = Objects.requireNonNull(trustPolicy, "trustPolicy");
        this.loader = Objects.requireNonNull(loader, "loader");
        this.serviceBinder = Objects.requireNonNull(serviceBinder, "serviceBinder");
    }

    public static EchoRuntimeModuleManager descriptorOnly() {
        EchoRuntimeModuleSandboxPolicy sandboxPolicy = EchoRuntimeModuleSandboxPolicy.descriptorOnlyPolicy();
        return new EchoRuntimeModuleManager(
                new EchoRuntimeModuleDescriptorScanner(new EchoRuntimeModuleDescriptorParser()),
                new EchoRuntimeModuleDependencyResolver(),
                EchoRuntimeModuleTrustPolicy.sandboxed(),
                new EchoRuntimeModuleLoader(sandboxPolicy),
                new EchoRuntimeModuleServiceBinder(sandboxPolicy)
        );
    }

    public static EchoRuntimeModuleManager executableAbiV1() {
        EchoRuntimeModuleSandboxPolicy sandboxPolicy = EchoRuntimeModuleSandboxPolicy.executableAbiV1Policy();
        return new EchoRuntimeModuleManager(
                new EchoRuntimeModuleDescriptorScanner(new EchoRuntimeModuleDescriptorParser()),
                new EchoRuntimeModuleDependencyResolver(),
                EchoRuntimeModuleTrustPolicy.sandboxed(),
                new EchoRuntimeModuleLoader(sandboxPolicy),
                new EchoRuntimeModuleServiceBinder(sandboxPolicy)
        );
    }

    public EchoRuntimeModuleRuntimeResult run(List<Path> roots, EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(roots, "roots");
        Objects.requireNonNull(services, "services");
        EchoRuntimeModuleLifecycleBus lifecycleBus = services.find(EchoRuntimeModuleLifecycleBus.class)
                .orElseGet(() -> {
                    EchoRuntimeModuleLifecycleBus bus = new EchoRuntimeModuleLifecycleBus();
                    services.register(EchoRuntimeModuleLifecycleBus.class, bus);
                    return bus;
                });
        EchoRuntimeModuleScanResult scanResult = scanner.scan(roots);
        EchoRuntimeModuleRegistry registry = new EchoRuntimeModuleRegistry();
        for (EchoRuntimeModuleDescriptor descriptor : scanResult.descriptors()) {
            registry.register(descriptor);
            transitionAll(
                    registry,
                    lifecycleBus,
                    descriptor.id(),
                    List.of(
                            EchoRuntimeModuleLifecycle.DISCOVERED,
                            EchoRuntimeModuleLifecycle.DESCRIPTOR_VALIDATED
                    ),
                    "module_manager.scan"
            );
        }

        EchoRuntimeModuleGraph dependencyGraph = dependencyResolver.resolve(scanResult.descriptors());
        EchoRuntimeFeatureGraph featureGraph = EchoRuntimeFeatureGraph.from(scanResult.descriptors());
        List<EchoRuntimeModuleIssue> trustIssues = trustPolicy.validate(scanResult.descriptors());
        EchoRuntimeModuleGraph graph = new EchoRuntimeModuleGraph(
                dependencyGraph.moduleIds(),
                dependencyGraph.dependencyEdges(),
                concat(scanResult.issues(), dependencyGraph.issues(), trustIssues),
                concatIds(dependencyGraph.failedModuleIds(), trustIssues.stream()
                        .filter(issue -> issue.severity() == EchoRuntimeModuleIssue.Severity.ERROR)
                        .map(EchoRuntimeModuleIssue::moduleId)
                        .filter(Objects::nonNull)
                        .toList())
        );
        services.register(EchoRuntimeModuleContentActivationRegistry.class, new EchoRuntimeModuleContentActivationRegistry());
        services.register(EchoRuntimeModuleServiceExportRegistry.class, new EchoRuntimeModuleServiceExportRegistry());
        services.register(EchoRuntimeModuleDataRegistry.class, new EchoRuntimeModuleDataRegistry());
        classifyRuntimeStatuses(registry, graph);
        loader.load(registry, graph, services);
        serviceBinder.bind(services, registry, graph, featureGraph);

        for (EchoRuntimeModuleDescriptor descriptor : registry.descriptors()) {
            if (registry.lifecycle(descriptor.id()) == EchoRuntimeModuleLifecycle.FAILED) {
                continue;
            }
            if (registry.runtimeStatus(descriptor.id()) == EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON) {
                transition(registry, lifecycleBus, descriptor.id(), EchoRuntimeModuleLifecycle.DISABLED, "module_manager.runtime_status");
                continue;
            }
            transitionAll(
                    registry,
                    lifecycleBus,
                    descriptor.id(),
                    List.of(
                            EchoRuntimeModuleLifecycle.SERVICES_BOUND,
                            EchoRuntimeModuleLifecycle.COMMON_INIT,
                            EchoRuntimeModuleLifecycle.READY
                    ),
                    "module_manager.init"
            );
        }

        if (!graph.issues().isEmpty()) {
            services.find(dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink.class).ifPresent(sink -> {
                for (EchoRuntimeModuleIssue issue : graph.issues()) {
                    sink.emit(new EchoRuntimeDiagnostic(
                            issue.code(),
                            issue.severity() == EchoRuntimeModuleIssue.Severity.ERROR
                                    ? EchoRuntimeDiagnosticSeverity.ERROR
                                    : EchoRuntimeDiagnosticSeverity.WARNING,
                            "module_runtime",
                            issue.summary(),
                            "",
                            Map.of("moduleId", issue.moduleId() == null ? "" : issue.moduleId())
                    ));
                }
            });
        }
        return new EchoRuntimeModuleRuntimeResult(registry, graph, featureGraph);
    }

    public void reloadData(EchoRuntimeModuleRuntimeResult result, EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(services, "services");
        loader.reloadData(result.registry(), services);
    }

    public void unload(EchoRuntimeModuleRuntimeResult result, EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(services, "services");
        loader.unload(result.registry(), services);
    }

    private static void classifyRuntimeStatuses(EchoRuntimeModuleRegistry registry, EchoRuntimeModuleGraph graph) {
        for (EchoRuntimeModuleDescriptor descriptor : registry.descriptors()) {
            if (graph.failedModuleIds().contains(descriptor.id())) {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON,
                        "module graph failed dependency or trust validation"
                );
            } else if (!descriptor.standalone()) {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON,
                        "descriptor standalone=false"
                );
            } else if (forceStandaloneExecution(descriptor)) {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                        "descriptor access.forceStandaloneExecution=true"
                );
            } else if (isDevOnly(descriptor)) {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_DEV_ONLY,
                        "descriptor is limited to development or example workflows"
                );
            } else if (isToolingOnly(descriptor)) {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_TOOLING_ONLY,
                        "descriptor provides runtime tooling or diagnostics, not direct gameplay"
                );
            } else {
                registry.setRuntimeStatus(
                        descriptor.id(),
                        EchoRuntimeModuleStatus.RUNTIME_ACTIVE,
                        "descriptor is eligible for standalone runtime activation"
                );
            }
        }
    }

    private static void transition(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleLifecycleBus lifecycleBus,
            String moduleId,
            EchoRuntimeModuleLifecycle lifecycle,
            String source
    ) {
        registry.transition(moduleId, lifecycle);
        lifecycleBus.publish(moduleId, lifecycle, source);
    }

    private static void transitionAll(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleLifecycleBus lifecycleBus,
            String moduleId,
            List<EchoRuntimeModuleLifecycle> states,
            String source
    ) {
        for (EchoRuntimeModuleLifecycle state : states) {
            transition(registry, lifecycleBus, moduleId, state, source);
        }
    }

    private static boolean isDevOnly(EchoRuntimeModuleDescriptor descriptor) {
        if (descriptor.side() == EchoRuntimeModuleSide.DEV) {
            return true;
        }
        String role = descriptor.role().toLowerCase(Locale.ROOT);
        if (role.contains("developer") || role.contains("example")) {
            return true;
        }
        return descriptor.gameModes().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals("dev_tools") || value.equals("creator_tools"))
                && !descriptor.official();
    }

    private static boolean isToolingOnly(EchoRuntimeModuleDescriptor descriptor) {
        String kind = descriptor.kind().toLowerCase(Locale.ROOT);
        String role = descriptor.role().toLowerCase(Locale.ROOT);
        if (kind.equals("tooling")) {
            return true;
        }
        return role.contains("agent")
                || role.contains("report")
                || role.contains("metadata")
                || role.contains("module_graph")
                || role.contains("asset_tooling");
    }

    private static boolean forceStandaloneExecution(EchoRuntimeModuleDescriptor descriptor) {
        Object value = descriptor.access().get("forceStandaloneExecution");
        return Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    @SafeVarargs
    private static <T> List<T> concat(List<T>... lists) {
        java.util.ArrayList<T> all = new java.util.ArrayList<>();
        for (List<T> list : lists) {
            all.addAll(list);
        }
        return List.copyOf(all);
    }

    private static List<String> concatIds(List<String> left, List<String> right) {
        java.util.TreeSet<String> all = new java.util.TreeSet<>();
        all.addAll(left);
        all.addAll(right);
        return all.stream().toList();
    }
}
