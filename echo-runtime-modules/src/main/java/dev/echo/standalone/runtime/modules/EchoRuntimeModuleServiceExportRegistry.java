package dev.echo.standalone.runtime.modules;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public final class EchoRuntimeModuleServiceExportRegistry {
    private final Map<String, EchoRuntimeModuleServiceExport> exports = new LinkedHashMap<>();

    public synchronized EchoRuntimeModuleServiceExport export(String moduleId, String serviceId, Object service) {
        if (exports.containsKey(serviceId)) {
            EchoRuntimeModuleServiceExport existing = exports.get(serviceId);
            throw new IllegalStateException("Service '" + serviceId + "' is already exported by module '"
                    + existing.moduleId() + "'");
        }
        EchoRuntimeModuleServiceExport export = new EchoRuntimeModuleServiceExport(moduleId, serviceId, service);
        exports.put(serviceId, export);
        return export;
    }

    public synchronized Optional<EchoRuntimeModuleServiceExport> findExport(String serviceId) {
        return Optional.ofNullable(exports.get(serviceId));
    }

    public synchronized <T> Optional<T> findService(String serviceId, Class<T> type) {
        return findExport(serviceId)
                .map(EchoRuntimeModuleServiceExport::service)
                .filter(type::isInstance)
                .map(type::cast);
    }

    public synchronized Map<String, EchoRuntimeModuleServiceExport> snapshot() {
        return Map.copyOf(new TreeMap<>(exports));
    }

    public synchronized List<EchoRuntimeModuleServiceExport> revokeModule(String moduleId) {
        List<EchoRuntimeModuleServiceExport> revoked = exports.values().stream()
                .filter(export -> export.moduleId().equals(moduleId))
                .toList();
        for (EchoRuntimeModuleServiceExport export : revoked) {
            exports.remove(export.serviceId());
        }
        return revoked;
    }
}
