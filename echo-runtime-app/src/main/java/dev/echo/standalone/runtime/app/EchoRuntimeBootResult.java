package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnostic;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;

import java.util.List;
import java.util.Objects;

public record EchoRuntimeBootResult(
        EchoRuntimeExitCode exitCode,
        EchoRuntimeLifecycle finalLifecycle,
        List<EchoRuntimeLifecycle> lifecycleTrace,
        List<EchoRuntimeDiagnostic> diagnostics,
        int ticksRun,
        boolean crashHandled,
        EchoRuntimeShutdownHook shutdownHook,
        EchoStandaloneSystemModuleBootResult systemModuleBoot,
        boolean ashfallFirstPlayableLoopReady,
        boolean liveWindowWalkthroughReady,
        String liveWindowWalkthroughSummary
) {
    public EchoRuntimeBootResult {
        Objects.requireNonNull(exitCode, "exitCode");
        Objects.requireNonNull(finalLifecycle, "finalLifecycle");
        Objects.requireNonNull(lifecycleTrace, "lifecycleTrace");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(shutdownHook, "shutdownHook");
        Objects.requireNonNull(systemModuleBoot, "systemModuleBoot");
        liveWindowWalkthroughSummary = EchoAppText.requireText(
                liveWindowWalkthroughSummary,
                "liveWindowWalkthroughSummary"
        );
        lifecycleTrace = List.copyOf(lifecycleTrace);
        diagnostics = List.copyOf(diagnostics);
        if (ticksRun < 0) {
            throw new IllegalArgumentException("ticksRun must not be negative");
        }
    }

    public boolean success() {
        return exitCode == EchoRuntimeExitCode.SUCCESS;
    }
}
