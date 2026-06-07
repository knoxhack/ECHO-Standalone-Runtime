package dev.echo.standalone.runtime.gameplay;

import java.util.List;
import java.util.Map;

public record EchoStorySaveState(
        boolean terminalOpen,
        boolean dataDriveRead,
        boolean missionStarted,
        String activeMissionId,
        List<String> unlockedArchiveIds,
        List<String> unlockedChapterIds,
        Map<String, Boolean> flags,
        Map<String, Integer> gameplayStats,
        List<String> loreUpdates,
        List<String> presenceLinks
) {
    public EchoStorySaveState {
        activeMissionId = activeMissionId == null ? "" : activeMissionId;
        unlockedArchiveIds = List.copyOf(unlockedArchiveIds);
        unlockedChapterIds = List.copyOf(unlockedChapterIds);
        flags = Map.copyOf(flags);
        gameplayStats = Map.copyOf(gameplayStats);
        loreUpdates = List.copyOf(loreUpdates);
        presenceLinks = List.copyOf(presenceLinks);
    }
}
