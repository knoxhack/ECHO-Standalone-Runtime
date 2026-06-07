package dev.echo.standalone.runtime.scripting;

import dev.echo.standalone.runtime.item.EchoItemId;

import java.util.Objects;

public final class EchoRuleConditionEvaluator {
    public EchoRuleConditionResult evaluate(EchoRuleCondition condition, EchoRuleExecutionContext context) {
        Objects.requireNonNull(condition, "condition");
        Objects.requireNonNull(context, "context");
        return switch (condition.type()) {
            case ALWAYS -> new EchoRuleConditionResult(condition, true, "always");
            case OBJECTIVE_ACTIVE -> objectiveActive(condition, context);
            case OBJECTIVE_COMPLETED -> objectiveCompleted(condition, context);
            case HYDRATION_AT_OR_BELOW -> hydrationAtOrBelow(condition, context);
            case ASH_DENSITY_AT_LEAST -> ashDensityAtLeast(condition, context);
            case HAZARD_COUNT_AT_LEAST -> hazardCountAtLeast(condition, context);
            case INVENTORY_CONTAINS -> inventoryContains(condition, context);
        };
    }

    private static EchoRuleConditionResult objectiveActive(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        boolean completed = context.gameplay().mission().objective(condition.target())
                .map(objective -> objective.completed())
                .orElse(false);
        boolean matched = !completed;
        return new EchoRuleConditionResult(condition, matched, completed ? "objective=COMPLETED" : "objective=ACTIVE");
    }

    private static EchoRuleConditionResult objectiveCompleted(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        boolean completed = context.gameplay().mission().objective(condition.target())
                .map(objective -> objective.completed())
                .orElse(false);
        return new EchoRuleConditionResult(condition, completed, completed ? "objective=COMPLETED" : "objective=ACTIVE");
    }

    private static EchoRuleConditionResult hydrationAtOrBelow(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        double hydration = context.gameplay().survival().hydration();
        return new EchoRuleConditionResult(
                condition,
                hydration <= condition.threshold(),
                "hydration=" + hydration
        );
    }

    private static EchoRuleConditionResult ashDensityAtLeast(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        double ashDensity = context.world().world().chunks().stream()
                .mapToDouble(chunk -> chunk.weather().ashDensity())
                .max()
                .orElse(0.0D);
        return new EchoRuleConditionResult(
                condition,
                ashDensity >= condition.threshold(),
                "ashDensity=" + ashDensity
        );
    }

    private static EchoRuleConditionResult hazardCountAtLeast(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        int hazardCount = context.world().world().hazardCount();
        return new EchoRuleConditionResult(
                condition,
                hazardCount >= condition.amount(),
                "hazards=" + hazardCount
        );
    }

    private static EchoRuleConditionResult inventoryContains(
            EchoRuleCondition condition,
            EchoRuleExecutionContext context
    ) {
        EchoItemId itemId = new EchoItemId(condition.target());
        int quantity = context.items().inventoryStore().all().stream()
                .flatMap(container -> container.slots().stream())
                .flatMap(slot -> slot.stack().stream())
                .filter(stack -> stack.itemId().equals(itemId))
                .mapToInt(stack -> stack.quantity())
                .sum();
        return new EchoRuleConditionResult(
                condition,
                quantity >= condition.amount(),
                "quantity=" + quantity
        );
    }
}
