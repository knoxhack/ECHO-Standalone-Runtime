package dev.echo.standalone.runtime.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

final class EchoDataObjects {
    private EchoDataObjects() {
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> object(String sourceLogicalId, String text) {
        Object value = EchoDataJson.parse(text);
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Data file must be a JSON object: " + sourceLogicalId);
        }
        return copyObject((Map<String, Object>) map);
    }

    static String string(Map<String, Object> object, String key, String fallback) {
        Object value = object.get(key);
        return value == null ? fallback : String.valueOf(value);
    }

    static List<String> stringList(Map<String, Object> object, String key) {
        Object value = object.get(key);
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        ArrayList<String> values = new ArrayList<>();
        for (Object item : list) {
            values.add(String.valueOf(item));
        }
        return values.stream().sorted().toList();
    }

    static Map<String, Object> copyObject(Map<String, Object> object) {
        LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
        new TreeMap<>(object).forEach((key, value) -> copy.put(key, copyValue(value)));
        return Map.copyOf(copy);
    }

    private static Object copyValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            LinkedHashMap<String, Object> copy = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                copy.put(String.valueOf(entry.getKey()), copyValue(entry.getValue()));
            }
            return Map.copyOf(copy);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(EchoDataObjects::copyValue).toList();
        }
        return value;
    }
}
