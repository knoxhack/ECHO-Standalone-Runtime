package dev.echo.standalone.runtime.ui;

import java.util.List;

public record EchoBasicModal(
        String id,
        String title,
        List<String> lines,
        boolean blocking
) implements EchoUiModal {
    public EchoBasicModal {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        lines = List.copyOf(lines);
    }

    @Override
    public EchoUiSurface render(EchoUiContext context) {
        return new EchoUiSurface(id, title, lines, "modal:" + id);
    }

    @Override
    public EchoUiInputResult handleInput(EchoUiInputEvent event, EchoUiContext context) {
        if (event.kind() == EchoUiInputKind.COMMAND && "dismiss".equalsIgnoreCase(event.value().trim())) {
            return EchoUiInputResult.closeModal(id, List.of("modal-dismissed:" + id));
        }
        if (blocking) {
            return EchoUiInputResult.handled(id, List.of("modal-consumed:" + id));
        }
        return EchoUiInputResult.ignored(id);
    }
}
