package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.compat.EchoAdapterCoreContentKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreDomain;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRegistryEntry;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreRuntimeKind;
import dev.echo.standalone.runtime.compat.EchoAdapterCoreStandaloneContentBridge;
import dev.echo.standalone.runtime.compat.EchoCinematicCoreStandaloneAdapter;

import java.util.Map;

public final class EchoCinematicCoreAdapterCoreParitySmokeHarness {
    private EchoCinematicCoreAdapterCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        EchoAdapterCoreStandaloneContentBridge bridge = EchoAdapterCoreStandaloneContentBridge.ashfallLive();
        Map<String, Object> activation = new EchoCinematicCoreStandaloneAdapter().activate(bridge);
        require(Boolean.TRUE.equals(activation.get("activated")),
                "CinematicCore standalone adapter should activate through AdapterCore");
        require(Boolean.TRUE.equals(activation.get("allRuntimeAliasesRegistered")),
                "CinematicCore standalone adapter should register aliases for every AdapterCore runtime");
        require(Boolean.TRUE.equals(activation.get("sequenceRenderRoundTrip")),
                "CinematicCore standalone adapter should preserve sequence rendering behavior");
        require(Boolean.TRUE.equals(activation.get("pacingRenderRoundTrip")),
                "CinematicCore standalone adapter should preserve pacing behavior");
        require(Boolean.TRUE.equals(activation.get("triggerUiRoundTrip")),
                "CinematicCore standalone adapter should preserve trigger UI behavior");

        @SuppressWarnings("unchecked")
        Map<String, Object> probe = (Map<String, Object>) activation.get("referenceProbe");
        require("ashfall/intro_stinger".equals(probe.get("normalizedSequenceId"))
                        && "camera/drop_pod".equals(probe.get("normalizedPathId")),
                "CinematicCore rendering contract should normalize sequence and path ids");
        require(Double.valueOf(1.0D).equals(probe.get("pacingUrgency"))
                        && Boolean.TRUE.equals(probe.get("screenshotModeAllowed")),
                "CinematicCore rendering contract should clamp pacing and preserve screenshot flag");
        require("mission/started".equals(probe.get("normalizedTriggerId")),
                "CinematicCore UI contract should normalize trigger ids");

        requireEntry(bridge, EchoCinematicCoreStandaloneAdapter.SEQUENCE_RENDER_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.RENDERING, "cinematiccore.rendering.sequence_contract_normalization");
        requireEntry(bridge, EchoCinematicCoreStandaloneAdapter.PACING_RENDER_CONTRACT_ID,
                EchoAdapterCoreContentKind.DATA_COMPONENT, EchoAdapterCoreDomain.RENDERING, "cinematiccore.rendering.pacing_envelope");
        requireEntry(bridge, EchoCinematicCoreStandaloneAdapter.TRIGGER_UI_CONTRACT_ID,
                EchoAdapterCoreContentKind.UI_SCREEN, EchoAdapterCoreDomain.UI_SCREENS, "cinematiccore.ui.trigger_overlay_contract");
        System.out.println("cinematiccore adaptercore parity smoke PASS contracts="
                + EchoCinematicCoreStandaloneAdapter.CONTRACT_IDS.size());
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
