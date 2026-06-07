package dev.echo.standalone.runtime.save;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public final class EchoSaveMigrationPlanner {
    public EchoSaveMigrationPlan plan(
            EchoSaveManifest manifest,
            int targetFormatVersion,
            EchoSaveRecoveryJournal journal
    ) throws IOException {
        Objects.requireNonNull(manifest, "manifest");
        Objects.requireNonNull(journal, "journal");
        if (targetFormatVersion < 1) {
            throw new IllegalArgumentException("targetFormatVersion must be positive");
        }
        ArrayList<EchoSaveMigrationStep> steps = new ArrayList<>();
        if (manifest.formatVersion() < targetFormatVersion) {
            for (int version = manifest.formatVersion() + 1; version <= targetFormatVersion; version++) {
                steps.add(new EchoSaveMigrationStep(
                        version,
                        "Apply save format " + version + " compatibility transform",
                        true
                ));
            }
        }
        EchoSaveMigrationPlan plan = new EchoSaveMigrationPlan(
                manifest.profileId(),
                manifest.slotId(),
                manifest.formatVersion(),
                targetFormatVersion,
                manifest.formatVersion() > targetFormatVersion,
                steps
        );
        journal.append(
                EchoSaveJournalEvent.MIGRATION_PLANNED,
                manifest.slotId(),
                "from=" + manifest.formatVersion() + " to=" + targetFormatVersion + " steps=" + steps.size()
        );
        return plan;
    }
}
