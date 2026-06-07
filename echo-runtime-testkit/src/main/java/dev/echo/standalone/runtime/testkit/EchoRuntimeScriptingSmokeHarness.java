package dev.echo.standalone.runtime.testkit;

import dev.echo.standalone.runtime.core.EchoDefaultRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntime;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntime;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.scripting.EchoRuleActionExecutor;
import dev.echo.standalone.runtime.scripting.EchoRuleConditionEvaluator;
import dev.echo.standalone.runtime.scripting.EchoRuleEngine;
import dev.echo.standalone.runtime.scripting.EchoRuleExecutionContext;
import dev.echo.standalone.runtime.scripting.EchoRuleExecutionReport;
import dev.echo.standalone.runtime.scripting.EchoRuleRegistry;
import dev.echo.standalone.runtime.scripting.EchoRuleSandboxPolicy;
import dev.echo.standalone.runtime.scripting.EchoRuleTrigger;
import dev.echo.standalone.runtime.scripting.EchoRuleValidationResult;
import dev.echo.standalone.runtime.scripting.EchoRuleValidator;
import dev.echo.standalone.runtime.scripting.EchoScriptingDiagnostics;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntime;
import dev.echo.standalone.runtime.scripting.EchoScriptingRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldGenerationProfiles;
import dev.echo.standalone.runtime.world.EchoWorldRuntime;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

public final class EchoRuntimeScriptingSmokeHarness {
    private EchoRuntimeScriptingSmokeHarness() {
    }

    public static void main(String[] args) {
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
}
