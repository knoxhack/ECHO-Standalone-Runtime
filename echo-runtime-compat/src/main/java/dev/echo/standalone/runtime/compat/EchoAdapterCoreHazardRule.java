package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreHazardRule(
        String hazardContentId,
        String sourceLiveVoxelId,
        String label,
        double ashExposurePerMinute,
        double hydrationDrainPerMinute,
        double hungerDrainPerMinute,
        int healthDamagePerPulse,
        boolean unstableGround,
        boolean electricalDischarge,
        boolean extractionStorm
) {
    public EchoAdapterCoreHazardRule {
        hazardContentId = EchoCompatText.requireText(hazardContentId, "hazardContentId");
        sourceLiveVoxelId = sourceLiveVoxelId == null ? "" : sourceLiveVoxelId.trim();
        label = EchoCompatText.requireText(label, "label");
        if (ashExposurePerMinute < 0.0D
                || hydrationDrainPerMinute < 0.0D
                || hungerDrainPerMinute < 0.0D
                || healthDamagePerPulse < 0) {
            throw new IllegalArgumentException("hazard effects must not be negative");
        }
    }

    public boolean contactHazard() {
        return !sourceLiveVoxelId.isBlank();
    }
}
