package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreShelterProfile(
        String shelterContentId,
        String anchorLiveVoxelId,
        double radius,
        double resourceMultiplier,
        double ashRecoveryPerMinute,
        double restHealthRecoveryPerMinute,
        double stormIntegrityDamagePerMinute
) {
    public EchoAdapterCoreShelterProfile {
        shelterContentId = EchoCompatText.requireText(shelterContentId, "shelterContentId");
        anchorLiveVoxelId = EchoCompatText.requireText(anchorLiveVoxelId, "anchorLiveVoxelId");
        if (radius <= 0.0D) {
            throw new IllegalArgumentException("radius must be positive");
        }
        if (resourceMultiplier < 0.0D || resourceMultiplier > 1.0D
                || ashRecoveryPerMinute < 0.0D
                || restHealthRecoveryPerMinute < 0.0D
                || stormIntegrityDamagePerMinute < 0.0D) {
            throw new IllegalArgumentException("shelter profile values are out of range");
        }
    }

    public static EchoAdapterCoreShelterProfile ashfall(
            EchoAdapterCoreStandaloneRegistry registry,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        String anchorId = bridge.shelterAnchorBlock().id();
        EchoAdapterCoreRegistryEntry anchor = registry.findLiveVoxelId(anchorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore shelter anchor for " + anchorId));
        return new EchoAdapterCoreShelterProfile(
                anchor.contentId(),
                anchor.liveVoxelId(),
                4.0D,
                0.25D,
                6.0D,
                2.0D,
                4.0D
        );
    }
}
