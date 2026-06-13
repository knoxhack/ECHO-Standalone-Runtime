package dev.echo.nativeplatform.contracts;

import java.util.Map;

public record EchoNativeServiceMutation(
        String moduleId,
        String surface,
        String action,
        String target,
        EchoNativeRuntimeSide side,
        Map<String, Object> evidence
) {
    public EchoNativeServiceMutation {
        moduleId = requireText(moduleId, "moduleId");
        surface = requireText(surface, "surface");
        action = requireText(action, "action");
        target = target == null ? "" : target.trim();
        side = side == null ? EchoNativeRuntimeSide.UNKNOWN : side;
        evidence = Map.copyOf(evidence == null ? Map.of() : evidence);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
