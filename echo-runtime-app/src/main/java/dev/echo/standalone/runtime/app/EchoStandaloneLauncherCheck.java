package dev.echo.standalone.runtime.app;

public record EchoStandaloneLauncherCheck(String checkId, boolean passed, String detail) {
    public EchoStandaloneLauncherCheck {
        checkId = requireText(checkId, "checkId");
        detail = requireText(detail, "detail");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
