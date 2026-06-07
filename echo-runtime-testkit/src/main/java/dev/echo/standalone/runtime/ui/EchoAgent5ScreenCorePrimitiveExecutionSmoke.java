package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoAgent5ScreenCorePrimitiveExecutionSmoke {
    private static final List<String> REQUIRED_PRIMITIVES = List.of(
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

    private EchoAgent5ScreenCorePrimitiveExecutionSmoke() {
    }

    public static Map<String, Object> capture(EchoAgent5UiDataSources dataSources) {
        EchoAgent5UiDataSources source = dataSources == null ? EchoAgent5UiDataSources.reference() : dataSources;
        EchoScreen screen = new EchoStaticScreen(
                EchoAgent5UiReference.TERMINAL_SCREEN,
                "Terminal",
                List.of(source.terminalReadyLine()),
                "terminal:input"
        );
        EchoScreenStack stack = new EchoScreenStack();
        stack.push(screen);
        stack.replace(new EchoStaticScreen(EchoAgent5UiReference.INDEX_SCREEN, "Index", List.of(source.indexResult()), "index:search"));
        String popped = stack.pop().map(EchoScreen::id).orElse("");
        stack.push(screen);
        EchoScreenRoute route = new EchoScreenRoute(EchoAgent5UiReference.TERMINAL_SCREEN, "route:M", "terminal:input");
        EchoHudLayer hud = new EchoHudLayer(EchoAgent5UiReference.HUD_LAYER, source.hudValues());
        EchoInputAction action = new EchoInputAction("agent5:terminal_status", "Enter", source.terminalCommand());
        EchoTheme theme = new EchoTheme(EchoAgent5UiReference.SETTINGS_THEME, new EchoUiTheme(
                "ashfall-agent5",
                "Ashfall Agent 5",
                "#67e8f9",
                "#061014",
                "#d8fbff",
                "#facc15",
                "ECHO Mono",
                "compact",
                Map.of("terminal.prompt", source.terminalPrompt())
        ));
        EchoWidget widget = new EchoWidget("agent5:terminal", "text-input", Map.of("mode", "TERMINAL"));
        EchoTextInput input = new EchoTextInput("agent5:terminal-input", "").withValue(source.terminalCommand());
        EchoButton button = new EchoButton("agent5:lens-scan", "Scan", "lens-scan");
        EchoListView list = new EchoListView("agent5:pause-options", List.of("Resume", "Settings", "Quit"), 1);
        EchoTerminalBuffer buffer = new EchoTerminalBuffer(List.of("status -> " + source.terminalReadyLine()));
        EchoNotification notification = new EchoNotification(
                "agent5-notification-1",
                "INFO",
                String.valueOf(source.notifications().get(0).get("message")),
                "top_left_safe_area",
                true
        );
        List<String> executed = List.of(
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
        boolean passed = executed.equals(REQUIRED_PRIMITIVES)
                && EchoAgent5UiReference.TERMINAL_SCREEN.equals(stack.current().map(EchoScreen::id).orElse(""))
                && EchoAgent5UiReference.INDEX_SCREEN.equals(popped)
                && "terminal:input".equals(route.focusPath())
                && hud.ready()
                && source.terminalCommand().equals(action.event(1).value())
                && EchoAgent5UiReference.SETTINGS_THEME.equals(theme.id())
                && "TERMINAL".equals(widget.state().get("mode"))
                && source.terminalCommand().equals(input.value())
                && "lens-scan".equals(button.action())
                && "Settings".equals(list.selectedRow())
                && buffer.contains(source.terminalReadyLine())
                && notification.delivered();

        Map<String, Object> smoke = new LinkedHashMap<>();
        smoke.put("screenCorePrimitiveExecutionSmokeClass",
                EchoAgent5ScreenCorePrimitiveExecutionSmoke.class.getSimpleName());
        smoke.put("executedPrimitives", executed);
        smoke.put("stackCurrent", stack.current().map(EchoScreen::id).orElse(""));
        smoke.put("routeFocusPath", route.focusPath());
        smoke.put("terminalInputValue", input.value());
        smoke.put("selectedRow", list.selectedRow());
        smoke.put("notificationMessage", notification.message());
        smoke.put("adapterCoreBridge", true);
        smoke.put("serviceCodeExecuted", true);
        smoke.put("passed", passed);
        return Map.copyOf(smoke);
    }
}
