package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public final class EchoTerminalScreen implements EchoUiScreen {
    private final String id;
    private final String title;
    private final EchoTerminalShell shell;

    public EchoTerminalScreen(String id, String title, EchoTerminalShell shell) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.id = id;
        this.title = title;
        this.shell = Objects.requireNonNull(shell, "shell");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    public EchoTerminalShell shell() {
        return shell;
    }

    @Override
    public EchoUiSurface render(EchoUiContext context) {
        return new EchoUiSurface(id, title, shell.outputLines(), "terminal:input");
    }

    @Override
    public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
        if (event.kind() != EchoUiInputKind.COMMAND && event.kind() != EchoUiInputKind.TEXT) {
            return EchoUiInputResult.ignored(id);
        }
        EchoTerminalCommandResult commandResult = shell.submit(event.value(), context.theme());
        String commandName = shell.history().isEmpty()
                ? ""
                : shell.history().getLast().split("\\s+")[0];
        return EchoUiInputResult.handled(id, List.of(
                "terminal-command:" + commandName,
                "terminal-lines:" + shell.outputLines().size(),
                "terminal-close-requested:" + commandResult.closeRequested()
        ));
    }
}
