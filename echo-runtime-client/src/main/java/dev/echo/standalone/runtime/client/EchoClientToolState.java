package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

record EchoClientToolState(Map<String, Integer> durabilityByItemId) {
    EchoClientToolState {
        LinkedHashMap<String, Integer> clean = new LinkedHashMap<>();
        if (durabilityByItemId != null) {
            for (Map.Entry<String, Integer> entry : durabilityByItemId.entrySet()) {
                String itemId = normalizeItemId(entry.getKey());
                int durability = entry.getValue() == null ? 0 : Math.max(0, entry.getValue());
                if (!itemId.isBlank() && durability > 0) {
                    clean.put(itemId, durability);
                }
            }
        }
        durabilityByItemId = Map.copyOf(clean);
    }

    static EchoClientToolState empty() {
        return new EchoClientToolState(Map.of());
    }

    EchoClientToolStatus status(EchoItemDefinition definition, EchoVoxelBlock targetBlock) {
        if (!isTool(definition)) {
            return EchoClientToolStatus.hand();
        }
        String itemId = definition.id().value();
        int maxDurability = maxDurability(definition);
        int durability = durabilityByItemId.getOrDefault(normalizeItemId(itemId), maxDurability);
        boolean broken = durability <= 0;
        double speed = broken ? 1.0D : miningSpeed(definition, targetBlock);
        return new EchoClientToolStatus(
                itemId,
                definition.displayName(),
                durability,
                maxDurability,
                speed,
                true,
                broken
        );
    }

    EchoClientToolState damage(EchoItemDefinition definition, int amount) {
        if (!isTool(definition) || amount <= 0) {
            return this;
        }
        String itemId = normalizeItemId(definition.id().value());
        int nextDurability = Math.max(0, durabilityByItemId.getOrDefault(itemId, maxDurability(definition)) - amount);
        LinkedHashMap<String, Integer> next = new LinkedHashMap<>(durabilityByItemId);
        if (nextDurability > 0) {
            next.put(itemId, nextDurability);
        } else {
            next.remove(itemId);
        }
        return new EchoClientToolState(next);
    }

    EchoClientToolState remove(EchoItemDefinition definition) {
        if (definition == null) {
            return this;
        }
        LinkedHashMap<String, Integer> next = new LinkedHashMap<>(durabilityByItemId);
        next.remove(normalizeItemId(definition.id().value()));
        return new EchoClientToolState(next);
    }

    static boolean isTool(EchoItemDefinition definition) {
        if (definition == null) {
            return false;
        }
        String id = normalizeItemId(definition.id().value());
        return definition.tagged("tool")
                || definition.tagged("mining")
                || id.contains("pick")
                || id.contains("hammer")
                || id.contains("axe");
    }

    static int maxDurability(EchoItemDefinition definition) {
        String id = normalizeItemId(definition == null ? "" : definition.id().value());
        if (id.contains("pick")) {
            return 64;
        }
        if (id.contains("hammer")) {
            return 48;
        }
        if (id.contains("axe")) {
            return 48;
        }
        return 32;
    }

    static int wearForBlock(EchoVoxelBlock block) {
        if (block == null || block.air()) {
            return 0;
        }
        return block.hardness() >= 3.0D ? 2 : 1;
    }

    private static double miningSpeed(EchoItemDefinition definition, EchoVoxelBlock block) {
        String itemId = normalizeItemId(definition.id().value());
        String blockId = block == null ? "" : block.id().toLowerCase(Locale.ROOT);
        if (itemId.contains("pick")) {
            return blockId.contains("ore") || blockId.contains("stone") || blockId.contains("scrap")
                    || blockId.contains("debris") || blockId.contains("metal") || (block != null && block.hardness() >= 1.2D)
                    ? 4.0D
                    : 2.2D;
        }
        if (itemId.contains("hammer")) {
            return 2.8D;
        }
        if (itemId.contains("axe")) {
            return blockId.contains("wood") || blockId.contains("log") || blockId.contains("rust") ? 3.0D : 1.8D;
        }
        return 2.0D;
    }

    private static String normalizeItemId(String itemId) {
        return itemId == null ? "" : itemId.trim().toLowerCase(Locale.ROOT);
    }
}

record EchoClientToolStatus(
        String itemId,
        String label,
        int durability,
        int maxDurability,
        double miningSpeed,
        boolean tool,
        boolean broken
) {
    static EchoClientToolStatus hand() {
        return new EchoClientToolStatus("echo:hand", "Hand", 0, 0, 1.0D, false, false);
    }

    boolean activeTool() {
        return tool && maxDurability > 0;
    }
}
