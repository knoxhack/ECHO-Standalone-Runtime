package dev.echo.standalone.runtime.modules;

public record EchoRuntimeSystemModuleStatus(
        String moduleId,
        EchoRuntimeModuleStatus status,
        String reason
) {
    public EchoRuntimeSystemModuleStatus {
        moduleId = requireText(moduleId, "moduleId");
        if (status == null) {
            throw new IllegalArgumentException("status must not be null");
        }
        reason = requireText(reason, "reason");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
