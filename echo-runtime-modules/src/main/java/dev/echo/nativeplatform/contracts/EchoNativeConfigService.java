package dev.echo.nativeplatform.contracts;

public interface EchoNativeConfigService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.config";
    }

    default EchoNativeMutationReceipt register(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
