package dev.echo.standalone.runtime.scripting;

import java.util.Objects;

public record EchoScriptingRuntimeResult(
        EchoRuleSandboxPolicy sandboxPolicy,
        EchoRuleRegistry ruleRegistry,
        EchoRuleValidator validator,
        EchoRuleConditionEvaluator conditionEvaluator,
        EchoRuleActionExecutor actionExecutor,
        EchoRuleEngine engine,
        EchoScriptingDiagnostics diagnostics,
        EchoRuleValidationResult validation,
        EchoRuleExecutionContext initialContext,
        EchoRuleExecutionReport initialReport
) {
    public EchoScriptingRuntimeResult {
        Objects.requireNonNull(sandboxPolicy, "sandboxPolicy");
        Objects.requireNonNull(ruleRegistry, "ruleRegistry");
        Objects.requireNonNull(validator, "validator");
        Objects.requireNonNull(conditionEvaluator, "conditionEvaluator");
        Objects.requireNonNull(actionExecutor, "actionExecutor");
        Objects.requireNonNull(engine, "engine");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(validation, "validation");
        Objects.requireNonNull(initialContext, "initialContext");
        Objects.requireNonNull(initialReport, "initialReport");
    }
}
