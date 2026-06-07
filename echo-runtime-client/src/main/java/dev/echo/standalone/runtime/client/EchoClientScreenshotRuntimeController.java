package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

final class EchoClientScreenshotRuntimeController {
    private final EchoClientScreenController screens;
    private final CaptureTarget captureTarget;
    private boolean screenshotRequested;

    EchoClientScreenshotRuntimeController(
            EchoClientScreenController screens,
            EchoGlfwWindow window
    ) {
        this(screens, new EchoClientScreenshotService(), window);
    }

    private EchoClientScreenshotRuntimeController(
            EchoClientScreenController screens,
            EchoClientScreenshotService screenshots,
            EchoGlfwWindow window
    ) {
        this(screens, () -> screenshots.captureFramebuffer(window.width(), window.height()));
    }

    EchoClientScreenshotRuntimeController(
            EchoClientScreenController screens,
            CaptureTarget captureTarget
    ) {
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        if (captureTarget == null) {
            throw new IllegalArgumentException("captureTarget must not be null");
        }
        this.screens = screens;
        this.captureTarget = captureTarget;
    }

    void updateInput(InputGate input) {
        if (input != null && input.consumeScreenshotRequest()) {
            requestScreenshot();
        }
    }

    void requestScreenshot() {
        screenshotRequested = true;
        screens.showToast("Screenshot queued");
    }

    void captureIfRequested() {
        if (!screenshotRequested) {
            return;
        }
        screenshotRequested = false;
        try {
            Path path = captureTarget.capture();
            screens.showToast("Screenshot saved");
            System.out.println("[echo-client] screenshot saved: " + path);
        } catch (Exception e) {
            screens.showToast("Screenshot failed");
            System.out.println("[echo-client] screenshot failed: " + e.getMessage());
        }
    }

    interface InputGate {
        boolean consumeScreenshotRequest();
    }

    interface CaptureTarget {
        Path capture() throws Exception;
    }
}
