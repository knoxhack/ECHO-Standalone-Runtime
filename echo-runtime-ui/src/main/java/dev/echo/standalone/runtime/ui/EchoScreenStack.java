package dev.echo.standalone.runtime.ui;

import java.util.Optional;

public final class EchoScreenStack {
    private final EchoUiScreenStack delegate = new EchoUiScreenStack();

    public void push(EchoScreen screen) {
        delegate.push(screen);
    }

    public Optional<EchoScreen> pop() {
        return delegate.pop().map(EchoScreen.class::cast);
    }

    public void replace(EchoScreen screen) {
        delegate.replace(screen);
    }

    public Optional<EchoScreen> current() {
        return delegate.current().map(EchoScreen.class::cast);
    }

    public int size() {
        return delegate.size();
    }
}
