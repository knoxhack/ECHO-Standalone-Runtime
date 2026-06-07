package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.render.EchoRenderCommand;
import dev.echo.standalone.runtime.render.EchoRenderUiBridge;
import dev.echo.standalone.runtime.ui.EchoMenuDefinition;
import dev.echo.standalone.runtime.ui.EchoMenuOption;
import dev.echo.standalone.runtime.ui.EchoMenuScreen;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiFrame;
import dev.echo.standalone.runtime.ui.EchoUiInputEvent;
import dev.echo.standalone.runtime.ui.EchoUiInputResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiScreen;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.util.ArrayList;
import java.util.List;

final class EchoClientUiBridge {
    private final EchoUiRuntimeResult runtime;
    private final EchoRenderUiBridge renderBridge = new EchoRenderUiBridge();
    private long inputSequence;

    EchoClientUiBridge() {
        runtime = new EchoUiRuntime().boot(
                new EchoDefaultRuntimeServiceRegistry(),
                new EchoStaticScreen("echoscreencore:boot", "ECHO", List.of("Booting ScreenCore"), "boot"),
                EchoUiTheme.defaultTerminal()
        );
    }

    void showStatic(String screenId, String title, List<String> lines, String focusPath) {
        runtime.screenStack().replace(new EchoStaticScreen(screenId, title, lines, focusPath));
    }

    void showMenu(
            String screenId,
            String title,
            List<EchoClientScreenOption> options,
            int selectedIndex,
            List<String> contextLines
    ) {
        ArrayList<EchoMenuOption> menuOptions = new ArrayList<>();
        for (EchoClientScreenOption option : options) {
            String action = option.command().name();
            String id = option.targetId().isBlank()
                    ? action.toLowerCase(java.util.Locale.ROOT)
                    : option.targetId();
            menuOptions.add(new EchoMenuOption(id, option.label(), action, option.enabled()));
        }
        EchoMenuScreen menu = new EchoMenuScreen(new EchoMenuDefinition(screenId, title, menuOptions, selectedIndex));
        runtime.screenStack().replace(new ClientMenuScreen(menu, contextLines));
    }

    EchoUiInputResult dispatchCommand(EchoClientScreenCommand command) {
        return runtime.dispatch(EchoUiInputEvent.command(inputSequence++, command.name()));
    }

    EchoUiFrame frame() {
        return runtime.frame();
    }

    List<EchoRenderCommand> renderCommands() {
        return renderBridge.commands(frame());
    }

    private record ClientMenuScreen(EchoMenuScreen menu, List<String> contextLines) implements EchoUiScreen {
        private ClientMenuScreen {
            contextLines = contextLines == null ? List.of() : List.copyOf(contextLines);
        }

        @Override
        public String id() {
            return menu.id();
        }

        @Override
        public String title() {
            return menu.title();
        }

        @Override
        public dev.echo.standalone.runtime.ui.EchoUiSurface render(dev.echo.standalone.runtime.ui.EchoUiContext context) {
            dev.echo.standalone.runtime.ui.EchoUiSurface surface = menu.render(context);
            ArrayList<String> lines = new ArrayList<>(contextLines);
            lines.addAll(surface.lines());
            return new dev.echo.standalone.runtime.ui.EchoUiSurface(surface.id(), surface.title(), lines, surface.focusPath());
        }

        @Override
        public EchoUiInputResult handleInput(EchoUiInputEvent event, dev.echo.standalone.runtime.ui.EchoUiContext context) {
            return menu.handleInput(event, context);
        }
    }
}
