package dev.echo.standalone.runtime.modules;

import java.util.Objects;

public record EchoRuntimeModuleRuntimeResult(
        EchoRuntimeModuleRegistry registry,
        EchoRuntimeModuleGraph moduleGraph,
        EchoRuntimeFeatureGraph featureGraph
) {
    public EchoRuntimeModuleRuntimeResult {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(moduleGraph, "moduleGraph");
        Objects.requireNonNull(featureGraph, "featureGraph");
    }
}
