package dev.echo.standalone.runtime.data;

import java.util.List;
import java.util.Objects;

public record EchoSoundDefinition(
        String id,
        String subtitle,
        List<String> soundAssets,
        String sourceLogicalId
) {
    public EchoSoundDefinition {
        id = EchoDataPaths.requireText(id, "id");
        subtitle = subtitle == null ? "" : subtitle;
        Objects.requireNonNull(soundAssets, "soundAssets");
        sourceLogicalId = EchoDataPaths.requireText(sourceLogicalId, "sourceLogicalId");
        soundAssets = soundAssets.stream().sorted().toList();
    }
}
