package dev.echo.standalone.runtime.compat;

public record EchoAdapterCoreSurvivalProfile(
        String survivalContentId,
        String waterLiveVoxelId,
        String foodLiveVoxelId,
        double walkHydrationDrainPerMinute,
        double sprintHydrationDrainPerMinute,
        double walkHungerDrainPerMinute,
        double sprintHungerDrainPerMinute,
        double waterHydrationRecovery,
        double waterAshRecovery,
        double foodHungerRecovery,
        int foodHealthRecovery,
        double dehydrationDamageThreshold,
        double starvationDamageThreshold,
        double ashExposureDamageThreshold,
        int deprivationHealthDamagePerPulse
) {
    public EchoAdapterCoreSurvivalProfile {
        survivalContentId = EchoCompatText.requireText(survivalContentId, "survivalContentId");
        waterLiveVoxelId = EchoCompatText.requireText(waterLiveVoxelId, "waterLiveVoxelId");
        foodLiveVoxelId = EchoCompatText.requireText(foodLiveVoxelId, "foodLiveVoxelId");
        if (walkHydrationDrainPerMinute < 0.0D
                || sprintHydrationDrainPerMinute < walkHydrationDrainPerMinute
                || walkHungerDrainPerMinute < 0.0D
                || sprintHungerDrainPerMinute < walkHungerDrainPerMinute
                || waterHydrationRecovery < 0.0D
                || waterAshRecovery < 0.0D
                || foodHungerRecovery < 0.0D
                || foodHealthRecovery < 0
                || dehydrationDamageThreshold < 0.0D
                || starvationDamageThreshold < 0.0D
                || ashExposureDamageThreshold < 0.0D
                || deprivationHealthDamagePerPulse < 0) {
            throw new IllegalArgumentException("survival profile values are out of range");
        }
    }

    public static EchoAdapterCoreSurvivalProfile ashfall(
            EchoAdapterCoreStandaloneRegistry registry,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        EchoAdapterCoreRegistryEntry profile = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.SURVIVAL_PROFILE_ID);
        String waterId = bridge.waterRationItem().id();
        String foodId = bridge.fieldRationItem().id();
        EchoAdapterCoreRegistryEntry water = registry.findLiveVoxelId(waterId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore water ration for " + waterId));
        EchoAdapterCoreRegistryEntry food = registry.findLiveVoxelId(foodId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No AdapterCore field ration for " + foodId));
        return new EchoAdapterCoreSurvivalProfile(
                profile.contentId(),
                water.liveVoxelId(),
                food.liveVoxelId(),
                1.25D,
                2.10D,
                0.85D,
                1.45D,
                28.0D,
                4.0D,
                30.0D,
                2,
                12.0D,
                8.0D,
                75.0D,
                1
        );
    }
}
