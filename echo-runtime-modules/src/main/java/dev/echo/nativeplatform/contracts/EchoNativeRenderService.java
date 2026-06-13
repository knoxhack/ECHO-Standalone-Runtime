package dev.echo.nativeplatform.contracts;

public interface EchoNativeRenderService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.render";
    }

    default EchoNativeMutationReceipt registerHudOverlay(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerRenderHook(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
