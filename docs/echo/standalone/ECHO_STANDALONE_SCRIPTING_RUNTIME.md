# ECHO Standalone Scripting / Rules Runtime

Phase 14.16 adds the first standalone scripting runtime as a sandboxed declarative rules engine. It introduces typed triggers, conditions, actions, validation, execution reports, and diagnostics.

This phase deliberately does not execute arbitrary source code. Rules are Java records made of known condition and action enums, then validated against a sandbox policy before execution.

## Runtime Pieces

- `EchoScriptingRuntime` creates and service-binds the debug rules runtime.
- `EchoRuleSandboxPolicy` defines the declarative-only sandbox limits.
- `EchoRuleRegistry` stores deterministic rule definitions.
- `EchoRuleValidator` validates rules against sandbox limits and current runtime state.
- `EchoRuleConditionEvaluator` evaluates supported declarative conditions.
- `EchoRuleActionExecutor` applies supported declarative actions.
- `EchoRuleEngine` evaluates trigger-matched rules in priority order.
- `EchoRuleExecutionReport` records evaluated rules, matched rules, actions, and applied actions.
- `EchoScriptingDiagnostics` records validation and execution diagnostics.

## Sandbox Policy

The Phase 14.16 policy is:

```text
policy: echo:declarative_rules_only
arbitraryCodeAllowed: false
maxRules: 32
maxConditionsPerRule: 8
maxActionsPerRule: 8
```

Allowed declarations are limited to the runtime enums:

```text
triggers: BOOTSTRAP, WORLD_TICK, MISSION_STATUS, SURVIVAL_CHANGED
conditions: ALWAYS, OBJECTIVE_ACTIVE, OBJECTIVE_COMPLETED, HYDRATION_AT_OR_BELOW, ASH_DENSITY_AT_LEAST, HAZARD_COUNT_AT_LEAST, INVENTORY_CONTAINS
actions: EMIT_DIAGNOSTIC, ADD_NOTIFICATION, COMPLETE_OBJECTIVE, AWARD_EXPERIENCE, ADD_MILESTONE, ADJUST_FACTION_REPUTATION
```

## Debug Rules

The debug rule set contains three `WORLD_TICK` rules:

```text
ashfall:storm_pressure_warning
ashfall:terminal_rule_unlock
ashfall:hydration_nudge
```

The rules validate against existing Ashfall debug runtime state, then execute in priority order. The initial execution evaluates three rules, matches three rules, executes eight actions, and applies eight actions.

## Execution Effects

The initial debug execution:

- emits an ash storm warning diagnostic.
- adds an ash storm shelter advisory notification.
- completes `ashfall:activate_terminal`.
- awards ten progression experience.
- adds the `rule_terminal_ready` milestone.
- adds a hydration recommendation notification without consuming water.

## Runtime Safety

The scripting runtime does not import or start a script engine. It does not use Java scripting APIs, Nashorn, Graal polyglot, Groovy, classloaders, reflection execution, sockets, Minecraft, NeoForge, native renderer APIs, or native audio APIs.

## Smoke Harness Coverage

The Phase 14.16 smoke harness proves:

- scripting runtime result, sandbox policy, rule registry, validator, condition evaluator, action executor, rule engine, diagnostics, validation result, execution context, and execution report are service-bound.
- arbitrary code execution is disabled.
- three debug rules are registered and bound to the world tick trigger.
- validation passes with no issues.
- the initial execution evaluates and matches all three rules.
- eight actions execute and apply.
- rule execution order is deterministic.
- the terminal objective is completed by a rule action.
- ten progression experience is awarded.
- the `rule_terminal_ready` milestone is added.
- two gameplay notifications are added by rules.
- hydration is not consumed by the recommendation rule.
- diagnostics are deterministic and contain no errors.

## Out Of Scope

Phase 14.16 does not:

- execute arbitrary scripts
- parse JavaScript, Lua, Groovy, Python, or other script source
- load plugins or classloaders
- expose reflection execution
- run user-provided bytecode
- watch files for hot reload
- persist rule state

The next phase is Phase 14.17, the Compatibility + Migration Layer.
