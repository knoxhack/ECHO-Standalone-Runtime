package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveMigrationPlan;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRecoveryJournal;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        int journalEntries = save.journal().readAll().size();
        require(journalEntries >= 10, "journal should record transaction, migration, and corruption events");

        System.out.println("phase14.7 save runtime smoke PASS files="
                + manifest.files().size()
                + " backups="
                + manifest.backupIds().size()
                + " migrationSteps="
                + migration.steps().size()
                + " corruptionIssues="
                + corrupted.issues().size()
                + " journalEntries="
                + journalEntries);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
