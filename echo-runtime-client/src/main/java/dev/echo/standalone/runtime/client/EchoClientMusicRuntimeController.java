package dev.echo.standalone.runtime.client;

final class EchoClientMusicRuntimeController {
    private final EchoClientScreenController screens;
    private EchoClientAudio audio;

    EchoClientMusicRuntimeController(EchoClientScreenController screens) {
        if (screens == null) {
            throw new IllegalArgumentException("screens must not be null");
        }
        this.screens = screens;
    }

    void attachAudio(EchoClientAudio audio) {
        this.audio = audio;
    }

    boolean update(long tick) {
        if (audio == null) {
            return false;
        }
        return audio.applyMusicMode(modeFor(screens.state()), tick);
    }

    boolean stop(long tick) {
        if (audio == null) {
            return false;
        }
        return audio.applyMusicMode(EchoClientMusicMode.SILENT, tick);
    }

    static EchoClientMusicMode modeFor(EchoClientGameState state) {
        if (state == EchoClientGameState.IN_GAME) {
            return EchoClientMusicMode.GAMEPLAY;
        }
        if (state == EchoClientGameState.SHUTDOWN) {
            return EchoClientMusicMode.SILENT;
        }
        return EchoClientMusicMode.MENU;
    }
}
