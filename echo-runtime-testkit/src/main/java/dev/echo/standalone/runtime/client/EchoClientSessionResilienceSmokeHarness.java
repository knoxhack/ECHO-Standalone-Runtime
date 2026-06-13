package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientSessionResilienceSmokeHarness {
    private static final Path REPORT_PATH = Path.of("reports/echo/standalone/client-session-resilience.json");
    private static final int PAUSE_RESUME_CYCLES = 8;
    private static final int SAVE_CONTINUE_CYCLES = 4;

    private EchoClientSessionResilienceSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-session-resilience-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientFocusLossRuntimeController focusLoss = new EchoClientFocusLossRuntimeController();

        services.startNewWorld("session-resilience", "Session Resilience");
        screens.showInGame();
        String slotId = services.worldSession().slotId();
        require(!slotId.isBlank(), "Session resilience smoke needs a concrete save slot");

        RecordingFocusInput focusInput = new RecordingFocusInput();
        require(focusLoss.handleFocusLost(screens, focusInput, services.hasActiveWorld()),
                "Window focus loss during gameplay should pause the active world");
        require(focusInput.releaseCount == 1
                        && !focusInput.cursorLocked
                        && !focusInput.staleKeyStatePresent
                        && !focusInput.gameplayTriggersArmed,
                "Window focus loss should unlock cursor and clear stale gameplay input");
        require(screens.state() == EchoClientGameState.PAUSED,
                "Focus loss should route gameplay into the pause screen");
        require(screens.screenKind() == EchoClientScreenKind.PAUSE_MENU,
                "Focus loss should publish the pause menu route");
        require(optionEnabled(screens.snapshot(services.hasActiveWorld()), EchoClientScreenCommand.RESUME_GAME),
                "Focus-loss pause menu should keep Resume available");
        require(services.hasActiveWorld() && services.worldSession().slotId().equals(slotId),
                "Focus-loss pause should preserve the active world session");
        require(worldSessions.resumeOrTitle(),
                "Resume after focus loss should return to the active world");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Resume after focus loss should restore gameplay state");

        for (int cycle = 0; cycle < PAUSE_RESUME_CYCLES; cycle++) {
            screens.showPauseMenu();
            require(screens.state() == EchoClientGameState.PAUSED,
                    "Pause cycle should enter PAUSED state");
            require(optionEnabled(screens.snapshot(services.hasActiveWorld()), EchoClientScreenCommand.RESUME_GAME),
                    "Pause cycle should keep Resume enabled");
            require(worldSessions.resumeOrTitle(),
                    "Pause cycle should resume an active world");
            require(screens.state() == EchoClientGameState.IN_GAME
                            && services.hasActiveWorld()
                            && services.worldSession().slotId().equals(slotId),
                    "Pause/resume cycle should preserve the same active slot");
        }

        for (int cycle = 0; cycle < SAVE_CONTINUE_CYCLES; cycle++) {
            services.captureMemorySave();
            require(services.hasContinuableSession(),
                    "Manual save cycle should make Continue available");
            worldSessions.quitToTitle();
            require(!services.hasActiveWorld(),
                    "Quit To Title should unload the active world before Continue");
            EchoClientScreenSnapshot title = screens.snapshot(services.hasContinuableSession());
            require(title.state() == EchoClientGameState.MAIN_MENU,
                    "Quit To Title should return to the main menu");
            require(optionEnabled(title, EchoClientScreenCommand.CONTINUE_GAME),
                    "Saved title should keep Continue enabled");
            require(worldSessions.beginContinueWorldLoad(),
                    "Continue cycle should begin saved-world loading");
            advanceLoading(screens);
            require(worldSessions.finishPendingWorldLoad().sessionAttached(),
                    "Continue cycle should attach the saved world");
            require(services.hasActiveWorld()
                            && services.worldSession().slotId().equals(slotId)
                            && screens.state() == EchoClientGameState.IN_GAME,
                    "Continue cycle should restore the same active slot into gameplay");
        }

        RecordingFocusInput pausedFocusInput = new RecordingFocusInput();
        screens.showPauseMenu();
        require(!focusLoss.handleFocusLost(screens, pausedFocusInput, services.hasActiveWorld()),
                "Focus loss while already paused should not stack another gameplay pause");
        require(pausedFocusInput.releaseCount == 1 && !pausedFocusInput.cursorLocked,
                "Focus loss while paused should still release stale input");
        require(focusLoss.focusLossCount() == 2,
                "Focus-loss controller should count both gameplay and paused focus-loss events");
        require(focusLoss.gameplayPauseCount() == 1,
                "Focus-loss controller should only count gameplay-origin pauses");

        writeReport(slotId, focusInput, focusLoss);
        System.out.println("client session resilience smoke PASS slot=" + slotId
                + " pauseResumeCycles=" + PAUSE_RESUME_CYCLES
                + " saveContinueCycles=" + SAVE_CONTINUE_CYCLES);
    }

    private static void advanceLoading(EchoClientScreenController screens) {
        for (int tick = 0; tick < 240 && screens.state() != EchoClientGameState.IN_GAME; tick++) {
            screens.updateLoading(1.0D / 20.0D);
        }
    }

    private static boolean optionEnabled(EchoClientScreenSnapshot snapshot, EchoClientScreenCommand command) {
        return snapshot.options().stream()
                .anyMatch(option -> option.command() == command && option.enabled());
    }

    private static void writeReport(
            String slotId,
            RecordingFocusInput focusInput,
            EchoClientFocusLossRuntimeController focusLoss
    ) throws IOException {
        String json = """
                {
                  "schema": "echo.standalone.client_session_resilience.v1",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "generator": "EchoClientSessionResilienceSmokeHarness",
                  "status": "PASS",
                  "slotId": "%s",
                  "coverage": {
                    "windowFocusLossPausesGameplay": true,
                    "focusLossUnlocksCursor": true,
                    "focusLossClearsStaleInput": true,
                    "focusLossPreservesActiveWorld": true,
                    "resumeAfterFocusLoss": true,
                    "pauseResumeSpam": true,
                    "saveQuitContinueCycles": true,
                    "pausedFocusLossDoesNotStackPause": true
                  },
                  "runtime": {
                    "focusLossEvents": %d,
                    "gameplayFocusLossPauses": %d,
                    "focusInputReleaseCount": %d,
                    "pauseResumeCycles": %d,
                    "saveContinueCycles": %d
                  }
                }
                """.formatted(
                escape(slotId),
                focusLoss.focusLossCount(),
                focusLoss.gameplayPauseCount(),
                focusInput.releaseCount,
                PAUSE_RESUME_CYCLES,
                SAVE_CONTINUE_CYCLES
        );
        Files.createDirectories(REPORT_PATH.getParent());
        Files.writeString(REPORT_PATH, json);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        try (var stream = Files.walk(root)) {
            List<Path> paths = stream.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingFocusInput implements EchoClientFocusLossRuntimeController.FocusLossInput {
        private boolean cursorLocked = true;
        private boolean staleKeyStatePresent = true;
        private boolean gameplayTriggersArmed = true;
        private int releaseCount;

        @Override
        public void releaseForFocusLoss() {
            cursorLocked = false;
            staleKeyStatePresent = false;
            gameplayTriggersArmed = false;
            releaseCount++;
        }
    }
}
