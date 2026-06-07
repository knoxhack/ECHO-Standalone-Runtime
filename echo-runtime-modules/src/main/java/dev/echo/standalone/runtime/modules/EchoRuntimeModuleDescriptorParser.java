package dev.echo.standalone.runtime.modules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimeModuleDescriptorParser {
    public EchoRuntimeModuleDescriptor parse(Path descriptorPath) throws IOException {
        Object parsed = EchoRuntimeModuleJson.parse(Files.readString(descriptorPath));
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Module descriptor must be a JSON object: " + descriptorPath);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) rawMap;
        List<String> missingRequiredFields = EchoRuntimeModuleDescriptorSchema.missingRequiredFields(json);
        if (!missingRequiredFields.isEmpty()) {
            throw new IllegalArgumentException("Module descriptor missing required fields: " + missingRequiredFields);
        }
        return new EchoRuntimeModuleDescriptor(
                text(json, "schema", EchoRuntimeModuleDescriptorSchema.SCHEMA_ID),
                text(json, "id", null),
                text(json, "name", null),
                text(json, "version", "0.0.0"),
                text(json, "kind", "runtime_module"),
                text(json, "role", ""),
                EchoRuntimeModuleSide.fromId(text(json, "side", "both").toLowerCase(Locale.ROOT))
                        .orElse(EchoRuntimeModuleSide.BOTH),
                text(json, "trust", text(json, "trustLevel", "sandboxed")),
                bool(json, "official"),
                bool(json, "standalone"),
                stringList(json, "requires"),
                stringList(json, "optional"),
                stringList(json, "provides"),
                stringList(json, "consumes"),
                stringList(json, "gameModes"),
                stringList(json, "permissions"),
                classPath(json),
                text(json, "entrypoint", ""),
                adapterCoreEntrypoint(json),
                stringObject(json, "requiresVersions"),
                stringObject(json, "optionalVersions"),
                object(json, "access"),
                descriptorPath.toAbsolutePath().normalize()
        );
    }

    private static String text(Map<String, Object> json, String key, String defaultValue) {
        Object value = json.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Descriptor field '" + key + "' must be a string");
        }
        return text;
    }

    private static boolean bool(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return false;
        }
        if (!(value instanceof Boolean bool)) {
            throw new IllegalArgumentException("Descriptor field '" + key + "' must be a boolean");
        }
        return bool;
    }

    private static List<String> stringList(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Descriptor field '" + key + "' must be an array");
        }
        return list.stream()
                .map(item -> {
                    if (!(item instanceof String text)) {
                        throw new IllegalArgumentException("Descriptor field '" + key + "' must contain only strings");
                    }
                    return text;
                })
                .toList();
    }

    private static Map<String, Object> object(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Descriptor field '" + key + "' must be an object");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> typed = (Map<String, Object>) map;
        return typed;
    }

    private static String adapterCoreEntrypoint(Map<String, Object> json) {
        String root = text(json, "adapterCoreEntrypoint", "");
        if (!root.isBlank()) {
            return root;
        }
        Map<String, Object> access = object(json, "access");
        Object nativeEntrypoint = access.get("nativeEntrypoint");
        if (nativeEntrypoint instanceof String text && !text.isBlank()) {
            return text;
        }
        Object standaloneEntrypoint = access.get("standaloneEntrypoint");
        if (standaloneEntrypoint instanceof String text && !text.isBlank()) {
            return text;
        }
        return "";
    }

    private static List<String> classPath(Map<String, Object> json) {
        List<String> classPath = stringList(json, "classPath");
        if (!classPath.isEmpty()) {
            return classPath;
        }
        return stringList(json, "classpath");
    }

    private static Map<String, String> stringObject(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Descriptor field '" + key + "' must be an object");
        }
        java.util.LinkedHashMap<String, String> typed = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof String objectKey) || !(entry.getValue() instanceof String objectValue)) {
                throw new IllegalArgumentException("Descriptor field '" + key + "' must contain only string keys and values");
            }
            typed.put(objectKey, objectValue);
        }
        return typed;
    }
}
