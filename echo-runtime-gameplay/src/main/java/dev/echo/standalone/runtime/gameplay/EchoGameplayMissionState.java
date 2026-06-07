package dev.echo.standalone.runtime.gameplay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoGameplayMissionState {
    private final String missionId;
    private final String title;
    private final LinkedHashMap<String, EchoGameplayMissionObjective> objectives = new LinkedHashMap<>();

    public EchoGameplayMissionState(String missionId, String title, List<EchoGameplayMissionObjective> objectives) {
        this.missionId = EchoGameplayText.requireText(missionId, "missionId");
        this.title = EchoGameplayText.requireText(title, "title");
        Objects.requireNonNull(objectives, "objectives");
        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("objectives must not be empty");
        }
        for (EchoGameplayMissionObjective objective : objectives) {
            registerObjective(objective);
        }
    }

    public synchronized String missionId() {
        return missionId;
    }

    public synchronized String title() {
        return title;
    }

    public synchronized EchoGameplayMissionStatus status() {
        return completedObjectiveCount() == objectiveCount()
                ? EchoGameplayMissionStatus.COMPLETED
                : EchoGameplayMissionStatus.ACTIVE;
    }

    public synchronized boolean completeObjective(String objectiveId) {
        String normalized = EchoGameplayText.requireText(objectiveId, "objectiveId");
        EchoGameplayMissionObjective objective = objectives.get(normalized);
        if (objective == null) {
            throw new IllegalArgumentException("Unknown objective id: " + normalized);
        }
        if (objective.completed()) {
            return false;
        }
        objectives.put(normalized, objective.complete());
        return true;
    }

    public synchronized Optional<EchoGameplayMissionObjective> objective(String objectiveId) {
        String normalized = EchoGameplayText.requireText(objectiveId, "objectiveId");
        return Optional.ofNullable(objectives.get(normalized));
    }

    public synchronized List<EchoGameplayMissionObjective> objectives() {
        return List.copyOf(objectives.values());
    }

    public synchronized int objectiveCount() {
        return objectives.size();
    }

    public synchronized int completedObjectiveCount() {
        return (int) objectives.values().stream()
                .filter(EchoGameplayMissionObjective::completed)
                .count();
    }

    public synchronized int progressPercent() {
        return (int) Math.round(100.0D * completedObjectiveCount() / objectiveCount());
    }

    private void registerObjective(EchoGameplayMissionObjective objective) {
        Objects.requireNonNull(objective, "objective");
        if (objectives.containsKey(objective.objectiveId())) {
            throw new IllegalArgumentException("Duplicate objective id: " + objective.objectiveId());
        }
        objectives.put(objective.objectiveId(), objective);
    }
}
