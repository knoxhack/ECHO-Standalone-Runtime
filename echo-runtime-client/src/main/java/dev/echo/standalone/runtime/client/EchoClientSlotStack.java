package dev.echo.standalone.runtime.client;

import dev.echo.standalone.runtime.item.EchoInventorySlot;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemStack;
import dev.echo.standalone.runtime.player.EchoVoxelHotbarSlot;
import dev.echo.standalone.runtime.world.EchoVoxelBlock;

import java.util.List;
import java.util.Map;
import java.util.Objects;

record EchoClientSlotStack(
        int index,
        EchoClientSlotStackKind kind,
        String runtimeId,
        String label,
        int count,
        EchoVoxelBlock block,
        Map<String, Double> itemModelPredicates,
        List<String> tooltipLines,
        int durability,
        int maxDurability
) {
    private static final int DAMAGE_PREDICATE_BUCKETS = 16;

    EchoClientSlotStack {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
        kind = kind == null ? EchoClientSlotStackKind.EMPTY : kind;
        runtimeId = runtimeId == null ? "" : runtimeId;
        label = label == null || label.isBlank() ? "Empty" : label;
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        if (kind == EchoClientSlotStackKind.EMPTY) {
            runtimeId = "";
            label = "Empty";
            count = 0;
            block = null;
        }
        if (kind == EchoClientSlotStackKind.BLOCK) {
            Objects.requireNonNull(block, "block");
        }
        itemModelPredicates = itemModelPredicates == null ? Map.of() : Map.copyOf(itemModelPredicates);
        tooltipLines = tooltipLines == null
                ? List.of()
                : tooltipLines.stream()
                        .filter(line -> line != null && !line.isBlank())
                        .map(String::trim)
                        .toList();
        maxDurability = Math.max(0, maxDurability);
        durability = Math.max(0, Math.min(durability, maxDurability));
        if (kind != EchoClientSlotStackKind.ITEM || itemModelPredicates.isEmpty()) {
            itemModelPredicates = Map.of();
        }
        if (kind == EchoClientSlotStackKind.EMPTY) {
            tooltipLines = List.of();
            durability = 0;
            maxDurability = 0;
        }
    }

    static EchoClientSlotStack empty(int index) {
        return new EchoClientSlotStack(
                index,
                EchoClientSlotStackKind.EMPTY,
                "",
                "Empty",
                0,
                null,
                Map.of(),
                List.of(),
                0,
                0
        );
    }

    static EchoClientSlotStack fromHotbarSlot(EchoVoxelHotbarSlot slot) {
        Objects.requireNonNull(slot, "slot");
        if (slot.empty()) {
            return empty(slot.index());
        }
        return new EchoClientSlotStack(
                slot.index(),
                EchoClientSlotStackKind.BLOCK,
                slot.block().id(),
                slot.label(),
                slot.count(),
                slot.block(),
                Map.of(),
                List.of("Placeable voxel block"),
                0,
                0
        );
    }

    static EchoClientSlotStack fromInventorySlot(EchoInventorySlot slot) {
        return fromInventorySlot(slot, EchoClientToolState.empty());
    }

    static EchoClientSlotStack fromInventorySlot(EchoInventorySlot slot, EchoClientToolState toolState) {
        Objects.requireNonNull(slot, "slot");
        if (slot.empty()) {
            return empty(slot.index());
        }
        EchoItemStack stack = slot.stack().orElseThrow();
        return fromItemStack(slot.index(), stack, toolState);
    }

    static EchoClientSlotStack fromItemStack(int index, EchoItemStack stack, EchoClientToolState toolState) {
        Objects.requireNonNull(stack, "stack");
        EchoItemDefinition definition = stack.definition();
        EchoClientToolStatus status = toolStatus(definition, toolState);
        return new EchoClientSlotStack(
                index,
                EchoClientSlotStackKind.ITEM,
                stack.itemId().value(),
                definition.displayName(),
                stack.quantity(),
                null,
                itemModelPredicates(status),
                tooltipLines(definition, status),
                status.activeTool() ? status.durability() : 0,
                status.activeTool() ? status.maxDurability() : 0
        );
    }

    static EchoClientSlotStack fromItemDefinition(int index, EchoItemDefinition definition, int count) {
        Objects.requireNonNull(definition, "definition");
        if (count <= 0) {
            return empty(index);
        }
        return new EchoClientSlotStack(
                index,
                EchoClientSlotStackKind.ITEM,
                definition.id().value(),
                definition.displayName(),
                count,
                null,
                Map.of(),
                definition.tooltipLines(),
                0,
                0
        );
    }

    boolean empty() {
        return kind == EchoClientSlotStackKind.EMPTY || count == 0;
    }

    boolean blockSlot() {
        return kind == EchoClientSlotStackKind.BLOCK && !empty();
    }

    boolean itemSlot() {
        return kind == EchoClientSlotStackKind.ITEM && !empty();
    }

    boolean durabilityTracked() {
        return maxDurability > 0;
    }

    private static EchoClientToolStatus toolStatus(EchoItemDefinition definition, EchoClientToolState toolState) {
        if (definition == null || !EchoClientToolState.isTool(definition)) {
            return EchoClientToolStatus.hand();
        }
        return (toolState == null ? EchoClientToolState.empty() : toolState)
                .status(definition, EchoVoxelBlock.AIR);
    }

    private static Map<String, Double> itemModelPredicates(EchoClientToolStatus status) {
        if (status == null || !status.activeTool() || status.maxDurability() <= 0) {
            return Map.of();
        }
        double damage = 1.0D - Math.max(0.0D, Math.min(1.0D, status.durability() / (double) status.maxDurability()));
        return Map.of(
                "damage", quantizedDamagePredicate(damage),
                "damaged", damage > 0.0D ? 1.0D : 0.0D
        );
    }

    private static double quantizedDamagePredicate(double damage) {
        if (!Double.isFinite(damage)) {
            return 0.0D;
        }
        double clamped = Math.max(0.0D, Math.min(1.0D, damage));
        if (clamped == 0.0D || clamped == 1.0D) {
            return clamped;
        }
        return Math.ceil(clamped * DAMAGE_PREDICATE_BUCKETS) / (double) DAMAGE_PREDICATE_BUCKETS;
    }

    private static List<String> tooltipLines(EchoItemDefinition definition, EchoClientToolStatus status) {
        if (definition == null) {
            return List.of();
        }
        java.util.ArrayList<String> lines = new java.util.ArrayList<>(definition.tooltipLines());
        if (status != null && status.activeTool()) {
            lines.add("Durability " + status.durability() + "/" + status.maxDurability());
        }
        return List.copyOf(lines);
    }
}
