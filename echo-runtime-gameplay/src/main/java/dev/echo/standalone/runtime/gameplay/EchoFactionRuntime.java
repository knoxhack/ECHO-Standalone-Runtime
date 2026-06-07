package dev.echo.standalone.runtime.gameplay;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoFactionRuntime {
    private final LinkedHashMap<String, EchoFactionStanding> standings = new LinkedHashMap<>();

    public synchronized void register(EchoFactionStanding standing) {
        Objects.requireNonNull(standing, "standing");
        if (standings.containsKey(standing.factionId())) {
            throw new IllegalArgumentException("Duplicate faction id: " + standing.factionId());
        }
        standings.put(standing.factionId(), standing);
    }

    public synchronized EchoFactionStanding adjustReputation(String factionId, int delta) {
        String normalized = EchoGameplayText.requireText(factionId, "factionId");
        EchoFactionStanding standing = require(normalized).adjust(delta);
        standings.put(normalized, standing);
        return standing;
    }

    public synchronized Optional<EchoFactionStanding> find(String factionId) {
        String normalized = EchoGameplayText.requireText(factionId, "factionId");
        return Optional.ofNullable(standings.get(normalized));
    }

    public synchronized EchoFactionStanding require(String factionId) {
        String normalized = EchoGameplayText.requireText(factionId, "factionId");
        EchoFactionStanding standing = standings.get(normalized);
        if (standing == null) {
            throw new IllegalArgumentException("Unknown faction id: " + normalized);
        }
        return standing;
    }

    public synchronized List<EchoFactionStanding> all() {
        return List.copyOf(standings.values());
    }

    public synchronized int count() {
        return standings.size();
    }
}
