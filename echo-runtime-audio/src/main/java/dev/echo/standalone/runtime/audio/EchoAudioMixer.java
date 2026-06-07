package dev.echo.standalone.runtime.audio;

import java.util.Objects;

public final class EchoAudioMixer {
    private final EchoAudioBackend backend;
    private EchoAudioVolumeProfile profile;

    public EchoAudioMixer(EchoAudioBackend backend, EchoAudioVolumeProfile profile) {
        this.backend = Objects.requireNonNull(backend, "backend");
        this.profile = Objects.requireNonNull(profile, "profile");
    }

    public synchronized EchoAudioPlaybackEvent submit(EchoAudioPlaybackRequest request) {
        return backend.submit(request, profile);
    }

    public synchronized EchoAudioVolumeProfile profile() {
        return profile;
    }

    public synchronized void setProfile(EchoAudioVolumeProfile profile) {
        this.profile = Objects.requireNonNull(profile, "profile");
    }
}
