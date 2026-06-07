package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoRuleAction(
        String actionId,
        EchoRuleActionType type,
        String target,
        String message,
        int amount,
        EchoScriptingDiagnosticSeverity severity
) {
    public EchoRuleAction {
        actionId = EchoScriptingText.requireText(actionId, "actionId");
        Objects.requireNonNull(type, "type");
        target = EchoScriptingText.optionalText(target);
        message = EchoScriptingText.optionalText(message);
        if (amount < 0 && type != EchoRuleActionType.ADJUST_FACTION_REPUTATION) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        Objects.requireNonNull(severity, "severity");
    }

    public static EchoRuleAction diagnostic(
            String actionId,
            EchoScriptingDiagnosticSeverity severity,
            String message
    ) {
        return new EchoRuleAction(actionId, EchoRuleActionType.EMIT_DIAGNOSTIC, "none", message, 0, severity);
    }

    public static EchoRuleAction notification(
            String actionId,
            EchoScriptingDiagnosticSeverity severity,
            String message
    ) {
        return new EchoRuleAction(actionId, EchoRuleActionType.ADD_NOTIFICATION, "none", message, 0, severity);
    }

    public static EchoRuleAction completeObjective(String actionId, String objectiveId) {
        return new EchoRuleAction(
                actionId,
                EchoRuleActionType.COMPLETE_OBJECTIVE,
                objectiveId,
                "none",
                0,
                EchoScriptingDiagnosticSeverity.INFO
        );
    }

    public static EchoRuleAction awardExperience(String actionId, int amount) {
        return new EchoRuleAction(
                actionId,
                EchoRuleActionType.AWARD_EXPERIENCE,
                "none",
                "none",
                amount,
                EchoScriptingDiagnosticSeverity.INFO
        );
    }

    public static EchoRuleAction addMilestone(String actionId, String milestoneId) {
        return new EchoRuleAction(
                actionId,
                EchoRuleActionType.ADD_MILESTONE,
                milestoneId,
                "none",
                0,
                EchoScriptingDiagnosticSeverity.INFO
        );
    }

    public static EchoRuleAction adjustFactionReputation(String actionId, String factionId, int delta) {
        return new EchoRuleAction(
                actionId,
                EchoRuleActionType.ADJUST_FACTION_REPUTATION,
                factionId,
                "none",
                delta,
                EchoScriptingDiagnosticSeverity.INFO
        );
    }
}
