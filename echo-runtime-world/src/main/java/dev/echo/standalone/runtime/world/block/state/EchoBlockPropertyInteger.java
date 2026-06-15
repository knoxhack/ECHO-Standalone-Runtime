package dev.echo.standalone.runtime.world.block.state;

import dev.echo.standalone.runtime.contracts.voxel.EchoBlockPropertyContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Integer blockstate property with a contiguous inclusive range, e.g. {@code age}, {@code level}.
 */
public final class EchoBlockPropertyInteger implements EchoBlockPropertyContract<Integer> {

    private final String name;
    private final int min;
    private final int max;
    private final int defaultValue;
    private final List<Integer> values;

    public EchoBlockPropertyInteger(String name, int min, int max) {
        this(name, min, max, min);
    }

    public EchoBlockPropertyInteger(String name, int min, int max, int defaultValue) {
        this.name = EchoBlockStateIds.requireText(name, "name");
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max");
        }
        if (defaultValue < min || defaultValue > max) {
            throw new IllegalArgumentException("defaultValue must be within [min, max]");
        }
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        ArrayList<Integer> list = new ArrayList<>(max - min + 1);
        for (int value = min; value <= max; value++) {
            list.add(value);
        }
        this.values = List.copyOf(list);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<Integer> type() {
        return Integer.class;
    }

    @Override
    public Collection<Integer> values() {
        return values;
    }

    @Override
    public Integer defaultValue() {
        return defaultValue;
    }

    public int min() {
        return min;
    }

    public int max() {
        return max;
    }

    @Override
    public Integer parse(String value) {
        Objects.requireNonNull(value, "value");
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < min || parsed > max) {
                throw new IllegalArgumentException(
                        "Value " + parsed + " out of range [" + min + ", " + max + "] for property " + name
                );
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value '" + value + "' for property " + name, e);
        }
    }

    @Override
    public String serialize(Integer value) {
        return requireValid(value).toString();
    }

    @Override
    public String toString() {
        return name;
    }
}
