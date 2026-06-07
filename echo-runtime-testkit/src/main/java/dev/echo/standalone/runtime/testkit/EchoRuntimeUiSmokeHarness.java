package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.assets.EchoAssetMount;
import dev.echo.standalone.runtime.assets.EchoAssetRuntime;
import dev.echo.standalone.runtime.assets.EchoAssetRuntimeResult;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommand;
import dev.echo.standalone.runtime.contracts.EchoRuntimeCommandResult;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.ui.EchoBasicModal;
import dev.echo.standalone.runtime.ui.EchoMenuDefinition;
import dev.echo.standalone.runtime.ui.EchoMenuOption;
import dev.echo.standalone.runtime.ui.EchoMenuRegistry;
import dev.echo.standalone.runtime.ui.EchoMenuScreen;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalScreen;
import dev.echo.standalone.runtime.ui.EchoTerminalShell;
import dev.echo.standalone.runtime.ui.EchoUiFrame;
import dev.echo.standalone.runtime.ui.EchoUiInputEvent;
import dev.echo.standalone.runtime.ui.EchoUiInputResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;
import dev.echo.standalone.runtime.ui.EchoUiThemeRuntime;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class EchoRuntimeUiSmokeHarness {
    private EchoRuntimeUiSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-ui-smoke");
        Path ashfallThemeRoot = fixtureRoot.resolve("ashfall-ui-pack");
        write(ashfallThemeRoot.resolve("data/ashfall/themes/terminal.json"), """
                {
                  "id": "ashfall-terminal",
                  "displayName": "Ashfall Terminal",
                  "accentColor": "#67e8f9",
                  "backgroundColor": "#061014",
                  "foregroundColor": "#d8fbff",
                  "warningColor": "#facc15",
                  "fontFamily": "ECHO Mono",
                  "density": "compact",
                  "tokens": {
                    "terminal.prompt": "ASH>",
                    "terminal.cursor": "block",
                    "surface.border": "single"
                  }
                }
                """);

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoAssetRuntime assetRuntime = new EchoAssetRuntime(List.of(
                new EchoAssetMount(0, "asset", ashfallThemeRoot, "ashfall-ui-pack")
        ));
        EchoAssetRuntimeResult assetResult = assetRuntime.load(services, List.of("ashfall:themes/terminal.json"));
        EchoUiTheme loadedTheme = new EchoUiThemeRuntime()
                .loadTheme(assetResult.resolver(), "ashfall", "terminal")
                .orElseThrow();

        EchoTerminalShell shell = new EchoTerminalShell();
        shell.commands().registerRuntime(new EchoRuntimeCommand("ashfall_status", "Report Ashfall route", context ->
                EchoRuntimeCommandResult.output("mission=secure_crash_site")));
        shell.promptPrefix(loadedTheme.tokens().get("terminal.prompt"));
        EchoTerminalScreen terminal = new EchoTerminalScreen("terminal", "Terminal", shell);
        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(services, terminal, loadedTheme);

        require(services.require(EchoUiRuntimeResult.class) == ui, "UI runtime result should be service-bound");
        require(services.require(EchoUiThemeRuntime.class).activeTheme().id().equals("ashfall-terminal"),
                "loaded theme should be active");
        require(ui.frame().screen().id().equals("terminal"), "terminal should be active screen");

        EchoUiInputResult status = ui.dispatch(EchoUiInputEvent.command(0, "status"));
        EchoUiInputResult theme = ui.dispatch(EchoUiInputEvent.command(1, "theme"));
        EchoUiInputResult echo = ui.dispatch(EchoUiInputEvent.command(2, "echo survivor online"));
        EchoUiInputResult ashfallStatus = ui.dispatch(EchoUiInputEvent.command(5, "ashfall_status"));
        require(status.handled(), "status command should be handled");
        require(theme.effects().contains("terminal-command:theme"), "theme command should route to terminal");
        require(echo.effects().contains("terminal-command:echo"), "echo command should route to terminal");
        require(ashfallStatus.effects().contains("terminal-command:ashfall_status"),
                "runtime command registry command should route to terminal");
        require(shell.outputLines().stream().anyMatch(line -> line.contains("survivor online")),
                "terminal output should contain echo response");
        require(shell.outputLines().stream().anyMatch(line -> line.contains("mission=secure_crash_site")),
                "terminal output should contain runtime command response");

        EchoMenuDefinition mainMenu = new EchoMenuDefinition(
                "ashfall:main_menu",
                "Ashfall",
                List.of(
                        new EchoMenuOption("continue", "Continue", "menu:continue", true),
                        new EchoMenuOption("new_run", "New Run", "menu:new_run", true),
                        new EchoMenuOption("settings", "Settings", "menu:settings", true),
                        new EchoMenuOption("locked_extraction", "Extraction", "menu:extract", false)
                ),
                1
        );
        EchoMenuRegistry menus = new EchoMenuRegistry();
        menus.register(mainMenu);
        require(menus.find("ashfall:main_menu").orElseThrow().enabledCount() == 3,
                "main menu should expose three enabled options");
        EchoMenuScreen menuScreen = new EchoMenuScreen(mainMenu);
        ui.screenStack().push(menuScreen);
        require(ui.frame().screen().lines().stream().anyMatch(line -> line.contains("Extraction [disabled]")),
                "menu render should expose disabled option state");
        EchoUiInputResult selectedMenu = ui.dispatch(EchoUiInputEvent.command(6, "menu:new_run"));
        EchoUiInputResult disabledMenu = ui.dispatch(EchoUiInputEvent.command(7, "locked_extraction"));
        require(selectedMenu.effects().contains("menu-action:menu:new_run"),
                "menu action should be routed by action id");
        require(disabledMenu.effects().contains("menu-option-disabled:locked_extraction"),
                "disabled menu option should not emit its action");
        ui.screenStack().pop();

        ui.screenStack().push(new EchoStaticScreen("boot", "Boot", List.of("boot handoff"), "boot:continue"));
        require(ui.frame().screen().id().equals("boot"), "pushed screen should become active");
        ui.screenStack().pop();
        require(ui.frame().screen().id().equals("terminal"), "popping screen should restore terminal");

        ui.modalStack().open(new EchoBasicModal("confirm", "Confirm", List.of("acknowledge"), true));
        EchoUiFrame modalFrame = ui.frame();
        require(modalFrame.modals().size() == 1, "modal frame should include top modal");
        EchoUiInputResult consumed = ui.dispatch(EchoUiInputEvent.command(3, "echo hidden"));
        require(consumed.effects().contains("modal-consumed:confirm"), "blocking modal should consume command");
        require(shell.outputLines().stream().noneMatch(line -> line.contains("hidden")),
                "blocked command should not reach terminal");
        EchoUiInputResult dismissed = ui.dispatch(EchoUiInputEvent.command(4, "dismiss"));
        require(dismissed.closeTopModal(), "dismiss command should request modal close");
        require(ui.modalStack().size() == 0, "modal stack should be empty after dismiss");

        System.out.println("phase14.6 ui runtime smoke PASS screens="
                + ui.screenStack().size()
                + " modals="
                + ui.modalStack().size()
                + " commands="
                + shell.history().size()
                + " theme="
                + ui.themeRuntime().activeTheme().id()
                + " lines="
                + shell.outputLines().size());
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
