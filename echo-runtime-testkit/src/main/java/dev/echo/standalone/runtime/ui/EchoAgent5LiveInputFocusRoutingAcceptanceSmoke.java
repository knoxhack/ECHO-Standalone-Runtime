package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveInputFocusRoutingAcceptanceSmoke {
    private EchoAgent5LiveInputFocusRoutingAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> focus = EchoAgent5FocusManagerSmoke.capture(source);
        Map<String, Object> editing = EchoAgent5TextEditingSmoke.capture(source);
        Map<String, Object> mouse = EchoAgent5MouseActivationSmoke.capture(source);
        Map<String, Object> list = EchoAgent5ListNavigationSmoke.capture(source);
        Map<String, Object> accepted = EchoAgent5LiveInputFocusRoutingAcceptance.assess(
                focus,
                editing,
                mouse,
                list
        );
        Map<String, Object> rejectedNoFocus = EchoAgent5LiveInputFocusRoutingAcceptance.assess(
                Map.of("passed", false),
                editing,
                mouse,
                list
        );
        Map<String, Object> rejectedNoEditing = EchoAgent5LiveInputFocusRoutingAcceptance.assess(
                focus,
                Map.of("passed", false),
                mouse,
                list
        );
        Map<String, Object> rejectedNoMouse = EchoAgent5LiveInputFocusRoutingAcceptance.assess(
                focus,
                editing,
                Map.of("passed", false),
                list
        );
        Map<String, Object> rejectedNoList = EchoAgent5LiveInputFocusRoutingAcceptance.assess(
                focus,
                editing,
                mouse,
                Map.of("passed", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_input_focus_routing:accepted:focus/text/mouse/list".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoFocus.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoEditing.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoMouse.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoList.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveInputFocusRoutingAcceptanceSmokeClass",
                EchoAgent5LiveInputFocusRoutingAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoFocus", rejectedNoFocus);
        smoke.put("rejectedNoEditing", rejectedNoEditing);
        smoke.put("rejectedNoMouse", rejectedNoMouse);
        smoke.put("rejectedNoList", rejectedNoList);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
