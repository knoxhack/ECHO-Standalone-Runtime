package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoReportCoreStandaloneAdapter;

import java.util.Map;

public final class EchoReportCoreAdapterCoreParitySmokeHarness {
    private EchoReportCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoReportCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "ReportCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "ReportCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("supportBundleRoundTrip")),
                "ReportCore standalone adapter should preserve support bundle behavior");
        require(Boolean.TRUE.equals(activation.get("releaseReadinessRoundTrip")),
                "ReportCore standalone adapter should preserve release readiness behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require(Boolean.TRUE.equals(probe.get("supportBundleLocalOnly"))
                        && Boolean.TRUE.equals(probe.get("supportBundleSecretsRedacted")),
                "ReportCore diagnostics contract should force local-only redacted support bundles");
        require("PASS".equals(probe.get("releaseStatus"))
                        && "reports/echo/diagnostics.json".equals(probe.get("artifactPath")),
                "ReportCore data contract should preserve release status and normalized artifact paths");

        requireEntry(bridge, EchoReportCoreStandaloneAdapter.SUPPORT_BUNDLE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "reportcore.diagnostics.support_bundle");
        requireEntry(bridge, EchoReportCoreStandaloneAdapter.RELEASE_READINESS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "reportcore.data.release_readiness");
        System.out.println("reportcore adaptercore parity smoke PASS contracts="
                + EchoReportCoreStandaloneAdapter.CONTRACT_IDS.size());
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
