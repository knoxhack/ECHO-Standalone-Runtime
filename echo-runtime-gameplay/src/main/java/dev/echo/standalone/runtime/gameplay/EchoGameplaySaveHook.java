package dev.echo.standalone.runtime.gameplay;

import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public final class EchoGameplaySaveHook {
    private final EchoGameplayMissionState mission;
    private final EchoSurvivalState survival;
    private final EchoProgressionState progression;
    private final EchoFactionRuntime factions;
    private final EchoNotificationLog notifications;

    public EchoGameplaySaveHook(
            EchoGameplayMissionState mission,
            EchoSurvivalState survival,
            EchoProgressionState progression,
            EchoFactionRuntime factions,
            EchoNotificationLog notifications
    ) {
        this.mission = Objects.requireNonNull(mission, "mission");
        this.survival = Objects.requireNonNull(survival, "survival");
        this.progression = Objects.requireNonNull(progression, "progression");
        this.factions = Objects.requireNonNull(factions, "factions");
        this.notifications = Objects.requireNonNull(notifications, "notifications");
    }

    public EchoGameplaySaveResult save(EchoSaveRuntimeResult saves, String slotId, String transactionId)
            throws IOException {
        Objects.requireNonNull(saves, "saves");
        EchoSaveTransaction transaction = saves.beginTransaction(slotId, transactionId);
        ArrayList<String> paths = new ArrayList<>();
        write(transaction, paths, "gameplay/summary.json",
                EchoGameplayJsonWriter.summary(mission, survival, progression, factions, notifications));
        write(transaction, paths, "gameplay/mission.json", EchoGameplayJsonWriter.mission(mission));
        write(transaction, paths, "gameplay/survival.json", EchoGameplayJsonWriter.survival(survival));
        write(transaction, paths, "gameplay/progression.json", EchoGameplayJsonWriter.progression(progression));
        write(transaction, paths, "gameplay/factions.json", EchoGameplayJsonWriter.factions(factions));
        write(transaction, paths, "gameplay/notifications.json", EchoGameplayJsonWriter.notifications(notifications));
        return new EchoGameplaySaveResult(
                transaction.commit(Map.of(
                        "missionId", mission.missionId(),
                        "missionStatus", mission.status().name(),
                        "progressionLevel", Integer.toString(progression.level())
                )),
                paths
        );
    }

    private static void write(EchoSaveTransaction transaction, ArrayList<String> paths, String path, String content) {
        transaction.writeText(path, content);
        paths.add(path);
    }
}
