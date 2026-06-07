package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveHoloMapWikiNavigationAcceptance {
    private EchoAgent5LiveHoloMapWikiNavigationAcceptance() {
    }

    public static Map<String, Object> assess(
            Map<String, Object> holoMapEndToEndAcceptance,
            Map<String, Object> wikiEndToEndAcceptance
    ) {
        Map<String, Object> holomap = holoMapEndToEndAcceptance == null ? Map.of() : holoMapEndToEndAcceptance;
        Map<String, Object> wiki = wikiEndToEndAcceptance == null ? Map.of() : wikiEndToEndAcceptance;
        EchoAgent5UiDataSources source = EchoAgent5UiDataSources.reference();
        Map<String, Object> holomapValues = source.holomapValues();
        Map<String, Object> wikiValues = source.wikiValues();
        boolean holomapAccepted = Boolean.TRUE.equals(holomap.get("accepted"))
                && ("holomap_end_to_end:J->HOLOMAP:" + holomapValues.get("marker")).equals(holomap.get("effect"))
                && "J".equals(holomap.get("key"))
                && "HOLOMAP".equals(holomap.get("surface"))
                && holomapValues.get("layer").equals(holomap.get("layer"))
                && holomapValues.get("marker").equals(holomap.get("marker"))
                && Boolean.TRUE.equals(holomap.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(holomap.get("renderAccepted"))
                && Boolean.TRUE.equals(holomap.get("interactionAccepted"))
                && Boolean.TRUE.equals(holomap.get("holomapRendered"));
        boolean wikiAccepted = Boolean.TRUE.equals(wiki.get("accepted"))
                && ("wiki_end_to_end:direct:WIKI:" + wikiValues.get("page")).equals(wiki.get("effect"))
                && "DIRECT".equals(wiki.get("key"))
                && "WIKI".equals(wiki.get("surface"))
                && wikiValues.get("guide").equals(wiki.get("guide"))
                && wikiValues.get("page").equals(wiki.get("page"))
                && wikiValues.get("link").equals(wiki.get("link"))
                && Boolean.TRUE.equals(wiki.get("physicalInputAccepted"))
                && Boolean.TRUE.equals(wiki.get("renderAccepted"))
                && Boolean.TRUE.equals(wiki.get("interactionAccepted"))
                && Boolean.TRUE.equals(wiki.get("wikiRendered"));
        boolean accepted = holomapAccepted && wikiAccepted;

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("accepted", accepted);
        result.put("holomapAccepted", holomapAccepted);
        result.put("wikiAccepted", wikiAccepted);
        result.put("holomapSurface", String.valueOf(holomap.getOrDefault("surface", "")));
        result.put("wikiSurface", String.valueOf(wiki.getOrDefault("surface", "")));
        result.put("layer", String.valueOf(holomap.getOrDefault("layer", "")));
        result.put("marker", String.valueOf(holomap.getOrDefault("marker", "")));
        result.put("guide", String.valueOf(wiki.getOrDefault("guide", "")));
        result.put("page", String.valueOf(wiki.getOrDefault("page", "")));
        result.put("effect", accepted
                ? "live_holomap_wiki_navigation:accepted:J/direct"
                : "live_holomap_wiki_navigation:rejected");
        result.put("adapterCoreBridge", true);
        result.put("serviceCodeExecuted", true);
        return Map.copyOf(result);
    }
}
