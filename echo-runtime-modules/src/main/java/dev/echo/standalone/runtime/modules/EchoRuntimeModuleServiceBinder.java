package dev.echo.standalone.runtime.modules;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.Objects;

public final class EchoRuntimeModuleServiceBinder {
    private final EchoRuntimeModuleSandboxPolicy sandboxPolicy;

    public EchoRuntimeModuleServiceBinder(EchoRuntimeModuleSandboxPolicy sandboxPolicy) {
        this.sandboxPolicy = Objects.requireNonNull(sandboxPolicy, "sandboxPolicy");
    }

    public void bind(
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleRegistry registry,
            EchoRuntimeModuleGraph moduleGraph,
            EchoRuntimeFeatureGraph featureGraph
    ) {
        Objects.requireNonNull(services, "services");
        services.register(EchoRuntimeModuleRegistry.class, registry);
        services.register(EchoRuntimeModuleGraph.class, moduleGraph);
        services.register(EchoRuntimeFeatureGraph.class, featureGraph);
        services.register(EchoRuntimeModuleSandboxPolicy.class, sandboxPolicy);
    }
}
