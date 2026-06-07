package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreFieldPowerProfile(
        String profileContentId,
        String microGeneratorLiveVoxelId,
        String powerCableLiveVoxelId,
        String energyMeterLiveVoxelId,
        String buildMicroGeneratorMissionId,
        String routePowerCableMissionId,
        String installEnergyMeterMissionId,
        int requiredFieldKits,
        int cableSegmentsRequired,
        int wattsPerGenerator,
        int meterReadingsRequired
) {
    public EchoAdapterCoreFieldPowerProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        microGeneratorLiveVoxelId = EchoCompatText.requireText(
                microGeneratorLiveVoxelId,
                "microGeneratorLiveVoxelId"
        );
        powerCableLiveVoxelId = EchoCompatText.requireText(powerCableLiveVoxelId, "powerCableLiveVoxelId");
        energyMeterLiveVoxelId = EchoCompatText.requireText(energyMeterLiveVoxelId, "energyMeterLiveVoxelId");
        buildMicroGeneratorMissionId = EchoCompatText.requireText(
                buildMicroGeneratorMissionId,
                "buildMicroGeneratorMissionId"
        );
        routePowerCableMissionId = EchoCompatText.requireText(
                routePowerCableMissionId,
                "routePowerCableMissionId"
        );
        installEnergyMeterMissionId = EchoCompatText.requireText(
                installEnergyMeterMissionId,
                "installEnergyMeterMissionId"
        );
        if (requiredFieldKits <= 0 || cableSegmentsRequired <= 0
                || wattsPerGenerator <= 0 || meterReadingsRequired <= 0) {
            throw new IllegalArgumentException("field power requirements must be positive");
        }
    }

    public static EchoAdapterCoreFieldPowerProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        EchoAdapterCoreRegistryEntry profile = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.FIELD_POWER_PROFILE_ID);
        EchoAdapterCoreRegistryEntry microGenerator = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.MICRO_GENERATOR_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry powerCable = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.POWER_CABLE_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry energyMeter = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ENERGY_METER_BLOCK_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_MICRO_GENERATOR_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.ROUTE_POWER_CABLE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.INSTALL_ENERGY_METER_MISSION_ID);
        return new EchoAdapterCoreFieldPowerProfile(
                profile.contentId(),
                microGenerator.liveVoxelId(),
                powerCable.liveVoxelId(),
                energyMeter.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.BUILD_MICRO_GENERATOR_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.ROUTE_POWER_CABLE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.INSTALL_ENERGY_METER_MISSION_ID,
                1,
                3,
                40,
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
