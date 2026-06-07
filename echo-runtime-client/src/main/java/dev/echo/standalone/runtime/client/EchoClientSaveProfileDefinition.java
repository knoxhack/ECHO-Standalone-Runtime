package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.save.EchoSaveProfile;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

record EchoClientSaveProfileDefinition(
        String schema,
        String profileId,
        String displayName,
        String packId,
        int formatVersion,
        Map<String, String> metadata
) {
    EchoClientSaveProfileDefinition {
        if (schema == null || schema.isBlank()) {
            throw new IllegalArgumentException("schema must not be blank");
        }
        if (profileId == null || profileId.isBlank()) {
            throw new IllegalArgumentException("profileId must not be blank");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }
        if (packId == null || packId.isBlank()) {
            throw new IllegalArgumentException("packId must not be blank");
        }
        if (formatVersion < 1) {
            throw new IllegalArgumentException("formatVersion must be positive");
        }
        metadata = Map.copyOf(new TreeMap<>(metadata == null ? Map.of() : metadata));
    }

    EchoSaveProfile toSaveProfile(Path root) {
        return new EchoSaveProfile(
                schema,
                profileId,
                displayName,
                packId,
                formatVersion,
                root,
                metadata
        );
    }
}
