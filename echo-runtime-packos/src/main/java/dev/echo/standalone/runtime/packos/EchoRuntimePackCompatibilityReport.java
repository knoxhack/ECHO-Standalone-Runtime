package dev.echo.standalone.runtime.packos;

import java.util.List;
import java.util.Objects;

public record EchoRuntimePackCompatibilityReport(
        boolean compatible,
        List<String> blockers,
        List<String> warnings
) {
    public EchoRuntimePackCompatibilityReport {
        Objects.requireNonNull(blockers, "blockers");
        Objects.requireNonNull(warnings, "warnings");
        blockers = List.copyOf(blockers);
        warnings = List.copyOf(warnings);
    }
}
