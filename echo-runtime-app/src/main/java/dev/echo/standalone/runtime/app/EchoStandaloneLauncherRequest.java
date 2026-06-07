package dev.echo.standalone.runtime.app;

import java.nio.file.Path;
import java.util.Objects;

public record EchoStandaloneLauncherRequest(
        EchoStandaloneLauncherMode mode,
        Path workspaceRoot,
        boolean allowLaunch,
        boolean includeSupportBundle
) {
    public EchoStandaloneLauncherRequest {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        workspaceRoot = workspaceRoot.toAbsolutePath().normalize();
    }

    public static EchoStandaloneLauncherRequest standalone(Path workspaceRoot) {
        return new EchoStandaloneLauncherRequest(
                EchoStandaloneLauncherMode.STANDALONE_RUNTIME,
                workspaceRoot,
                true,
                true
        );
    }

    public static EchoStandaloneLauncherRequest verifyOnly(Path workspaceRoot) {
        return new EchoStandaloneLauncherRequest(
                EchoStandaloneLauncherMode.STANDALONE_RUNTIME,
                workspaceRoot,
                false,
                true
        );
    }

    public static EchoStandaloneLauncherRequest platformHandoff(Path workspaceRoot) {
        return new EchoStandaloneLauncherRequest(
                EchoStandaloneLauncherMode.PLATFORM_HANDOFF,
                workspaceRoot,
                true,
                false
        );
    }
}
