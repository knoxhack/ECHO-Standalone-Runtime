package dev.echo.standalone.runtime.world.openlands;

import dev.echo.standalone.runtime.world.block.state.EchoBlockRegistry;

import java.util.List;
import java.util.Objects;

/**
 * Bootstraps Openlands blocks into an {@link EchoBlockRegistry} from loaded module definitions.
 */
public final class EchoOpenlandsBlocks {

    private EchoOpenlandsBlocks() {
    }

    public static void registerAll(EchoBlockRegistry registry, List<EchoOpenlandsBlockDefinition> blocks) {
        Objects.requireNonNull(registry, "registry");
        Objects.requireNonNull(blocks, "blocks");
        for (EchoOpenlandsBlockDefinition block : blocks) {
            registry.register(block.id(), block.displayName());
        }
    }
}
