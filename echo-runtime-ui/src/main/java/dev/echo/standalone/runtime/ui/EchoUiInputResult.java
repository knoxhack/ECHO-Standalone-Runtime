package dev.echo.standalone.runtime.ui;

import java.util.List;
import java.util.Objects;

public record EchoUiInputResult(
        boolean handled,
        String handledBy,
        List<String> effects,
        boolean closeTopModal
) {
    public EchoUiInputResult {
        handledBy = handledBy == null ? "" : handledBy;
        Objects.requireNonNull(effects, "effects");
        effects = List.copyOf(effects);
    }

    public static EchoUiInputResult ignored(String handledBy) {
        return new EchoUiInputResult(false, handledBy, List.of(), false);
    }

    public static EchoUiInputResult handled(String handledBy, List<String> effects) {
        return new EchoUiInputResult(true, handledBy, effects, false);
    }

    public static EchoUiInputResult closeModal(String handledBy, List<String> effects) {
        return new EchoUiInputResult(true, handledBy, effects, true);
    }
}
