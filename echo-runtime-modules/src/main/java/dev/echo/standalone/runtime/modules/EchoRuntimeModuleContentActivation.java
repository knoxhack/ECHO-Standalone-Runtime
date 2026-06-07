package dev.echo.standalone.runtime.modules;

public record EchoRuntimeModuleContentActivation(
        String moduleId,
        String kind,
        String contentId
) {
    public EchoRuntimeModuleContentActivation {
        moduleId = requireText(moduleId, "moduleId");
        kind = requireText(kind, "kind");
        contentId = requireText(contentId, "contentId");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
