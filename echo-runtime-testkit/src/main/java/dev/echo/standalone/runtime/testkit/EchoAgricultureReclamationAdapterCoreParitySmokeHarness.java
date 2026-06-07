package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAgricultureReclamationStandaloneAdapter;

import java.util.Map;

public final class EchoAgricultureReclamationAdapterCoreParitySmokeHarness {
    private EchoAgricultureReclamationAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoAgricultureReclamationStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "Agriculture Reclamation standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "Agriculture Reclamation standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("greenhouseMachineRulesRoundTrip")),
                "Agriculture Reclamation standalone adapter should preserve greenhouse machine rules");
        require(Boolean.TRUE.equals(activation.get("seedSupplyProcessRoundTrip")),
                "Agriculture Reclamation standalone adapter should preserve seed supply process behavior");
        require(Boolean.TRUE.equals(activation.get("processCardRoundTrip")),
                "Agriculture Reclamation standalone adapter should preserve process-card behavior");
        require(Boolean.TRUE.equals(activation.get("restorationEnvelopeRoundTrip")),
                "Agriculture Reclamation standalone adapter should preserve restoration envelope behavior");
        requireEntry(bridge, EchoAgricultureReclamationStandaloneAdapter.GREENHOUSE_BLOCK_CONTRACT_ID,
                EchoAdapterCoreContentKind.BLOCK, EchoAdapterCoreDomain.BLOCKS, "agriculture.blocks.greenhouse_machine_rules");
        requireEntry(bridge, EchoAgricultureReclamationStandaloneAdapter.SEED_ITEM_CONTRACT_ID,
                EchoAdapterCoreContentKind.ITEM, EchoAdapterCoreDomain.ITEMS, "agriculture.items.seed_supply_process");
        requireEntry(bridge, EchoAgricultureReclamationStandaloneAdapter.DASHBOARD_UI_CONTRACT_ID,
                EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "agriculture.ui.reclamation_process_cards");
        requireEntry(bridge, EchoAgricultureReclamationStandaloneAdapter.RESTORATION_WORLDGEN_CONTRACT_ID,
                EchoAdapterCoreContentKind.WORLDGEN_DEFINITION, EchoAdapterCoreDomain.WORLDGEN, "agriculture.worldgen.restoration_envelope");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.WORLDGEN).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoAgricultureReclamationStandaloneAdapter.MODULE_ID)),
                "Agriculture Reclamation worldgen domain should be backed by standalone AdapterCore bindings");
        System.out.println("agriculture reclamation adaptercore parity smoke PASS contracts="
                + EchoAgricultureReclamationStandaloneAdapter.CONTRACT_IDS.size());
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
