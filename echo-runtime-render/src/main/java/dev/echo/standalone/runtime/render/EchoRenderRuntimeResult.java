package dev.echo.standalone.runtime.render;

import java.util.Objects;

public record EchoRenderRuntimeResult(
        EchoRenderBackend backend,
        EchoRenderWindowState window,
        EchoRenderUiBridge uiBridge,
        EchoRenderSceneBuilder sceneBuilder,
        EchoRenderScene initialScene,
        EchoRenderFrame initialFrame
) {
    public EchoRenderRuntimeResult {
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(window, "window");
        Objects.requireNonNull(uiBridge, "uiBridge");
        Objects.requireNonNull(sceneBuilder, "sceneBuilder");
        Objects.requireNonNull(initialScene, "initialScene");
        Objects.requireNonNull(initialFrame, "initialFrame");
    }
}
