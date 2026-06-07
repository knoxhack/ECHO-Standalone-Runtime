package dev.echo.standalone.runtime.save;

import java.nio.file.Path;
import java.util.Objects;

public record EchoSaveSlot(
        EchoSaveProfile profile,
        String slotId,
        Path root
) {
    public EchoSaveSlot {
        Objects.requireNonNull(profile, "profile");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        Objects.requireNonNull(root, "root");
        root = root.toAbsolutePath().normalize();
    }

    public Path dataRoot() {
        return root.resolve("data");
    }

    public Path manifestPath() {
        return root.resolve("manifest.json");
    }

    public Path transactionsRoot() {
        return root.resolve(".transactions");
    }
}
