package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCameraCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCameraCoreAdapterCoreParitySmokeHarness {
    private EchoCameraCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCameraCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "CameraCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "CameraCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("renderProfileRoundTrip")),
                "CameraCore standalone adapter should preserve rendering profile behavior");
        require(Boolean.TRUE.equals(activation.get("shakeSafetyRoundTrip")),
                "CameraCore standalone adapter should preserve shake/safety behavior");
        require(Boolean.TRUE.equals(activation.get("inputTargetRoundTrip")),
                "CameraCore standalone adapter should preserve input target behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("prime/screenshot_mode".equals(probe.get("normalizedProfileId")),
                "CameraCore rendering contract should normalize profile ids");
        require("ashfall/nexus_burst".equals(probe.get("normalizedShakeId"))
                        && Double.valueOf(1.0D).equals(probe.get("shakeIntensity"))
                        && Double.valueOf(0.0D).equals(probe.get("maxFovChange")),
                "CameraCore rendering contract should clamp shake/safety values");
        require("Player Head".equals(probe.get("targetAnchor")),
                "CameraCore input contract should trim target anchor names");

        requireEntry(bridge, EchoCameraCoreStandaloneAdapter.RENDER_PROFILE_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.RENDERING, "cameracore.rendering.profile_contract_normalization");
        requireEntry(bridge, EchoCameraCoreStandaloneAdapter.SHAKE_SAFETY_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.RENDERING, "cameracore.rendering.shake_safety_envelope");
        requireEntry(bridge, EchoCameraCoreStandaloneAdapter.INPUT_TARGET_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.INPUT, "cameracore.input.target_anchor_contract");
        System.out.println("cameracore adaptercore parity smoke PASS contracts="
                + EchoCameraCoreStandaloneAdapter.CONTRACT_IDS.size());
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
