package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EchoAgent5LivePhysicalEventTranscriptAcceptance {
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

    private EchoAgent5LivePhysicalEventTranscriptAcceptance() {
    }

    public static Map<String, Object> assess(Object observedEvents) {
        List<?> events = observedEvents instanceof List<?> list ? list : List.of();
        Set<String> observedKeys = new LinkedHashSet<>();
        List<String> routedSurfaces = new ArrayList<>();
        List<String> rejectedEvents = new ArrayList<>();
        int previousSequence = 0;
        int previousPollIteration = 0;
        boolean sequenceOrdered = true;
        boolean pollMetricsPresent = true;
        for (Object event : events) {
            if (!(event instanceof Map<?, ?> map)) {
                rejectedEvents.add("not_a_map");
                sequenceOrdered = false;
                pollMetricsPresent = false;
                continue;
            }
            String key = text(map.get("key"));
            String surface = text(map.get("surface"));
            int sequence = integer(map.get("physicalEventSequence"));
            int pollIteration = integer(map.get("pollIteration"));
            int pollKeySamples = integer(map.get("pollKeySamples"));
            boolean ordered = sequence > previousSequence;
            boolean hasPollMetrics = pollIteration > 0
                    && pollIteration >= previousPollIteration
                    && pollKeySamples >= REQUIRED_KEYS.size();
            boolean validRoute = Boolean.TRUE.equals(map.get("handled"))
                    && Boolean.TRUE.equals(map.get("physicalPoller"))
                    && Boolean.TRUE.equals(map.get("serviceCodeExecuted"))
                    && REQUIRED.containsKey(key)
                    && REQUIRED.get(key).equals(surface);
            if (ordered && hasPollMetrics && validRoute) {
                observedKeys.add(key);
                routedSurfaces.add(surface);
            } else {
                rejectedEvents.add(key + "->" + surface);
            }
            sequenceOrdered = sequenceOrdered && ordered;
            pollMetricsPresent = pollMetricsPresent && hasPollMetrics;
            previousSequence = sequence;
            previousPollIteration = pollIteration;
        }
        List<String> missingKeys = REQUIRED_KEYS.stream()
                .filter(key -> !observedKeys.contains(key))
                .toList();
        boolean accepted = missingKeys.isEmpty()
                && rejectedEvents.isEmpty()
                && sequenceOrdered
                && pollMetricsPresent;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("requiredKeys", REQUIRED_KEYS);
        result.put("observedKeys", List.copyOf(observedKeys));
        result.put("routedSurfaces", List.copyOf(routedSurfaces));
        result.put("missingKeys", missingKeys);
        result.put("rejectedEvents", rejectedEvents);
        result.put("eventCount", events.size());
        result.put("sequenceOrdered", sequenceOrdered);
        result.put("pollMetricsPresent", pollMetricsPresent);
        result.put("effect", accepted
                ? "live_physical_event_transcript:accepted:" + observedKeys.size()
                : "live_physical_event_transcript:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        List<Map<String, Object>> acceptedEvents = new ArrayList<>();
        for (int i = 0; i < REQUIRED_KEYS.size(); i++) {
            String key = REQUIRED_KEYS.get(i);
            acceptedEvents.add(event(i + 1, i + 3, 51 + (i * 17), key, REQUIRED.get(key)));
        }
        Map<String, Object> accepted = assess(acceptedEvents);
        List<Map<String, Object>> missingSequenceEvents = new ArrayList<>(acceptedEvents);
        missingSequenceEvents.set(2, without(missingSequenceEvents.get(2), "physicalEventSequence"));
        Map<String, Object> rejectedMissingSequence = assess(missingSequenceEvents);
        List<Map<String, Object>> unorderedEvents = new ArrayList<>(acceptedEvents);
        unorderedEvents.set(1, event(1, 4, 68, "G", "INDEX"));
        Map<String, Object> rejectedUnordered = assess(unorderedEvents);
        List<Map<String, Object>> noPollMetricsEvents = new ArrayList<>(acceptedEvents);
        noPollMetricsEvents.set(4, without(noPollMetricsEvents.get(4), "pollIteration"));
        Map<String, Object> rejectedNoPollMetrics = assess(noPollMetricsEvents);
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_physical_event_transcript:accepted:17".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedMissingSequence.get("accepted"))
                && Boolean.FALSE.equals(rejectedMissingSequence.get("sequenceOrdered"))
                && Boolean.FALSE.equals(rejectedUnordered.get("accepted"))
                && Boolean.FALSE.equals(rejectedUnordered.get("sequenceOrdered"))
                && Boolean.FALSE.equals(rejectedNoPollMetrics.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoPollMetrics.get("pollMetricsPresent"));
        return Map.ofEntries(
                Map.entry("livePhysicalEventTranscriptAcceptanceClass",
                        EchoAgent5LivePhysicalEventTranscriptAcceptance.class.getSimpleName()),
                Map.entry("accepted", accepted),
                Map.entry("rejectedMissingSequence", rejectedMissingSequence),
                Map.entry("rejectedUnordered", rejectedUnordered),
                Map.entry("rejectedNoPollMetrics", rejectedNoPollMetrics),
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
            String surface
    ) {
        return Map.of(
                "handled", true,
                "physicalPoller", true,
                "serviceCodeExecuted", true,
                "physicalEventSequence", sequence,
                "pollIteration", pollIteration,
                "pollKeySamples", pollKeySamples,
                "key", key,
                "surface", surface
        );
    }

    private static Map<String, Object> without(Map<String, Object> source, String key) {
        Map<String, Object> copy = new LinkedHashMap<>(source);
        copy.remove(key);
        return Map.copyOf(copy);
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
