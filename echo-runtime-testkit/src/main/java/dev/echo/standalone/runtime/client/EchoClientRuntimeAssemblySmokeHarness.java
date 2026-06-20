package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

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
        require(runtime.focusLossRuntime() != null, "Runtime assembly should expose focus-loss runtime controller");
        require(runtime.runtimeBridge() != null, "Runtime assembly should expose engine runtime bridge");
        require(runtime.input() == null, "Runtime assembly should not create native input before live initialization");
        require(!runtime.launchContext().hasPackContext(),
                "Runtime assembly should default to an empty launcher context");

        runtime.screenRuntime().showInitialMainMenu();
        EchoClientScreenSnapshot title = runtime.screens().snapshot(false);
        require(title.state() == EchoClientGameState.MAIN_MENU,
                "Runtime assembly should publish the initial main menu through screen runtime");
        require(!runtime.runtimeBridge().screenshotInputGate().consumeScreenshotRequest(),
                "Runtime assembly bridge should be idle before native input is attached");

        EchoClientLaunchContext launchContext = EchoClientLaunchContext.parse(new String[] {
                "--live", "C:\\Echo\\Runtime",
                "--profileId", "ashfall-standalone-edition",
                "--installPath", "C:\\Echo\\Instances\\Ashfall Standalone Edition",
                "--packManifest", "C:\\Echo\\Instances\\Ashfall Standalone Edition\\.echo\\installed-manifest.json",
                "--devAccount", "EchoDev"
        });
        EchoClientRuntimeAssembly contextualRuntime = EchoClientRuntimeAssembly.create(800, 450, launchContext);
        contextualRuntime.screenRuntime().showInitialMainMenu();
        EchoClientScreenSnapshot contextualTitle = contextualRuntime.screens().snapshot(false);
        require(contextualRuntime.launchContext().hasPackContext(),
                "Runtime assembly should retain launcher pack context");
        require(contextualTitle.subtitle().contains("ashfall-standalone-edition"),
                "Main menu should surface standalone launch profile context");
        require(contextualRuntime.launchContext().installPath().equals(
                        Path.of("C:\\Echo\\Instances\\Ashfall Standalone Edition").toAbsolutePath().normalize()),
                "Runtime assembly should normalize launcher install path context");

        System.out.println("client runtime assembly smoke PASS width=" + runtime.window().width()
                + " title=" + title.kind());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
