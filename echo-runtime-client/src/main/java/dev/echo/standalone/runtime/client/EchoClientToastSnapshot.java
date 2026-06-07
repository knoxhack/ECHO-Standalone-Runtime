package dev.echo.standalone.runtime.client;

record EchoClientToastSnapshot(
        boolean visible,
        String message,
        double progress
) {
    static final EchoClientToastSnapshot EMPTY = new EchoClientToastSnapshot(false, "", 0.0D);

    EchoClientToastSnapshot {
        message = message == null ? "" : message.trim();
        progress = Math.max(0.0D, Math.min(1.0D, progress));
        visible = visible && !message.isBlank();
    }
}
