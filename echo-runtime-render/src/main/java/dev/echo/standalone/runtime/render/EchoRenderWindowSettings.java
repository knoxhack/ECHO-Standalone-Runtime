package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderWindowSettings(
        String title,
        EchoRenderViewport viewport,
        EchoRenderWindowMode mode,
        boolean vsync
) {
    public EchoRenderWindowSettings {
        title = EchoRenderText.requireText(title, "title");
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(mode, "mode");
    }

    public static EchoRenderWindowSettings headlessDebug() {
        return new EchoRenderWindowSettings(
                "ECHO Ashfall Debug Renderer",
                new EchoRenderViewport(1280, 720),
                EchoRenderWindowMode.HEADLESS,
                false
        );
    }

    public static EchoRenderWindowSettings windowedGame() {
        return new EchoRenderWindowSettings(
                "ECHO Ashfall Standalone",
                new EchoRenderViewport(1280, 720),
                EchoRenderWindowMode.WINDOWED,
                true
        );
    }

    public static EchoRenderWindowSettings fullscreenGame(int width, int height) {
        return new EchoRenderWindowSettings(
                "ECHO Ashfall Standalone",
                new EchoRenderViewport(width, height),
                EchoRenderWindowMode.FULLSCREEN,
                true
        );
    }
}
