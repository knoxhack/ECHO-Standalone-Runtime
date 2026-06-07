package dev.echo.standalone.runtime.save;

import java.util.List;
import java.util.Objects;

public record EchoSaveMigrationPlan(
        String profileId,
        String slotId,
        int fromVersion,
        int toVersion,
        boolean blocked,
        List<EchoSaveMigrationStep> steps
) {
    public EchoSaveMigrationPlan {
        profileId = EchoSavePaths.requireText(profileId, "profileId");
        slotId = EchoSavePaths.requireText(slotId, "slotId");
        if (fromVersion < 1 || toVersion < 1) {
            throw new IllegalArgumentException("migration versions must be positive");
        }
        Objects.requireNonNull(steps, "steps");
        steps = List.copyOf(steps);
        blocked = blocked || fromVersion > toVersion;
    }
}
