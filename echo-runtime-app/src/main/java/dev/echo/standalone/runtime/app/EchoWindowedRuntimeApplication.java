package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntime;
import dev.echo.standalone.runtime.contracts.EchoRuntimeApplication;
import dev.echo.standalone.runtime.contracts.EchoRuntimeContext;
import dev.echo.standalone.runtime.render.EchoRenderBackend;
import dev.echo.standalone.runtime.render.EchoRenderWindowLifecycleController;
import dev.echo.standalone.runtime.render.EchoSoftwareRenderBackend;

public final class EchoWindowedRuntimeApplication implements EchoRuntimeApplication {
    @Override
    public String applicationId() {
        return "echo-windowed-runtime";
    }

    @Override
    public EchoRuntime createRuntime(EchoRuntimeContext context) {
        EchoRuntimeLifecycleManager lifecycleManager = context.services().require(EchoRuntimeLifecycleManager.class);
        EchoRuntimeShutdownController shutdownController = context.services().require(EchoRuntimeShutdownController.class);
        if (Boolean.parseBoolean(context.configuration().properties().getOrDefault("echo.window.live", "false"))) {
            return new EchoLiveWindowRuntime(context, lifecycleManager, shutdownController);
        }
        EchoRenderBackend backend = new EchoSoftwareRenderBackend();
        EchoRenderWindowLifecycleController controller = new EchoRenderWindowLifecycleController(backend);
        context.services().register(EchoRenderBackend.class, backend);
        context.services().register(EchoRenderWindowLifecycleController.class, controller);
        return new EchoWindowedRuntime(context, lifecycleManager, shutdownController, backend, controller);
    }
}
