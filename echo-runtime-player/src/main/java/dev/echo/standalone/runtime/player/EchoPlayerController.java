package dev.echo.standalone.runtime.player;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.entity.EchoEntityState;
import dev.echo.standalone.runtime.gameplay.EchoGameplayHazardResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputAction;
import dev.echo.standalone.runtime.input.EchoInputActionEvent;
import dev.echo.standalone.runtime.input.EchoInputBinding;
import dev.echo.standalone.runtime.input.EchoInputContext;
import dev.echo.standalone.runtime.input.EchoInputEvent;
import dev.echo.standalone.runtime.input.EchoInputRuntimeResult;
import dev.echo.standalone.runtime.input.EchoInputRouteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoPlayerController {
    private final EchoEntityRuntimeResult entities;
    private final EchoGameplayRuntimeResult gameplay;
    private final EchoInputRuntimeResult input;
    private final EchoEntityId playerId;
    private final EchoPlayerCameraRig cameraRig;
    private final EchoPlayerInteractionTargeter targeter;
    private final EchoPlayerInventoryShortcuts shortcuts;
    private EchoPlayerFacing facing = EchoPlayerFacing.EAST;
    private Optional<EchoPlayerHazardFeedback> lastHazard = Optional.empty();

    public EchoPlayerController(
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoInputRuntimeResult input,
            EchoEntityId playerId,
            EchoPlayerCameraRig cameraRig,
            EchoPlayerInteractionTargeter targeter,
            EchoPlayerInventoryShortcuts shortcuts
    ) {
        this.entities = Objects.requireNonNull(entities, "entities");
        this.gameplay = Objects.requireNonNull(gameplay, "gameplay");
        this.input = Objects.requireNonNull(input, "input");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
        this.cameraRig = Objects.requireNonNull(cameraRig, "cameraRig");
        this.targeter = Objects.requireNonNull(targeter, "targeter");
        this.shortcuts = Objects.requireNonNull(shortcuts, "shortcuts");
    }

    public EchoPlayerControllerResult handle(EchoInputEvent event) {
        Objects.requireNonNull(event, "event");
        if (!event.active()) {
            return EchoPlayerControllerResult.ignored(event, state(), "input-inactive");
        }
        Optional<EchoInputBinding> binding = input.bindings().bindingFor(input.focus().activeContext(), event);
        if (binding.isEmpty() && input.focus().terminalFocused()) {
            EchoInputRouteResult routed = input.dispatch(event);
            return delegated(event, routed);
        }
        if (binding.isEmpty()) {
            return EchoPlayerControllerResult.ignored(event, state(), "unbound:" + event.control().stableId());
        }

        EchoInputAction action = binding.orElseThrow().action();
        return switch (action) {
            case MOVE_NORTH, MOVE_SOUTH, MOVE_WEST, MOVE_EAST -> move(event, action);
            case INTERACT, POINTER_PRIMARY, POINTER_SECONDARY -> interact(event, action);
            case QUICK_SLOT_1 -> shortcut(event, action, 0);
            case QUICK_SLOT_2 -> selectHotbar(event, action, 1);
            case QUICK_SLOT_3 -> selectHotbar(event, action, 2);
            case QUICK_SLOT_4 -> selectHotbar(event, action, 3);
            case QUICK_SLOT_5 -> selectHotbar(event, action, 4);
            case QUICK_SLOT_6 -> selectHotbar(event, action, 5);
            case QUICK_SLOT_7 -> selectHotbar(event, action, 6);
            case QUICK_SLOT_8 -> selectHotbar(event, action, 7);
            case QUICK_SLOT_9 -> selectHotbar(event, action, 8);
            case INVENTORY_TOGGLE, PAUSE_TOGGLE, MOUSE_LOOK -> delegated(event, input.dispatch(event));
            case TERMINAL_FOCUS, TERMINAL_BLUR, TERMINAL_SUBMIT_TEXT -> delegated(event, input.dispatch(event));
            default -> EchoPlayerControllerResult.ignored(event, state(), "unsupported-action:" + action.name());
        };
    }

    public EchoPlayerControllerState state() {
        EchoEntityState player = entities.store().require(playerId);
        return new EchoPlayerControllerState(
                player,
                facing,
                cameraRig.follow(player),
                targeter.target(player, facing),
                lastHazard
        );
    }

    private EchoPlayerControllerResult move(EchoInputEvent event, EchoInputAction action) {
        EchoPlayerFacing nextFacing = EchoPlayerFacing.fromAction(action).orElse(facing);
        facing = nextFacing;
        EchoEntityMovementResult movement = entities.movementSystem().move(
                entities.store(),
                new EchoEntityMovementIntent(playerId, nextFacing.deltaX(), nextFacing.deltaZ())
        );
        ArrayList<String> effects = new ArrayList<>();
        effects.add("controller:movement:" + movement.reason());
        effects.add("facing:" + facing.name());
        Optional<EchoPlayerHazardFeedback> hazard = Optional.empty();
        if (movement.moved()) {
            EchoGameplayHazardResult hazardResult = gameplay.hazardSystem().apply(playerId);
            hazard = Optional.of(EchoPlayerHazardFeedback.from(hazardResult));
            lastHazard = hazard;
            effects.add("camera:follow");
            effects.add("hazard:intensity=" + hazard.orElseThrow().intensity());
        } else {
            effects.add("collision:" + movement.reason());
        }
        EchoPlayerControllerState state = state();
        return new EchoPlayerControllerResult(
                movement.moved(),
                event,
                Optional.of(action),
                Optional.of(movement),
                Optional.empty(),
                Optional.empty(),
                hazard,
                state.target(),
                state,
                effects
        );
    }

    private EchoPlayerControllerResult interact(EchoInputEvent event, EchoInputAction action) {
        EchoPlayerControllerState before = state();
        Optional<EchoPlayerInteractionTarget> target = before.target();
        if (target.isEmpty()) {
            return new EchoPlayerControllerResult(
                    false,
                    event,
                    Optional.of(action),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    before,
                    List.of("interaction:no_target")
            );
        }
        EchoGameplayInteractionResult interaction;
        if (isDropPodTerminalTarget(target.orElseThrow().id())) {
            interaction = gameplay.interactionSystem().activateTerminal(playerId);
        } else if (target.orElseThrow().id().equals("ashfall:crash_cache")) {
            interaction = gameplay.interactionSystem().salvageCrashCache(playerId);
        } else {
            interaction = new EchoGameplayInteractionResult(
                    "player:unknown_target",
                    false,
                    false,
                    0,
                    "unsupported_target"
            );
        }
        EchoPlayerControllerState state = state();
        return new EchoPlayerControllerResult(
                interaction.success(),
                event,
                Optional.of(action),
                Optional.empty(),
                Optional.of(interaction),
                Optional.empty(),
                Optional.empty(),
                target,
                state,
                List.of("interaction:target=" + target.orElseThrow().id(), "interaction:" + interaction.reason())
        );
    }

    private static boolean isDropPodTerminalTarget(String targetId) {
        return "echoashfallprotocol:poi/drop_pod".equals(targetId)
                || "ashfall:terminal_pod".equals(targetId);
    }

    private EchoPlayerControllerResult shortcut(EchoInputEvent event, EchoInputAction action, int slotIndex) {
        EchoPlayerInventoryShortcutResult shortcut = shortcuts.useSlot(slotIndex);
        EchoPlayerControllerState state = state();
        return new EchoPlayerControllerResult(
                shortcut.used(),
                event,
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.of(shortcut),
                Optional.empty(),
                state.target(),
                state,
                List.of("shortcut:" + slotIndex, "shortcut:" + shortcut.reason())
        );
    }

    private EchoPlayerControllerResult selectHotbar(EchoInputEvent event, EchoInputAction action, int slotIndex) {
        EchoPlayerControllerState state = state();
        return new EchoPlayerControllerResult(
                true,
                event,
                Optional.of(action),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                state.target(),
                state,
                List.of("hotbar:selected:" + (slotIndex + 1))
        );
    }

    private EchoPlayerControllerResult delegated(EchoInputEvent event, EchoInputRouteResult routed) {
        EchoPlayerControllerState state = state();
        Optional<EchoInputAction> action = routed.action().map(EchoInputActionEvent::action);
        return new EchoPlayerControllerResult(
                routed.handled(),
                event,
                action,
                routed.movementResult(),
                routed.interactionResult(),
                Optional.empty(),
                Optional.empty(),
                state.target(),
                state,
                routed.effects()
        );
    }
}
