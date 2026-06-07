package dev.echo.standalone.runtime.save;

import java.util.List;
import java.util.Objects;

public record EchoSaveModSetCompatibilityReport(
        String profileId,
        String slotId,
        String algorithm,
        String expectedFingerprint,
        String currentFingerprint,
        List<String> expectedModuleIds,
        List<String> currentModuleIds,
        List<String> missingModuleIds,
        List<String> addedModuleIds,
        boolean compatible,
        boolean loadBlocked,
        boolean backupAvailable,
        boolean backupRequired,
        boolean manualApprovalRequired,
        boolean migrationPlanningRequired,
        String recoveryAction,
        String message
) {
    public EchoSaveModSetCompatibilityReport {
        profileId = EchoSavePaths.requireText(profileId, "profileId");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        algorithm = EchoSavePaths.requireText(algorithm, "algorithm");
        expectedFingerprint = EchoSavePaths.requireText(expectedFingerprint, "expectedFingerprint");
        currentFingerprint = EchoSavePaths.requireText(currentFingerprint, "currentFingerprint");
        Objects.requireNonNull(expectedModuleIds, "expectedModuleIds");
        Objects.requireNonNull(currentModuleIds, "currentModuleIds");
        Objects.requireNonNull(missingModuleIds, "missingModuleIds");
        Objects.requireNonNull(addedModuleIds, "addedModuleIds");
        expectedModuleIds = List.copyOf(expectedModuleIds);
        currentModuleIds = List.copyOf(currentModuleIds);
        missingModuleIds = List.copyOf(missingModuleIds);
        addedModuleIds = List.copyOf(addedModuleIds);
        recoveryAction = EchoSavePaths.requireText(recoveryAction, "recoveryAction");
        message = EchoSavePaths.requireText(message, "message");
    }
}
