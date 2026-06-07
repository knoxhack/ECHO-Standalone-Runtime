package dev.echo.standalone.runtime.packos;

import java.util.Optional;

public enum EchoRuntimePackChannel {
    DEV("dev"),
    ALPHA("alpha"),
    BETA("beta"),
    RELEASE("release");

    private final String id;

    EchoRuntimePackChannel(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimePackChannel> fromId(String id) {
        for (EchoRuntimePackChannel channel : values()) {
            if (channel.id.equals(id)) {
                return Optional.of(channel);
            }
        }
        return Optional.empty();
    }
}
