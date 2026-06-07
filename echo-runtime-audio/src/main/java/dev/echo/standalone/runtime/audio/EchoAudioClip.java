package dev.echo.standalone.runtime.audio;

import java.util.Objects;

public record EchoAudioClip(
        String clipId,
        String displayName,
        String assetKey,
        EchoAudioClipType type,
        EchoAudioBus bus,
        boolean looping,
        double baseGain
) {
    public EchoAudioClip {
        clipId = EchoAudioText.requireText(clipId, "clipId");
        displayName = EchoAudioText.requireText(displayName, "displayName");
        assetKey = EchoAudioText.requireText(assetKey, "assetKey");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(bus, "bus");
        if (baseGain < 0.0D || baseGain > 1.0D) {
            throw new IllegalArgumentException("baseGain must be between zero and one");
        }
    }
}
