package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.app.EchoSaveProfileFlowResult;
import dev.echo.standalone.runtime.app.EchoSaveProfileFlowRuntime;
import dev.echo.standalone.runtime.app.EchoSaveProfileSlotSummary;
import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityChecker;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

    private static boolean containsLine(List<String> lines, String text) {
        return lines.stream().anyMatch(line -> line.contains(text));
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
