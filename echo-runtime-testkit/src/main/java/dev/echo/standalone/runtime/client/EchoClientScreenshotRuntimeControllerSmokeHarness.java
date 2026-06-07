package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Path;

public final class EchoClientScreenshotRuntimeControllerSmokeHarness {
    private EchoClientScreenshotRuntimeControllerSmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientScreenController screens = new EchoClientScreenController();
        RecordingCaptureTarget capture = new RecordingCaptureTarget();
        EchoClientScreenshotRuntimeController screenshots =
                new EchoClientScreenshotRuntimeController(screens, capture);
        RecordingInputGate input = new RecordingInputGate();

        screenshots.captureIfRequested();
        require(capture.captures == 0, "Screenshot runtime should stay idle without a queued request");

        input.requested = true;
        screenshots.updateInput(input);
        require(!input.requested, "Screenshot input gate should consume the request");
        require(screens.snapshot(false).toast().visible(),
                "Screenshot request should publish a queued toast");

        screenshots.captureIfRequested();
        require(capture.captures == 1, "Queued screenshot should capture exactly once");
        require(capture.lastPath.equals(Path.of("build", "tmp", "screenshots", "ok.png")),
                "Screenshot runtime should expose the capture path returned by the target");
        require(screens.snapshot(false).toast().visible(),
                "Successful screenshot should publish a saved toast");

        capture.failNext = true;
        screenshots.requestScreenshot();
        screenshots.captureIfRequested();
        require(capture.captures == 2, "Failed screenshot should still attempt capture exactly once");
        require(screens.snapshot(false).toast().visible(),
                "Failed screenshot should publish a failure toast");

        System.out.println("client screenshot runtime controller smoke PASS captures=" + capture.captures
                + " last=" + capture.lastPath.getFileName());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingInputGate implements EchoClientScreenshotRuntimeController.InputGate {
        private boolean requested;

        @Override
        public boolean consumeScreenshotRequest() {
            boolean value = requested;
            requested = false;
            return value;
        }
    }

    private static final class RecordingCaptureTarget implements EchoClientScreenshotRuntimeController.CaptureTarget {
        private int captures;
        private boolean failNext;
        private Path lastPath = Path.of("build", "tmp", "screenshots", "none.png");

        @Override
        public Path capture() throws IOException {
            captures++;
            if (failNext) {
                failNext = false;
                throw new IOException("forced failure");
            }
            lastPath = Path.of("build", "tmp", "screenshots", "ok.png");
            return lastPath;
        }
    }
}
