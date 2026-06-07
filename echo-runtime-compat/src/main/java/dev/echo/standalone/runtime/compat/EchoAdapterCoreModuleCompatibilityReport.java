package dev.echo.standalone.runtime.compat;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record EchoAdapterCoreModuleCompatibilityReport(
        String moduleId,
        EchoAdapterCoreModuleCoverageStatus status,
        boolean standaloneDeclared,
        boolean adapterCoreDeclared,
        boolean adapterCoreProvider,
        boolean nativeEntrypointDeclared,
        boolean liveBindingAvailable,
        List<EchoAdapterCoreDomain> domains,
        List<EchoAdapterCoreRuntimeKind> runtimes,
        List<String> adapterKeys,
        List<String> gaps,
        Path descriptorPath
) {
    public EchoAdapterCoreModuleCompatibilityReport {
        moduleId = EchoCompatText.requireText(moduleId, "moduleId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(domains, "domains");
        Objects.requireNonNull(runtimes, "runtimes");
        Objects.requireNonNull(adapterKeys, "adapterKeys");
        Objects.requireNonNull(gaps, "gaps");
        Objects.requireNonNull(descriptorPath, "descriptorPath");
        domains = domains.stream().distinct().sorted().toList();
        runtimes = runtimes.stream().distinct().sorted().toList();
        adapterKeys = adapterKeys.stream().distinct().sorted().toList();
        gaps = List.copyOf(gaps);
        descriptorPath = descriptorPath.toAbsolutePath().normalize();
    }

    public boolean reportComplete() {
        return !moduleId.isBlank()
                && !domains.isEmpty()
                && !runtimes.isEmpty()
                && !adapterKeys.isEmpty()
                && (status == EchoAdapterCoreModuleCoverageStatus.ACTIVE || !gaps.isEmpty());
    }

    public boolean allRuntimeTargetsDeclared() {
        return EchoAdapterCoreContractLock.supportsEveryRuntime(runtimes);
    }
}
