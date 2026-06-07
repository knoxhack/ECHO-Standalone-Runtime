package dev.echo.standalone.runtime.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public final class EchoMenuRegistry {
    private final LinkedHashMap<String, EchoMenuDefinition> menus = new LinkedHashMap<>();

    public void register(EchoMenuDefinition menu) {
        Objects.requireNonNull(menu, "menu");
        menus.put(menu.id().toLowerCase(Locale.ROOT), menu);
    }

    public Optional<EchoMenuDefinition> find(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(menus.get(id.toLowerCase(Locale.ROOT)));
    }

    public List<EchoMenuDefinition> menus() {
        return List.copyOf(menus.values());
    }
}
