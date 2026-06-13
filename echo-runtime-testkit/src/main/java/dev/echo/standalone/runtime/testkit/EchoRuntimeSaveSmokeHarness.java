package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveFileState;
import dev.echo.standalone.runtime.save.EchoSaveJournalEntry;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveMigrationPlan;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRecoveryJournal;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class EchoRuntimeSaveSmokeHarness {
    private EchoRuntimeSaveSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-save-smoke");
        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-dev",
                "Ashfall Dev",
                "echoashfallprotocol",
                1,
                fixtureRoot.resolve("profiles/ashfall-dev"),
                Map.of("chapter", "ashfall")
        );

        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoSaveRuntimeResult save = new EchoSaveRuntime().open(services, profile);
        require(services.require(EchoSaveRuntimeResult.class) == save, "save runtime result should be service-bound");
        require(services.require(EchoSaveRecoveryJournal.class).journalPath().endsWith("recovery-journal.log"),
                "recovery journal should be service-bound");

        EchoSaveTransaction first = save.beginTransaction("slot-a", "tx-001");
        first.writeText("player/state.json", "{\"health\":100,\"hazard\":0}");
        first.writeText("world/summary.json", "{\"region\":\"crash_site\"}");
        EchoSaveCommitResult firstCommit = first.commit(Map.of("mission", "wake"));
        require(firstCommit.filesWritten() == 2, "first transaction should write two files");
        require(firstCommit.backup().isEmpty(), "first transaction should not create a backup");
        require(firstCommit.manifest().files().size() == 2, "manifest should track two files");

        EchoSaveCorruptionReport healthy = save.check("slot-a");
        require(healthy.healthy(), "fresh save should be healthy");
        require(healthy.checkedFiles() == 2, "corruption checker should inspect two files");

        EchoSaveMigrationPlan migration = save.planMigration("slot-a", 2);
        require(!migration.blocked(), "forward migration plan should not be blocked");
        require(migration.steps().size() == 1, "format 1 to 2 should produce one migration step");
        require(migration.steps().getFirst().requiresBackup(), "migration step should require a backup");

        EchoSaveTransaction second = save.beginTransaction("slot-a", "tx-002");
        second.writeText("player/state.json", "{\"health\":84,\"hazard\":2}");
        EchoSaveCommitResult secondCommit = second.commit(Map.of("mission", "terminal-online"));
        EchoSaveBackup backup = secondCommit.backup().orElseThrow();
        require(backup.dataFileCount() == 2, "backup should capture previous data files");
        require(secondCommit.manifest().files().size() == 2, "partial transaction should publish merged save data");
        require(secondCommit.manifest().backupIds().equals(java.util.List.of("slot-a-tx-002")),
                "manifest should record backup id");

        EchoSaveManifest manifest = save.readManifest("slot-a");
        require(manifest.file("player/state.json").isPresent(), "manifest should include player state");
        require(manifest.file("world/summary.json").isPresent(),
                "manifest should preserve unchanged data from the previous transaction");
        require(Files.isRegularFile(profile.slot("slot-a").dataRoot().resolve("world/summary.json")),
                "partial transaction should keep the previous world summary file active");
        require(!Files.exists(profile.slot("slot-a").transactionsRoot().resolve("tx-002")),
                "committed transaction staging should be removed");
        require(manifest.metadata().get("mission").equals("terminal-online"),
                "latest metadata should be recorded");
        EchoSaveCorruptionReport secondHealthy = save.check("slot-a");
        require(secondHealthy.healthy(), "merged committed save should remain healthy");
        require(secondHealthy.checkedFiles() == 2, "merged committed save should check two files");

        Files.writeString(profile.slot("slot-a").dataRoot().resolve("player/state.json"), "corrupt");
        EchoSaveCorruptionReport corrupted = save.check("slot-a");
        require(!corrupted.healthy(), "corrupted save should fail verification");
        require(corrupted.issues().stream().anyMatch(issue -> issue.code().equals("CHECKSUM_MISMATCH")),
                "corruption checker should report checksum mismatch");

        List<EchoSaveJournalEntry> journalEntries = save.journal().readAll();
        require(journalEntries.size() >= 10, "journal should record transaction, migration, and corruption events");

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                save,
                firstCommit,
                secondCommit,
                backup,
                manifest,
                healthy,
                secondHealthy,
                corrupted,
                migration,
                journalEntries
        );

        System.out.println("phase14.7 save runtime smoke PASS files="
                + manifest.files().size()
                + " backups="
                + manifest.backupIds().size()
                + " migrationSteps="
                + migration.steps().size()
                + " corruptionIssues="
                + corrupted.issues().size()
                + " journalEntries="
                + journalEntries.size());
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoSaveRuntimeResult save,
            EchoSaveCommitResult firstCommit,
            EchoSaveCommitResult secondCommit,
            EchoSaveBackup backup,
            EchoSaveManifest manifest,
            EchoSaveCorruptionReport healthy,
            EchoSaveCorruptionReport secondHealthy,
            EchoSaveCorruptionReport corrupted,
            EchoSaveMigrationPlan migration,
            List<EchoSaveJournalEntry> journalEntries
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoSaveProfile profile = save.profile();

        write(root.resolve("runtime-save.json"), """
                {
                  "schema": "echo.standalone.runtime_save.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "profileId": "%s",
                  "packId": "%s",
                  "formatVersion": %d,
                  "slotId": "%s",
                  "runtimeResultServiceBound": true,
                  "recoveryJournalServiceBound": true,
                  "transactionCount": 2,
                  "filesTracked": %d,
                  "backupCount": %d,
                  "migrationSteps": %d,
                  "corruptionDetected": %s,
                  "journalEntries": %d
                }
                """.formatted(
                escape(profile.profileId()),
                escape(profile.packId()),
                profile.formatVersion(),
                escape(manifest.slotId()),
                manifest.files().size(),
                manifest.backupIds().size(),
                migration.steps().size(),
                !corrupted.healthy(),
                journalEntries.size()
        ));

        write(root.resolve("save-manifest.json"), """
                {
                  "schema": "echo.standalone.save_manifest_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "manifestSchema": "%s",
                  "profileId": "%s",
                  "slotId": "%s",
                  "packId": "%s",
                  "formatVersion": %d,
                  "fileCount": %d,
                  "files": %s,
                  "backupIds": %s,
                  "metadata": %s,
                  "partialCommitPreservedUnchangedFiles": %s
                }
                """.formatted(
                escape(manifest.schema()),
                escape(manifest.profileId()),
                escape(manifest.slotId()),
                escape(manifest.packId()),
                manifest.formatVersion(),
                manifest.files().size(),
                jsonFileStates(manifest.files()),
                jsonArray(manifest.backupIds()),
                jsonStringMap(manifest.metadata()),
                manifest.file("world/summary.json").isPresent()
        ));

        write(root.resolve("save-transaction.json"), """
                {
                  "schema": "echo.standalone.save_transaction_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "firstTransactionFilesWritten": %d,
                  "firstTransactionCreatedBackup": %s,
                  "secondTransactionFilesWritten": %d,
                  "secondTransactionCreatedBackup": %s,
                  "secondTransactionMergedFileCount": %d,
                  "stagingRemovedAfterCommit": %s,
                  "latestMissionMetadata": "%s"
                }
                """.formatted(
                firstCommit.filesWritten(),
                firstCommit.backup().isPresent(),
                secondCommit.filesWritten(),
                secondCommit.backup().isPresent(),
                secondCommit.manifest().files().size(),
                !Files.exists(secondCommit.slot().transactionsRoot().resolve("tx-002")),
                escape(manifest.metadata().getOrDefault("mission", ""))
        ));

        write(root.resolve("save-backup.json"), """
                {
                  "schema": "echo.standalone.save_backup_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "backupId": "%s",
                  "slotId": "%s",
                  "dataFileCount": %d,
                  "manifestChecksumSha256": "%s",
                  "backupRootExists": %s,
                  "manifestRecordedBackupId": %s
                }
                """.formatted(
                escape(backup.backupId()),
                escape(backup.slotId()),
                backup.dataFileCount(),
                escape(backup.manifestChecksumSha256()),
                Files.isDirectory(backup.root()),
                manifest.backupIds().contains(backup.backupId())
        ));

        write(root.resolve("save-corruption.json"), """
                {
                  "schema": "echo.standalone.save_corruption_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "freshSaveHealthy": %s,
                  "mergedSaveHealthy": %s,
                  "corruptedSaveHealthy": %s,
                  "checkedFilesBeforeCorruption": %d,
                  "checkedFilesAfterMerge": %d,
                  "checkedFilesAfterCorruption": %d,
                  "issueCount": %d,
                  "issues": %s,
                  "checksumMismatchDetected": %s
                }
                """.formatted(
                healthy.healthy(),
                secondHealthy.healthy(),
                corrupted.healthy(),
                healthy.checkedFiles(),
                secondHealthy.checkedFiles(),
                corrupted.checkedFiles(),
                corrupted.issues().size(),
                jsonIssues(corrupted.issues()),
                corrupted.issues().stream().anyMatch(issue -> issue.code().equals("CHECKSUM_MISMATCH"))
        ));

        write(root.resolve("save-migration.json"), """
                {
                  "schema": "echo.standalone.save_migration_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "profileId": "%s",
                  "slotId": "%s",
                  "fromVersion": %d,
                  "toVersion": %d,
                  "blocked": %s,
                  "stepCount": %d,
                  "steps": %s,
                  "backupRequired": %s
                }
                """.formatted(
                escape(migration.profileId()),
                escape(migration.slotId()),
                migration.fromVersion(),
                migration.toVersion(),
                migration.blocked(),
                migration.steps().size(),
                jsonMigrationSteps(migration),
                migration.steps().stream().anyMatch(step -> step.requiresBackup())
        ));

        write(root.resolve("save-recovery-journal.json"), """
                {
                  "schema": "echo.standalone.save_recovery_journal_evidence.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "journalPath": "%s",
                  "entryCount": %d,
                  "events": %s,
                  "containsBegin": %s,
                  "containsStaged": %s,
                  "containsBackupCreated": %s,
                  "containsCommitted": %s,
                  "containsMigrationPlanned": %s,
                  "containsCorruptionChecked": %s
                }
                """.formatted(
                escape(save.journal().journalPath().toString()),
                journalEntries.size(),
                jsonJournalEntries(journalEntries),
                hasJournalEvent(journalEntries, "BEGIN"),
                hasJournalEvent(journalEntries, "STAGED"),
                hasJournalEvent(journalEntries, "BACKUP_CREATED"),
                hasJournalEvent(journalEntries, "COMMITTED"),
                hasJournalEvent(journalEntries, "MIGRATION_PLANNED"),
                hasJournalEvent(journalEntries, "CORRUPTION_CHECKED")
        ));
    }

    private static boolean hasJournalEvent(List<EchoSaveJournalEntry> entries, String eventName) {
        return entries.stream().anyMatch(entry -> entry.event().name().equals(eventName));
    }

    private static String jsonFileStates(List<EchoSaveFileState> files) {
        return files.stream()
                .map(file -> "{\"relativePath\": \"" + escape(file.relativePath())
                        + "\", \"checksumSha256\": \"" + escape(file.checksumSha256())
                        + "\", \"bytes\": " + file.bytes() + "}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonIssues(List<dev.echo.standalone.runtime.save.EchoSaveCorruptionIssue> issues) {
        return issues.stream()
                .map(issue -> "{\"severity\": \"" + issue.severity().name()
                        + "\", \"code\": \"" + escape(issue.code())
                        + "\", \"path\": \"" + escape(issue.path())
                        + "\", \"message\": \"" + escape(issue.message()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonMigrationSteps(EchoSaveMigrationPlan migration) {
        return migration.steps().stream()
                .map(step -> "{\"targetVersion\": " + step.targetVersion()
                        + ", \"action\": \"" + escape(step.action())
                        + "\", \"requiresBackup\": " + step.requiresBackup() + "}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonJournalEntries(List<EchoSaveJournalEntry> entries) {
        return entries.stream()
                .map(entry -> "{\"sequence\": " + entry.sequence()
                        + ", \"event\": \"" + entry.event().name()
                        + "\", \"transactionId\": \"" + escape(entry.transactionId())
                        + "\", \"message\": \"" + escape(entry.message()) + "\"}")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static String jsonStringMap(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + escape(entry.getValue()) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "{", "}"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(java.util.stream.Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
