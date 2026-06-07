package dev.echo.standalone.runtime.ui;

import java.util.Map;

public final class EchoAgent5LiveClientAttachmentAcceptanceSmoke {
    private EchoAgent5LiveClientAttachmentAcceptanceSmoke() {
    }

    public static Map<String, Object> capture() {
        Map<String, Object> accepted = EchoAgent5LiveClientAttachmentAcceptance.assess(
                true,
                true,
                true,
                9442L,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        Map<String, Object> rejectedNoClient = EchoAgent5LiveClientAttachmentAcceptance.assess(
                false,
                true,
                true,
                9442L,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        Map<String, Object> rejectedNoScreen = EchoAgent5LiveClientAttachmentAcceptance.assess(
                true,
                false,
                true,
                9442L,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        Map<String, Object> rejectedNoClientThread = EchoAgent5LiveClientAttachmentAcceptance.assess(
                true,
                true,
                false,
                9442L,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        Map<String, Object> rejectedNoWindow = EchoAgent5LiveClientAttachmentAcceptance.assess(
                true,
                true,
                true,
                0L,
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        Map<String, Object> rejectedScreenMismatch = EchoAgent5LiveClientAttachmentAcceptance.assess(
                true,
                true,
                true,
                9442L,
                "dev.echo.standalone.runtime.ui.OtherScreen",
                "dev.echo.standalone.runtime.ui.EchoAgent5UiScreenHost"
        );
        boolean passed = Boolean.TRUE.equals(accepted.get("accepted"))
                && "live_client_attachment:accepted:EchoAgent5UiScreenHost".equals(accepted.get("effect"))
                && Boolean.FALSE.equals(rejectedNoClient.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoScreen.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoClientThread.get("accepted"))
                && Boolean.FALSE.equals(rejectedNoWindow.get("accepted"))
                && Boolean.FALSE.equals(rejectedScreenMismatch.get("accepted"));
        return Map.of(
                "liveClientAttachmentAcceptanceSmokeClass",
                EchoAgent5LiveClientAttachmentAcceptanceSmoke.class.getSimpleName(),
                "accepted", accepted,
                "rejectedNoClient", rejectedNoClient,
                "rejectedNoScreen", rejectedNoScreen,
                "rejectedNoClientThread", rejectedNoClientThread,
                "rejectedNoWindow", rejectedNoWindow,
                "rejectedScreenMismatch", rejectedScreenMismatch,
                "adapterCoreBridge", true,
                "serviceCodeExecuted", true,
                "passed", passed
        );
    }
}
