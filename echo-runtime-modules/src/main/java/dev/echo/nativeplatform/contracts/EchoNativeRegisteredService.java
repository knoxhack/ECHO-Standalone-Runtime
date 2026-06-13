package dev.echo.nativeplatform.contracts;

import java.util.List;

public record EchoNativeRegisteredService(
        String moduleId,
        String serviceId,
        String implementationClass,
        List<String> surfaces
) {
    public EchoNativeRegisteredService {
        moduleId = requireText(moduleId, "moduleId");
        serviceId = requireText(serviceId, "serviceId");
        implementationClass = requireText(implementationClass, "implementationClass");
        surfaces = surfaces == null ? List.of() : List.copyOf(surfaces);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
