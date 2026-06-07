package dev.echo.standalone.runtime.client;

enum EchoClientMusicMode {
    SILENT(""),
    MENU("echo:music_menu"),
    GAMEPLAY("echo:music_survival");

    private final String clipId;

    EchoClientMusicMode(String clipId) {
        this.clipId = clipId;
    }

    String clipId() {
        return clipId;
    }
}
