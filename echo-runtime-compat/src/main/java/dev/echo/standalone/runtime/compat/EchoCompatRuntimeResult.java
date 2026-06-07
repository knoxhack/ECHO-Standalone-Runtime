package dev.echo.standalone.runtime.compat;

import java.util.List;
import java.util.Objects;

public record EchoCompatRuntimeResult(
        EchoRuntimeCompatibilityAdapterBoundary boundary,
        EchoCompatMappingRegistry mappingRegistry,
        List<EchoCompatSourceRecord> sourceRecords,
        EchoCompatMigrationPolicy migrationPolicy,
        EchoCompatTargetValidator targetValidator,
        EchoCompatValidationResult targetValidation,
        EchoCompatMigrationPlanner migrationPlanner,
        EchoCompatMigrationPlan migrationPlan,
        EchoCompatDiagnostics diagnostics,
        EchoAdapterCoreStandaloneContentBridge adapterCoreBridge,
        EchoAdapterCoreStandaloneRegistry adapterCoreRegistry
) {
    public EchoCompatRuntimeResult {
        Objects.requireNonNull(boundary, "boundary");
        Objects.requireNonNull(mappingRegistry, "mappingRegistry");
        Objects.requireNonNull(sourceRecords, "sourceRecords");
        sourceRecords = List.copyOf(sourceRecords);
        Objects.requireNonNull(migrationPolicy, "migrationPolicy");
        Objects.requireNonNull(targetValidator, "targetValidator");
        Objects.requireNonNull(targetValidation, "targetValidation");
        Objects.requireNonNull(migrationPlanner, "migrationPlanner");
        Objects.requireNonNull(migrationPlan, "migrationPlan");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(adapterCoreBridge, "adapterCoreBridge");
        Objects.requireNonNull(adapterCoreRegistry, "adapterCoreRegistry");
    }
}
