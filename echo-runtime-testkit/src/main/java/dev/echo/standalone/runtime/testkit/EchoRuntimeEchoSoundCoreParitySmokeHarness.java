package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.audio.EchoSoundCoreStandaloneAdapter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeEchoSoundCoreParitySmokeHarness {
    private EchoRuntimeEchoSoundCoreParitySmokeHarness() {
    }

    public static void main(String[] args) {
        Map<String, Object> nativeDispatch = executeNativeReferenceDispatch("echo-native-m17");
        EchoSoundCoreStandaloneAdapter standaloneAdapter = new EchoSoundCoreStandaloneAdapter();
        Map<String, Object> standaloneDispatch = standaloneAdapter.executeDispatch("echo-native-m17");
        Map<String, Object> standaloneActivation = standaloneAdapter.activate();

        require(nativeReferenceDispatchPassed(nativeDispatch), "native SoundCore reference dispatch should pass");
        require(standaloneAdapter.referenceDispatchPassed(standaloneDispatch), "standalone SoundCore dispatch should pass");
        require(Boolean.TRUE.equals(standaloneActivation.get("audioDispatchExecuted")),
                "standalone activation should execute audio dispatch");
        require(nativeDispatch.get("adapterCoreContract").equals(standaloneDispatch.get("adapterCoreContract")),
                "native and standalone audio contracts should match");
        require(nativeDispatch.get("profileId").equals(standaloneDispatch.get("profileId")),
                "native and standalone profile ids should match");
        require(nativeDispatch.get("backendId").equals(standaloneDispatch.get("backendId")),
                "native and standalone backend ids should match");
        require(nativeDispatch.get("volumeProfile").equals(standaloneDispatch.get("volumeProfile")),
                "native and standalone volume profiles should match");
        require(nativeDispatch.get("audioEvents").equals(standaloneDispatch.get("audioEvents")),
                "native and standalone audio events should match");
        require(nativeDispatch.get("networkActions").equals(standaloneDispatch.get("networkActions")),
                "native and standalone network actions should match");
        require(nativeDispatch.get("diagnostics").equals(standaloneDispatch.get("diagnostics")),
                "native and standalone diagnostics should match");

        System.out.println("echosoundcore parity smoke PASS contract="
                + nativeDispatch.get("adapterCoreContract")
                + " profile="
                + nativeDispatch.get("profileId")
                + " events="
                + ((List<?>) nativeDispatch.get("audioEvents")).size());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static Map<String, Object> executeNativeReferenceDispatch(String packId) {
        Map<String, Object> dispatch = new LinkedHashMap<>();
        dispatch.put("adapterCoreContract", EchoSoundCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID);
        dispatch.put("service", "echosoundcore:sound_service");
        dispatch.put("dispatchExecuted", true);
        dispatch.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        dispatch.put("profileId", EchoSoundCoreStandaloneAdapter.REFERENCE_PROFILE_ID);
        dispatch.put("backendId", "echo:recording_audio");
        dispatch.put("volumeProfile", "ashfall-debug-volume");
        dispatch.put("audioEvents", List.of(
                event("audio-event-001", "LOOP", "ashfall:ambience_ash_storm", "AMBIENCE", 0.3640D, "weather=ash_storm", 0L),
                event("audio-event-002", "LOOP", "ashfall:music_survival_pulse", "MUSIC", 0.2640D, "mission-status=ACTIVE", 1L),
                event("audio-event-003", "PLAY", "echo:ui_terminal_blip", "UI", 0.3600D, "ui=terminal_confirm", 2L),
                event("audio-event-004", "PLAY", "ashfall:mission_secure_stinger", "STINGER", 0.5100D, "mission-status=ACTIVE", 3L)
        ));
        dispatch.put("networkActions", List.of(
                networkAction("echosoundcore:play_audio_action", "echo:debug-client", "ashfall:mission_secure_stinger", true)
        ));
        dispatch.put("diagnostics", List.of(
                "sound.profile.loaded",
                "sound.ambience.loop.submitted",
                "sound.music.loop.submitted",
                "sound.ui_cue.submitted",
                "sound.stinger.submitted",
                "sound.network_action.ready"
        ));
        dispatch.put("referenceBehavior", "soundcore_dispatches_audio_profile");
        return Map.copyOf(dispatch);
    }

    private static boolean nativeReferenceDispatchPassed(Map<String, Object> dispatch) {
        return Boolean.TRUE.equals(dispatch.get("dispatchExecuted"))
                && EchoSoundCoreStandaloneAdapter.ADAPTERCORE_CONTRACT_ID.equals(dispatch.get("adapterCoreContract"))
                && EchoSoundCoreStandaloneAdapter.REFERENCE_PROFILE_ID.equals(dispatch.get("profileId"))
                && "echo:recording_audio".equals(dispatch.get("backendId"))
                && String.valueOf(dispatch.get("audioEvents")).contains("ashfall:ambience_ash_storm")
                && String.valueOf(dispatch.get("audioEvents")).contains("echo:ui_terminal_blip")
                && String.valueOf(dispatch.get("audioEvents")).contains("ashfall:mission_secure_stinger")
                && String.valueOf(dispatch.get("networkActions")).contains("echosoundcore:play_audio_action")
                && String.valueOf(dispatch.get("diagnostics")).contains("sound.network_action.ready");
    }

    private static Map<String, Object> event(
            String eventId,
            String action,
            String clipId,
            String bus,
            double effectiveGain,
            String reason,
            long tick
    ) {
        Map<String, Object> event = new LinkedHashMap<>();
        event.put("eventId", eventId);
        event.put("action", action);
        event.put("clipId", clipId);
        event.put("bus", bus);
        event.put("effectiveGain", effectiveGain);
        event.put("reason", reason);
        event.put("tick", tick);
        return Map.copyOf(event);
    }

    private static Map<String, Object> networkAction(String payloadId, String target, String clipId, boolean accepted) {
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("payloadId", payloadId);
        action.put("target", target);
        action.put("clipId", clipId);
        action.put("accepted", accepted);
        return Map.copyOf(action);
    }
}
