package dev.echo.standalone.runtime.scripting;

import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.item.EchoItemRuntimeResult;
import dev.echo.standalone.runtime.world.EchoWorldRuntimeResult;

import java.util.Objects;

public record EchoRuleExecutionContext(
        EchoWorldRuntimeResult world,
        EchoEntityRuntimeResult entities,
        EchoItemRuntimeResult items,
        EchoGameplayRuntimeResult gameplay,
        EchoScriptingDiagnostics diagnostics,
        EchoRuleTrigger trigger,
        long tick
) {
    public EchoRuleExecutionContext {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(entities, "entities");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(gameplay, "gameplay");
        Objects.requireNonNull(diagnostics, "diagnostics");
        Objects.requireNonNull(trigger, "trigger");
        if (tick < 0) {
            throw new IllegalArgumentException("tick must not be negative");
        }
    }

    public EchoRuleExecutionContext withTrigger(EchoRuleTrigger nextTrigger) {
        return new EchoRuleExecutionContext(
                world,
                entities,
                items,
                gameplay,
                diagnostics,
                nextTrigger,
                tick
        );
    }
}
