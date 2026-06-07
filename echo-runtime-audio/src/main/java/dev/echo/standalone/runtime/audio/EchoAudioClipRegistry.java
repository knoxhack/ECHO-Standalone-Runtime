package dev.echo.standalone.runtime.audio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoAudioClipRegistry {
    private final LinkedHashMap<String, EchoAudioClip> clips = new LinkedHashMap<>();

    public synchronized void register(EchoAudioClip clip) {
        Objects.requireNonNull(clip, "clip");
        if (clips.containsKey(clip.clipId())) {
            throw new IllegalArgumentException("Duplicate audio clip id: " + clip.clipId());
        }
        clips.put(clip.clipId(), clip);
    }

    public synchronized Optional<EchoAudioClip> find(String clipId) {
        String normalized = EchoAudioText.requireText(clipId, "clipId");
        return Optional.ofNullable(clips.get(normalized));
    }

    public synchronized EchoAudioClip require(String clipId) {
        String normalized = EchoAudioText.requireText(clipId, "clipId");
        EchoAudioClip clip = clips.get(normalized);
        if (clip == null) {
            throw new IllegalArgumentException("Unknown audio clip id: " + normalized);
        }
        return clip;
    }

    public synchronized List<EchoAudioClip> all() {
        return List.copyOf(clips.values());
    }

    public synchronized List<EchoAudioClip> byBus(EchoAudioBus bus) {
        Objects.requireNonNull(bus, "bus");
        return clips.values().stream()
                .filter(clip -> clip.bus() == bus)
                .toList();
    }

    public synchronized int count() {
        return clips.size();
    }
}
