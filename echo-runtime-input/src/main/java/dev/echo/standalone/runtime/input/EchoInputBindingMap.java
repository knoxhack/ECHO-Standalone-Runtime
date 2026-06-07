package dev.echo.standalone.runtime.input;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoInputBindingMap {
    private final LinkedHashMap<BindingKey, EchoInputBinding> bindings = new LinkedHashMap<>();

    public EchoInputBindingMap(List<EchoInputBinding> bindings) {
        Objects.requireNonNull(bindings, "bindings");
        bindings.forEach(this::bind);
    }

    public static EchoInputBindingMap ashfallDefaults() {
        return new EchoInputBindingMap(List.of(
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("W"), EchoInputAction.MOVE_NORTH, "Move forward"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("S"), EchoInputAction.MOVE_SOUTH, "Move backward"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("A"), EchoInputAction.MOVE_WEST, "Strafe left"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("D"), EchoInputAction.MOVE_EAST, "Strafe right"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("E"), EchoInputAction.INVENTORY_TOGGLE, "Inventory"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("ESCAPE"), EchoInputAction.PAUSE_TOGGLE, "Pause"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT1"), EchoInputAction.QUICK_SLOT_1, "Quick slot 1"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT2"), EchoInputAction.QUICK_SLOT_2, "Quick slot 2"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT3"), EchoInputAction.QUICK_SLOT_3, "Quick slot 3"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT4"), EchoInputAction.QUICK_SLOT_4, "Quick slot 4"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT5"), EchoInputAction.QUICK_SLOT_5, "Quick slot 5"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT6"), EchoInputAction.QUICK_SLOT_6, "Quick slot 6"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT7"), EchoInputAction.QUICK_SLOT_7, "Quick slot 7"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT8"), EchoInputAction.QUICK_SLOT_8, "Quick slot 8"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("DIGIT9"), EchoInputAction.QUICK_SLOT_9, "Quick slot 9"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.keyboard("BACKQUOTE"), EchoInputAction.TERMINAL_FOCUS, "Focus Terminal"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.mouse("PRIMARY"), EchoInputAction.POINTER_PRIMARY, "Mine"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.mouse("SECONDARY"), EchoInputAction.POINTER_SECONDARY, "Use or place"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.mouse("MOVE"), EchoInputAction.MOUSE_LOOK, "Mouse look"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("DPAD_UP"), EchoInputAction.MOVE_NORTH, "Gamepad north"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("DPAD_DOWN"), EchoInputAction.MOVE_SOUTH, "Gamepad south"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("DPAD_LEFT"), EchoInputAction.MOVE_WEST, "Gamepad west"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("DPAD_RIGHT"), EchoInputAction.MOVE_EAST, "Gamepad east"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("BUTTON_SOUTH"), EchoInputAction.INTERACT, "Gamepad interact"),
                new EchoInputBinding(EchoInputContext.GAMEPLAY, EchoInputControl.gamepad("BUTTON_NORTH"), EchoInputAction.TERMINAL_FOCUS, "Gamepad Terminal"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.mouse("PRIMARY"), EchoInputAction.INVENTORY_DRAG_STACK, "Move inventory stack"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.mouse("SECONDARY"), EchoInputAction.INVENTORY_SPLIT_STACK, "Split inventory stack"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.mouse("MOVE"), EchoInputAction.INVENTORY_SHOW_TOOLTIP, "Inventory tooltip"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("ENTER"), EchoInputAction.INVENTORY_USE_SELECTED, "Use selected inventory item"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT1"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 1"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT2"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 2"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT3"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 3"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT4"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 4"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT5"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 5"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT6"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 6"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT7"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 7"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT8"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 8"),
                new EchoInputBinding(EchoInputContext.UI, EchoInputControl.keyboard("DIGIT9"), EchoInputAction.INVENTORY_ASSIGN_HOTBAR, "Assign hotbar slot 9"),
                new EchoInputBinding(EchoInputContext.TERMINAL, EchoInputControl.keyboard("TEXT"), EchoInputAction.TERMINAL_SUBMIT_TEXT, "Terminal text"),
                new EchoInputBinding(EchoInputContext.TERMINAL, EchoInputControl.keyboard("ESCAPE"), EchoInputAction.TERMINAL_BLUR, "Leave Terminal")
        ));
    }

    public synchronized void bind(EchoInputBinding binding) {
        Objects.requireNonNull(binding, "binding");
        bindings.put(new BindingKey(binding.context(), binding.control()), binding);
    }

    public synchronized EchoInputBinding rebind(
            EchoInputContext context,
            EchoInputAction action,
            EchoInputControl control
    ) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(control, "control");
        bindings.entrySet().removeIf(entry -> entry.getValue().context() == context
                && entry.getValue().action() == action
                && entry.getValue().control().deviceType() == control.deviceType());
        EchoInputBinding binding = new EchoInputBinding(context, control, action, "Rebound " + action.name().toLowerCase());
        bind(binding);
        return binding;
    }

    public synchronized Optional<EchoInputBinding> bindingFor(EchoInputContext context, EchoInputEvent event) {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(event, "event");
        return Optional.ofNullable(bindings.get(new BindingKey(context, event.control())));
    }

    public synchronized List<EchoInputBinding> bindings() {
        ArrayList<EchoInputBinding> snapshot = new ArrayList<>(bindings.values());
        snapshot.sort(Comparator.comparing((EchoInputBinding binding) -> binding.context().name())
                .thenComparing(binding -> binding.action().name())
                .thenComparing(binding -> binding.control().stableId()));
        return List.copyOf(snapshot);
    }

    public synchronized Map<String, String> actionSummary() {
        LinkedHashMap<String, String> summary = new LinkedHashMap<>();
        bindings().forEach(binding -> summary.put(
                binding.context().name() + "." + binding.action().name() + "." + binding.control().deviceType().name(),
                binding.control().stableId()
        ));
        return Map.copyOf(summary);
    }

    private record BindingKey(EchoInputContext context, EchoInputControl control) {
        private BindingKey {
            Objects.requireNonNull(context, "context");
            Objects.requireNonNull(control, "control");
        }
    }
}
