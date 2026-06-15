package dev.echo.standalone.runtime.world.block.behavior;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockBehaviorContract;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime registry mapping block IDs to {@link EchoBlockBehaviorContract} metadata.
 *
 * <p>Behaviors are data-driven and can be loaded from JSON or registered programmatically. The
 * registry returns a default air-like behavior for unknown block IDs rather than throwing.
 */
public final class EchoBlockBehaviorRegistry {

    private final LinkedHashMap<String, EchoBlockBehaviorContract> behaviors = new LinkedHashMap<>();
    private final EchoBlockBehaviorContract defaultBehavior;
    private boolean frozen;

    public EchoBlockBehaviorRegistry() {
        this(EchoBlockBehavior.air("echo:air"));
    }

    public EchoBlockBehaviorRegistry(EchoBlockBehaviorContract defaultBehavior) {
        this.defaultBehavior = defaultBehavior;
    }

    public void register(EchoBlockBehaviorContract behavior) {
        ensureMutable();
        behaviors.put(behavior.blockId(), behavior);
    }

    public void registerDefaults(String... blockIds) {
        for (String blockId : blockIds) {
            register(EchoBlockBehavior.stone(blockId));
        }
    }

    public EchoBlockBehaviorContract get(String blockId) {
        return behaviors.getOrDefault(blockId, defaultBehavior);
    }

    public Optional<EchoBlockBehaviorContract> find(String blockId) {
        return Optional.ofNullable(behaviors.get(blockId));
    }

    public Map<String, EchoBlockBehaviorContract> behaviors() {
        return Map.copyOf(behaviors);
    }

    public int size() {
        return behaviors.size();
    }

    /**
     * Loads behavior definitions from JSON files in a directory tree.
     *
     * <p>Each {@code .json} file is expected to contain the behavior fields without a block ID;
     * the file name (without extension) is used as the block ID path.
     */
    public void loadDirectory(Path directory) throws IOException {
        ensureMutable();
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (var stream = Files.walk(directory)) {
            List<Path> paths = stream
                    .filter(p -> p.toString().endsWith(".json"))
                    .sorted()
                    .toList();
            for (Path path : paths) {
                String fileName = path.getFileName().toString();
                String idPath = fileName.substring(0, fileName.length() - ".json".length());
                String blockId = idPath.contains(":") ? idPath : idPath.replaceFirst("_", ":");
                try (InputStream in = Files.newInputStream(path)) {
                    register(EchoBlockBehaviorJsonLoader.load(blockId, in));
                }
            }
        }
    }

    public void freeze() {
        frozen = true;
    }

    public boolean frozen() {
        return frozen;
    }

    private void ensureMutable() {
        if (frozen) {
            throw new IllegalStateException("Block behavior registry is frozen");
        }
    }
}
