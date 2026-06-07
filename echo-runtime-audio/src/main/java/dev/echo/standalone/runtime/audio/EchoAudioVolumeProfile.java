package dev.echo.standalone.runtime.audio;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record EchoAudioVolumeProfile(
        String profileId,
        Map<EchoAudioBus, Double> busVolumes,
        boolean muted
) {
    public EchoAudioVolumeProfile {
        profileId = EchoAudioText.requireText(profileId, "profileId");
        Objects.requireNonNull(busVolumes, "busVolumes");
        EnumMap<EchoAudioBus, Double> copy = new EnumMap<>(EchoAudioBus.class);
        for (EchoAudioBus bus : EchoAudioBus.values()) {
            double volume = busVolumes.getOrDefault(bus, 1.0D);
            if (volume < 0.0D || volume > 1.0D) {
                throw new IllegalArgumentException("bus volume must be between zero and one: " + bus);
            }
            copy.put(bus, volume);
        }
        busVolumes = Map.copyOf(copy);
    }

    public double gainFor(EchoAudioBus bus, double baseGain) {
        Objects.requireNonNull(bus, "bus");
        if (muted) {
            return 0.0D;
        }
        return round(baseGain * busVolumes.get(EchoAudioBus.MASTER) * busVolumes.get(bus));
    }

    public EchoAudioVolumeProfile withMuted(boolean muted) {
        return new EchoAudioVolumeProfile(profileId, busVolumes, muted);
    }

    public EchoAudioVolumeProfile withMasterVolume(double volume) {
        return withBusVolume(EchoAudioBus.MASTER, volume);
    }

    public EchoAudioVolumeProfile withBusVolume(EchoAudioBus bus, double volume) {
        Objects.requireNonNull(bus, "bus");
        EnumMap<EchoAudioBus, Double> volumes = new EnumMap<>(EchoAudioBus.class);
        volumes.putAll(busVolumes);
        volumes.put(bus, volume);
        return new EchoAudioVolumeProfile(profileId, volumes, muted);
    }

    static double round(double value) {
        return Math.round(value * 10000.0D) / 10000.0D;
    }
}
