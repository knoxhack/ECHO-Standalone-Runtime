package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreFieldRecoveryProfile(
        String profileContentId,
        String radAwayLiveVoxelId,
        String stimPackLiveVoxelId,
        String handWarmerLiveVoxelId,
        String thermalLinerLiveVoxelId,
        String returnBeaconLiveVoxelId,
        String returnKeystoneLiveVoxelId,
        String useRadAwayMissionId,
        String useStimPackMissionId,
        String useHandWarmerMissionId,
        String installThermalLinerMissionId,
        String placeReturnBeaconMissionId,
        String bindReturnKeystoneMissionId,
        int radAwayExposureReduction,
        int stimPackHeal,
        int handWarmerWarmthSeconds,
        int thermalLinerWarmthSeconds,
        int returnBeaconSignalStrength,
        int returnKeystoneCharges
) {
    public EchoAdapterCoreFieldRecoveryProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        radAwayLiveVoxelId = EchoCompatText.requireText(radAwayLiveVoxelId, "radAwayLiveVoxelId");
        stimPackLiveVoxelId = EchoCompatText.requireText(stimPackLiveVoxelId, "stimPackLiveVoxelId");
        handWarmerLiveVoxelId = EchoCompatText.requireText(handWarmerLiveVoxelId, "handWarmerLiveVoxelId");
        thermalLinerLiveVoxelId = EchoCompatText.requireText(thermalLinerLiveVoxelId, "thermalLinerLiveVoxelId");
        returnBeaconLiveVoxelId = EchoCompatText.requireText(returnBeaconLiveVoxelId, "returnBeaconLiveVoxelId");
        returnKeystoneLiveVoxelId = EchoCompatText.requireText(returnKeystoneLiveVoxelId, "returnKeystoneLiveVoxelId");
        useRadAwayMissionId = EchoCompatText.requireText(useRadAwayMissionId, "useRadAwayMissionId");
        useStimPackMissionId = EchoCompatText.requireText(useStimPackMissionId, "useStimPackMissionId");
        useHandWarmerMissionId = EchoCompatText.requireText(useHandWarmerMissionId, "useHandWarmerMissionId");
        installThermalLinerMissionId = EchoCompatText.requireText(
                installThermalLinerMissionId,
                "installThermalLinerMissionId"
        );
        placeReturnBeaconMissionId = EchoCompatText.requireText(
                placeReturnBeaconMissionId,
                "placeReturnBeaconMissionId"
        );
        bindReturnKeystoneMissionId = EchoCompatText.requireText(
                bindReturnKeystoneMissionId,
                "bindReturnKeystoneMissionId"
        );
        if (radAwayExposureReduction <= 0
                || stimPackHeal <= 0
                || handWarmerWarmthSeconds <= 0
                || thermalLinerWarmthSeconds <= 0
                || returnBeaconSignalStrength <= 0
                || returnKeystoneCharges <= 0) {
            throw new IllegalArgumentException("field recovery profile values must be positive");
        }
    }

    public static EchoAdapterCoreFieldRecoveryProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIELD_RECOVERY_PROFILE_ID);
        EchoAdapterCoreRegistryEntry radAway = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RAD_AWAY_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry stimPack = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.STIM_PACK_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry handWarmer = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.HAND_WARMER_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry thermalLiner = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.THERMAL_LINER_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry returnBeacon = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RETURN_BEACON_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry returnKeystone = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RETURN_KEYSTONE_ITEM_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.USE_RAD_AWAY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.USE_STIM_PACK_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.USE_HAND_WARMER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.INSTALL_THERMAL_LINER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.PLACE_RETURN_BEACON_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BIND_RETURN_KEYSTONE_MISSION_ID);
        return new EchoAdapterCoreFieldRecoveryProfile(
                EchoAdapterCoreStandaloneContentBridge.FIELD_RECOVERY_PROFILE_ID,
                radAway.liveVoxelId(),
                stimPack.liveVoxelId(),
                handWarmer.liveVoxelId(),
                thermalLiner.liveVoxelId(),
                returnBeacon.liveVoxelId(),
                returnKeystone.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.USE_RAD_AWAY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.USE_STIM_PACK_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.USE_HAND_WARMER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_THERMAL_LINER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.PLACE_RETURN_BEACON_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BIND_RETURN_KEYSTONE_MISSION_ID,
                35,
                30,
                45,
                60,
                100,
                1
        );
    }

    private static EchoAdapterCoreRegistryEntry requireLiveVoxel(
            EchoAdapterCoreStandaloneRegistry registry,
            String liveVoxelId
    ) {
        EchoAdapterCoreRegistryEntry entry = registry.findLiveVoxelId(liveVoxelId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore live voxel entry for " + liveVoxelId));
        if (entry.liveVoxelId().isBlank()) {
            throw new IllegalStateException(liveVoxelId + " must expose a live voxel id");
        }
        return entry;
    }
}
