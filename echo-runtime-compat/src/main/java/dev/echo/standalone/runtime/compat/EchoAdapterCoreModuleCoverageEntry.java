package dev.echo.standalone.runtime.compat;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public record EchoAdapterCoreModuleCoverageEntry(
        String moduleId,
        String moduleName,
        String moduleVersion,
        EchoAdapterCoreModuleCoverageStatus status,
        boolean standaloneDeclared,
        boolean adapterCoreDeclared,
        boolean adapterCoreProvider,
        boolean nativeEntrypointDeclared,
        boolean liveBindingAvailable,
        List<EchoAdapterCoreDomain> adapterDomains,
        List<EchoAdapterCoreRuntimeKind> adapterRuntimes,
        List<String> adapterKeys,
        List<String> aliases,
        List<String> gaps,
        Path descriptorPath
) {
    public EchoAdapterCoreModuleCoverageEntry {
        moduleId = EchoCompatText.requireText(moduleId, "moduleId");
        moduleName = EchoCompatText.requireText(moduleName, "moduleName");
        moduleVersion = EchoCompatText.requireText(moduleVersion, "moduleVersion");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(adapterDomains, "adapterDomains");
        Objects.requireNonNull(adapterRuntimes, "adapterRuntimes");
        Objects.requireNonNull(adapterKeys, "adapterKeys");
        Objects.requireNonNull(aliases, "aliases");
        Objects.requireNonNull(gaps, "gaps");
        Objects.requireNonNull(descriptorPath, "descriptorPath");
        adapterDomains = adapterDomains.stream().distinct().sorted().toList();
        adapterRuntimes = adapterRuntimes.stream().distinct().sorted().toList();
        adapterKeys = adapterKeys.stream().sorted().toList();
        String canonicalModuleId = moduleId;
        aliases = aliases.stream()
                .distinct()
                .filter(alias -> !alias.equals(canonicalModuleId))
                .sorted()
                .toList();
        gaps = List.copyOf(gaps);
        descriptorPath = descriptorPath.toAbsolutePath().normalize();
    }

    public boolean active() {
        return status == EchoAdapterCoreModuleCoverageStatus.ACTIVE;
    }
}
