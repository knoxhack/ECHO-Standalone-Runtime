package dev.echo.standalone.runtime.packos;

import dev.echo.standalone.runtime.modules.EchoRuntimeFeatureGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleGraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoRuntimePackCompatibilityChecker {
    public EchoRuntimePackCompatibilityReport check(
            EchoRuntimePackProfile profile,
            EchoRuntimePackLockfile lockfile,
            EchoRuntimeModuleGraph moduleGraph,
            EchoRuntimeFeatureGraph featureGraph
    ) {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(lockfile, "lockfile");
        Objects.requireNonNull(moduleGraph, "moduleGraph");
        Objects.requireNonNull(featureGraph, "featureGraph");
        List<String> blockers = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (String moduleId : profile.enabledModules()) {
            if (!moduleGraph.moduleIds().contains(moduleId)) {
                blockers.add("Enabled module is not discovered: " + moduleId);
            }
            if (moduleGraph.failedModuleIds().contains(moduleId)) {
                blockers.add("Enabled module failed module runtime validation: " + moduleId);
            }
            if (!lockfile.lockedModules().containsKey(moduleId)) {
                warnings.add("Enabled module is not pinned in lockfile: " + moduleId);
            }
        }

        for (String feature : profile.enabledFeatures()) {
            if (!featureGraph.providersByFeature().containsKey(feature)) {
                blockers.add("Required pack feature has no provider: " + feature);
            }
        }

        if (!"plan_only".equals(profile.saveCompatibility().getOrDefault("migrationPolicy", "plan_only"))) {
            blockers.add("Save compatibility migrationPolicy must remain plan_only in Phase 14.4.");
        }

        return new EchoRuntimePackCompatibilityReport(blockers.isEmpty(), blockers, warnings);
    }
}
