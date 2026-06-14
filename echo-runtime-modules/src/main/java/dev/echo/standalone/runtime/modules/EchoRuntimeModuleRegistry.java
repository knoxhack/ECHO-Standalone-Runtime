package dev.echo.standalone.runtime.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoRuntimeModuleRegistry {
    private final Map<String, EchoRuntimeModuleDescriptor> descriptors = new LinkedHashMap<>();
    private final Map<String, String> aliases = new LinkedHashMap<>();
    private final Map<String, EchoRuntimeModuleLifecycle> lifecycle = new LinkedHashMap<>();
    private final Map<String, List<EchoRuntimeModuleLifecycle>> traces = new LinkedHashMap<>();
    private final Map<String, List<String>> notes = new LinkedHashMap<>();
    private final Map<String, EchoRuntimeModuleStatus> runtimeStatuses = new LinkedHashMap<>();

    public synchronized void register(EchoRuntimeModuleDescriptor descriptor) {
        Objects.requireNonNull(descriptor, "descriptor");
        descriptors.put(descriptor.id(), descriptor);
        for (String alias : descriptor.aliases()) {
            aliases.putIfAbsent(alias, descriptor.id());
        }
        lifecycle.put(descriptor.id(), EchoRuntimeModuleLifecycle.DISCOVERED);
        traces.put(descriptor.id(), new ArrayList<>(List.of(EchoRuntimeModuleLifecycle.DISCOVERED)));
        runtimeStatuses.put(descriptor.id(), EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON);
    }

    public synchronized void transition(String moduleId, EchoRuntimeModuleLifecycle next) {
        String canonicalModuleId = canonicalModuleId(moduleId);
        if (!descriptors.containsKey(canonicalModuleId)) {
            return;
        }
        lifecycle.put(canonicalModuleId, next);
        traces.computeIfAbsent(canonicalModuleId, ignored -> new ArrayList<>()).add(next);
    }

    public synchronized void transitionAll(String moduleId, List<EchoRuntimeModuleLifecycle> states) {
        for (EchoRuntimeModuleLifecycle state : states) {
            transition(moduleId, state);
        }
    }

    public synchronized void note(String moduleId, String note) {
        notes.computeIfAbsent(canonicalModuleId(moduleId), ignored -> new ArrayList<>()).add(note);
    }

    public synchronized void setRuntimeStatus(String moduleId, EchoRuntimeModuleStatus status, String reason) {
        Objects.requireNonNull(status, "status");
        String canonicalModuleId = canonicalModuleId(moduleId);
        if (!descriptors.containsKey(canonicalModuleId)) {
            return;
        }
        runtimeStatuses.put(canonicalModuleId, status);
        note(canonicalModuleId, status.id() + ": " + requireText(reason, "reason"));
    }

    public synchronized Optional<EchoRuntimeModuleDescriptor> find(String moduleId) {
        return Optional.ofNullable(descriptors.get(canonicalModuleId(moduleId)));
    }

    public synchronized List<EchoRuntimeModuleDescriptor> descriptors() {
        return descriptors.values().stream()
                .sorted(Comparator.comparing(EchoRuntimeModuleDescriptor::id))
                .toList();
    }

    public synchronized EchoRuntimeModuleLifecycle lifecycle(String moduleId) {
        return lifecycle.getOrDefault(canonicalModuleId(moduleId), EchoRuntimeModuleLifecycle.FAILED);
    }

    public synchronized List<EchoRuntimeModuleLifecycle> trace(String moduleId) {
        return List.copyOf(traces.getOrDefault(canonicalModuleId(moduleId), List.of()));
    }

    public synchronized List<String> notes(String moduleId) {
        return List.copyOf(notes.getOrDefault(canonicalModuleId(moduleId), List.of()));
    }

    public synchronized Map<String, EchoRuntimeModuleLifecycle> lifecycleSnapshot() {
        return Map.copyOf(lifecycle);
    }

    public synchronized EchoRuntimeModuleStatus runtimeStatus(String moduleId) {
        return runtimeStatuses.getOrDefault(canonicalModuleId(moduleId), EchoRuntimeModuleStatus.RUNTIME_DISABLED_WITH_REASON);
    }

    public synchronized Map<String, EchoRuntimeModuleStatus> runtimeStatusSnapshot() {
        return Map.copyOf(runtimeStatuses);
    }

    public synchronized Optional<String> canonicalId(String moduleId) {
        String canonicalModuleId = canonicalModuleId(moduleId);
        return descriptors.containsKey(canonicalModuleId) ? Optional.of(canonicalModuleId) : Optional.empty();
    }

    private String canonicalModuleId(String moduleId) {
        return aliases.getOrDefault(moduleId, moduleId);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
