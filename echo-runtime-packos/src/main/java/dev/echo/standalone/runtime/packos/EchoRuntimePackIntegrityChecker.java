package dev.echo.standalone.runtime.packos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRuntimePackIntegrityChecker {
    public EchoRuntimePackIntegrityReport check(EchoRuntimePackProfile profile, EchoRuntimePackLockfile lockfile) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(lockfile, "lockfile");
        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (!profile.packId().equals(lockfile.packId())) {
            blockers.add("Pack profile id does not match lockfile pack id.");
        }
        if (!profile.runtimeVersion().equals(lockfile.runtimeVersion())) {
            warnings.add("Pack profile runtime version differs from lockfile runtime version.");
        }
        for (String moduleId : lockfile.lockedModules().keySet()) {
            if (!profile.enabledModules().contains(moduleId)) {
                warnings.add("Lockfile pins a module that is not enabled by the profile: " + moduleId);
            }
        }
        for (String moduleId : profile.enabledModules()) {
            if (!lockfile.lockedModules().containsKey(moduleId)) {
                warnings.add("Profile enables a module not present in lockfile: " + moduleId);
            }
        }
        for (String feature : profile.enabledFeatures()) {
            if (!lockfile.lockedFeatures().contains(feature)) {
                warnings.add("Profile enables a feature not present in lockfile: " + feature);
            }
        }

        return new EchoRuntimePackIntegrityReport(blockers.isEmpty(), blockers, warnings);
    }
}
