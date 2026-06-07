package dev.echo.standalone.runtime.compat;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoRequiredSystemModuleRuntimeResult(
        List<EchoRequiredSystemModuleActivation> activations
) {
    public EchoRequiredSystemModuleRuntimeResult {
        Objects.requireNonNull(activations, "activations");
        activations = activations.stream()
                .sorted(Comparator.comparing(EchoRequiredSystemModuleActivation::moduleId))
                .toList();
    }

    public int activationCount() {
        return activations.size();
    }

    public int executableCount() {
        return (int) activations.stream()
                .filter(EchoRequiredSystemModuleActivation::active)
                .count();
    }

    public boolean allExecutable() {
        return !activations.isEmpty() && executableCount() == activations.size();
    }

    public EchoRequiredSystemModuleActivation require(String moduleId) {
        return activations.stream()
                .filter(activation -> activation.moduleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Missing required system module activation: " + moduleId));
    }
}
