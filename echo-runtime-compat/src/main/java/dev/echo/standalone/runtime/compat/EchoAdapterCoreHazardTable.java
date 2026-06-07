package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoAdapterCoreHazardTable {
    private final Map<String, EchoAdapterCoreHazardRule> byLiveVoxelId;
    private final EchoAdapterCoreHazardRule extractionStorm;

    public EchoAdapterCoreHazardTable(
            Map<String, EchoAdapterCoreHazardRule> contactRules,
            EchoAdapterCoreHazardRule extractionStorm
    ) {
        Objects.requireNonNull(contactRules, "contactRules");
        this.byLiveVoxelId = Map.copyOf(contactRules);
        this.extractionStorm = Objects.requireNonNull(extractionStorm, "extractionStorm");
        if (byLiveVoxelId.isEmpty()) {
            throw new IllegalArgumentException("hazard table must contain at least one AdapterCore contact hazard");
        }
        if (!extractionStorm.extractionStorm()) {
            throw new IllegalArgumentException("extraction storm rule must be marked as extraction storm");
        }
    }

    public static EchoAdapterCoreHazardTable ashfall(EchoAdapterCoreStandaloneRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        LinkedHashMap<String, EchoAdapterCoreHazardRule> rules = new LinkedHashMap<>();
        addContact(
                registry,
                rules,
                EchoAdapterCoreStandaloneContentBridge.TOXIC_ASH_BLOCK_ID,
                EchoAdapterCoreStandaloneContentBridge.TOXIC_ASH_HAZARD_ID,
                "toxic ash",
                18.0D,
                0.0D,
                0.0D,
                0,
                false,
                false
        );
        addContact(
                registry,
                rules,
                EchoAdapterCoreStandaloneContentBridge.SCORCHED_BASALT_BLOCK_ID,
                EchoAdapterCoreStandaloneContentBridge.HOT_ASH_HAZARD_ID,
                "hot ash",
                30.0D,
                1.0D,
                0.0D,
                0,
                false,
                false
        );
        addContact(
                registry,
                rules,
                EchoAdapterCoreStandaloneContentBridge.RUSTED_DEBRIS_BLOCK_ID,
                EchoAdapterCoreStandaloneContentBridge.UNSTABLE_GROUND_HAZARD_ID,
                "unstable ground",
                6.0D,
                0.0D,
                2.0D,
                1,
                true,
                false
        );
        addContact(
                registry,
                rules,
                EchoAdapterCoreStandaloneContentBridge.DAMAGED_POWER_NODE_BLOCK_ID,
                EchoAdapterCoreStandaloneContentBridge.ELECTRICAL_DISCHARGE_HAZARD_ID,
                "electrical discharge",
                0.0D,
                0.0D,
                0.0D,
                2,
                false,
                true
        );
        EchoAdapterCoreRegistryEntry storm = registry.requireContentId(
                EchoAdapterCoreStandaloneContentBridge.EXTRACTION_STORM_HAZARD_ID
        );
        EchoAdapterCoreHazardRule extractionStorm = new EchoAdapterCoreHazardRule(
                storm.contentId(),
                "",
                "extraction storm",
                30.0D,
                0.0D,
                0.0D,
                1,
                false,
                false,
                true
        );
        return new EchoAdapterCoreHazardTable(rules, extractionStorm);
    }

    public Optional<EchoAdapterCoreHazardRule> contactHazardFor(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byLiveVoxelId.get(block.id()));
    }

    public EchoAdapterCoreHazardRule extractionStorm() {
        return extractionStorm;
    }

    public int contactRuleCount() {
        return byLiveVoxelId.size();
    }

    private static void addContact(
            EchoAdapterCoreStandaloneRegistry registry,
            Map<String, EchoAdapterCoreHazardRule> rules,
            String sourceLiveVoxelId,
            String hazardContentId,
            String label,
            double ashExposurePerMinute,
            double hydrationDrainPerMinute,
            double hungerDrainPerMinute,
            int healthDamagePerPulse,
            boolean unstableGround,
            boolean electricalDischarge
    ) {
        EchoAdapterCoreRegistryEntry block = registry.findLiveVoxelId(sourceLiveVoxelId).orElseThrow(() ->
                new IllegalArgumentException("No AdapterCore live voxel entry for " + sourceLiveVoxelId));
        EchoAdapterCoreRegistryEntry hazard = registry.requireContentId(hazardContentId);
        rules.put(
                block.liveVoxelId(),
                new EchoAdapterCoreHazardRule(
                        hazard.contentId(),
                        block.liveVoxelId(),
                        label,
                        ashExposurePerMinute,
                        hydrationDrainPerMinute,
                        hungerDrainPerMinute,
                        healthDamagePerPulse,
                        unstableGround,
                        electricalDischarge,
                        false
                )
        );
    }
}
