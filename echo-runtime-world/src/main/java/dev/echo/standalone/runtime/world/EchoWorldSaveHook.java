package dev.echo.standalone.runtime.world;

import dev.echo.standalone.runtime.save.EchoSaveRuntimeResult;
import dev.echo.standalone.runtime.save.EchoSaveTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public final class EchoWorldSaveHook {
    private final EchoWorldState world;

    public EchoWorldSaveHook(EchoWorldState world) {
        this.world = Objects.requireNonNull(world, "world");
    }

    public EchoWorldSaveResult save(EchoSaveRuntimeResult saves, String slotId, String transactionId) throws IOException {
        Objects.requireNonNull(saves, "saves");
        EchoSaveTransaction transaction = saves.beginTransaction(slotId, transactionId);
        ArrayList<String> paths = new ArrayList<>();
        transaction.writeText("world/summary.json", EchoWorldJsonWriter.summary(world));
        paths.add("world/summary.json");
        for (EchoWorldChunk chunk : world.chunks()) {
            String path = "world/chunks/" + chunk.id().fileSafeKey() + ".json";
            transaction.writeText(path, EchoWorldJsonWriter.chunk(chunk));
            paths.add(path);
        }
        return new EchoWorldSaveResult(
                transaction.commit(Map.of("worldId", world.worldId(), "worldTick", Long.toString(world.tick()))),
                paths
        );
    }
}
