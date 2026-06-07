package dev.echo.standalone.runtime.ui;

public record EchoMenuOption(
        String id,
        String label,
        String action,
        boolean enabled
) {
    public EchoMenuOption {
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
