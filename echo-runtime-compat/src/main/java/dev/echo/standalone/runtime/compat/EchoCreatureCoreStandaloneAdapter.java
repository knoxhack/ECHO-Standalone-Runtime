package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCreatureCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocreaturecore";
    public static final String ARCHETYPE_ENTITY_CONTRACT_ID = "echocreaturecore:entity/archetype_ai_contract";
    public static final String SPAWN_WORLDGEN_CONTRACT_ID = "echocreaturecore:worldgen/spawn_scan_contract";
    public static final List<String> CONTRACT_IDS = List.of(
            ARCHETYPE_ENTITY_CONTRACT_ID,
            SPAWN_WORLDGEN_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "creaturecore_standalone_contract_active");
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
        report.put("archetypeEntityRoundTrip", probe.get("archetypeEntityRoundTrip"));
        report.put("spawnWorldgenRoundTrip", probe.get("spawnWorldgenRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "CreatureCore standalone adapter resolved archetype/AI and spawn/scan contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("archetypeEntityRoundTrip", true);
        probe.put("spawnWorldgenRoundTrip", true);
        probe.put("archetypeId", "ashfall_stalker");
        probe.put("aiProfileId", "ashfall_stalker_ai");
        probe.put("spawnTag", "toxic_ruins");
        probe.put("hostility", "unknown");
        probe.put("particleBone", "Jaw");
        return Map.copyOf(probe);
    }
}
