package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleRuntimeResult;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRequiredSystemModuleStandaloneRuntime {
    public static final List<String> REQUIRED_SYSTEM_MODULES = List.of(
            "echocore",
            "signalos",
            "signalosexample",
            "echobridgecore",
            "echoagentcore",
            "echoreportcore",
            "echometadatacore",
            "echomodulegraph"
    );

    public EchoRequiredSystemModuleRuntimeResult activate(
            EchoRuntimeServiceRegistry services,
            EchoRuntimeModuleRuntimeResult modules,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(modules, "modules");
        Objects.requireNonNull(bridge, "bridge");

        EchoRuntimeModuleRegistry moduleRegistry = modules.registry();
        ArrayList<EchoRequiredSystemModuleActivation> activations = new ArrayList<>();
        for (String moduleId : REQUIRED_SYSTEM_MODULES) {
            boolean descriptorLoaded = moduleRegistry.find(moduleId).isPresent();
            EchoRuntimeModuleStatus status = moduleRegistry.runtimeStatus(moduleId);
            List<EchoAdapterCoreRegistryEntry> entries = bridge.registry().entries().stream()
                    .filter(entry -> entry.binding().moduleId().equals(moduleId))
                    .toList();
            ArrayList<String> contractIds = new ArrayList<>();
            ArrayList<String> runtimeDomains = new ArrayList<>();
            boolean allRuntimeAliasesResolved = !entries.isEmpty();
            for (EchoAdapterCoreRegistryEntry entry : entries) {
                EchoAdapterCoreContentBinding binding = bridge.registry()
                        .requireContentId(entry.contentId())
                        .binding();
                contractIds.add(binding.contentId());
                runtimeDomains.add(entry.domain().id());
                for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
                    boolean aliasResolved = bridge.registry()
                            .findRuntimeId(runtimeKind, binding.idFor(runtimeKind))
                            .isPresent();
                    allRuntimeAliasesResolved = allRuntimeAliasesResolved && aliasResolved;
                }
            }
            activations.add(new EchoRequiredSystemModuleActivation(
                    moduleId,
                    status,
                    descriptorLoaded,
                    !entries.isEmpty(),
                    !entries.isEmpty(),
                    allRuntimeAliasesResolved,
                    contractIds,
                    runtimeDomains
            ));
        }

        EchoRequiredSystemModuleRuntimeResult result =
                new EchoRequiredSystemModuleRuntimeResult(activations);
        services.register(EchoRequiredSystemModuleRuntimeResult.class, result);
        services.register(EchoRequiredSystemModuleStandaloneRuntime.class, this);
        return result;
    }
}
