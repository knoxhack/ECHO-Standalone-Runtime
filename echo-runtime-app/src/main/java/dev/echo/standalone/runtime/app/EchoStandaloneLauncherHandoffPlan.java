package dev.echo.standalone.runtime.app;

public record EchoStandaloneLauncherHandoffPlan(
        String modeId,
        String runtimeFamily,
        String rendererTarget,
        String standaloneLaunchTask,
        String externalCommand,
        boolean launchesStandalone,
        boolean externalCommandPreserved
) {
    private static final String STANDALONE_CLIENT_TASK = ":echo-runtime-client:run";

    public EchoStandaloneLauncherHandoffPlan {
        modeId = text(modeId, "unknown-mode");
        runtimeFamily = text(runtimeFamily, "unknown-runtime");
        rendererTarget = text(rendererTarget, "unknown-renderer");
        standaloneLaunchTask = text(standaloneLaunchTask, "");
        externalCommand = text(externalCommand, "");
    }

    public static EchoStandaloneLauncherHandoffPlan forMode(EchoStandaloneLauncherMode mode) {
        EchoStandaloneLauncherMode safeMode = mode == null
                ? EchoStandaloneLauncherMode.STANDALONE_RUNTIME
                : mode;
        return switch (safeMode) {
            case STANDALONE_RUNTIME -> new EchoStandaloneLauncherHandoffPlan(
                    safeMode.id(),
                    "echo-standalone-client",
                    "opengl",
                    STANDALONE_CLIENT_TASK,
                    "",
                    true,
                    false
            );
            case PLATFORM_HANDOFF -> new EchoStandaloneLauncherHandoffPlan(
                    safeMode.id(),
                    "external-platform",
                    "external-platform",
                    "",
                    "external platform launcher",
                    false,
                    true
            );
        };
    }

    public boolean standaloneOpenGlClientTarget() {
        return launchesStandalone
                && rendererTarget.equals("opengl")
                && standaloneLaunchTask.equals(STANDALONE_CLIENT_TASK)
                && externalCommand.isBlank();
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
