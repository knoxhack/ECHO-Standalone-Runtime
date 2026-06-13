package dev.echo.nativeplatform.contracts;

public interface EchoNativeResourceService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.resources";
    }

    default EchoNativeMutationReceipt registerReloadListener(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt runDatagen(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
