package dev.echo.standalone.runtime.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class EchoClientShellRuntimeControllerSmokeHarness {
    private EchoClientShellRuntimeControllerSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path saveRoot = Path.of("build", "tmp", "client-shell-runtime-controller-smoke").toAbsolutePath();
        deleteRecursively(saveRoot);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices(EchoClientSaveSlotService.open(saveRoot));
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientWorldSessionController worldSessions = new EchoClientWorldSessionController(services, screens);
        EchoClientShellRuntimeController shellRuntime =
                new EchoClientShellRuntimeController(screens, worldSessions);
        RecordingInputGate input = new RecordingInputGate();
        RecordingHost host = new RecordingHost();

        screens.showMainMenu(false);
        require(!shellRuntime.updateBlockingFlow(input, 1.0D / 20.0D, host),
                "Idle title state should not be consumed by the blocking shell controller");
        require(input.cursorUnlocks == 0 && input.triggerClears == 0,
                "Idle title state should not touch gameplay input gates");

        require(worldSessions.beginNewWorldLoad(),
                "Shell runtime smoke should begin a pending new-world load");
        require(shellRuntime.updateBlockingFlow(input, 1.0D / 20.0D, host),
                "Loading state should be consumed by the blocking shell controller");
        require(input.cursorUnlocks == 1 && input.triggerClears == 1,
                "Loading state should unlock the cursor and clear gameplay triggers");
        advanceBlockingFlow(shellRuntime, input, host, screens);
        require(services.hasActiveWorld(), "Loading completion should attach an active runtime world");
        require(host.attachSessionRequests == 1,
                "Loading completion should ask the host to attach render/runtime session state");
        require(screens.state() == EchoClientGameState.IN_GAME,
                "Loading completion should leave the ScreenCore state in gameplay");

        screens.showPauseMenu();
        screens.showSaving();
        shellRuntime.beginSaving();
        int unlocksBeforeSaving = input.cursorUnlocks;
        int clearsBeforeSaving = input.triggerClears;
        require(shellRuntime.updateBlockingFlow(input, 0.10D, host),
                "Saving state should be consumed while the save overlay is visible");
        require(screens.state() == EchoClientGameState.SAVING,
                "Saving overlay should remain visible before the minimum display time");
        require(input.cursorUnlocks == unlocksBeforeSaving + 1
                        && input.triggerClears == clearsBeforeSaving + 1,
                "Saving state should unlock the cursor and clear gameplay triggers");
        require(shellRuntime.updateBlockingFlow(input, 0.20D, host),
                "Saving state should be consumed until it returns to pause");
        require(screens.state() == EchoClientGameState.PAUSED,
                "Saving overlay should return to the pause shell after the minimum display time");

        System.out.println("client shell runtime controller smoke PASS attachRequests="
                + host.attachSessionRequests
                + " inputUnlocks=" + input.cursorUnlocks);
    }

    private static void advanceBlockingFlow(
            EchoClientShellRuntimeController shellRuntime,
            RecordingInputGate input,
            RecordingHost host,
            EchoClientScreenController screens
    ) {
        for (int tick = 0; tick < 240 && screens.state() != EchoClientGameState.IN_GAME; tick++) {
            shellRuntime.updateBlockingFlow(input, 1.0D / 20.0D, host);
        }
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

    private static final class RecordingInputGate implements EchoClientShellRuntimeController.InputGate {
        private int cursorUnlocks;
        private int triggerClears;

        @Override
        public void unlockCursor() {
            cursorUnlocks++;
        }

        @Override
        public void clearGameplayTriggers() {
            triggerClears++;
        }
    }

    private static final class RecordingHost implements EchoClientShellRuntimeController.Host {
        private int attachSessionRequests;

        @Override
        public void attachSession() {
            attachSessionRequests++;
        }
    }
}
