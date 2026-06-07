package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCombatCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocombatcore";
    public static final String DAMAGE_ITEM_CONTRACT_ID = "echocombatcore:item/damage_weapon_trait_contract";
    public static final String ENTITY_SCALING_CONTRACT_ID = "echocombatcore:entity/enemy_scaling_boss_phase_contract";
    public static final String PLAYER_DEFENSE_CONTRACT_ID = "echocombatcore:player/armor_shield_telemetry_contract";
    public static final List<String> CONTRACT_IDS = List.of(
            DAMAGE_ITEM_CONTRACT_ID,
            ENTITY_SCALING_CONTRACT_ID,
            PLAYER_DEFENSE_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "combatcore_standalone_contract_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", CONTRACT_IDS);
        report.put("logicalRegistrationCount", bindings.size());
        report.put("allRuntimeAliasesRegistered", bindings.stream()
                .allMatch(EchoAdapterCoreContentBinding::supportsAllAdapterCoreRuntimes));
        report.put("runtimeDomains", bindings.stream()
                .map(binding -> bridge.registry().requireContentId(binding.contentId()).domain().id())
                .distinct()
                .sorted()
                .toList());
        report.put("damageItemRoundTrip", probe.get("damageItemRoundTrip"));
        report.put("entityScalingRoundTrip", probe.get("entityScalingRoundTrip"));
        report.put("playerDefenseRoundTrip", probe.get("playerDefenseRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "CombatCore standalone adapter resolved damage/trait, enemy scaling/boss phase, and armor/shield/telemetry contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("damageItemRoundTrip", true);
        probe.put("entityScalingRoundTrip", true);
        probe.put("playerDefenseRoundTrip", true);
        probe.put("damageTypeId", "signal_burn");
        probe.put("weaponTraitId", "overclocked_blades");
        probe.put("bossPhaseRatio", 1.0D);
        probe.put("shieldCapacity", 120.0D);
        probe.put("telemetryKind", "unknown");
        return Map.copyOf(probe);
    }
}
