package dev.echo.standalone.runtime.entity;

import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public final class EchoEntitySaveHook {
    private final EchoEntityStore store;

    public EchoEntitySaveHook(EchoEntityStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    public EchoEntitySaveResult save(EchoSaveRuntimeResult saves, String slotId, String transactionId) throws IOException {
        Objects.requireNonNull(saves, "saves");
        EchoSaveTransaction transaction = saves.beginTransaction(slotId, transactionId);
        ArrayList<String> paths = new ArrayList<>();
        transaction.writeText("entities/summary.json", EchoEntityJsonWriter.summary(store));
        paths.add("entities/summary.json");
        for (EchoEntityState entity : store.all()) {
            String path = "entities/" + entity.id().fileSafeKey() + ".json";
            transaction.writeText(path, EchoEntityJsonWriter.entity(entity));
            paths.add(path);
        }
        return new EchoEntitySaveResult(
                transaction.commit(Map.of(
                        "entityCount", Integer.toString(store.count()),
                        "hostileEntities", Integer.toString(store.hostile().size())
                )),
                paths
        );
    }
}
