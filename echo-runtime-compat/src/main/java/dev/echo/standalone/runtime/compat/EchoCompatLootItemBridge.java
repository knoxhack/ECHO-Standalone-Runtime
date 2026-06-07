package dev.echo.standalone.runtime.compat;

import dev.echo.standalone.runtime.data.EchoLootDefinition;
import dev.echo.standalone.runtime.data.EchoLootRegistry;
import dev.echo.standalone.runtime.item.EchoItemDefinition;
import dev.echo.standalone.runtime.item.EchoItemId;
import dev.echo.standalone.runtime.item.EchoItemRegistry;
import dev.echo.standalone.runtime.item.EchoLootEntry;
import dev.echo.standalone.runtime.item.EchoLootTable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class EchoCompatLootItemBridge {
    public Optional<EchoLootTable> toItemLootTable(EchoLootDefinition loot, EchoItemRegistry itemRegistry) {
        Objects.requireNonNull(loot, "loot");
        Objects.requireNonNull(itemRegistry, "itemRegistry");

        LinkedHashMap<EchoItemId, Integer> entries = new LinkedHashMap<>();
        for (String entry : loot.entries()) {
            Optional<EchoItemId> resolved = entry.startsWith("#")
                    ? resolveTaggedEntry(entry, itemRegistry)
                    : Optional.of(new EchoItemId(entry));
            if (resolved.isEmpty()) {
                return Optional.empty();
            }
            EchoItemId itemId = resolved.orElseThrow();
            if (itemRegistry.find(itemId).isEmpty()) {
                return Optional.empty();
            }
            entries.merge(itemId, 1, Integer::sum);
        }
        if (entries.isEmpty()) {
            return Optional.empty();
        }
        List<EchoLootEntry> itemEntries = entries.entrySet().stream()
                .map(entry -> new EchoLootEntry(entry.getKey(), entry.getValue()))
                .toList();
        return Optional.of(new EchoLootTable(loot.id(), itemEntries));
    }

    public List<EchoLootTable> toItemLootTables(EchoLootRegistry lootRegistry, EchoItemRegistry itemRegistry) {
        Objects.requireNonNull(lootRegistry, "lootRegistry");
        Objects.requireNonNull(itemRegistry, "itemRegistry");
        return lootRegistry.lootTables().stream()
                .map(loot -> toItemLootTable(loot, itemRegistry))
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<EchoItemId> resolveTaggedEntry(String entry, EchoItemRegistry itemRegistry) {
        String tag = entry.substring(1).trim();
        if (tag.isBlank()) {
            return Optional.empty();
        }
        return tagged(itemRegistry, tag).stream()
                .findFirst()
                .or(() -> {
                    int namespaceSeparator = tag.indexOf(':');
                    if (namespaceSeparator < 0 || namespaceSeparator == tag.length() - 1) {
                        return Optional.empty();
                    }
                    return tagged(itemRegistry, tag.substring(namespaceSeparator + 1)).stream().findFirst();
                })
                .map(EchoItemDefinition::id);
    }

    private static List<EchoItemDefinition> tagged(EchoItemRegistry itemRegistry, String tag) {
        return itemRegistry.tagged(tag).stream()
                .sorted(Comparator.comparing(definition -> definition.id().value()))
                .toList();
    }
}
