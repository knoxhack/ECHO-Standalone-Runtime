package dev.echo.standalone.runtime.client;

import java.util.EnumMap;
import java.util.Map;

record EchoClientEquipmentScreenModel(Map<EchoClientArmorSlot, EchoClientSlotStack> slots, EchoClientSlotStack offhandSlot) {
    EchoClientEquipmentScreenModel(Map<EchoClientArmorSlot, EchoClientSlotStack> slots) {
        this(slots, EchoClientSlotStack.empty(EchoClientInventoryLayout.offhandSlotIndex()));
    }

    EchoClientEquipmentScreenModel {
        EnumMap<EchoClientArmorSlot, EchoClientSlotStack> padded = new EnumMap<>(EchoClientArmorSlot.class);
        for (EchoClientArmorSlot slot : EchoClientArmorSlot.values()) {
            padded.put(slot, EchoClientSlotStack.empty(EchoClientInventoryLayout.equipmentSlotIndex(slot)));
        }
        if (slots != null) {
            for (Map.Entry<EchoClientArmorSlot, EchoClientSlotStack> entry : slots.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    padded.put(entry.getKey(), entry.getValue());
                }
            }
        }
        slots = Map.copyOf(padded);
        offhandSlot = offhandSlot == null
                ? EchoClientSlotStack.empty(EchoClientInventoryLayout.offhandSlotIndex())
                : offhandSlot;
    }

    static EchoClientEquipmentScreenModel fromEquipment(EchoClientEquipmentState equipment) {
        EchoClientEquipmentState safeEquipment = equipment == null ? EchoClientEquipmentState.empty() : equipment;
        EnumMap<EchoClientArmorSlot, EchoClientSlotStack> slots = new EnumMap<>(EchoClientArmorSlot.class);
        for (EchoClientArmorSlot slot : EchoClientArmorSlot.values()) {
            EchoClientSlotStack stack = safeEquipment.piece(slot)
                    .map(piece -> EchoClientSlotStack.fromItemStack(
                            EchoClientInventoryLayout.equipmentSlotIndex(slot),
                            piece.toStack(),
                            EchoClientToolState.empty()
                    ))
                    .orElseGet(() -> EchoClientSlotStack.empty(EchoClientInventoryLayout.equipmentSlotIndex(slot)));
            slots.put(slot, stack);
        }
        EchoClientSlotStack offhandStack = safeEquipment.offhand()
                .map(stack -> EchoClientSlotStack.fromItemStack(
                        EchoClientInventoryLayout.offhandSlotIndex(),
                        stack,
                        EchoClientToolState.empty()
                ))
                .orElseGet(() -> EchoClientSlotStack.empty(EchoClientInventoryLayout.offhandSlotIndex()));
        return new EchoClientEquipmentScreenModel(slots, offhandStack);
    }

    EchoClientSlotStack slot(EchoClientArmorSlot slot) {
        if (slot == null) {
            return EchoClientSlotStack.empty(0);
        }
        return slots.getOrDefault(slot, EchoClientSlotStack.empty(EchoClientInventoryLayout.equipmentSlotIndex(slot)));
    }
}
