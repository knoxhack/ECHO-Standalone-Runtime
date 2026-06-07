package dev.echo.standalone.runtime.packos;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.modules.EchoRuntimeFeatureGraph;
import dev.echo.standalone.runtime.modules.EchoRuntimeModuleGraph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public final class EchoRuntimePackOs {
    private final EchoRuntimePackProfileLoader profileLoader;
    private final EchoRuntimePackLockfileReader lockfileReader;
    private final EchoRuntimePackIntegrityChecker integrityChecker;
    private final EchoRuntimePackCompatibilityChecker compatibilityChecker;
    private final EchoRuntimePackRepairAdvisor repairAdvisor;

    public EchoRuntimePackOs(
            EchoRuntimePackProfileLoader profileLoader,
            EchoRuntimePackLockfileReader lockfileReader,
            EchoRuntimePackIntegrityChecker integrityChecker,
            EchoRuntimePackCompatibilityChecker compatibilityChecker,
            EchoRuntimePackRepairAdvisor repairAdvisor
    ) {
        this.profileLoader = Objects.requireNonNull(profileLoader, "profileLoader");
        this.lockfileReader = Objects.requireNonNull(lockfileReader, "lockfileReader");
        this.integrityChecker = Objects.requireNonNull(integrityChecker, "integrityChecker");
        this.compatibilityChecker = Objects.requireNonNull(compatibilityChecker, "compatibilityChecker");
        this.repairAdvisor = Objects.requireNonNull(repairAdvisor, "repairAdvisor");
    }

    public static EchoRuntimePackOs createDefault() {
        return new EchoRuntimePackOs(
                new EchoRuntimePackProfileLoader(),
                new EchoRuntimePackLockfileReader(),
                new EchoRuntimePackIntegrityChecker(),
                new EchoRuntimePackCompatibilityChecker(),
                new EchoRuntimePackRepairAdvisor()
        );
    }

    public EchoRuntimePackSession loadSession(
            Path profilePath,
            EchoRuntimeModuleGraph moduleGraph,
            EchoRuntimeFeatureGraph featureGraph,
            EchoRuntimeServiceRegistry services
    ) throws IOException {
        EchoRuntimePackProfile profile = profileLoader.load(profilePath);
        Path lockfilePath = profile.lockfilePath().isAbsolute()
                ? profile.lockfilePath()
                : profile.sourcePath().getParent().resolve(profile.lockfilePath()).normalize();
        EchoRuntimePackLockfile lockfile = lockfileReader.read(lockfilePath);
        EchoRuntimePackIntegrityReport integrityReport = integrityChecker.check(profile, lockfile);
        EchoRuntimePackCompatibilityReport compatibilityReport = compatibilityChecker.check(
                profile,
                lockfile,
                moduleGraph,
                featureGraph
        );
        EchoRuntimePackMountPlan mountPlan = EchoRuntimePackMountPlan.from(profile);
        EchoRuntimePackRepairPlan repairPlan = repairAdvisor.advise(integrityReport, compatibilityReport);
        EchoRuntimePackSession session = new EchoRuntimePackSession(
                profile,
                lockfile,
                mountPlan,
                integrityReport,
                compatibilityReport,
                repairPlan
        );
        services.register(EchoRuntimePackSession.class, session);
        services.register(EchoRuntimePackProfile.class, profile);
        services.register(EchoRuntimePackMountPlan.class, mountPlan);
        services.register(EchoRuntimePackIntegrityReport.class, integrityReport);
        services.register(EchoRuntimePackCompatibilityReport.class, compatibilityReport);
        services.register(EchoRuntimePackRepairPlan.class, repairPlan);
        return session;
    }
}
