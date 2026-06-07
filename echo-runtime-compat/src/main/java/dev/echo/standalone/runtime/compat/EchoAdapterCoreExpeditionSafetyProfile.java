package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreExpeditionSafetyProfile(
        String profileContentId,
        String basicFilterLiveVoxelId,
        String advancedFilterLiveVoxelId,
        String thermalArrayLiveVoxelId,
        String atmosphericScrubberLiveVoxelId,
        String radiationCleanserLiveVoxelId,
        String fieldMedBayLiveVoxelId,
        String fixMaskFilterMissionId,
        String craftAdvancedFilterMissionId,
        String buildThermalArrayMissionId,
        String warmUpAfterExposureMissionId,
        String buildAtmosphericScrubberMissionId,
        String buildRadiationCleanserMissionId,
        String buildFieldMedBayMissionId,
        String useFieldMedBayMissionId,
        int basicFilterCharges,
        int advancedFilterCharges,
        int warmthRecoverySeconds,
        int radiationCleanserCycles,
        int fieldMedBayTreatments
) {
    public EchoAdapterCoreExpeditionSafetyProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        basicFilterLiveVoxelId = EchoCompatText.requireText(basicFilterLiveVoxelId, "basicFilterLiveVoxelId");
        advancedFilterLiveVoxelId = EchoCompatText.requireText(
                advancedFilterLiveVoxelId,
                "advancedFilterLiveVoxelId"
        );
        thermalArrayLiveVoxelId = EchoCompatText.requireText(thermalArrayLiveVoxelId, "thermalArrayLiveVoxelId");
        atmosphericScrubberLiveVoxelId = EchoCompatText.requireText(
                atmosphericScrubberLiveVoxelId,
                "atmosphericScrubberLiveVoxelId"
        );
        radiationCleanserLiveVoxelId = EchoCompatText.requireText(
                radiationCleanserLiveVoxelId,
                "radiationCleanserLiveVoxelId"
        );
        fieldMedBayLiveVoxelId = EchoCompatText.requireText(fieldMedBayLiveVoxelId, "fieldMedBayLiveVoxelId");
        fixMaskFilterMissionId = EchoCompatText.requireText(fixMaskFilterMissionId, "fixMaskFilterMissionId");
        craftAdvancedFilterMissionId = EchoCompatText.requireText(
                craftAdvancedFilterMissionId,
                "craftAdvancedFilterMissionId"
        );
        buildThermalArrayMissionId = EchoCompatText.requireText(
                buildThermalArrayMissionId,
                "buildThermalArrayMissionId"
        );
        warmUpAfterExposureMissionId = EchoCompatText.requireText(
                warmUpAfterExposureMissionId,
                "warmUpAfterExposureMissionId"
        );
        buildAtmosphericScrubberMissionId = EchoCompatText.requireText(
                buildAtmosphericScrubberMissionId,
                "buildAtmosphericScrubberMissionId"
        );
        buildRadiationCleanserMissionId = EchoCompatText.requireText(
                buildRadiationCleanserMissionId,
                "buildRadiationCleanserMissionId"
        );
        buildFieldMedBayMissionId = EchoCompatText.requireText(
                buildFieldMedBayMissionId,
                "buildFieldMedBayMissionId"
        );
        useFieldMedBayMissionId = EchoCompatText.requireText(useFieldMedBayMissionId, "useFieldMedBayMissionId");
        if (basicFilterCharges <= 0
                || advancedFilterCharges <= 0
                || warmthRecoverySeconds <= 0
                || radiationCleanserCycles <= 0
                || fieldMedBayTreatments <= 0) {
            throw new IllegalArgumentException("expedition safety profile values must be positive");
        }
    }

    public static EchoAdapterCoreExpeditionSafetyProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.EXPEDITION_SAFETY_PROFILE_ID);
        EchoAdapterCoreRegistryEntry basicFilter = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.BASIC_FILTER_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry advancedFilter = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ADVANCED_FILTER_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry thermalArray = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.THERMAL_ARRAY_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry atmosphericScrubber = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ATMOSPHERIC_SCRUBBER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry radiationCleanser = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RADIATION_CLEANSER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry fieldMedBay = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.FIELD_MED_BAY_BLOCK_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIX_MASK_FILTER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.CRAFT_ADVANCED_FILTER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_ARRAY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.WARM_UP_AFTER_EXPOSURE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_RADIATION_CLEANSER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_FIELD_MED_BAY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.USE_FIELD_MED_BAY_MISSION_ID);
        return new EchoAdapterCoreExpeditionSafetyProfile(
                EchoAdapterCoreStandaloneContentBridge.EXPEDITION_SAFETY_PROFILE_ID,
                basicFilter.liveVoxelId(),
                advancedFilter.liveVoxelId(),
                thermalArray.liveVoxelId(),
                atmosphericScrubber.liveVoxelId(),
                radiationCleanser.liveVoxelId(),
                fieldMedBay.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.FIX_MASK_FILTER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.CRAFT_ADVANCED_FILTER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_ARRAY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.WARM_UP_AFTER_EXPOSURE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ATMOSPHERIC_SCRUBBER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_RADIATION_CLEANSER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FIELD_MED_BAY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.USE_FIELD_MED_BAY_MISSION_ID,
                60,
                180,
                20,
                2,
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
