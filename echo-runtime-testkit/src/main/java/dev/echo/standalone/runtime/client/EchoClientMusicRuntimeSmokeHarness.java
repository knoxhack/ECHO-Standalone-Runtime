package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.audio.EchoAudioBus;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackAction;
import dev.echo.standalone.runtime.audio.EchoAudioPlaybackEvent;
import dev.echo.standalone.runtime.audio.EchoRecordingAudioBackend;

public final class EchoClientMusicRuntimeSmokeHarness {
    private EchoClientMusicRuntimeSmokeHarness() {
    }

    public static void main(String[] args) {
        requireStateDrivenMusicSwitching();
        System.out.println("client music runtime smoke PASS menu=loop gameplay=loop stop=switch");
    }

    private static void requireStateDrivenMusicSwitching() {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoClientAudio audio = new EchoClientAudio();
        audio.init(backend);
        EchoClientScreenController screens = new EchoClientScreenController();
        EchoClientMusicRuntimeController music = new EchoClientMusicRuntimeController(screens);
        music.attachAudio(audio);

        screens.showMainMenu(false);
        require(music.update(1L), "Initial main menu should start menu music");
        require(!music.update(2L), "Repeating the same menu state should not replay menu music");
        require(audio.currentMusicClipId().equals("echo:music_menu"),
                "Audio should remember the active menu music clip");

        screens.showInGame();
        require(music.update(3L), "Entering gameplay should switch to survival music");
        require(audio.currentMusicClipId().equals("echo:music_survival"),
                "Audio should remember the active gameplay music clip");

        screens.showPauseMenu();
        require(music.update(4L), "Pausing should switch back to menu music");
        require(audio.currentMusicClipId().equals("echo:music_menu"),
                "Audio should remember menu music after pausing");

        require(music.stop(5L), "Stopping music should stop the active menu track");
        require(audio.currentMusicClipId().isBlank(),
                "Audio should clear active music after an explicit stop");

        require(backend.events().size() == 6,
                "Music switching should record loop stop loop stop loop stop");
        requireMusicEvent(backend.events().get(0), EchoAudioPlaybackAction.LOOP, "echo:music_menu", 1L);
        requireMusicEvent(backend.events().get(1), EchoAudioPlaybackAction.STOP, "echo:music_menu", 3L);
        requireMusicEvent(backend.events().get(2), EchoAudioPlaybackAction.LOOP, "echo:music_survival", 3L);
        requireMusicEvent(backend.events().get(3), EchoAudioPlaybackAction.STOP, "echo:music_survival", 4L);
        requireMusicEvent(backend.events().get(4), EchoAudioPlaybackAction.LOOP, "echo:music_menu", 4L);
        requireMusicEvent(backend.events().get(5), EchoAudioPlaybackAction.STOP, "echo:music_menu", 5L);
    }

    private static void requireMusicEvent(
            EchoAudioPlaybackEvent event,
            EchoAudioPlaybackAction action,
            String clipId,
            long tick
    ) {
        require(event.action() == action,
                "Music event should use action " + action + " for " + clipId);
        require(event.bus() == EchoAudioBus.MUSIC,
                "Music event should use the music bus");
        require(event.clip().clipId().equals(clipId),
                "Music event should target clip " + clipId);
        require(event.tick() == tick,
                "Music event should preserve the runtime tick");
        require(event.effectiveGain() > 0.0D && event.effectiveGain() <= 1.0D,
                "Music event should have a positive effective gain");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
