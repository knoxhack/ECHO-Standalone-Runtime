package dev.echo.standalone.runtime.compat;

public record EchoCompatMigrationPolicy(
        String policyId,
        boolean manualOnly,
        boolean executeAutomatically,
        boolean mutateSourceAllowed,
        boolean backupRequired
) {
    public EchoCompatMigrationPolicy {
        policyId = EchoCompatText.requireText(policyId, "policyId");
        if (!manualOnly && !executeAutomatically) {
            throw new IllegalArgumentException("non-manual policies must execute automatically");
        }
        if (executeAutomatically && !mutateSourceAllowed) {
            throw new IllegalArgumentException("automatic execution requires mutation permission");
        }
    }

    public static EchoCompatMigrationPolicy manualPlanOnly() {
        return new EchoCompatMigrationPolicy(
                "echo:manual_migration_plan_only",
                true,
                false,
                false,
                true
        );
    }
}
