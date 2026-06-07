package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreMidgameProgressionProfile(
        String profileContentId,
        String gasMaskLiveVoxelId,
        String schematicFragmentLiveVoxelId,
        String scrapPressLiveVoxelId,
        String itemPipeLiveVoxelId,
        String factoryControllerLiveVoxelId,
        String researchLabLiveVoxelId,
        String reinforcedPowerCableLiveVoxelId,
        String equipGasMaskMissionId,
        String findSchematicFragmentMissionId,
        String firstSchematicMissionId,
        String buildScrapPressMissionId,
        String installItemPipeMissionId,
        String buildFactoryControllerMissionId,
        String buildResearchLabMissionId,
        String upgradePowerCableMissionId,
        String setPowerPriorityMissionId,
        String overclockMachineMissionId,
        int itemPipeSegmentsRequired,
        int upgradedCableSegmentsRequired,
        int overclockHeatUnits
) {
    public EchoAdapterCoreMidgameProgressionProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        gasMaskLiveVoxelId = EchoCompatText.requireText(gasMaskLiveVoxelId, "gasMaskLiveVoxelId");
        schematicFragmentLiveVoxelId = EchoCompatText.requireText(
                schematicFragmentLiveVoxelId,
                "schematicFragmentLiveVoxelId"
        );
        scrapPressLiveVoxelId = EchoCompatText.requireText(scrapPressLiveVoxelId, "scrapPressLiveVoxelId");
        itemPipeLiveVoxelId = EchoCompatText.requireText(itemPipeLiveVoxelId, "itemPipeLiveVoxelId");
        factoryControllerLiveVoxelId = EchoCompatText.requireText(
                factoryControllerLiveVoxelId,
                "factoryControllerLiveVoxelId"
        );
        researchLabLiveVoxelId = EchoCompatText.requireText(researchLabLiveVoxelId, "researchLabLiveVoxelId");
        reinforcedPowerCableLiveVoxelId = EchoCompatText.requireText(
                reinforcedPowerCableLiveVoxelId,
                "reinforcedPowerCableLiveVoxelId"
        );
        equipGasMaskMissionId = EchoCompatText.requireText(equipGasMaskMissionId, "equipGasMaskMissionId");
        findSchematicFragmentMissionId = EchoCompatText.requireText(
                findSchematicFragmentMissionId,
                "findSchematicFragmentMissionId"
        );
        firstSchematicMissionId = EchoCompatText.requireText(firstSchematicMissionId, "firstSchematicMissionId");
        buildScrapPressMissionId = EchoCompatText.requireText(
                buildScrapPressMissionId,
                "buildScrapPressMissionId"
        );
        installItemPipeMissionId = EchoCompatText.requireText(
                installItemPipeMissionId,
                "installItemPipeMissionId"
        );
        buildFactoryControllerMissionId = EchoCompatText.requireText(
                buildFactoryControllerMissionId,
                "buildFactoryControllerMissionId"
        );
        buildResearchLabMissionId = EchoCompatText.requireText(
                buildResearchLabMissionId,
                "buildResearchLabMissionId"
        );
        upgradePowerCableMissionId = EchoCompatText.requireText(
                upgradePowerCableMissionId,
                "upgradePowerCableMissionId"
        );
        setPowerPriorityMissionId = EchoCompatText.requireText(
                setPowerPriorityMissionId,
                "setPowerPriorityMissionId"
        );
        overclockMachineMissionId = EchoCompatText.requireText(
                overclockMachineMissionId,
                "overclockMachineMissionId"
        );
        if (itemPipeSegmentsRequired <= 0 || upgradedCableSegmentsRequired <= 0 || overclockHeatUnits <= 0) {
            throw new IllegalArgumentException("midgame progression profile values must be positive");
        }
    }

    public static EchoAdapterCoreMidgameProgressionProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.MIDGAME_PROGRESSION_PROFILE_ID);
        EchoAdapterCoreRegistryEntry gasMask = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.GAS_MASK_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry schematicFragment = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCHEMATIC_FRAGMENT_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry scrapPress = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_PRESS_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry itemPipe = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ITEM_PIPE_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry factoryController = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.FACTORY_CONTROLLER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry researchLab = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RESEARCH_LAB_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry reinforcedPowerCable = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.REINFORCED_POWER_CABLE_BLOCK_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.EQUIP_GAS_MASK_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIND_SCHEMATIC_FRAGMENT_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIRST_SCHEMATIC_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_PRESS_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.INSTALL_ITEM_PIPE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_FACTORY_CONTROLLER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_RESEARCH_LAB_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.UPGRADE_POWER_CABLE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.SET_POWER_PRIORITY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.OVERCLOCK_MACHINE_MISSION_ID);
        return new EchoAdapterCoreMidgameProgressionProfile(
                EchoAdapterCoreStandaloneContentBridge.MIDGAME_PROGRESSION_PROFILE_ID,
                gasMask.liveVoxelId(),
                schematicFragment.liveVoxelId(),
                scrapPress.liveVoxelId(),
                itemPipe.liveVoxelId(),
                factoryController.liveVoxelId(),
                researchLab.liveVoxelId(),
                reinforcedPowerCable.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.EQUIP_GAS_MASK_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.FIND_SCHEMATIC_FRAGMENT_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.FIRST_SCHEMATIC_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_PRESS_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_ITEM_PIPE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FACTORY_CONTROLLER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_RESEARCH_LAB_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.UPGRADE_POWER_CABLE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.SET_POWER_PRIORITY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.OVERCLOCK_MACHINE_MISSION_ID,
                2,
                2,
                30
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
