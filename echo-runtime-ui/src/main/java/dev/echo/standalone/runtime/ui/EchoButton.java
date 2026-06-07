package dev.echo.standalone.runtime.ui;

public record EchoButton(
        String id,
        String label,
        String action
) {
    public EchoButton {
        id = requireText(id, "id");
        label = requireText(label, "label");
        action = requireText(action, "action");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
