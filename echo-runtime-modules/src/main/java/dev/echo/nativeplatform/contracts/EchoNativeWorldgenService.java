package dev.echo.nativeplatform.contracts;

public interface EchoNativeWorldgenService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.worldgen";
    }

    default EchoNativeMutationReceipt registerFeature(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt placeStructure(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
