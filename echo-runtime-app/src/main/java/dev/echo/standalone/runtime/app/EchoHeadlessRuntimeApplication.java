package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;

public final class EchoHeadlessRuntimeApplication implements EchoRuntimeApplication {
    private final int maxTicks;

    public EchoHeadlessRuntimeApplication(int maxTicks) {
        if (maxTicks < 0) {
            throw new IllegalArgumentException("maxTicks must not be negative");
        }
        this.maxTicks = maxTicks;
    }

    @Override
    public String applicationId() {
        return "echo-headless-app-runtime";
    }

    @Override
    public EchoRuntime createRuntime(EchoRuntimeContext context) {
        EchoRuntimeLifecycleManager lifecycleManager = context.services().require(EchoRuntimeLifecycleManager.class);
        EchoFixedStepRuntimeClock clock = context.services().require(EchoFixedStepRuntimeClock.class);
        EchoRuntimeShutdownController shutdownController = context.services().require(EchoRuntimeShutdownController.class);
        EchoHeadlessRuntimeTickLoop tickLoop = new EchoHeadlessRuntimeTickLoop(clock, maxTicks, context.configuration().properties());
        context.services().register(EchoHeadlessRuntimeTickLoop.class, tickLoop);
        return new EchoHeadlessRuntime(context, lifecycleManager, tickLoop, shutdownController);
    }
}
