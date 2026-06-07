package dev.echo.standalone.runtime.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EchoRuntimeModuleLifecycleBus {
    private final List<EchoRuntimeModuleLifecycleEvent> events = new ArrayList<>();
    private final List<Consumer<EchoRuntimeModuleLifecycleEvent>> listeners = new ArrayList<>();
    private long nextSequence;

    public synchronized void subscribe(Consumer<EchoRuntimeModuleLifecycleEvent> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        listeners.add(listener);
    }

    public void publish(String moduleId, EchoRuntimeModuleLifecycle lifecycle, String source) {
        EchoRuntimeModuleLifecycleEvent event;
        List<Consumer<EchoRuntimeModuleLifecycleEvent>> snapshot;
        synchronized (this) {
            event = new EchoRuntimeModuleLifecycleEvent(nextSequence++, moduleId, lifecycle, source);
            events.add(event);
            snapshot = List.copyOf(listeners);
        }
        for (Consumer<EchoRuntimeModuleLifecycleEvent> listener : snapshot) {
            try {
                listener.accept(event);
            } catch (RuntimeException ignored) {
                // Lifecycle observers must not be able to break module activation.
            }
        }
    }

    public synchronized List<EchoRuntimeModuleLifecycleEvent> events() {
        return List.copyOf(events);
    }

    public synchronized List<EchoRuntimeModuleLifecycleEvent> events(String moduleId) {
        return events.stream()
                .filter(event -> event.moduleId().equals(moduleId))
                .toList();
    }
}
