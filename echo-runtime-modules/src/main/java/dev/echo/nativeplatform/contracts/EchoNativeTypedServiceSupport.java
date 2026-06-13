package dev.echo.nativeplatform.contracts;

public interface EchoNativeTypedServiceSupport {
    String serviceId();

    default EchoNativeMutationReceipt receipt(EchoNativeServiceMutation mutation) {
        return receipt(mutation, EchoNativeLoadStatus.MUTATED);
    }

    default EchoNativeMutationReceipt receipt(
            EchoNativeServiceMutation mutation,
            EchoNativeLoadStatus status
    ) {
        return EchoNativeMutationReceipt.from(serviceId(), mutation, status, 0);
    }
}
