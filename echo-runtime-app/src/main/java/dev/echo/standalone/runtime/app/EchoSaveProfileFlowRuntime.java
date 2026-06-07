package dev.echo.standalone.runtime.app;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.save.EchoSaveBackup;
import dev.echo.standalone.runtime.save.EchoSaveCommitResult;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionIssue;
import dev.echo.standalone.runtime.save.EchoSaveCorruptionReport;
import dev.echo.standalone.runtime.save.EchoSaveManifest;
import dev.echo.standalone.runtime.save.EchoSaveMigrationPlan;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityChecker;
import dev.echo.standalone.runtime.save.EchoSaveModSetCompatibilityReport;
import dev.echo.standalone.runtime.save.EchoSaveProfile;
import dev.echo.standalone.runtime.save.EchoSaveRuntime;
import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;
import dev.echo.standalone.runtime.ui.EchoStaticScreen;
import dev.echo.standalone.runtime.ui.EchoUiRuntime;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiTheme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class EchoSaveProfileFlowRuntime {
    private static final String PRIMARY_SLOT = "ashfall-camp-01";
    private static final String CORRUPT_SLOT = "ashfall-corrupt-01";
    private static final String INCOMPATIBLE_MOD_SLOT = "ashfall-missing-module-01";
    private static final List<String> SAVED_MODULE_SET = List.of(
            "echoadaptercore",
            "echoashfallprotocol",
            "echoworldcore"
    );
    private static final List<String> CURRENT_INCOMPATIBLE_MODULE_SET = List.of(
            "echoadaptercore",
            "echoashfallprotocol",
            "echoscreencore"
    );

    public EchoSaveProfileFlowResult run(EchoRuntimeServiceRegistry services, Path saveRoot) throws IOException {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(saveRoot, "saveRoot");

        EchoSaveProfile profile = new EchoSaveProfile(
                "echo.standalone.save_profile.v1",
                "ashfall-profile-flow",
                "Ashfall Profile Flow",
                "echoashfallprotocol",
                1,
                saveRoot.resolve("profiles/ashfall-profile-flow"),
                Map.of(
                        "chapter", "ashfall",
                        "flow", "save-profile",
                        "runtimePhase", "15.8"
                )
        );
        EchoSaveRuntimeResult save = new EchoSaveRuntime().open(services, profile);

        EchoSaveCommitResult newGameCommit = commitNewGame(save);
        EchoSaveCommitResult autosaveCommit = commitAutosave(save);
        EchoSaveCommitResult manualSaveCommit = commitManualSave(save);

        EchoSaveCommitResult corruptSeedCommit = commitCorruptSeed(save);
        EchoSaveCommitResult corruptUpdateCommit = commitCorruptUpdate(save);
        String restoreBackupId = corruptUpdateCommit.backup()
                .orElseThrow(() -> new IllegalStateException("corrupt update must create a restore backup"))
                .backupId();
        Files.writeString(
                profile.slot(CORRUPT_SLOT).dataRoot().resolve("player/state.json"),
                "{\"corrupted\":true,\"phase\":\"15.8\"}"
        );
        EchoSaveCorruptionReport corruptionWarning = save.check(CORRUPT_SLOT);
        EchoSaveBackup restoredBackup = save.backupService().restoreBackup(
                profile.slot(CORRUPT_SLOT),
                restoreBackupId,
                "tx-restore-backup-001",
                save.journal()
        );
        EchoSaveCorruptionReport restoredReport = save.check(CORRUPT_SLOT);

        EchoSaveCommitResult incompatibleSeedCommit = commitIncompatibleModSeed(save);
        EchoSaveCommitResult incompatibleUpdateCommit = commitIncompatibleModUpdate(save);
        EchoSaveModSetCompatibilityReport incompatibleModSet =
                save.checkModSet(INCOMPATIBLE_MOD_SLOT, CURRENT_INCOMPATIBLE_MODULE_SET);

        EchoSaveMigrationPlan migrationPlan = save.planMigration(PRIMARY_SLOT, 2);
        EchoSaveProfileMigrationPrompt migrationPrompt = new EchoSaveProfileMigrationPrompt(
                PRIMARY_SLOT,
                migrationPlan.fromVersion(),
                migrationPlan.toVersion(),
                migrationPlan.steps().size(),
                !migrationPlan.steps().isEmpty(),
                migrationPlan.steps().stream().anyMatch(step -> step.requiresBackup()),
                true,
                false,
                "Save format 1 -> 2 requires backup and manual approval before migration."
        );

        EchoSaveCorruptionReport primaryReport = save.check(PRIMARY_SLOT);
        EchoSaveProfileContinueFlow continueFlow = new EchoSaveProfileContinueFlow(
                PRIMARY_SLOT,
                manualSaveCommit.manifest().metadata().getOrDefault("saveKind", "manual"),
                true,
                primaryReport.healthy(),
                autosaveCommit.manifest().metadata().containsValue("autosave"),
                manualSaveCommit.manifest().metadata().containsValue("manual")
        );
        EchoSaveProfileRestoreResult restoreResult = new EchoSaveProfileRestoreResult(
                CORRUPT_SLOT,
                restoredBackup,
                corruptionWarning,
                restoredReport,
                !corruptionWarning.healthy(),
                restoredReport.healthy()
        );

        List<EchoSaveProfileSlotSummary> slots = List.of(
                slotSummary(save, PRIMARY_SLOT, "Ashfall Camp 01", true, true, ""),
                slotSummary(
                        save,
                        CORRUPT_SLOT,
                        "Recovered Cache Slot",
                        false,
                        false,
                        firstWarningCode(corruptionWarning)
                ),
                slotSummary(
                        save,
                        INCOMPATIBLE_MOD_SLOT,
                        "Missing Module Recovery Slot",
                        false,
                        false,
                        "MOD_SET_MISMATCH",
                        "blocked"
                )
        );
        int backupCount = manualSaveCommit.manifest().backupIds().size()
                + corruptUpdateCommit.manifest().backupIds().size()
                + incompatibleUpdateCommit.manifest().backupIds().size();
        EchoSaveProfileFlowSummary summary = new EchoSaveProfileFlowSummary(
                "PASS",
                slots.size(),
                (int) slots.stream().filter(slot -> slot.status().equals("healthy")).count(),
                (int) slots.stream().filter(EchoSaveProfileSlotSummary::warningShown).count(),
                (int) slots.stream().filter(slot -> slot.status().equals("blocked")).count(),
                backupCount,
                migrationPlan.steps().size(),
                continueFlow.newGameAvailable(),
                continueFlow.continueAvailable(),
                continueFlow.autosaveAvailable(),
                continueFlow.manualSaveAvailable(),
                restoreResult.restored(),
                migrationPrompt.visible(),
                incompatibleModSet.loadBlocked()
        );

        EchoUiRuntimeResult ui = new EchoUiRuntime().boot(
                services,
                saveProfileScreen(continueFlow, slots, restoreResult, incompatibleModSet, migrationPrompt),
                EchoUiTheme.defaultTerminal()
        );
        EchoSaveProfileFlowResult result = new EchoSaveProfileFlowResult(
                profile,
                save,
                ui,
                slots,
                continueFlow,
                newGameCommit,
                autosaveCommit,
                manualSaveCommit,
                corruptionWarning,
                restoreResult,
                incompatibleModSet,
                migrationPrompt,
                summary
        );
        services.register(EchoSaveProfileFlowResult.class, result);
        return result;
    }

    private static EchoSaveCommitResult commitNewGame(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(PRIMARY_SLOT, "tx-new-game-001");
        transaction.writeText("player/state.json", "{\"health\":100,\"hydration\":100,\"ashExposure\":0}");
        transaction.writeText("world/summary.json", "{\"region\":\"crash_site\",\"safehouse\":\"cold\"}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"new_game\"}");
        return transaction.commit(Map.of(
                "displayName", "Ashfall Camp 01",
                "flowAction", "new_game",
                "saveKind", "new_game",
                "mission", "wake",
                "playtimeSeconds", "0"
        ));
    }

    private static EchoSaveCommitResult commitAutosave(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(PRIMARY_SLOT, "tx-autosave-001");
        transaction.writeText("player/state.json", "{\"health\":96,\"hydration\":82,\"ashExposure\":4}");
        transaction.writeText("world/summary.json", "{\"region\":\"crash_site\",\"safehouse\":\"lit\"}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"autosave\"}");
        return transaction.commit(Map.of(
                "displayName", "Ashfall Camp 01",
                "flowAction", "autosave",
                "saveKind", "autosave",
                "mission", "terminal-online",
                "playtimeSeconds", "75"
        ));
    }

    private static EchoSaveCommitResult commitManualSave(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(PRIMARY_SLOT, "tx-manual-001");
        transaction.writeText("player/state.json", "{\"health\":91,\"hydration\":76,\"ashExposure\":7}");
        transaction.writeText("world/summary.json", "{\"region\":\"relay_yard\",\"safehouse\":\"online\"}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"manual\"}");
        return transaction.commit(Map.of(
                "displayName", "Ashfall Camp 01",
                "flowAction", "manual_save",
                "saveKind", "manual",
                "mission", "relay-yard",
                "playtimeSeconds", "120"
        ));
    }

    private static EchoSaveCommitResult commitIncompatibleModSeed(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(INCOMPATIBLE_MOD_SLOT, "tx-modset-seed-001");
        transaction.writeText("player/state.json", "{\"health\":78,\"hydration\":66,\"ashExposure\":12}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"manual\"}");
        return transaction.commit(moduleSetMetadata(Map.of(
                "displayName", "Missing Module Recovery Slot",
                "flowAction", "manual_save",
                "saveKind", "manual",
                "mission", "worldcore-outpost",
                "playtimeSeconds", "240"
        )));
    }

    private static EchoSaveCommitResult commitIncompatibleModUpdate(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(INCOMPATIBLE_MOD_SLOT, "tx-modset-autosave-001");
        transaction.writeText("player/state.json", "{\"health\":73,\"hydration\":59,\"ashExposure\":16}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"autosave\"}");
        return transaction.commit(moduleSetMetadata(Map.of(
                "displayName", "Missing Module Recovery Slot",
                "flowAction", "autosave",
                "saveKind", "autosave",
                "mission", "worldcore-outpost",
                "playtimeSeconds", "270"
        )));
    }

    private static EchoSaveCommitResult commitCorruptSeed(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(CORRUPT_SLOT, "tx-corrupt-seed-001");
        transaction.writeText("player/state.json", "{\"health\":88,\"hydration\":70,\"ashExposure\":9}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"manual\"}");
        return transaction.commit(Map.of(
                "displayName", "Recovered Cache Slot",
                "flowAction", "manual_save",
                "saveKind", "manual",
                "mission", "cache-recovery",
                "playtimeSeconds", "180"
        ));
    }

    private static EchoSaveCommitResult commitCorruptUpdate(EchoSaveRuntimeResult save) throws IOException {
        EchoSaveTransaction transaction = save.beginTransaction(CORRUPT_SLOT, "tx-corrupt-backup-001");
        transaction.writeText("player/state.json", "{\"health\":64,\"hydration\":45,\"ashExposure\":18}");
        transaction.writeText("session/continue.json", "{\"available\":true,\"source\":\"autosave\"}");
        return transaction.commit(Map.of(
                "displayName", "Recovered Cache Slot",
                "flowAction", "autosave",
                "saveKind", "autosave",
                "mission", "cache-recovery",
                "playtimeSeconds", "210"
        ));
    }

    private static EchoSaveProfileSlotSummary slotSummary(
            EchoSaveRuntimeResult save,
            String slotId,
            String displayName,
            boolean canContinue,
            boolean selectedForContinue,
            String warningCode
    ) throws IOException {
        return slotSummary(save, slotId, displayName, canContinue, selectedForContinue, warningCode, "");
    }

    private static EchoSaveProfileSlotSummary slotSummary(
            EchoSaveRuntimeResult save,
            String slotId,
            String displayName,
            boolean canContinue,
            boolean selectedForContinue,
            String warningCode,
            String statusOverride
    ) throws IOException {
        EchoSaveManifest manifest = save.readManifest(slotId);
        EchoSaveCorruptionReport report = save.check(slotId);
        String status = statusOverride == null || statusOverride.isBlank()
                ? report.healthy() ? "healthy" : "warning"
                : statusOverride;
        return new EchoSaveProfileSlotSummary(
                slotId,
                displayName,
                manifest.metadata().getOrDefault("saveKind", "unknown"),
                status,
                canContinue && report.healthy(),
                selectedForContinue && report.healthy(),
                !manifest.backupIds().isEmpty(),
                manifest.files().size(),
                manifest.backupIds().size(),
                warningCode
        );
    }

    private static EchoStaticScreen saveProfileScreen(
            EchoSaveProfileContinueFlow continueFlow,
            List<EchoSaveProfileSlotSummary> slots,
            EchoSaveProfileRestoreResult restoreResult,
            EchoSaveModSetCompatibilityReport incompatibleModSet,
            EchoSaveProfileMigrationPrompt migrationPrompt
    ) {
        EchoSaveProfileSlotSummary primary = slots.getFirst();
        EchoSaveProfileSlotSummary recovered = slots.get(1);
        EchoSaveProfileSlotSummary blocked = slots.get(2);
        return new EchoStaticScreen(
                "echo.save-profile-flow",
                "Ashfall Save Profiles",
                List.of(
                        "New Game: " + primary.displayName() + " ready",
                        "Continue: " + continueFlow.selectedSlotId() + " -> " + continueFlow.selectedSaveKind(),
                        "Autosave: available for " + primary.slotId(),
                        "Manual Save: available for " + primary.slotId(),
                        "Corruption Warning: " + recovered.warningCode() + " on " + recovered.slotId(),
                        "Restore Backup: " + restoreResult.restoredBackup().backupId() + " restored",
                        "Incompatible Mods: "
                                + String.join(",", incompatibleModSet.missingModuleIds())
                                + " on "
                                + blocked.slotId(),
                        "Recovery: " + incompatibleModSet.recoveryAction(),
                        "Migration Prompt: format "
                                + migrationPrompt.fromVersion()
                                + " -> "
                                + migrationPrompt.toVersion()
                                + " backup required"
                ),
                "menu/continue"
        );
    }

    private static String firstWarningCode(EchoSaveCorruptionReport report) {
        return report.issues().stream()
                .map(EchoSaveCorruptionIssue::code)
                .findFirst()
                .orElse("");
    }

    private static Map<String, String> moduleSetMetadata(Map<String, String> baseMetadata) {
        java.util.TreeMap<String, String> metadata = new java.util.TreeMap<>(baseMetadata);
        metadata.put("saveEnvironmentFingerprintAlgorithm", EchoSaveModSetCompatibilityChecker.ALGORITHM);
        metadata.put(
                "saveEnvironmentFingerprint",
                Integer.toHexString(String.join(",", SAVED_MODULE_SET).hashCode())
        );
        metadata.put(EchoSaveModSetCompatibilityChecker.MODULE_IDS_METADATA_KEY, String.join(",", SAVED_MODULE_SET));
        metadata.put("saveEnvironmentModuleCount", Integer.toString(SAVED_MODULE_SET.size()));
        metadata.put("saveEnvironmentResourcePackIds", "echo-runtime-client,echoashfallprotocol");
        metadata.put("saveEnvironmentResourcePackCount", "2");
        return Map.copyOf(metadata);
    }
}
