package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;

public final class EchoPlayableBetaRuntimeApplication implements EchoRuntimeApplication {
    @Override
    public String applicationId() {
        return "echo-playable-beta-runtime";
    }

    @Override
    public EchoRuntime createRuntime(EchoRuntimeContext context) {
        EchoRuntimeLifecycleManager lifecycleManager = context.services().require(EchoRuntimeLifecycleManager.class);
        EchoRuntimeShutdownController shutdownController = context.services().require(EchoRuntimeShutdownController.class);
        return new EchoPlayableBetaRuntime(context, lifecycleManager, shutdownController);
    }
}
