package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.render.EchoRenderWindowEventType;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleResult;

import java.util.Objects;

public record EchoWindowedRuntimeResult(
        String runtimeId,
        EchoRenderWindowLifecycleResult windowLifecycle
) {
    public EchoWindowedRuntimeResult {
        runtimeId = requireText(runtimeId, "runtimeId");
        Objects.requireNonNull(windowLifecycle, "windowLifecycle");
    }

    public boolean created() {
        return hasEvent(EchoRenderWindowEventType.CREATED);
    }

    public boolean resized() {
        return hasEvent(EchoRenderWindowEventType.RESIZED);
    }

    public boolean fullscreenEntered() {
        return hasEvent(EchoRenderWindowEventType.FULLSCREEN_ENTERED);
    }

    public boolean windowedEntered() {
        return hasEvent(EchoRenderWindowEventType.WINDOWED_ENTERED);
    }

    public boolean closeRequested() {
        return hasEvent(EchoRenderWindowEventType.CLOSE_REQUESTED);
    }

    public boolean closed() {
        return hasEvent(EchoRenderWindowEventType.CLOSED)
                || hasEvent(EchoRenderWindowEventType.CRASH_SAFE_CLOSED);
    }

    private boolean hasEvent(EchoRenderWindowEventType type) {
        return windowLifecycle.events().stream().anyMatch(event -> event.type() == type);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
