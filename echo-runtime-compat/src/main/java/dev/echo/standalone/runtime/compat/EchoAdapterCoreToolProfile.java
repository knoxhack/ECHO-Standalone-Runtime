package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

public record EchoAdapterCoreToolProfile(
        String profileContentId,
        String scrapKnifeLiveVoxelId,
        String scrapMetalLiveVoxelId,
        String craftScrapKnifeMissionId,
        String scrapKnifeRecipeId,
        int scrapMetalRequired,
        int scrapKnifeMaxDurability,
        int rustedDebrisDurabilityCost,
        double handMiningSpeed,
        double scrapKnifeMiningSpeed
) {
    public EchoAdapterCoreToolProfile {
        profileContentId = EchoCompatText.requireText(profileContentId, "profileContentId");
        scrapKnifeLiveVoxelId = EchoCompatText.requireText(scrapKnifeLiveVoxelId, "scrapKnifeLiveVoxelId");
        scrapMetalLiveVoxelId = EchoCompatText.requireText(scrapMetalLiveVoxelId, "scrapMetalLiveVoxelId");
        craftScrapKnifeMissionId = EchoCompatText.requireText(craftScrapKnifeMissionId, "craftScrapKnifeMissionId");
        scrapKnifeRecipeId = EchoCompatText.requireText(scrapKnifeRecipeId, "scrapKnifeRecipeId");
        if (scrapMetalRequired <= 0
                || scrapKnifeMaxDurability <= 0
                || rustedDebrisDurabilityCost <= 0
                || handMiningSpeed <= 0.0D
                || scrapKnifeMiningSpeed <= handMiningSpeed) {
            throw new IllegalArgumentException("tool profile values must be positive and improve on hand mining");
        }
    }

    public static EchoAdapterCoreToolProfile ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        EchoAdapterCoreRegistryEntry profile = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.TOOL_PROFILE_ID);
        EchoAdapterCoreRegistryEntry scrapKnife = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_ITEM_ID
        );
        EchoAdapterCoreRegistryEntry scrapMetal = requireLiveVoxel(
                registry,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_METAL_ITEM_ID
        );
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.CRAFT_SCRAP_KNIFE_MISSION_ID);
        registry.requireContentId(EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_RECIPE_ID);
        return new EchoAdapterCoreToolProfile(
                profile.contentId(),
                scrapKnife.liveVoxelId(),
                scrapMetal.liveVoxelId(),
                EchoAdapterCoreStandaloneContentBridge.CRAFT_SCRAP_KNIFE_MISSION_ID,
                EchoAdapterCoreStandaloneContentBridge.SCRAP_KNIFE_RECIPE_ID,
                2,
                8,
                1,
                1.0D,
                2.4D
        );
    }

    public double speedFor(EchoVoxelBlock block, boolean scrapKnifeEquipped) {
        if (block == null || block.air() || !scrapKnifeEquipped) {
            return handMiningSpeed;
        }
        return switch (block.id()) {
            case EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID,
                    EchoAdapterCoreStandaloneContentBridge.ASH_HAZARD_MARKER_BLOCK_ID,
                    EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID -> scrapKnifeMiningSpeed;
            default -> handMiningSpeed;
        };
    }

    public int durabilityCostFor(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return 0;
        }
        return block.id().equals(EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID)
                ? rustedDebrisDurabilityCost
                : Math.max(1, rustedDebrisDurabilityCost);
    }

    private static EchoAdapterCoreRegistryEntry requireLiveVoxel(
            EchoAdapterCoreStandaloneRegistry registry,
            String liveVoxelId
    ) {
        return registry.findLiveVoxelId(liveVoxelId).orElseThrow(() ->
                new IllegalArgumentException("No AdapterCore live voxel entry for " + liveVoxelId));
    }
}
