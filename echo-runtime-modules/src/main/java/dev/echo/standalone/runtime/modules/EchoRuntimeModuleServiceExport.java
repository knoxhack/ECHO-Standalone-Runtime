package dev.echo.standalone.runtime.modules;

public record EchoRuntimeModuleServiceExport(
        String moduleId,
        String serviceId,
        Object service
) {
    public EchoRuntimeModuleServiceExport {
        moduleId = requireText(moduleId, "moduleId");
        serviceId = requireText(serviceId, "serviceId");
        if (service == null) {
            throw new IllegalArgumentException("service must not be null");
        }
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
