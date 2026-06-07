package dev.echo.standalone.runtime.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class EchoItemDefinitionInference {
    public static final int DEFAULT_MAX_STACK_SIZE = 64;

    private EchoItemDefinitionInference() {
    }

    public static EchoItemDefinition inferDefinition(
            String itemId,
            int requiredStack,
            String sourceTag,
            String tooltipLine
    ) {
        String id = EchoItemText.requireText(itemId, "itemId");
        int maxStack = Math.max(Math.max(1, requiredStack), DEFAULT_MAX_STACK_SIZE);
        return new EchoItemDefinition(
                new EchoItemId(id),
                inferDisplayName(id),
                inferCategory(id),
                maxStack,
                1.0D,
                List.of(EchoItemText.requireText(sourceTag, "sourceTag")),
                List.of(EchoItemText.requireText(tooltipLine, "tooltipLine"))
        );
    }

    public static EchoItemCategory inferCategory(String itemId) {
        String normalized = EchoItemText.requireText(itemId, "itemId").toLowerCase(Locale.ROOT);
        if (containsAny(normalized, "water", "ration", "food", "drink", "consumable", "rad_away", "stim")) {
            return EchoItemCategory.CONSUMABLE;
        }
        if (containsAny(normalized, "filter", "knife", "scanner", "hammer", "blade", "pick", "axe", "tool")) {
            return EchoItemCategory.TOOL;
        }
        if (containsAny(normalized, "armor", "vest", "helmet", "boots", "leggings", "equipment")) {
            return EchoItemCategory.EQUIPMENT;
        }
        if (containsAny(normalized, "quest", "mission", "keystone", "beacon")) {
            return EchoItemCategory.QUEST;
        }
        return EchoItemCategory.MATERIAL;
    }

    public static String inferDisplayName(String itemId) {
        String id = EchoItemText.requireText(itemId, "itemId");
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        String[] words = path.replace('-', '_').replace('/', '_').split("_+");
        ArrayList<String> result = new ArrayList<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            result.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return result.isEmpty() ? id : String.join(" ", result);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
