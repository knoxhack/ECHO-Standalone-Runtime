package dev.echo.standalone.runtime.data;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoSoundRegistry {
    private final LinkedHashMap<String, EchoSoundDefinition> sounds = new LinkedHashMap<>();
    private boolean frozen;

    public void register(EchoSoundDefinition sound) {
        ensureMutable();
        Objects.requireNonNull(sound, "sound");
        sounds.put(sound.id(), sound);
    }

    public Optional<EchoSoundDefinition> find(String id) {
        return Optional.ofNullable(sounds.get(id));
    }

    public List<EchoSoundDefinition> sounds() {
        return sounds.values().stream()
                .sorted(Comparator.comparing(EchoSoundDefinition::id))
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
            throw new IllegalStateException("Sound registry is frozen");
        }
    }
}
