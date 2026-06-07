package dev.echo.standalone.runtime.save;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public record EchoSaveProfile(
        String schema,
        String profileId,
        String displayName,
        String packId,
        int formatVersion,
        Path root,
        Map<String, String> metadata
) {
    public EchoSaveProfile {
        schema = EchoSavePaths.requireText(schema, "schema");
        profileId = EchoSavePaths.requireText(profileId, "profileId");
        displayName = EchoSavePaths.requireText(displayName, "displayName");
        packId = EchoSavePaths.requireText(packId, "packId");
        if (formatVersion < 1) {
            throw new IllegalArgumentException("formatVersion must be positive");
        }
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(metadata, "metadata");
        root = root.toAbsolutePath().normalize();
        metadata = Map.copyOf(new TreeMap<>(metadata));
    }

    public EchoSaveSlot slot(String slotId) {
        return new EchoSaveSlot(this, slotId, root.resolve("slots").resolve(slotId));
    }
}
