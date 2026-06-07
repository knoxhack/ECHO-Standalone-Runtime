package dev.echo.standalone.runtime.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EchoItemTooltipRenderer {
    public List<String> render(EchoItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        return render(stack, true, "");
    }

    public List<String> render(EchoItemStack stack, boolean enabled, String disabledReason) {
        Objects.requireNonNull(stack, "stack");
        ArrayList<String> lines = new ArrayList<>();
        lines.add(stack.definition().displayName());
        lines.add("Item: " + stack.definition().id().value());
        lines.add("Quantity: " + stack.quantity() + "/" + stack.definition().maxStackSize());
        lines.add("Category: " + stack.definition().category().name());
        lines.add("Use: " + usageLine(stack.definition()));
        lines.addAll(stack.definition().tooltipLines());
        if (!stack.definition().tags().isEmpty()) {
            lines.add("Tags: " + String.join(", ", stack.definition().tags()));
        }
        lines.add(enabled ? "State: Ready" : "State: Disabled - " + normalize(disabledReason, "action unavailable"));
        return List.copyOf(lines);
    }

    public List<String> renderUseFeedback(EchoItemStack stack, EchoInventoryOperationResult result) {
        Objects.requireNonNull(stack, "stack");
        Objects.requireNonNull(result, "result");
        ArrayList<String> lines = new ArrayList<>(render(stack, result.success(), result.reason()));
        lines.add(result.success()
                ? "Feedback: used " + result.quantity() + " " + stack.definition().displayName()
                : "Feedback: cannot use - " + normalize(result.reason(), "unavailable"));
        return List.copyOf(lines);
    }

    private static String usageLine(EchoItemDefinition definition) {
        if (definition.category() == EchoItemCategory.CONSUMABLE || definition.tagged("consumable")) {
            return "Consume to recover survival resources";
        }
        if (definition.category() == EchoItemCategory.TOOL || definition.tagged("crafting")) {
            return "Use for crafting, repair, or route progress";
        }
        if (definition.category() == EchoItemCategory.EQUIPMENT) {
            return "Equip or assign to a hotbar slot";
        }
        if (definition.category() == EchoItemCategory.MATERIAL) {
            return "Carry as crafting material";
        }
        return "Keep for mission progress";
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().replace('_', ' ');
    }
}
