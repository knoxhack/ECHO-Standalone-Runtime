package dev.echo.standalone.runtime.compat;

import java.util.List;
import java.util.Objects;

public record EchoAshfallGameplayFeatureContract(
        String featureId,
        EchoAshfallGameplayFeatureKind kind,
        EchoAshfallGameplayFeatureStatus status,
        EchoAdapterCoreDomain adapterDomain,
        String adapterKey,
        String neoForgeId,
        String nativeLoaderId,
        String standaloneRuntimeId,
        String contentId,
        String dataSource,
        List<String> saveFields,
        String note
) {
    public EchoAshfallGameplayFeatureContract {
        featureId = EchoCompatText.requireText(featureId, "featureId");
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(status, "status");
        adapterKey = EchoCompatText.optionalText(adapterKey);
        neoForgeId = EchoCompatText.optionalText(neoForgeId);
        nativeLoaderId = EchoCompatText.optionalText(nativeLoaderId);
        standaloneRuntimeId = EchoCompatText.optionalText(standaloneRuntimeId);
        contentId = EchoCompatText.optionalText(contentId);
        dataSource = EchoCompatText.optionalText(dataSource);
        saveFields = List.copyOf(saveFields == null ? List.of() : saveFields);
        note = EchoCompatText.optionalText(note);
    }

    public boolean requiresAdapterCoreBinding() {
        return status == EchoAshfallGameplayFeatureStatus.ADAPTERCORE_BACKED;
    }

    public boolean requiresSharedDataSource() {
        return status == EchoAshfallGameplayFeatureStatus.DATA_DRIVEN_SHARED;
    }

    public boolean blocksParity() {
        return status == EchoAshfallGameplayFeatureStatus.NEOFORGE_ONLY
                || status == EchoAshfallGameplayFeatureStatus.STANDALONE_ONLY
                || status == EchoAshfallGameplayFeatureStatus.MISSING_RUNTIME;
    }

    public boolean contractEvidenceComplete() {
        if (blocksParity()) {
            return false;
        }
        if (requiresAdapterCoreBinding()) {
            return adapterDomain != null
                    && !adapterKey.isBlank()
                    && !neoForgeId.isBlank()
                    && !nativeLoaderId.isBlank()
                    && !standaloneRuntimeId.isBlank()
                    && !contentId.isBlank();
        }
        return requiresSharedDataSource() && !dataSource.isBlank();
    }

    public boolean standaloneAliasRegisteredThroughAdapterCore() {
        return standaloneRuntimeId.isBlank()
                || standaloneRuntimeId.equals(neoForgeId)
                || status == EchoAshfallGameplayFeatureStatus.ADAPTERCORE_BACKED;
    }
}
