package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreWaterLoopProfile(
        String profileContentId,
        String rainCollectorLiveVoxelId,
        String waterPurifierLiveVoxelId,
        String dirtyWaterLiveVoxelId,
        String cleanWaterLiveVoxelId,
        int dirtyWaterPerCollection,
        int cleanWaterPerPurify,
        int forageFoodRations,
        int cleanWaterStockpileTarget,
        int rationStockpileTarget
) {
    public EchoAdapterCoreWaterLoopProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        rainCollectorLiveVoxelId = EchoCompatText.requireText(rainCollectorLiveVoxelId, "rainCollectorLiveVoxelId");
        waterPurifierLiveVoxelId = EchoCompatText.requireText(waterPurifierLiveVoxelId, "waterPurifierLiveVoxelId");
        dirtyWaterLiveVoxelId = EchoCompatText.requireText(dirtyWaterLiveVoxelId, "dirtyWaterLiveVoxelId");
        cleanWaterLiveVoxelId = EchoCompatText.requireText(cleanWaterLiveVoxelId, "cleanWaterLiveVoxelId");
        if (dirtyWaterPerCollection <= 0
                || cleanWaterPerPurify <= 0
                || forageFoodRations <= 0
                || cleanWaterStockpileTarget <= 0
                || rationStockpileTarget <= 0) {
            throw new IllegalArgumentException("water loop profile counts must be positive");
        }
    }

    public static EchoAdapterCoreWaterLoopProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        EchoAdapterCoreRegistryEntry profile = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.WATER_LOOP_PROFILE_ID);
        EchoAdapterCoreRegistryEntry rainCollector = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RAIN_COLLECTOR_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry waterPurifier = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.WATER_PURIFIER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry dirtyWater = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.DIRTY_WATER_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry cleanWater = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.WATER_RATION_ITEM_ID
        );
        return new EchoAdapterCoreWaterLoopProfile(
                profile.contentId(),
                rainCollector.liveVoxelId(),
                waterPurifier.liveVoxelId(),
                dirtyWater.liveVoxelId(),
                cleanWater.liveVoxelId(),
                2,
                2,
                2,
                3,
                4
        );
    }

    private static EchoAdapterCoreRegistryEntry requireLiveVoxel(
            EchoAdapterCoreStandaloneRegistry registry,
            String liveVoxelId
    ) {
        return registry.findLiveVoxelId(liveVoxelId).orElseThrow(() ->
                new IllegalArgumentException("No AdapterCore live voxel entry for " + liveVoxelId));
    }
}
