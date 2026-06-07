package dev.echo.standalone.runtime.app;

public record EchoAshfallInventoryUxResult(
        boolean adapterCoreBacked,
        boolean dragMovementReady,
        boolean stackSplitReady,
        boolean hotbarAssignmentReady,
        boolean tooltipReady,
        boolean disabledStatesReady,
        boolean consumeUseFeedbackReady,
        boolean keyboardMouseFlowReady,
        int diagnosticsCount,
        String summary
) {
    public EchoAshfallInventoryUxResult {
        summary = EchoAppText.requireText(summary, "summary");
        if (diagnosticsCount < 0) {
            throw new IllegalArgumentException("diagnosticsCount must not be negative");
        }
    }

    public boolean ready() {
        return adapterCoreBacked
                && dragMovementReady
                && stackSplitReady
                && hotbarAssignmentReady
                && tooltipReady
                && disabledStatesReady
                && consumeUseFeedbackReady
                && keyboardMouseFlowReady
                && diagnosticsCount >= 8;
    }
}
