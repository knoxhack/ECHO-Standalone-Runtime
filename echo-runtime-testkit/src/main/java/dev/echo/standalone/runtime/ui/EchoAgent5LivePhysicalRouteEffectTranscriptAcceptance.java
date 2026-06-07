package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance {
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

    private EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance() {
    }

    public static Map<String, Object> assess(Object observedEvents) {
        List<?> events = observedEvents instanceof List<?> list ? list : List.of();
        Set<String> observedKeys = new LinkedHashSet<>();
        List<String> routedSurfaces = new ArrayList<>();
        List<String> rejectedEvents = new ArrayList<>();
        for (Object event : events) {
            if (!(event instanceof Map<?, ?> map)) {
                rejectedEvents.add("not_a_map");
                continue;
            }
            String key = text(map.get("key"));
            String surface = text(map.get("surface"));
            boolean sampled = integer(map.get("physicalEventSequence")) > 0
                    && integer(map.get("pollIteration")) > 0
                    && integer(map.get("pollKeySamples")) >= REQUIRED_KEYS.size();
            boolean routeMatches = Boolean.TRUE.equals(map.get("handled"))
                    && Boolean.TRUE.equals(map.get("physicalPoller"))
                    && Boolean.TRUE.equals(map.get("serviceCodeExecuted"))
                    && REQUIRED.containsKey(key)
                    && REQUIRED.get(key).equals(surface);
            boolean surfaceEffectAccepted = Boolean.TRUE.equals(map.get("liveSurfaceAccepted"))
                    && Boolean.TRUE.equals(map.get("liveSurfaceRendered"))
                    && Boolean.TRUE.equals(map.get("physicalInputAccepted"))
                    && Boolean.TRUE.equals(map.get("screenOwnershipAccepted"))
                    && Boolean.TRUE.equals(map.get("renderCallbackAccepted"));
            boolean effectAccepted = Boolean.TRUE.equals(map.get("routeEffectAccepted"))
                    && surfaceEffectAccepted;
            if (sampled && routeMatches && effectAccepted) {
                observedKeys.add(key);
                routedSurfaces.add(surface);
            } else {
                rejectedEvents.add(key + "->" + surface);
            }
        }
        List<String> missingKeys = REQUIRED_KEYS.stream()
                .filter(key -> !observedKeys.contains(key))
                .toList();
        boolean accepted = missingKeys.isEmpty() && rejectedEvents.isEmpty();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("requiredKeys", REQUIRED_KEYS);
        result.put("observedKeys", List.copyOf(observedKeys));
        result.put("routedSurfaces", List.copyOf(routedSurfaces));
        result.put("missingKeys", missingKeys);
        result.put("rejectedEvents", rejectedEvents);
        result.put("eventCount", events.size());
        result.put("effect", accepted
                ? "live_physical_route_effect_transcript:accepted:" + observedKeys.size()
                : "live_physical_route_effect_transcript:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        List<Map<String, Object>> acceptedEvents = new ArrayList<>();
        for (int i = 0; i < REQUIRED_KEYS.size(); i++) {
            String key = REQUIRED_KEYS.get(i);
            acceptedEvents.add(event(i + 1, i + 3, 51 + (i * 17), key, REQUIRED.get(key), true));
        }
        Map<String, Object> accepted = assess(acceptedEvents);
        List<Map<String, Object>> noSurfaceEffectEvents = new ArrayList<>(acceptedEvents);
        noSurfaceEffectEvents.set(0, event(1, 3, 33, "M", "TERMINAL", false));
        Map<String, Object> rejectedNoSurfaceEffect = assess(noSurfaceEffectEvents);
        List<Map<String, Object>> noRouteEffectEvents = new ArrayList<>(acceptedEvents);
        noRouteEffectEvents.set(16, event(17, 19, 323, "Z", "ASHFALL_DRONE", false));
        Map<String, Object> rejectedNoRouteEffect = assess(noRouteEffectEvents);
        List<Map<String, Object>> noSampleMetricsEvents = new ArrayList<>(acceptedEvents);
        Map<String, Object> missingSample = new LinkedHashMap<>(noSampleMetricsEvents.get(1));
        missingSample.remove("pollIteration");
        noSampleMetricsEvents.set(1, Map.copyOf(missingSample));
        Map<String, Object> rejectedNoSampleMetrics = assess(noSampleMetricsEvents);
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_physical_route_effect_transcript:accepted:17".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoSurfaceEffect.get("accepted"))
                && strings(rejectedNoSurfaceEffect.get("missingKeys")).contains("M")
                && Boolean.FALSE.equals(rejectedNoRouteEffect.get("accepted"))
                && strings(rejectedNoRouteEffect.get("missingKeys")).contains("Z")
                && Boolean.FALSE.equals(rejectedNoSampleMetrics.get("accepted"))
                && strings(rejectedNoSampleMetrics.get("missingKeys")).contains("G");
        return Map.ofEntries(
                Map.entry("livePhysicalRouteEffectTranscriptAcceptanceClass",
                        EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance.class.getSimpleName()),
                Map.entry("accepted", accepted),
                Map.entry("rejectedNoSurfaceEffect", rejectedNoSurfaceEffect),
                Map.entry("rejectedNoRouteEffect", rejectedNoRouteEffect),
                Map.entry("rejectedNoSampleMetrics", rejectedNoSampleMetrics),
                Map.entry("passed", passed),
                Map.entry("adapterCoreBridge", true),
                Map.entry("serviceCodeExecuted", true)
        );
    }

    private static Map<String, Object> event(
            int sequence,
            int pollIteration,
            int pollKeySamples,
            String key,
            String surface,
            boolean effectAccepted
    ) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("handled", true);
        event.put("physicalPoller", true);
        event.put("serviceCodeExecuted", true);
        event.put("physicalEventSequence", sequence);
        event.put("pollIteration", pollIteration);
        event.put("pollKeySamples", pollKeySamples);
        event.put("key", key);
        event.put("surface", surface);
        event.put("routeEffectAccepted", effectAccepted);
        event.put("hudOverlay", false);
        event.put("liveSurfaceAccepted", effectAccepted);
        event.put("liveSurfaceRendered", effectAccepted);
        event.put("physicalInputAccepted", effectAccepted);
        event.put("screenOwnershipAccepted", effectAccepted);
        event.put("renderCallbackAccepted", effectAccepted);
        return Map.copyOf(event);
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(EchoAgent5LivePhysicalRouteEffectTranscriptAcceptance::text).toList();
        }
        return List.of();
    }

    private static int integer(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(text(value));
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
