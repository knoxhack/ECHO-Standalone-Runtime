package dev.echo.standalone.runtime.client;

record EchoClientSubtitleLine(
        String clipId,
        String text,
        double remainingSeconds
) {
    EchoClientSubtitleLine {
        clipId = text(clipId, "clipId");
        text = text(text, "text");
        remainingSeconds = Math.max(0.0D, Double.isFinite(remainingSeconds) ? remainingSeconds : 0.0D);
    }

    private static String text(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
