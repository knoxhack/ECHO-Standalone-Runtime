package dev.echo.standalone.runtime.scripting;

import dev.echo.standalone.runtime.gameplay.EchoGameplayNotificationSeverity;

import java.util.Objects;

public final class EchoRuleActionExecutor {
    public EchoRuleActionResult execute(
            EchoRuleDefinition rule,
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        Objects.requireNonNull(rule, "rule");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(context, "context");
        return switch (action.type()) {
            case EMIT_DIAGNOSTIC -> emitDiagnostic(rule, action, context);
            case ADD_NOTIFICATION -> addNotification(action, context);
            case COMPLETE_OBJECTIVE -> completeObjective(action, context);
            case AWARD_EXPERIENCE -> awardExperience(action, context);
            case ADD_MILESTONE -> addMilestone(action, context);
            case ADJUST_FACTION_REPUTATION -> adjustFaction(action, context);
        };
    }

    private static EchoRuleActionResult emitDiagnostic(
            EchoRuleDefinition rule,
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        context.diagnostics().add(new EchoScriptingDiagnostic(action.severity(), rule.ruleId(), action.message()));
        return new EchoRuleActionResult(action, true, "diagnostic emitted");
    }

    private static EchoRuleActionResult addNotification(
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        context.gameplay().notifications().add(toNotificationSeverity(action.severity()), action.message(), context.tick());
        return new EchoRuleActionResult(action, true, "notification added");
    }

    private static EchoRuleActionResult completeObjective(
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        boolean completed = context.gameplay().mission().completeObjective(action.target());
        return new EchoRuleActionResult(
                action,
                completed,
                completed ? "objective completed" : "objective already complete"
        );
    }

    private static EchoRuleActionResult awardExperience(
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        context.gameplay().progression().awardExperience(action.amount());
        return new EchoRuleActionResult(action, true, "experience awarded");
    }

    private static EchoRuleActionResult addMilestone(
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        context.gameplay().progression().addMilestone(action.target());
        return new EchoRuleActionResult(action, true, "milestone added");
    }

    private static EchoRuleActionResult adjustFaction(
            EchoRuleAction action,
            EchoRuleExecutionContext context
    ) {
        context.gameplay().factions().adjustReputation(action.target(), action.amount());
        return new EchoRuleActionResult(action, true, "faction reputation adjusted");
    }

    private static EchoGameplayNotificationSeverity toNotificationSeverity(EchoScriptingDiagnosticSeverity severity) {
        return switch (severity) {
            case INFO -> EchoGameplayNotificationSeverity.INFO;
            case WARNING -> EchoGameplayNotificationSeverity.WARNING;
            case ERROR -> EchoGameplayNotificationSeverity.CRITICAL;
        };
    }
}
