package dev.echo.standalone.runtime.client;

public final class EchoClientRuntimeAssemblySmokeHarness {
    private EchoClientRuntimeAssemblySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoClientRuntimeAssembly runtime = EchoClientRuntimeAssembly.create(960, 540);

        require(runtime.window().width() == 960, "Runtime assembly should create the configured window width");
        require(runtime.window().height() == 540, "Runtime assembly should create the configured window height");
        require(runtime.screens() != null, "Runtime assembly should expose ScreenCore state");
        require(runtime.settingsController() != null, "Runtime assembly should expose settings controller");
        require(runtime.gameplayRuntime() != null, "Runtime assembly should expose gameplay runtime controller");
        require(runtime.commands() != null, "Runtime assembly should expose command controller");
        require(runtime.particleRuntime() != null, "Runtime assembly should expose particle runtime controller");
        require(runtime.musicRuntime() != null, "Runtime assembly should expose music runtime controller");
        require(runtime.fullscreenShortcutRuntime() != null,
                "Runtime assembly should expose fullscreen shortcut runtime controller");
        require(runtime.renderRuntime() != null, "Runtime assembly should expose render runtime controller");
        require(runtime.shellRuntime() != null, "Runtime assembly should expose shell runtime controller");
        require(runtime.screenRuntime() != null, "Runtime assembly should expose screen runtime controller");
        require(runtime.slotGridRuntime() != null, "Runtime assembly should expose slot-grid runtime controller");
        require(runtime.screenshotRuntime() != null, "Runtime assembly should expose screenshot runtime controller");
        require(runtime.runtimeBridge() != null, "Runtime assembly should expose engine runtime bridge");
        require(runtime.input() == null, "Runtime assembly should not create native input before live initialization");

        runtime.screenRuntime().showInitialMainMenu();
        EchoClientScreenSnapshot title = runtime.screens().snapshot(false);
        require(title.state() == EchoClientGameState.MAIN_MENU,
                "Runtime assembly should publish the initial main menu through screen runtime");
        require(!runtime.runtimeBridge().screenshotInputGate().consumeScreenshotRequest(),
                "Runtime assembly bridge should be idle before native input is attached");

        System.out.println("client runtime assembly smoke PASS width=" + runtime.window().width()
                + " title=" + title.kind());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
