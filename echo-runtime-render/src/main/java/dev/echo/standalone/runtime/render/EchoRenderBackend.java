package dev.echo.standalone.runtime.render;

import java.util.List;
import java.util.Optional;

public interface EchoRenderBackend {
    String backendId();

    EchoRenderWindowState openWindow(EchoRenderWindowSettings settings);

    EchoRenderFrame render(EchoRenderScene scene);

    default EchoRenderWindowState resizeWindow(EchoRenderViewport viewport) {
        throw new UnsupportedOperationException("resizeWindow is not supported by " + backendId());
    }

    default EchoRenderWindowState setWindowMode(EchoRenderWindowMode mode) {
        throw new UnsupportedOperationException("setWindowMode is not supported by " + backendId());
    }

    default EchoRenderWindowState requestClose(String reason) {
        throw new UnsupportedOperationException("requestClose is not supported by " + backendId());
    }

    default EchoRenderWindowState closeAfterCrash(String operation) {
        requestClose("crash:" + operation);
        close();
        return windowState().orElseThrow(() -> new IllegalStateException("No window state after crash close"));
    }

    default Optional<EchoRenderWindowState> windowState() {
        return Optional.empty();
    }

    default List<EchoRenderWindowEvent> windowEvents() {
        return List.of();
    }

    List<EchoRenderFrame> frames();

    void close();
}
