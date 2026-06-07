package dev.echo.standalone.runtime.client;

record EchoClientScreenOption(
        String label,
        EchoClientScreenCommand command,
        boolean enabled,
        String tooltip,
        EchoClientScreenOptionKind kind,
        String valueText,
        double sliderValue,
        boolean active,
        String targetId
) {
    EchoClientScreenOption(String label, EchoClientScreenCommand command, boolean enabled) {
        this(label, command, enabled, "");
    }

    EchoClientScreenOption(String label, EchoClientScreenCommand command, boolean enabled, String tooltip) {
        this(label, command, enabled, tooltip, EchoClientScreenOptionKind.BUTTON, "", 0.0D, false, "");
    }

    static EchoClientScreenOption target(
            String label,
            EchoClientScreenCommand command,
            String targetId,
            String tooltip
    ) {
        return target(label, command, targetId, tooltip, true);
    }

    static EchoClientScreenOption target(
            String label,
            EchoClientScreenCommand command,
            String targetId,
            String tooltip,
            boolean enabled
    ) {
        return new EchoClientScreenOption(
                label,
                command,
                enabled,
                tooltip,
                EchoClientScreenOptionKind.BUTTON,
                "",
                0.0D,
                false,
                targetId
        );
    }

    static EchoClientScreenOption toggle(String label, boolean value, String tooltip) {
        return new EchoClientScreenOption(
                label,
                EchoClientScreenCommand.NONE,
                true,
                tooltip,
                EchoClientScreenOptionKind.TOGGLE,
                value ? "ON" : "OFF",
                value ? 1.0D : 0.0D,
                value,
                ""
        );
    }

    static EchoClientScreenOption slider(String label, int value, int min, int max, String tooltip) {
        int clamped = Math.max(min, Math.min(max, value));
        double percent = max <= min ? 0.0D : (clamped - min) / (double) (max - min);
        return new EchoClientScreenOption(
                label,
                EchoClientScreenCommand.NONE,
                true,
                tooltip,
                EchoClientScreenOptionKind.SLIDER,
                Integer.toString(clamped),
                percent,
                false,
                ""
        );
    }

    static EchoClientScreenOption text(String label, String value, boolean editing, String tooltip) {
        return new EchoClientScreenOption(
                label,
                EchoClientScreenCommand.NONE,
                true,
                tooltip,
                EchoClientScreenOptionKind.TEXT,
                value == null ? "" : value,
                0.0D,
                editing,
                ""
        );
    }

    EchoClientScreenOption {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label must not be blank");
        }
        if (command == null) {
            command = EchoClientScreenCommand.NONE;
        }
        tooltip = tooltip == null ? "" : tooltip.trim();
        if (kind == null) {
            kind = EchoClientScreenOptionKind.BUTTON;
        }
        valueText = valueText == null ? "" : valueText;
        sliderValue = Math.max(0.0D, Math.min(1.0D, sliderValue));
        targetId = targetId == null ? "" : targetId.trim();
    }
}
