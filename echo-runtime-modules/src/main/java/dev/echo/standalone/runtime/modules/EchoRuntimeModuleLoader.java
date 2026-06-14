package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSeverity;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.nativeplatform.contracts.EchoNativeActivationSurfaceRegistrar;
import dev.echo.nativeplatform.contracts.EchoNativeModuleDescriptor;
import dev.echo.nativeplatform.contracts.EchoNativeModuleEntrypoint;
import dev.echo.nativeplatform.contracts.EchoNativeModuleLoadContext;
import dev.echo.nativeplatform.contracts.EchoNativeRuntimeSide;
import dev.echo.nativeplatform.contracts.EchoNativeServiceRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        List<ModuleClassPathEntry> classPathEntries = List.of();
        try {
            classPathEntries = classPathEntries(descriptor);
            classLoader = new LocalModuleClassLoader(
                    classPathEntries,
                    EchoRuntimeModuleEntrypoint.class.getClassLoader(),
                    dependencyClassLoaders(descriptor)
            );
            Class<?> entrypointClass = Class.forName(descriptor.executableEntrypoint(), true, classLoader);
            Object instance = entrypointClass.getDeclaredConstructor().newInstance();
            ModuleLifecycle lifecycle = moduleLifecycle(descriptor, instance, classPathEntries);
            lifecycle.load(new EchoRuntimeModuleContext(descriptor, services));
            loadedModules.put(descriptor.id(), new LoadedModule(descriptor, classLoader, lifecycle));
            activationOrder.remove(descriptor.id());
            activationOrder.add(descriptor.id());
            registry.note(descriptor.id(), lifecycle.loadSummary(descriptor));
        } catch (Throwable throwable) {
            revokeModuleState(registry, services, descriptor, "module load failed");
            if (classLoader != null) {
                closeClassLoader(registry, services, descriptor, classLoader);
            }
            failModule(registry, services, descriptor, "module load failed", throwable);
        }
    }

    private List<ModuleClassPathEntry> classPathEntries(EchoRuntimeModuleDescriptor descriptor) {
        Path moduleRoot = descriptor.moduleRoot().toAbsolutePath().normalize();
        Path moduleRealRoot = realPathIfExists(moduleRoot);
        List<String> entries = descriptor.classPath();
        if (Files.isRegularFile(moduleRoot) && isArchivePath(moduleRoot)) {
            if (entries.isEmpty()) {
                return List.of(ModuleClassPathEntry.archiveRoot(moduleRoot));
            }
            ArrayList<ModuleClassPathEntry> archiveClassPathEntries = new ArrayList<>();
            for (String entry : entries) {
                if (".".equals(entry) || moduleRoot.getFileName().toString().equals(entry)) {
                    archiveClassPathEntries.add(ModuleClassPathEntry.archiveRoot(moduleRoot));
                    continue;
                }
                archiveClassPathEntries.add(ModuleClassPathEntry.archivePrefix(moduleRoot, safeArchivePrefix(entry)));
            }
            return List.copyOf(archiveClassPathEntries);
        }
        if (entries.isEmpty()) {
            entries = new ArrayList<>();
            entries.add(".");
            Path gradleClasses = moduleRoot.resolve("build/classes/java/main").normalize();
            if (Files.isDirectory(gradleClasses)) {
                entries.add("build/classes/java/main");
            }
            Path gradleResources = moduleRoot.resolve("build/resources/main").normalize();
            if (Files.isDirectory(gradleResources) && !gradleResources.equals(moduleRoot)) {
                entries.add("build/resources/main");
            }
        }
        ArrayList<ModuleClassPathEntry> classPathEntries = new ArrayList<>();
        for (String entry : entries) {
            Path classPathEntry = moduleRoot.resolve(entry).toAbsolutePath().normalize();
            Path securityPath = realPathIfExists(classPathEntry);
            if (!isWithin(securityPath, moduleRealRoot)) {
                throw new SecurityException("Module classPath entry escapes module root: " + entry);
            }
            classPathEntries.add(ModuleClassPathEntry.filesystem(classPathEntry));
        }
        return List.copyOf(classPathEntries);
    }

    private static String safeArchivePrefix(String entry) {
        String normalized = entry == null ? "" : entry.trim().replace('\\', '/');
        while (normalized.startsWith("./")) {
            normalized = normalized.substring(2);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.isBlank()
                || normalized.startsWith("/")
                || normalized.contains(":")
                || normalized.equals(".")
                || normalized.equals("..")
                || normalized.startsWith("../")
                || normalized.endsWith("/..")
                || normalized.contains("/../")) {
            throw new SecurityException("Archive module classPath entry escapes archive root: " + entry);
        }
        return normalized;
    }

    private static boolean isWithin(Path path, Path root) {
        return path.equals(root) || path.startsWith(root);
    }

    private static boolean isArchivePath(Path path) {
        String name = path.getFileName() == null ? "" : path.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".jar") || name.endsWith(".zip") || name.endsWith(".echo-addon");
    }

    private static Path realPathIfExists(Path path) {
        try {
            return Files.exists(path) ? path.toRealPath().normalize() : path.toAbsolutePath().normalize();
        } catch (IOException exception) {
            throw new SecurityException("Unable to resolve module classPath entry: " + path, exception);
        }
    }

    private List<LocalModuleClassLoader> dependencyClassLoaders(EchoRuntimeModuleDescriptor descriptor) {
        ArrayList<LocalModuleClassLoader> dependencies = new ArrayList<>();
        for (String dependencyId : descriptor.requires()) {
            LoadedModule loadedModule = loadedModules.get(dependencyId);
            if (loadedModule != null) {
                dependencies.add(loadedModule.classLoader());
            }
        }
        for (String dependencyId : descriptor.optional()) {
            LoadedModule loadedModule = loadedModules.get(dependencyId);
            if (loadedModule != null && !dependencies.contains(loadedModule.classLoader())) {
                dependencies.add(loadedModule.classLoader());
            }
        }
        return List.copyOf(dependencies);
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
        services.find(EchoRuntimeModuleDataRegistry.class)
                .map(dataRegistry -> dataRegistry.revokeRuntimeState(descriptor.id()))
                .filter(count -> count > 0)
                .ifPresent(count -> registry.note(
                        descriptor.id(),
                        reason + "; revoked " + count + " module data runtime entr" + (count == 1 ? "y" : "ies")
                ));
        services.find(EchoNativeServiceRegistry.class)
                .map(nativeRegistry -> nativeRegistry.revokeModule(descriptor.id()).size())
                .filter(count -> count > 0)
                .ifPresent(count -> registry.note(
                        descriptor.id(),
                        reason + "; revoked " + count + " native service" + (count == 1 ? "" : "s")
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

    private static ModuleLifecycle moduleLifecycle(
            EchoRuntimeModuleDescriptor descriptor,
            Object instance,
            List<ModuleClassPathEntry> classPathEntries
    ) {
        if (!descriptor.adapterCoreEntrypoint().isBlank()) {
            if (!(instance instanceof EchoRuntimeAdapterCoreEntrypoint adapterCoreEntrypoint)) {
                throw new IllegalStateException("AdapterCore entrypoint does not implement "
                        + EchoRuntimeAdapterCoreEntrypoint.class.getName() + ": "
                        + descriptor.adapterCoreEntrypoint());
            }
            return new AdapterCoreModuleLifecycle(adapterCoreEntrypoint);
        }
        if (!descriptor.nativeEntrypoint().isBlank()) {
            if (instance instanceof EchoNativeModuleEntrypoint nativeEntrypoint) {
                return new NativePlatformModuleLifecycle(nativeEntrypoint, classPathEntries);
            }
            return ReflectiveNativePlatformModuleLifecycle.from(instance, classPathEntries);
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

    private static List<Path> classPathPaths(List<ModuleClassPathEntry> classPathEntries) {
        return classPathEntries.stream()
                .map(ModuleClassPathEntry::path)
                .distinct()
                .toList();
    }

    private record ModuleClassPathEntry(Path path, String archivePrefix) {
        private ModuleClassPathEntry {
            Objects.requireNonNull(path, "path");
            archivePrefix = archivePrefix == null ? "" : archivePrefix;
        }

        private static ModuleClassPathEntry filesystem(Path path) {
            return new ModuleClassPathEntry(path, "");
        }

        private static ModuleClassPathEntry archiveRoot(Path archivePath) {
            return new ModuleClassPathEntry(archivePath, "");
        }

        private static ModuleClassPathEntry archivePrefix(Path archivePath, String archivePrefix) {
            return new ModuleClassPathEntry(archivePath, archivePrefix);
        }

        private String archiveEntryName(String classFile) {
            return archivePrefix.isBlank() ? classFile : archivePrefix + "/" + classFile;
        }
    }

    private record LoadedModule(
            EchoRuntimeModuleDescriptor descriptor,
            LocalModuleClassLoader classLoader,
            ModuleLifecycle lifecycle
    ) {
    }

    private static final class LocalModuleClassLoader extends ClassLoader implements AutoCloseable {
        private final List<ModuleClassPathEntry> classPathEntries;
        private final List<LocalModuleClassLoader> dependencyClassLoaders;

        private LocalModuleClassLoader(
                List<ModuleClassPathEntry> classPathEntries,
                ClassLoader parent,
                List<LocalModuleClassLoader> dependencyClassLoaders
        ) {
            super(parent);
            this.classPathEntries = List.copyOf(classPathEntries);
            this.dependencyClassLoaders = List.copyOf(dependencyClassLoaders);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            String classFile = name.replace('.', '/') + ".class";
            for (ModuleClassPathEntry classPathEntry : classPathEntries) {
                byte[] bytes = readClassBytes(classPathEntry, classFile);
                if (bytes.length > 0) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
            }
            for (LocalModuleClassLoader dependencyClassLoader : dependencyClassLoaders) {
                try {
                    return dependencyClassLoader.loadClass(name);
                } catch (ClassNotFoundException ignored) {
                    // Try the next declared dependency.
                }
            }
            throw new ClassNotFoundException(name);
        }

        private static byte[] readClassBytes(ModuleClassPathEntry classPathEntry, String classFile)
                throws ClassNotFoundException {
            try {
                if (Files.isDirectory(classPathEntry.path())) {
                    Path classPath = classPathEntry.path().resolve(classFile).normalize();
                    if (Files.isRegularFile(classPath)) {
                        return Files.readAllBytes(classPath);
                    }
                    return new byte[0];
                }
                if (Files.isRegularFile(classPathEntry.path())) {
                    try (JarFile jar = new JarFile(classPathEntry.path().toFile())) {
                        JarEntry entry = jar.getJarEntry(classPathEntry.archiveEntryName(classFile));
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

        default String loadSummary(EchoRuntimeModuleDescriptor descriptor) {
            return "ABI v1 entrypoint executed in isolated module classloader"
                    + (descriptor.adapterCoreEntrypoint().isBlank() ? "" : " through AdapterCore contract");
        }
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

    private static final class ReflectiveNativePlatformModuleLifecycle implements ModuleLifecycle {
        private final Object entrypoint;
        private final Method bootstrap;
        private final Method describeNativeSurfaces;
        private final Method moduleId;
        private final List<ModuleClassPathEntry> classPathEntries;
        private EchoNativeModuleLoadContext nativeContext;
        private EchoNativeServiceRegistry nativeRegistry;

        private ReflectiveNativePlatformModuleLifecycle(
                Object entrypoint,
                Method bootstrap,
                Method describeNativeSurfaces,
                Method moduleId,
                List<ModuleClassPathEntry> classPathEntries
        ) {
            this.entrypoint = entrypoint;
            this.bootstrap = bootstrap;
            this.describeNativeSurfaces = describeNativeSurfaces;
            this.moduleId = moduleId;
            this.classPathEntries = List.copyOf(classPathEntries);
        }

        private static ReflectiveNativePlatformModuleLifecycle from(
                Object entrypoint,
                List<ModuleClassPathEntry> classPathEntries
        ) {
            Method bootstrap = method(entrypoint.getClass(), "bootstrap");
            Method describeNativeSurfaces = method(entrypoint.getClass(), "describeNativeSurfaces", Map.class);
            Method moduleId = method(entrypoint.getClass(), "moduleId");
            if (bootstrap == null && describeNativeSurfaces == null) {
                throw new IllegalStateException("Native Platform entrypoint does not implement "
                        + EchoNativeModuleEntrypoint.class.getName()
                        + " and exposes no supported legacy bootstrap or describeNativeSurfaces method: "
                        + entrypoint.getClass().getName());
            }
            return new ReflectiveNativePlatformModuleLifecycle(
                    entrypoint,
                    bootstrap,
                    describeNativeSurfaces,
                    moduleId,
                    classPathEntries
            );
        }

        @Override
        public void load(EchoRuntimeModuleContext context) throws Exception {
            ensureNativeContext(context);
            nativeContext.attribute("nativeEntrypointBridge", "reflective_legacy_native_entrypoint");
            nativeContext.attribute("nativeEntrypointClass", entrypoint.getClass().getName());
            nativeContext.attribute("legacyNativeModuleId", reflectiveModuleId(context.descriptor().id()));
            if (bootstrap != null) {
                bootstrap.invoke(entrypoint);
                nativeContext.attribute("legacyNativeBootstrapExecuted", true);
                nativeContext.registerService(
                        "service." + context.descriptor().id() + ".legacy_native_bootstrap",
                        entrypoint,
                        "legacy",
                        "bootstrap"
                );
                nativeContext.recordMutation(
                        "lifecycle",
                        "legacy_native_bootstrap_invoked",
                        entrypoint.getClass().getName(),
                        dev.echo.nativeplatform.contracts.EchoNativeLoadStatus.MUTATED
                );
            }
            if (describeNativeSurfaces != null) {
                Map<String, Object> activation = activation();
                nativeContext.attribute("legacyNativeSurfaceActivation", activation);
                nativeContext.registerService(
                        "service." + context.descriptor().id() + ".legacy_native_surface",
                        activation,
                        "legacy",
                        "native_surface"
                );
                nativeContext.recordMutation(
                        "registry",
                        "legacy_native_surface_described",
                        context.descriptor().id(),
                        dev.echo.nativeplatform.contracts.EchoNativeLoadStatus.RESOLVED
                );
            }
        }

        @Override
        public void reloadData(EchoRuntimeModuleContext context) throws Exception {
            ensureNativeContext(context);
            if (describeNativeSurfaces != null) {
                nativeContext.attribute("legacyNativeSurfaceReloaded", activation());
            }
        }

        @Override
        public void unload(EchoRuntimeModuleContext context) {
            ensureNativeContext(context);
            nativeContext.attribute("legacyNativeUnloaded", true);
        }

        @Override
        public String loadSummary(EchoRuntimeModuleDescriptor descriptor) {
            List<String> attributes = nativeContext == null
                    ? List.of()
                    : nativeContext.attributes().keySet().stream().sorted().toList();
            int serviceCount = nativeRegistry == null ? 0 : nativeRegistry.servicesForModule(descriptor.id()).size();
            int mutationCount = nativeContext == null ? 0 : nativeContext.mutations().size();
            return "Native Platform ABI legacy entrypoint executed reflectively in isolated module classloader"
                    + "; nativeServices=" + serviceCount
                    + "; nativeMutations=" + mutationCount
                    + "; nativeAttributes=" + String.join(",", attributes);
        }

        private Map<String, Object> activation() throws ReflectiveOperationException {
            Object result = describeNativeSurfaces.invoke(entrypoint, bridgeContext());
            if (!(result instanceof Map<?, ?> map)) {
                return Map.of();
            }
            Map<String, Object> activation = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (entry.getKey() != null) {
                    activation.put(String.valueOf(entry.getKey()), entry.getValue());
                }
            }
            return Map.copyOf(activation);
        }

        private Map<String, String> bridgeContext() {
            EchoNativeModuleDescriptor descriptor = nativeContext.descriptor();
            Map<String, String> data = new LinkedHashMap<>();
            data.put("moduleId", descriptor.id());
            data.put("moduleName", descriptor.name());
            data.put("moduleVersion", descriptor.version());
            data.put("runtime", "echo_runtime_standalone");
            data.put("loader", "echo-standalone-runtime");
            data.put("packId", String.valueOf(nativeContext.attributes().getOrDefault("packId", "ashfall")));
            data.put("descriptorPath", descriptor.descriptorPath() == null ? "" : descriptor.descriptorPath().toString());
            for (Map.Entry<String, Object> entry : nativeContext.attributes().entrySet()) {
                data.putIfAbsent(entry.getKey(), entry.getValue() == null ? "" : String.valueOf(entry.getValue()));
            }
            return Map.copyOf(data);
        }

        private String reflectiveModuleId(String fallback) throws ReflectiveOperationException {
            if (moduleId == null) {
                return fallback;
            }
            Object value = moduleId.invoke(entrypoint);
            return value == null || String.valueOf(value).isBlank() ? fallback : String.valueOf(value);
        }

        private void ensureNativeContext(EchoRuntimeModuleContext context) {
            if (nativeContext != null && nativeRegistry != null) {
                return;
            }
            nativeRegistry = NativePlatformModuleLifecycle.nativeRegistry(context);
            nativeContext = new EchoNativeModuleLoadContext(
                    NativePlatformModuleLifecycle.nativeDescriptor(context.descriptor(), classPathEntries),
                    nativeRegistry,
                    NativePlatformModuleLifecycle.nativeContextAttributes(
                            context.descriptor(),
                            classPathEntries,
                            "native-platform-abi-v1-legacy-reflective"
                    )
            );
        }

        private static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException exception) {
                try {
                    Method method = type.getDeclaredMethod(name, parameterTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    return null;
                }
            }
        }
    }

    private static final class NativePlatformModuleLifecycle implements ModuleLifecycle {
        private static final List<String> LOAD_PHASES = List.of(
                "discover",
                "resolve",
                "loadClasses",
                "construct",
                "registerServices",
                "registerContent",
                "commonSetup",
                "clientSetup",
                "serverSetup",
                "ready"
        );

        private final EchoNativeModuleEntrypoint entrypoint;
        private final Method bootstrap;
        private final Method describeNativeSurfaces;
        private final List<ModuleClassPathEntry> classPathEntries;
        private EchoNativeModuleLoadContext nativeContext;
        private EchoNativeServiceRegistry nativeRegistry;

        private NativePlatformModuleLifecycle(
                EchoNativeModuleEntrypoint entrypoint,
                List<ModuleClassPathEntry> classPathEntries
        ) {
            this.entrypoint = entrypoint;
            this.bootstrap = method(entrypoint.getClass(), "bootstrap");
            this.describeNativeSurfaces = method(entrypoint.getClass(), "describeNativeSurfaces", Map.class);
            this.classPathEntries = List.copyOf(classPathEntries);
        }

        @Override
        public void load(EchoRuntimeModuleContext context) throws Exception {
            nativeRegistry = nativeRegistry(context);
            registerModuleAliases(context.descriptor());
            nativeContext = new EchoNativeModuleLoadContext(
                    nativeDescriptor(context.descriptor()),
                    nativeRegistry,
                    nativeContextAttributes(context.descriptor(), classPathEntries, "native-platform-abi-v1")
            );
            entrypoint.discover(nativeContext);
            entrypoint.resolve(nativeContext);
            entrypoint.loadClasses(nativeContext);
            entrypoint.construct(nativeContext);
            bridgeLegacyBootstrapIfNeeded(context.descriptor().id());
            entrypoint.registerServices(nativeContext);
            bridgeDescribeNativeSurfacesIfNeeded();
            entrypoint.registerContent(nativeContext);
            entrypoint.commonSetup(nativeContext);
            entrypoint.clientSetup(nativeContext);
            entrypoint.serverSetup(nativeContext);
            entrypoint.ready(nativeContext);
        }

        @Override
        public void reloadData(EchoRuntimeModuleContext context) throws Exception {
            ensureNativeContext(context);
            entrypoint.registerContent(nativeContext);
            bridgeDescribeNativeSurfacesIfNeeded();
            entrypoint.commonSetup(nativeContext);
            entrypoint.ready(nativeContext);
        }

        @Override
        public void unload(EchoRuntimeModuleContext context) throws Exception {
            ensureNativeContext(context);
            entrypoint.shutdown(nativeContext);
        }

        @Override
        public String loadSummary(EchoRuntimeModuleDescriptor descriptor) {
            List<String> attributes = nativeContext == null
                    ? List.of()
                    : nativeContext.attributes().keySet().stream().sorted().toList();
            int serviceCount = nativeRegistry == null ? 0 : nativeRegistry.servicesForModule(descriptor.id()).size();
            int mutationCount = nativeContext == null ? 0 : nativeContext.mutations().size();
            return "Native Platform ABI entrypoint executed in isolated module classloader; phases="
                    + String.join(",", LOAD_PHASES)
                    + "; nativeServices=" + serviceCount
                    + "; nativeMutations=" + mutationCount
                    + "; nativeAttributes=" + String.join(",", attributes);
        }

        private void ensureNativeContext(EchoRuntimeModuleContext context) {
            if (nativeContext != null && nativeRegistry != null) {
                return;
            }
            nativeRegistry = nativeRegistry(context);
            registerModuleAliases(context.descriptor());
            nativeContext = new EchoNativeModuleLoadContext(
                    nativeDescriptor(context.descriptor()),
                    nativeRegistry,
                    nativeContextAttributes(context.descriptor(), classPathEntries, "native-platform-abi-v1")
            );
        }

        private EchoNativeModuleDescriptor nativeDescriptor(EchoRuntimeModuleDescriptor descriptor) {
            return nativeDescriptor(descriptor, classPathEntries);
        }

        private void registerModuleAliases(EchoRuntimeModuleDescriptor descriptor) {
            for (String alias : descriptor.aliases()) {
                nativeRegistry.registerModuleAlias(alias, descriptor.id());
            }
        }

        private void bridgeLegacyBootstrapIfNeeded(String moduleId) throws ReflectiveOperationException {
            if (bootstrap == null || nativeContext.attributes().containsKey("legacyNativeBootstrapExecuted")) {
                return;
            }
            bootstrap.invoke(entrypoint);
            nativeContext.attribute("legacyNativeBootstrapExecuted", true);
            nativeContext.registerService(
                    "service." + moduleId + ".legacy_native_bootstrap",
                    entrypoint,
                    "legacy",
                    "bootstrap"
            );
            nativeContext.recordMutation(
                    "lifecycle",
                    "legacy_native_bootstrap_invoked",
                    entrypoint.getClass().getName(),
                    dev.echo.nativeplatform.contracts.EchoNativeLoadStatus.MUTATED
            );
        }

        private void bridgeDescribeNativeSurfacesIfNeeded() {
            if (describeNativeSurfaces == null || nativeContext.attributes().containsKey("nativeActivationSurface")) {
                return;
            }
            Map<String, Object> activation = EchoNativeActivationSurfaceRegistrar.activation(
                    nativeContext,
                    this::describeActivationSurface
            );
            EchoNativeActivationSurfaceRegistrar.registerServices(
                    nativeContext,
                    entrypoint,
                    activation,
                    "native_module_entrypoint",
                    "diagnostics"
            );
            EchoNativeActivationSurfaceRegistrar.registerContent(nativeContext, activation);
            EchoNativeActivationSurfaceRegistrar.ready(nativeContext);
        }

        private Map<String, Object> describeActivationSurface() {
            try {
                Object result = describeNativeSurfaces.invoke(
                        entrypoint,
                        EchoNativeActivationSurfaceRegistrar.bridgeContext(nativeContext)
                );
                if (!(result instanceof Map<?, ?> map)) {
                    return Map.of();
                }
                Map<String, Object> activation = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (entry.getKey() != null) {
                        activation.put(String.valueOf(entry.getKey()), entry.getValue());
                    }
                }
                return Map.copyOf(activation);
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(
                        "Native Platform entrypoint describeNativeSurfaces is not accessible: "
                                + entrypoint.getClass().getName(),
                        exception
                );
            } catch (InvocationTargetException exception) {
                Throwable cause = exception.getCause();
                if (cause instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                if (cause instanceof Error error) {
                    throw error;
                }
                throw new IllegalStateException(
                        "Native Platform entrypoint describeNativeSurfaces failed: "
                                + entrypoint.getClass().getName(),
                        cause
                );
            }
        }

        private static Map<String, Object> nativeContextAttributes(
                EchoRuntimeModuleDescriptor descriptor,
                List<ModuleClassPathEntry> classPathEntries,
                String adapter
        ) {
            Map<String, Object> attributes = new LinkedHashMap<>();
            attributes.put("runtime", "echo_runtime_standalone");
            attributes.put("loader", "echo-standalone-runtime");
            attributes.put("adapter", adapter);
            attributes.put("packId", "ashfall");
            Path moduleRoot = descriptor.moduleRoot().toAbsolutePath().normalize();
            attributes.put("moduleRoot", moduleRoot.toString());
            String repoRoot = inferRepoRoot(descriptor.descriptorPath(), moduleRoot, classPathEntries);
            if (!repoRoot.isBlank()) {
                attributes.put("repoRoot", repoRoot);
            }
            return Map.copyOf(attributes);
        }

        private static String inferRepoRoot(
                Path descriptorPath,
                Path moduleRoot,
                List<ModuleClassPathEntry> classPathEntries
        ) {
            ArrayList<Path> candidates = new ArrayList<>();
            if (descriptorPath != null) {
                candidates.add(descriptorPath);
            }
            candidates.add(moduleRoot);
            candidates.addAll(classPathPaths(classPathEntries));
            for (Path candidate : candidates) {
                Path current = candidate.toAbsolutePath().normalize();
                if (Files.isRegularFile(current)) {
                    current = current.getParent();
                }
                while (current != null) {
                    if (Files.exists(current.resolve("settings.gradle"))
                            && (Files.isDirectory(current.resolve("echo-native-platform"))
                            || Files.isDirectory(current.resolve("addons")))) {
                        return current.toString();
                    }
                    current = current.getParent();
                }
            }
            return "";
        }

        private static EchoNativeModuleDescriptor nativeDescriptor(
                EchoRuntimeModuleDescriptor descriptor,
                List<ModuleClassPathEntry> classPathEntries
        ) {
            return new EchoNativeModuleDescriptor(
                    descriptor.id(),
                    descriptor.name(),
                    descriptor.version(),
                    descriptor.kind(),
                    descriptor.role(),
                    descriptor.nativeEntrypoint(),
                    nativeSide(descriptor.side()),
                    descriptor.requires(),
                    descriptor.optional(),
                    descriptor.provides(),
                    descriptor.descriptorPath(),
                    classPathPaths(classPathEntries)
            );
        }

        private static EchoNativeRuntimeSide nativeSide(EchoRuntimeModuleSide side) {
            return switch (side) {
                case CLIENT -> EchoNativeRuntimeSide.CLIENT;
                case SERVER -> EchoNativeRuntimeSide.SERVER;
                case COMMON, BOTH -> EchoNativeRuntimeSide.COMMON;
                case DEV -> EchoNativeRuntimeSide.UNKNOWN;
            };
        }

        private static EchoNativeServiceRegistry nativeRegistry(EchoRuntimeModuleContext context) {
            return context.services().find(EchoNativeServiceRegistry.class)
                    .orElseGet(() -> {
                        EchoNativeServiceRegistry registry = new EchoNativeServiceRegistry();
                        context.services().register(EchoNativeServiceRegistry.class, registry);
                        return registry;
                    });
        }

        private static Method method(Class<?> type, String name, Class<?>... parameterTypes) {
            try {
                Method method = type.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException exception) {
                try {
                    Method method = type.getDeclaredMethod(name, parameterTypes);
                    method.setAccessible(true);
                    return method;
                } catch (NoSuchMethodException ignored) {
                    return null;
                }
            }
        }
    }
}
