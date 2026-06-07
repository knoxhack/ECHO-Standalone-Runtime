package dev.echo.standalone.runtime.ui;

public record EchoInputAction(
        String id,
        String key,
        String command
) {
    public EchoInputAction {
        id = requireText(id, "id");
        key = requireText(key, "key");
        command = requireText(command, "command");
    }

    public EchoUiInputEvent event(long sequence) {
        return EchoUiInputEvent.command(sequence, command);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
