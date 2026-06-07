package dev.echo.standalone.runtime.packos;

import java.util.List;
import java.util.Objects;

public record EchoRuntimePackRepairPlan(
        boolean clean,
        boolean executionAllowed,
        List<String> plannedActions
) {
    public EchoRuntimePackRepairPlan {
        Objects.requireNonNull(plannedActions, "plannedActions");
        plannedActions = List.copyOf(plannedActions);
    }
}
