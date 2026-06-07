package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneLauncherVerification(List<EchoStandaloneLauncherCheck> checks) {
    public EchoStandaloneLauncherVerification {
        Objects.requireNonNull(checks, "checks");
        checks = List.copyOf(checks);
    }

    public boolean ready() {
        return checks.stream().allMatch(EchoStandaloneLauncherCheck::passed);
    }

    public int passedCount() {
        return (int) checks.stream().filter(EchoStandaloneLauncherCheck::passed).count();
    }

    public int failedCount() {
        return checks.size() - passedCount();
    }
}
