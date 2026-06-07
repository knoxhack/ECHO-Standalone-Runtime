package dev.echo.standalone.runtime.client;

record EchoClientModalSnapshot(
        boolean visible,
        String title,
        String message,
        String confirmLabel,
        String cancelLabel,
        boolean confirmSelected
) {
    static final EchoClientModalSnapshot EMPTY =
            new EchoClientModalSnapshot(false, "", "", "Confirm", "Cancel", true);

    EchoClientModalSnapshot {
        title = title == null ? "" : title.trim();
        message = message == null ? "" : message.trim();
        confirmLabel = blankTo(confirmLabel, "Confirm");
        cancelLabel = blankTo(cancelLabel, "Cancel");
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
