package dev.echo.standalone.runtime.contracts;

import java.util.Optional;

public enum EchoRuntimeLifecycle {
    CREATED("created"),
    BOOTSTRAPPING("bootstrapping"),
    LOADING_PACKOS("loading_packos"),
    LOADING_MODULES("loading_modules"),
    RESOLVING_DEPENDENCIES("resolving_dependencies"),
    LOADING_ASSETS("loading_assets"),
    LOADING_CONFIG("loading_config"),
    LOADING_SAVE("loading_save"),
    INITIALIZING_SERVICES("initializing_services"),
    STARTING_RENDERER("starting_renderer"),
    STARTING_AUDIO("starting_audio"),
    STARTING_NETWORK("starting_network"),
    STARTING_GAME_LOOP("starting_game_loop"),
    RUNNING("running"),
    PAUSED("paused"),
    STOPPING("stopping"),
    STOPPED("stopped"),
    FAILED("failed"),
    CRASHED("crashed"),
    RECOVERING("recovering");

    private final String id;

    EchoRuntimeLifecycle(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimeLifecycle> fromId(String id) {
        for (EchoRuntimeLifecycle lifecycle : values()) {
            if (lifecycle.id.equals(id)) {
                return Optional.of(lifecycle);
            }
        }
        return Optional.empty();
    }
}
