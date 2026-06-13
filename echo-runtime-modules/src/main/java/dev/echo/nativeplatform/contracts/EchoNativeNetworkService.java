package dev.echo.nativeplatform.contracts;

public interface EchoNativeNetworkService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.network";
    }

    default EchoNativeMutationReceipt registerPacket(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt sendToPlayer(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt broadcast(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
