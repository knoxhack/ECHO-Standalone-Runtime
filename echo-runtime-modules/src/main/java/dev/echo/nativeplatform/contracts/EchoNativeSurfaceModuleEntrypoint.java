package dev.echo.nativeplatform.contracts;

import java.util.Map;

public interface EchoNativeSurfaceModuleEntrypoint extends EchoNativeModuleEntrypoint {
    Map<String, Object> describeNativeSurfaces(Map<String, String> context);

    @Override
    default void discover(EchoNativeModuleLoadContext context) {
        context.attribute("nativeEntrypointBridge", "surface_module_entrypoint");
        context.attribute("nativeEntrypointDelegateClass", getClass().getName());
        context.attribute("nativeSurfaceEntrypoint", true);
    }

    @Override
    default void registerServices(EchoNativeModuleLoadContext context) {
        Map<String, Object> activation = activation(context);
        EchoNativeActivationSurfaceRegistrar.registerServices(
                context,
                this,
                activation,
                "surface_module_entrypoint",
                "diagnostics"
        );
    }

    @Override
    default void registerContent(EchoNativeModuleLoadContext context) {
        EchoNativeActivationSurfaceRegistrar.registerContent(context, activation(context));
    }

    @Override
    default void ready(EchoNativeModuleLoadContext context) {
        EchoNativeActivationSurfaceRegistrar.ready(context);
    }

    private Map<String, Object> activation(EchoNativeModuleLoadContext context) {
        return EchoNativeActivationSurfaceRegistrar.activation(
                context,
                () -> describeNativeSurfaces(EchoNativeActivationSurfaceRegistrar.bridgeContext(context))
        );
    }
}
