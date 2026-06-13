package dev.echo.nativeplatform.contracts;

public interface EchoNativeSaveDataService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.save_data";
    }

    default EchoNativeMutationReceipt write(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
