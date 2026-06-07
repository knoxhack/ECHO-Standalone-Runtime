package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoTextureForgeStandaloneAdapter;

import java.util.Map;

public final class EchoTextureForgeAdapterCoreParitySmokeHarness {
    private EchoTextureForgeAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoTextureForgeStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "TextureForge standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "TextureForge standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("specRegistryRoundTrip")),
                "TextureForge standalone adapter should preserve spec registry behavior");
        require(Boolean.TRUE.equals(activation.get("promptExportRoundTrip")),
                "TextureForge standalone adapter should preserve prompt export behavior");
        require(Boolean.TRUE.equals(activation.get("reviewStateRoundTrip")),
                "TextureForge standalone adapter should preserve review state behavior");
        require(Boolean.TRUE.equals(activation.get("textureAuditRoundTrip")),
                "TextureForge standalone adapter should preserve audit behavior");
        require(Boolean.TRUE.equals(activation.get("dashboardSurfaceResolved")),
                "TextureForge standalone adapter should resolve dashboard surface behavior");
        requireEntry(bridge, EchoTextureForgeStandaloneAdapter.SPEC_REGISTRY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ASSETS, "textureforge.assets.spec_registry");
        requireEntry(bridge, EchoTextureForgeStandaloneAdapter.PROMPT_EXPORT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.ASSETS, "textureforge.assets.prompt_export");
        requireEntry(bridge, EchoTextureForgeStandaloneAdapter.REVIEW_STATE_CONTRACT_ID,
                EchoAdapterCoreContentKind.SAVE_RECORD, EchoAdapterCoreDomain.DATA, "textureforge.data.review_state");
        requireEntry(bridge, EchoTextureForgeStandaloneAdapter.TEXTURE_AUDIT_CONTRACT_ID,
                EchoAdapterCoreContentKind.DIAGNOSTIC, EchoAdapterCoreDomain.DIAGNOSTICS, "textureforge.diagnostics.texture_audit");
        requireEntry(bridge, EchoTextureForgeStandaloneAdapter.DASHBOARD_CONTRACT_ID,
                EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "textureforge.ui.dashboard");
        require(bridge.registry().entriesForDomain(EchoAdapterCoreDomain.ASSETS).stream()
                        .anyMatch(entry -> entry.binding().moduleId().equals(EchoTextureForgeStandaloneAdapter.MODULE_ID)),
                "TextureForge assets domain should be backed by standalone AdapterCore bindings");
        System.out.println("textureforge adaptercore parity smoke PASS contracts="
                + EchoTextureForgeStandaloneAdapter.CONTRACT_IDS.size());
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
