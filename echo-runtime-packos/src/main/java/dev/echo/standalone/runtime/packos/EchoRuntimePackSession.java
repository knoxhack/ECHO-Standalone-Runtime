package dev.echo.standalone.runtime.packos;

import java.util.Objects;

public record EchoRuntimePackSession(
        EchoRuntimePackProfile profile,
        EchoRuntimePackLockfile lockfile,
        EchoRuntimePackMountPlan mountPlan,
        EchoRuntimePackIntegrityReport integrityReport,
        EchoRuntimePackCompatibilityReport compatibilityReport,
        EchoRuntimePackRepairPlan repairPlan
) {
    public EchoRuntimePackSession {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(lockfile, "lockfile");
        Objects.requireNonNull(mountPlan, "mountPlan");
        Objects.requireNonNull(integrityReport, "integrityReport");
        Objects.requireNonNull(compatibilityReport, "compatibilityReport");
        Objects.requireNonNull(repairPlan, "repairPlan");
    }

    public boolean launchAllowed() {
        return integrityReport.integrityReady() && compatibilityReport.compatible();
    }

    public String packId() {
        return profile.packId();
    }
}
