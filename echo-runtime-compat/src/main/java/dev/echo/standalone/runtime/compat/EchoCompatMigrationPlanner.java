package dev.echo.standalone.runtime.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class EchoCompatMigrationPlanner {
    public EchoCompatMigrationPlan plan(
            String sourceProfileId,
            String targetProfileId,
            EchoCompatMappingRegistry registry,
            List<EchoCompatSourceRecord> sourceRecords,
            EchoCompatValidationResult validation,
            EchoCompatMigrationPolicy policy,
            EchoCompatDiagnostics diagnostics
    ) {
        sourceProfileId = EchoCompatText.requireText(sourceProfileId, "sourceProfileId");
        targetProfileId = EchoCompatText.requireText(targetProfileId, "targetProfileId");
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(sourceRecords, "sourceRecords");
        Objects.requireNonNull(validation, "validation");
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(diagnostics, "diagnostics");

        ArrayList<EchoCompatMigrationStep> steps = new ArrayList<>();
        if (policy.backupRequired()) {
            steps.add(new EchoCompatMigrationStep(
                    "compat-step-001-backup",
                    EchoCompatMigrationActionKind.REQUIRE_BACKUP,
                    sourceProfileId,
                    targetProfileId,
                    "Create a verified backup before manual compatibility migration",
                    true,
                    false
            ));
        }
        int nextStep = steps.size() + 1;
        boolean blocked = !validation.valid();
        for (EchoCompatSourceRecord sourceRecord : sourceRecords) {
            EchoCompatContentMapping mapping = registry.requireSource(sourceRecord.sourceId());
            EchoCompatMigrationActionKind actionKind = switch (mapping.status()) {
                case SUPPORTED -> EchoCompatMigrationActionKind.MAP_IDENTIFIER;
                case MANUAL_REVIEW -> EchoCompatMigrationActionKind.MANUAL_REVIEW;
                case BLOCKED -> EchoCompatMigrationActionKind.SKIP_BLOCKED;
            };
            if (mapping.status() == EchoCompatMappingStatus.BLOCKED) {
                blocked = true;
            }
            if (mapping.status() == EchoCompatMappingStatus.MANUAL_REVIEW) {
                diagnostics.warning(mapping.mappingId(), "manual review required for " + mapping.sourceId());
            }
            steps.add(new EchoCompatMigrationStep(
                    stepId(nextStep++, actionKind),
                    actionKind,
                    mapping.sourceId(),
                    mapping.targetId(),
                    descriptionFor(mapping),
                    policy.backupRequired(),
                    false
            ));
        }
        EchoCompatMigrationPlan plan = new EchoCompatMigrationPlan(
                "ashfall-compat-manual-plan",
                sourceProfileId,
                targetProfileId,
                policy,
                blocked,
                steps,
                validation
        );
        diagnostics.info("migration-plan", "planned manual compatibility migration with "
                + steps.size() + " steps");
        return plan;
    }

    private static String descriptionFor(EchoCompatContentMapping mapping) {
        return switch (mapping.status()) {
            case SUPPORTED -> "Map " + mapping.sourceId() + " to standalone definition " + mapping.targetId();
            case MANUAL_REVIEW -> "Manually review " + mapping.sourceId() + " before mapping to " + mapping.targetId();
            case BLOCKED -> "Skip blocked source " + mapping.sourceId();
        };
    }

    private static String stepId(int step, EchoCompatMigrationActionKind kind) {
        return String.format(Locale.ROOT, "compat-step-%03d-%s", step, kind.name().toLowerCase(Locale.ROOT));
    }
}
