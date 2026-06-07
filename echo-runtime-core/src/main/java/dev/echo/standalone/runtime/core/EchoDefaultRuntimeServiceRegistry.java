package dev.echo.standalone.runtime.core;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoDefaultRuntimeServiceRegistry implements EchoRuntimeServiceRegistry {
    private final Map<Class<?>, Object> services = new LinkedHashMap<>();

    @Override
    public synchronized <T> void register(Class<T> type, T service) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(service, "service");
        services.put(type, type.cast(service));
    }

    @Override
    public synchronized <T> Optional<T> find(Class<T> type) {
        Objects.requireNonNull(type, "type");
        Object service = services.get(type);
        if (service == null) {
            return Optional.empty();
        }
        return Optional.of(type.cast(service));
    }

    @Override
    public synchronized Map<Class<?>, Object> snapshot() {
        return Map.copyOf(services);
    }
}
