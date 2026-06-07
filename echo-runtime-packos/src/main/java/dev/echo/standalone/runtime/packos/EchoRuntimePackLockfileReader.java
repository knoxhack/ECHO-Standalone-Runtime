package dev.echo.standalone.runtime.packos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimePackLockfileReader {
    public EchoRuntimePackLockfile read(Path lockfilePath) throws IOException {
        Object parsed = EchoRuntimePackJson.parse(Files.readString(lockfilePath));
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Pack lockfile must be a JSON object: " + lockfilePath);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) rawMap;
        return new EchoRuntimePackLockfile(
                text(json, "schema", "echo.runtime.pack_lock.v1"),
                text(json, "packId", null),
                text(json, "runtimeVersion", null),
                stringMap(json, "lockedModules"),
                stringList(json, "lockedFeatures"),
                lockfilePath.toAbsolutePath().normalize()
        );
    }

    private static String text(Map<String, Object> json, String key, String defaultValue) {
        Object value = json.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Pack lockfile field '" + key + "' must be a string");
        }
        return text;
    }

    private static List<String> stringList(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Pack lockfile field '" + key + "' must be an array");
        }
        return list.stream().map(item -> {
            if (!(item instanceof String text)) {
                throw new IllegalArgumentException("Pack lockfile field '" + key + "' must contain only strings");
            }
            return text;
        }).toList();
    }

    private static Map<String, String> stringMap(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return Map.of();
        }
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Pack lockfile field '" + key + "' must be an object");
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        map.forEach((rawKey, rawValue) -> {
            if (!(rawKey instanceof String textKey) || !(rawValue instanceof String textValue)) {
                throw new IllegalArgumentException("Pack lockfile field '" + key + "' must map strings to strings");
            }
            result.put(textKey, textValue);
        });
        return result;
    }
}
