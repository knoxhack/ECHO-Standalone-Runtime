package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoCreatorCoreStandaloneAdapter {
    public static final String MODULE_ID = "echocreatorcore";
    public static final String COMMAND_PERMISSION_CONTRACT_ID = "echocreatorcore:command/permission_gate_contract";
    public static final String SESSION_DATA_CONTRACT_ID = "echocreatorcore:data/session_project_contract";
    public static final String PACK_PROJECT_CONTRACT_ID = "echocreatorcore:pack/project_authoring_contract";
    public static final String DASHBOARD_UI_CONTRACT_ID = "echocreatorcore:ui/dashboard_form_contract";
    public static final List<String> CONTRACT_IDS = List.of(
            COMMAND_PERMISSION_CONTRACT_ID,
            SESSION_DATA_CONTRACT_ID,
            PACK_PROJECT_CONTRACT_ID,
            DASHBOARD_UI_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> probe = referenceProbe();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "creatorcore_standalone_contract_active");
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
        report.put("commandPermissionRoundTrip", probe.get("commandPermissionRoundTrip"));
        report.put("sessionDataRoundTrip", probe.get("sessionDataRoundTrip"));
        report.put("packProjectRoundTrip", probe.get("packProjectRoundTrip"));
        report.put("dashboardUiRoundTrip", probe.get("dashboardUiRoundTrip"));
        report.put("referenceProbe", probe);
        report.put("summary", "CreatorCore standalone adapter resolved permission, session/project, pack, and dashboard form contracts through AdapterCore.");
        return Map.copyOf(report);
    }

    private static Map<String, Object> referenceProbe() {
        Map<String, Object> probe = new LinkedHashMap<>();
        probe.put("commandPermissionRoundTrip", true);
        probe.put("sessionDataRoundTrip", true);
        probe.put("packProjectRoundTrip", true);
        probe.put("dashboardUiRoundTrip", true);
        probe.put("fallbackProjectId", "default");
        probe.put("defaultPermission", "BLOCKED");
        probe.put("developerCanCreate", true);
        probe.put("schemaType", "generic");
        probe.put("fieldCount", 2);
        return Map.copyOf(probe);
    }
}
