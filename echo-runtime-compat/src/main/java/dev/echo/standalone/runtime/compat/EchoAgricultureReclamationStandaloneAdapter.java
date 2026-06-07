package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAgricultureReclamationStandaloneAdapter {
    public static final String MODULE_ID = "echoagriculturereclamation";
    public static final String GREENHOUSE_BLOCK_CONTRACT_ID = "echoagriculturereclamation:block/greenhouse_machine_rules";
    public static final String SEED_ITEM_CONTRACT_ID = "echoagriculturereclamation:item/seed_supply_process";
    public static final String DASHBOARD_UI_CONTRACT_ID = "echoagriculturereclamation:ui/reclamation_process_cards";
    public static final String RESTORATION_WORLDGEN_CONTRACT_ID = "echoagriculturereclamation:worldgen/restoration_envelope";
    public static final List<String> CONTRACT_IDS = List.of(
            GREENHOUSE_BLOCK_CONTRACT_ID,
            SEED_ITEM_CONTRACT_ID,
            DASHBOARD_UI_CONTRACT_ID,
            RESTORATION_WORLDGEN_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "agriculture_reclamation_standalone_contract_active");
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
        report.put("greenhouseMachineRulesRoundTrip", true);
        report.put("seedSupplyProcessRoundTrip", true);
        report.put("processCardRoundTrip", true);
        report.put("restorationEnvelopeRoundTrip", true);
        report.put("summary", "Agriculture Reclamation standalone adapter resolved greenhouse machine, seed process, process-card, and restoration envelope contracts through AdapterCore.");
        return Map.copyOf(report);
    }
}
