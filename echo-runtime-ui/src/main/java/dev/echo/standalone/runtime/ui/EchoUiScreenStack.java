package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoUiScreenStack {
    private final ArrayList<EchoUiScreen> screens = new ArrayList<>();

    public synchronized void push(EchoUiScreen screen) {
        screens.add(Objects.requireNonNull(screen, "screen"));
    }

    public synchronized Optional<EchoUiScreen> pop() {
        if (screens.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(screens.removeLast());
    }

    public synchronized void replace(EchoUiScreen screen) {
        Objects.requireNonNull(screen, "screen");
        if (!screens.isEmpty()) {
            screens.removeLast();
        }
        screens.add(screen);
    }

    public synchronized Optional<EchoUiScreen> current() {
        if (screens.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(screens.getLast());
    }

    public synchronized List<EchoUiScreen> snapshot() {
        return List.copyOf(screens);
    }

    public synchronized int size() {
        return screens.size();
    }
}
