package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveRouteBoundLensScanAcceptance {
    private EchoAgent5LiveRouteBoundLensScanAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> lens,
            Map<String, Object> routeEffectTranscript
    ) {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        boolean lensAccepted = acceptedLens(lens, source);
        List<String> observedKeys = strings(routeEffectTranscript == null
                ? null
                : routeEffectTranscript.get("observedKeys"));
        boolean routeBound = routeEffectTranscript != null
                && Boolean.TRUE.equals(routeEffectTranscript.get("accepted"))
                && observedKeys.contains("LEFT_ALT");
        boolean accepted = lensAccepted && routeBound;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("lensAccepted", lensAccepted);
        result.put("routeBound", routeBound);
        result.put("observedKeys", observedKeys);
        result.put("key", text(lens == null ? null : lens.get("key")));
        result.put("surface", text(lens == null ? null : lens.get("surface")));
        result.put("target", source.lensTarget());
        result.put("result", source.lensResult());
        result.put("effect", accepted
                ? "live_route_bound_lens_scan:accepted:LEFT_ALT->LENS"
                : "live_route_bound_lens_scan:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> lens = lensSnapshot(source);
        Map<String, Object> route = Map.of(
                "accepted", true,
                "observedKeys", List.of(
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
                )
        );
        Map<String, Object> accepted = assess(lens, route);
        Map<String, Object> rejectedNoLens = assess(Map.of(), route);
        Map<String, Object> rejectedNoRoute = assess(lens, Map.of(
                "accepted", true,
                "observedKeys", List.of("M", "G", "N")
        ));
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_route_bound_lens_scan:accepted:LEFT_ALT->LENS".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoLens.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRoute.get("accepted"));
        return Map.of(
                "liveRouteBoundLensScanAcceptanceClass",
                EchoAgent5LiveRouteBoundLensScanAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNoLens", rejectedNoLens,
                "rejectedNoRoute", rejectedNoRoute,
                "passed", passed,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static boolean acceptedLens(Map<String, Object> lens, EchoAgent5UiDataSources source) {
        return lens != null
                && Boolean.TRUE.equals(lens.get("accepted"))
                && "LEFT_ALT".equals(lens.get("key"))
                && "LENS".equals(lens.get("surface"))
                && source.lensTarget().equals(lens.get("target"))
                && Boolean.TRUE.equals(lens.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(lens.get("renderAccepted"))
                && Boolean.TRUE.equals(lens.get("focusAccepted"))
                && Boolean.TRUE.equals(lens.get("transcriptAccepted"))
                && Boolean.TRUE.equals(lens.get("scanExecuted"))
                && Boolean.TRUE.equals(lens.get("lensRendered"))
                && ("lens_end_to_end:LEFT_ALT->LENS:" + source.lensTarget()).equals(lens.get("effect"));
    }

    private static Map<String, Object> lensSnapshot(EchoAgent5UiDataSources source) {
        return Map.ofEntries(
                Map.entry("accepted", true),
                Map.entry("key", "LEFT_ALT"),
                Map.entry("surface", "LENS"),
                Map.entry("target", source.lensTarget()),
                Map.entry("result", source.lensResult()),
                Map.entry("physicalInputAccepted", true),
                Map.entry("renderAccepted", true),
                Map.entry("focusAccepted", true),
                Map.entry("transcriptAccepted", true),
                Map.entry("scanExecuted", true),
                Map.entry("lensRendered", true),
                Map.entry("effect", "lens_end_to_end:LEFT_ALT->LENS:" + source.lensTarget())
        );
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(EchoAgent5LiveRouteBoundLensScanAcceptance::text).toList();
        }
        return List.of();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
