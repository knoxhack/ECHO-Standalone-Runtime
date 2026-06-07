package dev.echo.standalone.runtime.app;

public record EchoAshfallFailRetryResult(
        boolean failed,
        String failureReason,
        int failureHealth,
        int hazardApplications,
        String checkpointId,
        boolean retried,
        String retryOutcome
) {
    public EchoAshfallFailRetryResult {
        failureReason = EchoAppText.requireText(failureReason, "failureReason");
        if (failureHealth < 0) {
            throw new IllegalArgumentException("failureHealth must not be negative");
        }
        if (hazardApplications < 0) {
            throw new IllegalArgumentException("hazardApplications must not be negative");
        }
        checkpointId = EchoAppText.requireText(checkpointId, "checkpointId");
        retryOutcome = EchoAppText.requireText(retryOutcome, "retryOutcome");
    }
}
