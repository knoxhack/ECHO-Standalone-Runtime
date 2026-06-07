package dev.echo.standalone.runtime.ui;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.util.Objects;

public final class EchoUiRuntime {
    public EchoUiRuntimeResult boot(
            EchoRuntimeServiceRegistry services,
            EchoUiScreen initialScreen,
            EchoUiTheme activeTheme
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(initialScreen, "initialScreen");
        Objects.requireNonNull(activeTheme, "activeTheme");

        EchoUiScreenStack screenStack = new EchoUiScreenStack();
        EchoUiModalStack modalStack = new EchoUiModalStack();
        EchoUiThemeRuntime themeRuntime = new EchoUiThemeRuntime(activeTheme);
        EchoUiInputRouter inputRouter = new EchoUiInputRouter(screenStack, modalStack, themeRuntime);
        screenStack.push(initialScreen);

        EchoUiRuntimeResult result = new EchoUiRuntimeResult(screenStack, modalStack, inputRouter, themeRuntime);
        services.register(EchoUiRuntimeResult.class, result);
        services.register(EchoUiScreenStack.class, screenStack);
        services.register(EchoUiModalStack.class, modalStack);
        services.register(EchoUiInputRouter.class, inputRouter);
        services.register(EchoUiThemeRuntime.class, themeRuntime);
        return result;
    }
}
