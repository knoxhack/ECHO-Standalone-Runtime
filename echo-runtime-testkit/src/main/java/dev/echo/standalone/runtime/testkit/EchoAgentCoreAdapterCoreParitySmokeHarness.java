package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAgentCoreStandaloneAdapter;

import java.util.Map;

public final class EchoAgentCoreAdapterCoreParitySmokeHarness {
    private EchoAgentCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoAgentCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "AgentCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "AgentCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("safeCommandPolicyRoundTrip")),
                "AgentCore standalone adapter should preserve safe command policy behavior");
        require(Boolean.TRUE.equals(activation.get("taskQueueRoundTrip")),
                "AgentCore standalone adapter should preserve task queue behavior");
        require(Boolean.TRUE.equals(activation.get("promptBundleRoundTrip")),
                "AgentCore standalone adapter should preserve prompt bundle behavior");
        require(Boolean.TRUE.equals(activation.get("runReportRoundTrip")),
                "AgentCore standalone adapter should preserve run report behavior");
        requireEntry(bridge, EchoAgentCoreStandaloneAdapter.SAFE_COMMAND_CONTRACT_ID,
                EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "agentcore.commands.safe_command");
        requireEntry(bridge, EchoAgentCoreStandaloneAdapter.TASK_QUEUE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "agentcore.data.task_queue");
        requireEntry(bridge, EchoAgentCoreStandaloneAdapter.PROMPT_BUNDLE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "agentcore.data.prompt_bundle");
        requireEntry(bridge, EchoAgentCoreStandaloneAdapter.RUN_REPORT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "agentcore.diagnostics.run_report");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.COMMANDS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoAgentCoreStandaloneAdapter.MODULE_ID)),
                "AgentCore commands domain should be backed by standalone AdapterCore bindings");
        System.out.println("agentcore adaptercore parity smoke PASS contracts="
                + EchoAgentCoreStandaloneAdapter.CONTRACT_IDS.size());
    }

    private static void requireEntry(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain,
            String adapterKey
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        require(entry.contentKind() == contentKind,
                contentId + " should use content kind " + contentKind);
        require(entry.domain() == domain,
                contentId + " should use AdapterCore domain " + domain.id());
        require(entry.binding().adapterKey().equals(adapterKey),
                contentId + " should expose stable adapter key " + adapterKey);
        for (EchoAdapterCoreRuntimeKind runtimeKind : EchoAdapterCoreRuntimeKind.values()) {
            require(bridge.registry().findRuntimeId(runtimeKind, entry.idFor(runtimeKind)).isPresent(),
                    contentId + " has unregistered runtime alias " + runtimeKind.adapterId());
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
