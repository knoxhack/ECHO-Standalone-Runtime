package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoSaveProfileFlowResult;
import dev.echo.standalone.runtime.app.EchoSaveProfileFlowRuntime;
import dev.echo.standalone.runtime.app.EchoSaveProfileFlowSummary;
import dev.echo.standalone.runtime.app.EchoSaveProfileMigrationPrompt;
import dev.echo.standalone.runtime.app.EchoSaveProfileRestoreResult;
import dev.echo.standalone.runtime.app.EchoSaveProfileSlotSummary;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionIssue;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityChecker;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityReport;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class EchoRuntimeSaveProfileFlowSmokeHarness {
    private EchoRuntimeSaveProfileFlowSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        Path fixtureRoot = Files.createTempDirectory("echo-runtime-save-profile-flow-smoke");
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoSaveProfileFlowResult result = new EchoSaveProfileFlowRuntime().run(services, fixtureRoot);

        require(services.require(EchoSaveProfileFlowResult.class) == result,
                "save profile flow result should be service-bound");
        require(services.require(EchoSaveRuntimeResult.class) == result.saveRuntime(),
                "save runtime should remain service-bound");
        require(services.require(EchoSaveModSetCompatibilityChecker.class)
                        == result.saveRuntime().modSetCompatibilityChecker(),
                "save mod-set compatibility checker should be service-bound");
        require(services.require(EchoUiRuntimeResult.class) == result.ui(),
                "save profile UI should be service-bound");

        require(result.profile().profileId().equals("ashfall-profile-flow"),
                "profile id should identify the save profile flow");
        require(result.newGameCommit().filesWritten() == 3, "new game should write three save files");
        require(result.autosaveCommit().backup().isPresent(), "autosave should create a backup");
        require(result.manualSaveCommit().backup().isPresent(), "manual save should create a backup");
        require(result.manualSaveCommit().manifest().metadata().get("saveKind").equals("manual"),
                "manual save should be the latest continue target");

        require(!result.corruptionWarning().healthy(), "corrupted slot should show a warning");
        require(result.corruptionWarning().issues().stream().anyMatch(issue -> issue.code().equals("CHECKSUM_MISMATCH")),
                "corruption warning should expose checksum mismatch");
        require(result.restoreResult().warningShown(), "restore flow should show the corruption warning first");
        require(result.restoreResult().restored(), "restore flow should recover from backup");
        require(result.restoreResult().afterRestore().healthy(), "restored slot should pass corruption checking");

        require(result.migrationPrompt().visible(), "migration prompt should be visible for format 1 -> 2");
        require(result.migrationPrompt().backupRequired(), "migration prompt should require backup");
        require(result.migrationPrompt().manualApprovalRequired(), "migration prompt should require manual approval");
        require(!result.migrationPrompt().executesAutomatically(), "migration prompt should not execute automatically");
        require(result.incompatibleModSet().loadBlocked(),
                "changed module sets should block unsafe named-slot loading");
        require(!result.incompatibleModSet().compatible(),
                "missing module recovery slot should be incompatible with the current module set");
        require(result.incompatibleModSet().missingModuleIds().contains("echoworldcore"),
                "incompatible mod-set report should name the missing saved module");
        require(result.incompatibleModSet().addedModuleIds().contains("echoscreencore"),
                "incompatible mod-set report should name added current modules");
        require(result.incompatibleModSet().backupAvailable(),
                "incompatible mod-set recovery should expose a backup option");
        require(result.incompatibleModSet().backupRequired(),
                "incompatible mod-set recovery should require a backup");
        require(result.incompatibleModSet().manualApprovalRequired(),
                "incompatible mod-set recovery should require manual approval");
        require(result.incompatibleModSet().migrationPlanningRequired(),
                "incompatible mod-set recovery should require migration planning");
        require(result.incompatibleModSet().recoveryAction().equals("restore_backup_or_open_migration_prompt"),
                "incompatible mod-set recovery should offer backup restore or migration prompt");

        require(result.continueFlow().newGameAvailable(), "new game action should be available");
        require(result.continueFlow().continueAvailable(), "continue action should be available");
        require(result.continueFlow().autosaveAvailable(), "autosave action should be available");
        require(result.continueFlow().manualSaveAvailable(), "manual save action should be available");
        require(result.continueFlow().selectedSlotId().equals("ashfall-camp-01"),
                "continue should select the primary healthy slot");

        List<EchoSaveProfileSlotSummary> slots = result.slots();
        require(slots.size() == 3, "three user-facing save slots should be visible");
        require(slots.stream().anyMatch(EchoSaveProfileSlotSummary::selectedForContinue),
                "one slot should be selected for continue");
        require(slots.stream().anyMatch(EchoSaveProfileSlotSummary::warningShown),
                "one slot should carry a user-facing warning code");
        require(slots.stream().anyMatch(slot -> slot.slotId().equals("ashfall-missing-module-01")
                        && slot.status().equals("blocked")
                        && !slot.canContinue()
                        && slot.warningCode().equals("MOD_SET_MISMATCH")),
                "incompatible module slot should be visible but blocked");

        List<String> uiLines = result.ui().frame().screen().lines();
        require(containsLine(uiLines, "New Game"), "UI should show New Game");
        require(containsLine(uiLines, "Continue"), "UI should show Continue");
        require(containsLine(uiLines, "Autosave"), "UI should show Autosave");
        require(containsLine(uiLines, "Manual Save"), "UI should show Manual Save");
        require(containsLine(uiLines, "Corruption Warning"), "UI should show Corruption Warning");
        require(containsLine(uiLines, "Restore Backup"), "UI should show Restore Backup");
        require(containsLine(uiLines, "Incompatible Mods"), "UI should show Incompatible Mods");
        require(containsLine(uiLines, "Recovery"), "UI should show incompatible-mod recovery action");
        require(containsLine(uiLines, "Migration Prompt"), "UI should show Migration Prompt");

        require(result.summary().status().equals("PASS"), "summary should pass");
        require(result.summary().visibleSlots() == 3, "summary should count visible slots");
        require(result.summary().healthySlots() == 2, "summary should count restored healthy slots");
        require(result.summary().warningCount() == 2, "summary should retain corruption and mod-set warnings");
        require(result.summary().blockedSlotCount() == 1, "summary should count blocked incompatible slots");
        require(result.summary().backupCount() == 4, "summary should count committed backups");
        require(result.summary().migrationStepCount() == 1, "summary should count migration steps");
        require(result.summary().incompatibleModSetPromptReady(),
                "summary should expose incompatible-mod recovery prompt readiness");

        writeReports(Path.of(".").toAbsolutePath().normalize(), result);
        System.out.println("phase15.8 save profile flow smoke PASS slots="
                + result.summary().visibleSlots()
                + " backups="
                + result.summary().backupCount()
                + " warnings="
                + result.summary().warningCount()
                + " migrationSteps="
                + result.summary().migrationStepCount()
                + " blocked="
                + result.summary().blockedSlotCount()
                + " continue="
                + result.continueFlow().selectedSlotId());
    }

    private static void writeReports(Path standaloneRoot, EchoSaveProfileFlowResult result) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);
        EchoSaveProfileFlowSummary summary = result.summary();

        write(root.resolve("runtime-save-profile-flow.json"), """
                {
                  "schema": "echo.standalone.runtime_save_profile_flow.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "%s",
                  "profileId": "%s",
                  "packId": "%s",
                  "saveRuntimeServiceBound": true,
                  "saveProfileFlowServiceBound": true,
                  "modSetCompatibilityCheckerServiceBound": true,
                  "uiServiceBound": true,
                  "visibleSlots": %d,
                  "healthySlots": %d,
                  "warningCount": %d,
                  "blockedSlotCount": %d,
                  "backupCount": %d,
                  "migrationStepCount": %d,
                  "newGameReady": %s,
                  "continueReady": %s,
                  "autosaveReady": %s,
                  "manualSaveReady": %s,
                  "restoreReady": %s,
                  "migrationPromptReady": %s,
                  "incompatibleModSetPromptReady": %s,
                  "uiLines": %s
                }
                """.formatted(
                escape(summary.status()),
                escape(result.profile().profileId()),
                escape(result.profile().packId()),
                summary.visibleSlots(),
                summary.healthySlots(),
                summary.warningCount(),
                summary.blockedSlotCount(),
                summary.backupCount(),
                summary.migrationStepCount(),
                summary.newGameReady(),
                summary.continueReady(),
                summary.autosaveReady(),
                summary.manualSaveReady(),
                summary.restoreReady(),
                summary.migrationPromptReady(),
                summary.incompatibleModSetPromptReady(),
                jsonArray(result.ui().frame().screen().lines())
        ));

        write(root.resolve("save-profile-slots.json"), """
                {
                  "schema": "echo.standalone.save_profile_slots.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "profileId": "%s",
                  "slotCount": %d,
                  "healthySlotCount": %d,
                  "warningSlotCount": %d,
                  "blockedSlotCount": %d,
                  "selectedSlotId": "%s",
                  "slots": %s
                }
                """.formatted(
                escape(result.profile().profileId()),
                result.slots().size(),
                result.slots().stream().filter(slot -> slot.status().equals("healthy")).count(),
                result.slots().stream().filter(EchoSaveProfileSlotSummary::warningShown).count(),
                result.slots().stream().filter(slot -> slot.status().equals("blocked")).count(),
                escape(result.continueFlow().selectedSlotId()),
                jsonSlots(result.slots())
        ));

        write(root.resolve("save-continue-flow.json"), """
                {
                  "schema": "echo.standalone.save_continue_flow.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "selectedSlotId": "%s",
                  "selectedSaveKind": "%s",
                  "newGameAvailable": %s,
                  "continueAvailable": %s,
                  "autosaveAvailable": %s,
                  "manualSaveAvailable": %s,
                  "selectedSlotCanContinue": %s
                }
                """.formatted(
                escape(result.continueFlow().selectedSlotId()),
                escape(result.continueFlow().selectedSaveKind()),
                result.continueFlow().newGameAvailable(),
                result.continueFlow().continueAvailable(),
                result.continueFlow().autosaveAvailable(),
                result.continueFlow().manualSaveAvailable(),
                result.slots().stream().anyMatch(slot -> slot.slotId().equals(result.continueFlow().selectedSlotId())
                        && slot.canContinue()
                        && slot.selectedForContinue())
        ));

        writeCommitReport(
                root.resolve("save-new-game-flow.json"),
                "echo.standalone.save_new_game_flow.v2",
                "new_game",
                result.newGameCommit(),
                false,
                result.summary().newGameReady()
        );
        writeCommitReport(
                root.resolve("save-autosave.json"),
                "echo.standalone.save_autosave.v2",
                "autosave",
                result.autosaveCommit(),
                true,
                result.summary().autosaveReady()
        );
        writeCommitReport(
                root.resolve("save-manual-save.json"),
                "echo.standalone.save_manual_save.v2",
                "manual",
                result.manualSaveCommit(),
                true,
                result.summary().manualSaveReady()
        );

        write(root.resolve("save-corruption-warning.json"), """
                {
                  "schema": "echo.standalone.save_corruption_warning.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "profileId": "%s",
                  "slotId": "%s",
                  "warningShown": %s,
                  "healthy": %s,
                  "checkedFiles": %d,
                  "issueCount": %d,
                  "checksumMismatchDetected": %s,
                  "issues": %s
                }
                """.formatted(
                escape(result.corruptionWarning().profileId()),
                escape(result.corruptionWarning().slotId()),
                !result.corruptionWarning().healthy(),
                result.corruptionWarning().healthy(),
                result.corruptionWarning().checkedFiles(),
                result.corruptionWarning().issues().size(),
                result.corruptionWarning().issues().stream().anyMatch(issue -> issue.code().equals("CHECKSUM_MISMATCH")),
                jsonIssues(result.corruptionWarning().issues())
        ));

        EchoSaveProfileRestoreResult restore = result.restoreResult();
        write(root.resolve("save-backup-restore-ui.json"), """
                {
                  "schema": "echo.standalone.save_backup_restore_ui.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "slotId": "%s",
                  "warningShown": %s,
                  "restored": %s,
                  "restoredBackupId": "%s",
                  "restoredBackupDataFileCount": %d,
                  "beforeRestoreHealthy": %s,
                  "afterRestoreHealthy": %s,
                  "beforeRestoreIssueCodes": %s,
                  "afterRestoreCheckedFiles": %d
                }
                """.formatted(
                escape(restore.slotId()),
                restore.warningShown(),
                restore.restored(),
                escape(restore.restoredBackup().backupId()),
                restore.restoredBackup().dataFileCount(),
                restore.beforeRestore().healthy(),
                restore.afterRestore().healthy(),
                jsonArray(restore.beforeRestore().issues().stream()
                        .map(EchoSaveCorruptionIssue::code)
                        .toList()),
                restore.afterRestore().checkedFiles()
        ));

        EchoSaveProfileMigrationPrompt migration = result.migrationPrompt();
        write(root.resolve("save-migration-prompt.json"), """
                {
                  "schema": "echo.standalone.save_migration_prompt.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "slotId": "%s",
                  "fromVersion": %d,
                  "toVersion": %d,
                  "stepCount": %d,
                  "visible": %s,
                  "backupRequired": %s,
                  "manualApprovalRequired": %s,
                  "executesAutomatically": %s,
                  "message": "%s"
                }
                """.formatted(
                escape(migration.slotId()),
                migration.fromVersion(),
                migration.toVersion(),
                migration.stepCount(),
                migration.visible(),
                migration.backupRequired(),
                migration.manualApprovalRequired(),
                migration.executesAutomatically(),
                escape(migration.message())
        ));

        EchoSaveModSetCompatibilityReport incompatible = result.incompatibleModSet();
        write(root.resolve("save-incompatible-mod-recovery.json"), """
                {
                  "schema": "echo.standalone.save_incompatible_mod_recovery.v2",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "profileId": "%s",
                  "slotId": "%s",
                  "algorithm": "%s",
                  "compatible": %s,
                  "loadBlocked": %s,
                  "backupAvailable": %s,
                  "backupRequired": %s,
                  "manualApprovalRequired": %s,
                  "migrationPlanningRequired": %s,
                  "recoveryAction": "%s",
                  "expectedModuleIds": %s,
                  "currentModuleIds": %s,
                  "missingModuleIds": %s,
                  "addedModuleIds": %s,
                  "message": "%s"
                }
                """.formatted(
                escape(incompatible.profileId()),
                escape(incompatible.slotId()),
                escape(incompatible.algorithm()),
                incompatible.compatible(),
                incompatible.loadBlocked(),
                incompatible.backupAvailable(),
                incompatible.backupRequired(),
                incompatible.manualApprovalRequired(),
                incompatible.migrationPlanningRequired(),
                escape(incompatible.recoveryAction()),
                jsonArray(incompatible.expectedModuleIds()),
                jsonArray(incompatible.currentModuleIds()),
                jsonArray(incompatible.missingModuleIds()),
                jsonArray(incompatible.addedModuleIds()),
                escape(incompatible.message())
        ));
    }

    private static void writeCommitReport(
            Path path,
            String schema,
            String expectedSaveKind,
            EchoSaveCommitResult commit,
            boolean backupExpected,
            boolean ready
    ) throws IOException {
        Optional<EchoSaveBackup> backup = commit.backup();
        Map<String, String> metadata = commit.manifest().metadata();
        write(path, """
                {
                  "schema": "%s",
                  "generatedAt": "1970-01-01T00:00:00Z",
                  "status": "PASS",
                  "slotId": "%s",
                  "expectedSaveKind": "%s",
                  "manifestSaveKind": "%s",
                  "ready": %s,
                  "filesWritten": %d,
                  "manifestFileCount": %d,
                  "backupExpected": %s,
                  "backupCreated": %s,
                  "backupId": "%s",
                  "backupDataFileCount": %d,
                  "metadata": %s
                }
                """.formatted(
                escape(schema),
                escape(commit.manifest().slotId()),
                escape(expectedSaveKind),
                escape(metadata.getOrDefault("saveKind", "")),
                ready,
                commit.filesWritten(),
                commit.manifest().files().size(),
                backupExpected,
                backup.isPresent(),
                escape(backup.map(EchoSaveBackup::backupId).orElse("")),
                backup.map(EchoSaveBackup::dataFileCount).orElse(0),
                jsonStringMap(metadata)
        ));
    }

    private static String jsonSlots(List<EchoSaveProfileSlotSummary> slots) {
        return slots.stream()
                .map(slot -> "{\"slotId\": \"" + escape(slot.slotId())
                        + "\", \"displayName\": \"" + escape(slot.displayName())
                        + "\", \"saveKind\": \"" + escape(slot.saveKind())
                        + "\", \"status\": \"" + escape(slot.status())
                        + "\", \"canContinue\": " + slot.canContinue()
                        + ", \"selectedForContinue\": " + slot.selectedForContinue()
                        + ", \"backupAvailable\": " + slot.backupAvailable()
                        + ", \"trackedFiles\": " + slot.trackedFiles()
                        + ", \"backupCount\": " + slot.backupCount()
                        + ", \"warningCode\": \"" + escape(slot.warningCode()) + "\"}")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String jsonIssues(List<EchoSaveCorruptionIssue> issues) {
        return issues.stream()
                .map(issue -> "{\"severity\": \"" + issue.severity().name()
                        + "\", \"code\": \"" + escape(issue.code())
                        + "\", \"path\": \"" + escape(issue.path())
                        + "\", \"message\": \"" + escape(issue.message()) + "\"}")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String jsonStringMap(Map<String, String> values) {
        return values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> "\"" + escape(entry.getKey()) + "\": \"" + escape(entry.getValue()) + "\"")
                .collect(Collectors.joining(", ", "{", "}"));
    }

    private static String jsonArray(List<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static boolean containsLine(List<String> lines, String text) {
        return lines.stream().anyMatch(line -> line.contains(text));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
