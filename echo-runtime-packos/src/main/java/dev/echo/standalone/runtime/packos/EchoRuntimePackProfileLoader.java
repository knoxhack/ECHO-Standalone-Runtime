package dev.echo.standalone.runtime.packos;

import dev.echo.standalone.runtime.contracts.EchoRuntimeMode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EchoRuntimePackProfileLoader {
    public EchoRuntimePackProfile load(Path profilePath) throws IOException {
        Object parsed = EchoRuntimePackJson.parse(Files.readString(profilePath));
        if (!(parsed instanceof Map<?, ?> rawMap)) {
            throw new IllegalArgumentException("Pack profile must be a JSON object: " + profilePath);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> json = (Map<String, Object>) rawMap;
        return new EchoRuntimePackProfile(
                text(json, "schema", "echo.runtime.pack.v1"),
                text(json, "packId", null),
                text(json, "packName", null),
                text(json, "variant", "base"),
                EchoRuntimePackChannel.fromId(text(json, "channel", "dev").toLowerCase(Locale.ROOT))
                        .orElse(EchoRuntimePackChannel.DEV),
                text(json, "runtimeVersion", null),
                stringList(json, "enabledModules"),
                stringList(json, "enabledFeatures"),
                Path.of(text(json, "lockfile", "echo.pack.lock.json")),
                stringMap(json, "saveCompatibility"),
                stringList(json, "assetPacks"),
                stringList(json, "dataPacks"),
                text(json, "theme", "echo_default"),
                EchoRuntimeMode.fromId(text(json, "launchMode", "headless-test"))
                        .orElse(EchoRuntimeMode.HEADLESS_TEST),
                profilePath.toAbsolutePath().normalize()
        );
    }

    private static String text(Map<String, Object> json, String key, String defaultValue) {
        Object value = json.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (!(value instanceof String text)) {
            throw new IllegalArgumentException("Pack profile field '" + key + "' must be a string");
        }
        return text;
    }

    private static List<String> stringList(Map<String, Object> json, String key) {
        Object value = json.get(key);
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Pack profile field '" + key + "' must be an array");
        }
        return list.stream().map(item -> {
            if (!(item instanceof String text)) {
                throw new IllegalArgumentException("Pack profile field '" + key + "' must contain only strings");
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
            throw new IllegalArgumentException("Pack profile field '" + key + "' must be an object");
        }
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        map.forEach((rawKey, rawValue) -> {
            if (!(rawKey instanceof String textKey) || !(rawValue instanceof String textValue)) {
                throw new IllegalArgumentException("Pack profile field '" + key + "' must map strings to strings");
            }
            result.put(textKey, textValue);
        });
        return result;
    }
}
