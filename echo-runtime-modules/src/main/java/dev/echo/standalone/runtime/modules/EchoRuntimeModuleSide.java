package dev.echo.standalone.runtime.modules;

import java.util.Optional;

public enum EchoRuntimeModuleSide {
    COMMON("common"),
    CLIENT("client"),
    SERVER("server"),
    DEV("dev"),
    BOTH("both");

    private final String id;

    EchoRuntimeModuleSide(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimeModuleSide> fromId(String id) {
        for (EchoRuntimeModuleSide side : values()) {
            if (side.id.equals(id)) {
                return Optional.of(side);
            }
        }
        return Optional.empty();
    }
}
