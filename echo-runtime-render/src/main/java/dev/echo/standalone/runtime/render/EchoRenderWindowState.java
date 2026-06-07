package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderWindowState(
        String windowId,
        String title,
        EchoRenderViewport viewport,
        EchoRenderWindowMode mode,
        boolean open,
        boolean closeRequested
) {
    public EchoRenderWindowState {
        windowId = EchoRenderText.requireText(windowId, "windowId");
        title = EchoRenderText.requireText(title, "title");
        Objects.requireNonNull(viewport, "viewport");
        Objects.requireNonNull(mode, "mode");
    }

    public EchoRenderWindowState withViewport(EchoRenderViewport nextViewport) {
        return new EchoRenderWindowState(windowId, title, nextViewport, mode, open, closeRequested);
    }

    public EchoRenderWindowState withMode(EchoRenderWindowMode nextMode) {
        return new EchoRenderWindowState(windowId, title, viewport, nextMode, open, closeRequested);
    }

    public EchoRenderWindowState withOpen(boolean nextOpen) {
        return new EchoRenderWindowState(windowId, title, viewport, mode, nextOpen, closeRequested);
    }

    public EchoRenderWindowState withCloseRequested(boolean nextCloseRequested) {
        return new EchoRenderWindowState(windowId, title, viewport, mode, open, nextCloseRequested);
    }
}
