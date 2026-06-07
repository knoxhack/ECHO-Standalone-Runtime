package dev.echo.standalone.runtime.input;

public final class EchoInputFocusState {
    private EchoInputContext activeContext = EchoInputContext.GAMEPLAY;
    private String focusPath = "gameplay:world";

    public synchronized EchoInputContext activeContext() {
        return activeContext;
    }

    public synchronized String focusPath() {
        return focusPath;
    }

    public synchronized boolean terminalFocused() {
        return activeContext == EchoInputContext.TERMINAL;
    }

    public synchronized void focusGameplay() {
        activeContext = EchoInputContext.GAMEPLAY;
        focusPath = "gameplay:world";
    }

    public synchronized void focusUi(String focusPath) {
        activeContext = EchoInputContext.UI;
        this.focusPath = EchoInputText.normalize(focusPath);
    }

    public synchronized void focusTerminal(String focusPath) {
        activeContext = EchoInputContext.TERMINAL;
        String normalized = EchoInputText.normalize(focusPath);
        this.focusPath = normalized.isBlank() ? "terminal:input" : normalized;
    }
}
