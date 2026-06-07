package dev.echo.standalone.runtime.contracts;

import java.util.Optional;

public enum EchoRuntimeTickLayer {
    PRE_TICK("pre_tick"),
    INPUT("input"),
    NETWORK("network"),
    WORLD("world"),
    ENTITY("entity"),
    PLAYER("player"),
    GAMEPLAY("gameplay"),
    UI("ui"),
    AUDIO("audio"),
    RENDER("render"),
    SAVE("save"),
    POST_TICK("post_tick");

    private final String id;

    EchoRuntimeTickLayer(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimeTickLayer> fromId(String id) {
        for (EchoRuntimeTickLayer layer : values()) {
            if (layer.id.equals(id)) {
                return Optional.of(layer);
            }
        }
        return Optional.empty();
    }
}
