package dev.echo.standalone.runtime.audio;

import java.util.Objects;

public record EchoAudioPlaybackRequest(
        String requestId,
        EchoAudioPlaybackAction action,
        EchoAudioClip clip,
        String reason,
        long tick
) {
    public EchoAudioPlaybackRequest {
        requestId = EchoAudioText.requireText(requestId, "requestId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(clip, "clip");
        reason = EchoAudioText.requireText(reason, "reason");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
    }
}
