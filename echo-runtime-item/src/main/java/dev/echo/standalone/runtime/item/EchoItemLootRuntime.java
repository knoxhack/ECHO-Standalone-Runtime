package dev.echo.standalone.runtime.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoItemLootRuntime {
    private final EchoItemRegistry registry;
    private final EchoInventoryOperations operations;
    private final LinkedHashMap<String, EchoLootTable> lootTables = new LinkedHashMap<>();

    public EchoItemLootRuntime(EchoItemRegistry registry, EchoInventoryOperations operations) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.operations = Objects.requireNonNull(operations, "operations");
    }

    public void register(EchoLootTable table) {
        Objects.requireNonNull(table, "table");
        lootTables.put(table.tableId(), table);
    }

    public Optional<EchoLootTable> find(String tableId) {
        return Optional.ofNullable(lootTables.get(tableId));
    }

    public Map<String, EchoLootTable> lootTables() {
        return Map.copyOf(lootTables);
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

    public EchoItemLootResult grantById(String tableId, EchoInventoryContainer inventory) {
        Objects.requireNonNull(tableId, "tableId");
        Objects.requireNonNull(inventory, "inventory");
        EchoLootTable table = find(tableId).orElse(null);
        if (table == null) {
            return new EchoItemLootResult(tableId, false, 0, 0, "unknown_loot_table");
        }
        return grant(table, inventory);
    }
}
