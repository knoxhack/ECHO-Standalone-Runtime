package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5PhysicalHotkeyPoller {
    private static final List<String> ORDER = List.of(
            "M",
            "G",
            "R",
            "U",
            "B",
            "LEFT_ALT",
            "RIGHT_ALT",
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

    private EchoAgent5PhysicalHotkeyPoller() {
    }

    public static Map<String, Boolean> emptyState() {
        Map<String, Boolean> state = new LinkedHashMap<>();
        for (String key : ORDER) {
            state.put(key, false);
        }
        return Map.copyOf(state);
    }

    public static Map<String, Object> poll(Map<String, Boolean> previous, Map<String, Boolean> current) {
        Map<String, Boolean> prev = previous == null ? emptyState() : previous;
        Map<String, Boolean> now = current == null ? emptyState() : current;
        for (String key : ORDER) {
            if (Boolean.TRUE.equals(now.get(key)) && !Boolean.TRUE.equals(prev.get(key))) {
                String surface = surfaceFor(key);
                String action = actionFor(key);
                Map<String, Object> event = new LinkedHashMap<>();
                event.put("handled", true);
                event.put("physicalPoller", true);
                event.put("serviceCodeExecuted", true);
                event.put("key", key);
                event.put("surface", surface);
                event.put("action", action);
                event.put("hudOverlay", false);
                if ("B".equals(key)) {
                    event.put("contextual", true);
                    event.put("alternateSurface", "ASHFALL_DRONE");
                    event.put("alternateAction", "ashfall.drone_assist");
                    event.put("sourceConflict", "echoindex.bookmark/echoashfallprotocol.drone_assist");
                }
                event.put("effect", "physical_hotkey:" + key + "->" + surface + ":" + action);
                return Map.copyOf(event);
            }
        }
        return Map.of(
                "handled", false,
                "physicalPoller", true,
                "serviceCodeExecuted", true,
                "effect", "physical_hotkey:none"
        );
    }

    private static String surfaceFor(String key) {
        return switch (key) {
            case "M" -> "TERMINAL";
            case "G", "R", "U", "B" -> "INDEX";
            case "LEFT_ALT", "RIGHT_ALT" -> "LENS";
            case "J", "K", "RIGHT_BRACKET", "LEFT_BRACKET", "BACKSLASH" -> "HOLOMAP";
            case "N" -> "SIGNALOS";
            case "ESCAPE" -> "PAUSE";
            case "X", "C", "Y", "Z" -> "ASHFALL_DRONE";
            default -> "";
        };
    }

    private static String actionFor(String key) {
        return switch (key) {
            case "M" -> "terminal.open";
            case "G" -> "index.catalog";
            case "R" -> "index.recipe";
            case "U" -> "index.usage";
            case "B" -> "index.bookmark";
            case "LEFT_ALT", "RIGHT_ALT" -> "lens.deep_scan";
            case "J" -> "holomap.open";
            case "K" -> "holomap.toggle_minimap";
            case "RIGHT_BRACKET" -> "holomap.zoom_in";
            case "LEFT_BRACKET" -> "holomap.zoom_out";
            case "BACKSLASH" -> "holomap.cycle_corner";
            case "N" -> "signalos.terminal";
            case "ESCAPE" -> "pause.toggle";
            case "X" -> "ashfall.drone_recall";
            case "C" -> "ashfall.drone_scan";
            case "Y" -> "ashfall.drone_scout";
            case "Z" -> "ashfall.drone_status";
            default -> "";
        };
    }
}
