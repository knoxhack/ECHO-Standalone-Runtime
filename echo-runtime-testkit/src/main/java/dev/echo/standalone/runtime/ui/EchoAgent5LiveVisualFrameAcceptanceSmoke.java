package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5LiveVisualFrameAcceptanceSmoke {
    private static final String SCREEN_CLASS = "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost";

    private EchoAgent5LiveVisualFrameAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> theme = EchoAgent5ThemeApplicationSmoke.capture(
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> layout = EchoAgent5RenderCoreLayoutSmoke.capture();
        Map<String, Object> camera = EchoAgent5CameraCinematicSmoke.capture(source);
        Map<String, Object> hud = EchoAgent5HudOverlaySmoke.capture(
                true,
                true,
                "hud:update",
                SCREEN_CLASS,
                "ashfall",
                12,
                3,
                2,
                1,
                source
        );
        Map<String, Object> accepted = EchoAgent5LiveVisualFrameAcceptance.assess(
                theme,
                layout,
                camera,
                hud
        );
        Map<String, Object> rejectedNoTheme = EchoAgent5LiveVisualFrameAcceptance.assess(
                Map.of("passed", false),
                layout,
                camera,
                hud
        );
        Map<String, Object> rejectedNoLayout = EchoAgent5LiveVisualFrameAcceptance.assess(
                theme,
                Map.of("passed", false),
                camera,
                hud
        );
        Map<String, Object> rejectedNoCamera = EchoAgent5LiveVisualFrameAcceptance.assess(
                theme,
                layout,
                Map.of("passed", false),
                hud
        );
        Map<String, Object> rejectedNoHud = EchoAgent5LiveVisualFrameAcceptance.assess(
                theme,
                layout,
                camera,
                Map.of("passed", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_visual_frame:accepted:theme/render/camera/hud".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoTheme.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoLayout.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoCamera.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoHud.get("accepted"));

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("liveVisualFrameAcceptanceSmokeClass",
                EchoAgent5LiveVisualFrameAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoTheme", rejectedNoTheme);
        smoke.put("rejectedNoLayout", rejectedNoLayout);
        smoke.put("rejectedNoCamera", rejectedNoCamera);
        smoke.put("rejectedNoHud", rejectedNoHud);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
