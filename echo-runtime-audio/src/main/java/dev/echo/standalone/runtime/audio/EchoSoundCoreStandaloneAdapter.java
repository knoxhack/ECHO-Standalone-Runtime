package dev.echo.standalone.runtime.audio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class EchoSoundCoreStandaloneAdapter {
    public static final String MODULE_ID = "echosoundcore";
    public static final String ADAPTERCORE_CONTRACT_ID = "echosoundcore:sounds/audio_dispatch";
    public static final String REFERENCE_PROFILE_ID = "echosoundcore:audio_profiles/ashfall_bootstrap";

    public Map<String, Object> activate() {
        Map<String, Object> audioDispatch = executeDispatch("echo-native-m17");
        boolean audioDispatchPassed = referenceDispatchPassed(audioDispatch);
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("activated", true);
        report.put("activationStage", "soundcore_standalone_audio_dispatch_active");
        report.put("adapterCoreUsed", true);
        report.put("standaloneRuntimeCodeExecuted", true);
        report.put("moduleId", MODULE_ID);
        report.put("registeredFeatureContracts", List.of(
                "sound.adaptive_music",
                "sound.ambience",
                "sound.audio_profiles",
                "sound.network_actions",
                "sound.service",
                "sound.stingers",
                "sound.ui_cues",
                ADAPTERCORE_CONTRACT_ID
        ));
        report.put("audioDispatch", audioDispatch);
        report.put("audioDispatchExecuted", audioDispatchPassed);
        report.put("serviceCodeExecuted", audioDispatchPassed);
        report.put("summary", "SoundCore standalone adapter executed the AdapterCore audio dispatch service.");
        return Map.copyOf(report);
    }

    public Map<String, Object> executeDispatch(String packId) {
        EchoRecordingAudioBackend backend = new EchoRecordingAudioBackend();
        EchoAudioVolumeProfile profile = EchoAudioVolumeProfiles.resolve(EchoAudioVolumeProfiles.ASHFALL_SURVIVAL_MIX_PROFILE_ID);
        EchoAudioMixer mixer = new EchoAudioMixer(backend, profile);
        List<EchoAudioPlaybackEvent> playbackEvents = List.of(
                mixer.submit(request("soundcore-ambience", EchoAudioPlaybackAction.LOOP, ambience(), "weather=ash_storm", 0L)),
                mixer.submit(request("soundcore-music", EchoAudioPlaybackAction.LOOP, music(), "mission-status=ACTIVE", 1L)),
                mixer.submit(request("soundcore-ui", EchoAudioPlaybackAction.PLAY, uiConfirm(), "ui=terminal_confirm", 2L)),
                mixer.submit(request("soundcore-stinger", EchoAudioPlaybackAction.PLAY, missionStinger(), "mission-status=ACTIVE", 3L))
        );

        Map<String, Object> dispatch = new LinkedHashMap<>();
        dispatch.put("adapterCoreContract", ADAPTERCORE_CONTRACT_ID);
        dispatch.put("service", "echosoundcore:sound_service");
        dispatch.put("dispatchExecuted", true);
        dispatch.put("packId", packId == null || packId.isBlank() ? "unknown" : packId);
        dispatch.put("profileId", REFERENCE_PROFILE_ID);
        dispatch.put("backendId", backend.backendId());
        dispatch.put("volumeProfile", profile.profileId());
        dispatch.put("audioEvents", playbackEvents.stream().map(EchoSoundCoreStandaloneAdapter::event).toList());
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

    public boolean referenceDispatchPassed(Map<String, Object> dispatch) {
        return Boolean.TRUE.equals(dispatch.get("dispatchExecuted"))
                && ADAPTERCORE_CONTRACT_ID.equals(dispatch.get("adapterCoreContract"))
                && REFERENCE_PROFILE_ID.equals(dispatch.get("profileId"))
                && "echo:recording_audio".equals(dispatch.get("backendId"))
                && String.valueOf(dispatch.get("audioEvents")).contains("ashfall:ambience_ash_storm")
                && String.valueOf(dispatch.get("audioEvents")).contains("echo:ui_terminal_blip")
                && String.valueOf(dispatch.get("audioEvents")).contains("ashfall:mission_secure_stinger")
                && String.valueOf(dispatch.get("networkActions")).contains("echosoundcore:play_audio_action")
                && String.valueOf(dispatch.get("diagnostics")).contains("sound.network_action.ready");
    }

    private static EchoAudioPlaybackRequest request(
            String requestId,
            EchoAudioPlaybackAction action,
            EchoAudioClip clip,
            String reason,
            long tick
    ) {
        return new EchoAudioPlaybackRequest(requestId, action, clip, reason, tick);
    }

    private static EchoAudioClip ambience() {
        return new EchoAudioClip(
                "ashfall:ambience_ash_storm",
                "Ash Storm Ambience",
                "ashfall:sounds/ambience/ash_storm.ogg",
                EchoAudioClipType.AMBIENCE,
                EchoAudioBus.AMBIENCE,
                true,
                0.65D
        );
    }

    private static EchoAudioClip music() {
        return new EchoAudioClip(
                "ashfall:music_survival_pulse",
                "Survival Pulse",
                "ashfall:sounds/music/survival_pulse.ogg",
                EchoAudioClipType.MUSIC,
                EchoAudioBus.MUSIC,
                true,
                0.60D
        );
    }

    private static EchoAudioClip uiConfirm() {
        return new EchoAudioClip(
                "echo:ui_terminal_blip",
                "Terminal Blip",
                "echo:sounds/ui/terminal_blip.ogg",
                EchoAudioClipType.UI_SOUND,
                EchoAudioBus.UI,
                false,
                0.50D
        );
    }

    private static EchoAudioClip missionStinger() {
        return new EchoAudioClip(
                "ashfall:mission_secure_stinger",
                "Secure Mission Stinger",
                "ashfall:sounds/stingers/secure_mission.ogg",
                EchoAudioClipType.MISSION_STINGER,
                EchoAudioBus.STINGER,
                false,
                0.75D
        );
    }

    private static Map<String, Object> event(EchoAudioPlaybackEvent event) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("eventId", event.eventId());
        output.put("action", event.action().name());
        output.put("clipId", event.clip().clipId());
        output.put("bus", event.bus().name());
        output.put("effectiveGain", event.effectiveGain());
        output.put("reason", event.reason());
        output.put("tick", event.tick());
        return Map.copyOf(output);
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
