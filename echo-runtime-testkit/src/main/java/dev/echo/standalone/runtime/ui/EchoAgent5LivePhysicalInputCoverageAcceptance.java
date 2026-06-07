package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoAgent5LivePhysicalInputCoverageAcceptance {
    private static final Map<String, String> REQUIRED = Map.ofEntries(
            Map.entry("M", "TERMINAL"),
            Map.entry("G", "INDEX"),
            Map.entry("R", "INDEX"),
            Map.entry("U", "INDEX"),
            Map.entry("B", "INDEX"),
            Map.entry("LEFT_ALT", "LENS"),
            Map.entry("J", "HOLOMAP"),
            Map.entry("K", "HOLOMAP"),
            Map.entry("RIGHT_BRACKET", "HOLOMAP"),
            Map.entry("LEFT_BRACKET", "HOLOMAP"),
            Map.entry("BACKSLASH", "HOLOMAP"),
            Map.entry("N", "SIGNALOS"),
            Map.entry("ESCAPE", "PAUSE"),
            Map.entry("X", "ASHFALL_DRONE"),
            Map.entry("C", "ASHFALL_DRONE"),
            Map.entry("Y", "ASHFALL_DRONE"),
            Map.entry("Z", "ASHFALL_DRONE")
    );
    private static final List<String> REQUIRED_KEYS = List.of(
            "M",
            "G",
            "R",
            "U",
            "B",
            "LEFT_ALT",
            "J",
            "K",
            "RIGHT_BRACKET",
            "LEFT_BRACKET",
            "BACKSLASH",
            "N",
            "ESCAPE",
            "X",
            "C",
            "Y",
            "Z"
    );

    private EchoAgent5LivePhysicalInputCoverageAcceptance() {
    }

    public static Map<String, Object> assess(Object observedEvents) {
        List<?> events = observedEvents instanceof List<?> list ? list : List.of();
        Set<String> observedKeys = new LinkedHashSet<>();
        List<String> rejectedEvents = new ArrayList<>();
        for (Object event : events) {
            if (!(event instanceof Map<?, ?> map)) {
                rejectedEvents.add("not_a_map");
                continue;
            }
            String key = text(map.get("key"));
            String surface = text(map.get("surface"));
            boolean valid = Boolean.TRUE.equals(map.get("handled"))
                    && Boolean.TRUE.equals(map.get("physicalPoller"))
                    && Boolean.TRUE.equals(map.get("serviceCodeExecuted"))
                    && REQUIRED.containsKey(key)
                    && REQUIRED.get(key).equals(surface);
            if (valid) {
                observedKeys.add(key);
            } else {
                rejectedEvents.add(key + "->" + surface);
            }
        }
        List<String> missingKeys = REQUIRED_KEYS.stream()
                .filter(key -> !observedKeys.contains(key))
                .toList();
        boolean accepted = missingKeys.isEmpty();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("requiredKeys", REQUIRED_KEYS);
        result.put("observedKeys", List.copyOf(observedKeys));
        result.put("missingKeys", missingKeys);
        result.put("rejectedEvents", rejectedEvents);
        result.put("observedCount", observedKeys.size());
        result.put("effect", accepted
                ? "live_physical_input_coverage:accepted:" + observedKeys.size()
                : "live_physical_input_coverage:rejected:missing=" + String.join(",", missingKeys));
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        List<Map<String, Object>> acceptedEvents = REQUIRED_KEYS.stream()
                .map(key -> event(key, REQUIRED.get(key)))
                .toList();
        Map<String, Object> accepted = assess(acceptedEvents);
        Map<String, Object> rejectedMissing = assess(acceptedEvents.stream()
                .filter(event -> !"Z".equals(event.get("key")))
                .toList());
        List<Map<String, Object>> wrongSurfaceEvents = new ArrayList<>(acceptedEvents);
        wrongSurfaceEvents.set(0, event("M", "INDEX"));
        Map<String, Object> rejectedWrongSurface = assess(wrongSurfaceEvents);
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_physical_input_coverage:accepted:17".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedMissing.get("accepted"))
                && strings(rejectedMissing.get("missingKeys")).contains("Z")
                && Boolean.FALSE.equals(rejectedWrongSurface.get("accepted"))
                && strings(rejectedWrongSurface.get("missingKeys")).contains("M");
        return Map.of(
                "livePhysicalInputCoverageAcceptanceClass",
                EchoAgent5LivePhysicalInputCoverageAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedMissing", rejectedMissing,
                "rejectedWrongSurface", rejectedWrongSurface,
                "passed", passed,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static Map<String, Object> event(String key, String surface) {
        return Map.of(
                "handled", true,
                "physicalPoller", true,
                "serviceCodeExecuted", true,
                "key", key,
                "surface", surface
        );
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(EchoAgent5LivePhysicalInputCoverageAcceptance::text).toList();
        }
        return List.of();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
