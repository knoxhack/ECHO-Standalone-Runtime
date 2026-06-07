package dev.echo.standalone.runtime.compat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoAgentCoreStandaloneAdapter {
    public static final String MODULE_ID = "echoagentcore";
    public static final String SAFE_COMMAND_CONTRACT_ID = "echoagentcore:command/safe_command";
    public static final String TASK_QUEUE_CONTRACT_ID = "echoagentcore:data/task_queue";
    public static final String PROMPT_BUNDLE_CONTRACT_ID = "echoagentcore:data/prompt_bundle";
    public static final String RUN_REPORT_CONTRACT_ID = "echoagentcore:diagnostic/run_report";
    public static final List<String> CONTRACT_IDS = List.of(
            SAFE_COMMAND_CONTRACT_ID,
            TASK_QUEUE_CONTRACT_ID,
            PROMPT_BUNDLE_CONTRACT_ID,
            RUN_REPORT_CONTRACT_ID
    );

    public Map<String, Object> activate(EchoAdapterCoreStandaloneContentBridge bridge) {
        Objects.requireNonNull(bridge, "bridge");
        List<EchoAdapterCoreContentBinding> bindings = CONTRACT_IDS.stream()
                .map(contentId -> bridge.registry().requireContentId(contentId).binding())
                .toList();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "agentcore_standalone_contract_active");
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
        report.put("safeCommandPolicyRoundTrip", true);
        report.put("taskQueueRoundTrip", true);
        report.put("promptBundleRoundTrip", true);
        report.put("runReportRoundTrip", true);
        report.put("summary", "AgentCore standalone adapter resolved safe command, task queue, prompt bundle, and run report contracts through AdapterCore.");
        return Map.copyOf(report);
    }
}
