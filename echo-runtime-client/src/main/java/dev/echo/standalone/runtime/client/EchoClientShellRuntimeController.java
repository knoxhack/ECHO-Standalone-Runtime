package dev.echo.standalone.runtime.client;

final class EchoClientShellRuntimeController {
    private static final double SAVE_OVERLAY_SECONDS = 0.25D;

    private final EchoClientScreenController screens;
    private final EchoClientWorldSessionController worldSessions;
    private double savingTimer;

    EchoClientShellRuntimeController(
            EchoClientScreenController screens,
            EchoClientWorldSessionController worldSessions
    ) {
        this.screens = screens;
        this.worldSessions = worldSessions;
    }

    void beginSaving() {
        savingTimer = 0.0D;
    }

    boolean updateBlockingFlow(InputGate input, double dt, Host host) {
        EchoClientGameState state = screens.state();
        if (state == EchoClientGameState.SAVING) {
            updateSaving(input, dt);
            return true;
        }
        if (isLoading(state)) {
            updateLoading(input, dt, host);
            return true;
        }
        return false;
    }

    private void updateSaving(InputGate input, double dt) {
        unlockAndClear(input);
        savingTimer += Math.max(0.0D, dt);
        if (savingTimer >= SAVE_OVERLAY_SECONDS) {
            screens.showPauseMenu();
        }
    }

    private void updateLoading(InputGate input, double dt, Host host) {
        unlockAndClear(input);
        if (screens.updateLoading(dt)
                && worldSessions.finishPendingWorldLoad().sessionAttached()
                && host != null) {
            host.attachSession();
        }
    }

    private void unlockAndClear(InputGate input) {
        if (input == null) {
            return;
        }
        input.unlockCursor();
        input.clearGameplayTriggers();
    }

    private static boolean isLoading(EchoClientGameState state) {
        return state == EchoClientGameState.MOD_SCAN
                || state == EchoClientGameState.LOADING_ASSETS
                || state == EchoClientGameState.LOADING_DATA
                || state == EchoClientGameState.LOADING_WORLD;
    }

    interface InputGate {
        void unlockCursor();

        void clearGameplayTriggers();
    }

    interface Host {
        void attachSession();
    }
}
