package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class EchoRuntimeModuleLoader {
    private final EchoRuntimeModuleSandboxPolicy sandboxPolicy;
    private final Map<String, LoadedModule> loadedModules = new LinkedHashMap<>();
    private final List<String> activationOrder = new ArrayList<>();

    public EchoRuntimeModuleLoader(EchoRuntimeModuleSandboxPolicy sandboxPolicy) {
        this.sandboxPolicy = Objects.requireNonNull(sandboxPolicy, "sandboxPolicy");
    }

    public void load(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleGraph graph,
            EchoRuntimeServiceRegistry services
    ) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(services, "services");
        EchoRuntimeModuleLifecycleBus lifecycleBus = lifecycleBus(services);
        for (String moduleId : graph.dependencyOrderedModuleIds()) {
            EchoRuntimeModuleDescriptor descriptor = registry.find(moduleId).orElse(null);
            if (descriptor == null) {
                continue;
            }
            if (graph.failedModuleIds().contains(descriptor.id())) {
                transition(registry, lifecycleBus, descriptor.id(), EchoRuntimeModuleLifecycle.FAILED, "module_loader.graph");
                continue;
            }
            String failedDependency = firstFailedRequiredDependency(registry, descriptor);
            if (!failedDependency.isBlank()) {
                failRequiredDependency(registry, services, descriptor, failedDependency);
                continue;
            }
            transitionAll(
                    registry,
                    lifecycleBus,
                    descriptor.id(),
                    List.of(
                            EchoRuntimeModuleLifecycle.DEPENDENCIES_RESOLVED,
                            EchoRuntimeModuleLifecycle.FEATURES_RESOLVED,
                            EchoRuntimeModuleLifecycle.TRUST_VALIDATED,
                            EchoRuntimeModuleLifecycle.LOADED
                    ),
                    "module_loader.load"
            );
            if (sandboxPolicy.descriptorOnly()) {
                registry.note(descriptor.id(), "descriptor-only load; no classloader or module code execution");
                continue;
            }
            if (!sandboxPolicy.classloaderCreationAllowed() || !sandboxPolicy.moduleCodeExecutionAllowed()) {
                registry.note(descriptor.id(), "module execution skipped by sandbox policy");
                continue;
            }
            if (registry.runtimeStatus(descriptor.id()) != EchoRuntimeModuleStatus.RUNTIME_ACTIVE) {
                registry.note(descriptor.id(), "module execution skipped for status " + registry.runtimeStatus(descriptor.id()).id());
                continue;
            }
            if (descriptor.executableEntrypoint().isBlank()) {
                registry.note(descriptor.id(), "module execution skipped; descriptor has no ABI v1 entrypoint");
                continue;
            }
            executeLoad(registry, services, descriptor);
        }
    }

    private static String firstFailedRequiredDependency(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleDescriptor descriptor
    ) {
        for (String dependencyId : descriptor.requires()) {
            if (registry.find(dependencyId).isPresent()
                    && registry.lifecycle(dependencyId) == EchoRuntimeModuleLifecycle.FAILED) {
                return dependencyId;
            }
        }
        return "";
    }

    private void failRequiredDependency(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleDescriptor descriptor,
            String dependencyId
    ) {
        transition(
                registry,
                lifecycleBus(services),
                descriptor.id(),
                EchoRuntimeModuleLifecycle.FAILED,
                "module_loader.dependency_failure"
        );
        String summary = "required dependency failed before module load";
        registry.note(descriptor.id(), summary + ": " + dependencyId);
        services.find(EchoRuntimeDiagnosticSink.class).ifPresent(sink -> sink.emit(new EchoRuntimeDiagnostic(
                "echo.runtime.module.required_dependency_failed",
                EchoRuntimeDiagnosticSeverity.ERROR,
                "module_runtime",
                summary,
                dependencyId,
                Map.of("moduleId", descriptor.id(), "dependencyId", dependencyId)
        )));
    }

    public void reloadData(EchoRuntimeModuleRegistry registry, EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(services, "services");
        EchoRuntimeModuleLifecycleBus lifecycleBus = lifecycleBus(services);
        for (Map.Entry<String, LoadedModule> entry : List.copyOf(loadedModules.entrySet())) {
            EchoRuntimeModuleDescriptor descriptor = entry.getValue().descriptor();
            try {
                entry.getValue().lifecycle().reloadData(new EchoRuntimeModuleContext(descriptor, services));
                transition(registry, lifecycleBus, entry.getKey(), EchoRuntimeModuleLifecycle.DATA_RELOADED, "module_loader.reload");
                registry.note(entry.getKey(), "ABI v1 data reload hook executed");
            } catch (Throwable throwable) {
                failModule(registry, services, descriptor, "module data reload failed", throwable);
                revokeModuleState(registry, services, descriptor, "module data reload failed");
                closeClassLoader(registry, services, descriptor, entry.getValue().classLoader());
                loadedModules.remove(entry.getKey());
                activationOrder.remove(entry.getKey());
            }
        }
    }

    public void unload(EchoRuntimeModuleRegistry registry, EchoRuntimeServiceRegistry services) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(services, "services");
        EchoRuntimeModuleLifecycleBus lifecycleBus = lifecycleBus(services);
        ArrayList<String> unloadOrder = new ArrayList<>(activationOrder);
        Collections.reverse(unloadOrder);
        for (String moduleId : unloadOrder) {
            LoadedModule loadedModule = loadedModules.get(moduleId);
            if (loadedModule == null) {
                continue;
            }
            EchoRuntimeModuleDescriptor descriptor = loadedModule.descriptor();
            try {
                loadedModule.lifecycle().unload(new EchoRuntimeModuleContext(descriptor, services));
                transition(registry, lifecycleBus, moduleId, EchoRuntimeModuleLifecycle.UNLOADED, "module_loader.unload");
                registry.note(moduleId, "ABI v1 unload hook executed");
            } catch (Throwable throwable) {
                failModule(registry, services, descriptor, "module unload failed", throwable);
            } finally {
                revokeModuleState(registry, services, descriptor, "module unloaded");
                closeClassLoader(registry, services, descriptor, loadedModule.classLoader());
                loadedModules.remove(moduleId);
                activationOrder.remove(moduleId);
            }
        }
    }

    private void executeLoad(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleDescriptor descriptor
    ) {
        LocalModuleClassLoader classLoader = null;
        try {
            classLoader = new LocalModuleClassLoader(
                    classPathEntries(descriptor),
                    EchoRuntimeModuleEntrypoint.class.getClassLoader()
            );
            Class<?> entrypointClass = Class.forName(descriptor.executableEntrypoint(), true, classLoader);
            Object instance = entrypointClass.getDeclaredConstructor().newInstance();
            ModuleLifecycle lifecycle = moduleLifecycle(descriptor, instance);
            lifecycle.load(new EchoRuntimeModuleContext(descriptor, services));
            loadedModules.put(descriptor.id(), new LoadedModule(descriptor, classLoader, lifecycle));
            activationOrder.remove(descriptor.id());
            activationOrder.add(descriptor.id());
            registry.note(descriptor.id(), "ABI v1 entrypoint executed in isolated module classloader"
                    + (descriptor.adapterCoreEntrypoint().isBlank() ? "" : " through AdapterCore contract"));
        } catch (Throwable throwable) {
            revokeModuleState(registry, services, descriptor, "module load failed");
            if (classLoader != null) {
                closeClassLoader(registry, services, descriptor, classLoader);
            }
            failModule(registry, services, descriptor, "module load failed", throwable);
        }
    }

    private List<Path> classPathEntries(EchoRuntimeModuleDescriptor descriptor) {
        Path moduleRoot = descriptor.descriptorPath().getParent().getParent().toAbsolutePath().normalize();
        List<String> entries = descriptor.classPath();
        if (entries.isEmpty()) {
            entries = new ArrayList<>();
            entries.add(".");
            Path gradleClasses = moduleRoot.resolve("../../classes/java/main").normalize();
            if (Files.isDirectory(gradleClasses)) {
                entries.add("../../classes/java/main");
            }
            Path gradleResources = moduleRoot.resolve("../../resources/main").normalize();
            if (Files.isDirectory(gradleResources) && !gradleResources.equals(moduleRoot)) {
                entries.add("../../resources/main");
            }
        }
        ArrayList<Path> classPathEntries = new ArrayList<>();
        for (String entry : entries) {
            Path classPathEntry = moduleRoot.resolve(entry).toAbsolutePath().normalize();
            if (!classPathEntry.startsWith(moduleRoot.getParent()) && !classPathEntry.startsWith(moduleRoot)) {
                throw new SecurityException("Module classPath entry escapes module root: " + entry);
            }
            classPathEntries.add(classPathEntry);
        }
        return List.copyOf(classPathEntries);
    }

    private void revokeModuleState(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleDescriptor descriptor,
            String reason
    ) {
        services.find(EchoRuntimeModuleServiceExportRegistry.class)
                .map(exportRegistry -> exportRegistry.revokeModule(descriptor.id()).size())
                .filter(count -> count > 0)
                .ifPresent(count -> registry.note(
                        descriptor.id(),
                        reason + "; revoked " + count + " service export" + (count == 1 ? "" : "s")
                ));
        services.find(EchoRuntimeModuleContentActivationRegistry.class)
                .map(activationRegistry -> activationRegistry.deactivateModule(descriptor.id()).size())
                .filter(count -> count > 0)
                .ifPresent(count -> registry.note(
                        descriptor.id(),
                        reason + "; deactivated " + count + " content activation" + (count == 1 ? "" : "s")
                ));
    }

    private void failModule(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleDescriptor descriptor,
            String summary,
            Throwable throwable
    ) {
        transition(registry, lifecycleBus(services), descriptor.id(), EchoRuntimeModuleLifecycle.FAILED, "module_loader.failure");
        registry.note(descriptor.id(), summary + ": " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage());
        services.find(EchoRuntimeDiagnosticSink.class).ifPresent(sink -> sink.emit(new EchoRuntimeDiagnostic(
                "echo.runtime.module.execution_failed",
                EchoRuntimeDiagnosticSeverity.ERROR,
                "module_runtime",
                summary,
                throwable.getMessage() == null ? "" : throwable.getMessage(),
                Map.of("moduleId", descriptor.id(), "entrypoint", descriptor.executableEntrypoint())
        )));
    }

    private static ModuleLifecycle moduleLifecycle(EchoRuntimeModuleDescriptor descriptor, Object instance) {
        if (!descriptor.adapterCoreEntrypoint().isBlank()) {
            if (!(instance instanceof EchoRuntimeAdapterCoreEntrypoint adapterCoreEntrypoint)) {
                throw new IllegalStateException("AdapterCore entrypoint does not implement "
                        + EchoRuntimeAdapterCoreEntrypoint.class.getName() + ": "
                        + descriptor.adapterCoreEntrypoint());
            }
            return new AdapterCoreModuleLifecycle(adapterCoreEntrypoint);
        }
        if (!(instance instanceof EchoRuntimeModuleEntrypoint entrypoint)) {
            throw new IllegalStateException("Entrypoint does not implement "
                    + EchoRuntimeModuleEntrypoint.class.getName() + ": " + descriptor.entrypoint());
        }
        return new RuntimeModuleLifecycle(entrypoint);
    }

    private void closeClassLoader(
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleDescriptor descriptor,
            LocalModuleClassLoader classLoader
    ) {
        try {
            classLoader.close();
        } catch (IOException exception) {
            failModule(registry, services, descriptor, "module classloader close failed", exception);
        }
    }

    private static EchoRuntimeModuleLifecycleBus lifecycleBus(EchoRuntimeServiceRegistry services) {
        return services.find(EchoRuntimeModuleLifecycleBus.class)
                .orElseGet(EchoRuntimeModuleLifecycleBus::new);
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

    private record LoadedModule(
            EchoRuntimeModuleDescriptor descriptor,
            LocalModuleClassLoader classLoader,
            ModuleLifecycle lifecycle
    ) {
    }

    private static final class LocalModuleClassLoader extends ClassLoader implements AutoCloseable {
        private final List<Path> classPathEntries;

        private LocalModuleClassLoader(List<Path> classPathEntries, ClassLoader parent) {
            super(parent);
            this.classPathEntries = List.copyOf(classPathEntries);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            String classFile = name.replace('.', '/') + ".class";
            for (Path classPathEntry : classPathEntries) {
                byte[] bytes = readClassBytes(classPathEntry, classFile);
                if (bytes.length > 0) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
            throw new ClassNotFoundException(name);
        }

        private static byte[] readClassBytes(Path classPathEntry, String classFile) throws ClassNotFoundException {
            try {
                if (Files.isDirectory(classPathEntry)) {
                    Path classPath = classPathEntry.resolve(classFile).normalize();
                    if (Files.isRegularFile(classPath)) {
                        return Files.readAllBytes(classPath);
                    }
                    return new byte[0];
                }
                if (Files.isRegularFile(classPathEntry)) {
                    try (JarFile jar = new JarFile(classPathEntry.toFile())) {
                        JarEntry entry = jar.getJarEntry(classFile);
                        if (entry == null) {
                            return new byte[0];
                        }
                        try (InputStream input = jar.getInputStream(entry)) {
                            return input.readAllBytes();
                        }
                    }
                }
                return new byte[0];
            } catch (IOException exception) {
                throw new ClassNotFoundException(classFile, exception);
            }
        }

        @Override
        public void close() throws IOException {
        }
    }

    private interface ModuleLifecycle {
        void load(EchoRuntimeModuleContext context) throws Exception;

        void reloadData(EchoRuntimeModuleContext context) throws Exception;

        void unload(EchoRuntimeModuleContext context) throws Exception;
    }

    private record RuntimeModuleLifecycle(EchoRuntimeModuleEntrypoint entrypoint) implements ModuleLifecycle {
        @Override
        public void load(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.onLoad(context);
        }

        @Override
        public void reloadData(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.onDataReload(context);
        }

        @Override
        public void unload(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.onUnload(context);
        }
    }

    private record AdapterCoreModuleLifecycle(EchoRuntimeAdapterCoreEntrypoint entrypoint) implements ModuleLifecycle {
        @Override
        public void load(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.activate(context);
        }

        @Override
        public void reloadData(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.reloadData(context);
        }

        @Override
        public void unload(EchoRuntimeModuleContext context) throws Exception {
            entrypoint.deactivate(context);
        }
    }
}
