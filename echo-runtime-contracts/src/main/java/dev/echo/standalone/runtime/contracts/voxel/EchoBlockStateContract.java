package dev.echo.standalone.runtime.contracts.voxel;

import java.util.Collection;
import java.util.Optional;

/**
 * Contract for an immutable blockstate: a block plus a specific combination of property values.
 *
 * <p>Implementations must guarantee value stability and must derive {@link #stableId()} from the
 * owning block's property table so that identical states always have identical IDs within a
 * runtime session.
 */
public interface EchoBlockStateContract {

    /**
     * Returns the block this state belongs to.
     */
    EchoBlockContract block();

    /**
     * Returns all properties defined by {@link #block()}.
     */
    default Collection<? extends EchoBlockPropertyContract<?>> properties() {
        return block().properties();
    }

    /**
     * Returns the value for the given property.
     */
    <T> T value(EchoBlockPropertyContract<T> property);

    /**
     * Returns the string value for a property by name, if present.
     */
    default Optional<String> value(String name) {
        for (EchoBlockPropertyContract<?> property : properties()) {
            if (property.name().equals(name)) {
                return Optional.of(property.serialize(value(property)));
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a new state with the given property set to {@code value}.
     */
    <T> EchoBlockStateContract with(EchoBlockPropertyContract<T> property, T value);

    /**
     * Returns the stable numeric state ID for this state within the current runtime.
     */
    int stableId();

    /**
     * Returns {@code true} if this state represents air.
     */
    default boolean air() {
        return block().air();
    }
}
