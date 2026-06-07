package dev.echo.standalone.runtime.ui;

import java.util.Objects;

public record EchoUiContext(
        EchoUiScreenStack screenStack,
        EchoUiModalStack modalStack,
        EchoUiThemeRuntime themeRuntime
) {
    public EchoUiContext {
        Objects.requireNonNull(screenStack, "screenStack");
        Objects.requireNonNull(modalStack, "modalStack");
        Objects.requireNonNull(themeRuntime, "themeRuntime");
    }

    public EchoUiTheme theme() {
        return themeRuntime.activeTheme();
    }
}
