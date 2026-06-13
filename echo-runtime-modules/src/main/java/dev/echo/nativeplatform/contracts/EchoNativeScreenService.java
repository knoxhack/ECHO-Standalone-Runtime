package dev.echo.nativeplatform.contracts;

public interface EchoNativeScreenService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.screens";
    }

    default EchoNativeMutationReceipt registerSurface(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerMenu(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerKeybind(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt open(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
