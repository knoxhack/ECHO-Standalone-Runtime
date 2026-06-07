package dev.echo.standalone.runtime.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRecordingRenderBackend implements EchoRenderBackend {
    private final ArrayList<EchoRenderFrame> frames = new ArrayList<>();
    private final ArrayList<EchoRenderWindowEvent> events = new ArrayList<>();
    private EchoRenderWindowState window;
    private int eventSequence;

    @Override
    public String backendId() {
        return "echo:recording_renderer";
    }

    @Override
    public synchronized EchoRenderWindowState openWindow(EchoRenderWindowSettings settings) {
        Objects.requireNonNull(settings, "settings");
        window = new EchoRenderWindowState(
                "window:recording-debug",
                settings.title(),
                settings.viewport(),
                settings.mode(),
                true,
                false
        );
        record(EchoRenderWindowEventType.CREATED, "window created");
        return window;
    }

    @Override
    public synchronized EchoRenderFrame render(EchoRenderScene scene) {
        Objects.requireNonNull(scene, "scene");
        if (window == null || !window.open()) {
            throw new IllegalStateException("Recording renderer has no open window");
        }
        EchoRenderFrame frame = new EchoRenderFrame(
                frames.size(),
                window,
                scene,
                scene.commands().size(),
                List.of(new EchoRenderDiagnostic(
                        EchoRenderDiagnosticSeverity.INFO,
                        "recorded commands=" + scene.commands().size()
                ))
        );
        frames.add(frame);
        return frame;
    }

    @Override
    public synchronized List<EchoRenderFrame> frames() {
        return List.copyOf(frames);
    }

    @Override
    public synchronized EchoRenderWindowState resizeWindow(EchoRenderViewport viewport) {
        Objects.requireNonNull(viewport, "viewport");
        requireOpenWindow();
        window = window.withViewport(viewport);
        record(EchoRenderWindowEventType.RESIZED, "window resized to " + viewport.width() + "x" + viewport.height());
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState setWindowMode(EchoRenderWindowMode mode) {
        Objects.requireNonNull(mode, "mode");
        requireOpenWindow();
        window = window.withMode(mode);
        EchoRenderWindowEventType eventType = mode == EchoRenderWindowMode.FULLSCREEN
                ? EchoRenderWindowEventType.FULLSCREEN_ENTERED
                : EchoRenderWindowEventType.WINDOWED_ENTERED;
        record(eventType, "window mode set to " + mode.name());
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState requestClose(String reason) {
        requireOpenWindow();
        window = window.withCloseRequested(true);
        record(EchoRenderWindowEventType.CLOSE_REQUESTED, reason == null || reason.isBlank() ? "close requested" : reason);
        return window;
    }

    @Override
    public synchronized EchoRenderWindowState closeAfterCrash(String operation) {
        if (window == null) {
            window = new EchoRenderWindowState(
                    "window:recording-debug",
                    EchoRenderWindowSettings.windowedGame().title(),
                    EchoRenderWindowSettings.windowedGame().viewport(),
                    EchoRenderWindowMode.WINDOWED,
                    false,
                    true
            );
            record(EchoRenderWindowEventType.CRASH_SAFE_CLOSED, "window closed after crash before creation");
            return window;
        }
        window = window.withCloseRequested(true).withOpen(false);
        record(EchoRenderWindowEventType.CRASH_SAFE_CLOSED, operation == null || operation.isBlank()
                ? "window closed after crash"
                : "window closed after crash:" + operation);
        return window;
    }

    @Override
    public synchronized java.util.Optional<EchoRenderWindowState> windowState() {
        return java.util.Optional.ofNullable(window);
    }

    @Override
    public synchronized List<EchoRenderWindowEvent> windowEvents() {
        return List.copyOf(events);
    }

    @Override
    public synchronized void close() {
        if (window == null) {
            return;
        }
        if (!window.open()) {
            return;
        }
        window = window.withOpen(false);
        record(EchoRenderWindowEventType.CLOSED, "window closed");
    }

    private void requireOpenWindow() {
        if (window == null || !window.open()) {
            throw new IllegalStateException("Recording renderer has no open window");
        }
    }

    private void record(EchoRenderWindowEventType type, String detail) {
        eventSequence += 1;
        events.add(new EchoRenderWindowEvent(
                "window-event-" + eventSequence,
                type,
                window,
                detail
        ));
    }
}
