package dev.echo.standalone.runtime.entity;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoEntityStore {
    private final LinkedHashMap<EchoEntityId, EchoEntityState> entities = new LinkedHashMap<>();
    private List<EchoEntityState> allView;
    private List<EchoEntityState> livingView;
    private List<EchoEntityState> hostileView;
    private long version;

    public synchronized void register(EchoEntityState state) {
        Objects.requireNonNull(state, "state");
        if (entities.containsKey(state.id())) {
            throw new IllegalArgumentException("Duplicate entity id: " + state.id().value());
        }
        entities.put(state.id(), state);
        invalidateViews();
    }

    public synchronized EchoEntityState update(EchoEntityState state) {
        Objects.requireNonNull(state, "state");
        if (!entities.containsKey(state.id())) {
            throw new IllegalArgumentException("Unknown entity id: " + state.id().value());
        }
        entities.put(state.id(), state);
        invalidateViews();
        return state;
    }

    public synchronized Optional<EchoEntityState> find(EchoEntityId id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(entities.get(id));
    }

    public synchronized EchoEntityState require(EchoEntityId id) {
        return find(id).orElseThrow(() -> new IllegalArgumentException("Unknown entity id: " + id.value()));
    }

    public synchronized Optional<EchoEntityState> remove(EchoEntityId id) {
        Objects.requireNonNull(id, "id");
        Optional<EchoEntityState> removed = Optional.ofNullable(entities.remove(id));
        if (removed.isPresent()) {
            invalidateViews();
        }
        return removed;
    }

    public synchronized List<EchoEntityState> all() {
        if (allView == null) {
            allView = List.copyOf(entities.values());
        }
        return allView;
    }

    public synchronized List<EchoEntityState> living() {
        if (livingView == null) {
            livingView = filteredView(EntityView.LIVING);
        }
        return livingView;
    }

    public synchronized List<EchoEntityState> hostile() {
        if (hostileView == null) {
            hostileView = filteredView(EntityView.HOSTILE);
        }
        return hostileView;
    }

    public synchronized Map<EchoEntityId, EchoEntityState> snapshot() {
        return Map.copyOf(entities);
    }

    public synchronized int count() {
        return entities.size();
    }

    public synchronized long version() {
        return version;
    }

    public synchronized void clear() {
        entities.clear();
        invalidateViews();
    }

    private void invalidateViews() {
        version++;
        allView = null;
        livingView = null;
        hostileView = null;
    }

    private List<EchoEntityState> filteredView(EntityView view) {
        ArrayList<EchoEntityState> filtered = new ArrayList<>();
        for (EchoEntityState entity : entities.values()) {
            if ((view == EntityView.LIVING && entity.alive())
                    || (view == EntityView.HOSTILE && entity.hostile())) {
                filtered.add(entity);
            }
        }
        return List.copyOf(filtered);
    }

    private enum EntityView {
        LIVING,
        HOSTILE
    }
}
