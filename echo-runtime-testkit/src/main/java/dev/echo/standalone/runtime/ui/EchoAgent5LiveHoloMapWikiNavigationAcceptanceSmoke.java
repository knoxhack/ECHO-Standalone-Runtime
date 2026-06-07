package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveHoloMapWikiNavigationAcceptanceSmoke {
    private EchoAgent5LiveHoloMapWikiNavigationAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> holomap = object(EchoAgent5HoloMapEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> wiki = object(EchoAgent5WikiEndToEndAcceptanceSmoke.capture(source)
                .get("accepted"));
        Map<String, Object> accepted = EchoAgent5LiveHoloMapWikiNavigationAcceptance.assess(holomap, wiki);
        Map<String, Object> rejectedNoHoloMap = EchoAgent5LiveHoloMapWikiNavigationAcceptance.assess(
                Map.of("accepted", false),
                wiki
        );
        Map<String, Object> rejectedNoWiki = EchoAgent5LiveHoloMapWikiNavigationAcceptance.assess(
                holomap,
                Map.of("accepted", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_holomap_wiki_navigation:accepted:J/direct".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoHoloMap.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoWiki.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveHoloMapWikiNavigationAcceptanceSmokeClass",
                EchoAgent5LiveHoloMapWikiNavigationAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoHoloMap", rejectedNoHoloMap);
        smoke.put("rejectedNoWiki", rejectedNoWiki);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> object(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }
}
