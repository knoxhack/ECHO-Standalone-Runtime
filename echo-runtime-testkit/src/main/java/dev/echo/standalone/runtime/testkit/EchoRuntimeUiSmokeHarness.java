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
import java.nio.charset.StandardCharsets;
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
        EchoUiFrame menuFrame = ui.frame();
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
        EchoUiFrame bootFrame = ui.frame();
        require(ui.frame().screen().id().equals("boot"), "pushed screen should become active");
        ui.screenStack().pop();
        EchoUiFrame restoredTerminalFrame = ui.frame();
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
        EchoUiFrame finalFrame = ui.frame();

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                ui,
                loadedTheme,
                shell,
                mainMenu,
                menuFrame,
                selectedMenu,
                disabledMenu,
                bootFrame,
                restoredTerminalFrame,
                modalFrame,
                status,
                theme,
                echo,
                ashfallStatus,
                consumed,
                dismissed,
                finalFrame
        );

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

    private static void writeReports(
            Path standaloneRoot,
            EchoUiRuntimeResult ui,
            EchoUiTheme loadedTheme,
            EchoTerminalShell shell,
            EchoMenuDefinition mainMenu,
            EchoUiFrame menuFrame,
            EchoUiInputResult selectedMenu,
            EchoUiInputResult disabledMenu,
            EchoUiFrame bootFrame,
            EchoUiFrame restoredTerminalFrame,
            EchoUiFrame modalFrame,
            EchoUiInputResult status,
            EchoUiInputResult theme,
            EchoUiInputResult echo,
            EchoUiInputResult ashfallStatus,
            EchoUiInputResult consumed,
            EchoUiInputResult dismissed,
            EchoUiFrame finalFrame
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        write(root.resolve("runtime-ui.json"), """
                {
                  "schema": "echo.standalone.runtime_ui.v2",
                  "status": "PASS",
                  "phase": "14.6",
                  "summary": "UI runtime booted service-bound screen, modal, router, and theme services, loaded an Ashfall theme from mounted assets, routed Terminal commands, menus, screen push/pop, and blocking modals.",
                  "serviceBound": true,
                  "screenStackBound": true,
                  "modalStackBound": true,
                  "inputRouterBound": true,
                  "themeRuntimeBound": true,
                  "activeScreen": "%s",
                  "screenStackSize": %d,
                  "modalStackSize": %d,
                  "activeTheme": "%s",
                  "terminalHistoryCount": %d,
                  "terminalLineCount": %d,
                  "frameTheme": "%s"
                }
                """.formatted(
                escape(finalFrame.screen().id()),
                ui.screenStack().size(),
                ui.modalStack().size(),
                escape(ui.themeRuntime().activeTheme().id()),
                shell.history().size(),
                shell.outputLines().size(),
                escape(finalFrame.theme().id())
        ));

        write(root.resolve("ui-screen-stack.json"), """
                {
                  "schema": "echo.standalone.ui_screen_stack.v2",
                  "status": "PASS",
                  "initialScreen": "terminal",
                  "menuScreen": %s,
                  "bootScreen": %s,
                  "restoredTerminalScreen": %s,
                  "finalScreen": %s,
                  "pushPopRestoredTerminal": %s,
                  "screenStackSize": %d
                }
                """.formatted(
                surfaceJson(menuFrame.screen()),
                surfaceJson(bootFrame.screen()),
                surfaceJson(restoredTerminalFrame.screen()),
                surfaceJson(finalFrame.screen()),
                bootFrame.screen().id().equals("boot") && restoredTerminalFrame.screen().id().equals("terminal"),
                ui.screenStack().size()
        ));

        write(root.resolve("ui-menus.json"), """
                {
                  "schema": "echo.standalone.ui_menus.v2",
                  "status": "PASS",
                  "menuId": "%s",
                  "title": "%s",
                  "optionCount": %d,
                  "enabledCount": %d,
                  "selectedOption": "%s",
                  "options": %s,
                  "renderedDisabledOption": %s,
                  "selectedMenu": %s,
                  "disabledMenu": %s,
                  "enabledActionRouted": %s,
                  "disabledOptionBlocked": %s
                }
                """.formatted(
                escape(mainMenu.id()),
                escape(mainMenu.title()),
                mainMenu.options().size(),
                mainMenu.enabledCount(),
                escape(mainMenu.selectedOption().id()),
                menuOptionsJson(mainMenu.options()),
                menuFrame.screen().lines().stream().anyMatch(line -> line.contains("Extraction [disabled]")),
                inputResultJson(selectedMenu),
                inputResultJson(disabledMenu),
                selectedMenu.effects().contains("menu-action:menu:new_run"),
                disabledMenu.effects().contains("menu-option-disabled:locked_extraction")
        ));

        write(root.resolve("ui-input-router.json"), """
                {
                  "schema": "echo.standalone.ui_input_router.v2",
                  "status": "PASS",
                  "statusCommand": %s,
                  "themeCommand": %s,
                  "echoCommand": %s,
                  "runtimeCommand": %s,
                  "modalConsumed": %s,
                  "modalDismissed": %s,
                  "modalFrame": %s,
                  "modalConsumesScreenInput": %s,
                  "dismissClosesTopModal": %s
                }
                """.formatted(
                inputResultJson(status),
                inputResultJson(theme),
                inputResultJson(echo),
                inputResultJson(ashfallStatus),
                inputResultJson(consumed),
                inputResultJson(dismissed),
                frameJson(modalFrame),
                consumed.effects().contains("modal-consumed:confirm"),
                dismissed.closeTopModal()
        ));

        write(root.resolve("ui-theme-runtime.json"), """
                {
                  "schema": "echo.standalone.ui_theme_runtime.v2",
                  "status": "PASS",
                  "loadedFromMountedAsset": true,
                  "themeId": "%s",
                  "displayName": "%s",
                  "accentColor": "%s",
                  "backgroundColor": "%s",
                  "foregroundColor": "%s",
                  "warningColor": "%s",
                  "fontFamily": "%s",
                  "density": "%s",
                  "tokens": %s,
                  "promptToken": "%s",
                  "activeThemeMatchesFrame": %s
                }
                """.formatted(
                escape(loadedTheme.id()),
                escape(loadedTheme.displayName()),
                escape(loadedTheme.accentColor()),
                escape(loadedTheme.backgroundColor()),
                escape(loadedTheme.foregroundColor()),
                escape(loadedTheme.warningColor()),
                escape(loadedTheme.fontFamily()),
                escape(loadedTheme.density()),
                stringMapJson(loadedTheme.tokens()),
                escape(loadedTheme.tokens().get("terminal.prompt")),
                loadedTheme.id().equals(finalFrame.theme().id())
        ));

        write(root.resolve("ui-terminal-shell.json"), """
                {
                  "schema": "echo.standalone.ui_terminal_shell.v2",
                  "status": "PASS",
                  "prompt": "%s",
                  "historyCount": %d,
                  "history": %s,
                  "outputLineCount": %d,
                  "outputLines": %s,
                  "statusHandled": %s,
                  "themeHandled": %s,
                  "echoHandled": %s,
                  "runtimeCommandHandled": %s,
                  "echoResponsePresent": %s,
                  "runtimeCommandResponsePresent": %s,
                  "blockedModalCommandHidden": %s
                }
                """.formatted(
                escape(loadedTheme.tokens().get("terminal.prompt")),
                shell.history().size(),
                jsonStringArray(shell.history()),
                shell.outputLines().size(),
                jsonStringArray(shell.outputLines()),
                status.handled(),
                theme.effects().contains("terminal-command:theme"),
                echo.effects().contains("terminal-command:echo"),
                ashfallStatus.effects().contains("terminal-command:ashfall_status"),
                shell.outputLines().stream().anyMatch(line -> line.contains("survivor online")),
                shell.outputLines().stream().anyMatch(line -> line.contains("mission=secure_crash_site")),
                shell.outputLines().stream().noneMatch(line -> line.contains("hidden"))
        ));
    }

    private static String frameJson(EchoUiFrame frame) {
        return """
                {
                  "screen": %s,
                  "modals": %s,
                  "themeId": "%s"
                }""".formatted(
                surfaceJson(frame.screen()),
                surfacesJson(frame.modals()),
                escape(frame.theme().id())
        ).strip();
    }

    private static String surfacesJson(List<dev.echo.standalone.runtime.ui.EchoUiSurface> surfaces) {
        return surfaces.stream()
                .map(EchoRuntimeUiSmokeHarness::surfaceJson)
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String surfaceJson(dev.echo.standalone.runtime.ui.EchoUiSurface surface) {
        return """
                {
                  "id": "%s",
                  "title": "%s",
                  "focusPath": "%s",
                  "lineCount": %d,
                  "lines": %s
                }""".formatted(
                escape(surface.id()),
                escape(surface.title()),
                escape(surface.focusPath()),
                surface.lines().size(),
                jsonStringArray(surface.lines())
        ).strip();
    }

    private static String menuOptionsJson(List<EchoMenuOption> options) {
        return options.stream()
                .map(option -> """
                        {
                          "id": "%s",
                          "label": "%s",
                          "action": "%s",
                          "enabled": %s
                        }""".formatted(
                        escape(option.id()),
                        escape(option.label()),
                        escape(option.action()),
                        option.enabled()
                ).strip())
                .collect(java.util.stream.Collectors.joining(",\n", "[\n", "\n]"));
    }

    private static String inputResultJson(EchoUiInputResult result) {
        return """
                {
                  "handled": %s,
                  "handledBy": "%s",
                  "effects": %s,
                  "closeTopModal": %s
                }""".formatted(
                result.handled(),
                escape(result.handledBy()),
                jsonStringArray(result.effects()),
                result.closeTopModal()
        ).strip();
    }

    private static String stringMapJson(java.util.Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + escape(entry.getValue()) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonStringArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
