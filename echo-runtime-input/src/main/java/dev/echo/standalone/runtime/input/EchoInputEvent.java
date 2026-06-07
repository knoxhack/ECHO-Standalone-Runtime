package dev.echo.standalone.runtime.input;

import java.util.Objects;

public record EchoInputEvent(
        long sequence,
        EchoInputControl control,
        EchoInputEventType eventType,
        double value,
        String text
) {
    public EchoInputEvent {
        Objects.requireNonNull(control, "control");
        Objects.requireNonNull(eventType, "eventType");
        text = text == null ? "" : text;
        if (sequence < 0) {
            throw new IllegalArgumentException("sequence must not be negative");
        }
    }

    public static EchoInputEvent press(long sequence, EchoInputControl control) {
        return new EchoInputEvent(sequence, control, EchoInputEventType.PRESSED, 1.0D, "");
    }

    public static EchoInputEvent release(long sequence, EchoInputControl control) {
        return new EchoInputEvent(sequence, control, EchoInputEventType.RELEASED, 0.0D, "");
    }

    public static EchoInputEvent axis(long sequence, EchoInputControl control, double value) {
        return new EchoInputEvent(sequence, control, EchoInputEventType.AXIS, value, "");
    }

    public static EchoInputEvent text(long sequence, String text) {
        return new EchoInputEvent(
                sequence,
                EchoInputControl.keyboard("TEXT"),
                EchoInputEventType.TEXT,
                1.0D,
                text
        );
    }

    public boolean active() {
        return eventType == EchoInputEventType.PRESSED
                || eventType == EchoInputEventType.TEXT
                || (eventType == EchoInputEventType.AXIS && Math.abs(value) >= 0.5D);
    }
}
