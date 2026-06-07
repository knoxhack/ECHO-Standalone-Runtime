package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneAlphaReadinessResult(
        String gateId,
        EchoStandaloneAlphaReadinessStatus status,
        List<EchoStandaloneAlphaReadinessCheck> checks,
        EchoStandaloneLauncherResult launcherResult
) {
    public EchoStandaloneAlphaReadinessResult {
        gateId = requireText(gateId, "gateId");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(checks, "checks");
        Objects.requireNonNull(launcherResult, "launcherResult");
        checks = List.copyOf(checks);
        status = checks.stream().anyMatch(EchoStandaloneAlphaReadinessCheck::blocked)
                ? EchoStandaloneAlphaReadinessStatus.BLOCKED
                : status;
    }

    public boolean ready() {
        return status == EchoStandaloneAlphaReadinessStatus.READY;
    }

    public int checkCount() {
        return checks.size();
    }

    public int passedCount() {
        return (int) checks.stream().filter(EchoStandaloneAlphaReadinessCheck::passed).count();
    }

    public int blockedCount() {
        return (int) checks.stream().filter(EchoStandaloneAlphaReadinessCheck::blocked).count();
    }

    public boolean supportBundleReady() {
        return launcherResult.supportBundle()
                .map(EchoStandaloneSupportBundle::complete)
                .orElse(false);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
