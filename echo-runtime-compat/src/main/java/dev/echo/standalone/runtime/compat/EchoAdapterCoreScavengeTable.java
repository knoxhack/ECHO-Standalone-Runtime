package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoAdapterCoreScavengeTable {
    private final Map<String, EchoAdapterCoreScavengeReward> byLiveVoxelId;

    public EchoAdapterCoreScavengeTable(Map<String, EchoAdapterCoreScavengeReward> rewards) {
        Objects.requireNonNull(rewards, "rewards");
        this.byLiveVoxelId = Map.copyOf(rewards);
        if (byLiveVoxelId.isEmpty()) {
            throw new IllegalArgumentException("scavenge table must contain at least one AdapterCore reward");
        }
    }

    public static EchoAdapterCoreScavengeTable ashfall(
            EchoAdapterCoreStandaloneRegistry registry,
            EchoAdapterCoreStandaloneContentBridge bridge
    ) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(bridge, "bridge");
        String waterId = bridge.waterRationItem().id();
        String foodId = bridge.fieldRationItem().id();
        String repairId = bridge.powerRepairKitItem().id();
        requireLiveVoxel(registry, waterId);
        requireLiveVoxel(registry, foodId);
        requireLiveVoxel(registry, repairId);

        LinkedHashMap<String, EchoAdapterCoreScavengeReward> rewards = new LinkedHashMap<>();
        add(
                registry,
                rewards,
                bridge.rustedDebrisBlock().id(),
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_LOOT_ID,
                0,
                1,
                0,
                "adaptercore loot rusted debris: field ration recovered"
        );
        add(
                registry,
                rewards,
                bridge.crashCacheBlock().id(),
                EchoAdapterCoreStandaloneContentBridge.CRASH_CACHE_LOOT_ID,
                1,
                1,
                0,
                "adaptercore loot crash cache: ration pack recovered"
        );
        add(
                registry,
                rewards,
                bridge.damagedPowerNodeBlock().id(),
                EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_LOOT_ID,
                0,
                0,
                1,
                "adaptercore loot damaged power node: repair kit recovered"
        );
        return new EchoAdapterCoreScavengeTable(rewards);
    }

    public Optional<EchoAdapterCoreScavengeReward> rewardFor(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byLiveVoxelId.get(block.id()));
    }

    public int rewardCount() {
        return byLiveVoxelId.size();
    }

    private static void add(
            EchoAdapterCoreStandaloneRegistry registry,
            Map<String, EchoAdapterCoreScavengeReward> rewards,
            String sourceLiveVoxelId,
            String lootTableContentId,
            int waterRations,
            int foodRations,
            int repairKits,
            String message
    ) {
        EchoAdapterCoreRegistryEntry block = requireLiveVoxel(registry, sourceLiveVoxelId);
        require(registry, lootTableContentId);
        rewards.put(
                block.liveVoxelId(),
                new EchoAdapterCoreScavengeReward(
                        block.contentId(),
                        block.liveVoxelId(),
                        lootTableContentId,
                        waterRations,
                        foodRations,
                        repairKits,
                        message
                )
        );
    }

    private static EchoAdapterCoreRegistryEntry require(EchoAdapterCoreStandaloneRegistry registry, String contentId) {
        return registry.requireContentId(contentId);
    }

    private static EchoAdapterCoreRegistryEntry requireLiveVoxel(
            EchoAdapterCoreStandaloneRegistry registry,
            String liveVoxelId
    ) {
        return registry.findLiveVoxelId(liveVoxelId).orElseThrow(() ->
                new IllegalArgumentException("No AdapterCore live voxel entry for " + liveVoxelId));
    }
}
