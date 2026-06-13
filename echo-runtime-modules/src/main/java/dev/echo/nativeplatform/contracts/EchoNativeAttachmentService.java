package dev.echo.nativeplatform.contracts;

public interface EchoNativeAttachmentService extends EchoNativeTypedServiceSupport {
    @Override
    default String serviceId() {
        return "echo.native.attachments";
    }

    default EchoNativeMutationReceipt attach(EchoNativeServiceMutation mutation) {
        return receipt(mutation);
    }
}
