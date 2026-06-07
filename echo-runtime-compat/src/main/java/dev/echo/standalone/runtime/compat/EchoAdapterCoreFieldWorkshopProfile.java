package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreFieldWorkshopProfile(
        String profileContentId,
        String handRecyclerLiveVoxelId,
        String machineCasingLiveVoxelId,
        String scrapWireLiveVoxelId,
        String scrapCircuitLiveVoxelId,
        String buildHandRecyclerMissionId,
        String makeMachineCasingMissionId,
        String assembleFieldKitMissionId,
        int scrapMetalForCasing,
        int scrapWireForFieldKit,
        int scrapCircuitForFieldKit
) {
    public EchoAdapterCoreFieldWorkshopProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        handRecyclerLiveVoxelId = EchoCompatText.requireText(handRecyclerLiveVoxelId, "handRecyclerLiveVoxelId");
        machineCasingLiveVoxelId = EchoCompatText.requireText(machineCasingLiveVoxelId, "machineCasingLiveVoxelId");
        scrapWireLiveVoxelId = EchoCompatText.requireText(scrapWireLiveVoxelId, "scrapWireLiveVoxelId");
        scrapCircuitLiveVoxelId = EchoCompatText.requireText(scrapCircuitLiveVoxelId, "scrapCircuitLiveVoxelId");
        buildHandRecyclerMissionId = EchoCompatText.requireText(buildHandRecyclerMissionId, "buildHandRecyclerMissionId");
        makeMachineCasingMissionId = EchoCompatText.requireText(makeMachineCasingMissionId, "makeMachineCasingMissionId");
        assembleFieldKitMissionId = EchoCompatText.requireText(assembleFieldKitMissionId, "assembleFieldKitMissionId");
        if (scrapMetalForCasing <= 0 || scrapWireForFieldKit <= 0 || scrapCircuitForFieldKit <= 0) {
            throw new IllegalArgumentException("field workshop material requirements must be positive");
        }
    }

    public static EchoAdapterCoreFieldWorkshopProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        EchoAdapterCoreRegistryEntry profile = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.FIELD_WORKSHOP_PROFILE_ID);
        EchoAdapterCoreRegistryEntry handRecycler = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.HAND_RECYCLER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry machineCasing = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.MACHINE_CASING_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry scrapWire = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_WIRE_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry scrapCircuit = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_CIRCUIT_ITEM_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_HAND_RECYCLER_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.MAKE_MACHINE_CASING_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.ASSEMBLE_FIELD_KIT_MISSION_ID);
        return new EchoAdapterCoreFieldWorkshopProfile(
                profile.contentId(),
                handRecycler.liveVoxelId(),
                machineCasing.liveVoxelId(),
                scrapWire.liveVoxelId(),
                scrapCircuit.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.BUILD_HAND_RECYCLER_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.MAKE_MACHINE_CASING_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.ASSEMBLE_FIELD_KIT_MISSION_ID,
                2,
                1,
                1
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
