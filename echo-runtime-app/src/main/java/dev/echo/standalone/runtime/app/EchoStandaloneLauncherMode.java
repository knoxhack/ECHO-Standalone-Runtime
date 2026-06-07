package dev.echo.standalone.runtime.app;

public enum EchoStandaloneLauncherMode {
    STANDALONE_RUNTIME("standalone-runtime", true),
    PLATFORM_HANDOFF("platform-handoff", false);

    private final String id;
    private final boolean launchesStandalone;

    EchoStandaloneLauncherMode(String id, boolean launchesStandalone) {
        this.id = id;
        this.launchesStandalone = launchesStandalone;
    }

    public String id() {
        return id;
    }

    public boolean launchesStandalone() {
        return launchesStandalone;
    }
}
