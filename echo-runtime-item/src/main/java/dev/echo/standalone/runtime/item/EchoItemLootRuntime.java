package dev.echo.standalone.runtime.item;

import java.util.Objects;

public final class EchoItemLootRuntime {
    private final EchoItemRegistry registry;
    private final EchoInventoryOperations operations;

    public EchoItemLootRuntime(EchoItemRegistry registry, EchoInventoryOperations operations) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public EchoItemLootResult grant(EchoLootTable table, EchoInventoryContainer inventory) {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(inventory, "inventory");
        int entriesGranted = 0;
        int quantityGranted = 0;
        for (EchoLootEntry entry : table.entries()) {
            EchoItemDefinition definition = registry.require(entry.itemId());
            EchoInventoryOperationResult added = operations.add(inventory, new EchoItemStack(definition, entry.quantity()));
            if (added.quantity() > 0) {
                entriesGranted++;
                quantityGranted += added.quantity();
            }
            if (!added.success()) {
                return new EchoItemLootResult(table.tableId(), false, entriesGranted, quantityGranted, added.reason());
            }
        }
        return new EchoItemLootResult(table.tableId(), true, entriesGranted, quantityGranted, "granted");
    }
}
