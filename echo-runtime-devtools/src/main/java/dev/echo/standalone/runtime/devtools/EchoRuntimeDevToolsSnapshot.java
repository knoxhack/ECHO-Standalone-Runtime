package dev.echo.standalone.runtime.devtools;

import java.util.List;
import java.util.Objects;

public record EchoRuntimeDevToolsSnapshot(
        String runtimeId,
        String runtimeMode,
        List<String> capabilityFlags,
        List<String> serviceTypeNames,
        int serviceCount,
        boolean diagnosticProbeEmitted
) {
    public EchoRuntimeDevToolsSnapshot {
        runtimeId = requireText(runtimeId, "runtimeId");
        runtimeMode = requireText(runtimeMode, "runtimeMode");
        Objects.requireNonNull(capabilityFlags, "capabilityFlags");
        Objects.requireNonNull(serviceTypeNames, "serviceTypeNames");
        capabilityFlags = List.copyOf(capabilityFlags);
        serviceTypeNames = List.copyOf(serviceTypeNames);
        if (serviceCount < 0) {
            throw new IllegalArgumentException("serviceCount must not be negative");
        }
    }

    public boolean ready() {
        return diagnosticProbeEmitted
                && serviceCount == serviceTypeNames.size()
                && !runtimeId.isBlank()
                && !runtimeMode.isBlank();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
