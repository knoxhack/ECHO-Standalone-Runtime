package dev.echo.standalone.runtime.ui;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EchoMenuDefinition(
        String id,
        String title,
        List<EchoMenuOption> options,
        int selectedIndex
) {
    public EchoMenuDefinition {
        id = requireText(id, "id");
        title = requireText(title, "title");
        Objects.requireNonNull(options, "options");
        if (options.isEmpty()) {
            throw new IllegalArgumentException("options must not be empty");
        }
        options = List.copyOf(options);
        if (selectedIndex < 0 || selectedIndex >= options.size()) {
            selectedIndex = 0;
        }
    }

    public EchoMenuOption selectedOption() {
        return options.get(selectedIndex);
    }

    public Optional<EchoMenuOption> option(String idOrAction) {
        String normalized = idOrAction == null ? "" : idOrAction.trim();
        if (normalized.isBlank()) {
            return Optional.empty();
        }
        return options.stream()
                .filter(option -> option.id().equals(normalized) || option.action().equals(normalized))
                .findFirst();
    }

    public long enabledCount() {
        return options.stream().filter(EchoMenuOption::enabled).count();
    }

    public List<EchoMenuOption> sortedOptions() {
        return options.stream()
                .sorted(Comparator.comparing(EchoMenuOption::id))
                .toList();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
