package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemCategory;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

record EchoClientArmorPiece(
        EchoClientArmorSlot slot,
        String itemId,
        String displayName,
        int armorPoints,
        int durability,
        int maxDurability
) {
    EchoClientArmorPiece {
        slot = slot == null ? EchoClientArmorSlot.CHEST : slot;
        itemId = itemId == null || itemId.isBlank() ? "echo:unknown_armor" : itemId.trim();
        displayName = displayName == null || displayName.isBlank() ? itemId : displayName.trim();
        armorPoints = Math.max(0, Math.min(20, armorPoints));
        maxDurability = Math.max(1, maxDurability);
        durability = Math.max(0, Math.min(maxDurability, durability));
    }

    static Optional<EchoClientArmorPiece> fromItem(EchoItemDefinition definition) {
        if (definition == null) {
            return Optional.empty();
        }
        String id = normalize(definition.id().value());
        EchoClientArmorSlot slot = inferSlot(definition, id);
        if (slot == null) {
            return Optional.empty();
        }
        int points = inferArmorPoints(slot, id);
        int durability = inferDurability(slot, id);
        return Optional.of(new EchoClientArmorPiece(
                slot,
                definition.id().value(),
                definition.displayName(),
                points,
                durability,
                durability
        ));
    }

    EchoClientArmorPiece damage(int amount) {
        int loss = Math.max(0, amount);
        if (loss == 0 || durability == 0) {
            return this;
        }
        return new EchoClientArmorPiece(slot, itemId, displayName, armorPoints, durability - loss, maxDurability);
    }

    boolean broken() {
        return durability <= 0;
    }

    EchoItemStack toStack() {
        return new EchoItemStack(new EchoItemDefinition(
                new EchoItemId(itemId),
                displayName,
                EchoItemCategory.TOOL,
                1,
                1.0D,
                List.of("armor", "equipment", slot.id()),
                List.of("Armor " + armorPoints, "Durability " + durability + "/" + maxDurability)
        ), 1);
    }

    String debugText() {
        return slot.id().toUpperCase(Locale.ROOT) + ":" + armorPoints + "@" + durability;
    }

    private static EchoClientArmorSlot inferSlot(EchoItemDefinition definition, String id) {
        if (definition.tagged("helmet") || definition.tagged("head") || id.contains("helmet")
                || id.contains("gas_mask") || id.contains("mask")) {
            return EchoClientArmorSlot.HEAD;
        }
        if (definition.tagged("chestplate") || definition.tagged("chest") || id.contains("chestplate")
                || id.contains("vest") || id.contains("cuirass")) {
            return EchoClientArmorSlot.CHEST;
        }
        if (definition.tagged("leggings") || definition.tagged("legs") || id.contains("leggings")
                || id.contains("leg_armor") || id.contains("pants")) {
            return EchoClientArmorSlot.LEGS;
        }
        if (definition.tagged("boots") || definition.tagged("feet") || id.contains("boots")) {
            return EchoClientArmorSlot.FEET;
        }
        if (definition.tagged("armor") || id.endsWith("_armor") || id.contains(":armor_")) {
            return EchoClientArmorSlot.CHEST;
        }
        return null;
    }

    private static int inferArmorPoints(EchoClientArmorSlot slot, String id) {
        int base = switch (slot) {
            case HEAD -> 2;
            case CHEST -> 6;
            case LEGS -> 5;
            case FEET -> 2;
        };
        if (id.contains("scrap") || id.contains("leather") || id.contains("cloth")) {
            return Math.max(1, base - 1);
        }
        if (id.contains("alloy") || id.contains("steel") || id.contains("dense")) {
            return base + 1;
        }
        if (id.contains("power") || id.contains("exo")) {
            return base + 2;
        }
        return base;
    }

    private static int inferDurability(EchoClientArmorSlot slot, String id) {
        int base = switch (slot) {
            case HEAD -> 90;
            case CHEST -> 180;
            case LEGS -> 160;
            case FEET -> 110;
        };
        if (id.contains("scrap") || id.contains("cloth")) {
            return Math.max(30, base - 35);
        }
        if (id.contains("alloy") || id.contains("steel") || id.contains("dense")) {
            return base + 50;
        }
        if (id.contains("power") || id.contains("exo")) {
            return base + 90;
        }
        return base;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
