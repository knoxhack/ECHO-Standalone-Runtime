package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoMenuScreen implements EchoUiScreen {
    private final EchoMenuDefinition menu;

    public EchoMenuScreen(EchoMenuDefinition menu) {
        this.menu = Objects.requireNonNull(menu, "menu");
    }

    @Override
    public String id() {
        return menu.id();
    }

    @Override
    public String title() {
        return menu.title();
    }

    public EchoMenuDefinition menu() {
        return menu;
    }

    @Override
    public EchoUiSurface render(EchoUiContext context) {
        ArrayList<String> lines = new ArrayList<>();
        for (int index = 0; index < menu.options().size(); index++) {
            EchoMenuOption option = menu.options().get(index);
            String marker = index == menu.selectedIndex() ? ">" : " ";
            String state = option.enabled() ? "" : " [disabled]";
            lines.add(marker + " " + option.label() + state);
        }
        return new EchoUiSurface(menu.id(), menu.title(), lines, menu.selectedOption().id());
    }

    @Override
    public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
        if (event.kind() != EchoUiInputKind.COMMAND) {
            return EchoUiInputResult.ignored(menu.id());
        }
        return menu.option(event.value())
                .map(option -> option.enabled()
                        ? EchoUiInputResult.handled(menu.id(), List.of(
                        "menu-option:" + option.id(),
                        "menu-action:" + option.action()
                ))
                        : EchoUiInputResult.handled(menu.id(), List.of(
                        "menu-option-disabled:" + option.id()
                )))
                .orElseGet(() -> EchoUiInputResult.ignored(menu.id()));
    }
}
