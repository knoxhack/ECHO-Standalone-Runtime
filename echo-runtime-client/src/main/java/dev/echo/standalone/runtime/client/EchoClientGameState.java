package dev.echo.standalone.runtime.client;

enum EchoClientGameState {
    BOOT,
    MAIN_MENU,
    MOD_SCAN,
    LOADING_ASSETS,
    LOADING_DATA,
    LOADING_WORLD,
    IN_GAME,
    PAUSED,
    DEAD,
    SCREEN_OPEN,
    SAVING,
    QUITTING_TO_TITLE,
    SHUTDOWN
}
