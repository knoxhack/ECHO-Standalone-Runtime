package dev.echo.standalone.runtime.scripting;

import dev.echo.standalone.runtime.contracts.EchoRuntimeServiceRegistry;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntime;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.List;
import java.util.Objects;

public final class EchoScriptingRuntime {
    public EchoScriptingRuntimeResult createDebugRules(
            EchoRuntimeServiceRegistry services,
            EchoWorldRuntimeResult world,
            EchoEntityRuntimeResult entities,
            EchoItemRuntimeResult items,
            EchoGameplayRuntimeResult gameplay
    ) {
        Objects.requireNonNull(services, "services");
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");

        EchoRuleSandboxPolicy policy = EchoRuleSandboxPolicy.declarativeOnly();
        EchoRuleRegistry registry = new EchoRuleRegistry();
        registerDebugRules(registry);
        EchoScriptingDiagnostics diagnostics = new EchoScriptingDiagnostics();
        diagnostics.info("runtime", "scripting sandbox initialized");

        EchoRuleExecutionContext initialContext = new EchoRuleExecutionContext(
                world,
                entities,
                items,
                gameplay,
                diagnostics,
                EchoRuleTrigger.WORLD_TICK,
                world.world().tick()
        );
        EchoRuleValidator validator = new EchoRuleValidator(policy);
        EchoRuleValidationResult validation = validator.validate(registry, initialContext);
        if (!validation.valid()) {
            diagnostics.warning("runtime", "debug rules failed validation");
        } else {
            diagnostics.info("runtime", "validated " + registry.count() + " declarative rules");
        }
        EchoRuleConditionEvaluator conditionEvaluator = new EchoRuleConditionEvaluator();
        EchoRuleActionExecutor actionExecutor = new EchoRuleActionExecutor();
        EchoRuleEngine engine = new EchoRuleEngine(registry, conditionEvaluator, actionExecutor);
        EchoRuleExecutionReport initialReport = validation.valid()
                ? engine.execute(EchoRuleTrigger.WORLD_TICK, initialContext)
                : new EchoRuleExecutionReport(EchoRuleTrigger.WORLD_TICK, 0, 0, 0, 0, List.of());

        EchoScriptingRuntimeResult result = new EchoScriptingRuntimeResult(
                policy,
                registry,
                validator,
                conditionEvaluator,
                actionExecutor,
                engine,
                diagnostics,
                validation,
                initialContext,
                initialReport
        );
        services.register(EchoScriptingRuntimeResult.class, result);
        services.register(EchoRuleSandboxPolicy.class, policy);
        services.register(EchoRuleRegistry.class, registry);
        services.register(EchoRuleValidator.class, validator);
        services.register(EchoRuleConditionEvaluator.class, conditionEvaluator);
        services.register(EchoRuleActionExecutor.class, actionExecutor);
        services.register(EchoRuleEngine.class, engine);
        services.register(EchoScriptingDiagnostics.class, diagnostics);
        services.register(EchoRuleValidationResult.class, validation);
        services.register(EchoRuleExecutionContext.class, initialContext);
        services.register(EchoRuleExecutionReport.class, initialReport);
        return result;
    }

    private static void registerDebugRules(EchoRuleRegistry registry) {
        registry.register(new EchoRuleDefinition(
                "ashfall:storm_pressure_warning",
                "Storm Pressure Warning",
                EchoRuleTrigger.WORLD_TICK,
                10,
                true,
                List.of(
                        EchoRuleCondition.ashDensityAtLeast("ash-density", 0.65D),
                        EchoRuleCondition.hazardCountAtLeast("hazard-count", 1)
                ),
                List.of(
                        EchoRuleAction.diagnostic(
                                "storm-diagnostic",
                                EchoScriptingDiagnosticSeverity.WARNING,
                                "Ash storm rule matched."
                        ),
                        EchoRuleAction.notification(
                                "storm-notification",
                                EchoScriptingDiagnosticSeverity.WARNING,
                                "Rules: ash storm shelter advisory issued."
                        )
                )
        ));
        registry.register(new EchoRuleDefinition(
                "ashfall:terminal_rule_unlock",
                "Terminal Rule Unlock",
                EchoRuleTrigger.WORLD_TICK,
                20,
                true,
                List.of(
                        EchoRuleCondition.objectiveActive("terminal-objective-active", "ashfall:activate_terminal"),
                        EchoRuleCondition.inventoryContains(
                                "salvage-available",
                                EchoItemRuntime.SCRAP_METAL_ITEM_ID,
                                3
                        )
                ),
                List.of(
                        EchoRuleAction.completeObjective("complete-terminal-objective", "ashfall:activate_terminal"),
                        EchoRuleAction.awardExperience("award-terminal-rule-xp", 10),
                        EchoRuleAction.addMilestone("add-terminal-rule-milestone", "rule_terminal_ready"),
                        EchoRuleAction.diagnostic(
                                "terminal-diagnostic",
                                EchoScriptingDiagnosticSeverity.INFO,
                                "Terminal rule completed objective from declarative conditions."
                        )
                )
        ));
        registry.register(new EchoRuleDefinition(
                "ashfall:hydration_nudge",
                "Hydration Nudge",
                EchoRuleTrigger.WORLD_TICK,
                30,
                true,
                List.of(
                        EchoRuleCondition.hydrationAtOrBelow("hydration-low", 60.0D),
                        EchoRuleCondition.inventoryContains(
                                "water-available",
                                EchoItemRuntime.CLEAN_WATER_BOTTLE_ITEM_ID,
                                1
                        )
                ),
                List.of(
                        EchoRuleAction.notification(
                                "hydration-notification",
                                EchoScriptingDiagnosticSeverity.INFO,
                                "Rules: water ration recommended before the next push."
                        ),
                        EchoRuleAction.diagnostic(
                                "hydration-diagnostic",
                                EchoScriptingDiagnosticSeverity.INFO,
                                "Hydration rule matched with water available."
                        )
                )
        ));
    }
}
