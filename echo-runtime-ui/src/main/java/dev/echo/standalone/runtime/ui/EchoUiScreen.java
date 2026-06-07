package dev.echo.standalone.runtime.ui;

public interface EchoUiScreen {
    String id();

    String title();

    EchoUiSurface render(EchoUiContext context);

    default EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
        return EchoUiInputResult.ignored(id());
    }
}
