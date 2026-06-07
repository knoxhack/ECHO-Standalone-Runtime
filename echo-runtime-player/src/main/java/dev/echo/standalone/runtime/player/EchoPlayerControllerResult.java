package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record EchoPlayerControllerResult(
        boolean handled,
        EchoInputEvent source,
        Optional<EchoInputAction> action,
        Optional<EchoEntityMovementResult> movement,
        Optional<EchoGameplayInteractionResult> interaction,
        Optional<EchoPlayerInventoryShortcutResult> shortcut,
        Optional<EchoPlayerHazardFeedback> hazard,
        Optional<EchoPlayerInteractionTarget> target,
        EchoPlayerControllerState state,
        List<String> effects
) {
    public EchoPlayerControllerResult {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(movement, "movement");
        Objects.requireNonNull(interaction, "interaction");
        Objects.requireNonNull(shortcut, "shortcut");
        Objects.requireNonNull(hazard, "hazard");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(effects, "effects");
        effects = List.copyOf(effects);
    }

    public static EchoPlayerControllerResult ignored(
            EchoInputEvent source,
            EchoPlayerControllerState state,
            String reason
    ) {
        return new EchoPlayerControllerResult(
                false,
                source,
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                state.target(),
                state,
                List.of(reason)
        );
    }
}
