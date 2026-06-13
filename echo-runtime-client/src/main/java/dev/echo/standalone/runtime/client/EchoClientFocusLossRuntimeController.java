package dev.echo.standalone.runtime.client;

final class EchoClientFocusLossRuntimeController {
    private int focusLossCount;
    private int gameplayPauseCount;

    boolean handleFocusLost(
            EchoClientScreenController screens,
            FocusLossInput input,
            boolean hasActiveWorld
    ) {
        focusLossCount++;
        if (input != null) {
            input.releaseForFocusLoss();
        }
        if (screens == null || !hasActiveWorld || screens.state() != EchoClientGameState.IN_GAME) {
            return false;
        }
        screens.showPauseMenu();
        screens.showToast("Paused because the window lost focus");
        gameplayPauseCount++;
        return true;
    }

    int focusLossCount() {
        return focusLossCount;
    }

    int gameplayPauseCount() {
        return gameplayPauseCount;
    }

    interface FocusLossInput {
        void releaseForFocusLoss();
    }
}
