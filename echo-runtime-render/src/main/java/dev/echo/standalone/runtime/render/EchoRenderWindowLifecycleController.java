package dev.echo.standalone.runtime.render;

import java.util.Objects;

public final class EchoRenderWindowLifecycleController {
    private final EchoRenderBackend backend;

    public EchoRenderWindowLifecycleController(EchoRenderBackend backend) {
        this.backend = Objects.requireNonNull(backend, "backend");
    }

    public EchoRenderWindowState create(EchoRenderWindowSettings settings) {
        return backend.openWindow(settings);
    }

    public EchoRenderWindowState resize(EchoRenderViewport viewport) {
        return backend.resizeWindow(viewport);
    }

    public EchoRenderWindowState fullscreen() {
        return backend.setWindowMode(EchoRenderWindowMode.FULLSCREEN);
    }

    public EchoRenderWindowState windowed(EchoRenderViewport viewport) {
        EchoRenderWindowState window = backend.setWindowMode(EchoRenderWindowMode.WINDOWED);
        if (!window.viewport().equals(viewport)) {
            window = backend.resizeWindow(viewport);
        }
        return window;
    }

    public EchoRenderWindowState requestClose(String reason) {
        return backend.requestClose(reason);
    }

    public EchoRenderWindowLifecycleResult close(String reason) {
        EchoRenderWindowState requested = backend.windowState()
                .filter(EchoRenderWindowState::open)
                .map(state -> backend.requestClose(reason))
                .orElseThrow(() -> new IllegalStateException("No open window to close"));
        backend.close();
        EchoRenderWindowState closed = backend.windowState().orElse(requested.withOpen(false));
        return new EchoRenderWindowLifecycleResult(
                closed,
                backend.windowEvents(),
                false,
                !closed.open(),
                reason
        );
    }

    public EchoRenderWindowLifecycleResult runCrashSafe(String operation, Runnable action) {
        Objects.requireNonNull(action, "action");
        try {
            action.run();
            EchoRenderWindowState state = backend.windowState()
                    .orElseThrow(() -> new IllegalStateException("Window lifecycle did not create a window"));
            return new EchoRenderWindowLifecycleResult(
                    state,
                    backend.windowEvents(),
                    false,
                    !state.open(),
                    operation
            );
        } catch (Throwable throwable) {
            EchoRenderWindowState closed = backend.closeAfterCrash(operation);
            return new EchoRenderWindowLifecycleResult(
                    closed,
                    backend.windowEvents(),
                    true,
                    !closed.open(),
                    throwable.getClass().getName() + ":" + throwable.getMessage()
            );
        }
    }
}
