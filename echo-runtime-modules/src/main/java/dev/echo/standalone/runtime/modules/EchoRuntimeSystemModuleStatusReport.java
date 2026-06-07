package dev.echo.standalone.runtime.modules;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record EchoRuntimeSystemModuleStatusReport(
        List<EchoRuntimeSystemModuleStatus> entries
) {
    public EchoRuntimeSystemModuleStatusReport {
        Objects.requireNonNull(entries, "entries");
        entries = entries.stream()
                .sorted(Comparator.comparing(EchoRuntimeSystemModuleStatus::moduleId))
                .toList();
    }

    public static EchoRuntimeSystemModuleStatusReport forRequiredModules(
            EchoRuntimeModuleRegistry registry,
            List<String> requiredModuleIds
    ) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(requiredModuleIds, "requiredModuleIds");
        return new EchoRuntimeSystemModuleStatusReport(requiredModuleIds.stream()
                .distinct()
                .sorted()
                .map(moduleId -> registry.find(moduleId)
                        .map(descriptor -> new EchoRuntimeSystemModuleStatus(
                                moduleId,
                                registry.runtimeStatus(moduleId),
                                registry.notes(moduleId).isEmpty()
                                        ? "descriptor loaded"
                                        : String.join("; ", registry.notes(moduleId))
                        ))
                        .orElseGet(() -> new EchoRuntimeSystemModuleStatus(
                                moduleId,
                                EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON,
                                "missing runtime descriptor"
                        )))
                .toList());
    }

    public EchoRuntimeSystemModuleStatus require(String moduleId) {
        return entries.stream()
                .filter(entry -> entry.moduleId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing system module status: " + moduleId));
    }
}
