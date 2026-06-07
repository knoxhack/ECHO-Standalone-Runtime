package dev.echo.standalone.runtime.app;

import java.nio.file.Path;
import java.util.Objects;

public record EchoStandaloneLauncherDetection(
        Path workspaceRoot,
        boolean settingsFilePresent,
        boolean buildFilePresent,
        boolean docsRootPresent,
        boolean reportsRootPresent,
        String runtimeVersion
) {
    public EchoStandaloneLauncherDetection {
        Objects.requireNonNull(workspaceRoot, "workspaceRoot");
        runtimeVersion = requireText(runtimeVersion, "runtimeVersion");
    }

    public boolean standaloneWorkspace() {
        return settingsFilePresent && buildFilePresent && docsRootPresent && reportsRootPresent;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
