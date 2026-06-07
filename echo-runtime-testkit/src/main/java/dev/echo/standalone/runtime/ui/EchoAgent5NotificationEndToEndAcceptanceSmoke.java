package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.Map;

public final class EchoAgent5NotificationEndToEndAcceptanceSmoke {
    private EchoAgent5NotificationEndToEndAcceptanceSmoke() {
    }

    public static Map<String, Object> capture(
            String packId,
            int moduleCount,
            int itemCount,
            int missionCount,
            int regionCount,
            EchoAgent5UiDataSources dataSources
    ) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        Map<String, Object> queue = EchoAgent5NotificationQueueSmoke.capture(
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        Map<String, Object> dismiss = EchoAgent5NotificationDismissSmoke.capture(
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        Map<String, Object> hud = EchoAgent5HudOverlaySmoke.capture(
                true,
                true,
                "H",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                packId,
                moduleCount,
                itemCount,
                missionCount,
                regionCount,
                source
        );
        Map<String, Object> accepted = EchoAgent5NotificationEndToEndAcceptance.assess(queue, dismiss, hud);
        Map<String, Object> rejectedNoQueue = EchoAgent5NotificationEndToEndAcceptance.assess(
                Map.of("passed", false),
                dismiss,
                hud
        );
        Map<String, Object> rejectedNoDismiss = EchoAgent5NotificationEndToEndAcceptance.assess(
                queue,
                Map.of("passed", false),
                hud
        );
        Map<String, Object> rejectedNoHud = EchoAgent5NotificationEndToEndAcceptance.assess(
                queue,
                dismiss,
                Map.of("passed", false)
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "notification_end_to_end:queue->hud:drop-oldest".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoQueue.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoDismiss.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoHud.get("accepted"));
        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("notificationEndToEndAcceptanceSmokeClass",
                EchoAgent5NotificationEndToEndAcceptanceSmoke.class.getSimpleName());
        smoke.put("accepted", accepted);
        smoke.put("rejectedNoQueue", rejectedNoQueue);
        smoke.put("rejectedNoDismiss", rejectedNoDismiss);
        smoke.put("rejectedNoHud", rejectedNoHud);
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
