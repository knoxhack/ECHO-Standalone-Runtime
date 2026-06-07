package dev.echo.standalone.runtime.audio;

import java.util.Objects;

public record EchoAudioPlaybackEvent(
        String eventId,
        EchoAudioPlaybackAction action,
        EchoAudioClip clip,
        EchoAudioBus bus,
        double effectiveGain,
        String reason,
        long tick
) {
    public EchoAudioPlaybackEvent {
        eventId = EchoAudioText.requireText(eventId, "eventId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(clip, "clip");
        Objects.requireNonNull(bus, "bus");
        if (effectiveGain < 0.0D || effectiveGain > 1.0D) {
            throw new IllegalArgumentException("effectiveGain must be between zero and one");
        }
        reason = EchoAudioText.requireText(reason, "reason");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
    }
}
