package dev.echo.nativeplatform.contracts;

public interface EchoNativeLifecycleService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.lifecycle";
    }

    default EchoNativeMutationReceipt phase(EchoNativeServiceMutation mutation) {
        return receipt(mutation, EchoNativeLoadStatus.RESOLVED);
    }

    default EchoNativeMutationReceipt registerGameTest(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt shutdown(EchoNativeServiceMutation mutation) {
        return receipt(mutation, EchoNativeLoadStatus.RESOLVED);
    }
}
