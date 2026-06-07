package dev.echo.standalone.runtime.input;

import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.ui.EchoUiInputResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EchoInputRouteResult(
        boolean handled,
        EchoInputRouteTarget target,
        Optional<EchoInputActionEvent> action,
        Optional<EchoUiInputResult> uiResult,
        Optional<EchoEntityMovementResult> movementResult,
        Optional<EchoGameplayInteractionResult> interactionResult,
        List<String> effects
) {
    public EchoInputRouteResult {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(uiResult, "uiResult");
        Objects.requireNonNull(movementResult, "movementResult");
        Objects.requireNonNull(interactionResult, "interactionResult");
        Objects.requireNonNull(effects, "effects");
        effects = List.copyOf(effects);
    }

    public static EchoInputRouteResult ignored(String reason) {
        return new EchoInputRouteResult(
                false,
                EchoInputRouteTarget.IGNORED,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of(reason)
        );
    }

    public static EchoInputRouteResult ui(
            EchoInputActionEvent action,
            EchoUiInputResult uiResult,
            List<String> effects
    ) {
        return new EchoInputRouteResult(
                uiResult.handled(),
                EchoInputRouteTarget.UI,
                Optional.of(action),
                Optional.of(uiResult),
                Optional.empty(),
                Optional.empty(),
                effects
        );
    }

    public static EchoInputRouteResult movement(
            EchoInputActionEvent action,
            EchoEntityMovementResult movement,
            List<String> effects
    ) {
        return new EchoInputRouteResult(
                movement.moved(),
                EchoInputRouteTarget.GAMEPLAY,
                Optional.of(action),
                Optional.empty(),
                Optional.of(movement),
                Optional.empty(),
                effects
        );
    }

    public static EchoInputRouteResult interaction(
            EchoInputActionEvent action,
            EchoGameplayInteractionResult interaction,
            List<String> effects
    ) {
        return new EchoInputRouteResult(
                interaction.success(),
                EchoInputRouteTarget.GAMEPLAY,
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.of(interaction),
                effects
        );
    }

    public static EchoInputRouteResult handled(
            EchoInputActionEvent action,
            EchoInputRouteTarget target,
            List<String> effects
    ) {
        return new EchoInputRouteResult(
                true,
                target,
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                effects
        );
    }
}
