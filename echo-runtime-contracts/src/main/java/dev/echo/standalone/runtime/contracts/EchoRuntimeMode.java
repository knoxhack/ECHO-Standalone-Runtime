package dev.echo.standalone.runtime.contracts;

import java.util.Optional;

public enum EchoRuntimeMode {
    HEADLESS_TEST("headless-test"),
    WINDOWED_DEV("windowed-dev"),
    PLAYABLE_BETA("playable-beta"),
    PACKAGED_TESTER("packaged-tester");

    private final String id;

    EchoRuntimeMode(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimeMode> fromId(String id) {
        for (EchoRuntimeMode mode : values()) {
            if (mode.id.equals(id)) {
                return Optional.of(mode);
            }
        }
        return Optional.empty();
    }
}
