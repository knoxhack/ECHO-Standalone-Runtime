package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5LiveRouteBoundHoloMapWikiAcceptance {
    private EchoAgent5LiveRouteBoundHoloMapWikiAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> holomap,
            Map<String, Object> wiki,
            Map<String, Object> routeEffectTranscript
    ) {
        boolean holomapAccepted = acceptedHoloMap(holomap);
        boolean wikiAccepted = acceptedWiki(wiki);
        List<String> observedKeys = strings(routeEffectTranscript == null
                ? null
                : routeEffectTranscript.get("observedKeys"));
        boolean routeBound = routeEffectTranscript != null
                && Boolean.TRUE.equals(routeEffectTranscript.get("accepted"))
                && observedKeys.contains("J");
        boolean accepted = holomapAccepted && wikiAccepted && routeBound;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("holomapAccepted", holomapAccepted);
        result.put("wikiAccepted", wikiAccepted);
        result.put("routeBound", routeBound);
        result.put("observedKeys", observedKeys);
        result.put("holomapKey", text(holomap == null ? null : holomap.get("key")));
        result.put("holomapSurface", text(holomap == null ? null : holomap.get("surface")));
        result.put("wikiKey", text(wiki == null ? null : wiki.get("key")));
        result.put("wikiSurface", text(wiki == null ? null : wiki.get("surface")));
        result.put("layer", text(holomap == null ? null : holomap.get("layer")));
        result.put("marker", text(holomap == null ? null : holomap.get("marker")));
        result.put("guide", text(wiki == null ? null : wiki.get("guide")));
        result.put("page", text(wiki == null ? null : wiki.get("page")));
        result.put("link", text(wiki == null ? null : wiki.get("link")));
        result.put("effect", accepted
                ? "live_route_bound_holomap_wiki:accepted:J/direct"
                : "live_route_bound_holomap_wiki:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }

    public static Map<String, Object> smoke() {
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> holomap = object(EchoAgent5HoloMapEndToEndAcceptanceSmoke.capture(source).get("accepted"));
        Map<String, Object> wiki = object(EchoAgent5WikiEndToEndAcceptanceSmoke.capture(source).get("accepted"));
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
        Map<String, Object> accepted = assess(holomap, wiki, route);
        Map<String, Object> rejectedNoHoloMap = assess(Map.of(), wiki, route);
        Map<String, Object> rejectedNoWiki = assess(holomap, Map.of(), route);
        Map<String, Object> rejectedNoRoute = assess(holomap, wiki, Map.of(
                "accepted", true,
                "observedKeys", List.of("M", "G", "LEFT_ALT", "N")
        ));
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_route_bound_holomap_wiki:accepted:J/direct".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoHoloMap.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoWiki.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoRoute.get("accepted"));
        return Map.of(
                "liveRouteBoundHoloMapWikiAcceptanceClass",
                EchoAgent5LiveRouteBoundHoloMapWikiAcceptance.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNoHoloMap", rejectedNoHoloMap,
                "rejectedNoWiki", rejectedNoWiki,
                "rejectedNoRoute", rejectedNoRoute,
                "passed", passed,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true
        );
    }

    private static boolean acceptedHoloMap(Map<String, Object> holomap) {
        Map<String, Object> expected = EchoAgent5UiDataSources.reference().holomapValues();
        return holomap != null
                && Boolean.TRUE.equals(holomap.get("accepted"))
                && "J".equals(holomap.get("key"))
                && "HOLOMAP".equals(holomap.get("surface"))
                && expected.get("layer").equals(holomap.get("layer"))
                && expected.get("marker").equals(holomap.get("marker"))
                && Boolean.TRUE.equals(holomap.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(holomap.get("renderAccepted"))
                && Boolean.TRUE.equals(holomap.get("interactionAccepted"))
                && Boolean.TRUE.equals(holomap.get("holomapRendered"))
                && ("holomap_end_to_end:J->HOLOMAP:" + expected.get("marker")).equals(holomap.get("effect"));
    }

    private static boolean acceptedWiki(Map<String, Object> wiki) {
        Map<String, Object> expected = EchoAgent5UiDataSources.reference().wikiValues();
        return wiki != null
                && Boolean.TRUE.equals(wiki.get("accepted"))
                && "DIRECT".equals(wiki.get("key"))
                && "WIKI".equals(wiki.get("surface"))
                && expected.get("guide").equals(wiki.get("guide"))
                && expected.get("page").equals(wiki.get("page"))
                && expected.get("link").equals(wiki.get("link"))
                && Boolean.TRUE.equals(wiki.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(wiki.get("renderAccepted"))
                && Boolean.TRUE.equals(wiki.get("interactionAccepted"))
                && Boolean.TRUE.equals(wiki.get("wikiRendered"))
                && ("wiki_end_to_end:direct:WIKI:" + expected.get("page")).equals(wiki.get("effect"));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private static List<String> strings(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(EchoAgent5LiveRouteBoundHoloMapWikiAcceptance::text).toList();
        }
        return List.of();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
