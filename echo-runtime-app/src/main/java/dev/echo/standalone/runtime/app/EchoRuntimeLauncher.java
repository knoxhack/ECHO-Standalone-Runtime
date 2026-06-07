package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeConfiguration;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.contracts.EchoRuntimeDiagnosticSink;
import dev.echo.standalone.runtime.contracts.EchoRuntimeEnvironment;
import dev.echo.standalone.runtime.contracts.EchoRuntimeLifecycle;
import dev.echo.standalone.runtime.contracts.EchoRuntimePlatform;
import dev.echo.standalone.runtime.contracts.EchoRuntimeShutdownHook;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.core.EchoRuntimeDiagnosticCollector;

import java.util.Objects;

public final class EchoRuntimeLauncher {
    private static final String RUNTIME_VERSION = "0.1.0-phase14.20-alpha-readiness";

    public EchoRuntimeBootResult launch(EchoRuntimeBootContext bootContext) {
        Objects.requireNonNull(bootContext, "bootContext");

        EchoRuntimeLogBridge logBridge = new EchoRuntimeLogBridge();
        EchoRuntimeLifecycleManager lifecycleManager = new EchoRuntimeLifecycleManager(EchoRuntimeLifecycle.CREATED, logBridge);
        EchoRuntimeCrashHandler crashHandler = new EchoRuntimeCrashHandler(logBridge);
        EchoRuntimeShutdownController shutdownController = new EchoRuntimeShutdownController();
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoFixedStepRuntimeClock clock = new EchoFixedStepRuntimeClock(bootContext.bootInstant(), bootContext.tickBudget());

        EchoRuntimeConfiguration configuration = new EchoRuntimeConfiguration(
                RUNTIME_VERSION,
                bootContext.mode(),
                bootContext.mode().id().contains("headless"),
                true,
                bootContext.properties()
        );
        EchoRuntimeEnvironment environment = new EchoRuntimeEnvironment(
                bootContext.runtimeId(),
                bootContext.mode(),
                EchoRuntimeLifecycle.CREATED,
                bootContext.workspaceRoot(),
                bootContext.reportsRoot(),
                true
        );
        EchoRuntimeContext runtimeContext = new EchoRuntimeContext(
                environment,
                configuration,
                services,
                logBridge,
                clock,
                bootContext.capabilities(),
                EchoRuntimePlatform.standaloneEcho()
        );

        services.register(EchoRuntimeLifecycleManager.class, lifecycleManager);
        services.register(EchoRuntimeCrashHandler.class, crashHandler);
        services.register(EchoRuntimeLogBridge.class, logBridge);
        services.register(EchoRuntimeDiagnosticCollector.class, logBridge);
        services.register(EchoRuntimeDiagnosticSink.class, logBridge);
        services.register(EchoRuntimeShutdownController.class, shutdownController);
        services.register(EchoFixedStepRuntimeClock.class, clock);

        lifecycleManager.transition(EchoRuntimeLifecycle.BOOTSTRAPPING);
        lifecycleManager.transition(EchoRuntimeLifecycle.LOADING_CONFIG);
        lifecycleManager.transition(EchoRuntimeLifecycle.INITIALIZING_SERVICES);

        EchoRuntimeCrashReport<EchoStandaloneSystemModuleBootResult> systemModuleReport = crashHandler.guardReport(
                "boot-system-modules",
                () -> new EchoStandaloneSystemModuleBootRuntime().boot(runtimeContext)
        );
        if (!systemModuleReport.success()) {
            lifecycleManager.transition(EchoRuntimeLifecycle.CRASHED);
            return result(EchoRuntimeExitCode.CRASHED, lifecycleManager, logBridge, services, true, shutdownController);
        }

        EchoRuntimeCrashReport<Void> configureReport = crashHandler.guardReport(
                "configure-application",
                () -> {
                    bootContext.application().configure(runtimeContext);
                    return null;
                }
        );
        if (!configureReport.success()) {
            lifecycleManager.transition(EchoRuntimeLifecycle.CRASHED);
            return result(EchoRuntimeExitCode.CRASHED, lifecycleManager, logBridge, services, true, shutdownController);
        }

        EchoRuntimeCrashReport<EchoRuntime> runtimeReport = crashHandler.guardReport(
                "create-runtime",
                () -> bootContext.application().createRuntime(runtimeContext)
        );
        if (!runtimeReport.success()) {
            lifecycleManager.transition(EchoRuntimeLifecycle.CRASHED);
            return result(EchoRuntimeExitCode.CRASHED, lifecycleManager, logBridge, services, true, shutdownController);
        }

        EchoRuntime runtime = runtimeReport.value();
        EchoRuntimeCrashReport<Void> startReport = crashHandler.guardReport(
                "start-runtime",
                () -> {
                    runtime.start();
                    return null;
                }
        );
        if (!startReport.success()) {
            lifecycleManager.transition(EchoRuntimeLifecycle.CRASHED);
            return result(EchoRuntimeExitCode.CRASHED, lifecycleManager, logBridge, services, true, shutdownController);
        }

        return result(EchoRuntimeExitCode.SUCCESS, lifecycleManager, logBridge, services, false, shutdownController);
    }

    private static EchoRuntimeBootResult result(
            EchoRuntimeExitCode exitCode,
            EchoRuntimeLifecycleManager lifecycleManager,
            EchoRuntimeLogBridge logBridge,
            EchoDefaultRuntimeServiceRegistry services,
            boolean crashHandled,
            EchoRuntimeShutdownController shutdownController
    ) {
        int ticksRun = services.find(EchoHeadlessRuntimeTickLoop.class)
                .map(EchoHeadlessRuntimeTickLoop::ticksRun)
                .orElse(0);
        EchoRuntimeShutdownHook hook = shutdownController.shutdownHook()
                .orElse(EchoRuntimeShutdownHook.noop("runtime_not_started"));
        EchoStandaloneSystemModuleBootResult systemModuleBoot = services.find(EchoStandaloneSystemModuleBootResult.class)
                .orElse(EchoStandaloneSystemModuleBootResult.inactive());
        boolean ashfallFirstPlayableLoopReady = services.find(EchoStandalonePlayableLoopResult.class)
                .map(EchoStandalonePlayableLoopResult::ready)
                .orElse(false);
        EchoStandaloneLiveWindowWalkthroughResult liveWindowWalkthrough = services
                .find(EchoStandaloneLiveWindowWalkthroughResult.class)
                .orElse(null);
        return new EchoRuntimeBootResult(
                exitCode,
                lifecycleManager.current(),
                lifecycleManager.trace(),
                logBridge.diagnostics(),
                ticksRun,
                crashHandled,
                hook,
                systemModuleBoot,
                ashfallFirstPlayableLoopReady,
                liveWindowWalkthrough != null && liveWindowWalkthrough.ready(),
                liveWindowWalkthrough == null ? "not recorded" : liveWindowWalkthrough.summary()
        );
    }
}
