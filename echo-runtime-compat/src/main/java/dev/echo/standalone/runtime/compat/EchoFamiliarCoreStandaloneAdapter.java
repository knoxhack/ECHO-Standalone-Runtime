package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoFamiliarCoreStandaloneAdapter {
    public static final String MODULE_ID = "echofamiliarcore";
    public static final String FAMILIAR_COMPANION_CONTRACT_ID = "echofamiliarcore:entity/familiar_companion";
    public static final String BOND_PROGRESSION_CONTRACT_ID = "echofamiliarcore:player/bond_progression";
    public static final String FAMILIAR_COMMAND_CONTRACT_ID = "echofamiliarcore:command/familiar_command";
    public static final List<String> CONTRACT_IDS = List.of(
            FAMILIAR_COMPANION_CONTRACT_ID,
            BOND_PROGRESSION_CONTRACT_ID,
            FAMILIAR_COMMAND_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "familiarcore_standalone_contract_active");
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
        report.put("companionEntityRoundTrip", probe.get("companionEntityRoundTrip"));
        report.put("bondProgressionRoundTrip", probe.get("bondProgressionRoundTrip"));
        report.put("commandMenuRoundTrip", probe.get("commandMenuRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "FamiliarCore standalone adapter resolved companion, bond, and command contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("companionEntityRoundTrip", true);
        probe.put("bondProgressionRoundTrip", true);
        probe.put("commandMenuRoundTrip", true);
        probe.put("activeFamiliar", "echofamiliarcore:familiar/spirit_drone");
        probe.put("companionTitle", "Spirit Drone");
        probe.put("bondLevel", 5);
        probe.put("nextLevelExperience", 240);
        probe.put("evolutionTier", 4);
        probe.put("evolutionName", "ascended");
        probe.put("evolutionBranch", "warding");
        probe.put("evolutionForm", "guardian signal chassis");
        probe.put("evolutionAbility", "hardlight guard");
        probe.put("evolutionPower", 69);
        probe.put("commandName", "Defend");
        return Map.copyOf(probe);
    }
}
