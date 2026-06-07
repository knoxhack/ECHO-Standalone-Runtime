package dev.echo.standalone.runtime.modules;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeModuleContentActivationRegistry {
    private final Map<String, List<EchoRuntimeModuleContentActivation>> activations = new LinkedHashMap<>();

    public synchronized EchoRuntimeModuleContentActivation register(String moduleId, String kind, String contentId) {
        EchoRuntimeModuleContentActivation activation = new EchoRuntimeModuleContentActivation(moduleId, kind, contentId);
        activations.computeIfAbsent(moduleId, ignored -> new ArrayList<>()).add(activation);
        return activation;
    }

    public synchronized List<EchoRuntimeModuleContentActivation> activations(String moduleId) {
        return List.copyOf(activations.getOrDefault(moduleId, List.of()));
    }

    public synchronized Map<String, List<EchoRuntimeModuleContentActivation>> snapshot() {
        LinkedHashMap<String, List<EchoRuntimeModuleContentActivation>> snapshot = new LinkedHashMap<>();
        activations.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> snapshot.put(entry.getKey(), entry.getValue().stream()
                        .sorted(Comparator.comparing(EchoRuntimeModuleContentActivation::kind)
                                .thenComparing(EchoRuntimeModuleContentActivation::contentId))
                        .toList()));
        return Map.copyOf(snapshot);
    }

    public synchronized List<EchoRuntimeModuleContentActivation> deactivateModule(String moduleId) {
        List<EchoRuntimeModuleContentActivation> removed = activations.remove(moduleId);
        return List.copyOf(removed == null ? List.of() : removed);
    }
}
