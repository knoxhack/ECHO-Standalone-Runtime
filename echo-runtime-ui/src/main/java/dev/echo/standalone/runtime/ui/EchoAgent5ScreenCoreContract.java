package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Map;

public record EchoAgent5ScreenCoreContract(
        List<String> primitives,
        Map<String, String> runtimeBindings
) {
    public static final List<String> REQUIRED_PRIMITIVES = List.of(
            "EchoScreen",
            "EchoScreenStack",
            "EchoScreenRoute",
            "EchoHudLayer",
            "EchoInputAction",
            "EchoTheme",
            "EchoWidget",
            "EchoTextInput",
            "EchoButton",
            "EchoListView",
            "EchoTerminalBuffer",
            "EchoNotification"
    );

    public EchoAgent5ScreenCoreContract {
        primitives = List.copyOf(primitives);
        runtimeBindings = Map.copyOf(runtimeBindings);
    }

    public static EchoAgent5ScreenCoreContract runtime() {
        return new EchoAgent5ScreenCoreContract(REQUIRED_PRIMITIVES, Map.ofEntries(
                Map.entry("EchoScreen", EchoScreen.class.getName()),
                Map.entry("EchoScreenStack", EchoScreenStack.class.getName()),
                Map.entry("EchoScreenRoute", EchoScreenRoute.class.getName()),
                Map.entry("EchoHudLayer", EchoHudLayer.class.getName()),
                Map.entry("EchoInputAction", EchoInputAction.class.getName()),
                Map.entry("EchoTheme", EchoTheme.class.getName()),
                Map.entry("EchoWidget", EchoWidget.class.getName()),
                Map.entry("EchoTextInput", EchoTextInput.class.getName()),
                Map.entry("EchoButton", EchoButton.class.getName()),
                Map.entry("EchoListView", EchoListView.class.getName()),
                Map.entry("EchoTerminalBuffer", EchoTerminalBuffer.class.getName()),
                Map.entry("EchoNotification", EchoNotification.class.getName())
        ));
    }

    public boolean satisfied() {
        return primitives.containsAll(REQUIRED_PRIMITIVES)
                && REQUIRED_PRIMITIVES.stream().allMatch(runtimeBindings::containsKey);
    }
}
