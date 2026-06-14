package dev.echo.standalone.runtime.contracts.voxel;

import java.util.Collection;
import java.util.Objects;

/**
 * Contract for a typed blockstate property such as {@code axis}, {@code facing}, or {@code powered}.
 *
 * <p>Properties are immutable, comparable by name, and expose all legal values. Implementations
 * must validate that values assigned to a state are members of {@link #values()}.
 *
 * @param <T> the property value type: {@link Boolean}, {@link Integer}, or an enum-like type
 */
public interface EchoBlockPropertyContract<T> {

    /**
     * Returns the property name as it appears in blockstate JSON, e.g. {@code "facing"}.
     */
    String name();

    /**
     * Returns the value type class.
     */
    Class<T> type();

    /**
     * Returns the set of legal values for this property, in the canonical order used for
     * stable state-id generation.
     */
    Collection<T> values();

    /**
     * Returns the default value used when a blockstate omits this property.
     */
    T defaultValue();

    /**
     * Parses a string value into the property type. Throws {@link IllegalArgumentException} for
     * unknown values.
     */
    T parse(String value);

    /**
     * Serializes a value to its canonical string representation.
     */
    String serialize(T value);

    /**
     * Validates that the supplied value is a legal value for this property.
     */
    default boolean valid(T value) {
        return values().contains(value);
    }

    default T requireValid(T value) {
        Objects.requireNonNull(value, "value");
        if (!valid(value)) {
            throw new IllegalArgumentException(
                    "Illegal value " + serialize(value) + " for property " + name()
            );
        }
        return value;
    }
}
