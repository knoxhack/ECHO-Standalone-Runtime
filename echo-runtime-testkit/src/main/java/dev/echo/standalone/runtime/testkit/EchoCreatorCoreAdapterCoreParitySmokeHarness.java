package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCreatorCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCreatorCoreAdapterCoreParitySmokeHarness {
    private EchoCreatorCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCreatorCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "CreatorCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "CreatorCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("commandPermissionRoundTrip")),
                "CreatorCore standalone adapter should preserve command permission behavior");
        require(Boolean.TRUE.equals(activation.get("sessionDataRoundTrip")),
                "CreatorCore standalone adapter should preserve session data behavior");
        require(Boolean.TRUE.equals(activation.get("packProjectRoundTrip")),
                "CreatorCore standalone adapter should preserve pack project behavior");
        require(Boolean.TRUE.equals(activation.get("dashboardUiRoundTrip")),
                "CreatorCore standalone adapter should preserve dashboard UI form behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("default".equals(probe.get("fallbackProjectId"))
                        && "BLOCKED".equals(probe.get("defaultPermission"))
                        && Boolean.TRUE.equals(probe.get("developerCanCreate")),
                "CreatorCore command/data/pack contracts should preserve permission and project defaults");
        require("generic".equals(probe.get("schemaType"))
                        && Integer.valueOf(2).equals(probe.get("fieldCount")),
                "CreatorCore UI contract should preserve form schema defaults");

        requireEntry(bridge, EchoCreatorCoreStandaloneAdapter.COMMAND_PERMISSION_CONTRACT_ID,
                EchoAdapterCoreContentKind.COMMAND, EchoAdapterCoreDomain.COMMANDS, "creatorcore.commands.permission_gate_contract");
        requireEntry(bridge, EchoCreatorCoreStandaloneAdapter.SESSION_DATA_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.DATA, "creatorcore.data.session_project_contract");
        requireEntry(bridge, EchoCreatorCoreStandaloneAdapter.PACK_PROJECT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.PACKS, "creatorcore.packs.project_authoring_contract");
        requireEntry(bridge, EchoCreatorCoreStandaloneAdapter.DASHBOARD_UI_CONTRACT_ID,
                EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "creatorcore.ui.dashboard_form_contract");
        System.out.println("creatorcore adaptercore parity smoke PASS contracts="
                + EchoCreatorCoreStandaloneAdapter.CONTRACT_IDS.size());
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
