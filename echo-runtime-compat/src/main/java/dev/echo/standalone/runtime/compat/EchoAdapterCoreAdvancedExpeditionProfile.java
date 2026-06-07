package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreAdvancedExpeditionProfile(
        String profileContentId,
        String filterWorkbenchLiveVoxelId,
        String oreGrinderLiveVoxelId,
        String isotopeRefinerLiveVoxelId,
        String relayStationLiveVoxelId,
        String denseAlloyLiveVoxelId,
        String alloyBladeLiveVoxelId,
        String alloyHelmetLiveVoxelId,
        String alloyChestplateLiveVoxelId,
        String relayScannerLensLiveVoxelId,
        String scoutDroneItemLiveVoxelId,
        String scoutDroneEntityContentId,
        String buildFilterWorkbenchMissionId,
        String buildOreGrinderMissionId,
        String findDenseAlloyMissionId,
        String buildIsotopeRefinerMissionId,
        String forgeAlloyWeaponMissionId,
        String equipAlloyKitMissionId,
        String activateRelayStationMissionId,
        String buildScoutDroneMissionId,
        int denseAlloyPerOreGrind,
        int alloyWeaponDurability,
        int relaySignalStrength,
        int scoutDroneRangeMeters
) {
    public EchoAdapterCoreAdvancedExpeditionProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        filterWorkbenchLiveVoxelId = EchoCompatText.requireText(
                filterWorkbenchLiveVoxelId,
                "filterWorkbenchLiveVoxelId"
        );
        oreGrinderLiveVoxelId = EchoCompatText.requireText(oreGrinderLiveVoxelId, "oreGrinderLiveVoxelId");
        isotopeRefinerLiveVoxelId = EchoCompatText.requireText(
                isotopeRefinerLiveVoxelId,
                "isotopeRefinerLiveVoxelId"
        );
        relayStationLiveVoxelId = EchoCompatText.requireText(relayStationLiveVoxelId, "relayStationLiveVoxelId");
        denseAlloyLiveVoxelId = EchoCompatText.requireText(denseAlloyLiveVoxelId, "denseAlloyLiveVoxelId");
        alloyBladeLiveVoxelId = EchoCompatText.requireText(alloyBladeLiveVoxelId, "alloyBladeLiveVoxelId");
        alloyHelmetLiveVoxelId = EchoCompatText.requireText(alloyHelmetLiveVoxelId, "alloyHelmetLiveVoxelId");
        alloyChestplateLiveVoxelId = EchoCompatText.requireText(
                alloyChestplateLiveVoxelId,
                "alloyChestplateLiveVoxelId"
        );
        relayScannerLensLiveVoxelId = EchoCompatText.requireText(
                relayScannerLensLiveVoxelId,
                "relayScannerLensLiveVoxelId"
        );
        scoutDroneItemLiveVoxelId = EchoCompatText.requireText(
                scoutDroneItemLiveVoxelId,
                "scoutDroneItemLiveVoxelId"
        );
        scoutDroneEntityContentId = EchoCompatText.requireText(
                scoutDroneEntityContentId,
                "scoutDroneEntityContentId"
        );
        buildFilterWorkbenchMissionId = EchoCompatText.requireText(
                buildFilterWorkbenchMissionId,
                "buildFilterWorkbenchMissionId"
        );
        buildOreGrinderMissionId = EchoCompatText.requireText(buildOreGrinderMissionId, "buildOreGrinderMissionId");
        findDenseAlloyMissionId = EchoCompatText.requireText(findDenseAlloyMissionId, "findDenseAlloyMissionId");
        buildIsotopeRefinerMissionId = EchoCompatText.requireText(
                buildIsotopeRefinerMissionId,
                "buildIsotopeRefinerMissionId"
        );
        forgeAlloyWeaponMissionId = EchoCompatText.requireText(
                forgeAlloyWeaponMissionId,
                "forgeAlloyWeaponMissionId"
        );
        equipAlloyKitMissionId = EchoCompatText.requireText(equipAlloyKitMissionId, "equipAlloyKitMissionId");
        activateRelayStationMissionId = EchoCompatText.requireText(
                activateRelayStationMissionId,
                "activateRelayStationMissionId"
        );
        buildScoutDroneMissionId = EchoCompatText.requireText(buildScoutDroneMissionId, "buildScoutDroneMissionId");
        if (denseAlloyPerOreGrind <= 0
                || alloyWeaponDurability <= 0
                || relaySignalStrength <= 0
                || scoutDroneRangeMeters <= 0) {
            throw new IllegalArgumentException("advanced expedition profile values must be positive");
        }
    }

    public static EchoAdapterCoreAdvancedExpeditionProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.ADVANCED_EXPEDITION_PROFILE_ID);
        EchoAdapterCoreRegistryEntry filterWorkbench = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.FILTER_WORKBENCH_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry oreGrinder = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ORE_GRINDER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry isotopeRefiner = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ISOTOPE_REFINER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry relayStation = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RELAY_STATION_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry denseAlloy = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.DENSE_ALLOY_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry alloyBlade = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ALLOY_BLADE_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry alloyHelmet = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ALLOY_HELMET_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry alloyChestplate = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ALLOY_CHESTPLATE_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry relayScannerLens = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.RELAY_SCANNER_LENS_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry scoutDroneItem = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ITEM_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ENTITY_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_FILTER_WORKBENCH_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_ORE_GRINDER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FIND_DENSE_ALLOY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_ISOTOPE_REFINER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.FORGE_ALLOY_WEAPON_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.EQUIP_ALLOY_KIT_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.ACTIVATE_RELAY_STATION_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_SCOUT_DRONE_MISSION_ID);
        return new EchoAdapterCoreAdvancedExpeditionProfile(
                EchoAdapterCoreStandaloneContentBridge.ADVANCED_EXPEDITION_PROFILE_ID,
                filterWorkbench.liveVoxelId(),
                oreGrinder.liveVoxelId(),
                isotopeRefiner.liveVoxelId(),
                relayStation.liveVoxelId(),
                denseAlloy.liveVoxelId(),
                alloyBlade.liveVoxelId(),
                alloyHelmet.liveVoxelId(),
                alloyChestplate.liveVoxelId(),
                relayScannerLens.liveVoxelId(),
                scoutDroneItem.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.SCOUT_DRONE_ENTITY_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_FILTER_WORKBENCH_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ORE_GRINDER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.FIND_DENSE_ALLOY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_ISOTOPE_REFINER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.FORGE_ALLOY_WEAPON_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.EQUIP_ALLOY_KIT_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.ACTIVATE_RELAY_STATION_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCOUT_DRONE_MISSION_ID,
                3,
                350,
                100,
                240
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
