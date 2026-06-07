package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

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

    private static void rejectRestrictedServiceType(Class<?> type, String moduleId) {
        Objects.requireNonNull(type, "type");
        if (type == EchoRuntimeServiceRegistry.class
                || type == EchoRuntimeModuleContentActivationRegistry.class
                || type == EchoRuntimeModuleServiceExportRegistry.class) {
            throw new EchoRuntimeModulePermissionException(moduleId, "restricted service " + type.getName());
        }
    }
}
