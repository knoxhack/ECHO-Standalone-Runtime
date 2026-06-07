package dev.echo.standalone.runtime.save;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

public record EchoSaveManifest(
        String schema,
        String profileId,
        String slotId,
        String packId,
        int formatVersion,
        String createdAt,
        String updatedAt,
        List<EchoSaveFileState> files,
        List<String> backupIds,
        Map<String, String> metadata
) {
    public EchoSaveManifest {
        schema = EchoSavePaths.requireText(schema, "schema");
        profileId = EchoSavePaths.requireText(profileId, "profileId");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        packId = EchoSavePaths.requireText(packId, "packId");
        if (formatVersion < 1) {
            throw new IllegalArgumentException("formatVersion must be positive");
        }
        createdAt = EchoSavePaths.requireText(createdAt, "createdAt");
        updatedAt = EchoSavePaths.requireText(updatedAt, "updatedAt");
        Objects.requireNonNull(files, "files");
        Objects.requireNonNull(backupIds, "backupIds");
        Objects.requireNonNull(metadata, "metadata");
        files = files.stream()
                .sorted(Comparator.comparing(EchoSaveFileState::relativePath))
                .toList();
        backupIds = backupIds.stream().sorted().toList();
        metadata = Map.copyOf(new TreeMap<>(metadata));
    }

    public Optional<EchoSaveFileState> file(String relativePath) {
        String normalized = EchoSavePaths.requireRelativePath(relativePath, "relativePath");
        return files.stream()
                .filter(file -> file.relativePath().equals(normalized))
                .findFirst();
    }
}
