package dev.echo.standalone.runtime.assets;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class EchoLangResolver {
    private final EchoMinecraftAssetResolver resolver;

    public EchoLangResolver(EchoMinecraftAssetResolver resolver) {
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    public Map<String, String> load(String namespace, String locale) throws IOException {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        for (String json : resolver.loadAllLang(namespace, locale)) {
            merged.putAll(flatStringMap(json));
        }
        return Map.copyOf(merged);
    }

    public String translate(String namespace, String locale, String key, String fallback) throws IOException {
        return load(namespace, locale).getOrDefault(key, fallback);
    }

    private static Map<String, String> flatStringMap(String json) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        int index = 0;
        while (index < json.length()) {
            int keyStart = json.indexOf('"', index);
            if (keyStart < 0) {
                break;
            }
            int keyEnd = json.indexOf('"', keyStart + 1);
            int colon = keyEnd < 0 ? -1 : json.indexOf(':', keyEnd + 1);
            int valueStart = colon < 0 ? -1 : json.indexOf('"', colon + 1);
            int valueEnd = valueStart < 0 ? -1 : json.indexOf('"', valueStart + 1);
            if (keyEnd < 0 || colon < 0 || valueStart < 0 || valueEnd < 0) {
                break;
            }
            values.put(json.substring(keyStart + 1, keyEnd), json.substring(valueStart + 1, valueEnd));
            index = valueEnd + 1;
        }
        return Map.copyOf(values);
    }
}
