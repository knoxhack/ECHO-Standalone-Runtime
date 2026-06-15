package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Enum blockstate property backed by a fixed ordered set of string values, e.g. {@code axis},
 * {@code half}, {@code shape}.
 */
public final class EchoBlockPropertyEnum implements EchoBlockPropertyContract<String> {

    private final String name;
    private final List<String> values;
    private final Map<String, String> valueIndex;
    private final String defaultValue;

    public EchoBlockPropertyEnum(String name, String firstValue, String secondValue, String... moreValues) {
        this(name, firstValue, merge(firstValue, secondValue, moreValues));
    }

    public EchoBlockPropertyEnum(String name, String defaultValue, String[] values) {
        this.name = EchoBlockStateIds.requireText(name, "name");
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("values must not be empty");
        }
        LinkedHashMap<String, String> index = new LinkedHashMap<>();
        for (String value : values) {
            String normalized = normalize(value);
            if (index.put(normalized, normalized) != null) {
                throw new IllegalArgumentException("Duplicate enum value '" + value + "' for property " + name);
            }
        }
        this.values = List.copyOf(index.values());
        this.valueIndex = Map.copyOf(index);
        String normalizedDefault = normalize(defaultValue);
        if (!valueIndex.containsKey(normalizedDefault)) {
            throw new IllegalArgumentException(
                    "defaultValue '" + defaultValue + "' is not a valid value for property " + name
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
        if (!valueIndex.containsKey(normalized)) {
            throw new IllegalArgumentException(
                    "Illegal enum value '" + value + "' for property " + name + "; expected one of " + values
            );
        }
        return normalized;
    }

    @Override
    public String serialize(String value) {
        return requireValid(value);
    }

    private static String normalize(String value) {
        return EchoBlockStateIds.requireText(value, "value").trim().toLowerCase(Locale.ROOT);
    }

    private static String[] merge(String first, String second, String[] more) {
        String[] result = new String[2 + (more == null ? 0 : more.length)];
        result[0] = first;
        result[1] = second;
        if (more != null) {
            System.arraycopy(more, 0, result, 2, more.length);
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
