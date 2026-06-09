package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoPackCoreStandaloneAdapter;
import dev.echo.standalone.runtime.compat.EchoReportCoreStandaloneAdapter;

import java.util.List;
import java.util.Map;

public final class EchoModpackCommandCenterAdapterCoreParitySmokeHarness {
    private EchoModpackCommandCenterAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoPackCoreStandaloneAdapter packCore = new EchoPackCoreStandaloneAdapter();
        Map<String, Object> activation = packCore.activate();
        @SuppressWarnings("unchecked")
        Map<String, Object> loadPlan = (Map<String, Object>) activation.get("packLoadPlan");
        @SuppressWarnings("unchecked")
        Map<String, Object> profile = (Map<String, Object>) loadPlan.get("profile");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> steps = (List<Map<String, Object>>) loadPlan.get("loadPlan");

        require(Boolean.TRUE.equals(activation.get("activated")),
                "PackCore modpack command-center adapter should activate");
        require(Boolean.TRUE.equals(activation.get("packLoadPlanExecuted")),
                "PackCore should execute the Ashfall load-plan contract");
        require(packCore.referencePlanPassed(loadPlan),
                "PackCore load-plan reference behavior should pass");
        require(String.valueOf(profile.get("requiredModuleIds")).contains("echoreportcore")
                        && String.valueOf(profile.get("requiredModuleIds")).contains("echoterminal"),
                "Ashfall pack profile should route command-center diagnostics and terminal surfaces");
        require(steps.stream().anyMatch(step -> "activate_player_surfaces".equals(step.get("id"))
                        && String.valueOf(step.get("target")).contains("echoterminal")),
                "PackCore load plan should activate command-center terminal surfaces");
        require(steps.stream().anyMatch(step -> "emit_repair_preview".equals(step.get("id"))
                        && Boolean.TRUE.equals(step.get("requiresConfirmation"))),
                "PackCore command-center repair preview should remain confirmation-gated");

        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> reportCore = new EchoReportCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(reportCore.get("activated")),
                "ReportCore command-center diagnostics should activate through AdapterCore");
        require(Boolean.TRUE.equals(reportCore.get("allRuntimeAliasesRegistered")),
                "ReportCore command-center diagnostics should register every AdapterCore runtime alias");
        require(Boolean.TRUE.equals(reportCore.get("supportBundleRoundTrip")),
                "ReportCore command-center diagnostics should preserve support bundle behavior");
        require(Boolean.TRUE.equals(reportCore.get("releaseReadinessRoundTrip")),
                "ReportCore command-center diagnostics should preserve release readiness behavior");
        requireEntry(bridge, EchoReportCoreStandaloneAdapter.SUPPORT_BUNDLE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS);
        requireEntry(bridge, EchoReportCoreStandaloneAdapter.RELEASE_READINESS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA);

        System.out.println("modpack command center adaptercore parity smoke PASS pack="
                + EchoPackCoreStandaloneAdapter.REFERENCE_PACK_ID
                + " steps="
                + steps.size()
                + " reportContracts="
                + EchoReportCoreStandaloneAdapter.CONTRACT_IDS.size());
    }

    private static void requireEntry(
            EchoAdapterCoreStandaloneContentBridge bridge,
            String contentId,
            EchoAdapterCoreContentKind contentKind,
            EchoAdapterCoreDomain domain
    ) {
        EchoAdapterCoreRegistryEntry entry = bridge.registry().requireContentId(contentId);
        require(entry.contentKind() == contentKind,
                contentId + " should use content kind " + contentKind);
        require(entry.domain() == domain,
                contentId + " should use AdapterCore domain " + domain.id());
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
