package dev.echo.standalone.runtime.audio;

import java.util.EnumMap;
import java.util.Locale;

public final class EchoAudioVolumeProfiles {
    public static final String ASHFALL_SURVIVAL_MIX_PROFILE_ID = "echoashfallprotocol:audio/volume/survival_mix";

    private EchoAudioVolumeProfiles() {
    }

    public static EchoAudioVolumeProfile resolve(String profileId) {
        String normalized = profileId == null ? "" : profileId.trim().toLowerCase(Locale.ROOT);
        if (normalized.equals(ASHFALL_SURVIVAL_MIX_PROFILE_ID)
                || normalized.equals("echoashfallprotocol:ashfall_survival_mix")
                || normalized.equals("ashfall_survival_mix")) {
            return ashfallSurvivalMix();
        }
        EnumMap<EchoAudioBus, Double> volumes = new EnumMap<>(EchoAudioBus.class);
        volumes.put(EchoAudioBus.MASTER, 0.80D);
        volumes.put(EchoAudioBus.MUSIC, 0.55D);
        volumes.put(EchoAudioBus.AMBIENCE, 0.70D);
        volumes.put(EchoAudioBus.SFX, 0.82D);
        volumes.put(EchoAudioBus.UI, 0.90D);
        volumes.put(EchoAudioBus.STINGER, 0.85D);
        volumes.put(EchoAudioBus.ALERT, 0.92D);
        volumes.put(EchoAudioBus.DIAGNOSTIC, 1.00D);
        return new EchoAudioVolumeProfile("client-default-volume", volumes, false);
    }

    public static EchoAudioVolumeProfile ashfallSurvivalMix() {
        EnumMap<EchoAudioBus, Double> volumes = new EnumMap<>(EchoAudioBus.class);
        volumes.put(EchoAudioBus.MASTER, 0.80D);
        volumes.put(EchoAudioBus.MUSIC, 0.55D);
        volumes.put(EchoAudioBus.AMBIENCE, 0.70D);
        volumes.put(EchoAudioBus.SFX, 0.82D);
        volumes.put(EchoAudioBus.UI, 0.90D);
        volumes.put(EchoAudioBus.STINGER, 0.85D);
        volumes.put(EchoAudioBus.ALERT, 0.92D);
        volumes.put(EchoAudioBus.DIAGNOSTIC, 1.00D);
        return new EchoAudioVolumeProfile("ashfall-debug-volume", volumes, false);
    }
}
