package dev.echo.standalone.runtime.client;

final class EchoClientFullscreenShortcutRuntimeController {
    private final EchoClientScreenController screens;

    EchoClientFullscreenShortcutRuntimeController(EchoClientScreenController screens) {
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        this.screens = screens;
    }

    boolean update(InputGate input) {
        if (input == null || !input.consumeToggleFullscreen()) {
            return false;
        }
        boolean fullscreen = screens.toggleFullscreenPreference();
        screens.showToast("Fullscreen " + (fullscreen ? "ON" : "OFF"));
        return true;
    }

    @FunctionalInterface
    interface InputGate {
        boolean consumeToggleFullscreen();
    }
}
