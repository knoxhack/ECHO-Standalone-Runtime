package dev.echo.nativeplatform.contracts;

public interface EchoNativeRegistryService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.registry";
    }

    default EchoNativeMutationReceipt deferredRegister(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt register(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerBlockEntity(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerCreativeTab(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerRecipe(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }

    default EchoNativeMutationReceipt registerLootModifier(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
