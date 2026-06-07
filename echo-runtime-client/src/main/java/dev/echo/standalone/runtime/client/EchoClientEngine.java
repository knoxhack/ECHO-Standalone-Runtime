package dev.echo.standalone.runtime.client;

/**
 * Thin shell around the runtime assembly.
 * Owns the native loop, timing, and window lifecycle only.
 * All game logic, state routing, and screen management live in {@link EchoClientRuntimeAssembly}.
 */
final class EchoClientEngine {
    private static final double FIXED_TIMESTEP = 1.0D / 60.0D;
    private static final double TARGET_FRAME_TIME = 1.0D / 60.0D;
    private static final int MAX_UPDATES_PER_FRAME = 5;
    private static final int INITIAL_WIDTH = 1280;
    private static final int INITIAL_HEIGHT = 720;

    private final EchoClientRuntimeAssembly runtime;

    private long frames;
    private double fpsTimer;
    private int fps;

    EchoClientEngine() {
        this(EchoClientRuntimeAssembly.create(INITIAL_WIDTH, INITIAL_HEIGHT));
    }

    EchoClientEngine(EchoClientRuntimeAssembly runtime) {
        this.runtime = runtime;
    }

    void start() {
        runtime.initializeNativeResources();
        runLoop();
    }

    private void runLoop() {
        double previous = now();
        double accumulator = 0.0D;

        while (!runtime.window().shouldClose()) {
            double current = now();
            double frameTime = Math.min(current - previous, 0.25D);
            previous = current;
            accumulator += frameTime;
            fpsTimer += frameTime;

            runtime.window().pollEvents();
            runtime.resizeRendererIfNeeded();

            int updates = 0;
            while (accumulator >= FIXED_TIMESTEP && updates < MAX_UPDATES_PER_FRAME) {
                runtime.update(FIXED_TIMESTEP);
                accumulator -= FIXED_TIMESTEP;
                updates++;
            }

            runtime.resizeRendererIfNeeded();
            runtime.renderFrame(fps, frames);
            runtime.screenshotRuntime().captureIfRequested();
            runtime.window().swapBuffers();
            paceFrame(current);

            frames++;
            if (fpsTimer >= 1.0D) {
                fps = (int) frames;
                frames = 0;
                fpsTimer -= 1.0D;
            }
        }
    }

    void close() {
        runtime.close();
    }

    private static double now() {
        return System.nanoTime() / 1_000_000_000.0D;
    }

    private static void paceFrame(double frameStartSeconds) {
        double remaining = TARGET_FRAME_TIME - (now() - frameStartSeconds);
        if (remaining <= 0.001D) {
            return;
        }
        long millis = (long) (remaining * 1000.0D);
        int nanos = (int) ((remaining * 1_000_000_000.0D) - millis * 1_000_000.0D);
        try {
            Thread.sleep(millis, Math.max(0, Math.min(999_999, nanos)));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
