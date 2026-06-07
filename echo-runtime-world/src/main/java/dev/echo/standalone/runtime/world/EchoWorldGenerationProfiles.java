package dev.echo.standalone.runtime.world;

public final class EchoWorldGenerationProfiles {
    private EchoWorldGenerationProfiles() {
    }

    public static EchoWorldGenerationSettings ashfallCrashSite() {
        return new EchoWorldGenerationSettings(
                "ashfall-debug-world",
                1409L,
                4,
                "ashfall:crash_site",
                "ashfall:surface",
                EchoWorldDebugProfile.ashfallCrashSite()
        );
    }
}
