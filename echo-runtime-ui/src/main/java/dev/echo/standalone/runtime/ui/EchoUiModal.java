package dev.echo.standalone.runtime.ui;

public interface EchoUiModal {
    String id();

    String title();

    EchoUiSurface render(EchoUiContext context);

    EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context);
}
