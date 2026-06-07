package dev.echo.standalone.runtime.input;

import dev.echo.standalone.runtime.entity.EchoEntityId;
import dev.echo.standalone.runtime.entity.EchoEntityMovementIntent;
import dev.echo.standalone.runtime.entity.EchoEntityMovementResult;
import dev.echo.standalone.runtime.entity.EchoEntityRuntimeResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayInteractionResult;
import dev.echo.standalone.runtime.gameplay.EchoGameplayRuntimeResult;
import dev.echo.standalone.runtime.ui.EchoUiInputEvent;
import dev.echo.standalone.runtime.ui.EchoUiInputResult;
import dev.echo.standalone.runtime.ui.EchoUiRuntimeResult;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class EchoInputRouter {
    private final EchoInputBindingMap bindings;
    private final EchoInputFocusState focus;
    private final EchoUiRuntimeResult ui;
    private final EchoEntityRuntimeResult entities;
    private final EchoGameplayRuntimeResult gameplay;
    private final EchoEntityId playerId;

    public EchoInputRouter(
            EchoInputBindingMap bindings,
            EchoInputFocusState focus,
            EchoUiRuntimeResult ui,
            EchoEntityRuntimeResult entities,
            EchoGameplayRuntimeResult gameplay,
            EchoEntityId playerId
    ) {
        this.bindings = Objects.requireNonNull(bindings, "bindings");
        this.focus = Objects.requireNonNull(focus, "focus");
        this.ui = Objects.requireNonNull(ui, "ui");
        this.entities = Objects.requireNonNull(entities, "entities");
        this.gameplay = Objects.requireNonNull(gameplay, "gameplay");
        this.playerId = Objects.requireNonNull(playerId, "playerId");
    }

    public EchoInputRouteResult route(EchoInputEvent event) {
        Objects.requireNonNull(event, "event");
        if (!event.active()) {
            return EchoInputRouteResult.ignored("input-inactive:" + event.control().stableId());
        }

        EchoInputContext context = focus.activeContext();
        Optional<EchoInputBinding> binding = bindings.bindingFor(context, event);
        if (binding.isEmpty() && context == EchoInputContext.TERMINAL && event.eventType() != EchoInputEventType.TEXT) {
            binding = bindings.bindingFor(EchoInputContext.GAMEPLAY, event);
        }
        if (binding.isEmpty() && event.eventType() == EchoInputEventType.TEXT && focus.terminalFocused()) {
            binding = bindings.bindingFor(EchoInputContext.TERMINAL, event);
        }
        if (binding.isEmpty()) {
            return EchoInputRouteResult.ignored("unbound:" + context.name() + ":" + event.control().stableId());
        }

        EchoInputActionEvent action = new EchoInputActionEvent(
                event.sequence(),
                focus.activeContext(),
                binding.get().action(),
                event
        );
        return switch (binding.get().action()) {
            case TERMINAL_FOCUS -> focusTerminal(action);
            case TERMINAL_BLUR -> blurTerminal(action);
            case TERMINAL_SUBMIT_TEXT -> routeTerminalText(action);
            case MOVE_NORTH -> routeMovement(action, 0, -1);
            case MOVE_SOUTH -> routeMovement(action, 0, 1);
            case MOVE_WEST -> routeMovement(action, -1, 0);
            case MOVE_EAST -> routeMovement(action, 1, 0);
            case INTERACT, POINTER_PRIMARY, POINTER_SECONDARY -> routeInteract(action);
            case QUICK_SLOT_1 -> routeQuickSlot(action, 1);
            case QUICK_SLOT_2 -> routeHotbarSelect(action, 2);
            case QUICK_SLOT_3 -> routeHotbarSelect(action, 3);
            case QUICK_SLOT_4 -> routeHotbarSelect(action, 4);
            case QUICK_SLOT_5 -> routeHotbarSelect(action, 5);
            case QUICK_SLOT_6 -> routeHotbarSelect(action, 6);
            case QUICK_SLOT_7 -> routeHotbarSelect(action, 7);
            case QUICK_SLOT_8 -> routeHotbarSelect(action, 8);
            case QUICK_SLOT_9 -> routeHotbarSelect(action, 9);
            case INVENTORY_TOGGLE -> routeOverlayToggle(action, "inventory");
            case INVENTORY_DRAG_STACK -> routeInventoryUi(action, "drag_move", "click/drag move");
            case INVENTORY_SPLIT_STACK -> routeInventoryUi(action, "split_stack", "stack split");
            case INVENTORY_ASSIGN_HOTBAR -> routeInventoryUi(
                    action,
                    "assign_hotbar:" + hotbarSlotFromControl(action.source().control()),
                    "hotbar assignment"
            );
            case INVENTORY_SHOW_TOOLTIP -> routeInventoryUi(action, "show_tooltip", "item description tooltip");
            case INVENTORY_USE_SELECTED -> routeInventoryUi(action, "use_selected", "consume/use feedback");
            case PAUSE_TOGGLE -> routeOverlayToggle(action, "pause");
            case MOUSE_LOOK -> routeMouseLook(action);
        };
    }

    private EchoInputRouteResult focusTerminal(EchoInputActionEvent action) {
        String focusPath = ui.frame().screen().focusPath();
        focus.focusTerminal(focusPath);
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.UI,
                List.of("focus:terminal", "focusPath:" + focus.focusPath())
        );
    }

    private EchoInputRouteResult blurTerminal(EchoInputActionEvent action) {
        focus.focusGameplay();
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.UI,
                List.of("focus:gameplay")
        );
    }

    private EchoInputRouteResult routeTerminalText(EchoInputActionEvent action) {
        if (!focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-not-focused");
        }
        EchoUiInputResult result = ui.dispatch(EchoUiInputEvent.text(action.sequence(), action.source().text()));
        return EchoInputRouteResult.ui(
                action,
                result,
                List.of("route:ui", "focusPath:" + focus.focusPath())
        );
    }

    private EchoInputRouteResult routeMovement(EchoInputActionEvent action, int deltaX, int deltaZ) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        EchoEntityMovementResult movement = entities.movementSystem().move(
                entities.store(),
                new EchoEntityMovementIntent(playerId, deltaX, deltaZ)
        );
        return EchoInputRouteResult.movement(
                action,
                movement,
                List.of("route:gameplay", "movement:" + movement.reason())
        );
    }

    private EchoInputRouteResult routeInteract(EchoInputActionEvent action) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        EchoGameplayInteractionResult terminal = gameplay.interactionSystem().activateTerminal(playerId);
        if (terminal.success()) {
            return EchoInputRouteResult.interaction(
                    action,
                    terminal,
                    List.of("route:gameplay", "interaction:" + terminal.interactionId())
            );
        }
        EchoGameplayInteractionResult cache = gameplay.interactionSystem().salvageCrashCache(playerId);
        return EchoInputRouteResult.interaction(
                action,
                cache,
                List.of("route:gameplay", "interaction:" + cache.interactionId(), "terminal:" + terminal.reason())
        );
    }

    private EchoInputRouteResult routeQuickSlot(EchoInputActionEvent action, int slot) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        EchoGameplayInteractionResult water = gameplay.interactionSystem().drinkWater(playerId);
        return EchoInputRouteResult.interaction(
                action,
                water,
                List.of("route:gameplay", "quick-slot:" + slot, "interaction:" + water.interactionId())
        );
    }

    private EchoInputRouteResult routeHotbarSelect(EchoInputActionEvent action, int slot) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.GAMEPLAY,
                List.of("route:gameplay", "quick-slot:" + slot, "select-hotbar")
        );
    }

    private EchoInputRouteResult routeOverlayToggle(EchoInputActionEvent action, String overlay) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.UI,
                List.of("route:ui", "overlay:" + overlay, "mouse:released")
        );
    }

    private EchoInputRouteResult routeInventoryUi(
            EchoInputActionEvent action,
            String operation,
            String feedback
    ) {
        if (focus.activeContext() != EchoInputContext.UI) {
            return EchoInputRouteResult.ignored("inventory-ui-not-focused");
        }
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.UI,
                List.of(
                        "route:ui",
                        "overlay:inventory",
                        "inventory:" + operation,
                        "feedback:" + feedback
                )
        );
    }

    private EchoInputRouteResult routeMouseLook(EchoInputActionEvent action) {
        if (focus.terminalFocused()) {
            return EchoInputRouteResult.ignored("terminal-focus-blocks-gameplay");
        }
        return EchoInputRouteResult.handled(
                action,
                EchoInputRouteTarget.GAMEPLAY,
                List.of("route:gameplay", "mouse-look:captured")
        );
    }

    private static int hotbarSlotFromControl(EchoInputControl control) {
        String code = control.code();
        if (code.startsWith("DIGIT") && code.length() == 6) {
            char digit = code.charAt(5);
            if (digit >= '1' && digit <= '9') {
                return digit - '0';
            }
        }
        return 0;
    }
}
