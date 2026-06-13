package dev.echo.nativeplatform.contracts;

public interface EchoNativeEventService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.events";
    }

    default EchoNativeMutationReceipt subscribe(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt publish(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
