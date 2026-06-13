package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.scripting.EchoRuleAction;
import dev.echo.standalone.runtime.scripting.EchoRuleActionExecutor;
import dev.echo.standalone.runtime.scripting.EchoRuleActionResult;
import dev.echo.standalone.runtime.scripting.EchoRuleConditionEvaluator;
import dev.echo.standalone.runtime.scripting.EchoRuleConditionResult;
import dev.echo.standalone.runtime.scripting.EchoRuleDefinition;
import dev.echo.standalone.runtime.scripting.EchoRuleEngine;
import dev.echo.standalone.runtime.scripting.EchoRuleExecutionContext;
import dev.echo.standalone.runtime.scripting.EchoRuleExecutionReport;
import dev.echo.standalone.runtime.scripting.EchoRuleExecutionResult;
import dev.echo.standalone.runtime.scripting.EchoRuleRegistry;
import dev.echo.standalone.runtime.scripting.EchoRuleSandboxPolicy;
import dev.echo.standalone.runtime.scripting.EchoRuleTrigger;
import dev.echo.standalone.runtime.scripting.EchoRuleValidationResult;
import dev.echo.standalone.runtime.scripting.EchoRuleValidator;
import dev.echo.standalone.runtime.scripting.EchoScriptingDiagnostic;
import dev.echo.standalone.runtime.scripting.EchoScriptingDiagnostics;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntime;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class EchoRuntimeScriptingSmokeHarness {
    private EchoRuntimeScriptingSmokeHarness() {
    }

    public static void main(String[] args) throws IOException {
        EchoDefaultRuntimeServiceRegistry services = new EchoDefaultRuntimeServiceRegistry();
        EchoWorldRuntimeResult world = new EchoWorldRuntime().createDebugWorld(
                services,
                EchoWorldGenerationProfiles.ashfallCrashSite()
        );
        EchoEntityRuntimeResult entities = new EchoEntityRuntime().createDebugEntities(services, world);
        EchoItemRuntimeResult items = new EchoItemRuntime().createDebugInventory(services, entities);
        EchoGameplayRuntimeResult gameplay = new EchoGameplayRuntime().createDebugGameplay(
                services,
                world,
                entities,
                items
        );
        EchoScriptingRuntimeResult scripting = new EchoScriptingRuntime().createDebugRules(
                services,
                world,
                entities,
                items,
                gameplay
        );

        require(services.require(EchoScriptingRuntimeResult.class) == scripting,
                "scripting runtime result should be service-bound");
        require(services.require(EchoRuleSandboxPolicy.class) == scripting.sandboxPolicy(),
                "sandbox policy should be service-bound");
        require(services.require(EchoRuleRegistry.class) == scripting.ruleRegistry(),
                "rule registry should be service-bound");
        require(services.require(EchoRuleValidator.class) == scripting.validator(),
                "rule validator should be service-bound");
        require(services.require(EchoRuleConditionEvaluator.class) == scripting.conditionEvaluator(),
                "condition evaluator should be service-bound");
        require(services.require(EchoRuleActionExecutor.class) == scripting.actionExecutor(),
                "action executor should be service-bound");
        require(services.require(EchoRuleEngine.class) == scripting.engine(),
                "rule engine should be service-bound");
        require(services.require(EchoScriptingDiagnostics.class) == scripting.diagnostics(),
                "scripting diagnostics should be service-bound");
        require(services.require(EchoRuleValidationResult.class) == scripting.validation(),
                "validation result should be service-bound");
        require(services.require(EchoRuleExecutionContext.class) == scripting.initialContext(),
                "initial execution context should be service-bound");
        require(services.require(EchoRuleExecutionReport.class) == scripting.initialReport(),
                "initial execution report should be service-bound");

        require(scripting.sandboxPolicy().policyId().equals("echo:declarative_rules_only"),
                "sandbox policy id should be stable");
        require(!scripting.sandboxPolicy().arbitraryCodeAllowed(),
                "scripting runtime must not allow arbitrary code");
        require(scripting.ruleRegistry().count() == 3,
                "debug scripting registry should contain three rules");
        require(scripting.ruleRegistry().byTrigger(EchoRuleTrigger.WORLD_TICK).size() == 3,
                "world tick trigger should contain three rules");
        require(scripting.validation().valid(), "debug rules should validate");
        require(scripting.validation().issues().isEmpty(), "debug rules should not have validation issues");

        EchoRuleExecutionReport report = scripting.initialReport();
        require(report.trigger() == EchoRuleTrigger.WORLD_TICK,
                "initial report should execute the world tick trigger");
        require(report.evaluatedRules() == 3, "initial report should evaluate three rules");
        require(report.matchedRules() == 3, "initial report should match three rules");
        require(report.actionCount() == 8, "initial report should execute eight actions");
        require(report.appliedActionCount() == 8, "initial report should apply eight actions");
        require(report.matchedRuleIds().get(0).equals("ashfall:storm_pressure_warning"),
                "storm pressure rule should run first");
        require(report.matchedRuleIds().get(1).equals("ashfall:terminal_rule_unlock"),
                "terminal rule should run second");
        require(report.matchedRuleIds().get(2).equals("ashfall:hydration_nudge"),
                "hydration rule should run third");

        require(gameplay.mission().objective("ashfall:activate_terminal")
                        .orElseThrow()
                        .completed(),
                "terminal objective should be completed by declarative rule action");
        require(gameplay.mission().completedObjectiveCount() == 1,
                "exactly one mission objective should be completed");
        require(gameplay.progression().experience() == 10,
                "terminal rule should award ten experience");
        require(gameplay.progression().milestones().contains("rule_terminal_ready"),
                "terminal rule milestone should be present");
        require(gameplay.notifications().count() == 3,
                "initial gameplay notification plus two rule notifications should be present");
        requireDouble(gameplay.survival().hydration(), 55.0D,
                "hydration nudge should recommend water without consuming it");

        require(scripting.diagnostics().count() == 8,
                "diagnostics should include initialization, validation, matches, and rule diagnostics");
        require(scripting.diagnostics().warningCount() == 1,
                "storm pressure diagnostic should be the only warning");
        require(scripting.diagnostics().errorCount() == 0,
                "debug scripting runtime should have no error diagnostics");

        writeReports(
                Path.of(".").toAbsolutePath().normalize(),
                scripting,
                gameplay
        );

        System.out.println("phase14.16 scripting runtime smoke PASS rules="
                + scripting.ruleRegistry().count()
                + " evaluated="
                + report.evaluatedRules()
                + " matched="
                + report.matchedRules()
                + " actions="
                + report.actionCount()
                + " diagnostics="
                + scripting.diagnostics().count()
                + " notifications="
                + gameplay.notifications().count()
                + " arbitraryCode="
                + scripting.sandboxPolicy().arbitraryCodeAllowed());
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void requireDouble(double actual, double expected, String message) {
        if (Math.abs(actual - expected) > 0.0001D) {
            throw new AssertionError(message + ": expected=" + expected + " actual=" + actual);
        }
    }

    private static void writeReports(
            Path standaloneRoot,
            EchoScriptingRuntimeResult scripting,
            EchoGameplayRuntimeResult gameplay
    ) throws IOException {
        Path root = standaloneRoot.resolve("reports/echo/standalone");
        Files.createDirectories(root);

        EchoRuleExecutionReport report = scripting.initialReport();
        List<EchoRuleDefinition> rules = scripting.ruleRegistry().all();
        int conditionCount = rules.stream()
                .mapToInt(rule -> rule.conditions().size())
                .sum();
        int matchedConditionCount = report.results().stream()
                .flatMap(result -> result.conditionResults().stream())
                .filter(EchoRuleConditionResult::matched)
                .mapToInt(result -> 1)
                .sum();
        int actionDefinitionCount = rules.stream()
                .mapToInt(rule -> rule.actions().size())
                .sum();
        List<EchoRuleActionResult> actionResults = report.results().stream()
                .flatMap(result -> result.actionResults().stream())
                .toList();

        write(root.resolve("runtime-scripting.json"), """
                {
                  "schema": "echo.standalone.runtime_scripting.v2",
                  "status": "PASS",
                  "phase": "14.16",
                  "summary": "Scripting runtime created service-bound declarative rule services, validated sandboxed rules, executed world-tick rules, applied gameplay effects, and recorded diagnostics without arbitrary code execution.",
                  "runtimeResultServiceBound": true,
                  "sandboxPolicyServiceBound": true,
                  "ruleRegistryServiceBound": true,
                  "validatorServiceBound": true,
                  "conditionEvaluatorServiceBound": true,
                  "actionExecutorServiceBound": true,
                  "engineServiceBound": true,
                  "diagnosticsServiceBound": true,
                  "validationServiceBound": true,
                  "contextServiceBound": true,
                  "executionReportServiceBound": true,
                  "policyId": "%s",
                  "arbitraryCodeAllowed": %s,
                  "ruleCount": %d,
                  "evaluatedRules": %d,
                  "matchedRules": %d,
                  "actionCount": %d,
                  "appliedActionCount": %d,
                  "diagnosticCount": %d,
                  "warningCount": %d,
                  "errorCount": %d,
                  "notificationCount": %d
                }
                """.formatted(
                escape(scripting.sandboxPolicy().policyId()),
                scripting.sandboxPolicy().arbitraryCodeAllowed(),
                scripting.ruleRegistry().count(),
                report.evaluatedRules(),
                report.matchedRules(),
                report.actionCount(),
                report.appliedActionCount(),
                scripting.diagnostics().count(),
                scripting.diagnostics().warningCount(),
                scripting.diagnostics().errorCount(),
                gameplay.notifications().count()
        ));

        write(root.resolve("scripting-sandbox.json"), """
                {
                  "schema": "echo.standalone.scripting_sandbox.v2",
                  "status": "PASS",
                  "policyId": "%s",
                  "declarativeOnly": %s,
                  "arbitraryCodeAllowed": %s,
                  "maxRules": %d,
                  "maxConditionsPerRule": %d,
                  "maxActionsPerRule": %d,
                  "allowedTriggers": %s,
                  "allowedConditions": %s,
                  "allowedActions": %s
                }
                """.formatted(
                escape(scripting.sandboxPolicy().policyId()),
                !scripting.sandboxPolicy().arbitraryCodeAllowed(),
                scripting.sandboxPolicy().arbitraryCodeAllowed(),
                scripting.sandboxPolicy().maxRules(),
                scripting.sandboxPolicy().maxConditionsPerRule(),
                scripting.sandboxPolicy().maxActionsPerRule(),
                enumArray(scripting.sandboxPolicy().allowedTriggers()),
                enumArray(scripting.sandboxPolicy().allowedConditions()),
                enumArray(scripting.sandboxPolicy().allowedActions())
        ));

        write(root.resolve("scripting-rules.json"), """
                {
                  "schema": "echo.standalone.scripting_rules.v2",
                  "status": "PASS",
                  "ruleCount": %d,
                  "enabledRuleCount": %d,
                  "ruleIds": %s,
                  "rules": %s,
                  "priorityOrder": %s
                }
                """.formatted(
                scripting.ruleRegistry().count(),
                (int) rules.stream().filter(EchoRuleDefinition::enabled).count(),
                stringArray(rules.stream().map(EchoRuleDefinition::ruleId).toList()),
                rulesJson(rules),
                stringArray(rules.stream().map(EchoRuleDefinition::ruleId).toList())
        ));

        write(root.resolve("scripting-triggers.json"), """
                {
                  "schema": "echo.standalone.scripting_triggers.v2",
                  "status": "PASS",
                  "executedTrigger": "%s",
                  "contextTrigger": "%s",
                  "contextTick": %d,
                  "worldTickRuleCount": %d,
                  "allowedTriggers": %s,
                  "allRulesTriggered": %s
                }
                """.formatted(
                report.trigger().name(),
                scripting.initialContext().trigger().name(),
                scripting.initialContext().tick(),
                scripting.ruleRegistry().byTrigger(EchoRuleTrigger.WORLD_TICK).size(),
                enumArray(scripting.sandboxPolicy().allowedTriggers()),
                report.evaluatedRules() == scripting.ruleRegistry().byTrigger(EchoRuleTrigger.WORLD_TICK).size()
        ));

        write(root.resolve("scripting-conditions.json"), """
                {
                  "schema": "echo.standalone.scripting_conditions.v2",
                  "status": "PASS",
                  "conditionCount": %d,
                  "matchedConditionCount": %d,
                  "conditionTypes": %s,
                  "matchedConditionResults": %s,
                  "allConditionsMatched": %s
                }
                """.formatted(
                conditionCount,
                matchedConditionCount,
                stringArray(rules.stream()
                        .flatMap(rule -> rule.conditions().stream())
                        .map(condition -> condition.type().name())
                        .distinct()
                        .sorted()
                        .toList()),
                conditionResultsJson(report.results()),
                conditionCount == matchedConditionCount
        ));

        write(root.resolve("scripting-actions.json"), """
                {
                  "schema": "echo.standalone.scripting_actions.v2",
                  "status": "PASS",
                  "actionDefinitionCount": %d,
                  "actionCount": %d,
                  "appliedActionCount": %d,
                  "actionTypes": %s,
                  "actionResults": %s,
                  "allActionsApplied": %s,
                  "objectiveCompleted": %s,
                  "experienceAwarded": %d,
                  "milestonePresent": %s
                }
                """.formatted(
                actionDefinitionCount,
                report.actionCount(),
                report.appliedActionCount(),
                stringArray(rules.stream()
                        .flatMap(rule -> rule.actions().stream())
                        .map(action -> action.type().name())
                        .distinct()
                        .sorted()
                        .toList()),
                actionResultsJson(actionResults),
                actionResults.stream().allMatch(EchoRuleActionResult::applied),
                gameplay.mission().objective("ashfall:activate_terminal").orElseThrow().completed(),
                gameplay.progression().experience(),
                gameplay.progression().milestones().contains("rule_terminal_ready")
        ));

        write(root.resolve("scripting-validation.json"), """
                {
                  "schema": "echo.standalone.scripting_validation.v2",
                  "status": "PASS",
                  "valid": %s,
                  "issueCount": %d,
                  "warningCount": %d,
                  "errorCount": %d,
                  "maxRulesRespected": %s,
                  "maxConditionsRespected": %s,
                  "maxActionsRespected": %s
                }
                """.formatted(
                scripting.validation().valid(),
                scripting.validation().issues().size(),
                scripting.validation().warningCount(),
                scripting.validation().errorCount(),
                scripting.ruleRegistry().count() <= scripting.sandboxPolicy().maxRules(),
                rules.stream().allMatch(rule -> rule.conditions().size()
                        <= scripting.sandboxPolicy().maxConditionsPerRule()),
                rules.stream().allMatch(rule -> rule.actions().size()
                        <= scripting.sandboxPolicy().maxActionsPerRule())
        ));

        write(root.resolve("scripting-execution.json"), """
                {
                  "schema": "echo.standalone.scripting_execution.v2",
                  "status": "PASS",
                  "trigger": "%s",
                  "evaluatedRules": %d,
                  "matchedRules": %d,
                  "actionCount": %d,
                  "appliedActionCount": %d,
                  "matchedRuleIds": %s,
                  "results": %s,
                  "missionObjectiveCompleted": %s,
                  "completedObjectiveCount": %d,
                  "experience": %d,
                  "milestones": %s,
                  "notificationCount": %d,
                  "hydration": %.1f
                }
                """.formatted(
                report.trigger().name(),
                report.evaluatedRules(),
                report.matchedRules(),
                report.actionCount(),
                report.appliedActionCount(),
                stringArray(report.matchedRuleIds()),
                executionResultsJson(report.results()),
                gameplay.mission().objective("ashfall:activate_terminal").orElseThrow().completed(),
                gameplay.mission().completedObjectiveCount(),
                gameplay.progression().experience(),
                stringArray(gameplay.progression().milestones()),
                gameplay.notifications().count(),
                gameplay.survival().hydration()
        ));

        write(root.resolve("scripting-diagnostics.json"), """
                {
                  "schema": "echo.standalone.scripting_diagnostics.v2",
                  "status": "PASS",
                  "diagnosticCount": %d,
                  "warningCount": %d,
                  "errorCount": %d,
                  "diagnostics": %s,
                  "containsSandboxInitialized": %s,
                  "containsValidation": %s,
                  "containsStormWarning": %s
                }
                """.formatted(
                scripting.diagnostics().count(),
                scripting.diagnostics().warningCount(),
                scripting.diagnostics().errorCount(),
                diagnosticsJson(scripting.diagnostics().all()),
                scripting.diagnostics().all().stream()
                        .anyMatch(diagnostic -> diagnostic.message().contains("sandbox initialized")),
                scripting.diagnostics().all().stream()
                        .anyMatch(diagnostic -> diagnostic.message().contains("validated 3")),
                scripting.diagnostics().all().stream()
                        .anyMatch(diagnostic -> diagnostic.message().contains("Ash storm rule matched"))
        ));

        write(root.resolve("scripting-boundaries.json"), """
                {
                  "schema": "echo.standalone.scripting_boundaries.v2",
                  "status": "PASS",
                  "declarativeRulesOnly": true,
                  "arbitraryCodeAllowed": %s,
                  "jvmScriptEngineFree": true,
                  "nashornFree": true,
                  "graalPolyglotFree": true,
                  "groovyRuntimeFree": true,
                  "minecraftFreeRuntime": true,
                  "boundedRuleCount": %s,
                  "boundedConditions": %s,
                  "boundedActions": %s,
                  "forbiddenBoundaryCheckedByVerifier": true
                }
                """.formatted(
                scripting.sandboxPolicy().arbitraryCodeAllowed(),
                scripting.ruleRegistry().count() <= scripting.sandboxPolicy().maxRules(),
                rules.stream().allMatch(rule -> rule.conditions().size()
                        <= scripting.sandboxPolicy().maxConditionsPerRule()),
                rules.stream().allMatch(rule -> rule.actions().size()
                        <= scripting.sandboxPolicy().maxActionsPerRule())
        ));
    }

    private static String rulesJson(List<EchoRuleDefinition> rules) {
        return rules.stream()
                .map(rule -> """
                        {
                            "ruleId": "%s",
                            "displayName": "%s",
                            "trigger": "%s",
                            "priority": %d,
                            "enabled": %s,
                            "conditionCount": %d,
                            "actionCount": %d
                          }""".formatted(
                        escape(rule.ruleId()),
                        escape(rule.displayName()),
                        rule.trigger().name(),
                        rule.priority(),
                        rule.enabled(),
                        rule.conditions().size(),
                        rule.actions().size()
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String conditionResultsJson(List<EchoRuleExecutionResult> results) {
        return results.stream()
                .flatMap(result -> result.conditionResults().stream()
                        .map(conditionResult -> conditionResultJson(result.rule().ruleId(), conditionResult)))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String conditionResultJson(String ruleId, EchoRuleConditionResult result) {
        return """
                {
                    "ruleId": "%s",
                    "conditionId": "%s",
                    "type": "%s",
                    "target": "%s",
                    "matched": %s,
                    "actual": "%s"
                  }""".formatted(
                escape(ruleId),
                escape(result.condition().conditionId()),
                result.condition().type().name(),
                escape(result.condition().target()),
                result.matched(),
                escape(result.actual())
        );
    }

    private static String actionResultsJson(List<EchoRuleActionResult> results) {
        return results.stream()
                .map(result -> actionResultJson(result.action(), result))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String actionResultJson(EchoRuleAction action, EchoRuleActionResult result) {
        return """
                {
                    "actionId": "%s",
                    "type": "%s",
                    "target": "%s",
                    "amount": %d,
                    "applied": %s,
                    "reason": "%s"
                  }""".formatted(
                escape(action.actionId()),
                action.type().name(),
                escape(action.target()),
                action.amount(),
                result.applied(),
                escape(result.reason())
        );
    }

    private static String executionResultsJson(List<EchoRuleExecutionResult> results) {
        return results.stream()
                .map(result -> """
                        {
                            "ruleId": "%s",
                            "matched": %s,
                            "conditionCount": %d,
                            "actionCount": %d,
                            "appliedActionCount": %d
                          }""".formatted(
                        escape(result.rule().ruleId()),
                        result.matched(),
                        result.conditionResults().size(),
                        result.actionResults().size(),
                        (int) result.actionResults().stream().filter(EchoRuleActionResult::applied).count()
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String diagnosticsJson(List<EchoScriptingDiagnostic> diagnostics) {
        return diagnostics.stream()
                .map(diagnostic -> """
                        {
                            "severity": "%s",
                            "ruleId": "%s",
                            "message": "%s"
                          }""".formatted(
                        diagnostic.severity().name(),
                        escape(diagnostic.ruleId()),
                        escape(diagnostic.message())
                ))
                .collect(Collectors.joining(",\n", "[\n", "\n  ]"));
    }

    private static String enumArray(Collection<? extends Enum<?>> values) {
        return values.stream()
                .map(Enum::name)
                .sorted()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String stringArray(Collection<String> values) {
        return values.stream()
                .map(value -> "\"" + escape(value) + "\"")
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
