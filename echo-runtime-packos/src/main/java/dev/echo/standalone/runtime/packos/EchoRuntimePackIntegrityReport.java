package dev.echo.standalone.runtime.packos;

import java.util.List;
import java.util.Objects;

public record EchoRuntimePackIntegrityReport(
        boolean integrityReady,
        List<String> blockers,
        List<String> warnings
) {
    public EchoRuntimePackIntegrityReport {
        Objects.requireNonNull(blockers, "blockers");
        Objects.requireNonNull(warnings, "warnings");
        blockers = List.copyOf(blockers);
        warnings = List.copyOf(warnings);
    }
}
