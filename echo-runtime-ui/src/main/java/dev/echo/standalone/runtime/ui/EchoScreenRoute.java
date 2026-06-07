package dev.echo.standalone.runtime.ui;

public record EchoScreenRoute(
        String screenId,
        String route,
        String focusPath
) {
    public EchoScreenRoute {
        screenId = requireText(screenId, "screenId");
        route = requireText(route, "route");
        focusPath = focusPath == null ? "" : focusPath;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
