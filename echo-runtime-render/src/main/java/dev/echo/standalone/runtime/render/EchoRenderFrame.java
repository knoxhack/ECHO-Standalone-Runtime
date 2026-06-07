package dev.echo.standalone.runtime.render;

import java.util.List;
import java.util.Objects;

public record EchoRenderFrame(
        long frameIndex,
        EchoRenderWindowState window,
        EchoRenderScene scene,
        int submittedCommandCount,
        List<EchoRenderDiagnostic> diagnostics
) {
    public EchoRenderFrame {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex must not be negative");
        }
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(scene, "scene");
        if (submittedCommandCount < 0) {
            throw new IllegalArgumentException("submittedCommandCount must not be negative");
        }
        Objects.requireNonNull(diagnostics, "diagnostics");
        diagnostics = List.copyOf(diagnostics);
    }
}
