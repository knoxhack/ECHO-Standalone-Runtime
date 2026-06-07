package dev.echo.standalone.runtime.item;

import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public final class EchoItemSaveHook {
    private final EchoItemRegistry registry;
    private final EchoInventoryStore store;

    public EchoItemSaveHook(EchoItemRegistry registry, EchoInventoryStore store) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.store = Objects.requireNonNull(store, "store");
    }

    public EchoItemSaveResult save(EchoSaveRuntimeResult saves, String slotId, String transactionId) throws IOException {
        Objects.requireNonNull(saves, "saves");
        EchoSaveTransaction transaction = saves.beginTransaction(slotId, transactionId);
        ArrayList<String> paths = new ArrayList<>();
        transaction.writeText("items/summary.json", EchoItemJsonWriter.summary(registry, store));
        paths.add("items/summary.json");
        for (EchoInventoryContainer container : store.all()) {
            String path = "items/inventories/" + container.id().fileSafeKey() + ".json";
            transaction.writeText(path, EchoItemJsonWriter.container(container));
            paths.add(path);
        }
        return new EchoItemSaveResult(
                transaction.commit(Map.of(
                        "itemDefinitions", Integer.toString(registry.count()),
                        "inventories", Integer.toString(store.count())
                )),
                paths
        );
    }
}
