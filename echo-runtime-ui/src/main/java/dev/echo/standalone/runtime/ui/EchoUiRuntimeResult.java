package dev.echo.standalone.runtime.ui;

import java.util.Objects;

public record EchoUiRuntimeResult(
        EchoUiScreenStack screenStack,
        EchoUiModalStack modalStack,
        EchoUiInputRouter inputRouter,
        EchoUiThemeRuntime themeRuntime
) {
    public EchoUiRuntimeResult {
        Objects.requireNonNull(screenStack, "screenStack");
        Objects.requireNonNull(modalStack, "modalStack");
        Objects.requireNonNull(inputRouter, "inputRouter");
        Objects.requireNonNull(themeRuntime, "themeRuntime");
    }

    public EchoUiInputResult dispatch(EchoUiInputEvent event) {
        return inputRouter.route(event);
    }

    public EchoUiFrame frame() {
        EchoUiContext context = new EchoUiContext(screenStack, modalStack, themeRuntime);
        EchoUiSurface screen = screenStack.current()
                .orElseThrow(() -> new IllegalStateException("Cannot render UI without a screen"))
                .render(context);
        return new EchoUiFrame(
                screen,
                modalStack.snapshot().stream()
                        .map(modal -> modal.render(context))
                        .toList(),
                themeRuntime.activeTheme()
        );
    }
}
