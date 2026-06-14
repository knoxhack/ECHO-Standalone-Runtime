package dev.echo.standalone.runtime.contracts.voxel;

import java.util.Collection;
import java.util.Optional;

/**
 * Contract for a block type. A block owns a default state and an ordered set of properties that
 * define all possible states.
 *
 * <p>This interface intentionally does not expose rendering, collision, or behavior metadata.
 * Those are provided by registries in implementation modules so that the contracts layer stays
 * free of Minecraft, NeoForge, and LWJGL dependencies.
 */
public interface EchoBlockContract {

    /**
     * Returns the namespaced block ID, e.g. {@code "minecraft:stone"}.
     */
    String id();

    /**
     * Returns the human-readable display name.
     */
    String displayName();

    /**
     * Returns {@code true} if this block is air.
     */
    boolean air();

    /**
     * Returns the ordered properties that define this block's states.
     */
    Collection<? extends EchoBlockPropertyContract<?>> properties();

    /**
     * Returns the default state (all properties at default values).
     */
    EchoBlockStateContract defaultState();

    /**
     * Finds a property by name.
     */
    default Optional<? extends EchoBlockPropertyContract<?>> property(String name) {
        for (EchoBlockPropertyContract<?> property : properties()) {
            if (property.name().equals(name)) {
                return Optional.of(property);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the total number of possible states for this block.
     */
    default int stateCount() {
        int count = 1;
        for (EchoBlockPropertyContract<?> property : properties()) {
            count *= property.values().size();
        }
        return count;
    }
}
