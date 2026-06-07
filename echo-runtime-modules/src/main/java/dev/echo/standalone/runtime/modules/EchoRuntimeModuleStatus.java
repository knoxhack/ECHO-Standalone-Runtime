package dev.echo.standalone.runtime.modules;

import java.util.Optional;

public enum EchoRuntimeModuleStatus {
    RUNTIME_ACTIVE("runtime-active"),
    RUNTIME_TOOLING_ONLY("runtime-tooling-only"),
    RUNTIME_DEV_ONLY("runtime-dev-only"),
    RUNTIME_DISABLED_WITH_REASON("runtime-disabled-with-reason");

    private final String id;

    EchoRuntimeModuleStatus(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static Optional<EchoRuntimeModuleStatus> fromId(String id) {
        for (EchoRuntimeModuleStatus status : values()) {
            if (status.id.equals(id)) {
                return Optional.of(status);
            }
        }
        return Optional.empty();
    }
}
