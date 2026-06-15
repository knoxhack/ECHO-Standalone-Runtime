package dev.echo.standalone.runtime.save;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;

import java.io.IOException;
import java.util.Objects;

public final class EchoSaveRuntime {
    private final EchoSaveBackupService backupService;
    private final EchoSaveCorruptionChecker corruptionChecker;
    private final EchoSaveModSetCompatibilityChecker modSetCompatibilityChecker;
    private final EchoSaveRegistryCompatibilityChecker registryCompatibilityChecker;
    private final EchoSaveMigrationPlanner migrationPlanner;
    private final EchoSaveManifestCodec manifestCodec;
    private final EchoSaveChecksum checksum;

    public EchoSaveRuntime() {
        this(
                new EchoSaveBackupService(),
                new EchoSaveCorruptionChecker(),
                new EchoSaveModSetCompatibilityChecker(),
                new EchoSaveRegistryCompatibilityChecker(),
                new EchoSaveMigrationPlanner(),
                new EchoSaveManifestCodec(),
                new EchoSaveChecksum()
        );
    }

    public EchoSaveRuntime(
            EchoSaveBackupService backupService,
            EchoSaveCorruptionChecker corruptionChecker,
            EchoSaveModSetCompatibilityChecker modSetCompatibilityChecker,
            EchoSaveRegistryCompatibilityChecker registryCompatibilityChecker,
            EchoSaveMigrationPlanner migrationPlanner,
            EchoSaveManifestCodec manifestCodec,
            EchoSaveChecksum checksum
    ) {
        this.backupService = Objects.requireNonNull(backupService, "backupService");
        this.corruptionChecker = Objects.requireNonNull(corruptionChecker, "corruptionChecker");
        this.modSetCompatibilityChecker = Objects.requireNonNull(
                modSetCompatibilityChecker,
                "modSetCompatibilityChecker"
        );
        this.registryCompatibilityChecker = Objects.requireNonNull(
                registryCompatibilityChecker,
                "registryCompatibilityChecker"
        );
        this.migrationPlanner = Objects.requireNonNull(migrationPlanner, "migrationPlanner");
        this.manifestCodec = Objects.requireNonNull(manifestCodec, "manifestCodec");
        this.checksum = Objects.requireNonNull(checksum, "checksum");
    }

    public EchoSaveRuntimeResult open(EchoRuntimeServiceRegistry services, EchoSaveProfile profile) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(profile, "profile");
        java.nio.file.Files.createDirectories(profile.root().resolve("slots"));
        java.nio.file.Files.createDirectories(profile.root().resolve("backups"));
        EchoSaveRecoveryJournal journal = new EchoSaveRecoveryJournal(profile.root().resolve("recovery-journal.log"));
        EchoSaveRuntimeResult result = new EchoSaveRuntimeResult(
                profile,
                journal,
                backupService,
                corruptionChecker,
                modSetCompatibilityChecker,
                registryCompatibilityChecker,
                migrationPlanner,
                manifestCodec,
                checksum
        );
        services.register(EchoSaveRuntimeResult.class, result);
        services.register(EchoSaveRecoveryJournal.class, journal);
        services.register(EchoSaveBackupService.class, backupService);
        services.register(EchoSaveCorruptionChecker.class, corruptionChecker);
        services.register(EchoSaveModSetCompatibilityChecker.class, modSetCompatibilityChecker);
        services.register(EchoSaveMigrationPlanner.class, migrationPlanner);
        services.register(EchoSaveManifestCodec.class, manifestCodec);
        return result;
    }
}
