package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public record EchoUiFrame(
        EchoUiSurface screen,
        List<EchoUiSurface> modals,
        EchoUiTheme theme
) {
    public EchoUiFrame {
        Objects.requireNonNull(screen, "screen");
        Objects.requireNonNull(modals, "modals");
        Objects.requireNonNull(theme, "theme");
        modals = List.copyOf(modals);
    }
}
