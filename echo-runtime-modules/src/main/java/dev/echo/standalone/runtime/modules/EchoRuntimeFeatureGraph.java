package dev.echo.standalone.runtime.modules;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoRuntimeFeatureGraph(
        Map<String, List<String>> providersByFeature,
        Map<String, List<String>> consumersByFeature,
        List<String> missingRequiredFeatures
) {
    public EchoRuntimeFeatureGraph {
        Objects.requireNonNull(providersByFeature, "providersByFeature");
        Objects.requireNonNull(consumersByFeature, "consumersByFeature");
        Objects.requireNonNull(missingRequiredFeatures, "missingRequiredFeatures");
        providersByFeature = copy(providersByFeature);
        consumersByFeature = copy(consumersByFeature);
        missingRequiredFeatures = List.copyOf(missingRequiredFeatures);
    }

    public static EchoRuntimeFeatureGraph from(List<EchoRuntimeModuleDescriptor> descriptors) {
        TreeMap<String, List<String>> providers = new TreeMap<>();
        TreeMap<String, List<String>> consumers = new TreeMap<>();
        for (EchoRuntimeModuleDescriptor descriptor : descriptors) {
            for (String feature : descriptor.provides()) {
                providers.compute(feature, (ignored, current) -> append(current, descriptor.id()));
            }
            for (String feature : descriptor.consumes()) {
                consumers.compute(feature, (ignored, current) -> append(current, descriptor.id()));
            }
        }
        List<String> missing = consumers.keySet().stream()
                .filter(feature -> !providers.containsKey(feature))
                .toList();
        return new EchoRuntimeFeatureGraph(providers, consumers, missing);
    }

    private static List<String> append(List<String> current, String value) {
        if (current == null) {
            return List.of(value);
        }
        java.util.ArrayList<String> next = new java.util.ArrayList<>(current);
        next.add(value);
        next.sort(String::compareTo);
        return List.copyOf(next);
    }

    private static Map<String, List<String>> copy(Map<String, List<String>> input) {
        TreeMap<String, List<String>> copy = new TreeMap<>();
        input.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }
}
