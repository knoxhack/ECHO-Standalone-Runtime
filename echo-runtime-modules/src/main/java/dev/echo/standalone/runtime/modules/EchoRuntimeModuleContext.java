package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

public final class EchoRuntimeModuleContext {
    private final EchoRuntimeModuleDescriptor descriptor;
    private final EchoRuntimeServiceRegistry services;

    public EchoRuntimeModuleContext(
            EchoRuntimeModuleDescriptor descriptor,
            EchoRuntimeServiceRegistry services
    ) {
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");
        this.services = Objects.requireNonNull(services, "services");
    }

    public EchoRuntimeModuleDescriptor descriptor() {
        return descriptor;
    }

    EchoRuntimeServiceRegistry services() {
        return services;
    }

    public <T> T requireService(Class<T> type) {
        rejectRestrictedServiceType(type, descriptor.id());
        return services.require(type);
    }

    public boolean hasPermission(String permission) {
        return descriptor.permissions().contains(permission);
    }

    public void requirePermission(String permission) {
        if (!hasPermission(permission)) {
            throw new EchoRuntimeModulePermissionException(descriptor.id(), permission);
        }
    }

    public EchoRuntimeModuleContentActivation registerContent(String kind, String contentId) {
        requirePermission(EchoRuntimeModulePermissionCatalog.CONTENT_REGISTER);
        return services.require(EchoRuntimeModuleContentActivationRegistry.class)
                .register(descriptor.id(), kind, contentId);
    }

    public EchoRuntimeModuleServiceExport exportService(String serviceId, Object service) {
        requirePermission(EchoRuntimeModulePermissionCatalog.SERVICES_EXPORT);
        return services.require(EchoRuntimeModuleServiceExportRegistry.class)
                .export(descriptor.id(), serviceId, service);
    }

    public <T> Optional<T> importService(String serviceId, Class<T> type) {
        requirePermission(EchoRuntimeModulePermissionCatalog.SERVICES_IMPORT);
        return services.require(EchoRuntimeModuleServiceExportRegistry.class)
                .findService(serviceId, type);
    }

    public void publishConfig(String key, String value) {
        requirePermission(EchoRuntimeModulePermissionCatalog.CLIENT_CONFIG);
        services.require(EchoRuntimeModuleDataRegistry.class)
                .publishConfig(descriptor.id(), requireText(key, "key"), requireText(value, "value"));
    }

    public EchoRuntimeModuleDataRegistry.EchoRuntimeModuleAsset registerAsset(String assetId, String relativePath) {
        requirePermission(EchoRuntimeModulePermissionCatalog.ASSETS_READ);
        String normalizedAssetPath = confinedModulePath(relativePath);
        return services.require(EchoRuntimeModuleDataRegistry.class)
                .registerAsset(
                        descriptor.id(),
                        requireText(assetId, "assetId"),
                        normalizeSeparators(relativePath),
                        normalizedAssetPath
                );
    }

    public void writeSaveData(String key, String value) {
        requirePermission(EchoRuntimeModulePermissionCatalog.DATA_PERSISTENCE);
        services.require(EchoRuntimeModuleDataRegistry.class)
                .writeSaveData(descriptor.id(), requireText(key, "key"), requireText(value, "value"));
    }

    public Optional<String> readSaveData(String key) {
        requirePermission(EchoRuntimeModulePermissionCatalog.DATA_PERSISTENCE);
        return services.require(EchoRuntimeModuleDataRegistry.class)
                .saveValue(descriptor.id(), requireText(key, "key"));
    }

    private static void rejectRestrictedServiceType(Class<?> type, String moduleId) {
        Objects.requireNonNull(type, "type");
        if (type == EchoRuntimeServiceRegistry.class
                || type == EchoRuntimeModuleContentActivationRegistry.class
                || type == EchoRuntimeModuleServiceExportRegistry.class
                || type == EchoRuntimeModuleDataRegistry.class) {
            throw new EchoRuntimeModulePermissionException(moduleId, "restricted service " + type.getName());
        }
    }

    private String confinedModulePath(String relativePath) {
        requireText(relativePath, "relativePath");
        Path requested = Path.of(relativePath).normalize();
        if (requested.isAbsolute() || requested.startsWith("..") || "..".equals(requested.toString())) {
            throw new SecurityException("Module asset path escapes module root: " + relativePath);
        }
        Path moduleRoot = descriptor.moduleRoot().toAbsolutePath().normalize();
        if (Files.isRegularFile(moduleRoot)) {
            return normalizeSeparators(requested.toString());
        }
        Path resolved = moduleRoot.resolve(requested).toAbsolutePath().normalize();
        if (!resolved.startsWith(moduleRoot)) {
            throw new SecurityException("Module asset path escapes module root: " + relativePath);
        }
        return resolved.toString();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }

    private static String normalizeSeparators(String value) {
        return value.replace('\\', '/');
    }
}
