package dev.echo.standalone.runtime.app;

import java.util.Objects;
import java.util.Optional;

public record EchoStandaloneLauncherResult(
        EchoStandaloneLauncherRequest request,
        EchoStandaloneLauncherDetection detection,
        EchoStandaloneLauncherVerification verification,
        EchoStandaloneLauncherRepairPlan repairPlan,
        EchoStandaloneLauncherHandoffPlan handoffPlan,
        Optional<EchoStandaloneSupportBundle> supportBundle,
        Optional<EchoRuntimeBootResult> launchResult,
        boolean handoffPreserved
) {
    public EchoStandaloneLauncherResult {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(detection, "detection");
        Objects.requireNonNull(verification, "verification");
        Objects.requireNonNull(repairPlan, "repairPlan");
        Objects.requireNonNull(handoffPlan, "handoffPlan");
        Objects.requireNonNull(supportBundle, "supportBundle");
        Objects.requireNonNull(launchResult, "launchResult");
    }

    public boolean launched() {
        return launchResult.isPresent();
    }

    public boolean ready() {
        return detection.standaloneWorkspace() && verification.ready();
    }
}
