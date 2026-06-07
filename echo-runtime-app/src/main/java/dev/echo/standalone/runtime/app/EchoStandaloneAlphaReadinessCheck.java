package dev.echo.standalone.runtime.app;

public record EchoStandaloneAlphaReadinessCheck(
        String checkId,
        String category,
        boolean passed,
        boolean blocking,
        String detail
) {
    public EchoStandaloneAlphaReadinessCheck {
        checkId = requireText(checkId, "checkId");
        category = requireText(category, "category");
        detail = requireText(detail, "detail");
    }

    public boolean blocked() {
        return blocking && !passed;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
