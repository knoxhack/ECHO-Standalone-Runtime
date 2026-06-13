package dev.echo.nativeplatform.contracts;

public interface EchoNativeCommandService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.commands";
    }

    default EchoNativeMutationReceipt register(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
