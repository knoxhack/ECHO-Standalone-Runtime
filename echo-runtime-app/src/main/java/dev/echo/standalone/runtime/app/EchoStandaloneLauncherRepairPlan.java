package dev.echo.standalone.runtime.app;

import java.util.List;
import java.util.Objects;

public record EchoStandaloneLauncherRepairPlan(
        String planId,
        boolean planningOnly,
        List<String> actions
) {
    public EchoStandaloneLauncherRepairPlan {
        planId = requireText(planId, "planId");
        Objects.requireNonNull(actions, "actions");
        actions = actions.stream()
                .map(action -> requireText(action, "action"))
                .toList();
    }

    public int actionCount() {
        return actions.size();
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value.trim();
    }
}
