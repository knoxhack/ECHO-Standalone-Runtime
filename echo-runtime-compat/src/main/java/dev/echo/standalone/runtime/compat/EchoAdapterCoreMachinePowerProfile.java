package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreMachinePowerProfile(
        String profileContentId,
        String scrapDynamoLiveVoxelId,
        String batteryBankLiveVoxelId,
        String thermalBurnerLiveVoxelId,
        String energyCellLiveVoxelId,
        String buildScrapDynamoMissionId,
        String chargeBasicBatteryMissionId,
        String buildBatteryBankMissionId,
        String buildThermalBurnerMissionId,
        int wattsPerScrapDynamo,
        int energyCellsPerCharge,
        int batteryBankCapacityCells,
        int thermalBurnerHeatUnits
) {
    public EchoAdapterCoreMachinePowerProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        scrapDynamoLiveVoxelId = EchoCompatText.requireText(scrapDynamoLiveVoxelId, "scrapDynamoLiveVoxelId");
        batteryBankLiveVoxelId = EchoCompatText.requireText(batteryBankLiveVoxelId, "batteryBankLiveVoxelId");
        thermalBurnerLiveVoxelId = EchoCompatText.requireText(thermalBurnerLiveVoxelId, "thermalBurnerLiveVoxelId");
        energyCellLiveVoxelId = EchoCompatText.requireText(energyCellLiveVoxelId, "energyCellLiveVoxelId");
        buildScrapDynamoMissionId = EchoCompatText.requireText(
                buildScrapDynamoMissionId,
                "buildScrapDynamoMissionId"
        );
        chargeBasicBatteryMissionId = EchoCompatText.requireText(
                chargeBasicBatteryMissionId,
                "chargeBasicBatteryMissionId"
        );
        buildBatteryBankMissionId = EchoCompatText.requireText(
                buildBatteryBankMissionId,
                "buildBatteryBankMissionId"
        );
        buildThermalBurnerMissionId = EchoCompatText.requireText(
                buildThermalBurnerMissionId,
                "buildThermalBurnerMissionId"
        );
        if (wattsPerScrapDynamo <= 0
                || energyCellsPerCharge <= 0
                || batteryBankCapacityCells <= 0
                || thermalBurnerHeatUnits <= 0) {
            throw new IllegalArgumentException("machine power profile values must be positive");
        }
    }

    public static EchoAdapterCoreMachinePowerProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.MACHINE_POWER_PROFILE_ID);
        EchoAdapterCoreRegistryEntry scrapDynamo = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_DYNAMO_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry batteryBank = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.BATTERY_BANK_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry thermalBurner = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.THERMAL_BURNER_BLOCK_ID
        );
        EchoAdapterCoreRegistryEntry energyCell = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.ENERGY_CELL_ITEM_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_DYNAMO_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.CHARGE_BASIC_BATTERY_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_BATTERY_BANK_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_BURNER_MISSION_ID);
        return new EchoAdapterCoreMachinePowerProfile(
                EchoAdapterCoreStandaloneContentBridge.MACHINE_POWER_PROFILE_ID,
                scrapDynamo.liveVoxelId(),
                batteryBank.liveVoxelId(),
                thermalBurner.liveVoxelId(),
                energyCell.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.BUILD_SCRAP_DYNAMO_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.CHARGE_BASIC_BATTERY_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_BATTERY_BANK_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.BUILD_THERMAL_BURNER_MISSION_ID,
                80,
                1,
                3,
                60
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
