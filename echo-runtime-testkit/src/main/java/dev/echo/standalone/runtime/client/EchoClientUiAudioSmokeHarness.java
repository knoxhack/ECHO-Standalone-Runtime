package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;

import java.nio.file.Path;

public final class EchoClientUiAudioSmokeHarness {
    private EchoClientUiAudioSmokeHarness() {
    }

    public static void main(String[] args) {
        requireScreenFeedbackAudio();
        System.out.println("client ui audio smoke PASS cue=echo:client_ui_click bus=ui");
    }

    private static void requireScreenFeedbackAudio() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);

        EchoClientRuntimeServices services = new EchoClientRuntimeServices();
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientSettingsController settings = new EchoClientSettingsController(
                screens,
                new EchoClientSettingsStore(Path.of("build", "tmp", "client-ui-audio-smoke.properties")),
                new RecordingSettingsHost()
        );
        EchoClientScreenRuntimeController screenRuntime =
                new EchoClientScreenRuntimeController(services, screens, settings);
        screenRuntime.attachAudio(audio);

        screens.showMainMenu(false);
        require(!screenRuntime.playPendingUiFeedback(),
                "Showing the initial screen should not emit an input feedback sound");

        screens.moveSelection(1, false, 720);
        require(screenRuntime.playPendingUiFeedback(),
                "Keyboard menu selection changes should emit UI feedback");

        screens.showMainMenu(false);
        selectCommand(screens, EchoClientScreenCommand.OPEN_CREATE_WORLD, false);
        EchoClientScreenCommand openCreateWorld = screens.activateSelection(false);
        require(openCreateWorld == EchoClientScreenCommand.OPEN_CREATE_WORLD,
                "Activating New Game should route to the create-world screen");
        require(screenRuntime.playPendingUiFeedback(),
                "Activating a menu option should emit UI feedback");
        require(screens.executeNavigationCommand(openCreateWorld, false),
                "Create-world navigation command should open the create-world screen");

        selectCommand(screens, EchoClientScreenCommand.START_NEW_GAME, false);
        EchoClientScreenCommand newGame = screens.activateSelection(false);
        require(newGame == EchoClientScreenCommand.NONE && screens.modalOpen(),
                "Activating New Game should open the confirmation modal through ScreenCore");
        require(screenRuntime.playPendingUiFeedback(),
                "Activating a menu option should emit UI feedback even when the command opens a modal");

        EchoClientScreenCommand confirm = screens.confirmModalSelection();
        require(confirm == EchoClientScreenCommand.START_NEW_GAME,
                "Confirming the create-world modal should return the pending command");
        require(screenRuntime.playPendingUiFeedback(),
                "Confirming a modal should emit UI feedback");
        require(!screenRuntime.playPendingUiFeedback(),
                "UI feedback should be consumed after one audio flush");

        require(backend.events().size() == 4,
                "Four UI interactions should submit exactly four UI click audio events");
        for (EchoAudioPlaybackEvent event : backend.events()) {
            require(event.action() == EchoAudioPlaybackAction.PLAY,
                    "UI feedback should be a one-shot play event");
            require(event.bus() == EchoAudioBus.UI,
                    "UI feedback should use the UI audio bus");
            require(event.clip().clipId().equals("echo:client_ui_click"),
                    "UI feedback should use the client UI click clip");
            require(event.effectiveGain() > 0.0D,
                    "UI feedback should have audible effective gain");
        }
    }

    private static void selectCommand(
            EchoClientScreenController screens,
            EchoClientScreenCommand command,
            boolean hasSession
    ) {
        for (int i = 0; i < 32; i++) {
            EchoClientScreenSnapshot snapshot = screens.snapshot(hasSession);
            if (snapshot.selectedIndex() >= 0
                    && snapshot.selectedIndex() < snapshot.options().size()
                    && snapshot.options().get(snapshot.selectedIndex()).command() == command) {
                screens.consumeUiFeedbackPulse();
                return;
            }
            screens.moveSelection(1, hasSession, 720);
            screens.consumeUiFeedbackPulse();
        }
        throw new AssertionError("Unable to select command " + command);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static final class RecordingSettingsHost implements EchoClientSettingsController.Host {
        @Override
        public void applyInputSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyAudioSettings(EchoClientSettings settings) {
        }

        @Override
        public void applyRenderSettings(int chunkViewDistance, boolean chunkViewChanged) {
        }

        @Override
        public void applyWindowSettings(boolean fullscreen, boolean vSync) {
        }

        @Override
        public void settingsSaveFailed(Path path, String message) {
        }
    }
}
