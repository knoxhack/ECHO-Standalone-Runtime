package dev.echo.standalone.runtime.ui;

import java.util.Objects;

public record EchoUiInputEvent(
        long sequence,
        EchoUiInputKind kind,
        String targetId,
        String value
) {
    public EchoUiInputEvent {
        Objects.requireNonNull(kind, "kind");
        targetId = normalize(targetId);
        value = value == null ? "" : value;
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
    }

    public static EchoUiInputEvent command(long sequence, String value) {
        return new EchoUiInputEvent(sequence, EchoUiInputKind.COMMAND, "", value);
    }

    public static EchoUiInputEvent text(long sequence, String value) {
        return new EchoUiInputEvent(sequence, EchoUiInputKind.TEXT, "", value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
