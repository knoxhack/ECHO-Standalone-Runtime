package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.util.Objects;

public record EchoSaveRuntimeResult(
        EchoSaveProfile profile,
        EchoSaveRecoveryJournal journal,
        EchoSaveBackupService backupService,
        EchoSaveCorruptionChecker corruptionChecker,
        EchoSaveModSetCompatibilityChecker modSetCompatibilityChecker,
        EchoSaveMigrationPlanner migrationPlanner,
        EchoSaveManifestCodec manifestCodec,
        EchoSaveChecksum checksum
) {
    public EchoSaveRuntimeResult {
        Objects.requireNonNull(profile, "profile");
        Objects.requireNonNull(journal, "journal");
        Objects.requireNonNull(backupService, "backupService");
        Objects.requireNonNull(corruptionChecker, "corruptionChecker");
        Objects.requireNonNull(modSetCompatibilityChecker, "modSetCompatibilityChecker");
        Objects.requireNonNull(migrationPlanner, "migrationPlanner");
        Objects.requireNonNull(manifestCodec, "manifestCodec");
        Objects.requireNonNull(checksum, "checksum");
    }

    public EchoSaveTransaction beginTransaction(String slotId, String transactionId) throws IOException {
        EchoSaveSlot slot = profile.slot(slotId);
        java.nio.file.Files.createDirectories(slot.root());
        journal.append(EchoSaveJournalEvent.BEGIN, transactionId, "slot=" + slotId);
        return new EchoSaveTransaction(slot, transactionId, journal, backupService, manifestCodec, checksum);
    }

    public EchoSaveManifest readManifest(String slotId) throws IOException {
        EchoSaveSlot slot = profile.slot(slotId);
        return manifestCodec.read(slot.manifestPath());
    }

    public EchoSaveCorruptionReport check(String slotId) throws IOException {
        return corruptionChecker.check(profile.slot(slotId), journal);
    }

    public EchoSaveModSetCompatibilityReport checkModSet(String slotId, java.util.List<String> currentModuleIds)
            throws IOException {
        return modSetCompatibilityChecker.check(readManifest(slotId), currentModuleIds, journal);
    }

    public EchoSaveMigrationPlan planMigration(String slotId, int targetFormatVersion) throws IOException {
        return migrationPlanner.plan(readManifest(slotId), targetFormatVersion, journal);
    }
}
