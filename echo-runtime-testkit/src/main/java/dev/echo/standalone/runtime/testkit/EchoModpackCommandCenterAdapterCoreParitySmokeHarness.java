package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoModpackCommandCenterStandaloneAdapter;

import java.util.Map;

public final class EchoModpackCommandCenterAdapterCoreParitySmokeHarness {
    private EchoModpackCommandCenterAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoModpackCommandCenterStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "Command Center standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "Command Center standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("catalogSummaryRoundTrip")),
                "Command Center standalone adapter should preserve catalog behavior");
        require(Boolean.TRUE.equals(activation.get("readinessRoundTrip")),
                "Command Center standalone adapter should preserve readiness behavior");
        require(Boolean.TRUE.equals(activation.get("localToolingRoundTrip")),
                "Command Center standalone adapter should preserve local tooling behavior");
        require(Boolean.TRUE.equals(activation.get("launcherMetadataRoundTrip")),
                "Command Center standalone adapter should preserve launcher metadata behavior");
        require(Boolean.TRUE.equals(activation.get("reportBundleRoundTrip")),
                "Command Center standalone adapter should preserve report bundle behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require(Integer.valueOf(3).equals(probe.get("featureTotal"))
                        && Integer.valueOf(2).equals(probe.get("implementedCount")),
                "Command Center catalog contract should preserve feature summary counts");
        require(Integer.valueOf(82).equals(probe.get("readinessScore"))
                        && "mods-folder".equals(probe.get("nextActionId")),
                "Command Center readiness contract should preserve score and next action priority");
        require("configured".equals(probe.get("executorStatus"))
                        && "echo".equals(probe.get("launcherProjectSlug"))
                        && "adaptercore-domain-matrix".equals(probe.get("reportBundleId")),
                "Command Center tooling contracts should preserve executor, launcher, and report identifiers");

        requireEntry(bridge, EchoModpackCommandCenterStandaloneAdapter.CATALOG_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "commandcenter.data.catalog");
        requireEntry(bridge, EchoModpackCommandCenterStandaloneAdapter.READINESS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "commandcenter.diagnostics.readiness");
        requireEntry(bridge, EchoModpackCommandCenterStandaloneAdapter.LOCAL_TOOLING_CONTRACT_ID,
                EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "commandcenter.commands.local_tooling");
        requireEntry(bridge, EchoModpackCommandCenterStandaloneAdapter.LAUNCHER_METADATA_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PACKS, "commandcenter.packs.launcher_metadata");
        requireEntry(bridge, EchoModpackCommandCenterStandaloneAdapter.REPORT_BUNDLE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ASSETS, "commandcenter.assets.report_bundle");
        System.out.println("commandcenter adaptercore parity smoke PASS contracts="
                + EchoModpackCommandCenterStandaloneAdapter.CONTRACT_IDS.size());
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
