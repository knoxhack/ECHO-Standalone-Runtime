package dev.echo.standalone.runtime.ui;

import java.util.Objects;
import java.util.Optional;

public final class EchoUiInputRouter {
    private final EchoUiScreenStack screenStack;
    private final EchoUiModalStack modalStack;
    private final EchoUiThemeRuntime themeRuntime;

    public EchoUiInputRouter(
            EchoUiScreenStack screenStack,
            EchoUiModalStack modalStack,
            EchoUiThemeRuntime themeRuntime
    ) {
        this.screenStack = Objects.requireNonNull(screenStack, "screenStack");
        this.modalStack = Objects.requireNonNull(modalStack, "modalStack");
        this.themeRuntime = Objects.requireNonNull(themeRuntime, "themeRuntime");
    }

    public EchoUiInputResult route(EchoUiInputEvent event) {
        Objects.requireNonNull(event, "event");
        EchoUiContext context = new EchoUiContext(screenStack, modalStack, themeRuntime);
        Optional<EchoUiModal> modal = modalStack.top();
        if (modal.isPresent()) {
            EchoUiInputResult result = modal.get().handleInput(event, context);
            if (result.closeTopModal()) {
                modalStack.closeTop();
            }
            return result;
        }

        Optional<EchoUiScreen> screen = screenStack.current();
        if (screen.isEmpty()) {
            return EchoUiInputResult.ignored("ui-router");
        }
        return screen.get().handleInput(event, context);
    }
}
