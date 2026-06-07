package dev.echo.standalone.runtime.compat;

import java.util.Objects;

public record EchoAdapterCoreScavengeReward(
        String sourceBlockContentId,
        String sourceLiveVoxelId,
        String lootTableContentId,
        int waterRations,
        int foodRations,
        int repairKits,
        String message
) {
    public EchoAdapterCoreScavengeReward {
        sourceBlockContentId = EchoCompatText.requireText(sourceBlockContentId, "sourceBlockContentId");
        sourceLiveVoxelId = EchoCompatText.requireText(sourceLiveVoxelId, "sourceLiveVoxelId");
        lootTableContentId = EchoCompatText.requireText(lootTableContentId, "lootTableContentId");
        message = EchoCompatText.requireText(message, "message");
        if (waterRations < 0 || foodRations < 0 || repairKits < 0) {
            throw new IllegalArgumentException("scavenge reward counts must not be negative");
        }
        Objects.requireNonNull(message, "message");
    }

    public boolean rewarded() {
        return waterRations > 0 || foodRations > 0 || repairKits > 0;
    }
}
