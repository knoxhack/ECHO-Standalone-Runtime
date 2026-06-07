package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoAssetCoreStandaloneAdapter;

import java.util.Map;

public final class EchoAssetCoreAdapterCoreParitySmokeHarness {
    private EchoAssetCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoAssetCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "AssetCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "AssetCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("assetRegistryRoundTrip")),
                "AssetCore standalone adapter should preserve asset registry behavior");
        require(Boolean.TRUE.equals(activation.get("assetValidationRoundTrip")),
                "AssetCore standalone adapter should preserve validation behavior");
        require(Boolean.TRUE.equals(activation.get("textureForgePromptReady")),
                "AssetCore standalone adapter should preserve TextureForge prompt readiness");
        require(Boolean.TRUE.equals(activation.get("textureForgeReportContractResolved")),
                "AssetCore standalone adapter should preserve TextureForge report contract resolution");
        requireEntry(
                bridge,
                EchoAssetCoreStandaloneAdapter.ASSET_REGISTRY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.ASSETS,
                "assetcore.assets.asset_registry"
        );
        requireEntry(
                bridge,
                EchoAssetCoreStandaloneAdapter.ASSET_VALIDATION_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "assetcore.data.asset_validation"
        );
        requireEntry(
                bridge,
                EchoAssetCoreStandaloneAdapter.TEXTUREFORGE_PROMPTS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.ASSETS,
                "assetcore.assets.textureforge_prompts"
        );
        requireEntry(
                bridge,
                EchoAssetCoreStandaloneAdapter.TEXTUREFORGE_REPORTS_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT,
                EchoAdapterCoreDomain.DATA,
                "assetcore.data.textureforge_reports"
        );
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.ASSETS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoAssetCoreStandaloneAdapter.MODULE_ID)),
                "AssetCore assets domain should be backed by standalone AdapterCore bindings");
        System.out.println("assetcore adaptercore parity smoke PASS contracts="
                + EchoAssetCoreStandaloneAdapter.CONTRACT_IDS.size());
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
