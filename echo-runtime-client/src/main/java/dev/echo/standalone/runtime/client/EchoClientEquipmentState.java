package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

record EchoClientEquipmentState(List<EchoClientArmorPiece> armorPieces, EchoItemStack offhandStack) {
    EchoClientEquipmentState(List<EchoClientArmorPiece> armorPieces) {
        this(armorPieces, null);
    }

    EchoClientEquipmentState {
        EnumMap<EchoClientArmorSlot, EchoClientArmorPiece> deduped = new EnumMap<>(EchoClientArmorSlot.class);
        if (armorPieces != null) {
            for (EchoClientArmorPiece piece : armorPieces) {
                if (piece != null && !piece.broken()) {
                    deduped.put(piece.slot(), piece);
                }
            }
        }
        armorPieces = List.copyOf(deduped.values());
    }

    static EchoClientEquipmentState empty() {
        return new EchoClientEquipmentState(List.of(), null);
    }

    Optional<EchoItemStack> offhand() {
        return Optional.ofNullable(offhandStack);
    }

    boolean offhandEmpty() {
        return offhandStack == null;
    }

    EchoClientEquipmentState withOffhand(EchoItemStack stack) {
        return new EchoClientEquipmentState(armorPieces, stack);
    }

    EchoClientEquipmentState withoutOffhand() {
        return new EchoClientEquipmentState(armorPieces, null);
    }

    Optional<EchoClientArmorPiece> piece(EchoClientArmorSlot slot) {
        if (slot == null) {
            return Optional.empty();
        }
        return armorPieces.stream()
                .filter(piece -> piece.slot() == slot)
                .findFirst();
    }

    EchoClientEquipmentState equip(EchoClientArmorPiece piece) {
        if (piece == null || piece.broken()) {
            return this;
        }
        ArrayList<EchoClientArmorPiece> next = new ArrayList<>();
        for (EchoClientArmorPiece current : armorPieces) {
            if (current.slot() != piece.slot()) {
                next.add(current);
            }
        }
        next.add(piece);
        return new EchoClientEquipmentState(next, offhandStack);
    }

    EchoClientEquipmentState unequip(EchoClientArmorSlot slot) {
        if (slot == null || armorPieces.isEmpty()) {
            return this;
        }
        ArrayList<EchoClientArmorPiece> next = new ArrayList<>();
        for (EchoClientArmorPiece current : armorPieces) {
            if (current.slot() != slot) {
                next.add(current);
            }
        }
        return new EchoClientEquipmentState(next, offhandStack);
    }

    EchoClientEquipmentState damageArmor(int amount) {
        int loss = Math.max(1, amount > 0 ? 1 : 0);
        if (loss == 0 || armorPieces.isEmpty()) {
            return this;
        }
        ArrayList<EchoClientArmorPiece> next = new ArrayList<>();
        for (EchoClientArmorPiece piece : armorPieces) {
            EchoClientArmorPiece damaged = piece.damage(loss);
            if (!damaged.broken()) {
                next.add(damaged);
            }
        }
        return new EchoClientEquipmentState(next, offhandStack);
    }

    int armorPoints() {
        int points = 0;
        for (EchoClientArmorPiece piece : armorPieces) {
            points += piece.armorPoints();
        }
        return Math.min(20, points);
    }

    int armorSlotsFilled() {
        return armorPieces.size();
    }

    int mitigateDamage(int rawDamage) {
        int safeDamage = Math.max(0, rawDamage);
        if (safeDamage == 0) {
            return 0;
        }
        int points = armorPoints();
        if (points <= 0) {
            return safeDamage;
        }
        double reduction = Math.min(0.80D, points * 0.04D);
        return Math.max(1, (int) Math.ceil(safeDamage * (1.0D - reduction)));
    }

    String debugText() {
        if (armorPieces.isEmpty()) {
            return offhandStack == null ? "NONE" : "OFFHAND:" + offhandStack.itemId().value();
        }
        ArrayList<String> parts = new ArrayList<>();
        for (EchoClientArmorSlot slot : EchoClientArmorSlot.values()) {
            piece(slot).ifPresent(piece -> parts.add(piece.debugText()));
        }
        if (offhandStack != null) {
            parts.add("OFFHAND:" + offhandStack.itemId().value() + "x" + offhandStack.quantity());
        }
        return String.join(" ", parts);
    }

    Map<String, String> summaryMetadata() {
        return Map.of(
                "armorPoints", Integer.toString(armorPoints()),
                "armorSlots", Integer.toString(armorSlotsFilled()),
                "offhand", offhandStack == null ? "" : offhandStack.itemId().value()
        );
    }
}
