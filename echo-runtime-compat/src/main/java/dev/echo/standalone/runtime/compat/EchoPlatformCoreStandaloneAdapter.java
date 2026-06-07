package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoPlatformCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoplatformcore";
    public static final String MODULE_IDENTITY_CONTRACT_ID = "echoplatformcore:data/module_identity";
    public static final String CAPABILITY_REPORT_CONTRACT_ID = "echoplatformcore:diagnostic/capability_report";
    public static final String TRUST_POLICY_CONTRACT_ID = "echoplatformcore:data/trust_policy";
    public static final List<String> CONTRACT_IDS = List.of(
            MODULE_IDENTITY_CONTRACT_ID,
            CAPABILITY_REPORT_CONTRACT_ID,
            TRUST_POLICY_CONTRACT_ID
    );
    public static final List<String> PLATFORM_FEATURES = List.of(
            "platform.contracts",
            "platform.capabilities",
            "platform.roles",
            "platform.permissions",
            "platform.api_stability",
            "platform.deprecations",
            "adapter.neoforge",
            "adapter.native_planned"
    );
    public static final List<String> PLATFORM_PERMISSIONS = List.of(
            "registry.blocks",
            "registry.items",
            "registry.entities",
            "registry.sounds",
            "registry.menus",
            "network.clientbound",
            "network.serverbound",
            "world.read",
            "world.write",
            "player.data",
            "save.migrate",
            "ui.screens",
            "ui.hud",
            "resources.assets",
            "resources.data",
            "pack.read",
            "pack.modify",
            "diagnostics.write",
            "ai.safe_actions",
            "bridge.execute_safe_action"
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "platformcore_standalone_contract_active");
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
        report.put("platformFeatureCount", PLATFORM_FEATURES.size());
        report.put("platformPermissionCount", PLATFORM_PERMISSIONS.size());
        report.put("platformFeatures", PLATFORM_FEATURES);
        report.put("platformPermissions", PLATFORM_PERMISSIONS);
        report.put("summary", "PlatformCore standalone adapter resolved module identity, capability report, and trust policy contracts through AdapterCore.");
        return Map.copyOf(report);
    }
}
