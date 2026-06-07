package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoValidationCoreStandaloneAdapter;

import java.util.Map;

public final class EchoValidationCoreAdapterCoreParitySmokeHarness {
    private EchoValidationCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoValidationCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "ValidationCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "ValidationCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Integer.valueOf(0).equals(activation.get("validationRuleCount")),
                "ValidationCore clean reference engine should match native empty rule count");
        require(Integer.valueOf(0).equals(activation.get("diagnosticCount")),
                "ValidationCore clean reference report should match native empty diagnostic count");
        require("INFO".equals(activation.get("highestSeverity")),
                "ValidationCore clean reference report should match native INFO severity");
        require(Boolean.TRUE.equals(activation.get("serviceCodeExecuted")),
                "ValidationCore standalone adapter should execute validation service behavior");
        require(Boolean.TRUE.equals(activation.get("validationEngineRoundTrip")),
                "ValidationCore standalone adapter should exercise validation engine behavior");
        require(Boolean.TRUE.equals(activation.get("packValidationRoundTrip")),
                "ValidationCore standalone adapter should exercise pack validation behavior");
        require(Boolean.TRUE.equals(activation.get("diagnosticReportRoundTrip")),
                "ValidationCore standalone adapter should exercise diagnostic report behavior");
        require(Boolean.TRUE.equals(activation.get("repairSuggestionRoundTrip")),
                "ValidationCore standalone adapter should exercise repair suggestion behavior");
        requireEntry(
                bridge,
                EchoValidationCoreStandaloneAdapter.PACK_VALIDATION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "validationcore.data.pack_validation"
        );
        requireEntry(
                bridge,
                EchoValidationCoreStandaloneAdapter.DIAGNOSTIC_REPORT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC,
                EchoAdapterCoreDomain.DIAGNOSTICS,
                "validationcore.diagnostics.diagnostic_report"
        );
        requireEntry(
                bridge,
                EchoValidationCoreStandaloneAdapter.REPAIR_SUGGESTION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC,
                EchoAdapterCoreDomain.DIAGNOSTICS,
                "validationcore.diagnostics.repair_suggestion"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.DIAGNOSTICS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoValidationCoreStandaloneAdapter.MODULE_ID)),
                "ValidationCore diagnostics domain should be backed by standalone AdapterCore bindings");
        System.out.println("validationcore adaptercore parity smoke PASS contracts="
                + EchoValidationCoreStandaloneAdapter.CONTRACT_IDS.size());
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
