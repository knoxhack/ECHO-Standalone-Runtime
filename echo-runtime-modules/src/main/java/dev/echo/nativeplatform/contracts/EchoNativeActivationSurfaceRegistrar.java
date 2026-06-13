package dev.echo.nativeplatform.contracts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class EchoNativeActivationSurfaceRegistrar {
    private EchoNativeActivationSurfaceRegistrar() {
    }

    public static Map<String, String> bridgeContext(EchoNativeModuleLoadContext context) {
        Map<String, String> data = new LinkedHashMap<>();
        data.put("moduleId", context.descriptor().id());
        data.put("moduleName", context.descriptor().name());
        data.put("moduleVersion", context.descriptor().version());
        data.put("runtime", "echo_runtime_standalone");
        data.put("loader", "echo-standalone-runtime");
        data.put("packId", text(context.attributes().getOrDefault("packId", "standalone-runtime")));
        Path descriptorPath = context.descriptor().descriptorPath();
        data.put("descriptorPath", descriptorPath == null ? "" : descriptorPath.toString());
        for (Map.Entry<String, Object> entry : context.attributes().entrySet()) {
            data.putIfAbsent(entry.getKey(), text(entry.getValue()));
        }
        return Map.copyOf(data);
    }

    public static Map<String, Object> activation(
            EchoNativeModuleLoadContext context,
            Supplier<Map<String, Object>> activationSupplier
    ) {
        Map<String, Object> existing = object(context.attributes().get("nativeActivationSurface"));
        if (!existing.isEmpty()) {
            return existing;
        }
        Map<String, Object> activation = object(activationSupplier.get());
        context.attribute("nativeActivationSurface", activation);
        context.attribute("nativeActivationStage", text(activation.get("activationStage")));
        context.attribute("nativeAdapterCodeExecuted",
                Boolean.TRUE.equals(activation.get("nativeAdapterCodeExecuted")));
        context.attribute("nativeServiceCodeExecuted",
                Boolean.TRUE.equals(activation.get("serviceCodeExecuted")));
        context.attribute("nativeLogicalRegistrationCount", logicalRegistrationCount(activation));
        return activation;
    }

    public static void registerServices(
            EchoNativeModuleLoadContext context,
            Object service,
            Map<String, Object> activation,
            String... declaredSurfaces
    ) {
        Map<String, Object> safeActivation = object(activation);
        String moduleId = context.descriptor().id();
        String activationServiceId = "service." + normalized(moduleId) + ".native_activation";
        registerIfAbsent(
                context,
                activationServiceId,
                safeActivation,
                surfaces(safeActivation, declaredSurfaces)
        );
        if (service != null) {
            registerIfAbsent(
                    context,
                    "module." + normalized(moduleId) + ".native_entrypoint",
                    service,
                    concat(surfaces(safeActivation, declaredSurfaces), List.of("entrypoint"))
            );
        }
        for (String contract : stringList(safeActivation.get("registeredFeatureContracts"))) {
            registerIfAbsent(
                    context,
                    "feature." + normalized(moduleId) + "." + normalized(contract),
                    Map.of("kind", "feature_contract", "id", contract, "moduleId", moduleId),
                    List.of("features", "contracts")
            );
        }
        recordActivationMutations(context, safeActivation);
    }

    public static void registerContent(EchoNativeModuleLoadContext context, Map<String, Object> activation) {
        Map<String, Object> safeActivation = object(activation);
        int count = logicalRegistrationCount(safeActivation);
        if (count > 0 || !stringList(safeActivation.get("registeredFeatureContracts")).isEmpty()) {
            context.recordMutation(
                    "registry",
                    "native_activation_surface_content_registered",
                    context.descriptor().id(),
                    EchoNativeLoadStatus.MUTATED
            );
        }
        for (Map<String, Object> registration : registryRegistrations(safeActivation)) {
            String registry = text(registration.get("registry"));
            String id = text(registration.get("id"));
            if (registry.isBlank() || id.isBlank()) {
                continue;
            }
            registerIfAbsent(
                    context,
                    "content." + normalized(context.descriptor().id()) + "." + normalized(registry) + "." + normalized(id),
                    Map.of("kind", "registry_registration", "evidence", Map.copyOf(registration)),
                    List.of("registry", normalized(registry))
            );
        }
    }

    public static void ready(EchoNativeModuleLoadContext context) {
        context.attribute("nativeActivationReady", true);
        context.recordMutation(
                "lifecycle",
                "native_activation_surface_ready",
                context.descriptor().id(),
                EchoNativeLoadStatus.RESOLVED
        );
    }

    private static void registerIfAbsent(
            EchoNativeModuleLoadContext context,
            String serviceId,
            Object service,
            List<String> surfaces
    ) {
        if (context.serviceRegistry().hasService(context.descriptor().id(), serviceId)
                || context.serviceRegistry().hasService(serviceId)) {
            return;
        }
        context.registerService(serviceId, service, surfaces.toArray(String[]::new));
    }

    private static List<Map<String, Object>> registryRegistrations(Map<String, Object> activation) {
        Map<String, Object> registryBridge = object(activation.get("registryBridge"));
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> registration : objectList(registryBridge.get("registrations"))) {
            result.add(registration);
        }
        for (Map<String, Object> registration : objectList(registryBridge.get("entries"))) {
            result.add(registration);
        }
        return List.copyOf(result);
    }

    private static void recordActivationMutations(EchoNativeModuleLoadContext context, Map<String, Object> activation) {
        if (Boolean.TRUE.equals(activation.get("nativeAdapterCodeExecuted"))) {
            context.recordMutation(
                    "adapter",
                    "native_activation_adapter_code_executed",
                    context.descriptor().id(),
                    EchoNativeLoadStatus.MUTATED
            );
        }
        if (Boolean.TRUE.equals(activation.get("serviceCodeExecuted"))) {
            context.recordMutation(
                    "service",
                    "native_activation_service_code_executed",
                    context.descriptor().id(),
                    EchoNativeLoadStatus.MUTATED
            );
        }
        if (Boolean.TRUE.equals(activation.get("registryMutated"))) {
            context.recordMutation(
                    "registry",
                    "native_activation_registry_mutated",
                    context.descriptor().id(),
                    EchoNativeLoadStatus.MUTATED
            );
        }
        if (Boolean.TRUE.equals(activation.get("transformsPerformed"))) {
            context.recordMutation(
                    "transform",
                    "native_activation_transforms_performed",
                    context.descriptor().id(),
                    EchoNativeLoadStatus.MUTATED
            );
        }
    }

    private static int logicalRegistrationCount(Map<String, Object> activation) {
        Object count = activation.get("logicalRegistrationCount");
        if (count instanceof Number number) {
            return Math.max(0, number.intValue());
        }
        return stringList(activation.get("registeredFeatureContracts")).size();
    }

    private static List<String> surfaces(Map<String, Object> activation, String... declaredSurfaces) {
        ArrayList<String> result = new ArrayList<>();
        for (String surface : declaredSurfaces == null ? new String[0] : declaredSurfaces) {
            addSurface(result, surface);
        }
        for (String domain : stringList(activation.get("adapterDomains"))) {
            addSurface(result, domain);
        }
        for (String target : stringList(activation.get("runtimeTargets"))) {
            addSurface(result, target);
        }
        if (result.isEmpty()) {
            result.add("native_activation");
        }
        return List.copyOf(result);
    }

    private static List<String> concat(List<String> left, List<String> right) {
        ArrayList<String> result = new ArrayList<>(left);
        for (String value : right) {
            addSurface(result, value);
        }
        return List.copyOf(result);
    }

    private static void addSurface(List<String> result, String value) {
        String surface = normalized(value);
        if (!surface.isBlank() && !result.contains(surface)) {
            result.add(surface);
        }
    }

    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<String> result = new ArrayList<>();
        for (Object item : list) {
            String text = text(item);
            if (!text.isBlank()) {
                result.add(text);
            }
        }
        return List.copyOf(result);
    }

    private static List<Map<String, Object>> objectList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> object = object(item);
            if (!object.isEmpty()) {
                result.add(object);
            }
        }
        return List.copyOf(result);
    }

    private static Map<String, Object> object(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return Map.of();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() != null) {
                result.put(String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return Map.copyOf(result);
    }

    private static String normalized(String value) {
        String text = value == null ? "" : value.toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder();
        boolean previousSeparator = false;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if ((ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')) {
                result.append(ch);
                previousSeparator = false;
            } else if (!previousSeparator) {
                result.append('.');
                previousSeparator = true;
            }
        }
        while (result.length() > 0 && result.charAt(result.length() - 1) == '.') {
            result.deleteCharAt(result.length() - 1);
        }
        return result.toString();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
