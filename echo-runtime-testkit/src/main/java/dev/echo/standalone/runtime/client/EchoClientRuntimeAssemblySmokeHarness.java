package dev.echo.standalone.runtime.client;

import java.nio.file.Path;

public final class EchoClientRuntimeAssemblySmokeHarness {
    private static final Path DEFAULT_PACK_ROOT = Path.of(
            "..",
            "ECHO-Ashfall-Standalone-Edition",
            "tmp",
            "rebuild-official-modpack-assets",
            "ashfall-standalone-edition"
    ).toAbsolutePath().normalize();

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

        Path packRoot = DEFAULT_PACK_ROOT;
        Path modulesRoot = packRoot.resolve("mods").toAbsolutePath().normalize();
        Path packManifest = packRoot.resolve(".echo").resolve("pack-manifest.json").toAbsolutePath().normalize();
        Path evidenceOutput = Path.of("build", "tmp", "visible-evidence").toAbsolutePath().normalize();
        Path evidenceManifest = Path.of(
                "reports",
                "echo",
                "standalone",
                "packaged-visible-client-captures.json"
        ).toAbsolutePath().normalize();
        EchoClientLaunchContext launchContext = EchoClientLaunchContext.parse(new String[] {
                "--live", "C:\\Echo\\Runtime",
                "--profileId", "ashfall-standalone-edition",
                "--installPath", packRoot.toString(),
                "--packManifest", packManifest.toString(),
                "--pack-root", packRoot.toString(),
                "--modules-root", modulesRoot.toString(),
                "--devAccount", "EchoDev",
                "--visible-evidence-capture",
                "--packaged-client-evidence",
                "--evidence-output", evidenceOutput.toString(),
                "--evidence-manifest", evidenceManifest.toString()
        });
        require(launchContext.installPath().equals(packRoot),
                "Launch context should normalize launcher install path context");
        require(launchContext.visibleEvidenceCapture(),
                "Launch context should retain visible evidence capture mode");
        require(launchContext.packagedClientEvidence(),
                "Launch context should retain packaged-client evidence mode");
        require(launchContext.evidenceOutputRoot().equals(evidenceOutput),
                "Launch context should normalize visible evidence output root");
        require(launchContext.evidenceManifest().equals(evidenceManifest),
                "Launch context should normalize visible evidence manifest path");

        EchoClientLaunchContext contextualLaunchContext = new EchoClientLaunchContext(
                false,
                null,
                "ashfall-standalone-edition",
                null,
                null,
                null,
                null,
                "EchoDev",
                false,
                false,
                false,
                false,
                null,
                null
        );
        EchoClientRuntimeAssembly contextualRuntime = EchoClientRuntimeAssembly.create(800, 450, contextualLaunchContext);
        contextualRuntime.screenRuntime().showInitialMainMenu();
        EchoClientScreenSnapshot contextualTitle = contextualRuntime.screens().snapshot(false);
        require(contextualRuntime.launchContext().hasPackContext(),
                "Runtime assembly should retain launcher pack context");
        require(contextualTitle.subtitle().contains("ashfall-standalone-edition"),
                "Main menu should surface standalone launch profile context");
        require(!contextualRuntime.launchContext().strictPackMode(),
                "Runtime assembly smoke should not trigger strict installed-pack module execution");

        System.out.println("client runtime assembly smoke PASS width=" + runtime.window().width()
                + " title=" + title.kind());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
