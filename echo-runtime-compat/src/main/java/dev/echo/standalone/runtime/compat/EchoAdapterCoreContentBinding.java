package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreContentBinding(
        String moduleId,
        String contentId,
        EchoAdapterCoreContentKind contentKind,
        String adapterKey,
        String neoForgeId,
        String nativeLoaderId,
        String standaloneRuntimeId,
        String liveVoxelId,
        boolean standaloneReady
) {
    public EchoAdapterCoreContentBinding {
        moduleId = EchoCompatText.requireText(moduleId, "moduleId");
        contentId = EchoCompatText.requireText(contentId, "contentId");
        Objects.requireNonNull(contentKind, "contentKind");
        adapterKey = EchoCompatText.requireText(adapterKey, "adapterKey");
        neoForgeId = EchoCompatText.requireText(neoForgeId, "neoForgeId");
        nativeLoaderId = EchoCompatText.requireText(nativeLoaderId, "nativeLoaderId");
        standaloneRuntimeId = EchoCompatText.requireText(standaloneRuntimeId, "standaloneRuntimeId");
        liveVoxelId = EchoCompatText.optionalText(liveVoxelId);
    }

    public String idFor(EchoAdapterCoreRuntimeKind runtimeKind) {
        Objects.requireNonNull(runtimeKind, "runtimeKind");
        return switch (runtimeKind) {
            case NEOFORGE -> neoForgeId;
            case ECHO_NATIVE_LOADER -> nativeLoaderId;
            case ECHO_RUNTIME_STANDALONE -> standaloneRuntimeId;
        };
    }

    public boolean supportsAllAdapterCoreRuntimes() {
        return !neoForgeId.isBlank()
                && !nativeLoaderId.isBlank()
                && !standaloneRuntimeId.isBlank();
    }
}
