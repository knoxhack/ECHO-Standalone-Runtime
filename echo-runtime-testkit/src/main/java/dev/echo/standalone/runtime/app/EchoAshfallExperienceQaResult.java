package dev.echo.standalone.runtime.app;

public record EchoAshfallExperienceQaResult(
        boolean fullChapterComplete,
        boolean midRunSaveLoadReady,
        boolean deathRecoveryReady,
        boolean inventoryManipulationReady,
        boolean inventoryUxReady,
        boolean visibleHudFeedbackReady,
        boolean visibleActionParticlesReady,
        boolean terminalBranchingReady,
        boolean scavengeDepletionReady,
        boolean powerRepairFlowReady,
        boolean extractionEventReady,
        boolean hazardVarietyReady,
        boolean shelterSystemReady,
        boolean survivalNeedsReady,
        boolean waterLoopReady,
        boolean toolProgressionReady,
        boolean fieldWorkshopReady,
        boolean fieldPowerReady,
        boolean machinePowerReady,
        boolean midgameProgressionReady,
        boolean expeditionSafetyReady,
        boolean advancedExpeditionReady,
        boolean fieldRecoveryReady,
        boolean canonicalRouteReady,
        boolean failureRecoveryReady,
        boolean hudObjectiveStateReady,
        boolean routeWideGuidanceReady,
        boolean audioCueCoverageReady,
        boolean corruptedSaveDetected,
        boolean adapterCoreParityReady,
        int diagnosticsCount,
        long restoredFrameChecksum,
        String summary
) {
    public EchoAshfallExperienceQaResult {
        summary = EchoAppText.requireText(summary, "summary");
        if (diagnosticsCount < 0) {
            throw new IllegalArgumentException("diagnosticsCount must not be negative");
        }
    }

    public boolean ready() {
        return fullChapterComplete
                && midRunSaveLoadReady
                && deathRecoveryReady
                && inventoryManipulationReady
                && inventoryUxReady
                && visibleHudFeedbackReady
                && visibleActionParticlesReady
                && terminalBranchingReady
                && scavengeDepletionReady
                && powerRepairFlowReady
                && extractionEventReady
                && hazardVarietyReady
                && shelterSystemReady
                && survivalNeedsReady
                && waterLoopReady
                && toolProgressionReady
                && fieldWorkshopReady
                && fieldPowerReady
                && machinePowerReady
                && midgameProgressionReady
                && expeditionSafetyReady
                && advancedExpeditionReady
                && fieldRecoveryReady
                && canonicalRouteReady
                && failureRecoveryReady
                && hudObjectiveStateReady
                && routeWideGuidanceReady
                && audioCueCoverageReady
                && corruptedSaveDetected
                && adapterCoreParityReady
                && diagnosticsCount >= 30
                && restoredFrameChecksum != 0L;
    }
}
