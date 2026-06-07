package dev.echo.standalone.runtime.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoUiModalStack {
    private final ArrayList<EchoUiModal> modals = new ArrayList<>();

    public synchronized void open(EchoUiModal modal) {
        modals.add(Objects.requireNonNull(modal, "modal"));
    }

    public synchronized Optional<EchoUiModal> closeTop() {
        if (modals.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(modals.removeLast());
    }

    public synchronized Optional<EchoUiModal> top() {
        if (modals.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(modals.getLast());
    }

    public synchronized List<EchoUiModal> snapshot() {
        return List.copyOf(modals);
    }

    public synchronized int size() {
        return modals.size();
    }
}
