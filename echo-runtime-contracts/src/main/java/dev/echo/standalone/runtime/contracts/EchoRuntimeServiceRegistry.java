package dev.echo.standalone.runtime.contracts;

import java.util.Map;
import java.util.Optional;

public interface EchoRuntimeServiceRegistry {
    <T> void register(Class<T> type, T service);

    <T> Optional<T> find(Class<T> type);

    default <T> T require(Class<T> type) {
        return find(type).orElseThrow(() -> new IllegalStateException("Missing runtime service: " + type.getName()));
    }

    Map<Class<?>, Object> snapshot();
}
