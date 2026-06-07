package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoMissionRegistry {
    private final LinkedHashMap<String, EchoMissionDefinition> missions = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoMissionDefinition mission) {
        ensureMutable();
        Objects.requireNonNull(mission, "mission");
        missions.put(mission.id(), mission);
    }

    public Optional<EchoMissionDefinition> find(String id) {
        return Optional.ofNullable(missions.get(id));
    }

    public List<EchoMissionDefinition> missions() {
        return missions.values().stream()
                .sorted(Comparator.comparing(EchoMissionDefinition::id))
                .toList();
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Mission registry is frozen");
        }
    }
}
