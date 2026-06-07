package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.modules.EchoRuntimeModuleStatus;

import java.util.List;
import java.util.Objects;

public record EchoRequiredSystemModuleActivation(
        String moduleId,
        EchoRuntimeModuleStatus runtimeStatus,
        boolean descriptorLoaded,
        boolean adapterCoreUsed,
        boolean standaloneRuntimeCodeExecuted,
        boolean allRuntimeAliasesResolved,
        List<String> contractIds,
        List<String> runtimeDomains
) {
    public EchoRequiredSystemModuleActivation {
        moduleId = EchoCompatText.requireText(moduleId, "moduleId");
        Objects.requireNonNull(runtimeStatus, "runtimeStatus");
        Objects.requireNonNull(contractIds, "contractIds");
        Objects.requireNonNull(runtimeDomains, "runtimeDomains");
        contractIds = contractIds.stream().distinct().sorted().toList();
        runtimeDomains = runtimeDomains.stream().distinct().sorted().toList();
    }

    public boolean active() {
        return descriptorLoaded
                && adapterCoreUsed
                && standaloneRuntimeCodeExecuted
                && allRuntimeAliasesResolved
                && !contractIds.isEmpty()
                && !runtimeDomains.isEmpty()
                && runtimeStatus != EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON;
    }
}
