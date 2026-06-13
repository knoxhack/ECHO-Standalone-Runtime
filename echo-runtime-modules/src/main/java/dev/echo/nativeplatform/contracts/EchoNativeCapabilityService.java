package dev.echo.nativeplatform.contracts;

public interface EchoNativeCapabilityService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.capabilities";
    }

    default EchoNativeMutationReceipt register(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt mutate(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerIntegration(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
