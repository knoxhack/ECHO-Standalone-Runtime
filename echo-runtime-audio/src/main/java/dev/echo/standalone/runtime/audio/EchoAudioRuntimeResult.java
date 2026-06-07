package dev.echo.standalone.runtime.audio;

import java.util.List;
import java.util.Objects;

public record EchoAudioRuntimeResult(
        EchoAudioBackend backend,
        EchoAudioClipRegistry clipRegistry,
        EchoAudioVolumeProfile volumeProfile,
        EchoAudioMixer mixer,
        EchoAudioCuePlanner cuePlanner,
        EchoAudioCuePlan initialCuePlan,
        List<EchoAudioPlaybackEvent> initialEvents
) {
    public EchoAudioRuntimeResult {
        Objects.requireNonNull(backend, "backend");
        Objects.requireNonNull(clipRegistry, "clipRegistry");
        Objects.requireNonNull(volumeProfile, "volumeProfile");
        Objects.requireNonNull(mixer, "mixer");
        Objects.requireNonNull(cuePlanner, "cuePlanner");
        Objects.requireNonNull(initialCuePlan, "initialCuePlan");
        Objects.requireNonNull(initialEvents, "initialEvents");
        initialEvents = List.copyOf(initialEvents);
    }
}
