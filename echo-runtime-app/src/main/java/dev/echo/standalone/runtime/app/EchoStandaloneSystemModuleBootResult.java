package dev.echo.standalone.runtime.app;

public record EchoStandaloneSystemModuleBootResult(
        int moduleDescriptors,
        int adapterCoreCoverageTotal,
        int requiredSystemModules,
        int executableSystemModules,
        boolean adapterCoreRuntimeBridgeActive
) {
    public EchoStandaloneSystemModuleBootResult {
        if (moduleDescriptors < 0
                || adapterCoreCoverageTotal < 0
                || requiredSystemModules < 0
                || executableSystemModules < 0) {
            throw new IllegalArgumentException("system module boot counts must not be negative");
        }
        if (executableSystemModules > requiredSystemModules) {
            throw new IllegalArgumentException("executable system module count exceeds required count");
        }
    }

    public static EchoStandaloneSystemModuleBootResult inactive() {
        return new EchoStandaloneSystemModuleBootResult(0, 0, 0, 0, false);
    }
}
