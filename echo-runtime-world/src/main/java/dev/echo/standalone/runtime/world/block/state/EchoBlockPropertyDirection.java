package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Direction blockstate property, e.g. {@code facing}, {@code facing_all}, {@code facing_horizontal}.
 */
public final class EchoBlockPropertyDirection implements EchoBlockPropertyContract<String> {

    public static final List<String> SIX = List.of("down", "up", "north", "south", "west", "east");
    public static final List<String> HORIZONTAL = List.of("north", "south", "west", "east");
    public static final List<String> VERTICAL = List.of("up", "down");

    private final String name;
    private final List<String> values;
    private final String defaultValue;

    public EchoBlockPropertyDirection(String name) {
        this(name, SIX);
    }

    public EchoBlockPropertyDirection(String name, Collection<String> values) {
        this(name, values, values.iterator().next());
    }

    public EchoBlockPropertyDirection(String name, Collection<String> values, String defaultValue) {
        this.name = EchoBlockStateIds.requireText(name, "name");
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }
        for (String value : values) {
            validateDirection(value);
        }
        this.values = List.copyOf(values);
        String normalizedDefault = normalize(defaultValue);
        if (!this.values.contains(normalizedDefault)) {
            throw new IllegalArgumentException(
                    "defaultValue '" + defaultValue + "' is not valid for property " + name
            );
        }
        this.defaultValue = normalizedDefault;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<String> type() {
        return String.class;
    }

    @Override
    public Collection<String> values() {
        return values;
    }

    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @Override
    public String parse(String value) {
        Objects.requireNonNull(value, "value");
        String normalized = normalize(value);
        validateDirection(normalized);
        if (!values.contains(normalized)) {
            throw new IllegalArgumentException(
                    "Direction '" + value + "' is not allowed for property " + name + "; expected one of " + values
            );
        }
        return normalized;
    }

    @Override
    public String serialize(String value) {
        return requireValid(value);
    }

    private static void validateDirection(String value) {
        String normalized = normalize(value);
        if (!SIX.contains(normalized) && !"none".equals(normalized)) {
            throw new IllegalArgumentException("Invalid direction value '" + value + "'");
        }
    }

    private static String normalize(String value) {
        return EchoBlockStateIds.requireText(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return name;
    }
}
